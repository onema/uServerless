#!/usr/bin/env bash
# Use this for local deployment
# ./deploy.sh --aws-profile dev
COMMAND=$1
echo $COMMAND
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
    $PROFILE=$1
    serverless deploy --stage "${STAGE_NAME}" $PROFILE
    checkExitCode $?
}

function cleanup() {
    rm -rf package
    rm package.zip
}

function uninstall() {
    serverless remove --stage "${STAGE_NAME}"
}

case "$COMMAND" in
    'install')
         install ;;

    'deploy')
        $PROFILE=$@
        install
        deploy $PROFILE
        cleanup ;;

    'cleanup')
        cleanup ;;

    *)
        echo """usage: deploy.sh [COMMAND]
            install         Installs all packages and moves source code to a package directory.
            deploy          Installs, deploys and cleans up package. Deployment uses serverless framework.
            cleanup         Removes the package directory.
        """
        exit 0 ;;
esac
