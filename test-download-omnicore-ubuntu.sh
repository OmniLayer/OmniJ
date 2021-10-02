#!/bin/bash
OMNICORE_BASE=https://github.com/OmniLayer/omnicore/releases/download
OMNICORE_VERSION=0.11.0
TARGETPLATFORM=x86_64-linux-gnu
OMNICORE_RELEASE=omnicore-${OMNICORE_VERSION}
OMNICORE_FILE=omnicore-${OMNICORE_VERSION}-${TARGETPLATFORM}.tar.gz
OMNICORE_URL=${OMNICORE_BASE}/v${OMNICORE_VERSION}/${OMNICORE_FILE}
OMNICORE_HASH=71cd24d67a49c842692d385c785173ddec9ddee8edeb45a633a93b01552856ba

wget "$OMNICORE_URL"
echo "$OMNICORE_HASH $OMNICORE_FILE" | shasum --algorithm 256 --check
mkdir -p copied-artifacts/src/
tar zxvf $OMNICORE_FILE -C /tmp
mv /tmp/$OMNICORE_RELEASE/bin/omnicored copied-artifacts/src/
