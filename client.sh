#!/bin/sh
set -x
CLI=bitcoin-cli
TESTADDR=`$CLI -regtest -datadir=regtest-datadir getnewaddress`
echo "TESTADDR is $TESTADDR"
TESTTX=`$CLI -regtest -datadir=regtest-datadir sendtoaddress $TESTADDR 1.0`
echo sendtoaddress returned $?
$CLI -regtest -datadir=regtest-datadir gettransaction $TESTTX
$CLI -regtest -datadir=regtest-datadir setgenerate true 1
$CLI -regtest -datadir=regtest-datadir gettransaction $TESTTX
echo Adresses
$CLI -regtest -datadir=regtest-datadir getaddressesbyaccount 
echo Balance
$CLI -regtest -datadir=regtest-datadir getbalance 

