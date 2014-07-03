#!/bin/bash
set -x

function cleanup {
    kill $BTCPID
}
trap cleanup EXIT

# Assume bitcoind built elsewhere and coied by Jenkins Copy Artifact plugin
BTCD=./copied-artifacts/src/bitcoind
DATADIR=$HOME/.bitcoin
chmod +x $BTCD

# Run Bitcoin on main net mode
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR
mkdir -p logs
#$BTCD -server -datadir=$DATADIR -debug > logs/bitcoin.log &
$BTCD -server -debug
BTCSTATUS=$?
BTCPID=$!

# Run consensus tests
echo "Running consensus tests..."
./gradlew test --tests com.msgilligan.mastercoin.consensus

exit $BTCSTATUS


