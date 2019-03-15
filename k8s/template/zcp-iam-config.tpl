apiVersion: v1
kind: ConfigMap
metadata:
  name: zcp-iam-config
  namespace: ${namespace}
  labels:
    component: zcp-iam
data:
  SPRING_ACTIVE_PROFILE: stage
  KEYCLOAK_MASTER_REALM: master
  KEYCLOAK_MASTER_CLIENTID: master-realm
  # can we use the internal service name instead of public dns?
  KEYCLOAK_SERVER_URL: https://${domain_prefix}iam.cloudzcp.io/auth/
  KUBE_APISERVER_URL: https://${api_server}
  JENKINS_SERVER_URL: http://zcp-jenkins:8080
  JENKINS_TEMPLATE_PATH: classpath:jenkins/folder.xml
  WSH_TEMPLATE: classpath:/ssh/pod.yaml
  WSH_IMAGE: ${wsh_image}
  DB_NAME: ${db_name}
  DB_HOST: ${db_host}
  DB_PORT: ${db_port}
