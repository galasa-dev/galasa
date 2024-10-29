#! /usr/bin/env bash 

BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
# cd "${BASEDIR}"

./build-locally.sh --delta
rc=$? ; if [[ "$rc" != "0" ]]; then error "Failed to build" ; exit 1 ; fi

galasactl runs submit local  \
--obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr  \
--class dev.galasa.example.banking.payee/dev.galasa.example.banking.payee.TestPayee \
--galasahome $BASEDIR/temp/home \
--log -