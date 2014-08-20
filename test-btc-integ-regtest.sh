#!/bin/bash
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

BTCD=copied-artifacts/src/bitcoind
DATADIR=$HOME/.bitcoin
MSCLOG=/tmp/mastercore.log

# Assume bitcoind built elsewhere and copied by Jenkins Copy Artifact plugin
chmod +x $BTCD

# Set up bitcoin conf and data dir
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR

# setup logging
mkdir -p logs
#rm -f $MSCLOG 
touch $MSCLOG
ln -sf $MSCLOG logs/mastercore.log

# remove all regtest data
rm -rf $DATADIR/regtest

# Run Bitcoin in regtest mode
$BTCD -server -regtest -datadir=$DATADIR -debug > logs/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

# Give server some time to start
sleep 30

# Run integration tests
echo "Running BTC integration tests in regtest mode..."
./gradlew regTestBTC
GRADLESTATUS=$?

exit $GRADLESTATUS
