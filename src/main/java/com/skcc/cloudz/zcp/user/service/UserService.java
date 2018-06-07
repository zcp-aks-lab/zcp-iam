package com.skcc.cloudz.zcp.user.service;

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

import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.util.Util;
import com.skcc.cloudz.zcp.manager.KeycloakManager;
import com.skcc.cloudz.zcp.manager.KubeAuthManager;
import com.skcc.cloudz.zcp.manager.KubeCoreManager;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;
import com.skcc.cloudz.zcp.user.vo.LoginInfoVO;
import com.skcc.cloudz.zcp.user.vo.MemberVO;
import com.skcc.cloudz.zcp.user.vo.PassResetVO;
import com.skcc.cloudz.zcp.user.vo.ServiceAccountVO;
import com.skcc.cloudz.zcp.user.vo.UserVO;

import ch.qos.logback.classic.Logger;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ObjectReference;
import io.kubernetes.client.models.V1ResourceQuota;
import io.kubernetes.client.models.V1RoleBinding;
import io.kubernetes.client.models.V1RoleBindingList;
import io.kubernetes.client.models.V1RoleRef;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.models.V1Subject;

@Service
public class UserService {

	private final Logger LOG = (Logger) LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	KeycloakManager keycloakDao;
	
	@Autowired
	KubeCoreManager CoreMng;
	
	@Autowired
	KubeAuthManager AuthMng;
	
//	@Autowired
//	NamespaceService nmSrv;
	
	
	@Value("${kube.cluster.role.binding.prefix}")
	String clusterRoleBindingPrefix;
	
	@Value("${kube.role.binding.prefix}")
	String roleBindingPrefix;
	
	@Value("${kube.service.account.prefix}")
	String serviceAccountPrefix;
	
	@Value("${kube.system.namespace}")
	String systemNamespace;
	
	public List<UserVO> getUserList() throws ApiException {
		List<UserRepresentation> keyCloakUser = keycloakDao.getUserList();
		
		List<UserVO> userList = new ArrayList();
		for(UserRepresentation cloakUser : keyCloakUser) {
			UserVO user = new UserVO();
			user.setUserId(cloakUser.getUsername());
			user.setEmail(cloakUser.getEmail());
			user.setName(cloakUser.getLastName() + cloakUser.getFirstName());
			user.setDate(new Timestamp(cloakUser.getCreatedTimestamp()).toString());
			//user.setClusterRole(cloakUser.getAttributes().get("clusterRole").toString());
			user.setStatus(cloakUser.isEnabled());
			userList.add(user);
		}
		
		for(UserVO user : userList) {
			V1RoleBindingList  rolebinding =null;
			rolebinding = AuthMng.RoleBindingListOfUser(user.getUserId());
			int count = rolebinding.getItems().size();
			user.setUsedNamespace(count);
		}
		
		return userList;
		
	}
	
	public List<UserVO> getUserList(String namespace) throws ApiException {
		List<UserRepresentation> keyCloakUser = keycloakDao.getUserList();
		
		List<UserVO> userList = new ArrayList();
		V1RoleBindingList rolebinding = AuthMng.RoleBindingListOfNamespace(namespace);
		List<V1RoleBinding> bindingUsers = rolebinding.getItems();
		for(V1RoleBinding binding : bindingUsers) {
			String name = binding.getMetadata().getName();
			for(UserRepresentation cloakUser : keyCloakUser) {
				if(name.equals(this.roleBindingPrefix + cloakUser.getUsername())) {
					UserVO user = new UserVO();
					user.setUserId(cloakUser.getUsername());
					user.setEmail(cloakUser.getEmail());
					user.setName(cloakUser.getLastName() + cloakUser.getFirstName());
					user.setDate(new Timestamp(cloakUser.getCreatedTimestamp()).toString());
					//user.setClusterRole(cloakUser.getAttributes().get("clusterRole").toString());
					user.setStatus(cloakUser.isEnabled());
					userList.add(user);	
				}
			}	
		}
		
		for(UserVO user : userList) {
			V1RoleBindingList mapUser = AuthMng.RoleBindingListOfUser(user.getUserId());
			int count = mapUser.getItems().size();
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
			CoreMng.deleteServiceAccount(serviceAccountPrefix + userName, systemNamespace, deleteOption);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}

		//2.cluster role binding 삭제
		try {
			AuthMng.deleteClusterRoleBinding(serviceAccountPrefix + userName, deleteOption);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}
		
		keycloakDao.deleteUser(userName);
	}
	
