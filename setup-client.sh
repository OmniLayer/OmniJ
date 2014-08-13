#!/bin/sh
set -x
DATADIR=bitcoin-datadir
CLI="copied-artifacts/src/bitcoin-cli -regtest -datadir=$DATADIR"
$CLI setgenerate true 101 
$CLI getblockcount
