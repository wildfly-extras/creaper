sslContextAttrs = ['name': atrName]
if (atrCipherSuiteFilter != null) sslContextAttrs['cipher-suite-filter'] = atrCipherSuiteFilter
if (atrMaximumSessionCacheSize != null) sslContextAttrs['maximum-session-cache-size'] = atrMaximumSessionCacheSize
if (atrSessionTimeout != null) sslContextAttrs['session-timeout'] = atrSessionTimeout
if (atrKeyManager != null) sslContextAttrs['key-manager'] = atrKeyManager
if (atrTrustManager != null) sslContextAttrs['trust-manager'] = atrTrustManager
if (atrProtocols != null) sslContextAttrs['protocols'] = atrProtocols
if (atrAuthenticationOptional != null) sslContextAttrs['authentication-optional'] = atrAuthenticationOptional
if (atrNeedClientAuth != null) sslContextAttrs['need-client-auth'] = atrNeedClientAuth
if (atrWantClientAuth != null) sslContextAttrs['want-client-auth'] = atrWantClientAuth
if (atrSecurityDomain != null) sslContextAttrs['security-domain'] = atrSecurityDomain
if (atrRealmMapper != null) sslContextAttrs['realm-mapper'] = atrRealmMapper
if (atrPreRealmPrincipalTransformer != null) sslContextAttrs['pre-realm-principal-transformer'] = atrPreRealmPrincipalTransformer
if (atrPostRealmPrincipalTransformer != null) sslContextAttrs['post-realm-principal-transformer'] = atrPostRealmPrincipalTransformer
if (atrFinalPrincipalTransformer != null) sslContextAttrs['final-principal-transformer'] = atrFinalPrincipalTransformer
if (atrUseCipherSuitesOrder != null) sslContextAttrs['use-cipher-suites-order'] = atrUseCipherSuitesOrder
if (atrWrap != null) sslContextAttrs['wrap'] = atrWrap
if (atrProviders != null) sslContextAttrs['providers'] = atrProviders
if (atrProviderName != null) sslContextAttrs['provider-name'] = atrProviderName

def sslContextDefinition = {
    'server-ssl-context'(sslContextAttrs)
}

def isExistingTls = elytronSubsystem.'tls'.any { it.name() == 'tls' }
if (! isExistingTls) {
    elytronSubsystem.appendNode { 'tls' { 'server-ssl-contexts' sslContextDefinition } }
    return
}

def isExistingServerSslContexts = elytronSubsystem.'tls'.'server-ssl-contexts'.any { it.name() == 'server-ssl-contexts' }
if (! isExistingServerSslContexts) {
    elytronSubsystem.'tls'.appendNode { 'server-ssl-contexts' sslContextDefinition }
    return
}

def existingServerSslContext = elytronSubsystem.'tls'.'server-ssl-contexts'.'server-ssl-context'.find { it.'@name' == atrName }
if (existingServerSslContext && !atrReplaceExisting) {
    throw new IllegalStateException("Server SSL context with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingServerSslContext) {
        existingServerSslContext.replaceNode sslContextDefinition
    } else {
        elytronSubsystem.'tls'.'server-ssl-contexts'.appendNode sslContextDefinition
    }
}
