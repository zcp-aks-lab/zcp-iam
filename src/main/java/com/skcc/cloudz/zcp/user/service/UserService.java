package com.skcc.cloudz.zcp.user.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.manager.KeyCloakManager;
import com.skcc.cloudz.zcp.manager.KubeCoreManager;
import com.skcc.cloudz.zcp.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.manager.ResourcesLabelManager;
import com.skcc.cloudz.zcp.manager.ResourcesNameManager;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;
import com.skcc.cloudz.zcp.user.vo.ClusterRole;
import com.skcc.cloudz.zcp.user.vo.MemberVO;
import com.skcc.cloudz.zcp.user.vo.PassResetVO;
import com.skcc.cloudz.zcp.user.vo.UserList;
import com.skcc.cloudz.zcp.user.vo.ZcpUser;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1ClusterRoleBindingList;
import io.kubernetes.client.models.V1ClusterRoleList;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ObjectReference;
import io.kubernetes.client.models.V1ResourceQuota;
import io.kubernetes.client.models.V1RoleBinding;
import io.kubernetes.client.models.V1RoleBindingList;
import io.kubernetes.client.models.V1RoleRef;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.models.V1ServiceAccountList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.models.V1Subject;

@Service
public class UserService {

	private final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private KeyCloakManager keyCloakManager;

	@Autowired
	private KubeCoreManager kubeCoreManager;

	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;

	@Value("${kube.cluster.role.binding.prefix}")
	private String clusterRoleBindingPrefix;

	@Value("${kube.role.binding.prefix}")
	private String roleBindingPrefix;

	@Value("${kube.service.account.prefix}")
	private String serviceAccountPrefix;

	@Value("${kube.system.namespace}")
	private String zcpSystemNamespace;

	public UserList getUserList() throws ZcpException {
		List<UserRepresentation> keyCloakUsers = keyCloakManager.getUserList();

		List<ZcpUser> users = new ArrayList<ZcpUser>();
		for (UserRepresentation cloakUser : keyCloakUsers) {
			ZcpUser user = new ZcpUser();
			user.setId(cloakUser.getId());
			user.setUsername(cloakUser.getUsername());
			user.setEmail(cloakUser.getEmail());
			user.setLastName(cloakUser.getLastName());
			user.setFirstName(cloakUser.getFirstName());
			user.setCreatedDate(new Date(cloakUser.getCreatedTimestamp()));
			user.setEnabled(cloakUser.isEnabled());

			users.add(user);
		}

		Map<String, V1ClusterRoleBinding> mappedClusterRoleBindings = null;
		try {
			mappedClusterRoleBindings = getMappedClusterRoleBindings();
		} catch (ApiException e) {
			throw new ZcpException("ZCP-0001");
		}

		Map<String, List<V1RoleBinding>> mappedRoleBindings = null;
		try {
			mappedRoleBindings = getMappedRoleBindings();
		} catch (ApiException e) {
			throw new ZcpException("ZCP-0001");
		}

		for (ZcpUser user : users) {
			List<V1RoleBinding> userRoleBindins = mappedRoleBindings.get(user.getUsername());
			if (userRoleBindins != null) {
				user.setUsedNamespace(userRoleBindins.size());
			}
			V1ClusterRoleBinding userClusterRoleBinding = mappedClusterRoleBindings.get(user.getUsername());
			if (userClusterRoleBinding != null) {
				user.setClusterRole(ClusterRole.getClusterRole(userClusterRoleBinding.getRoleRef().getName()));
			}
		}

		return new UserList(users);
	}

	private Map<String, List<V1RoleBinding>> getMappedRoleBindings() throws ApiException {
		List<V1RoleBinding> allRoleBindings = kubeRbacAuthzManager.getRoleBindingListAllNamespaces().getItems();
		Map<String, List<V1RoleBinding>> map = new HashMap<>();
		for (V1RoleBinding roleBinding : allRoleBindings) {
			String username = roleBinding.getMetadata().getLabels()
					.get(ResourcesLabelManager.SYSTEM_USERNAME_LABEL_NAME);
			List<V1RoleBinding> userRoleBindings = map.get(username);
			if (userRoleBindings == null) {
				userRoleBindings = new ArrayList<>();
				userRoleBindings.add(roleBinding);
				map.put(username, userRoleBindings);
			} else {
				map.get(username).add(roleBinding);
			}
		}

		return map;
	}

