package kr.co.aiblab.test.milo.milo

import androidx.annotation.WorkerThread
import kr.co.aiblab.milo.MiloClient
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig
import org.eclipse.milo.opcua.stack.client.DiscoveryClient
import org.eclipse.milo.opcua.stack.core.Stack
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import java.security.Security
import java.util.concurrent.ExecutionException
import java.util.function.Predicate


class MiloClientRunner {

    private var opcUaClient: OpcUaClient? = null

    @Throws(Exception::class)
    private fun createClient(keyStoreLoader: KeyStoreLoader): OpcUaClient {
        val config = with(OpcUaClientConfig.builder()) {
            setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
            setApplicationUri("urn:eclipse:milo:examples:client")
            setCertificate(keyStoreLoader.clientCertificate)
            setKeyPair(keyStoreLoader.clientKeyPair)
            setEndpoint(getEndpointDescription())
            build()
        }
        return OpcUaClient.create(config)
    }

    private fun getClient(keyStoreLoader: KeyStoreLoader): OpcUaClient =
        createClient(keyStoreLoader)

    @WorkerThread
    fun connect(keyStoreLoader: KeyStoreLoader, callback: OpcUaCallback.Connection) {
        val client = getClient(keyStoreLoader)
        try {
            client.connect().get()
            callback.connected(client)
            opcUaClient = client
        } catch (e: InterruptedException) {
            callback.connectionFailed(e)
        } catch (e: ExecutionException) {
            callback.connectionFailed(e)
        }
    }

    @WorkerThread
    fun disconnect(callback: OpcUaCallback.Disconnection) {
        opcUaClient?.let {
            try {
                it.disconnect().get()
                callback.disconnected(it)
            } catch (e: InterruptedException) {
                callback.disconnectionFailed(e)
            } catch (e: ExecutionException) {
                callback.disconnectionFailed(e)
            } finally {
                opcUaClient = null
                Stack.releaseSharedResources()
            }
        } ?: run {
            callback.disconnectionFailed(IllegalStateException("No connected OpcUaClient..."))
        }
    }

    @WorkerThread
    fun executeMiloClient(miloClient: MiloClient, callback: OpcUaCallback.MiloClientExecute) {
        opcUaClient?.let {
            miloClient.execute(it)
        } ?: run {
            callback.executeFailed(IllegalStateException("No connected OpcUaClient..."))
        }
    }

    private fun getEndpointDescription(): EndpointDescription = try {
        DiscoveryClient.getEndpoints(endpointUrl).get()
    } catch (ex: Throwable) { // try the explicit discovery endpoint as well
        var discoveryUrl: String =
            endpointUrl
        if (!discoveryUrl.endsWith("/")) {
            discoveryUrl += "/"
        }
        discoveryUrl += "discovery"
        DiscoveryClient.getEndpoints(discoveryUrl).get()
    }.stream()
        .filter { e: EndpointDescription ->
            e.securityPolicyUri == securityPolicy.uri
        }
        .filter(endpointFilter)
        .findFirst()
        .orElseThrow {
            Exception("no desired endpoints returned")
        }

    companion object {
        init {
            // Required for SecurityPolicy.Aes256_Sha256_RsaPss
            Security.addProvider(BouncyCastleProvider())
        }

        const val endpointUrl: String = "opc.tcp://192.168.0.103:4840"
//        const val endpointUrl: String = "https://192.168.0.115:8443/milo"
        val securityPolicy: SecurityPolicy = SecurityPolicy.None
        val endpointFilter: Predicate<EndpointDescription> = Predicate {
            true
        }

        @Volatile
        private var instance: MiloClientRunner? = null

        @JvmStatic
        fun getInstance(): MiloClientRunner =
            instance ?: synchronized(this) {
                instance ?: MiloClientRunner().also {
                    instance = it
                }
            }
    }
}