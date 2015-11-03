def address = null

def defHornet = { 'cluster-password' (password) }
def defArtemis = { 'cluster' ('password': password) }

if (messagingActivemq) {
    address = messagingActivemq.server
    if ( address.cluster.any { it.name() == 'cluster' } ) {
        address.cluster.@password = password
    } else {
        address.appendNode defArtemis
    }
} else if (messagingHornetq) {
    address = messagingHornetq.'hornetq-server'
    if ( address.'cluster-password'.any { it.name() == 'cluster-password' } ) {
        address.'cluster-password'.replaceNode defHornet
    } else {
        address.appendNode defHornet
    }
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}
