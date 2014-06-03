#!/bin/sh
CLI=bitcoin-cli
$CLI -regtest -datadir=regtest-datadir getblockcount
$CLI -regtest -datadir=regtest-datadir setgenerate true 101 
$CLI -regtest -datadir=regtest-datadir getblockcount
