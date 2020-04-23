#!/bin/bash

. setenv.sh

#version='0.4.17'  # at catalog
version='5.7.0'
name='zcp-iam-db'

helm install stable/mongodb \
  --version "${version}" \
  --namespace "${namespace}" \
  --name "${name}" \
  --set persistence.storageClass=ibmc-block-retain-silver \
  --set persistence.size=20Gi \
  -f values-mongodb.yaml
