#!/usr/bin/env bash

IMAGE_TAG=${1:-'latest'}
IMAGE="haradayoshiaki777/sns-db:${IMAGE_TAG}"

docker build --no-cache -t ${IMAGE} .

docker push ${IMAGE}