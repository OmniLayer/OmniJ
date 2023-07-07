#!/bin/bash
OMNICORE_BASE=https://github.com/OmniLayer/omnicore/releases/download
OMNICORE_VERSION=0.12.0.1
TARGETPLATFORM=x86_64-linux-gnu
OMNICORE_RELEASE=omnicore-${OMNICORE_VERSION}
OMNICORE_FILE=omnicore-${OMNICORE_VERSION}-${TARGETPLATFORM}.tar.gz
OMNICORE_URL=${OMNICORE_BASE}/v${OMNICORE_VERSION}/${OMNICORE_FILE}
OMNICORE_HASH=d6ac1e9af48f9d77ea88b697f35d3825a9b8c972f8de37f1213b433945a435a5

wget "$OMNICORE_URL"
echo "$OMNICORE_HASH $OMNICORE_FILE" | shasum --algorithm 256 --check
mkdir -p copied-artifacts/src/
tar zxvf $OMNICORE_FILE -C /tmp
mv /tmp/$OMNICORE_RELEASE/bin/omnicored copied-artifacts/src/
