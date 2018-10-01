package com.skcc.cloudz.zcp.iam.api.cluster.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.iam.api.cluster.service.ClusterService;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

@RestController
@RequestMapping("/iam")
public class ClusterController {
	@Autowired
	private ClusterService clusterService;

	@RequestMapping(value = "/clusters/verify", method = RequestMethod.GET)
	public Response<Object> verify(
			@RequestParam(name="dry-run", required=false, defaultValue="true") boolean dry) throws Exception {
		// Ref : https://spoqa.github.io/2013/06/11/more-restful-interface.html#controller
		Response<Object> response = new Response<>();
		Object data = clusterService.verify(null, dry);
		response.setData(data);
		return response;
	}
}
