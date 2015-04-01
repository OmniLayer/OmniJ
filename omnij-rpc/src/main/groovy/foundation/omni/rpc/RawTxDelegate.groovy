package foundation.omni.rpc

/**
 * Add raw Tx creation to any class
 */
trait RawTxDelegate {
    @Delegate
    RawTxBuilder builder
}