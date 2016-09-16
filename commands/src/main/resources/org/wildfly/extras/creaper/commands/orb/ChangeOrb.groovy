import org.wildfly.extras.creaper.commands.orb.Attribute
import groovy.util.slurpersupport.GPathResult

// --- HELPER METHODS
java.util.LinkedHashMap.metaClass.fill = { GPathResult initPath, String attrName, Attribute attrToSet ->
    if (initPath."@${attrName}".size() > 0) delegate.put(attrName, initPath."@${attrName}".text())
    if (attrToSet.hasValue()) delegate.put(attrName, attrToSet.get())
    if (attrToSet.isUndefine()) delegate.remove(attrName)
    return delegate
}

// --- EXECUTION
if (iiop.size() <= 0) {
    throw new IllegalStateException("Configuration file does not support ${subsystem} subsystem")
}

// Element paths definitions
def propertiesPath = iiop.properties
def propertyPath = propertiesPath.property
def orbPath = iiop.orb
def initializersPath = isOpenjdkIiop ? iiop.initializers : orbPath.initializers
def namingPath = iiop.naming
def securityPath = iiop.security
def iorPath = iiop.'ior-settings'
def transportConfigPath = isOpenjdkIiop ? iiop.'transport-config' : iorPath.'transport-config'
def asContextPath = isOpenjdkIiop ? iiop.'as-context' : iorPath.'as-context'
def sasContextPath = isOpenjdkIiop ? iiop.'sas-context' : iorPath.'sas-context'

// Gathering values
def propertiesClone = properties.clone()
properties = [:]
propertyPath.iterator().each { properties.put(it.@name.text(), it.@value.text()) }
propertiesClone.each {
    if (it.value.isUndefine()) {
        properties.remove(it.key)
    } else if (it.value.hasValue()) {
        properties.put(it.key, it.value.get())
    }
}

def orbAttr = [:]
    .fill(orbPath, (isOpenjdkIiop ? 'giop-version' : 'giop-minor-version'), giopVersion)
    .fill(orbPath, 'socket-binding', socketBinding)
    .fill(orbPath, 'ssl-socket-binding', sslSocketBinding)
    .fill(orbPath, 'persistent-server-id', persistentServerId)

def initializersAttr = [:]
    .fill(initializersPath, 'security', security)
    .fill(initializersPath, 'transactions', transactions)

def namingAttr = [:]
    .fill(namingPath, 'root-context', rootContext)
    .fill(namingPath, 'export-corbaloc', exportCorbaloc)

def securityAttr = [:]
    .fill(securityPath, 'security-domain', securityDomain)
    .fill(securityPath, 'add-component-via-interceptor', addComponentViaInterceptor)
    .fill(securityPath, 'client-supports', clientSupports)
    .fill(securityPath, 'client-requires', clientRequires)
    .fill(securityPath, 'server-supports', serverSupports)
    .fill(securityPath, 'server-requires', serverRequires)
    .fill(securityPath, 'support-ssl', supportSsl)

def transportConfigAttr = [:]
    .fill(transportConfigPath, 'integrity', integrity)
    .fill(transportConfigPath, 'confidentiality', confidentiality)
    .fill(transportConfigPath, 'trust-in-target',  trustInTarget)
    .fill(transportConfigPath, 'trust-in-client', trustInClient)
    .fill(transportConfigPath, 'detect-replay', detectReplay)
    .fill(transportConfigPath, 'detect-misordering', detectMisordering)

def asContextAttr = [:]
    .fill(asContextPath, 'auth-method', authMethod)
    .fill(asContextPath, 'realm', realm)
    .fill(asContextPath, 'required', required)

def sasContextAttr = [:]
        .fill(sasContextPath, 'caller-propagation', callerPropagation)

iiop.replaceBody {
    if (isOpenjdkIiop) {
        // <orb ... /><initializers ... />
        if (!orbAttr.isEmpty()) orb (orbAttr)
        if (!initializersAttr.isEmpty()) initializers (initializersAttr)
    } else {
        // <orb ... ><initializers ... /></orb>
        if (!orbAttr.isEmpty() || !initializersAttr.isEmpty()) orb (orbAttr) { initializers (initializersAttr) }
    }

    // <naming ... />
    if (!namingAttr.isEmpty()) naming (namingAttr)

    // <security ... />
    if (!securityAttr.isEmpty()) security (securityAttr)

    def composite = {
        // <transport-config ... />
        if (!transportConfigAttr.isEmpty()) 'transport-config' (transportConfigAttr)

        // <as-context ... />
        if (!asContextAttr.isEmpty()) 'as-context' (asContextAttr)

        // <sas-context ... />
        if (!sasContextAttr.isEmpty()) 'sas-context' (sasContextAttr)
    }

    if (isOpenjdkIiop) {
        composite()
    } else {
        if (!transportConfigAttr.isEmpty() || !asContextAttr.isEmpty() || !sasContextAttr.isEmpty())
            'ior-settings' composite
    }

    if (!properties.isEmpty()) {
        'properties' {
            properties.each {
                'property' (name: it.key, value: it.value)
            }
        }
    }
}