	public void createUser(MemberVO vo) throws ApiException {
		//1. service account 생성
		createServiceAccount(vo);
		
		//2. clusterRolebindinding 생성
		V1ClusterRoleBinding binding = createClusterRoleBinding(vo);
		this.createAndEditClusterRoleBinding(vo.getUserName(), binding);
		
		keycloakDao.createUser(vo);
	}
	
	private V1ServiceAccount createServiceAccount(MemberVO vo) throws ApiException {
		ServiceAccountVO data = new ServiceAccountVO();
		data.setNamespace(systemNamespace);
		data.setApiVersion("v1");
		data.setKind("ServiceAccount");
		V1ObjectMeta smetadata = new V1ObjectMeta();
		smetadata.setName(serviceAccountPrefix + vo.getUserName());
		data.setMetadata(smetadata);
		
		return createAndEditServiceAccount(serviceAccountPrefix + vo.getUserName(), systemNamespace, data);
		
	}
	
	/**
	 * @return
	 * @throws ApiException
	 * @throws ParseException
	 * 
	 * 사용자 로그인시 namespace 정보와 clusterbinding 정보를 가져옴
	 * @throws ZcpException 
	 * 
	 */
	public LoginInfoVO getUserInfo(String username) throws ApiException, ParseException, ZcpException{
		LoginInfoVO info = new LoginInfoVO();
		MemberVO user = new MemberVO();
		user.setUserName(username);
		user = keycloakDao.getUser(user);
		info.setUser(user);
		
		V1ClusterRoleBinding clusterrolebinding =  getClusterRoleBinding(username);
		if(clusterrolebinding == null) {
			LOG.debug("cluster role binding is nothing..");
			return info;
		}
		V1Namespace mapNamespace = null;
		String namespace = clusterrolebinding.getSubjects().get(0).getNamespace();
		try {
			mapNamespace =CoreMng.namespace(namespace);
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}
		
		info.setClusterrolebinding(clusterrolebinding);
		info.setNamespace(mapNamespace);
		
        return info;
		
	}
	
