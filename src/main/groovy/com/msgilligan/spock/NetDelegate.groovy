package com.msgilligan.spock

/**
 * User: sean
 * Date: 7/31/14
 * Time: 12:38 PM
 */
trait NetDelegate {
    @Delegate NetClient client = new NetClient()
}
