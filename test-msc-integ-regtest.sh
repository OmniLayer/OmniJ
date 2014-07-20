#!/bin/bash
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

BTCD=copied-artifacts/src/bitcoind
DATADIR=$HOME/.bitcoin
MSCLOG=/tmp/mastercore.log

# Assume bitcoind built elsewhere and coied by Jenkins Copy Artifact plugin
chmod +x $BTCD

# Set up bitcoin conf and data dir
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR

# setup logging
mkdir -p logs
#rm -f $MSCLOG 
touch $MSCLOG
ln -sf $MSCLOG logs/mastercore.log

# remove persistence files since 
rm -rf $DATADIR/MP_{persist,spinfo,txlist}

# Run Bitcoin in regtest mode
$BTCD -server -regtest -datadir=$DATADIR -debug > logs/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

# Give server some time to start
sleep 30

# Run integration tests
echo "Running integration tests in regtest mode..."
./gradlew integrationTest --tests com.msgilligan.*
GRADLESTATUS=$?

exit $GRADLESTATUS
