package com.skcc.cloudz.zcp.member.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.json.simple.parser.ParseException;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.internal.LinkedTreeMap;
import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.member.dao.MemberKeycloakDao;
import com.skcc.cloudz.zcp.member.dao.MemberKubeDao;
import com.skcc.cloudz.zcp.member.vo.KubeDeleteOptionsVO;
import com.skcc.cloudz.zcp.member.vo.MemberVO;
import com.skcc.cloudz.zcp.member.vo.RoleBindingVO;
import com.skcc.cloudz.zcp.member.vo.ServiceAccountVO;
import com.skcc.cloudz.zcp.member.vo.UserVO;

import ch.qos.logback.classic.Logger;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRole;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ResourceQuota;
import io.kubernetes.client.models.V1RoleRef;
import io.kubernetes.client.models.V1Subject;

@Service
public class MemberService {

	private final Logger LOG = (Logger) LoggerFactory.getLogger(MemberService.class);
	
	@Autowired
	MemberKeycloakDao keycloakDao;
	
	@Autowired
	MemberKubeDao kubeDao;
	
	@Value("${kube.cluster.role.binding.prefix}")
	String clusterRoleBindingPrefix;
	
	@Value("${kube.role.binding.prefix}")
	String roleBindingPrefix;
	
	@Value("${kube.service.account.prefix}")
	String serviceAccountPrefix;
	
	@Value("${kube.system.namespace}")
	String systemNamespace;
	
	public Object getUserList() throws ApiException {
		List<UserRepresentation> keyCloakUser = keycloakDao.getUserList();
		
		List<UserVO> userList = new ArrayList();
		for(UserRepresentation cloakUser : keyCloakUser) {
			UserVO user = new UserVO();
			user.setUserId(cloakUser.getUsername());
			user.setEmail(cloakUser.getEmail());
			user.setName(cloakUser.getLastName() + cloakUser.getFirstName());
			user.setDate(new Timestamp(cloakUser.getCreatedTimestamp()).toString());
			user.setClusterRole(cloakUser.getAttributes().get("clusterRole").toString());
			user.setStatus(cloakUser.isEnabled());
			userList.add(user);
		}
		
		for(UserVO user : userList) {
			LinkedTreeMap rolebinding =null;
			rolebinding = kubeDao.RoleBindingListOfUser(user.getUserId());
			int count = ((List<LinkedTreeMap>)rolebinding.get("items")).size();
			user.setUsedNamespace(count);
		}
		
		return userList;
		
	}
	
	public Object getUserList(String namespace) throws ApiException {
		List<UserRepresentation> keyCloakUser = keycloakDao.getUserList();
		
		List<UserVO> userList = new ArrayList();
		LinkedTreeMap rolebinding = kubeDao.RoleBindingListOfNamespace(namespace);
		List<LinkedTreeMap> bindingUsers = (List<LinkedTreeMap>)rolebinding.get("items");
		for(LinkedTreeMap binding : bindingUsers) {
			String name = (String)((LinkedTreeMap)binding.get("metadata")).get("name") ;
			for(UserRepresentation cloakUser : keyCloakUser) {
				if(name.equals(this.roleBindingPrefix + cloakUser.getUsername())) {
					UserVO user = new UserVO();
					user.setUserId(cloakUser.getUsername());
					user.setEmail(cloakUser.getEmail());
					user.setName(cloakUser.getLastName() + cloakUser.getFirstName());
					user.setDate(new Timestamp(cloakUser.getCreatedTimestamp()).toString());
					user.setClusterRole(cloakUser.getAttributes().get("clusterRole").toString());
					user.setStatus(cloakUser.isEnabled());
					userList.add(user);	
				}
			}	
		}
		
		for(UserVO user : userList) {
			LinkedTreeMap mapUser = kubeDao.RoleBindingListOfUser(user.getUserId());
			int count = ((List<LinkedTreeMap>)mapUser.get("items")).size();
			user.setUsedNamespace(count);
		}
		
		return userList;
		
	}
	
	public void editUser(MemberVO vo) throws ZcpException{
		keycloakDao.editUser(vo);
	}
	
	public void editUserPassword(MemberVO vo) throws ZcpException{
		keycloakDao.editUserPassword(vo);
	}
	
	public void deleteUser(MemberVO vo) throws ZcpException, ApiException {
		//1.service account 삭제
		V1DeleteOptions deleteOption = new V1DeleteOptions();
		try {
			kubeDao.deleteServiceAccount(serviceAccountPrefix + vo.getUserName(), systemNamespace, deleteOption);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}

		//2.cluster role binding 삭제
		try {
			kubeDao.deleteClusterRoleBinding(serviceAccountPrefix + vo.getUserName(), deleteOption);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}
		
		keycloakDao.deleteUser(vo);
	}
	
