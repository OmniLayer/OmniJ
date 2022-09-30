package foundation.omni.rpc

import foundation.omni.tx.RawTxBuilder

/**
 * Add raw Tx creation to any class
 * @deprecated Don't use delegation, keep a reference to a RawTxBuilder object
 */
@Deprecated
trait RawTxDelegate {
    @Delegate
    RawTxBuilder builder = new RawTxBuilder()
}
