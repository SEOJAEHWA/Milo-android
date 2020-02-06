package kr.co.aiblab.test.milo.client

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.aiblab.test.milo.milo.MiloClient
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId


class SubscribeExample(
    private val data: MutableLiveData<String>
) : MiloClient {

    override suspend fun execute(
        client: OpcUaClient
    ) {
        withContext(Dispatchers.IO) {
            val subscription: UaSubscription = client.subscriptionManager
                .createSubscription(1000.0)
                .get()

            val readValueId = ReadValueId(
                Identifiers.Server_ServerStatus_CurrentTime,
                AttributeId.Value.uid(),
                null,
                QualifiedName.NULL_VALUE
            )

            val clientHandle = subscription.nextClientHandle()

            val parameters = MonitoringParameters(
                clientHandle,
                5000.0,
                null,
                UInteger.valueOf(10),
                true
            )

            val request = MonitoredItemCreateRequest(
                readValueId,
                MonitoringMode.Reporting,
                parameters
            )

            val items = subscription.createMonitoredItems(
                TimestampsToReturn.Both,
                listOf(request)
            ) { item, _ ->
                item.setValueConsumer { it: UaMonitoredItem, value: DataValue ->
                    run {
                        val message =
                            "subscription value received: item=${it.readValueId.nodeId}, " +
                                    "value=${value.value}"
                        data.postValue(message)
                    }
                }
            }.get()

            for (item in items) {
                val message: String = if (item.statusCode.isGood) {
                    "item created for nodeId=${item.readValueId.nodeId}"
                } else {
                    "failed to create item for nodeId=${item.readValueId.nodeId} " +
                            "(status=${item.statusCode})"
                }
                data.postValue(message)
            }
        }
    }
}