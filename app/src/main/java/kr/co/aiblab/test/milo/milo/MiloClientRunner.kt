package kr.co.aiblab.test.milo.milo

import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig
import org.eclipse.milo.opcua.stack.client.DiscoveryClient
import org.eclipse.milo.opcua.stack.core.Stack
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import java.security.Security
import java.util.concurrent.ExecutionException


class MiloClientRunner<T>(
    private val keyStoreLoader: KeyStoreLoader,
    private val miloClient: MiloClient<T>
) {
    @Throws(Exception::class)
    private fun createClient(client: MiloClient<T>): OpcUaClient {
        val endpoint = getEndpointDescription(client)
        Logger.d(
            "Using endpoint: ${endpoint.endpointUrl} " +
                    "[$client.getSecurityPolicy()/${endpoint.securityMode}]"
        )

        val config = with(OpcUaClientConfig.builder()) {
            setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
            setApplicationUri("urn:eclipse:milo:examples:client")
            setCertificate(keyStoreLoader.clientCertificate)
            setKeyPair(keyStoreLoader.clientKeyPair)
            setEndpoint(endpoint)
            build()
        }
        return OpcUaClient.create(config)
    }

    suspend fun run(): T = withContext(Dispatchers.IO) {
        val client = createClient(miloClient)

        client.connect().get()

        val result = miloClient.execute(client)

        try {
            client.disconnect().get()
            Stack.releaseSharedResources()
        } catch (e: InterruptedException) {
            Logger.e("Error disconnecting:${e.message}", e)
        } catch (e: ExecutionException) {
            Logger.e("Error disconnecting:${e.message}", e)
        }

        result
    }

    private fun getEndpointDescription(client: MiloClient<T>): EndpointDescription =
        try {
            DiscoveryClient.getEndpoints(client.getEndpointUrl()).get()
        } catch (ex: Throwable) { // try the explicit discovery endpoint as well
            Logger.e(ex.message ?: "")
            var discoveryUrl: String = client.getEndpointUrl()
            if (!discoveryUrl.endsWith("/")) {
                discoveryUrl += "/"
            }
            discoveryUrl += "discovery"
            Logger.d("Trying explicit discovery URL: $discoveryUrl")
            DiscoveryClient.getEndpoints(discoveryUrl).get()
        }.stream()
            .filter { e: EndpointDescription ->
                e.securityPolicyUri == client.getSecurityPolicy().uri
            }
            .filter(client.endpointFilter())
            .findFirst()
            .orElseThrow {
                Exception("no desired endpoints returned")
            }

    companion object {
        init {
            // Required for SecurityPolicy.Aes256_Sha256_RsaPss
            Security.addProvider(BouncyCastleProvider())
        }
    }
}