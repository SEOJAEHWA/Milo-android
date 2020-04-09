package kr.co.aiblab.test.milo.client

import androidx.lifecycle.MutableLiveData
import kr.co.aiblab.milo.MiloClient
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId


class SubscribeExample(
    private val data: MutableLiveData<String>,
    private val nodeId: NodeId
) : MiloClient {

    override fun execute(
        client: OpcUaClient
    ) {
        val subscription: UaSubscription = client.subscriptionManager
            .createSubscription(1000.0)
            .get()

        val readValueId = ReadValueId(
            nodeId,
            AttributeId.Value.uid(),
            null,
            QualifiedName.NULL_VALUE
        )

        val clientHandle = subscription.nextClientHandle()

        val parameters = MonitoringParameters(
            clientHandle,
            3000.0,
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
                        "[Subscribing...]" +
                                "\nSubscription value received:" +
                                "\n    item=${it.readValueId.nodeId}, " +
                                "\n    value=${value.value}" +
                                "\n    serverTime=${value.serverTime}" +
                                "\n"
                    data.postValue(message)
                }
            }
        }.get()

        for (item in items) {
            val message: String = if (item.statusCode.isGood) {
                "[Subscribe] " +
                        "\nItem created for nodeId=${item.readValueId.nodeId}"
            } else {
                "[Subscribe] " +
                        "\nFailed to create item for nodeId=${item.readValueId.nodeId} " +
                        "(status=${item.statusCode})"
            }
            data.postValue(message)
        }
    }
}