package com.skcc.cloudz.zcp.namespace.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.json.simple.parser.ParseException;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.internal.LinkedTreeMap;
import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.vo.KubeDeleteOptionsVO;
import com.skcc.cloudz.zcp.common.vo.RoleBindingVO;
import com.skcc.cloudz.zcp.common.vo.ServiceAccountVO;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;
import com.skcc.cloudz.zcp.user.dao.UserKeycloakDao;
import com.skcc.cloudz.zcp.user.dao.UserKubeDao;
import com.skcc.cloudz.zcp.user.vo.MemberVO;
import com.skcc.cloudz.zcp.user.vo.UserVO;

import ch.qos.logback.classic.Logger;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRole;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceSpec;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ResourceQuota;
import io.kubernetes.client.models.V1RoleRef;
import io.kubernetes.client.models.V1Subject;

@Service
public class NamespaceService {

	private final Logger LOG = (Logger) LoggerFactory.getLogger(NamespaceService.class);
	
	@Autowired
	UserKeycloakDao keycloakDao;
	
	@Autowired
	UserKubeDao kubeDao;
	
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
	
	public void deleteUser(String  userName) throws ZcpException, ApiException {
		//1.service account 삭제
		V1DeleteOptions deleteOption = new V1DeleteOptions();
		try {
			kubeDao.deleteServiceAccount(serviceAccountPrefix + userName, systemNamespace, deleteOption);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}

		//2.cluster role binding 삭제
		try {
			kubeDao.deleteClusterRoleBinding(serviceAccountPrefix + userName, deleteOption);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}
		
		keycloakDao.deleteUser(userName);
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
		HashMap data = new HashMap();
		LinkedTreeMap clusterrolebinding =  getClusterRoleBinding(username);
		if(clusterrolebinding == null) return data;
		String namespace = ((List<LinkedTreeMap>)clusterrolebinding.get("subjects")).get(0).get("namespace").toString();
		LinkedTreeMap mapNamespace = (LinkedTreeMap) kubeDao.namespaceList(namespace);
		
		
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
		try {
			return serviceAccount.findAny().get();
		}catch(NoSuchElementException e) {
			return null;
		}finally {
			return null;		
		}
	
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
		
		try {
			return (LinkedTreeMap) kubeDao.namespaceList(namespace);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}
		return null;
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
	public void createAndEditNamespace(NamespaceVO data) throws ApiException {
		V1ObjectMeta namespace_meta = new V1ObjectMeta();
		V1ObjectMeta quota_meta = new V1ObjectMeta();
		V1ObjectMeta limit_meta = new V1ObjectMeta();
		
		namespace_meta.setName(data.getNamespace());
		quota_meta.setName(data.getNamespace());
		limit_meta.setName(data.getNamespace());
		
		V1Namespace namespacevo = new V1Namespace();
		V1ResourceQuota quotavo = data.getResourceQuota();
		V1LimitRange limitvo = data.getLimitRange();
		namespacevo.setApiVersion("v1");
		namespacevo.setKind("Namespace");
		namespacevo.setSpec(new V1NamespaceSpec().addFinalizersItem("kubernetes"));
		namespacevo.setMetadata(namespace_meta);
		quotavo.setApiVersion("v1");
		quotavo.setKind("ResourceQuota");
		quotavo.setMetadata(quota_meta);
		limitvo.setApiVersion("v1");
		limitvo.setKind("LimitRange");
		limitvo.setMetadata(quota_meta);
		
		String namespace = data.getNamespace();
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
	
	public void initUserPassword(Map<String, Object> password) throws ZcpException {
		keycloakDao.initUserPassword(password);
	}
	
	public void removeOtpPassword(MemberVO vo) {
		keycloakDao.removeOtpPassword(vo);
	}
}
