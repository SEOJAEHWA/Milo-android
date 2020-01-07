package kr.co.aiblab.test.milo.client

import android.util.Log
import com.google.common.collect.ImmutableList
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.nodes.VariableNode
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.enumerated.ServerState
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import java.util.concurrent.CompletableFuture


class ReadExample : MiloClient {

    override fun run(client: OpcUaClient, future: CompletableFuture<OpcUaClient>) {
        // synchronous connect
        client.connect().get()

        // synchronous read request via VariableNode
        val node: VariableNode = client.addressSpace
            .createVariableNode(Identifiers.Server_ServerStatus_StartTime)
        val value: DataValue = node.readValue().get()

        Log.i("MILO", "StartTime=${value.value.value}")

        // asynchronous read request
        readServerStateAndTime(client)!!.thenAccept { values: List<DataValue?>? ->
            val v0 = values!![0]
            val v1 = values[1]
            Log.d("MILO", "State=${ServerState.from(v0!!.value.value as Int)}")
            Log.d("MILO", "CurrentTime=${v1!!.value.value}")

            future.complete(client)
        }
    }

    private fun readServerStateAndTime(client: OpcUaClient): CompletableFuture<List<DataValue?>?>? {
        val nodeIds: List<NodeId> = ImmutableList.of(
            Identifiers.Server_ServerStatus_State,
            Identifiers.Server_ServerStatus_CurrentTime
        )
        return client.readValues(0.0, TimestampsToReturn.Both, nodeIds)
    }
}