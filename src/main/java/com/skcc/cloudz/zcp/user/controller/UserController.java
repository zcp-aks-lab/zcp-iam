package com.skcc.cloudz.zcp.user.controller;

import static com.skcc.cloudz.zcp.common.util.ValidUtil.EMAIL;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.util.ValidUtil;
import com.skcc.cloudz.zcp.common.vo.Response;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;
import com.skcc.cloudz.zcp.user.service.UserService;
import com.skcc.cloudz.zcp.user.vo.LoginInfoVO;
import com.skcc.cloudz.zcp.user.vo.MemberVO;
import com.skcc.cloudz.zcp.user.vo.UserVO;

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
	Response<List<UserVO>> getUsers(HttpServletRequest httpServletRequest) throws ApiException{
		Response<List<UserVO>> vo = new Response();
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
	Response<List<UserVO>> userListOfNamespace(HttpServletRequest httpServletRequest, @PathVariable("namespace") String namespace) throws ApiException{
		Response<List<UserVO>> vo = new Response();
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
		Response<List<NamespaceVO>> vo = new Response();
		vo.setData(userSvc.getNamespaces(mode, userName));
		return vo;
	}
	
	
	/**
	 * user info - need to login user
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 * @throws ParseException 
	 * @throws ZcpException 
	 */
	@RequestMapping(value="/user/{userName}", method=RequestMethod.GET)
	Response<LoginInfoVO> getUser(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws IOException, ApiException, ParseException, ZcpException{
		Response<LoginInfoVO> vo = new Response();
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
		Response<String> vo = new Response();
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
	Response<?> modifyUser(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws ZcpException{
		Response<?> vo = new Response();
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
	Response<?> addUser(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ApiException{
		Response<?> vo = new Response();
		String msg = ValidUtil.required(memberVO,  "userName", "firstName", "lastName", "clusterRole");
		if(!ValidUtil.check(EMAIL, memberVO.getEmail())) msg="email invalid";
		
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
	@Deprecated
	Response<?> initUserPassword(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody HashMap password) throws ZcpException{
		Response<?> vo = new Response();
		String msg = ValidUtil.required(password, "actionType");
		
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			password.put("userName", userName);
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
	Response<?> removeOtpPassword(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws ZcpException{
		Response<?> vo = new Response();
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
	Response<?> deleteUser(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws ZcpException, ApiException{
		Response<?> vo = new Response();
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
	Response<?> resetPassword(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws ZcpException{
		Response<?> vo = new Response();
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
	Response<List<Map>> getClusterRole(HttpServletRequest httpServletRequest) throws  ApiException {
		Response<List<Map>> vo = new Response();
		vo.setData(userSvc.clusterRoleList());
		return vo;
	}
	
	
	
	/**
	 * 
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ApiException
	 * @throws ZcpException
	 */
	@RequestMapping(value="/user/{userName}/clusterRoleBinding", method=RequestMethod.PUT)
	Response<?> editClusterRole(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws ApiException, ZcpException{
		Response<?> vo = new Response();
		String msg = ValidUtil.required(memberVO.getAttribute(),  "clusterRole");
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
	
	
	@RequestMapping(value="/user/{userName}/logout", method=RequestMethod.PUT)
	Response<?> logout(HttpSession session) throws ApiException, ZcpException{
		Response<?> vo = new Response();
		session.invalidate();
		return vo;
	}
	
}
