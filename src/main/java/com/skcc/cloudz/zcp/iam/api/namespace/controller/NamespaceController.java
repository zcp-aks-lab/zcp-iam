package com.skcc.cloudz.zcp.iam.api.namespace.controller;

import javax.validation.Valid;

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

import com.skcc.cloudz.zcp.iam.api.namespace.service.NamespaceService;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.ItemList;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.LabelVO;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.NamespaceResourceDetailVO;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.NamespaceResourceVO;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.RoleBindingVO;
import com.skcc.cloudz.zcp.iam.common.model.ZcpUserList;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

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
	public Response<V1NamespaceList> getNamespaces() throws Exception {
		Response<V1NamespaceList> response = new Response<>();
		response.setData(namespaceService.getNamespaces());

		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}", method = RequestMethod.GET)
	public Response<V1Namespace> getNamespace(@PathVariable("namespace") String namespace) throws Exception {
		Response<V1Namespace> response = new Response<>();
		response.setData(namespaceService.getNamespace(namespace));

		return response;
	}

	@RequestMapping(value = "/namespace", method = RequestMethod.POST)
	public Response<Object> saveNamespace(@RequestBody NamespaceResourceVO vo) throws Exception {
		Response<Object> response = new Response<>();
		namespaceService.saveNamespace(vo);
		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}", method = RequestMethod.DELETE)
	public Response<Object> deleteNamespace(@PathVariable("namespace") String namespace,
			@RequestParam(required = true, value = "userId") String userId) throws Exception {
		Response<Object> response = new Response<>();
		namespaceService.deleteNamespace(namespace, userId);
		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/users", method = RequestMethod.GET)
	public Response<ZcpUserList> getUsersByNamespace(@PathVariable("namespace") String namespace) throws Exception {
		Response<ZcpUserList> response = new Response<>();
		response.setData(namespaceService.getUsersByNamespace(namespace));
		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/resource", method = RequestMethod.GET)
	public Response<NamespaceResourceDetailVO> getNamespaceResource(@PathVariable("namespace") String namespace,
			@RequestParam(required = true, value = "userId") String userId) throws Exception {
		Response<NamespaceResourceDetailVO> response = new Response<>();
		response.setData(namespaceService.getNamespaceResource(namespace, userId));
		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/label", method = RequestMethod.POST)
	public Response<Object> createNamespaceLabel(@PathVariable("namespace") String namespace,
			@RequestBody @Valid LabelVO label) throws Exception {
		namespaceService.createNamespaceLabel(namespace, label.getLabel());
		return new Response<Object>();
	}

	@RequestMapping(value = "/namespace/{namespace}/label", method = RequestMethod.DELETE)
	public Response<Object> deleteNamespaceLabel(@PathVariable("namespace") String namespace,
			@RequestBody @Valid LabelVO label) throws Exception {
		namespaceService.deleteNamespaceLabel(namespace, label.getLabel());
		return new Response<Object>();
	}

	@RequestMapping(value = "/namespace/labels", method = RequestMethod.GET)
	public Response<ItemList<String>> getAllLabels() throws Exception {
		Response<ItemList<String>> response = new Response<>();
		response.setData(namespaceService.getAllLabels());
		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/labels", method = RequestMethod.GET)
	public Response<ItemList<String>> getNamespaceLabels(@PathVariable("namespace") String namespace) throws Exception {
		Response<ItemList<String>> vo = new Response<>();
		vo.setData(namespaceService.getLabelsByNamespace(namespace));
		return vo;
	}

	@RequestMapping(value = "/namespace/{namespace}/roleBinding", method = RequestMethod.POST)
	public Response<Object> createRoleBinding(@PathVariable("namespace") String namespace,
			@RequestBody @Valid RoleBindingVO vo) throws Exception {
		Response<Object> response = new Response<>();
		namespaceService.createRoleBinding(namespace, vo);
		return response;

	}

	@RequestMapping(value = "/namespace/{namespace}/roleBinding", method = RequestMethod.PUT)
	public Response<Object> editRoleBinding(@PathVariable("namespace") String namespace,
			@RequestBody @Valid RoleBindingVO vo) throws Exception {
		Response<Object> resposne = new Response<>();
		namespaceService.editRoleBinding(namespace, vo);
		return resposne;

	}

	@RequestMapping(value = "/namespace/{namespace}/roleBinding", method = RequestMethod.DELETE)
	public Response<Object> deleteRoleBinding(@PathVariable("namespace") String namespace,
			@RequestBody @Valid RoleBindingVO vo) throws Exception {
		Response<Object> response = new Response<>();
		namespaceService.deleteRoleBinding(namespace, vo);
		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/verify", method = RequestMethod.GET)
	public Response<Object> verify(
			@PathVariable("namespace") String namespace,
			@RequestParam(name="dry-run", required=false, defaultValue="true") boolean dry) throws Exception {
		// Ref : https://spoqa.github.io/2013/06/11/more-restful-interface.html#controller
		Response<Object> response = new Response<>();
		Object data = namespaceService.verify(namespace, dry);
		response.setData(data);
		return response;
	}
}
