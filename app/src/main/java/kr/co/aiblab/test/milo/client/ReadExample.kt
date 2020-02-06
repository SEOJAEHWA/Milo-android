package kr.co.aiblab.test.milo.client

import androidx.lifecycle.MutableLiveData
import com.google.common.collect.ImmutableList
import kr.co.aiblab.milo.MiloClient
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.enumerated.ServerState
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn


class ReadExample(
    private val data: MutableLiveData<String>
) : MiloClient {

    override fun execute(
        client: OpcUaClient
    ) {
        val nodeIds: List<NodeId> = ImmutableList.of(
            Identifiers.Server_ServerStatus_State,
            Identifiers.Server_ServerStatus_CurrentTime
        )

        val readValues = client.readValues(0.0, TimestampsToReturn.Both, nodeIds)

        val dataValue = readValues.get()
        dataValue?.let {
            val state = ServerState.from(it[0]!!.value.value as Int)
            val currentTime = it[1]!!.value.value
            val message = "State=$state\nCurrentTime=$currentTime"
            data.postValue(message)
        }
    }
}