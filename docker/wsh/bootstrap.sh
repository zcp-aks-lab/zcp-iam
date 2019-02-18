#!/bin/bash

function log () {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] - $@"
}

UNIT=60 #sec = 1 min
function expire () {
  expired=$(( $1 + $2 * UNIT ))
  remain=$(( $expired - $(date '+%s') ))

  if (( $remain <= 0 )); then
    # https://stackoverflow.com/a/39380672
    from=$(date -d "1970.01.01-00:00:$1" '+%Y-%m-%d %H:%M:%S')
    at=$(date -d "1970.01.01-00:00:$expired" '+%Y-%m-%d %H:%M:%S')

    log "There is no update during $2 min from ${from}."
    log "Web ssh is expired at ${at}"

    return 1  # false
  fi

  return 0  # true
}

# UPPERCASE := environment variables
# lowercase := local variables
sa=/run/secrets/kubernetes.io/serviceaccount
config=~/.kube/config

cluster=${CLUSTER:-cloudzcp}
server=${MASTER_SERVER:-https://kubernetes.default}
token=${KUBE_TOKEN}
username=${USERNAME:-user}
namespace=$(cat $sa/namespace)
var_ns=$(echo $namespace | sed 's/-/_/g')
internal=10

log "Setup kubectl config"
kubectl config --kubeconfig=$config set-credentials "${username}" --token="${token}"
kubectl config --kubeconfig=$config set-cluster "${cluster}" --server="${server}" --certificate-authority="${sa}/ca.crt"
kubectl config --kubeconfig=$config set-context ctx --user="${username}" --namespace="${namespace}" --cluster="${cluster}"
kubectl config --kubeconfig=$config use-context ctx

if [ ! -e ".env" ]; then
  cat /dev/null > .env
  echo "conn_${var_ns}=1" >> .env;
  echo "token=${token}" >> .env;
fi

var_conn="conn_${var_ns}"
prev_conn=0
prev_token=${token}

while true; do
  export $(grep -v '^#' .env | xargs);

  # check connections
  conn="$(echo ${!var_conn})"
  if [ "$conn" -le "0" ]; then
    log "There is no connection. stop process."
    env && exit
  fi;

  # check update time
#   if [ $(expire "${updatetime}" 60) ]; then
#     exit 1
#   fi

  # system logs
  if [ "${conn}" -ne "${prev_conn}" ]; then
    prev_conn=${conn}
    log "Connection count(s) is changed : ${conn}"
  fi

  # update token
  if [ "${token}" != "${prev_token}" ]; then
    kubectl config --kubeconfig=$config set-credentials "${username}" --token="${token}" 1>/dev/null
    prev_token=${token}
    log 'Update token'
  fi

  sleep $internal
done