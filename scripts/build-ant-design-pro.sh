#!/usr/bin/env bash

pushd ant-design-pro/web
yarn install
yarn run build
popd
rm -rf ant-design-pro/src/main/resources/dist/*
cp ant-design-pro/web/dist/* ant-design-pro/src/main/resources/dist/
sbt "project ant-design-pro" assembly
