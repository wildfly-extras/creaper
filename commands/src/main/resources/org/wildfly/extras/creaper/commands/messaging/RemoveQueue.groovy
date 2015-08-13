def queueAddress = null

if (messagingActivemq) {
    queueAddress = messagingActivemq.server.'jms-queue'
} else if ( messagingHornetq) {
    queueAddress = messagingHornetq.'hornetq-server'.'jms-destinations'.'jms-queue'
}
if (!queueAddress) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

def queue = queueAddress.find { it.@name == queueName }
if (!queue) {
    throw new IllegalStateException("Can't remove queue $queueName as it does not exist in the configuration")
}

queue.replaceNode {}
