#!/bin/bash
out_dir=.tmp

# variables be set as jenkins job properties. use this variables when you install manually.
keycloak_user=cloudzcp-admin
keycloak_pwd=                # CHANGE
keycloak_db_user=keycloak
keycloak_db_pwd=             # CHANGE
keycloak_db_database=keycloak
jenkins_user=cloudzcp-admin
jenkins_token=api-token      # CHANGE

#db_uri=mongodb://localhost:27017
db_host=zcp-iam-db-mongo
db_port=27017
db_name=iam
db_user=iam
db_pwd=iam   # CHANGE

sa=zcp-system-admin
domain_prefix=pog-dev-   # CHANGE
api_server=kubernetes.default # CHANGE
namespace=zcp-system
image=registry.au-syd.bluemix.net/cloudzcp/zcp-iam:1.2.1
wsh_image=zcr.cloudzcp.io/cloudzcp/wsh:1.2.0
#wsh_image=cloudzcp/wsh:1.2.0   # public

replicas=1

{
  # for secret
  jenkins_user_token=$jenkins_user:$jenkins_token

  pod=$(kubectl get pod -n zcp-system | grep 'zcp-oidc-postgresql' | grep -v 'backup' | awk '{print $1}')
  client_secret=$(kubectl exec $pod -n zcp-system -- bash -c "PGPASSWORD=$keycloak_db_pwd psql -U$keycloak_db_user -d$keycloak_db_database -c \"select secret from client where realm_id = 'master' and client_id = 'master-realm';\" -tA")

  #TODO: for loop
  encoded_keycloak_user=$(echo -n $keycloak_user | base64)
  encoded_keycloak_pwd=$(echo -n $keycloak_pwd | base64)
  encoded_client_secret=$(echo -n $client_secret | base64)
  encoded_jenkins_user_token=$(echo -n $jenkins_user_token | base64)

  encoded_db_user=$(echo -n $db_user | base64)
  encoded_db_pwd=$(echo -n $db_pwd | base64)

  db_port=$(printf '"%s"' "${db_port}")
}
