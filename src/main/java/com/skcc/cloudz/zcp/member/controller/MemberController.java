package com.skcc.cloudz.zcp.member.controller;

import static com.skcc.cloudz.zcp.common.util.ValidUtil.EMAIL;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.util.ValidUtil;
import com.skcc.cloudz.zcp.common.vo.RtnVO;
import com.skcc.cloudz.zcp.member.service.MemberService;
import com.skcc.cloudz.zcp.member.vo.CommonVO;
import com.skcc.cloudz.zcp.member.vo.KubeDeleteOptionsVO;
import com.skcc.cloudz.zcp.member.vo.MemberVO;
import com.skcc.cloudz.zcp.member.vo.NamespaceVO;
import com.skcc.cloudz.zcp.member.vo.RoleBindingVO;
import com.skcc.cloudz.zcp.member.vo.ServiceAccountVO;

import io.kubernetes.client.ApiException;
@Configuration
@RestController
@RequestMapping("/iamTest")
public class MemberController {

	private static final Logger LOG = LoggerFactory.getLogger(MemberController.class);    
	
	@Autowired
	MemberService memberSvc;
	
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
		vo.setData(memberSvc.getUserList());
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
//		String msg = ValidUtil.required(map,  "namespace");
//		if(msg != null) {
//			vo.setMsg(msg);
//			vo.setCode("500");
//		}
//		else {
			vo.setData(memberSvc.getUserList(namespace));
//		}
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
		//vo.setData(memberSvc.getClusterRoleBinding("admin"));// test code//admin
//		String msg = ValidUtil.required(map,  "username");
//		if(msg != null) {
//			vo.setMsg(msg);
//			vo.setCode("500");
//		}
//		else {
			vo.setData(memberSvc.getUserInfo(userName));
//		}
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
		//vo.setData(memberSvc.getServiceAccountToken("kube-system", "tiller"));// test code
//		String msg = ValidUtil.required(map,  "userName", "namespace");
//		if(msg != null) {
//			vo.setMsg(msg);
//			vo.setCode("500");
//		}
//		else {
			vo.setData(memberSvc.getServiceAccountToken(namespace, userName));	
//		}
		
