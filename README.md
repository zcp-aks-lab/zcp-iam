# The zcp-iam Installation Guide

zcp-iam 은 zcp-portal-ui (Console)의 back-end api server 로서, KeyCloak 과 Kubernetes(이하 k8s) 의 Proxy 역할을 하는 API Server 이다.

zcp-iam 을 설치하기 이전에 k8s cluster 가 설치되어 있어야 하고, cluster role 권한으로 `kubectl` 을 수행 할 수 있는 환경을 갖추어야 한다.

## Clone this project into the desktop
```
$ git clone https://github.com/cnpst/zcp-iam.git
```

## Deploy the application
프로젝트 별로 수정해야 하는 파일은 **configmap, ingress, secret** 세 가지이다.

k8s configuration 파일 디렉토리로 이동한다.

```
$ cd zcp-iam/k8s
```

### :one: zcp-iam에서 사용 할 zcp-system-admin 및 Console Admin (cloudzcp-admin) 사용자 용 serviceAccount 을 생성한다.
zcp-system namespace 에 **bluemix container registry** 용 secret - `bluemix-cloudzcp-secret` 이 생성 되어 있어야 한다.

```
$ kubectl create -f zcp-system-admin-sa-crb.yaml
```

다음 명령어로 생성된 secret 을 확인한다.
```
$ kubectl get secret -n zcp-system
```

### :two: ConfigMap을 수정한 후 생성 한다.
#### 프로젝트의 `api-server endpoint` 정보를 변경해야 한다.

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

#### ConfigMap 에 `api-server endpoint` 정보 변경
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

#### ConfigMap 생성
```
$ kubectl create -f zcp-iam-config.yaml
```
### :three: Secret을 수정한 후 생성 한다.

#### KeyCloak 설치 시 admin crediential 정보와 KeyCloak의 master realm에 있는 master-realm client의 secret 값을 변경해야 한다. 

```
apiVersion: v1
kind: Secret
metadata:
  name: zcp-iam-secret
  namespace: zcp-system
type: Opaque
data:
  KEYCLOAK_MASTER_CLIENT_SECRET: NjcyNDVhOWYtY2JjMy00YmJhLWE2NGYtMTc1MDM3Y2Y3YmI5  
  KEYCLOAK_MASTER_USERNAME: Y2xvdWR6Y3AtYWRtaW4=
  KEYCLOAK_MASTER_PASSWORD: Y2xvdWR6Y3AhMjMk
```

##### KeyCloak 의 master realm client 의 secret 정보를 확인하는 방법

* KeyCloak 에서 사용하는 Postgresql 에 접속하여 Client 테이블에서 Secret 정보를 Select 한다.

```
$ kubectl exec -it zcp-oidc-postgresql-c94cc488f-znq2v psql                                                                   
psql (9.6.2)
Type "help" for help.

keycloak=# select secret from client where realm_id = 'master' and client_id = 'master-realm';
                secret
--------------------------------------
 237ef2ad-c5f3-491a-89ab-dab04c8bf268
(1 row)

keycloak=# \q
```

Secret 정보를 복사 한 후 base64로 incoding 한다.

```
$ echo -n "secret of master realm client" | base64
```

`KEYCLOAK_MASTER_CLIENT_SECRET` 의 value 를 base64 incoding 된 값으로 변경한다.

KeyCloak 설치 시 설정한 admin id/password (KEYCLOAK_ADMIN_ID, KEYCLOAK_ADMIN_PWD) 도 base64 incoding 한 후, 각각 `KEYCLOAK_MASTER_USERNAME`, `KEYCLOAK_MASTER_PASSWORD` 의 value 값을 변경한다.

(:white_check_mark: KeyCloak 설치 시 admin id/password 변경하지 않은 경우 그대로 사용하면 됨)

#### Secret 생성

```
$ kubectl create -f zcp-iam-secret.yaml
```

### :four: Deployment와 Service를 생성 한다.
zcp-iam 의 container image tag 정보를 확인 한 후, 생성 한다.
현재는 bluemix container registry `image: registry.au-syd.bluemix.net/cloudzcp/zcp-iam:0.9.3` 를 사용한다.
```
$ cd ../
$ kubectl create -f zcp-iam-deployment-ibm.yaml
```

다음 명령어로 zcp-iam 이 정상적으로 배포되었는지 확인한다.
```
$ kubectl get pod -n zcp-system
```
