apiVersion: v1
kind: Secret
metadata:
  name: zcp-iam-secret
  namespace: ${namespace}
  labels:
    component: zcp-iam
type: Opaque
data:
  KEYCLOAK_MASTER_CLIENT_SECRET: ${client_secret}
  KEYCLOAK_MASTER_USERNAME: ${keycloak_user}
  KEYCLOAK_MASTER_PASSWORD: ${keycloak_pwd}
  JENKINS_USER_TOKEN: ${jenkins_user_token}
  DB_USER: ${db_user}
  DB_PWD: ${db_pwd}