package com.skcc.cloudz.zcp.member.controller;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.util.ValidUtil;
import com.skcc.cloudz.zcp.common.vo.RtnVO;
import com.skcc.cloudz.zcp.member.service.MemberService;
import com.skcc.cloudz.zcp.member.vo.KubeDeleteOptionsVO;
import com.skcc.cloudz.zcp.member.vo.MemberVO;
import com.skcc.cloudz.zcp.member.vo.NamespaceVO;
import com.skcc.cloudz.zcp.member.vo.RoleBindingVO;
import com.skcc.cloudz.zcp.member.vo.ServiceAccountVO;

import io.kubernetes.client.ApiException;
import static com.skcc.cloudz.zcp.common.util.ValidUtil.EMAIL;
@Configuration
@RestController
@RequestMapping("/iam/member")
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
	@RequestMapping("/userList")
	Object userList(HttpServletRequest httpServletRequest) throws ApiException{
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
	@RequestMapping("/userListOfNamespace")
	Object userList(HttpServletRequest httpServletRequest, @RequestBody HashMap<String, String> map) throws ApiException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(map,  "namespace");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			vo.setData(memberSvc.getUserList(map.get("namespace")));
		}
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
	@RequestMapping("/getUserInfoWithLogin")
	Object getUserInfoWithLogin(HttpServletRequest httpServletRequest, @RequestBody HashMap<String, String> map) throws IOException, ApiException, ParseException{
		RtnVO vo = new RtnVO();
		//vo.setData(memberSvc.getClusterRoleBinding("admin"));// test code//admin
		String msg = ValidUtil.required(map,  "username");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			vo.setData(memberSvc.getUserInfo(map.get("username")));
		}
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
	@RequestMapping("/getServiceAccountToken")
	Object getServiceAccountToken(HttpServletRequest httpServletRequest, @RequestBody HashMap<String, String> map) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		//vo.setData(memberSvc.getServiceAccountToken("kube-system", "tiller"));// test code
		String msg = ValidUtil.required(map,  "username", "namespace");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			vo.setData(memberSvc.getServiceAccountToken(map.get("namespace"), map.get("username")));	
		}
		
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
	@RequestMapping("/getNamespace")
	Object getNamespace(HttpServletRequest httpServletRequest, @RequestBody HashMap<String, String> map) throws  ApiException, ParseException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(map,  "namespace");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			vo.setData(memberSvc.getNamespace(map.get("namespace")));	
		}
		
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
	@RequestMapping("/getNamespaceResource")
	Object getNamespaceResource(HttpServletRequest httpServletRequest, @RequestBody HashMap<String, String> map) throws  ApiException, ParseException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(map,  "namespace");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			vo.setData(memberSvc.getNamespaceResource(map.get("namespace")));	
		}
		
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
	@RequestMapping("/getAllOfNamespace")
	Object getAllOfNamespace(HttpServletRequest httpServletRequest) throws  ApiException, ParseException{
		RtnVO vo = new RtnVO();
		//String msg = ValidUtil.required(map,  "namespace");
		String msg=null;
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			vo.setData(memberSvc.getAllOfNamespace());	
		}
		
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
	@RequestMapping("/createAndEditNamespace")
	Object createAndEditNamespace(HttpServletRequest httpServletRequest, @RequestBody NamespaceVO data) throws ApiException {
		RtnVO vo = new RtnVO();
		//String msg = ValidUtil.required(data,  "namespace");
		String msg=null;
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.createAndEditNamespace(data.getNamespace(), data.getResourceQuota(), data.getLimitRange());
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
	@RequestMapping("/clusterRoleList")
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
	@RequestMapping("/editUser")
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
	@RequestMapping("/createUser")
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
	
	
	
	@RequestMapping("/editClusterRole")
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
	@RequestMapping("/deleteUser")
	Object deleteUser(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ZcpException, ApiException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(memberVO, "userName");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			memberSvc.deleteUser(memberVO);	
		}
		return vo;
	}
	
	
	/**
	 * 비밀번호 변경
	 * @param httpServletRequest
	 * @param memberVO
	 * @return
	 * @throws ZcpException 
	 */
	@RequestMapping("/editUserPassword")
	Object editUserPassword(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO) throws ZcpException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(memberVO,  "username", "password");
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
	@RequestMapping("/createRoleBinding")
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
	@RequestMapping("/deleteRoleBinding")
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
