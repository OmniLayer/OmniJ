#!/bin/bash
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

BTCD=copied-artifacts/src/mastercored
DATADIR=$HOME/.bitcoin
MSCLOG=/tmp/mastercore.log

# Assume mastercored built elsewhere and coied by Jenkins Copy Artifact plugin
chmod +x $BTCD

# Set up bitcoin conf and data dir
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR

# setup logging
mkdir -p logs
#rm -f $MSCLOG 
touch $MSCLOG
ln -sf $MSCLOG logs/mastercore.log

# remove Master Protocol persistence directories/files
rm -rf $DATADIR/MP_*

# Run Bitcoin on main net mode
$BTCD -server -datadir=$DATADIR -debug > logs/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

# Give server some time to start
sleep 30

# Run consensus tests
echo "Running consensus tests..."
./gradlew consensusTest
GRADLESTATUS=$?

exit $GRADLESTATUS