	private Map<String, V1ClusterRoleBinding> getMappedClusterRoleBindings() throws ApiException {
		List<V1ClusterRoleBinding> clusterRoleBindings = kubeRbacAuthzManager.getClusterRoleBindingList().getItems();

		Map<String, V1ClusterRoleBinding> map = new HashMap<>();
		for (V1ClusterRoleBinding clusterRoleBinding : clusterRoleBindings) {
			String username = clusterRoleBinding.getMetadata().getLabels()
					.get(ResourcesLabelManager.SYSTEM_USERNAME_LABEL_NAME);
			map.put(username, clusterRoleBinding);
		}

		return map;
	}

	public UserList getUserListByNamespace(String namespace) throws ApiException {

		List<ZcpUser> users = new ArrayList<ZcpUser>();
		V1RoleBindingList rolebindingList = kubeRbacAuthzManager.getRoleBindingListByNamespace(namespace);
		List<V1RoleBinding> rolebindings = rolebindingList.getItems();
		List<UserRepresentation> keyCloakUsers = keyCloakManager.getUserList();

		for (V1RoleBinding binding : rolebindings) {
			String name = binding.getMetadata().getName();
			for (UserRepresentation cloakUser : keyCloakUsers) {
				if (name.equals(this.roleBindingPrefix + cloakUser.getUsername())) {
					ZcpUser user = new ZcpUser();
					user.setUsername(cloakUser.getUsername());
					user.setEmail(cloakUser.getEmail());
					user.setLastName(cloakUser.getLastName());
					user.setFirstName(cloakUser.getFirstName());
					user.setCreatedDate(new Date(cloakUser.getCreatedTimestamp()));
					user.setEnabled(cloakUser.isEnabled());

					users.add(user);
				}
			}
		}
		// TODO
		// for (ZcpUser user : users) {
		// V1RoleBindingList mapUser =
		// kubeRbacAuthzManager.getRoleBindingList(user.getUsername());
		// int count = mapUser.getItems().size();
		// user.setUsedNamespace(count);
		// }

		UserList userlist = new UserList();
		userlist.setItems(users);

		return userlist;

	}

