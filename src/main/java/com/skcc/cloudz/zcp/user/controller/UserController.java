package com.skcc.cloudz.zcp.user.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

import com.skcc.cloudz.zcp.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.util.ValidUtil;
import com.skcc.cloudz.zcp.common.vo.Response;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;
import com.skcc.cloudz.zcp.user.service.UserService;
import com.skcc.cloudz.zcp.user.vo.MemberVO;
import com.skcc.cloudz.zcp.user.vo.PassResetVO;
import com.skcc.cloudz.zcp.user.vo.UserList;
import com.skcc.cloudz.zcp.user.vo.ZcpUser;

import io.kubernetes.client.ApiException;
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

	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
	public Response<ZcpUser> getUser(@PathVariable("username") String username) throws Exception {
		logger.debug("The requested username is {}", username);

		Response<ZcpUser> response = new Response<ZcpUser>();
		response.setData(userService.getUser(username));
		return response;
	}

	/**
	 * all namespace list by user id
	 * 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value = "/user/{username}/namespaces", method = RequestMethod.GET)
	Response<List<NamespaceVO>> getUserNamespaces(HttpServletRequest httpServletRequest,
			@PathVariable("username") String username, @RequestParam("mode") String mode) throws ApiException {
		Response<List<NamespaceVO>> vo = new Response<List<NamespaceVO>>();
		vo.setData(userService.getNamespaces(mode, username));
		return vo;
	}

	/**
	 * User Logout
	 * 
	 * @param session
	 * @return
	 * @throws ApiException
	 * @throws ZcpException
	 * @throws KeyCloakException
	 */
	@RequestMapping(value = "/user/{username}/logout", method = RequestMethod.POST)
	public Response<Object> logout(HttpSession session, @PathVariable("username") String username)
			throws KeyCloakException {
		Response<Object> vo = new Response<Object>();
		userService.logout(username);
		return vo;
	}

	/**
	 * get serviceAccount token
	 * 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 * @throws InterruptedException
	 * 
	 */
	@RequestMapping(value = "/user/{username}/serviceAccountToken", method = RequestMethod.GET)
	Response<String> regenerateServiceAccount(HttpServletRequest httpServletRequest,
			@PathVariable("username") String username) throws IOException, ApiException, InterruptedException {
		Response<String> vo = new Response<String>();
		vo.setData(userService.getServiceAccountToken("zcp-system", username));
		return vo;
	}

	/**
	 * edit user
	 * 
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ZcpException
	 */
	@RequestMapping(value = "/user/{username}", method = RequestMethod.PUT)
	Response<Object> modifyUser(HttpServletRequest httpServletRequest, @PathVariable("username") String username,
			@RequestBody MemberVO memberVO) throws KeyCloakException {
		Response<Object> vo = new Response<Object>();
		memberVO.setUserName(username);
		userService.editUser(memberVO);
		return vo;
	}

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public Response<Object> addUser(@RequestBody @Valid ZcpUser zcpUser) throws Exception {
		Response<Object> response = new Response<Object>();
		userService.createUser(zcpUser);
		return response;
	}

	/**
	 * not yet implement - initialize user password
	 * 
	 * @param httpServletRequest
	 * @param username
	 * @param password
	 * @return
	 * @throws ZcpException
	 */
	@RequestMapping(value = "/user/{username}/initUserPassword", method = RequestMethod.PUT)
	Response<Object> initUserPassword(HttpServletRequest httpServletRequest, @PathVariable("username") String username,
			@RequestBody PassResetVO password) throws KeyCloakException {
		Response<Object> vo = new Response<Object>();
		String msg = ValidUtil.required(password, "actions");

		if (msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		} else {
			password.setUserName(username);
			userService.initUserPassword(password);
		}
		return vo;
	}

	/**
	 * delete the user opt password
	 * 
	 * @param httpServletRequest
	 * @param user
	 * @return
	 * @throws ZcpException
	 */
	@RequestMapping(value = "/user/{username}/removeOtpPassword", method = RequestMethod.DELETE)
	Response<Object> removeOtpPassword(HttpServletRequest httpServletRequest, @PathVariable("username") String username)
			throws KeyCloakException {
		Response<Object> vo = new Response<Object>();
		userService.removeOtpPassword(username);
		return vo;
	}

	@RequestMapping(value = "/user/{username}", method = RequestMethod.DELETE)
	Response<Object> deleteUser(@PathVariable("username") String username) throws KeyCloakException, ApiException {
		Response<Object> response = new Response<Object>();
		userService.deleteUser(username);
		return response;
	}

	/**
	 * chanage password
	 * 
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ZcpException
	 */
	@RequestMapping(value = "/user/{username}/resetPassword", method = RequestMethod.PUT)
	Response<Object> resetPassword(HttpServletRequest httpServletRequest, @PathVariable("username") String username,
			@RequestBody MemberVO memberVO) throws KeyCloakException {
		Response<Object> vo = new Response<Object>();
		String msg = ValidUtil.required(memberVO, "password");
		if (msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		} else {
			memberVO.setUserName(username);
			userService.editUserPassword(memberVO);
		}
		return vo;
	}

	@RequestMapping(value = "/clusterRoles", method = RequestMethod.GET)
	public Response<V1ClusterRoleList> getClusterRole() throws ApiException {
		Response<V1ClusterRoleList> response = new Response<>();
		response.setData(userService.clusterRoleList());
		return response;
	}

	/**
	 * only clusterRoleBinding
	 * 
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ApiException
	 * @throws ZcpException
	 */
	@RequestMapping(value = "/user/{username}/clusterRoleBinding", method = RequestMethod.PUT)
	Response<Object> editClusterRole(HttpServletRequest httpServletRequest, @PathVariable("username") String username,
			@RequestBody MemberVO memberVO) throws ApiException, KeyCloakException {
		Response<Object> vo = new Response<Object>();
		String msg = ValidUtil.required(memberVO, "clusterRole");
		if (msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		} else {
			memberVO.setUserName(username);
			userService.giveClusterRole(memberVO);
		}
		return vo;
	}

}
