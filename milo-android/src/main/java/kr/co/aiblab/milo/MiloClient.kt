package kr.co.aiblab.milo

import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import java.util.function.Predicate

interface MiloClient {

    @Throws(Exception::class)
    fun execute(
        client: OpcUaClient
    )
}