	public void createUser(ZcpUser zcpUser) throws ZcpException {
		// 1. create service account
		V1ServiceAccountList serviceAccountList = null;
		try {
			serviceAccountList = kubeCoreManager.getServiceAccountListByUsername(zcpSystemNamespace,
					zcpUser.getUsername());
		} catch (ApiException e) {
			// ignore
		}
		
		if (serviceAccountList != null) {
			V1Status status = null;
			try {
				status = kubeCoreManager.deleteServiceAccountListByUsername(zcpSystemNamespace,
						zcpUser.getUsername());
			} catch (ApiException e) {
				throw new ZcpException("ZCP-008", e.getMessage());
			}
			
			logger.debug("The serviceaccounts of user({}) have been removed. {}", zcpUser.getUsername(), status.getMessage());
		}

		V1ServiceAccount serviceAccount = getServiceAccount(zcpUser.getUsername());

		try {
			serviceAccount = kubeCoreManager.createServiceAccount(zcpSystemNamespace, serviceAccount);
		} catch (ApiException e) {
			throw new ZcpException("ZCP-009", e.getMessage());
		}

		// 2. create clusterRolebindinding
		 V1ClusterRoleBindingList clusterRoleBindingList = null;
		 try {
			clusterRoleBindingList = kubeRbacAuthzManager.getClusterRoleBindingListByUsername(zcpUser.getUsername());
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		if (clusterRoleBindingList != null) {
			V1Status status = null;
			try {
				status = kubeRbacAuthzManager.deleteClusterRoleBindingByUsername(zcpUser.getUsername());
			} catch (ApiException e) {
				throw new ZcpException("ZCP-008", e.getMessage());
			}
			
			logger.debug("The clusterRoleBindings of user({}) have been removed. {}", zcpUser.getUsername(), status.getMessage());
		}
		
		V1ClusterRoleBinding clusterRoleBinding = getClusterRoleBinding(zcpUser);
		try {
			clusterRoleBinding = kubeRbacAuthzManager.createClusterRoleBinding(clusterRoleBinding);
		} catch (ApiException e) {
			throw new ZcpException("ZCP-009", e.getMessage());
		}

		// 3. create keycloak user
		 keyCloakManager.createUser(zcpUser);
	}

	private V1ServiceAccount getServiceAccount(String username) {
		String serviceAccountName = ResourcesNameManager.getServiceAccountName(username);
		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(serviceAccountName);
		metadata.setNamespace(zcpSystemNamespace);
		metadata.setLabels(ResourcesLabelManager.getSystemUsernameLabels(username));

		V1ServiceAccount serviceAccount = new V1ServiceAccount();
		serviceAccount.setMetadata(metadata);

		return serviceAccount;
	}

	private V1ClusterRoleBinding getClusterRoleBinding(ZcpUser zcpUser) {
		String username = zcpUser.getUsername();
		String serviceAccountName = ResourcesNameManager.getServiceAccountName(username);
		String clusterRoleBindingName = ResourcesNameManager.getServiceAccountName(username);

		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(clusterRoleBindingName);
		metadata.setNamespace(zcpSystemNamespace);
		metadata.setLabels(ResourcesLabelManager.getSystemUsernameLabels(username));

		V1Subject subject = new V1Subject();
		subject.setKind("ServiceAccount");
		subject.setName(serviceAccountName);
		subject.setNamespace(zcpSystemNamespace);

		List<V1Subject> subjects = new ArrayList<V1Subject>();
		subjects.add(subject);

		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup("rbac.authorization.k8s.io");
		roleRef.setKind("ClusterRole");
		roleRef.setName(zcpUser.getClusterRole().getRole());

		V1ClusterRoleBinding clusterRoleBinding = new V1ClusterRoleBinding();
		clusterRoleBinding.setMetadata(metadata);
		clusterRoleBinding.setRoleRef(roleRef);
		clusterRoleBinding.setSubjects(subjects);

		return clusterRoleBinding;
	}

	public void giveClusterRole(MemberVO vo) throws ApiException, KeyCloakException {
		// 2. clusterRolebindinding 식제
		try {
			kubeRbacAuthzManager.deleteClusterRoleBinding(this.clusterRoleBindingPrefix + vo.getUserName(),
					new V1DeleteOptions());
		} catch (ApiException e) {
			if (!e.getMessage().equals("Not Found")) {
				throw e;
			}
		}

		// 2. clusterRolebindinding 생성
		// V1ClusterRoleBinding binding = createClusterRoleBinding(vo);
		// this.createClusterRoleBinding(vo.getUserName(), binding);

		keyCloakManager.editAttribute(vo);

	}

	public ZcpUser getUser(String username) throws ZcpException {
		ZcpUser zcpUser = null;
		try {
			zcpUser = keyCloakManager.getUser(username);
		} catch (KeyCloakException e) {
			throw new ZcpException("ZCP-0001", e.getMessage());
		}

		V1ClusterRoleBinding userClusterRoleBinding = getClusterRoleBindingByUsername(username);
		if (userClusterRoleBinding == null) {
			// If user registered by himself, the clusterrolebindings may not exist before
			// cluster-admin confirms the user.
			logger.debug("The clusterrolebinding of user({}) does not exist yet", username);
		} else {
			zcpUser.setClusterRole(ClusterRole.getClusterRole(userClusterRoleBinding.getRoleRef().getName()));
		}

		List<V1RoleBinding> userRoleBindings = null;
		try {
			userRoleBindings = kubeRbacAuthzManager.getRoleBindingListByUsername(username).getItems();
		} catch (ApiException e1) {
			throw new ZcpException("ZCP-0001");
		}

		if (userRoleBindings != null && !userRoleBindings.isEmpty()) {
			List<String> userNamespaces = new ArrayList<>();
			for (V1RoleBinding roleBinding : userRoleBindings) {
				userNamespaces.add(roleBinding.getMetadata().getNamespace());
			}

			zcpUser.setNamespaces(userNamespaces);
			zcpUser.setUsedNamespace(userNamespaces.size());
		}

		return zcpUser;

	}

	public void editUser(MemberVO vo) throws KeyCloakException {
		keyCloakManager.editUser(vo);
	}

	public void editUserPassword(MemberVO vo) throws KeyCloakException {
		keyCloakManager.editUserPassword(vo);
	}

	public void deleteUser(String username) throws KeyCloakException, ApiException {
		// 1.service account 삭제
		V1DeleteOptions deleteOption = new V1DeleteOptions();
		try {
			kubeCoreManager.deleteServiceAccount(serviceAccountPrefix + username, zcpSystemNamespace, deleteOption);
		} catch (ApiException e) {
			if (!e.getMessage().equals("Not Found")) {
				throw e;
			}
		}

		// 2.cluster role binding 삭제
		try {
			kubeRbacAuthzManager.deleteClusterRoleBinding(serviceAccountPrefix + username, deleteOption);
		} catch (ApiException e) {
			if (!e.getMessage().equals("Not Found")) {
				throw e;
			}
		}

		keyCloakManager.deleteUser(username);
	}

	/**
	 * @return
	 * @throws ApiException
	 * @throws ParseException
	 * 
	 *             네임 스페이스 정보
	 * 
	 */
	public List<NamespaceVO> getNamespaces(String mode, String username) throws ApiException {
		List<NamespaceVO> namespaceList = new ArrayList<NamespaceVO>();

		// if("simple".equals(mode)) {
		// V1RoleBindingList rolebinding = AuthMng.RoleBindingListOfUser(username);
		//
		// List<String> name = new ArrayList<String>();
		// for(V1RoleBinding nm : rolebinding.getItems()) {
		// name.add( nm.getMetadata().getName());
		// }
		// NamespaceVO vo = new NamespaceVO();
		// vo.setNamespace(Util.asCommaData(name));
		// namespaceList.add(vo);
		// }else if("full".equals(mode)) {
		V1RoleBindingList rolebinding = kubeRbacAuthzManager.getRoleBindingListByNamespace(username);
		for (V1RoleBinding nm : rolebinding.getItems()) {
			String namespaceName = nm.getMetadata().getName();
			NamespaceVO vo = new NamespaceVO();
			vo.setNamespace(namespaceName);
			V1ResourceQuota quota = kubeCoreManager.getQuota(namespaceName, namespaceName);
			V1LimitRange limitRanges = kubeCoreManager.getLimitRanges(namespaceName, namespaceName);
			vo.setLimitRange(limitRanges);
			vo.setResourceQuota(quota);
			namespaceList.add(vo);
		}
		// }

		return namespaceList;

	}

	/**
	 * 사용자 이름에 따른 clusterrolebinding 값
	 * 
	 * @param username
	 * @return
	 * @throws ApiException
	 */
	public V1ClusterRoleBinding getClusterRoleBindingByUsername(String username) throws ZcpException {

		List<V1ClusterRoleBinding> userClusterRoleBindings = null;
		try {
			userClusterRoleBindings = kubeRbacAuthzManager.getClusterRoleBindingListByUsername(username).getItems();
		} catch (ApiException e) {
			e.printStackTrace();
			throw new ZcpException("ZCP-0004", e.getMessage());
		}

		if (userClusterRoleBindings == null || userClusterRoleBindings.isEmpty()) {
			// If user registered by himself, user's clusterrolebinding does not exist
			// before cluster-admin confirms user.
			return null;
		}

		if (userClusterRoleBindings.size() > 1) {
			throw new ZcpException("ZCP-0002", "The clusterrolebindings of user(" + username + ") should be only one");
		}

		return userClusterRoleBindings.get(0);

		// Stream<V1ClusterRoleBinding> serviceAccount =
		// userClusterRoleBindings.stream().filter((srvAcc) -> {
		// List<V1Subject> subjects = srvAcc.getSubjects();
		// if (subjects != null) {
		// Stream<V1Subject> s = subjects.stream().filter((subject) -> {
		// log.debug("kind={} , name={}", subject.getKind(), subject.getName());
		// return subject.getKind().equals("ServiceAccount")
		// && subject.getName().equals(serviceAccountPrefix + username);
		// });
		// if (s != null)
		// return s.count() > 0;
		// else
		// return false;
		// }
		// return false;
		// });
		// try {
		// return serviceAccount.findAny().get();
		// } catch (NoSuchElementException e) {
		// return null;
		// }

	}

	public String getServiceAccountToken(String namespace, String username)
			throws IOException, ApiException, InterruptedException {
		// 1. delete service account
		kubeCoreManager.deleteServiceAccount(serviceAccountPrefix + username, namespace, new V1DeleteOptions());

		// 2. create service account
		MemberVO vo = new MemberVO();
		vo.setUserName(username);
		// createServiceAccount(vo);

		// 3. get secretes though serviceAccount
		Thread.sleep(100);// sync problem raise
		List<V1ObjectReference> secrets = kubeCoreManager.getServiceAccountListByUsername(namespace, username)
				.getItems().get(0).getSecrets();
		if (secrets != null)
			for (V1ObjectReference secret : secrets) {
				String secretName = secret.getName();
				Map<String, byte[]> secretList = kubeCoreManager.getSecret(namespace, secretName).getData();

				return new String(secretList.get("token"));
			}

		return null;
	}

	public void initUserPassword(PassResetVO password) throws KeyCloakException {
		keyCloakManager.initUserPassword(password);
	}

	public void removeOtpPassword(String username) throws KeyCloakException {
		keyCloakManager.removeOtpPassword(username);
	}

	public V1ClusterRoleList clusterRoleList() throws ApiException {
		return kubeRbacAuthzManager.getClusterRoleList();
	}

	public void logout(String username) throws KeyCloakException {
		keyCloakManager.logout(username);
	}

}
