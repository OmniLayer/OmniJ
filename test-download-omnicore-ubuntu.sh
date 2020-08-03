#!/bin/bash
OMNICORE_HOST=https://bintray.com/artifact/download/omni/OmniBinaries
OMNICORE_RELEASE=omnicore-0.8.2
OMNICORE_FILE=$OMNICORE_RELEASE-x86_64-linux-gnu.tar.gz
OMNICORE_HASH=13fad4537f98ab5356454436df6a808995c2c462563d4256a191fea0f10458e9

wget "$OMNICORE_HOST/$OMNICORE_FILE"
echo "$OMNICORE_HASH  $OMNICORE_FILE" | shasum --algorithm 256 --check
mkdir -p copied-artifacts/src/
tar zxvf $OMNICORE_FILE -C /tmp
mv /tmp/$OMNICORE_RELEASE/bin/omnicored copied-artifacts/src/
