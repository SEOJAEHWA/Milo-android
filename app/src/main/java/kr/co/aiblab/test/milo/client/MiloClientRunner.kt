package kr.co.aiblab.test.milo.client

import android.content.Context
import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig
import org.eclipse.milo.opcua.stack.client.DiscoveryClient
import org.eclipse.milo.opcua.stack.core.Stack
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import java.io.File
import java.nio.file.Paths
import java.security.Security
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


class MiloClientRunner(private val client: MiloClient) {

    private val future: CompletableFuture<OpcUaClient> = CompletableFuture()

    @Throws(Exception::class)
    private fun createClient(context: Context): OpcUaClient {

        val securityTempDir =
            Paths.get(getInternalStorageDirectoryPath(context, "secure").absolutePath)
        Log.d("MILO", "security temp dir: ${securityTempDir.toAbsolutePath()}")

        val loader = KeyStoreLoader().load(securityTempDir)

        val securityPolicy: SecurityPolicy = client.getSecurityPolicy()

        val endpoints: List<EndpointDescription> = try {
            DiscoveryClient.getEndpoints(client.getEndpointUrl()).get()
        } catch (ex: Throwable) { // try the explicit discovery endpoint as well
            Log.e("MILO", ex.message, ex)
            var discoveryUrl: String = client.getEndpointUrl()
            if (!discoveryUrl.endsWith("/")) {
                discoveryUrl += "/"
            }
            discoveryUrl += "discovery"
            Log.d("MILO", "Trying explicit discovery URL: $discoveryUrl")
            DiscoveryClient.getEndpoints(discoveryUrl).get()
        }

        val endpoint = endpoints.stream()
            .filter { e: EndpointDescription -> e.securityPolicyUri == securityPolicy.uri }
            .filter(client.endpointFilter())
            .findFirst()
            .orElseThrow {
                java.lang.Exception(
                    "no desired endpoints returned"
                )
            }!!

        Log.d(
            "MILO",
            "Using endpoint: ${endpoint.endpointUrl} [$securityPolicy/${endpoint.securityMode}]"
        )

        val config = with(OpcUaClientConfig.builder()) {
            setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
            setApplicationUri("urn:eclipse:milo:examples:client")
            setCertificate(loader.clientCertificate)
            setKeyPair(loader.clientKeyPair)
            setEndpoint(endpoint)
            build()
        }
        return OpcUaClient.create(config)
    }

    fun run(context: Context) {
        try {
            val client = createClient(context)
            future.whenCompleteAsync { c: OpcUaClient?, ex: Throwable? ->
                if (ex != null) {
                    Log.e("MILO", "Error running example: ${ex.message}", ex)
                }
                try {
                    client.disconnect().get()
                    Stack.releaseSharedResources()
                } catch (e: InterruptedException) {
                    Log.e("MILO", "Error disconnecting:${e.message}", e)
                } catch (e: ExecutionException) {
                    Log.e("MILO", "Error disconnecting:${e.message}", e)
                }
//                try {
//                    Thread.sleep(1000)
//                    System.exit(0)
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
            }
            try {
                this.client.run(client, future)
                future[15, TimeUnit.SECONDS]
            } catch (t: Throwable) {
                Log.e("MILO", "Error running client example: ${t.message}", t)
                future.completeExceptionally(t)
            }
        } catch (t: Throwable) {
            Log.e("MILO", "Error getting client: ${t.message}", t)
            future.completeExceptionally(t)
//            try {
//                Thread.sleep(1000)
//                System.exit(0)
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
        }
//        try {
//            Thread.sleep(999999999)
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
    }

    companion object {
        init {
            // Required for SecurityPolicy.Aes256_Sha256_RsaPss
            Security.addProvider(BouncyCastleProvider())
        }

//        fun getSecureDirectory(context: Context, dirName: String): File =
//            File(getInternalStorageDirectoryPath(context, "secure"), dirName)

        private fun getInternalStorageDirectoryPath(context: Context, directoryName: String): File {
            val directory =
                File("${context.filesDir}${File.separator}${directoryName}${File.separator}")
            if (!directory.exists()) {
                directory.mkdir()
            }
            return directory
        }
    }
}