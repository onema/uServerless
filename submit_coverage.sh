#!/usr/bin/env bash

if [[ "${CODEBUILD_SOURCE_VERSION}" =~ [0-9a-f]{5,40} ]] ; then
  echo "Generating coverage report"
  sbt coverageReport;
  echo "Publishing coverage report"
  sbt codacyCoverage;
fi