	public void createUser(MemberVO vo) throws ApiException {
		//1. service account 생성
		ServiceAccountVO data = new ServiceAccountVO();
		data.setNamespace(systemNamespace);
		data.setApiVersion("v1");
		data.setKind("ServiceAccount");
		V1ObjectMeta smetadata = new V1ObjectMeta();
		smetadata.setName(serviceAccountPrefix + vo.getUserName());
		data.setMetadata(smetadata);
		this.createAndEditServiceAccount(serviceAccountPrefix + vo.getUserName(), systemNamespace, data);
		
		//2. clusterRolebindinding 생성
		V1ClusterRoleBinding binding = new V1ClusterRoleBinding();
		V1ObjectMeta cmetadata = new V1ObjectMeta();
		List<V1Subject> subjects = new ArrayList();
		V1RoleRef roleRef = new V1RoleRef();
		V1Subject subject = new V1Subject();
		subject.setKind("ServiceAccount");
		subject.setName(serviceAccountPrefix + vo.getUserName());
		subject.setNamespace(systemNamespace);
		roleRef.setApiGroup("rbac.authorization.k8s.io");
		roleRef.setKind("ClusterRole");
		roleRef.setName(vo.getAttribute().get("clusterRole").toString());
		cmetadata.setName(clusterRoleBindingPrefix + vo.getUserName());
		binding.setApiVersion("rbac.authorization.k8s.io/v1");
		binding.setKind("ClusterRoleBinding");
		subjects.add(subject);
		binding.setSubjects(subjects);
		binding.setRoleRef(roleRef);
		binding.setMetadata(cmetadata);
		this.createAndEditClusterRoleBinding(vo.getUserName(), binding);
		
		keycloakDao.createUser(vo);
	}
	
//	public ClusterRole getClusterRoles() throws IOException, ApiException{
//	    return KubeDao.getClusterRoles();
//	}
//
//	public ClusterRoleBinding clusterrolebindings() throws IOException, ApiException{
//		return KubeDao.clusteRroleBindings();
//	}
	
//	public ServiceAccount serviceAccount() throws IOException, ApiException{
//		return KubeDao.serviceAccount();
//	}
	
	/**
	 * @return
	 * @throws ApiException
	 * @throws ParseException
	 * 
	 * 사용자 로그인시 namespace 정보와 clusterbinding 정보를 가져옴
	 * 
	 */
	public Map getUserInfo(String username) throws ApiException, ParseException{
		LinkedTreeMap clusterrolebinding =  getClusterRoleBinding(username);
		String namespace = ((List<LinkedTreeMap>)clusterrolebinding.get("subjects")).get(0).get("namespace").toString();
		LinkedTreeMap mapNamespace = (LinkedTreeMap) kubeDao.namespaceList(namespace);
		
		HashMap data = new HashMap();
		data.put("clusterrolebinding", clusterrolebinding);
		data.put("namespace", mapNamespace);
        
        return data;
		
	}
	
//	private String getNamespace(LinkedTreeMap clusterrolebinding) {
//		return ((List<LinkedTreeMap>)clusterrolebinding.get("subjects")).get(0).get("namespace").toString();
//	}
	
	/**
	 * 사용자 이름에 따른 clusterrolebinding 값
	 * @param username
	 * @return
	 * @throws ApiException
	 */
	public LinkedTreeMap getClusterRoleBinding(String username) throws ApiException{
		
		LinkedTreeMap map = (LinkedTreeMap) kubeDao.clusterRoleBindingList();
		List<LinkedTreeMap> items= (List<LinkedTreeMap>)map.get("items");
		Stream<LinkedTreeMap> serviceAccount = items.stream().filter((srvAcc) -> { 
			List<LinkedTreeMap> subjects = (List<LinkedTreeMap>) ((LinkedTreeMap)srvAcc).get("subjects");
			if(subjects != null) {
				Stream s = subjects.stream().filter((subject) -> {
					LOG.debug("kind={} , name={}", subject.get("kind"), subject.get("name"));
					return subject.get("kind").toString().equals("ServiceAccount") 
							&& subject.get("name").toString().equals(clusterRoleBindingPrefix+ username);
				});
				if(s != null)
					return s.count()>0;
				else return false;
			}
			return false;
		});
		
		return serviceAccount.findAny().get();
		
	}
	
	
	/**
	 * @return
	 * @throws ApiException
	 * @throws ParseException
	 * 
	 * 네임 스페이스 정보
	 * 
	 */
	public LinkedTreeMap getNamespace(String namespace) throws ApiException, ParseException{
		return (LinkedTreeMap) kubeDao.namespaceList(namespace);
	}
	
