def secRealmToRemove = management.'security-realms'.'security-realm'.find { it.@name == atrSecurityRealmName }

if (secRealmToRemove) {
    secRealmToRemove.replaceNode {}
} else {
    throw new IllegalStateException("Security realm ${atrSecurityRealmName} not found => can't remove it")
}
