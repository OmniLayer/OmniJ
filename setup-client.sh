#!/bin/sh
bitcoin-cli -regtest -datadir=regtest-datadir getblockcount
bitcoin-cli -regtest -datadir=regtest-datadir setgenerate true 101 
bitcoin-cli -regtest -datadir=regtest-datadir getblockcount
