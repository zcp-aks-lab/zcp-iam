#!/bin/bash
out_dir=.tmp

# variables be set as jenkins job properties. use this variables when you install manually.
#keycloak_user=username
#keycloak_pwd=password
#jenkins_user=username
#jenkins_token=api-token

domain_prefix=pou-dev-
api_server=kubernetes.default
image=cloudzcp/zcp-iam:1.1.0-alpha
namespace=console
sa=zcp-system-admin2

replicas=${replicas:-1}

# for secret
jenkins_user_token=$jenkins_user:$jenkins_token

pod=$(kubectl get pod -n zcp-system | grep 'zcp-oidc-postgresql' | grep -v 'backup' | awk '{print $1}')
client_secret=$(kubectl exec $pod -n zcp-system -- psql -c "select secret from client where realm_id = 'master' and client_id = 'master-realm';" -tA)

#TODO: for loop
keycloak_user=$(echo -n $keycloak_user | base64)
keycloak_pwd=$(echo -n $keycloak_pwd | base64)
client_secret=$(echo -n $client_secret | base64)
jenkins_user_token=$(echo -n $jenkins_user_token | base64)
