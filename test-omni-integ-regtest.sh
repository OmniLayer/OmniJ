#!/bin/bash
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

BTCD=copied-artifacts/src/omnicored
DATADIR=$HOME/.bitcoin
LOGDIR=logs
MSCLOG=/tmp/mastercore.log

# Assume omnicored built elsewhere and copied by Jenkins Copy Artifact plugin
chmod +x $BTCD

# Setup bitcoin conf and data dir
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR

# setup logging
mkdir -p $LOGDIR
touch $MSCLOG
ln -sf $MSCLOG $LOGDIR/mastercore.log

# Remove all regtest data
rm -rf $DATADIR/regtest

# Run omnicored in regtest mode
$BTCD -regtest -datadir=$DATADIR -omnialertallowsender=any -omniactivationallowsender=any -paytxfee=0.0001 -minrelaytxfee=0.00001 -limitancestorcount=750 -limitdescendantcount=750 -rpcserialversion=0 > $LOGDIR/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

# Give server some time to start
#sleep 30

# Run integration tests
echo "Running Omni RPC integration tests in RegTest mode..."
./gradlew clean :omnij-rpc:regTest :omnij-cli:regTest
GRADLESTATUS=$?

exit $GRADLESTATUS
