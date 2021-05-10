#!/bin/bash
set -x
OMNICORE_BASE=https://github.com/OmniLayer/omnicore/releases/download
OMNICORE_VERSION=0.10.0
TARGETPLATFORM=x86_64-linux-gnu
OMNICORE_RELEASE=omnicore-${OMNICORE_VERSION}
OMNICORE_FILE=omnicore-${OMNICORE_VERSION}-${TARGETPLATFORM}.tar.gz
OMNICORE_URL=${OMNICORE_BASE}/v${OMNICORE_VERSION}/${OMNICORE_FILE}
OMNICORE_HASH=13fad4537f98ab5356454436df6a808995c2c462563d4256a191fea0f10458e9

wget "$OMNICORE_URL"
echo "$OMNICORE_HASH $OMNICORE_FILE" | shasum --algorithm 256 --check
mkdir -p copied-artifacts/src/
tar zxvf $OMNICORE_FILE -C /tmp
mv /tmp/$OMNICORE_RELEASE/bin/omnicored copied-artifacts/src/
