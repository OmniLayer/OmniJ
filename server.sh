#!/bin/sh
set -x
BTCD=bitcoind
mkdir -p regtest-datadir
cp bitcoin.conf regtest-datadir
$BTCD -server -regtest -datadir=regtest-datadir -debug

