#!/bin/sh
set -x
mkdir -p regtest-datadir
cp bitcoin.conf regtest-datadir
bitcoind -server -regtest -datadir=regtest-datadir -debug

