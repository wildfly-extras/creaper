def sslAttrs = [:]
if (caCertificateFile != null) sslAttrs['ca-certificate-file'] = caCertificateFile
if (caCertificatePassword != null) sslAttrs['ca-certificate-password'] = caCertificatePassword
if (caRevocationUrl != null) sslAttrs['ca-revocation-url'] = caRevocationUrl
if (certificateFile != null) sslAttrs['certificate-file'] = certificateFile
if (certificateKeyFile != null) sslAttrs['certificate-key-file'] = certificateKeyFile
if (cipherSuite != null) sslAttrs['cipher-suite'] = cipherSuite
if (keyAlias != null) sslAttrs['key-alias'] = keyAlias
if (keystoreType != null) sslAttrs['keystore-type'] = keystoreType
if (password != null) sslAttrs['password'] = password
if (protocol != null) sslAttrs['protocol'] = protocol
if (sessionCacheSize != null) sslAttrs['session-cache-size'] = sessionCacheSize
if (sessionTimeout != null) sslAttrs['session-timeout'] = sessionTimeout
if (sslProtocol != null) sslAttrs['ssl-protocol'] = sslProtocol
if (truststoreType != null) sslAttrs['truststore-type'] = truststoreType
if (verifyClient != null) sslAttrs['verify-client'] = verifyClient
if (verifyDepth != null) sslAttrs['verify-depth'] = verifyDepth

def sslDefinition = {
    ssl(sslAttrs) {
    }
}

def existingConnector = web.connector.find { it.'@name' == connectorName }
if (!existingConnector) {
    throw new IllegalStateException("Connector $connectorName doesn't exist => can't define its ssl configuration")
} else {
    if (existingConnector.children().isEmpty()) {
        existingConnector.appendNode sslDefinition
    } else {
        existingConnector.ssl.replaceNode sslDefinition
    }
}
