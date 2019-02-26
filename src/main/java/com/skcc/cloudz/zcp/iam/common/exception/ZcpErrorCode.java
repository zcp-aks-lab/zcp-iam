package com.skcc.cloudz.zcp.iam.common.exception;

public enum ZcpErrorCode {
	
	//common
	UNKNOWN_ERROR						            (10000, "Unknown error"),
	KUBERNETES_UNKNOWN_ERROR			            (10001, "Unknown kobernetes error"),
	KEYCLOAK_UNKNOWN_ERROR				            (10002, "Unknown keycloak error"),
	USER_NOT_FOUND						            (10003, ""),
	CLUSTERROLEBINDING_NOT_FOUND		            (10004, ""),
	PERMISSION_DENY						            (10005, ""),
	UNSUPPORTED_TYPE					            (10006, "error : Unsupported type"),
	INVALID_ARGUMENT								(10007, "Invalid Arguments"),
	INVALID_CONFIGURATION							(10008, "Invalid Configureation"),
	NO_PERMISSON									(10009, "Has no permission."),
	                                                
	//app                                           
	DEPOLYMENT_LIST_ERROR				            (11001, "error : Unauthorized"),
	                                                
	//metric                                        
	ROLE_BINDING_LIST_ERROR				            (21002, "error : Role binding list by username"),
	LIST_NODE_METRICS_ERROR				            (21003, "error : Node metrics list"),
	NODE_LIST_ERROR						            (21004, "error : Node list"),
	DEPLOYMENT_LIST_ERROR				            (21005, "error : Deployment list"),
	POD_LIST_ERROR						            (21006, "error : Pod list"),
	CLUSTER_ROLE_STATUS_ERROR			            (21007, "error : Cluster role status"),
	NAMESPACE_ROLE_STATUS_ERROR 		            (21008, "error : Namespace role status"),
	                                                
	//namespace                                     
	ROLEBINDING_NOT_FOUND				            (31001, "error : Rolebinding not found"),
	CREATE_NAMESAPCE_ERROR				            (31002, "error : Create namespace"),
	DELETE_NAMESPACE_ERROR				            (31003, "error : Delete namespace"),
	ROLE_BINDING_LIST_BY_NAMESPACE_ERROR            (31004, "error : Role binding list by namespace"),
	REPLACE_NAMESPACE_ERROR				            (31005, "error : Replace namesapce"),
	CREATE_ROLE_BINDING_ERROR			            (31006, "error : Create role binding"),
	DELETE_ROLE_BINDING_ERROR			            (31007, "error : Delete role binding"),
	DELETE_LIMIT_RANGE_ERROR			            (31008, "error : Delete limit range"),
	CREATE_LIMIT_RANGE_ERROR			            (31009, "error : Create limit range"),
	EDIT_LIMIT_RANGE_ERROR				            (31010, "error : Edit limit range"),
	DELETE_RESOURCE_QUOTA_ERROR			            (31011, "error : Delete resource quota"),
	CREATE_RESOURCE_QUOTA_ERROR			            (31012, "error : Create resource quota"),
	EDIT_RESOURCE_QUOTA_ERROR			            (31013, "error : Edit resource quota"),
	NAMESAPCE_LIST_ERROR				            (31014, "error : Namespace list"),
	GET_NAMESPACE_ERROR					            (31015, "error : Get namespace"),
	LABEL_INVALID_ERROR								(31016, "error : Label invalid"),

	//secret
	GET_SECRET_LIST									(32001, "Fail to load the list of namespace secrets"),
	GET_SECRET										(32002, "Fail to load namespace secret"),
	SECRET_DATA_KEY_NOT_FOUND				        (32003, "There is no data(key) in the secret"),
	CREATE_SECRET_ERROR					            (32004, "Fail to create new secret"),
	DELETE_SECRET_ERROR					            (32005, "error : Delete namespace secret"),
	
	//rbac                                          
	CLUSTER_ROLE_TYPE_ERROR				            (41001, "error : The type is null"),
	CLUSTER_ROLE_ERROR								(41002, "error : Cluster role"),
	ROLEBINDING_NOT_FOUND_ERROR						(41003, "error : Not found the role binding"),
	
	//user
	MAPPED_CLUSTER_ROLE_BINDINGS_ERROR 				(51001, "error : Mapped cluster role bindings"),
	MAPPED_ROLE_BINDINGS_ERROR 						(51002, "error : Mapped role binding"),
	DELETE_SERVICE_ACCOUNT_LIST_BY_USERNAME_ERROR	(51003, "error : Delete service account list by username"),
	CREATE_SERVICE_ACCOUNT_ERROR					(51004, "error : Create service account"),
	CLUSTER_ROLE_BINDING_LIST_BY_USERNAME_ERROR		(51005, "error : Cluster role binding list by username"),
	DELETE_CLUSTER_ROLE_BINDING_BY_USERNAME_ERROR	(51006, "error : Delete cluster role binding by username"),
	CREATE_CLUSTER_ROLE_BINDING_ERROR				(51007, "error : Create cluster role binding"),
	EDIT_USER_ERROR									(51008, "error : Edit user"),
	DELETE_ROLE_BINDING_LIST_BY_USERNAME_ERROR		(51009, "error : Delete role binding list by username"),
	GET_USER_ERROR									(51010, "error : Get username"),
	SERVICE_ACCOUNT_LIST_BY_USERNAME_ERROR			(51011, "error : Service account list by username"),
	SERVICE_ACCOUNT_ERROR							(51012, "error : Service account"),
	EDIT_USER_PASSWORD_ERROR						(51013, "error : Edit user password"),
	ROLE_BINDING_LIST_BY_USERNAME_ERROR				(51014, "error : Role binding list by username"),
	DELETE_CLUSTER_ROLE_BINDING_BY_USERNAME			(51015, "error : Delete cluster role binding by username"),
	RESET_USER_CREDENTIALS_ERROR					(51016, "error : Reset user credentials"),
	DELETE_USER_OTP_PASSWORD_ERROR					(51017, "error : Delete user otp password"),
	ENABLE_USER_OTP_PASSWORD_ERROR					(51018, "error : Enable user otp password"),
	LOGOUT_ERROR									(51019, "error : Logout"),
	GET_SECRET_ERROR								(51020, "error : Get secret"),
	EDIT_ATTRIBUTE_ERROR                            (51021, "error : Edit user attribute"),
	GET_ATTRIBUTE_ERROR                             (51022, "error : Get user attribute"),

	//addon
	ADD_USER_NAMESPACE_ROLE							(61001, "error : Mapped cluster role bindings"),
	DELETE_USER_NAMESPCE_ROLE						(61002, "error : Mapped role binding"),
	ADD_USER_CLUSTER_ROLE							(61003, "error : Mapped cluster role bindings"),
	DELETE_USER_CLUSTER_ROLE						(61004, "error : Mapped role binding"),
	
	//resources
	RESOURCE_NOT_FOUND								(70404, "Not Found"),
	RESOURCE_INVALID								(70422, "Invalid")
	;

	
	
	
	private int code;
	private String message;
	
	private ZcpErrorCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}


}
