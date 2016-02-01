def secDomainToRemove = securitySubsystem.'security-domains'.'security-domain'.find { it.@name == atrSecurityDomainName }

if (secDomainToRemove) {
    secDomainToRemove.replaceNode {}
} else {
    throw new IllegalStateException("Security domain ${atrSecurityDomainName} not found => can't remove it")
}
