# The zcp-iam Installation Guide

> zcp-iam 은 zcp-portal-ui (Console)의 back-end api server 로서, KeyCloak 과 Kubernetes(이하 k8s) 의 Proxy 역할을 하는 API Server 이다.
> zcp-iam 을 설치하기 이전에 k8s cluster 가 설치되어 있어야 하고, cluster role 권한으로 `kubectl` 을 수행 할 수 있는 환경을 갖추어야 한다.

## clone this project into the desktop
```
$ git clone https://github.com/cnpst/zcp-iam.git
```

## deploy the application
> 프로젝트 별로 수정해야 하는 파일은 configmap, ingress, secret 세 가지이다.


```
$ cd zcp-iam/k8s
```

### zcp-iam에서 사용 할 zcp-system-admin 및 Console Admin (cloudzcp-admin) 사용자 용 serviceAccount 을 생성한다.
> zcp-system namespace 에 bluemix container registry 용 secret - `bluemix-cloudzcp-secret` 이 생성 되어 있어야 한다.

```
$ kubectl create -f zcp-system-admin-sa-crb.yaml
```

다음 명령어로 생성된 secret 을 확인한다.
```
$ kubectl get secret -n zcp-system
```

### configmap을 수정하고 배포한다.
프로젝트의 api-server endpoint 정보를 변경해야 한다.
`api-server endpoint` 정보 확인
```
$ kubectl config view
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: REDACTED
    server: https://169.56.69.242:23078
  name: zcp-demo
  ...
  ...
```

ConfigMap 에 `api-server endpoint` 정보 변경
```
$ cd site/
$ vi zcp-iam-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: zcp-iam-config
  namespace: zcp-system
data:
  SPRING_ACTIVE_PROFILE: stage
  KEYCLOAK_MASTER_REALM: master
  KEYCLOAK_MASTER_CLIENTID: master-realm
  KEYCLOAK_SERVER_URL: https://lawai-iam.cloudzcp.io/auth/
  KUBE_APISERVER_URL: https://169.56.69.242:30439
```

ConfigMap 배포
```
$ kubectl create -f zcp-iam-config.yaml
```
### secret을 수정하고 배포한다.
### deployment를 배포한다.
### ingress 를 수정하고 배포한다.
