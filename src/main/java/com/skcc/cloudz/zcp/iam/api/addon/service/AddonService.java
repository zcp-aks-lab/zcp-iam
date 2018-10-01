package com.skcc.cloudz.zcp.iam.api.addon.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.skcc.cloudz.zcp.iam.api.namespace.service.NamespaceEventListener;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;

/**
 * <code>NamespaceEventListener</code> 구현체에 대한 Composite Class.
 * Addon 전체에 Event를 전달한다. (callback 호출)
 */
@Service
public class AddonService implements NamespaceEventListener {

	@Autowired
	private List<NamespaceEventListener> lifecyleListener;
	
	public void onCreateNamespace(String namespace) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.onCreateNamespace(namespace);
		}
	}

	public void onDeleteNamespace(String namespace) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.onDeleteNamespace(namespace);
		}
	}

	public void addNamespaceRoles(String namespace, String username, ClusterRole role) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.addNamespaceRoles(namespace, username, role);
		}
	}

	public void deleteNamspaceRoles(String namespace, String username, ClusterRole role) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.deleteNamspaceRoles(namespace, username, role);
		}
	}

	public void verify(String namespace, Map<String, Object> ctx) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.verify(namespace, ctx);
		}
	}
	
	public static class NamespaceEventAdapter implements NamespaceEventListener {
		public void onCreateNamespace(String namespace) throws ZcpException {}
		public void onDeleteNamespace(String namespace) throws ZcpException {}
		public void addNamespaceRoles(String namespace, String username, ClusterRole newRole) throws ZcpException {}
		public void deleteNamspaceRoles(String namespace, String username, ClusterRole oldRole) throws ZcpException {}
		public void verify(String namespace, Map<String, Object> ctx) throws ZcpException {}
		

		public static boolean isDryRun(Map<String, Object> ctx) {
			Object dry = ctx.get(NamespaceEventListener.DRY_RUN);
			return Boolean.TRUE.equals(dry);
		}
		
//		public static void setMessage(Map<String, Object> ctx, String fmt, Object... args) {
//			String msg = MessageFormat.format(fmt, args);
//
//			Object logs = ctx.get("log");
//			if(logs instanceof List) {
//				List.class.cast(logs).add(msg);
//			} else {
//				ctx.put("log", Lists.newArrayList(msg) );
//			}
//		}
		
		public final void log(Map<String, Object> ctx, String fmt, Object... args) {
			StringBuffer msg = new StringBuffer();
			msg.append(isDryRun(ctx) ? "[DRY] " : "")
				.append(this.getClass().getSimpleName())
				.append(" :: ");
			new MessageFormat(fmt).format(args, msg, null);

			Object logs = ctx.get(NamespaceEventListener.LOG);
			if(logs instanceof List) {
				List.class.cast(logs).add(msg);
			} else {
				ctx.put(NamespaceEventListener.LOG, Lists.newArrayList(msg) );
			}
		}
	}
}
