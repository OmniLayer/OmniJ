package com.msgilligan.bitcoinj.rpc;

import org.bitcoinj.core.Sha256Hash;

/**
 * Data class for Outpoint as used by RPC methods
 */
public class Outpoint {
    private final Sha256Hash txid;
    private final int         vout;

    public Outpoint(Sha256Hash txid, int vout) {
        this.txid = txid;
        this.vout = vout;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public int getVout() {
        return vout;
    }
}
