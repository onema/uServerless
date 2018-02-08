#!/usr/bin/env bash
# Use this for local deployment
# ./build.sh --profile staging
COMMAND=$1
echo $COMMAND
STAGE_NAME=$2
shift 2
STAGE_NAME=${STAGE_NAME:-dev}

function checkExitCode() {
    ret=$?
    if [ $1 -ne 0 ]; then
        exit 1;
    fi
}

function install() {
    echo "CURRENT DIRECTORY"
    pwd
    sbt compile
    checkExitCode $?

    sbt assembly
    checkExitCode $?

    checkExitCode $?
}

function deploy() {
    echo $@
    serverless deploy --stage "${STAGE_NAME}" $@
    checkExitCode $?
}

function cleanup() {
    rm -rf package
    rm package.zip
}

function remove() {
    echo $@
    serverless remove --stage "${STAGE_NAME}" $@
    checkExitCode $?
}

case "$COMMAND" in
    'install')
         install ;;

    'deploy')
#        install
        deploy $@
        cleanup ;;

    'remove')
        remove  $@
        ;;

    'cleanup')
        cleanup ;;

    *)
        echo """usage: deploy.sh [COMMAND] [STATE_NAME]
            install         Installs all packages and moves source code to a package directory.
            deploy          Installs, deploys and cleans up package. Deployment uses serverless framework.
            cleanup         Removes the package directory.
        """
        exit 0 ;;
esac
