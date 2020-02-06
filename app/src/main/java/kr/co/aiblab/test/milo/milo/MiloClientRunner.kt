package kr.co.aiblab.test.milo.milo

import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig
import org.eclipse.milo.opcua.stack.client.DiscoveryClient
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import java.security.Security


class MiloClientRunner(
    private val keyStoreLoader: KeyStoreLoader
) {
    @Throws(Exception::class)
    private fun createClient(): OpcUaClient {
        val endpoint = getEndpointDescription()
        Logger.d(
            "Using endpoint: ${endpoint.endpointUrl} " +
                    "[SecurityPolicy()/${endpoint.securityMode}]"
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

    suspend fun get(): OpcUaClient = withContext(Dispatchers.Default) {
        createClient()
    }

    private fun getEndpointDescription(): EndpointDescription = try {
        DiscoveryClient.getEndpoints(MiloClient.endpointUrl).get()
    } catch (ex: Throwable) { // try the explicit discovery endpoint as well
        Logger.e(ex.message ?: "")
        var discoveryUrl: String = MiloClient.endpointUrl
        if (!discoveryUrl.endsWith("/")) {
            discoveryUrl += "/"
        }
        discoveryUrl += "discovery"
        Logger.d("Trying explicit discovery URL: $discoveryUrl")
        DiscoveryClient.getEndpoints(discoveryUrl).get()
    }.stream()
        .filter { e: EndpointDescription ->
            e.securityPolicyUri == MiloClient.securityPolicy.uri
        }
        .filter(MiloClient.endpointFilter)
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
