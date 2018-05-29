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
	 * 전체 사용자 리스트
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
	 * 네임스페이스에 해당하는 사용자 리스트
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
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 * 사용자 로그인시에 인증시 필요
	 * @throws ParseException 
	 */
	@RequestMapping(value="/user/login/{userName}", method=RequestMethod.GET)
	Object getUserInfoWithLogin(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName) throws IOException, ApiException, ParseException{
		RtnVO vo = new RtnVO();
		vo.setData(userSvc.getUserInfo(userName));
		return vo;
	}
	
	
	/**
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 * 최초 사용자 등록후 kubernetes 접근 토큰
	 */
	@RequestMapping(value="/user/{userName}/{namespace}/serviceAccountToken", method=RequestMethod.GET)
	Object getServiceAccountToken(HttpServletRequest httpServletRequest, @PathVariable("userName") String userName, @PathVariable("namespace") String namespace) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		vo.setData(userSvc.getServiceAccountToken(namespace, userName));	
		return vo;
	}
	
	
	
	/**
	 * 사용자 변경
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
	 * 사용자 생성
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
	
	
	@RequestMapping(value="/user/{userName}/initUserPassword", method=RequestMethod.PUT)
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
	 * 사용자 OTP 삭제
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
	 * 사용자 삭제
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
	 * 비밀번호 변경
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
	 * 클러스터 조회
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
	
	
	
	@RequestMapping(value="/user/clusterRole", method=RequestMethod.PUT)
	Object editClusterRole(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ApiException, ZcpException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(memberVO,  "userName");
		String msg2 = ValidUtil.required(memberVO.getAttribute(),  "clusterRole");
		if(msg != null || msg2 !=null) {
			String m = msg != null ? msg : msg2;
			vo.setMsg(m);
			vo.setCode("500");
		}
		else {
			userSvc.giveClusterRole(memberVO);	
		}
		return vo;
	}
	
}
