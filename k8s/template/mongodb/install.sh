#!/bin/bash

. ../setenv.sh

helm install stable/mongodb --version 0.4.17 \
  --namespace "${namespace}" \
  -n zcp-iam-mongodb \
  -f values-mongodb-ibm.yaml
