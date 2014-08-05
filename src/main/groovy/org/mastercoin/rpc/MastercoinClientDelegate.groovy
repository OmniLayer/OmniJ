package org.mastercoin.rpc

/**
 * User: sean
 * Date: 7/23/14
 * Time: 10:54 PM
 */
trait MastercoinClientDelegate {
    @Delegate
    MastercoinClient client

//    def methodMissing(String name, args) {
//        client."$name"(*args)
//    }
//
//    def propertyMissing(String name) {
//        client."$name"
//    }
//
//    def propertyMissing(String name, value) {
//        client."$name" = value
//    }
}
