connectorAttrs = ['name': connectorName]
if (enableLookups != null) connectorAttrs['enable-lookups'] = enableLookups
if (enabled != null) connectorAttrs['enabled'] = enabled
if (executor != null) connectorAttrs['executor'] = executor
if (maxConnections != null) connectorAttrs['max-connections'] = maxConnections
if (maxPostSize != null) connectorAttrs['max-post-size'] = maxPostSize
if (maxSavePostSize != null) connectorAttrs['max-save-post-size'] = maxSavePostSize
if (protocol != null) connectorAttrs['protocol'] = protocol
if (proxyBinding != null) connectorAttrs['proxy-binding'] = proxyBinding
if (proxyName != null) connectorAttrs['proxy-name'] = proxyName
if (proxyPort != null) connectorAttrs['proxy-port'] = proxyPort
if (redirectBinding != null) connectorAttrs['redirect-binding'] = redirectBinding
if (redirectPort != null) connectorAttrs['redirect-port'] = redirectPort
if (secure != null) connectorAttrs['secure'] = secure
if (scheme != null) connectorAttrs['scheme'] = scheme
if (socketBinding != null) connectorAttrs['socket-binding'] = socketBinding

def connectorDefinition = {
    connector(connectorAttrs) {
        if (virtualServers != null) {
            virtualServers.each {
                'virtual-server'(name: it)
            }
        }
    }
}

def existingConnector = web.connector.find { it.'@name' == connectorName }
if (existingConnector && !replaceExisting) {
    throw new IllegalStateException("Connector $connectorName already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (existingConnector) {
    existingConnector.replaceNode connectorDefinition
} else {
    web.appendNode connectorDefinition
}
