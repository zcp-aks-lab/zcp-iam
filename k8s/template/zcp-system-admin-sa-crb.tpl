apiVersion: v1
kind: ServiceAccount
metadata:
  labels:
    cloudzcp.io/zcp-system-admin: 'yes'
  name: ${sa}
  namespace: ${namespace}
# TODO
imagePullSecrets:
- name: bluemix-cloudzcp-secret

# TODO
# should add the imagePullSecrets into the default sa of the zcp-system namespace
  
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  labels:
    cloudzcp.io/zcp-system-admin: 'yes'
  name: ${sa}
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: ${sa}
  namespace: ${namespace}
  
---
apiVersion: v1
kind: ServiceAccount
metadata:
  labels:
    cloudzcp.io/zcp-system: 'true'
    cloudzcp.io/zcp-system-user: 'true'
    cloudzcp.io/zcp-system-username: cloudzcp-admin
  name: zcp-system-sa-cloudzcp-admin
  namespace: ${namespace}
  
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  labels:
    cloudzcp.io/zcp-system: 'true'
    cloudzcp.io/zcp-system-user: 'true'
    cloudzcp.io/zcp-system-username: cloudzcp-admin
  name: zcp-system-crb-cloudzcp-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: zcp-system-sa-cloudzcp-admin
  namespace: ${namespace}
  
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  labels:
    cloudzcp.io/zcp-system: 'true'
    cloudzcp.io/zcp-system-user: 'true'
    cloudzcp.io/zcp-system-username: cloudzcp-admin
  name: zcp-system-rb-cloudzcp-admin
  namespace: default
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: view
subjects:
- kind: ServiceAccount
  name: zcp-system-sa-cloudzcp-admin
  namespace: ${namespace}
