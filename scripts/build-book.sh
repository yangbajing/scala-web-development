#!/usr/bin/env bash

sbt book/paradox

if [ ! -d docs ]; then
  mkdir docs
fi
rm -rf docs/*
cp -r book/target/paradox/site/main/* docs/

