package com.skcc.cloudz.zcp.iam.api.namespace.service;

import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;

public interface NamespaceEventListener {
	/**
	 * Namespace 생성 시 호출된다.
	 * @param namespace
	 * @throws ZcpException
	 */
	public void onCreateNamespace(String namespace) throws ZcpException;

	/**
	 * Namespace 삭제 시 호출된다.
	 * @param namespace
	 * @throws ZcpException
	 */
	public void onDeleteNamespace(String namespace) throws ZcpException;

	/**
	 * Namespace에 사용자 Role 추가 시 호출된다.
	 * @param namespace
	 * @param username
	 * @param clusterRole
	 * @throws ZcpException
	 */
	public void addNamespaceRoles(String namespace, String username, ClusterRole newRole) throws ZcpException; 

	/**
	 * Namespace에 사용자 Role이 삭제 시 호출된다.
	 * @param namespace
	 * @param username
	 * @param oldRoleName
	 * @throws ZcpException
	 */
	public void deleteNamspaceRoles(String namespace, String username, ClusterRole oldRole) throws ZcpException;
}
