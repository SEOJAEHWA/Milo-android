package kr.co.aiblab.test.milo.milo

import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import java.util.function.Predicate

interface MiloClient {

    @Throws(Exception::class)
    suspend fun execute(
        client: OpcUaClient
    )

    companion object {
        const val endpointUrl: String = "https://192.168.0.115:8443/milo"
        val securityPolicy: SecurityPolicy = SecurityPolicy.None
        val endpointFilter: Predicate<EndpointDescription> = Predicate {
            true
        }
        val identityProvider: IdentityProvider = AnonymousProvider()
    }
}