package kr.co.aiblab.test.milo.client

import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import kr.co.aiblab.milo.MiloClient
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult
import org.eclipse.milo.opcua.stack.core.util.ConversionUtil.l
import java.util.concurrent.CompletableFuture


class MethodExample(
    private val data: MutableLiveData<String>
) : MiloClient {

    override fun execute(
        client: OpcUaClient
    ) {
        val input = 16.0
        sqrt(client, input).exceptionally {
            Logger.e(it, "error invoking sqrt()")
            data.postValue("error invoking sqrt() >> -1.0")
            -1.0
        }.thenAccept {
            Logger.i("sqrt($input) = $it")
            data.postValue("sqrt($input) = $it")
        }
    }

    private fun sqrt(
        client: OpcUaClient,
        input: Double
    ): CompletableFuture<Double> {
        val objectId = NodeId.parse("ns=2;s=HelloWorld")
        val methodId = NodeId.parse("ns=2;s=HelloWorld/sqrt(x)")
        val request = CallMethodRequest(objectId, methodId, arrayOf(Variant(input)))
        return client.call(request).thenCompose<Double> { result: CallMethodResult ->
            val statusCode: StatusCode = result.statusCode
            if (statusCode.isGood) {
                val value = l(result.outputArguments)[0].value as Double
                return@thenCompose CompletableFuture.completedFuture(value)
            } else {
                val inputArgumentResults: Array<StatusCode>? = result.inputArgumentResults
                val stringBuilder = StringBuilder()
                for (i in inputArgumentResults!!.indices) {
                    stringBuilder.append("inputArgumentResults[${i}]=${inputArgumentResults[i]}")
                        .append("\n")
                }
                Logger.d(stringBuilder.toString())
                val completableFuture: CompletableFuture<Double> = CompletableFuture()
                completableFuture.completeExceptionally(UaException(statusCode))
                return@thenCompose completableFuture
            }
        }
    }
}