#!/bin/sh
CLI=bitcoin-cli
TESTADDR=`$CLI -regtest -datadir=regtest-datadir getnewaddress`
TESTTX=`$CLI -regtest -datadir=regtest-datadir sendtoaddress $TESTADDR 1.0`
$CLI -regtest -datadir=regtest-datadir gettransaction $TESTTX
echo Adresses
$CLI -regtest -datadir=regtest-datadir getaddressesbyaccount 
echo Balance
$CLI -regtest -datadir=regtest-datadir getbalance 