	/**
	 * @return
	 * @throws ApiException
	 * @throws ParseException
	 * 
	 * 네임 스페이스 정보
	 * 
	 */
	public Map getNamespaceResource(String namespace) throws ApiException, ParseException{
		Map resource = new HashMap(); 
		Map quota =  (LinkedTreeMap) kubeDao.getQuota(namespace);
		Map limitRanges =  (LinkedTreeMap) kubeDao.getLimitRanges(namespace);
		resource.put("resourceQuota", quota);
		resource.put("limitRanges", limitRanges);
		
		return resource;
		
	}
	
	/**
	 * 전체 네임스페이스
	 * @param namespace
	 * @return
	 * @throws ApiException
	 * @throws ParseException
	 */
	public List<Map> getAllOfNamespace() throws ApiException, ParseException{
		List<Map> namespaceList = new ArrayList();
		LinkedTreeMap map =  kubeDao.namespaceList("");
		List<LinkedTreeMap> item = (List<LinkedTreeMap>) map.get("items");
		item.stream().forEach((data) ->{
			String name = ((LinkedTreeMap)data.get("metadata")).get("name").toString();
			Map<String, String> mapNamespace = new HashMap();
			mapNamespace.put("name", name);
			namespaceList.add(mapNamespace);
		});
		return namespaceList;
	}
	
	
	/**
	 * 네임스페이스 생성 또는 변경
	 * @param namespacevo
	 * @param quotavo
	 * @param limitvo
	 * @throws ApiException
	 */
	public void createAndEditNamespace(V1Namespace namespacevo, V1ResourceQuota quotavo, V1LimitRange limitvo) throws ApiException {
		String namespace = namespacevo.getMetadata().getName();
		try {
			kubeDao.createNamespace(namespace, namespacevo);
		} catch (ApiException e) {
			if(e.getMessage().equals("Conflict")) {
				kubeDao.editNamespace(namespace, namespacevo);
			}else {
				throw e;	
			}
		}
		
		try {
			kubeDao.createLimitRanges(namespace, limitvo);
		} catch (ApiException e) {
			if(e.getMessage().equals("Conflict")) {
				String name = limitvo.getMetadata().getName();
				kubeDao.editLimitRanges(namespace, name, limitvo);
			}else {
				throw e;	
			}
		}
		
		try {
			kubeDao.createQuota(namespace, quotavo);
		} catch (ApiException e) {
			if(e.getMessage().equals("Conflict")) {
				String name = limitvo.getMetadata().getName();
				kubeDao.editQuota(namespace, name, quotavo);
			}else {
				throw e;	
			}
		}
	}
	
	
	/**
	 * 클러스터 조회
	 * @return
	 * @throws ApiException
	 */
	public List clusterRoleList() throws ApiException{
		List<Map> clusterRoleNameList = new ArrayList();
		kubeDao.clusterRoleList().getItems().stream().forEach((data) -> {
			Map<String, String> clusterRoleName = new HashMap();
			clusterRoleName.put("name", data.getMetadata().getName());
			clusterRoleNameList.add(clusterRoleName);
		});
		
		return clusterRoleNameList;
	}
	
	public void giveClusterRole(MemberVO vo) throws ApiException, ZcpException {
		//keycloakDao.
		
		//1. clusterRolebindinding 생성
		V1ClusterRoleBinding binding = new V1ClusterRoleBinding();
		V1ObjectMeta cmetadata = new V1ObjectMeta();
		List<V1Subject> subjects = new ArrayList();
		V1RoleRef roleRef = new V1RoleRef();
		V1Subject subject = new V1Subject();
		subject.setKind("ServiceAccount");
		subject.setName(serviceAccountPrefix + vo.getUserName());
		subject.setNamespace(systemNamespace);
		roleRef.setApiGroup("rbac.authorization.k8s.io");
		roleRef.setKind("ClusterRole");
		roleRef.setName(vo.getAttribute().get("clusterRole").toString());
		cmetadata.setName(clusterRoleBindingPrefix + vo.getUserName());
		binding.setApiVersion("rbac.authorization.k8s.io/v1");
		binding.setKind("ClusterRoleBinding");
		subjects.add(subject);
		binding.setSubjects(subjects);
		binding.setRoleRef(roleRef);
		binding.setMetadata(cmetadata);
		this.createAndEditClusterRoleBinding(vo.getUserName(), binding);
		
		keycloakDao.editAttribute(vo);
		
	}
	
