def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == realmName }
if (!isSecurityRealmExist) {
  throw new IllegalStateException("Security realm with name $realmName does not exist.")
}

def truststoreAttrs = [:]
if (truststorePassword != null) truststoreAttrs['keystore-password'] = truststorePassword
if (truststorePath != null) truststoreAttrs['path'] = truststorePath
if (truststoreProvider != null) truststoreAttrs['provider'] = truststoreProvider
if (truststoreRelativeTo != null) truststoreAttrs['relative-to'] = truststoreRelativeTo

def truststoreDefinition = {
    'truststore'(truststoreAttrs)
}

def usedSecurityRealm = securityRealm.find { it.'@name' == realmName }
def authenticationElement = usedSecurityRealm.authentication
def isExistingAuthentication = authenticationElement.any { it.name() == 'authentication' }

if (isExistingAuthentication) {
  def isTruststoreExists = authenticationElement.truststore.any { it.name() == 'truststore' }
  if (isTruststoreExists && !replaceExisting) {
    throw new IllegalStateException("Truststore authnetication already exists in security realm with name $realmName.")
  } else {
    if (isTruststoreExists) {
      authenticationElement.truststore.find().replaceNode truststoreDefinition
    } else {
      authenticationElement.appendNode truststoreDefinition
    }
  }
} else {
  usedSecurityRealm.appendNode {
    'authentication'(truststoreDefinition)
  }
}
