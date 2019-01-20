package com.skcc.cloudz.zcp.iam.api.user.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.iam.api.user.service.UserService;
import com.skcc.cloudz.zcp.iam.api.user.vo.MemberVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.ResetCredentialVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.ResetPasswordVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.UpdateClusterRoleVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.UpdatePasswordVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.UserAttributeVO;
import com.skcc.cloudz.zcp.iam.common.model.ZcpKubeConfig;
import com.skcc.cloudz.zcp.iam.common.model.ZcpUser;
import com.skcc.cloudz.zcp.iam.common.model.ZcpUserList;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

import io.kubernetes.client.models.V1RoleBindingList;

@Configuration
@RestController
@RequestMapping("/iam")
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpUserList> getUsers(@RequestParam (required=false, value="keyword") String keyword) throws Exception {
		Response<ZcpUserList> response = new Response<>();
		response.setData(userService.getUsers(keyword));
		return response;
	}

	@RequestMapping(value = "/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> addUser(@RequestBody @Valid MemberVO user) throws Exception {
		Response<Object> response = new Response<Object>();
		userService.createUser(user);
		return response;
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpUser> getUser(@PathVariable("id") String id) throws Exception {
		logger.debug("The requested id is {}", id);

		Response<ZcpUser> response = new Response<ZcpUser>();
		response.setData(userService.getUser(id));
		return response;
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> editUser(@PathVariable("id") String id, @RequestBody @Valid MemberVO user)
			throws Exception {
		userService.updateUser(id, user);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> removeUser(@PathVariable("id") String id) throws Exception {
		Response<Object> response = new Response<Object>();
		userService.deleteUser(id);
		return response;
	}

	@RequestMapping(value = "/user/{id}/logout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> logout(@PathVariable("id") String id) throws Exception {
		userService.logout(id);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}/password", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> editPassword(@PathVariable("id") String id, @RequestBody @Valid UpdatePasswordVO vo)
			throws Exception {
		userService.updateUserPassword(id, vo);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}/resetPassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> resetPassword(@PathVariable("id") String id, @RequestBody @Valid ResetPasswordVO vo)
			throws Exception {
		userService.resetUserPassword(id, vo);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}/resetCredentials", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> resetCredentials(@PathVariable("id") String id, @RequestBody @Valid ResetCredentialVO vo)
			throws Exception {
		userService.resetUserCredentials(id, vo);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}/otpPassword", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> removeOtpPassword(@PathVariable("id") String id) throws Exception {
		Response<Object> vo = new Response<Object>();
		userService.deleteOtpPassword(id);
		return vo;
	}
	
	@RequestMapping(value = "/user/{id}/otpPassword", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> enableOtpPassword(@PathVariable("id") String id) throws Exception {
		Response<Object> vo = new Response<Object>();
		userService.enableOtpPassword(id);
		return vo;
	}

	@RequestMapping(value = "/user/{id}/clusterRoleBinding", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<Object> editClusterRoleBinding(@PathVariable("id") String id,
			@RequestBody @Valid UpdateClusterRoleVO vo) throws Exception {
		userService.updateUserClusterRole(id, vo);
		return new Response<Object>();
	}

	@RequestMapping(value = "/user/{id}/kubeconfig", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpKubeConfig> getKubeConfig(@PathVariable("id") String id,
			@RequestParam("namespace") String namespace) throws Exception {
		Response<ZcpKubeConfig> response = new Response<>();
		response.setData(userService.getKubeConfig(id, namespace));
		return response;
	}

	@RequestMapping(value = "/user/{id}/roleBindings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<V1RoleBindingList> getRoleBindings(@PathVariable("id") String id) throws Exception {
		Response<V1RoleBindingList> response = new Response<>();
		response.setData(userService.getUserRoleBindings(id));
		return response;
	}

	@RequestMapping(value = "/user/{id}/serviceAccount", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<String> resetServiceAccount(@PathVariable("id") String id) throws Exception {
		userService.resetUserServiceAccount(id);
		return new Response<String>();
	}
	
	@RequestMapping(value = "/user/{id}/attributes", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<Object> editUserAttribute(@PathVariable("id") String id,
            @RequestBody UserAttributeVO userAttributeVO) throws Exception {
	    userService.updateUserAttribute(id, userAttributeVO);
        return new Response<Object>();
    }
	
	@RequestMapping(value = "/user/{id}/attributes/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<Object> getUserAttribute(@PathVariable("id") String id,
            @PathVariable("key") String key) throws Exception {
	    Response<Object> response = new Response<Object>();
	    response.setData(userService.getUserAttribute(id, key));
        return response;
    }

}
