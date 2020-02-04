package kr.co.aiblab.test.milo.client

import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.aiblab.test.milo.milo.MiloClient
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.*
import java.util.concurrent.atomic.AtomicLong


class SubscribeExample : MiloClient<Variant?> {

    private val clientHandles: AtomicLong = AtomicLong(1L)

    override suspend fun execute(
        client: OpcUaClient
    ): Variant? = withContext(Dispatchers.IO) {
        val subscription: UaSubscription = client.subscriptionManager
            .createSubscription(1000.0)
            .get()

        val readValueId = ReadValueId(
            Identifiers.Server,
            AttributeId.EventNotifier.uid(),
            null,
            QualifiedName.NULL_VALUE
        )

        val clientHandle = UInteger.valueOf(clientHandles.getAndIncrement())

        val parameters = MonitoringParameters(
            clientHandle,
            0.0,
            ExtensionObject.encode(client.serializationContext, eventFilter),
            UInteger.valueOf(1),
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
        ).get()

        val monitoredItem = items[0]

        monitoredItem.setEventConsumer { item: UaMonitoredItem, vs: Array<Variant> ->
            Logger.i("Event Received from ${item.readValueId.nodeId}")
//            for (i in vs.indices) {
//                Logger.i("\tvariant[$i]: ${vs[i].value}")
//            }
        }

        null
    }

    override suspend fun execute(
        client: OpcUaClient,
        data: MutableLiveData<Variant?>
    ) {

    }

    private var eventFilter: EventFilter = EventFilter(
        arrayOf(
            SimpleAttributeOperand(
                Identifiers.BaseEventType,
                arrayOf(
                    QualifiedName(
                        0,
                        "EventId"
                    )
                ),
                AttributeId.Value.uid(),
                null
            ),
            SimpleAttributeOperand(
                Identifiers.BaseEventType,
                arrayOf(
                    QualifiedName(
                        0,
                        "EventType"
                    )
                ),
                AttributeId.Value.uid(),
                null
            ),
            SimpleAttributeOperand(
                Identifiers.BaseEventType,
                arrayOf(
                    QualifiedName(
                        0,
                        "Severity"
                    )
                ),
                AttributeId.Value.uid(),
                null
            ),
            SimpleAttributeOperand(
                Identifiers.BaseEventType,
                arrayOf(
                    QualifiedName(
                        0,
                        "Time"
                    )
                ),
                AttributeId.Value.uid(),
                null
            ),
            SimpleAttributeOperand(
                Identifiers.BaseEventType,
                arrayOf(
                    QualifiedName(
                        0,
                        "Message"
                    )
                ),
                AttributeId.Value.uid(),
                null
            )
        ),
        ContentFilter(null)
    )
}