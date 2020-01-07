package kr.co.aiblab.test.milo.client

import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

interface MiloClient {

    fun getEndpointUrl(): String {
        return "https://192.168.0.115:8443/milo"
//        return "opc.tcp://localhost:12686/milo"
    }

    fun endpointFilter(): Predicate<EndpointDescription> {
        return Predicate {
            true
        }
    }

    fun getSecurityPolicy(): SecurityPolicy {
        return SecurityPolicy.None
    }

    fun getIdentityProvider(): IdentityProvider {
        return AnonymousProvider()
    }

    @Throws(Exception::class)
    fun run(
        client: OpcUaClient,
        future: CompletableFuture<OpcUaClient>
    )
}