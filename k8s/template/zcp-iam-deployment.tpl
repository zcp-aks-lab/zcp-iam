apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: zcp-iam
  namespace: ${namespace}
spec:
  replicas: ${replicas}
  template:
    metadata:
      labels:
        component: zcp-iam
        app: zcp-iam
      annotations: # https://www.weave.works/docs/cloud/latest/tasks/monitor/configuration-k8s/
        prometheus.io/path: /prometheus
        prometheus.io/port: '9000'
        prometheus.io/scrape: 'true'
    spec:
      tolerations:
      - key: "management"
        operator: "Equal"
        value: 'true'
        effect: "NoSchedule"
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: beta.kubernetes.io/arch
                operator: In
                values:
                - "amd64"
              - key: role
                operator: In
                values:
                - "management"
      containers:
      - name: zcp-iam
        image: ${image}
        imagePullPolicy: Always
        ports:
        - name: cont-port
          containerPort: 8181
        - name: prometheus
          containerPort: 9000
        resources:
          requests:
            memory: "512Mi"
            cpu: "200m"
          limits:
            memory: "1536Mi"
            cpu: "800m"
        envFrom:
        - configMapRef:
            name: zcp-iam-config
        env:
          - name: KEYCLOAK_MASTER_CLIENT_SECRET
            valueFrom:
              secretKeyRef:
                name: zcp-iam-secret
                key: KEYCLOAK_MASTER_CLIENT_SECRET
          - name: KEYCLOAK_MASTER_USERNAME
            valueFrom:
              secretKeyRef:
                name: zcp-iam-secret
                key: KEYCLOAK_MASTER_USERNAME
          - name: KEYCLOAK_MASTER_PASSWORD
            valueFrom:
              secretKeyRef:
                name: zcp-iam-secret
                key: KEYCLOAK_MASTER_PASSWORD
          - name: JENKINS_USER_TOKEN
            valueFrom:
              secretKeyRef:
                name: zcp-iam-secret
                key: JENKINS_USER_TOKEN
          - name: DB_USER
            valueFrom:
              secretKeyRef:
                name: zcp-iam-secret
                key: DB_USER
          - name: DB_PWD
            valueFrom:
              secretKeyRef:
                name: zcp-iam-secret
                key: DB_PWD
      serviceAccount: ${sa}
      serviceAccountName: ${sa}
---

apiVersion: v1
kind: Service
metadata:
  name: zcp-iam
  labels:
    component: zcp-iam
  namespace: ${namespace}
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8181
    protocol: TCP
    name: http
  selector:
    component: zcp-iam

