#!/bin/sh
set -x
DATADIR=bitcoin-datadir
CLI="copied-artifacts/src/bitcoin-cli -regtest -datadir=$DATADIR"
ACCOUNTNAME="msc-account"
ACCOUNTADDR=`$CLI getaccountaddress $ACCOUNTNAME`
EXTADDR="moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP"
$CLI sendtoaddress $ACCOUNTADDR 11.0001
$CLI setgenerate true 1
AMOUNTS=`printf '{"%s":%s,"%s":%s}' $EXTADDR 10.0 $ACCOUNTADDR 1.0`
echo "AMOUNTS JSON = $AMOUNTS"
TXID=`$CLI sendmany $ACCOUNTNAME $AMOUNTS`
$CLI setgenerate true 1
TX=`$CLI gettransaction $TXID`
RAWTX=`$CLI getrawtransaction $TXID 1`
BALANCE=`$CLI getbalance $ACCOUNTNAME`
RECEIVED=`$CLI getreceivedbyaddress $ACCOUNTADDR`


