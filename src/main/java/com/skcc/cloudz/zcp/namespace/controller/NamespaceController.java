package com.skcc.cloudz.zcp.namespace.controller;

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
import com.skcc.cloudz.zcp.member.vo.KubeDeleteOptionsVO;
import com.skcc.cloudz.zcp.member.vo.MemberVO;
import com.skcc.cloudz.zcp.member.vo.NamespaceVO;
import com.skcc.cloudz.zcp.member.vo.RoleBindingVO;
import com.skcc.cloudz.zcp.member.vo.ServiceAccountVO;
import com.skcc.cloudz.zcp.namespace.service.NamespaceService;

import io.kubernetes.client.ApiException;
@Configuration
@RestController
@RequestMapping("/iam")
public class NamespaceController {

	private static final Logger LOG = LoggerFactory.getLogger(NamespaceController.class);    
	
	@Autowired
	NamespaceService namespaceSvc;
	
	
	
	
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
		vo.setData(namespaceSvc.getNamespace(namespace));	
		
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
		vo.setData(namespaceSvc.getNamespaceResource(namespace));	
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
		vo.setData(namespaceSvc.getNamespaceResource(""));	
		
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
		vo.setData(namespaceSvc.getAllOfNamespace());	
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
		namespaceSvc.createAndEditNamespace(data);
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
	@RequestMapping(value="/namespace/{namespace}/roleBinding", method=RequestMethod.PUT)
	Object createRoleBinding(HttpServletRequest httpServletRequest 
			, @PathVariable("namespace") String namespace
			, @RequestBody RoleBindingVO data) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(data,  "userName", "clusterRole");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			data.setNamespace(namespace);
			namespaceSvc.createRoleBinding(data);	
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
	@RequestMapping(value="/namespace/{namespace}/roleBinding", method=RequestMethod.DELETE)
	Object deleteRoleBinding(HttpServletRequest httpServletRequest
			, @PathVariable("namespace") String namespace
			, @RequestBody KubeDeleteOptionsVO data) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		String msg = ValidUtil.required(data,  "userName");
		if(msg != null) {
			vo.setMsg(msg);
			vo.setCode("500");
		}
		else {
			data.setNamespace(namespace);
			namespaceSvc.deleteRoleBinding(data);	
		}
		return vo;
		
	}
	
	
	
}
