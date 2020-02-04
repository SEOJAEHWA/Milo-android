package kr.co.aiblab.test.milo.client

import androidx.lifecycle.MutableLiveData
import com.google.common.collect.ImmutableList
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.aiblab.test.milo.milo.MiloClient
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn


class ReadExample : MiloClient<List<DataValue?>?> {

    override suspend fun execute(
        client: OpcUaClient
        // FIXME LiveData 를 넘겨 observing?
    ): List<DataValue?>? = withContext(Dispatchers.IO) {

        val node = client.addressSpace.createVariableNode(Identifiers.Server_ServerStatus_StartTime)
        val value: DataValue = node.readValue().get()

        Logger.i("StartTime=${value.value.value}")

        val nodeIds: List<NodeId> = ImmutableList.of(
            Identifiers.Server_ServerStatus_State,
            Identifiers.Server_ServerStatus_CurrentTime
        )

        val readValues = client.readValues(0.0, TimestampsToReturn.Both, nodeIds)
        readValues.get()
    }

    override suspend fun execute(
        client: OpcUaClient,
        data: MutableLiveData<List<DataValue?>?>
    ) {
        withContext(Dispatchers.IO) {
            val node = client.addressSpace.createVariableNode(Identifiers.Server_ServerStatus_StartTime)
            val value: DataValue = node.readValue().get()

            Logger.i("StartTime=${value.value.value}")

            val nodeIds: List<NodeId> = ImmutableList.of(
                Identifiers.Server_ServerStatus_State,
                Identifiers.Server_ServerStatus_CurrentTime
            )

            val readValues = client.readValues(0.0, TimestampsToReturn.Both, nodeIds)
            data.postValue(readValues.get())
        }
    }
}