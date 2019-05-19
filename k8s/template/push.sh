#!/bin/bash

source setenv.sh

registry=${domain_prefix}registry.cloudzcp.io
from=cloudzcp/wsh:1.2.0
to=${registry}/${from}

echo ""
echo "Move a web ssh docker image ${from} -> ${to}"
docker pull ${from}
docker tag ${from} ${to}

echo ""
docker login ${registry}
docker push ${to}

echo ""
docker pull ${to}