	/**
	 * @return
	 * @throws ApiException
	 * @throws ParseException
	 * 
	 * 네임 스페이스 정보
	 * 
	 */
	public List<NamespaceVO> getNamespaces(String mode, String userName) throws ApiException{
		List<NamespaceVO> namespaceList = new ArrayList();
		
		if("simple".equals(mode)) {
			V1RoleBindingList rolebinding = AuthMng.RoleBindingListOfUser(userName);
			
			List<String> name = new ArrayList();
			String namespaceNames ="";
			for(V1RoleBinding nm : rolebinding.getItems()) {
				name.add( nm.getMetadata().getName());
			}
			NamespaceVO vo = new NamespaceVO();
			vo.setNamespace(Util.asCommaData(name));
			namespaceList.add(vo);
		}else if("full".equals(mode)) {
			V1RoleBindingList rolebinding = AuthMng.RoleBindingListOfUser(userName);
			for(V1RoleBinding nm : rolebinding.getItems()) {
				String namespaceName = nm.getMetadata().getName();
				NamespaceVO vo = new NamespaceVO();
				vo.setNamespace(namespaceName);
				V1ResourceQuota quota =  CoreMng.getQuota(namespaceName, namespaceName);
				V1LimitRange limitRanges =  CoreMng.getLimitRanges(namespaceName, namespaceName);
				vo.setLimitRange(limitRanges);
				vo.setResourceQuota(quota);
				namespaceList.add(vo);
			}
		}
		
		return namespaceList;
		
	}
	
	
	/**
	 * 사용자 이름에 따른 clusterrolebinding 값
	 * @param username
	 * @return
	 * @throws ApiException
	 */
	public V1ClusterRoleBinding getClusterRoleBinding(String username) throws ApiException{
		
		List<V1ClusterRoleBinding> items = AuthMng.clusterRoleBindingList().getItems();
		Stream<V1ClusterRoleBinding> serviceAccount = items.stream().filter((srvAcc) -> { 
			List<V1Subject> subjects = srvAcc.getSubjects();
			if(subjects != null) {
				Stream s = subjects.stream().filter((subject) -> {
					LOG.debug("kind={} , name={}", subject.getKind(), subject.getName());
					return subject.getKind().equals("ServiceAccount") 
							&& subject.getName().equals(serviceAccountPrefix+ username);
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
		}

	
	}
	
	
	public String getServiceAccountToken(String namespace, String username) throws IOException, ApiException, InterruptedException{
		//1. delete service account
		CoreMng.deleteServiceAccount(serviceAccountPrefix + username, namespace, new V1DeleteOptions());
		
		//2. create service account
		MemberVO vo = new MemberVO();
		vo.setUserName(username);
		createServiceAccount(vo);
		
		//3. get secretes though serviceAccount 
		Thread.sleep(100);//sync problem raise
		List<V1ObjectReference> secrets = CoreMng.getServiceAccount(namespace, username).getItems().get(0).getSecrets();
		if(secrets != null)
			for(V1ObjectReference secret : secrets) {
				String secretName = secret.getName();
				Map<String, byte[]> secretList = CoreMng.getSecret(namespace, secretName).getData();
				
				return new String(secretList.get("token"));
			}
		
		return null;
	}

	

	private V1ServiceAccount  createAndEditServiceAccount(String name, String namespace, ServiceAccountVO vo) throws ApiException {
		try {
			return CoreMng.createServiceAccount(vo.getNamespace(), vo);
		} catch (ApiException e) {
			if(e.getMessage().equals("Conflict")) {
				return CoreMng.editServiceAccount(name, vo.getNamespace(), vo);
			}else {
				throw e;	
			}
		}
	}
	
	private void createAndEditClusterRoleBinding(String username, V1ClusterRoleBinding clusterRoleBinding) throws ApiException {
		try {
			AuthMng.createClusterRoleBinding( clusterRoleBinding, username);
		} catch (ApiException e) {
			if(e.getMessage().equals("Conflict")) {
				//kubeDao.editClusterRoleBinding(clusterRoleBinding.getMetadata().getName(), clusterRoleBinding, username);
				LOG.debug(e.getMessage());
			}else {
				throw e;	
			}
		}
	}
	
	public void initUserPassword(PassResetVO password) throws ZcpException {
		keycloakDao.initUserPassword(password);
	}
	
	public void removeOtpPassword(String userName) throws ZcpException {
		keycloakDao.removeOtpPassword(userName);
	}
	
	/**
	 * 클러스터 조회
	 * @return
	 * @throws ApiException
	 */
	public List<Map> clusterRoleList() throws ApiException{
		List<Map> clusterRoleNameList = new ArrayList();
		AuthMng.clusterRoleList().getItems().stream().forEach((data) -> {
			Map<String, String> clusterRoleName = new HashMap();
			clusterRoleName.put("name", data.getMetadata().getName());
			clusterRoleNameList.add(clusterRoleName);
		});
		
		return clusterRoleNameList;
	}
	
	public void giveClusterRole(MemberVO vo) throws ApiException, ZcpException {
		//2. clusterRolebindinding 식제
		try {
			AuthMng.deleteClusterRoleBinding(this.clusterRoleBindingPrefix + vo.getUserName(), new V1DeleteOptions());
		}catch(ApiException e) {
			if(!e.getMessage().equals("Not Found")){
				throw e;
			}
		}
		
		//2. clusterRolebindinding 생성
		V1ClusterRoleBinding binding = createClusterRoleBinding(vo);
		this.createAndEditClusterRoleBinding(vo.getUserName(), binding);
		
		keycloakDao.editAttribute(vo);
		
	}
	
	private V1ClusterRoleBinding createClusterRoleBinding(MemberVO vo) {
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
		roleRef.setName(vo.getClusterRole().name());
		cmetadata.setName(clusterRoleBindingPrefix + vo.getUserName());
		binding.setApiVersion("rbac.authorization.k8s.io/v1");
		binding.setKind("ClusterRoleBinding");
		subjects.add(subject);
		binding.setSubjects(subjects);
		binding.setRoleRef(roleRef);
		binding.setMetadata(cmetadata);
		return binding;
	}
	
}
