# Installation Guide

zcp-iam 은 zcp-portal-ui (Console)의 back-end api server 로서, KeyCloak 과 Kubernetes(이하 k8s) 의 Proxy 역할을 하는 API Server 이다.

zcp-iam 을 설치하기 이전에 k8s cluster 가 설치되어 있어야 하고, cluster-admin role 권한으로 `kubectl` 을 수행 할 수 있는 환경을 갖추어야 한다.

## Clone Project

Clone this project into the desktop

```
$ git clone https://github.com/cnpst/zcp-iam.git
```

## Create ServiceAccount

zcp-iam에서 사용 할 zcp-system-admin 및 Console Admin (cloudzcp-admin) 사용자 용 serviceAccount 을 생성한다.

zcp-system namespace 에 **bluemix container registry** 용 secret - `bluemix-cloudzcp-secret` 이 생성 되어 있어야 한다.

```
$ kubectl create -f zcp-system-admin-sa-crb.yaml

$ kubectl get secret -n zcp-system  # check to create
```

## Generate YAML (Kubernetes Resources)

설치 환경에 맞게 `setenv.sh` 파일을 수정한 후, `template.sh` 파일을 실행한다.

각 정보를 확인하는 자세한 방법은 Appendix 를 참고한다.

```
$ cd zcp-iam/k8s/template

$ cat setenv.sh
#!/bin/bash
out_dir=.tmp

# variables be set as jenkins job properties. use this variables when you install manually.
keycloak_user=cloudzcp-admin
keycloak_pwd=
jenkins_user=cloudzcp-admin
jenkins_token=api-token

sa=zcp-system-admin
domain_prefix=pog-dev-
api_server=kubernetes.default
namespace=zcp-system
image=registry.au-syd.bluemix.net/cloudzcp/zcp-iam:1.1.0

replicas=1
...
```

`template.sh` 파일은 템플릿 파일(`.tpl`/`.tpl2`)을 변환하여 YAML 파일을 생성한다.

`.tmp` 는 `setenv.sh` 파일의 `out_dir` 값과 동일하다.

```
$ bash template.sh

$ ls -l .tmp
...
```

## Create Kubernetes Resource

템플릿을 통해 생성된 YAML 파일을 아래의 명령으로 실행한다.

```
$ kubectl create -f .tmp

## check to create
$ kubectl get deploy,po,cm,secret,svc -n zcp-system -l component=zcp-iam
```

## Appendix

### Jenkins 의 api-token 정보를 확인하는 방법

- Jenkins에 로그인 한다. (폴더 생성권한 필요)
- 우측 상단의 사용자 이름을 클릭한다.
- 좌측 메뉴의 설정 페이지로 이동한다.
- API Token > Legacy API Token 버튼을 클릭하여 값을 확인한다.
- Secret 정보를 복사 한 후 base64로 incoding 한다.

### ~~KeyCloak 의 master realm client 의 secret 정보를 확인하는 방법~~

- KeyCloak 에서 사용하는 Postgresql 에 접속하여 Client 테이블에서 Secret 정보를 Select 한다.

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

`KEYCLOAK_MASTER_CLIENT_SECRET` 의 value 를 base64 encoding 된 값으로 변경한다.

KeyCloak 설치 시 설정한 admin id/password (KEYCLOAK_ADMIN_ID, KEYCLOAK_ADMIN_PWD) 도 base64 encoding 한 후, 각각 `KEYCLOAK_MASTER_USERNAME`, `KEYCLOAK_MASTER_PASSWORD` 의 value 값을 변경한다.

(:white_check_mark: KeyCloak 설치 시 admin id/password 변경하지 않은 경우 그대로 사용하면 됨)
