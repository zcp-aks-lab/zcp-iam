apiVersion: v1
kind: Secret
metadata:
  name: zcp-iam-secret
  namespace: ${namespace}
  labels:
    component: zcp-iam
type: Opaque
data:
  KEYCLOAK_MASTER_CLIENT_SECRET: ${encoded_client_secret}
  KEYCLOAK_MASTER_USERNAME: ${encoded_keycloak_user}
  KEYCLOAK_MASTER_PASSWORD: ${encoded_keycloak_pwd}
  JENKINS_USER_TOKEN: ${encoded_jenkins_user_token}
  DB_USER: ${encoded_db_user}
  DB_PWD: ${encoded_db_pwd}