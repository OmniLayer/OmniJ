#!/bin/bash
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

BTCD=copied-artifacts/src/mastercored
DATADIR=$HOME/.bitcoin
LOGDIR=logs
MSCLOG=/tmp/mastercore.log

# Assume mastercored built elsewhere and copied by Jenkins Copy Artifact plugin
chmod +x $BTCD

# Setup bitcoin conf and data dir
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR

# setup logging
mkdir -p $LOGDIR
touch $MSCLOG
ln -sf $MSCLOG $LOGDIR/mastercore.log

# Remove Omni Core persistence directories/files
rm -rf $DATADIR/MP_*

# Run mastercored in mainnet mode
$BTCD -datadir=$DATADIR > $LOGDIR/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

# Give server some time to start
sleep 30

# Run consensus tests
echo "Running consensus tests..."
./gradlew :omnij-rpc:consensusTest
GRADLESTATUS=$?

exit $GRADLESTATUS
