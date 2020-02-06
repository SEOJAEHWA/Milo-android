package kr.co.aiblab.test.milo.milo

import org.eclipse.milo.opcua.sdk.client.OpcUaClient

interface OpcUaCallback {

    interface Connection {

        fun connected(client: OpcUaClient)

        fun connectionFailed(e: Exception)
    }

    interface Disconnection {

        fun disconnected(client: OpcUaClient)

        fun disconnectionFailed(e: Exception)
    }

    interface MiloClientExecute {

        fun executeFailed(e: Exception)
    }
}