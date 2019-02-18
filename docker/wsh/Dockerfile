FROM alpine:3.9

# apk --no-cache := https://github.com/gliderlabs/docker-alpine/blob/master/docs/usage.md#disabling-cache

    # install base command
RUN apk -v --no-cache add bash curl \
    # install kubectl
    && curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl \
    && chmod +x ./kubectl && mv ./kubectl /usr/local/bin/kubectl \
    # install helm
    && apk -v --no-cache add openssl \
    && curl https://raw.githubusercontent.com/helm/helm/master/scripts/get | sh -s -- -v v2.9.1
    #&& helm init -c   # on bootstrap.sh

COPY bootstrap.sh /

ENTRYPOINT ["/bin/bash"]
CMD ["bootstrap.sh"]

# build script
# $ cd zcp-iam/docker/wsh
# $ docker build . -t cloudzcp/wsh:0.0.3 
# $ docker push cloudzcp/wsh:0.0.3