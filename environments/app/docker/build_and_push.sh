#!/usr/bin/env bash

IMAGE_TAG=${1:-'latest'}
IMAGE="haradayoshiaki777/sns-api:${IMAGE_TAG}"

DOCKER_BUILDKIT=1 docker image build -t ${IMAGE} -f ./Dockerfile ../../../sns-clone-by-tdd/

docker push  ${IMAGE}