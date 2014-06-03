#!/bin/sh
TESTADDR=`bitcoin-cli -regtest -datadir=regtest-datadir getnewaddress`
TESTTX=`bitcoin-cli -regtest -datadir=regtest-datadir sendtoaddress $TESTADDR 1.0`
bitcoin-cli -regtest -datadir=regtest-datadir gettransaction $TESTTX
echo Adresses
bitcoin-cli -regtest -datadir=regtest-datadir getaddressesbyaccount 
echo Balance
bitcoin-cli -regtest -datadir=regtest-datadir getbalance 

