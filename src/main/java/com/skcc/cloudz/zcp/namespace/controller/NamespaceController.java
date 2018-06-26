package com.skcc.cloudz.zcp.namespace.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.common.annotation.NullProperty;
import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.model.UserList;
import com.skcc.cloudz.zcp.common.vo.Response;
import com.skcc.cloudz.zcp.namespace.service.NamespaceService;
import com.skcc.cloudz.zcp.namespace.vo.InquiryNamespaceVO;
import com.skcc.cloudz.zcp.namespace.vo.ItemList;
import com.skcc.cloudz.zcp.namespace.vo.LabelVO;
import com.skcc.cloudz.zcp.namespace.vo.NamespaceVO;
import com.skcc.cloudz.zcp.namespace.vo.QuotaVO;
import com.skcc.cloudz.zcp.namespace.vo.RoleBindingVO;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;

@Configuration
@RestController
@RequestMapping("/iam")
public class NamespaceController {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(NamespaceController.class);

	@Autowired
	private NamespaceService namespaceService;

	@RequestMapping(value = "/namespaces", method = RequestMethod.GET)
	@NullProperty(field = { "items.metadata.creationTimestamp", "items.spec" })
	public Response<V1NamespaceList> getNamespaces() throws Exception {
		Response<V1NamespaceList> response = new Response<V1NamespaceList>();
		response.setData(namespaceService.getNamespaceList());

		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}", method = RequestMethod.GET)
	@NullProperty(field = { "metadata.creationTimestamp", "spec" })
	public Response<V1Namespace> getNamespace(@PathVariable("namespace") String namespace) throws Exception {
		Response<V1Namespace> response = new Response<V1Namespace>();
		response.setData(namespaceService.getNamespace(namespace));

		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/users", method = RequestMethod.GET)
	public Response<UserList> getUserListByNamespace(@PathVariable("namespace") String namespace) throws Exception {
		Response<UserList> vo = new Response<UserList>();
		vo.setData(namespaceService.getUserListByNamespace(namespace));
		return vo;
	}

	/**
	 * resource info by namespace
	 * 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value = "/namespace/{namespace}/resource", method = RequestMethod.GET)
	Response<NamespaceVO> getNamespaceResource(HttpServletRequest httpServletRequest,
			@PathVariable("namespace") String namespace) throws ApiException, ParseException {
		Response<NamespaceVO> vo = new Response<NamespaceVO>();
		vo.setData(namespaceService.getNamespaceResource(namespace));
		return vo;
	}

	/**
	 * resource quota info
	 * 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value = "/resourceQuotas", method = RequestMethod.GET)
	Response<ItemList<QuotaVO>> getResourceQuotas(@ModelAttribute InquiryNamespaceVO inquiry) throws Exception {
		Response<ItemList<QuotaVO>> response = new Response<>();
		response.setData(namespaceService.getResourceQuotaList(inquiry));
		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/label", method = RequestMethod.POST)
	public Response<Object> createNamespaceLabel(@PathVariable("namespace") String namespace,
			@RequestBody @Valid LabelVO label) throws ZcpException {
		namespaceService.createNamespaceLabel(namespace, label.getLabel());
		return new Response<Object>();
	}

	@RequestMapping(value = "/namespace/{namespace}/label", method = RequestMethod.DELETE)
	public Response<Object> deleteNamespaceLabel(@PathVariable("namespace") String namespace,
			@RequestBody @Valid LabelVO label) throws ZcpException {
		namespaceService.deleteNamespaceLabel(namespace, label.getLabel());
		return new Response<Object>();
	}

	/**
	 * all Namespace Label
	 * 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 * @throws ZcpException
	 */
	@RequestMapping(value = "/namespace/labels", method = RequestMethod.GET)
	Response<ItemList<String>> getNamespaceLabel(HttpServletRequest httpServletRequest)
			throws ApiException, ParseException, ZcpException {
		Response<ItemList<String>> vo = new Response<ItemList<String>>();
		vo.setData(namespaceService.getLabelsOfNamespaces());
		return vo;
	}
	
	/**
	 * specific Namespace Label
	 * 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 * @throws ZcpException
	 */
	@RequestMapping(value = "/namespace/{namespace}/labels", method = RequestMethod.GET)
	Response<ItemList<String>> getNamespaceLabel(@PathVariable("namespace") String namespace)
			throws ApiException, ParseException, ZcpException {
		Response<ItemList<String>> vo = new Response<ItemList<String>>();
		vo.setData(namespaceService.getLabelsOfNamespaces(namespace));
		return vo;
	}

	/**
	 * all namespace resource info
	 * 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	@RequestMapping(value = "/resource", method = RequestMethod.GET)
	Response<NamespaceVO> getAllOfNamespaceResource(HttpServletRequest httpServletRequest)
			throws ApiException, ParseException {
		Response<NamespaceVO> vo = new Response<NamespaceVO>();
		vo.setData(namespaceService.getNamespaceResource(""));
		return vo;
	}

	/**
	 * namespace name only
	 * 
	 * @param httpServletRequest
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws ApiException
	 */
	// @RequestMapping(value="/namespace/onlyNames", method=RequestMethod.GET)
	// @Deprecated
	// Response<List<Map>> getAllOfNamespace(HttpServletRequest httpServletRequest)
	// throws ApiException, ParseException{
	// Response<List<Map>> vo = new Response();
	// vo.setData(namespaceSvc.getAllOfNamespace());
	// return vo;
	// }

	/**
	 * create namespace
	 * 
	 * @param httpServletRequest
	 * @param data
	 * @return
	 * @throws ApiException
	 */
	@RequestMapping(value = "/namespace", method = RequestMethod.POST)
	Response<Object> addNamespace(HttpServletRequest httpServletRequest, @RequestBody NamespaceVO data)
			throws ApiException {
		Response<Object> vo = new Response<Object>();
		namespaceService.createAndEditNamespace(data);
		return vo;
	}

	@RequestMapping(value = "/namespace/{namespace}/roleBinding", method = RequestMethod.POST)
	public Response<Object> createRoleBinding(HttpServletRequest httpServletRequest,
			@PathVariable("namespace") String namespace, @RequestBody @Valid RoleBindingVO vo) throws Exception {
		Response<Object> response = new Response<Object>();
		namespaceService.createRoleBinding(namespace, vo);
		return response;

	}

	@RequestMapping(value = "/namespace/{namespace}/roleBinding", method = RequestMethod.PUT)
	public Response<Object> editRoleBinding(HttpServletRequest httpServletRequest,
			@PathVariable("namespace") String namespace, @RequestBody @Valid RoleBindingVO vo) throws Exception {
		Response<Object> resposne = new Response<Object>();
		namespaceService.editRoleBinding(namespace, vo);
		return resposne;

	}

	@RequestMapping(value = "/namespace/{namespace}/roleBinding", method = RequestMethod.DELETE)
	public Response<Object> deleteRoleBinding(HttpServletRequest httpServletRequest,
			@PathVariable("namespace") String namespace, @RequestBody @Valid RoleBindingVO vo) throws Exception {
		Response<Object> response = new Response<Object>();
		namespaceService.deleteRoleBinding(namespace, vo);
		return response;
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
	@RequestMapping(value = "/namespace/{namespace}", method = RequestMethod.DELETE)
	Response<Object> deleteNamespace(HttpServletRequest httpServletRequest, @PathVariable("namespace") String namespace)
			throws IOException, ApiException {
		Response<Object> vo = new Response<Object>();
		namespaceService.deleteNamespace(namespace);
		return vo;
	}

}
