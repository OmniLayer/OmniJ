#!/bin/bash
set -x

DEBUG=$1
if [ $DEBUG = "0" ] ; then
DBGFLAG = ""
else
DBGFLAG="-debug"
fi

BTCD=copied-artifacts/src/bitcoind
DATADIR=bitcoin-datadir
MSCLOG=/tmp/mastercore.log

# Assume bitcoind built elsewhere and copied by Jenkins Copy Artifact plugin
chmod +x $BTCD

# Set up bitcoin conf and data dir
rm -rf $DATADIR/regtest
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR

# setup logging
mkdir -p logs
#rm -f $MSCLOG 
touch $MSCLOG
ln -sf $MSCLOG logs/mastercore.log

# remove persistence files since 
#rm -rf $DATADIR/MP_{persist,spinfo,txlist}

# Run Bitcoin in regtest mode
$BTCD -printtoconsole -server -regtest -datadir=$DATADIR $DBGFLAG > logs/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!
