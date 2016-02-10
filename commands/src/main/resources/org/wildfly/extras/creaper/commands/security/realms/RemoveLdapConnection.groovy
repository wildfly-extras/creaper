def outboundConnectionsElement = management.'outbound-connections'
def outboundConnectionsExists = outboundConnectionsElement.any { it.name() == 'outbound-connections' }

if (!outboundConnectionsExists) {
    throw new IllegalStateException("Ldap outbound connection was not found => can't remove it")
}

def ldapConnectionToRemove = outboundConnectionsElement.'ldap'.find { it.@name == atrLdapConnectionName }

if (ldapConnectionToRemove) {
    if ( outboundConnectionsElement.'ldap'.size() > 1 ) {
        ldapConnectionToRemove.replaceNode {}
    } else {
        outboundConnectionsElement.find().replaceNode {}
    }
} else {
    throw new IllegalStateException("Ldap outbound connection ${atrSecurityRealmName} not found => can't remove it")
}
