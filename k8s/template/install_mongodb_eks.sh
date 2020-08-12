#!/bin/bash

. setenv.sh

#version='0.4.17'  # at catalog
version='8.2.4'
name='zcp-iam-db'

helm install bitnami/mongodb \
  --version "${version}" \
  --namespace "${namespace}" \
  --name "${name}" \
  --set auth.username="${db_user}" \
  --set auth.password="${db_pwd}" \
  --set auth.database="${db_name}" \
  --set persistence.storageClass=efs-zcp-retain \
  --set persistence.size=20Gi \
  -f values-mongodb.yaml
