def handlerExists = management.'audit-log'.'handlers'.'syslog-handler'.find { it.'@name' == atrName }

handlerAttrs = ['name': atrName, 'formatter': atrFormatter]
protocolAttrs = [:]
authAttrs = [:]

if (nn(atrAppName)) {
    handlerAttrs ['app-name'] = atrAppName
}
if (nn(atrFacility)) {
    handlerAttrs['facility'] = atrFacility
}
if (nn(atrMaxFailureCount)) {
    handlerAttrs['max-failure-count'] = atrMaxFailureCount
}
if (nn(atrMaxLength)) {
    handlerAttrs['max-length'] = atrMaxLength
}
if (nn(atrSyslogFormat)) {
    handlerAttrs['syslog-format'] = atrSyslogFormat
}
if (nn(atrTruncate)) {
    handlerAttrs['truncate'] = atrTruncate
}
if (nn(atrHost)) {
    protocolAttrs['host'] = atrHost
}
if (nn(atrMessageTransfer)) {
    protocolAttrs['message-transfer'] = atrMessageTransfer
}
if (nn(atrPort)) {
    protocolAttrs['port'] = atrPort
}
if (nn(atrReconnectTimeout)) {
    protocolAttrs['reconnect-timeout'] = atrReconnectTimeout
}

if (nn(atrKeyPassword)) {
    authAttrs['key-password'] = atrKeyPassword
}
if (nn(atrKeystorePassword)) {
    authAttrs['keystore-password'] = atrKeystorePassword
}
if (nn(atrKeystorePath)) {
    authAttrs['keystore-path'] = atrKeystorePath
}
if (nn(atrKeystoreRelativeTo)) {
    authAttrs['keystore-relative-to'] = atrKeystoreRelativeTo
}

def newHandlerDef

if (nn(atrTransportProtocol)) {
    if (atrTransportProtocol == 'udp') {
        newHandlerDef = {
            'syslog-handler'(handlerAttrs) {
                'udp'(protocolAttrs)
            }
        }
    } else if (atrTransportProtocol == 'tcp') {
        newHandlerDef = {
            'syslog-handler'(handlerAttrs) {
                'tcp'(protocolAttrs)
            }
        }
    } else {
        if (atrAuthenticationType == 'truststore') {
            newHandlerDef = {
                'syslog-handler'(handlerAttrs) {
                    'tls'(protocolAttrs) {
                        'truststore'(authAttrs)
                    }
                }
            }
        } else {
            newHandlerDef = {
                'syslog-handler'(handlerAttrs) {
                    'tls'(protocolAttrs) {
                        'client-certificate-store'(authAttrs)
                    }
                }
            }
        }
    }
} else {
    throw new IllegalStateException("Transport protocol is not known")
}

if (!handlerExists) {
    management.'audit-log'.'handlers'.appendNode newHandlerDef
} else if (atrReplaceExisting) {
    handlerExists.replaceNode newHandlerDef
} else {
    throw new IllegalStateException("Syslog handler $atrName already exists. If you want to replace existing handler, please set replaceExisting.")
}

/**
 * Checking if parameter is not null.
 * We can't use if(object) ... as object could be null or false
 * and we need to differentiate such states
 */
def nn(Object... object) {
    if (object == null) return false
    return object.any { it != null }
}
