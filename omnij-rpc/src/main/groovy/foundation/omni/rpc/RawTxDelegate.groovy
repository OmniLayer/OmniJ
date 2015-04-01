package foundation.omni.rpc

import foundation.omni.tx.RawTxBuilder

/**
 * Add raw Tx creation to any class
 */
trait RawTxDelegate {
    @Delegate
    RawTxBuilder builder
}