		return vo;
	}
	
	
	/**
	 * 네임 스페이스 정보
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/{namespace}", method=RequestMethod.GET)
	Object getNamespace(HttpServletRequest httpServletRequest, @PathVariable("namespace") String namespace) throws  ApiException, ParseException{
		RtnVO vo = new RtnVO();
//		String msg = ValidUtil.required(map,  "namespace");
//		if(msg != null) {
//			vo.setMsg(msg);
//			vo.setCode("500");
//		}
//		else {
			vo.setData(memberSvc.getNamespace(namespace));	
//		}
		
		return vo;
	}
	
	/**
	 * 네임 스페이스 리소스 정보
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/{namespace}/resource", method=RequestMethod.GET)
	Object getNamespaceResource(HttpServletRequest httpServletRequest, @PathVariable("namespace") String namespace) throws  ApiException, ParseException{
		RtnVO vo = new RtnVO();
//		String msg = ValidUtil.required(map,  "namespace");
//		if(msg != null) {
//			vo.setMsg(msg);
//			vo.setCode("500");
//		}
//		else {
			vo.setData(memberSvc.getNamespaceResource(namespace));	
//		}
		
		return vo;
	}
	
	/**
	 * 네임 스페이스 리소스 정보
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/allOfResource", method=RequestMethod.GET)
	Object getAllOfNamespaceResource(HttpServletRequest httpServletRequest) throws  ApiException, ParseException{
		RtnVO vo = new RtnVO();
		//String msg = ValidUtil.required(map,  "namespace");
//		String msg= null;
//		if(msg != null) {
//			vo.setMsg(msg);
//			vo.setCode("500");
//		}
//		else {
			vo.setData(memberSvc.getNamespaceResource(""));	
//		}
		
		return vo;
	}
	
	/**
	 * 전체 네임스페이스 이름만
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/onlyNames", method=RequestMethod.GET)
	Object getAllOfNamespace(HttpServletRequest httpServletRequest) throws  ApiException, ParseException{
		RtnVO vo = new RtnVO();
//		//String msg = ValidUtil.required(map,  "namespace");
//		String msg=null;
//		if(msg != null) {
//			vo.setMsg(msg);
//			vo.setCode("500");
//		}
//		else {
			vo.setData(memberSvc.getAllOfNamespace());	
//		}
		
		return vo;
	}
	
	
	/**
	 * 네임스페이스 생성
	 * 
	 * @param httpServletRequest
	 * @param data
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/createAndEditNamespace", method=RequestMethod.PUT)
	Object createAndEditNamespace(HttpServletRequest httpServletRequest, @RequestBody NamespaceVO data) throws ApiException {
		RtnVO vo = new RtnVO();
		//String msg = ValidUtil.required(data,  "namespace");
		String msg=null;
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.createAndEditNamespace(data);
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
	@RequestMapping(value="/clusterRole/list", method=RequestMethod.GET)
	Object getClusterRole(HttpServletRequest httpServletRequest) throws  ApiException {
		RtnVO vo = new RtnVO();
		vo.setData(memberSvc.clusterRoleList());
		return vo;
	}
	
	/**
	 * 사용자 변경
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ZcpException 
	 */
	@RequestMapping(value="/user/edit", method=RequestMethod.PUT)
	Object modifyUser(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ZcpException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(memberVO,  "userName");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.editUser(memberVO);	
		}
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
	@RequestMapping(value="/user/createUser", method=RequestMethod.PUT)
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
			memberSvc.createUser(memberVO);	
		}
		return vo;
	}
	
	
	@RequestMapping(value="/user/initUserPassword", method=RequestMethod.PUT)
	Object initUserPassword(HttpServletRequest httpServletRequest, @RequestBody HashMap password) throws ZcpException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(password,  "userName", "actionType");
		
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.initUserPassword(password);	
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
	Object removeOtpPassword(HttpServletRequest httpServletRequest, @RequestBody MemberVO user) throws ZcpException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(user,  "userName");
		
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.removeOtpPassword(user);	
		}
		return vo;
	}
	
	
	@RequestMapping(value="/clusterRole/edit", method=RequestMethod.PUT)
	Object editClusterRole(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ApiException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(memberVO,  "userName");
		String msg2 = ValidUtil.required(memberVO.getAttribute(),  "clusterRole");
		if(msg != null || msg2 !=null) {
			String m = msg != null ? msg : msg2;
			vo.setMsg(m);
			vo.setCode("500");
		}
		else {
			memberSvc.createUser(memberVO);	
		}
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
//		String msg = ValidUtil.required(memberVO, "userName");
//		if(msg != null) {
//			vo.setMsg(msg);
//			vo.setCode("500");
//		}
//		else {
			memberSvc.deleteUser(userName);	
//		}
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
	Object editUserPassword(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ZcpException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(memberVO, "password");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.editUserPassword(memberVO);	
		}
		return vo;
	}
	
	
	/**
	 * 네임스페이스 권한 - 사용자별 네임스페이와 로바인딩
	 * @param httpServletRequest
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/roleBinding", method=RequestMethod.PUT)
	Object createRoleBinding(HttpServletRequest httpServletRequest, @RequestBody RoleBindingVO data) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(data,  "userName", "namespace", "clusterRole");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.createRoleBinding(data);	
		}
		return vo;
		
	}
	


	/**
	 * 네임스페이스 롤 바인딩 삭제
	 * 
	 * @param httpServletRequest
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/roleBinding", method=RequestMethod.DELETE)
	Object deleteRoleBinding(HttpServletRequest httpServletRequest, @RequestBody KubeDeleteOptionsVO data) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(data,  "userName", "namespace");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.deleteRoleBinding(data);	
		}
		return vo;
		
	}
	
	
//	@RequestMapping("/serviceAccount")
//	Object serviceAccount(HttpServletRequest httpServletRequest) throws IOException, ApiException{
//		RtnVO vo = new RtnVO();
////		ObjectMapper mapper = new ObjectMapper();
////		//Map<Object, Object> map = new HashMap<Object, Object>(); // convert JSON string to
////		String json = memberSvc.serviceAccount();
////		//Map map = mapper.readValue(json, new TypeReference<Map<Object, Object>>(){});
////		//vo.setData(map);
//		return vo;
//	}
	
	@RequestMapping("/createServiceAccount")
	@Deprecated
	Object createServiceAccount(HttpServletRequest httpServletRequest, @RequestBody ServiceAccountVO data) throws IOException, ApiException, ParseException{
		RtnVO vo = new RtnVO();
		memberSvc.createServiceAccount(data);
		return vo;
	}
	
	@RequestMapping("/deleteServiceAccount")
	@Deprecated
	Object deleteServiceAccount(HttpServletRequest httpServletRequest, @RequestBody ServiceAccountVO data) throws IOException, ApiException, ParseException{
		RtnVO vo = new RtnVO();
		memberSvc.createServiceAccount(data);
		return vo;
	}
	
	
	@RequestMapping("/deleteClusterRoleBinding")
	@Deprecated
	Object deleteClusterRoleBinding(HttpServletRequest httpServletRequest, @RequestBody KubeDeleteOptionsVO data) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		memberSvc.deleteClusterRoleBinding(data);
		return vo;
	}
	
	
	
	
	
	
}
