#!/bin/bash
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

# Assume bitcoind built elsewhere and coied by Jenkins Copy Artifact plugin
BTCD=copied-artifacts/src/bitcoind
DATADIR=$HOME/.bitcoin
chmod +x $BTCD

# Run Bitcoin on main net mode
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR
mkdir -p logs
$BTCD -server -datadir=$DATADIR -debug > logs/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

# Give server some time to start
sleep 30

# Run consensus tests
echo "Running consensus tests..."
./gradlew test --tests com.msgilligan.mastercoin.consensus.msc.*
GRADLESTATUS=$?

exit $GRADLESTATUS
