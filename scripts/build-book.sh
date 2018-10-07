#!/usr/bin/env bash

if [ ! -d docs ]; then
  mkdir docs
fi
rm -rf docs/*
cp -r book/target/paradox/site/main/* docs/
