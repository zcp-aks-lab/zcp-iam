package com.skcc.cloudz.zcp.namespace.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

import com.skcc.cloudz.zcp.common.util.ValidUtil;
import com.skcc.cloudz.zcp.common.vo.Response;
import com.skcc.cloudz.zcp.common.vo.RoleBindingVO;
import com.skcc.cloudz.zcp.namespace.service.NamespaceService;
import com.skcc.cloudz.zcp.namespace.vo.KubeDeleteOptionsVO;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1NamespaceList;
@Configuration
@RestController
@RequestMapping("/iam")
public class NamespaceController {

	private static final Logger LOG = LoggerFactory.getLogger(NamespaceController.class);    
	
	@Autowired
	NamespaceService namespaceSvc;
	
	
	/**
	 * namespace info
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/{namespace}", method=RequestMethod.GET)
	Response<V1NamespaceList> getNamespace(HttpServletRequest httpServletRequest, @PathVariable("namespace") String namespace) throws  ApiException, ParseException{
		Response<V1NamespaceList> vo = new Response();
		vo.setData(namespaceSvc.getNamespace(namespace));	
		
		return vo;
	}
	
	/**
	 * resource info by namespace
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/{namespace}/resource", method=RequestMethod.GET)
	Response<NamespaceVO> getNamespaceResource(HttpServletRequest httpServletRequest, @PathVariable("namespace") String namespace) throws  ApiException, ParseException{
		Response<NamespaceVO> vo = new Response();
		vo.setData(namespaceSvc.getNamespaceResource(namespace));	
		return vo;
	}
	
	/**
	 * all namespace resource info
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/resource", method=RequestMethod.GET)
	Response<NamespaceVO> getAllOfNamespaceResource(HttpServletRequest httpServletRequest) throws  ApiException, ParseException{
		Response<NamespaceVO> vo = new Response();
		vo.setData(namespaceSvc.getNamespaceResource(""));
		return vo;
	}
	
	/**
	 * namespace name only
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/onlyNames", method=RequestMethod.GET)
	Response<List<Map>> getAllOfNamespace(HttpServletRequest httpServletRequest) throws  ApiException, ParseException{
		Response<List<Map>> vo = new Response();
		vo.setData(namespaceSvc.getAllOfNamespace());	
		return vo;
	}
	
	
	/**
	 * create namespace
	 * 
	 * @param httpServletRequest
	 * @param data
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace", method=RequestMethod.POST)
	Response<?> addNamespace(HttpServletRequest httpServletRequest, @RequestBody NamespaceVO data) throws ApiException {
		Response<?> vo = new Response();
		namespaceSvc.createAndEditNamespace(data);
		return vo;
	}
	
	
	
	
	/**
	 * each user namespace and rolebinding
	 * @param httpServletRequest
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/{namespace}/roleBinding", method=RequestMethod.PUT)
	Response<?> createRoleBinding(HttpServletRequest httpServletRequest 
			, @PathVariable("namespace") String namespace
			, @RequestBody RoleBindingVO data) throws IOException, ApiException{
		Response<?> vo = new Response();
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
	 * delete rolebinding by namespace
	 * 
	 * @param httpServletRequest
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value="/namespace/{namespace}/roleBinding", method=RequestMethod.DELETE)
	Response<?> deleteRoleBinding(HttpServletRequest httpServletRequest
			, @PathVariable("namespace") String namespace
			, @RequestBody KubeDeleteOptionsVO data) throws IOException, ApiException{
		Response<?> vo = new Response();
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