	public LinkedTreeMap serviceAccountList(String namesapce, String username) throws IOException, ApiException{
		LinkedTreeMap map = kubeDao.serviceAccountList(namesapce);
		List<LinkedTreeMap> c = (List<LinkedTreeMap>)map.values().toArray()[3];
		List<String> serviceAccountList = new ArrayList();
		for(LinkedTreeMap data : c) {
			LinkedTreeMap metadata =(LinkedTreeMap)data.get("metadata");
			if(metadata.get("name").equals(clusterRoleBindingPrefix + username)){
				return map;
			}	
		}
		return null;
	}
	
	
	public LinkedTreeMap getServiceAccount(String namespace, String username) throws IOException, ApiException{
		return kubeDao.getServiceAccount(namespace, username);
	}
	
	
	public String getServiceAccountToken(String namespace, String username) throws IOException, ApiException{
		List<LinkedTreeMap> secrets =(List<LinkedTreeMap>) kubeDao.getServiceAccount(namespace, username).get("secrets");
		for(LinkedTreeMap secret : secrets) {
			String secretName = secret.get("name").toString();
			LinkedTreeMap secretList = (LinkedTreeMap) kubeDao.getSecret(namespace, secretName).get("data");
			
			return secretList.get("token").toString();
		}
		return null;
	}

	
	
	
	
	public void createServiceAccount(ServiceAccountVO data) throws IOException, ApiException{
		LinkedTreeMap c = kubeDao.createServiceAccount(data.getNamespace(), data);
	}
	
	
	public void deleteClusterRoleBinding(KubeDeleteOptionsVO data) throws IOException, ApiException{
		LinkedTreeMap status = kubeDao.deleteClusterRoleBinding(data.getName(), data);
	}
	
	public void createClusterRole(V1ClusterRole data) throws IOException, ApiException{
		LinkedTreeMap c = kubeDao.createClusterRole(data);
	}
	
	public void createRoleBinding(RoleBindingVO binding) throws IOException, ApiException{
		Map<String, String> labels = new HashMap();
		labels.put("zcp-system-user", "true");
		labels.put("zcp-system-username", binding.getUserName());
		
		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(roleBindingPrefix + binding.getUserName());
		metadata.setLabels(labels);
		metadata.setNamespace(binding.getNamespace());
		
		V1Subject subject = new V1Subject();
		subject.setKind("ServiceAccount");
		subject.setName(serviceAccountPrefix + binding.getUserName());
		subject.setNamespace(systemNamespace);
		
		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup("rbac.authorization.k8s.io");
		roleRef.setKind("ClusterRole");
		roleRef.setName(binding.getClusterRole());
		
		List<V1Subject> subjects = new ArrayList();

		binding.setApiVersion("rbac.authorization.k8s.io/v1");
		binding.setKind("RoleBinding");
		binding.setSubjects(subjects);
		binding.setRoleRef(roleRef);
		binding.setMetadata(metadata);
		
		subjects.add(subject);
		
		try {
			kubeDao.createRoleBinding(binding.getNamespace(), binding);
		} catch (ApiException e) {
			if(e.getMessage().equals("Conflict")) {
				
			}else {
				throw e;	
			}
		}
		
	}
	
	public void deleteRoleBinding(KubeDeleteOptionsVO data) throws IOException, ApiException{
		try {
			LinkedTreeMap status = kubeDao.deleteRoleBinding(data.getNamespace(), roleBindingPrefix + data.getUserName() , data);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}
		
	}
	

	public void  createAndEditServiceAccount(String name, String namespace, ServiceAccountVO vo) throws ApiException {
		try {
			kubeDao.createServiceAccount(vo.getNamespace(), vo);
		} catch (ApiException e) {
			if(e.getMessage().equals("Conflict")) {
				kubeDao.editServiceAccount(name, vo.getNamespace(), vo);
			}else {
				throw e;	
			}
		}
	}
	
	public void createAndEditClusterRoleBinding(String username, V1ClusterRoleBinding clusterRoleBinding) throws ApiException {
		try {
			kubeDao.createClusterRoleBinding( clusterRoleBinding, username);
		} catch (ApiException e) {
			if(e.getMessage().equals("Conflict")) {
				kubeDao.editClusterRoleBinding(clusterRoleBinding.getMetadata().getName(), clusterRoleBinding, username);
			}else {
				throw e;	
			}
		}
	}
	
}
