package kr.co.aiblab.test.milo.client

import androidx.lifecycle.MutableLiveData
import kr.co.aiblab.milo.MiloClient
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription


class BrowseExample(
    private val data: MutableLiveData<String>
) : MiloClient {

    override fun execute(
        client: OpcUaClient
    ) {
        val browse = BrowseDescription(
            Identifiers.RootFolder,
            BrowseDirection.Forward,
            Identifiers.References,
            true,
            UInteger.valueOf(NodeClass.Object.value or NodeClass.Variable.value),
            UInteger.valueOf(BrowseResultMask.All.value)
        )

        val browseResult = client.browse(browse).get()
        browseResult.references?.asList()?.let {
            val stringBuilder = StringBuilder()
            for (referenceDescription in it) {
                stringBuilder.append("Node=${referenceDescription.browseName.name}")
                stringBuilder.append("\n")
            }
            data.postValue(stringBuilder.toString())
        }
    }
}