#!/bin/bash
set -x

# Assume bitcoind built elsewhere and copied by Jenkins Copy Artifact plugin
BTCD=./copied-artifacts/src/bitcoind
DATADIR=$HOME/.bitcoin
chmod +x $BTCD

# Run Bitcoin in main net mode
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR
mkdir -p logs
$BTCD -server -datadir=$DATADIR -debug > logs/bitcoin.log
