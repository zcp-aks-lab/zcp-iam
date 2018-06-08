package com.skcc.cloudz.zcp.user.controller;

import static com.skcc.cloudz.zcp.common.util.ValidUtil.EMAIL;
import static com.skcc.cloudz.zcp.common.util.ValidUtil.SERVICE_ACCOUNT_NAME;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.simple.parser.ParseException;
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

import com.skcc.cloudz.zcp.common.exception.KeycloakException;
import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.util.ValidUtil;
import com.skcc.cloudz.zcp.common.vo.Response;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;
import com.skcc.cloudz.zcp.user.service.UserService;
import com.skcc.cloudz.zcp.user.vo.ClusterRole;
import com.skcc.cloudz.zcp.user.vo.LoginInfoVO;
import com.skcc.cloudz.zcp.user.vo.MemberVO;
import com.skcc.cloudz.zcp.user.vo.PassResetVO;
import com.skcc.cloudz.zcp.user.vo.UserList;

import io.kubernetes.client.ApiException;

@Configuration
@RestController
@RequestMapping("/iam")
public class UserController {

	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);    
	
	@Autowired
	UserService userSvc;
	
	/**
	 * all user list
	 * 
	 * @param httpServletRequest
	 * @return
	 * @throws ApiException 
	 */
	@RequestMapping(value="/users", method=RequestMethod.GET)
	Response<UserList> getUsers(HttpServletRequest httpServletRequest) throws ApiException{
		Response<UserList> vo = new Response<UserList>();
		vo.setData(userSvc.getUserList());
		return vo;
	}
	
	
	/**
	 * all user list by namespace
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value="/user/namespace/{namespace}", method=RequestMethod.GET)
	@Deprecated
	Response<UserList> userListOfNamespace(HttpServletRequest httpServletRequest, @PathVariable("namespace") String namespace) throws ApiException{
		Response<UserList> vo = new Response<UserList>();
		vo.setData(userSvc.getUserList(namespace));
		return vo;
	}
	
	
	/**
	 * all namespace list by user id
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value="/user/{userName}/namespaces", method=RequestMethod.GET)
	Response<List<NamespaceVO>> getUserNamespaces(HttpServletRequest httpServletRequest
			, @PathVariable("userName") String userName
			, @RequestParam("mode") String mode) throws ApiException{
		Response<List<NamespaceVO>> vo = new Response<List<NamespaceVO>>();
		vo.setData(userSvc.getNamespaces(mode, userName));
		return vo;
	}
	
	/**
	 * User Logout
	 * @param session
	 * @return
	 * @throws ApiException
	 * @throws ZcpException
	 * @throws KeycloakException 
	 */
	@RequestMapping(value="/user/{userName}/logout", method=RequestMethod.POST)
	Response<Object> logout(HttpSession session
			, @PathVariable("userName") String userName) throws KeycloakException{
		Response<Object> vo = new Response<Object>();
		userSvc.logout(userName);
		return vo;
	}
	
	/**
	 * user info - need to login user
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws KeycloakException 
	 * @throws IOException
	 * @throws ApiException
	 * @throws ParseException 
	 * @throws ZcpException 
	 */
	@RequestMapping(value="/user/{userName}", method=RequestMethod.GET)
	Response<LoginInfoVO> getUser(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws ApiException, ParseException, KeycloakException {
		Response<LoginInfoVO> vo = new Response<LoginInfoVO>();
		vo.setData(userSvc.getUserInfo(userName));
		return vo;
	}
	
	
	/**
	 * get serviceAccount  token 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 * @throws InterruptedException 
	 * 
	 */
	@RequestMapping(value="/user/{userName}/serviceAccountToken", method=RequestMethod.GET)
	Response<String> regenerateServiceAccount(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws IOException, ApiException, InterruptedException{
		Response<String> vo = new Response<String>();
		vo.setData(userSvc.getServiceAccountToken("zcp-system", userName));	
		return vo;
	}
	
	
	
	/**
	 * edit user
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ZcpException 
	 */
	@RequestMapping(value="/user/{userName}", method=RequestMethod.PUT)
	Response<Object> modifyUser(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws KeycloakException{
		Response<Object> vo = new Response<Object>();
		memberVO.setUserName(userName);
		userSvc.editUser(memberVO);	
		return vo;
	}
	
	/**
	 * create user
	 * 
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value="/user", method=RequestMethod.POST)
	Response<Object> addUser(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ApiException{
		Response<Object> vo = new Response<Object>();
		String msg = ValidUtil.required(memberVO,  "userName", "firstName", "lastName", "clusterRole");
		if(!ValidUtil.check(EMAIL, memberVO.getEmail())) msg="email is invalid";
		if(!ValidUtil.check(SERVICE_ACCOUNT_NAME, memberVO.getUserName())) msg="userName is invalid";
		
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			userSvc.createUser(memberVO);	
		}
		return vo;
	}
	
	
	
	/**
	 * not yet implement - initialize user password
	 * @param httpServletRequest
	 * @param userName
	 * @param password
	 * @return
	 * @throws ZcpException
	 */
	@RequestMapping(value="/user/{userName}/initUserPassword", method=RequestMethod.PUT)
	Response<Object> initUserPassword(HttpServletRequest httpServletRequest
			, @PathVariable("userName") String userName, @RequestBody PassResetVO password) throws KeycloakException{
		Response<Object> vo = new Response<Object>();
		String msg = ValidUtil.required(password, "actions");
		
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			password.setUserName(userName);
			userSvc.initUserPassword(password);	
		}
		return vo;
	}
	
	
	/**
	 * delete the user opt password 
	 * @param httpServletRequest
	 * @param user
	 * @return
	 * @throws ZcpException
	 */
	@RequestMapping(value="/user/{userName}/removeOtpPassword", method=RequestMethod.DELETE)
	Response<Object> removeOtpPassword(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws KeycloakException{
		Response<Object> vo = new Response<Object>();
		userSvc.removeOtpPassword(userName);	
		return vo;
	}
	
	
	/**
	 * delete user
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ZcpException 
	 * @throws ApiException 
	 */
	@RequestMapping(value="/user/{userName}", method=RequestMethod.DELETE)
	Response<Object> deleteUser(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws KeycloakException, ApiException{
		Response<Object> vo = new Response<Object>();
		userSvc.deleteUser(userName);	
		return vo;
	}
	
	
	/**
	 * chanage password
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ZcpException 
	 */
	@RequestMapping(value="/user/{userName}/resetPassword", method=RequestMethod.PUT)
	Response<Object> resetPassword(HttpServletRequest httpServletRequest
			, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws KeycloakException{
		Response<Object> vo = new Response<Object>();
		String msg = ValidUtil.required(memberVO, "password");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberVO.setUserName(userName);
			userSvc.editUserPassword(memberVO);	
		}
		return vo;
	}
	
	/**
	 * all only cluster name  list
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value="/user/clusterRole", method=RequestMethod.GET)
	Response<List<ClusterRole>> getClusterRole(HttpServletRequest httpServletRequest) throws  ApiException {
		Response<List<ClusterRole>> vo = new Response<List<ClusterRole>>();
		vo.setData(userSvc.clusterRoleList());
		return vo;
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
	@RequestMapping(value="/user/{userName}/clusterRoleBinding", method=RequestMethod.PUT)
	Response<Object> editClusterRole(HttpServletRequest httpServletRequest
			, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws ApiException, KeycloakException{
		Response<Object> vo = new Response<Object>();
		String msg = ValidUtil.required(memberVO,  "clusterRole");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberVO.setUserName(userName);
			userSvc.giveClusterRole(memberVO);	
		}
		return vo;
	}
	
	
	
	
}
