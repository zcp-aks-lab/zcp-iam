package com.skcc.cloudz.zcp.user.controller;

import static com.skcc.cloudz.zcp.common.util.ValidUtil.EMAIL;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.util.ValidUtil;
import com.skcc.cloudz.zcp.common.vo.RtnVO;
import com.skcc.cloudz.zcp.user.service.UserService;
import com.skcc.cloudz.zcp.user.vo.MemberVO;

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
	@RequestMapping(value="/user", method=RequestMethod.GET)
	Object allOfList(HttpServletRequest httpServletRequest) throws ApiException{
		RtnVO vo = new RtnVO();
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
	@RequestMapping(value="/user/{namespace}", method=RequestMethod.GET)
	Object listOfNamespace(HttpServletRequest httpServletRequest, @PathVariable("namespace") String namespace) throws ApiException{
		RtnVO vo = new RtnVO();
		vo.setData(userSvc.getUserList(namespace));
		return vo;
	}
	
	
	/**
	 * need to login user
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 * @throws ParseException 
	 */
	@RequestMapping(value="/user/login/{userName}", method=RequestMethod.GET)
	Object getUserInfoWithLogin(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws IOException, ApiException, ParseException{
		RtnVO vo = new RtnVO();
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
	 * 
	 */
	@RequestMapping(value="/user/{userName}/{namespace}/serviceAccountToken", method=RequestMethod.GET)
	Object getServiceAccountToken(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @PathVariable("namespace") String namespace) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		vo.setData(userSvc.getServiceAccountToken(namespace, userName));	
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
	Object modifyUser(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws ZcpException{
		RtnVO vo = new RtnVO();
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
	Object createUser(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ApiException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(memberVO,  "userName", "firstName", "lastName");
		String msg2 = ValidUtil.required(memberVO.getAttribute(),  "clusterRole");
		if(ValidUtil.check(EMAIL, memberVO.getEmail())) msg="email invalid";
		
		if(msg != null || msg2 !=null) {
			String m = msg != null ? msg : msg2;
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			userSvc.createUser(memberVO);	
		}
		return vo;
	}
	
	
	
	/**
	 * not implement - initialize user password
	 * @param httpServletRequest
	 * @param userName
	 * @param password
	 * @return
	 * @throws ZcpException
	 */
	@RequestMapping(value="/user/{userName}/initUserPassword", method=RequestMethod.PUT)
	@Deprecated
	Object initUserPassword(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody HashMap password) throws ZcpException{
		RtnVO vo = new RtnVO();
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
	@RequestMapping(value="/user/removeOtpPassword", method=RequestMethod.DELETE)
	Object removeOtpPassword(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody MemberVO user) throws ZcpException{
		RtnVO vo = new RtnVO();
		user.setUserName(userName);
		userSvc.removeOtpPassword(user);	
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
	Object deleteUser(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws ZcpException, ApiException{
		RtnVO vo = new RtnVO();
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
	@RequestMapping(value="/user/{userName}/editPassword", method=RequestMethod.PUT)
	Object editUserPassword(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws ZcpException{
		RtnVO vo = new RtnVO();
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
	 * all cluster name only list
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value="/user/clusterRole", method=RequestMethod.GET)
	Object getClusterRole(HttpServletRequest httpServletRequest) throws  ApiException {
		RtnVO vo = new RtnVO();
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
	@RequestMapping(value="/user/{userName}/clusterRole", method=RequestMethod.PUT)
	Object editClusterRole(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @RequestBody MemberVO memberVO) throws ApiException, ZcpException{
		RtnVO vo = new RtnVO();
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
	
}
