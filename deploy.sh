#!/usr/bin/env bash
checkExitCode() {
    ret=$?
    if [ $1 -ne 0 ]; then
        exit 1;
    fi
}

sbt compile
checkExitCode $?

sbt assembly
checkExitCode $?

serverless deploy $@
checkExitCode $?
