#!/bin/bash
set -x

function cleanup {
    kill -3 $BTCPID
}
trap cleanup EXIT

# Assume bitcoind built elsewhere and coied by Jenkins Copy Artifact plugin
BTCD=copied-artifacts/src/bitcoind
DATADIR=regtest-datadir
chmod +x $BTCD

# Run Bitcoin in regtest mode
mkdir -p $DATADIR
cp bitcoin.conf $DATADIR
mkdir -p logs
$BTCD -server -regtest -datadir=$DATADIR -debug > logs/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

# Give server some time to start
sleep 30

# Run integration tests
echo "Running integration tests in regtest mode..."
./gradlew test --tests com.msgilligan.bitcoin.rpc*
GRADLESTATUS=$?

exit $GRADLESTATUS
