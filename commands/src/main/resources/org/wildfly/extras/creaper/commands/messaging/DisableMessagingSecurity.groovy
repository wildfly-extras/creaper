def address = null

def defHornet = { 'security-enabled' ('false') }
def defArtemis = { 'security' (enabled: 'false') }

if (messagingActivemq) {
    address = messagingActivemq.server
    if ( address.security.any { it.name() == 'security' } ) {
        address.security.@enabled = 'false'
    } else {
        address.appendNode defArtemis
    }
} else if (messagingHornetq) {
    address = messagingHornetq.'hornetq-server'
    if ( address.'security-enabled'.any { it.name() == 'security-enabled' } ) {
        address.'security-enabled'.replaceNode defHornet
    } else {
        address.appendNode defHornet
    }
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}
