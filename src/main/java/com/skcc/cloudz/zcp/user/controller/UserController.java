package com.skcc.cloudz.zcp.user.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.common.annotation.NullProperty;
import com.skcc.cloudz.zcp.common.model.UserList;
import com.skcc.cloudz.zcp.common.model.ZcpUser;
import com.skcc.cloudz.zcp.common.vo.Response;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;
import com.skcc.cloudz.zcp.user.service.UserService;
import com.skcc.cloudz.zcp.user.vo.MemberVO;
import com.skcc.cloudz.zcp.user.vo.ResetCredentialVO;
import com.skcc.cloudz.zcp.user.vo.ResetPasswordVO;
import com.skcc.cloudz.zcp.user.vo.UpdateClusterRoleVO;
import com.skcc.cloudz.zcp.user.vo.UpdatePasswordVO;

import io.kubernetes.client.models.V1ClusterRoleList;

@Configuration
@RestController
@RequestMapping("/iam")
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public Response<UserList> getUsers() throws Exception {
		Response<UserList> response = new Response<UserList>();
		response.setData(userService.getUserList());
		return response;
	}

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public Response<Object> addUser(@RequestBody @Valid MemberVO user) throws Exception {
		Response<Object> response = new Response<Object>();
		userService.createUser(user);
		return response;
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
	public Response<ZcpUser> getUser(@PathVariable("id") String id) throws Exception {
		logger.debug("The requested id is {}", id);

		Response<ZcpUser> response = new Response<ZcpUser>();
		response.setData(userService.getUser(id));
		return response;
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.PUT)
	public Response<Object> editUser(@PathVariable("id") String id, @RequestBody @Valid MemberVO user)
			throws Exception {
		userService.editUser(id, user);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
	public Response<Object> deleteUser(@PathVariable("id") String id) throws Exception {
		Response<Object> response = new Response<Object>();
		userService.deleteUser(id);
		return response;
	}

	@RequestMapping(value = "/user/{id}/logout", method = RequestMethod.POST)
	public Response<Object> logout(@PathVariable("id") String id) throws Exception {
		userService.logout(id);
		return new Response<Object>();
	}

	@RequestMapping(value = "/clusterRoles", method = RequestMethod.GET)
	@NullProperty(field = { "items.metadata.creationTimestamp", "items.rules" })
	public Response<V1ClusterRoleList> getClusterRoleList() throws Exception {
		Response<V1ClusterRoleList> response = new Response<>();
		response.setData(userService.clusterRoleList());
		return response;
	}

	@RequestMapping(value = "/user/{id}/password", method = RequestMethod.PUT)
	public Response<Object> updatePassword(@PathVariable("id") String id,
			@RequestBody @Valid UpdatePasswordVO vo) throws Exception {
		userService.updateUserPassword(id, vo);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}/resetPassword", method = RequestMethod.PUT)
	public Response<Object> resetPassword(@PathVariable("id") String id,
			@RequestBody @Valid ResetPasswordVO vo) throws Exception {
		userService.resetUserPassword(id, vo);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}/resetCredentials", method = RequestMethod.PUT)
	public Response<Object> resetCredentials(@PathVariable("id") String id,
			@RequestBody @Valid ResetCredentialVO vo) throws Exception {
		userService.resetUserCredentials(id, vo);
		return new Response<Object>();
	}

	@Deprecated
	@RequestMapping(value = "/user/{username}/namespaces", method = RequestMethod.GET)
	Response<List<NamespaceVO>> getUserNamespaces(@PathVariable("username") String username,
			@RequestParam("mode") String mode) throws Exception {
		Response<List<NamespaceVO>> vo = new Response<List<NamespaceVO>>();
		vo.setData(userService.getNamespaces(mode, username));
		return vo;
	}

	@Deprecated
	@RequestMapping(value = "/user/{username}/serviceAccountToken", method = RequestMethod.GET)
	Response<String> regenerateServiceAccount(HttpServletRequest httpServletRequest,
			@PathVariable("username") String username) throws Exception {
		Response<String> vo = new Response<String>();
		vo.setData(userService.getServiceAccountToken("zcp-system", username));
		return vo;
	}

	@RequestMapping(value = "/user/{id}/otpPassword", method = RequestMethod.DELETE)
	public Response<Object> deleteOtpPassword(@PathVariable("id") String id) throws Exception {
		Response<Object> vo = new Response<Object>();
		userService.deleteOtpPassword(id);
		return vo;
	}

	@RequestMapping(value = "/user/{id}/clusterRoleBinding", method = RequestMethod.PUT)
	public Response<Object> updateClusterRoleBinding(@PathVariable("id") String id,
			@RequestBody @Valid UpdateClusterRoleVO vo) throws Exception {

		userService.updateUserClusterRole(id, vo);

		return new Response<Object>();
	}

}
