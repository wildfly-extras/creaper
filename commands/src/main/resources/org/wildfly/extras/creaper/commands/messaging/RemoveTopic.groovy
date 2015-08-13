def topicAddress = null

if (messagingActivemq) {
    topicAddress = messagingActivemq.server.'jms-topic'
} else if ( messagingHornetq) {
    topicAddress = messagingHornetq.'hornetq-server'.'jms-destinations'.'jms-topic'
}
if (!topicAddress) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

def topic = topicAddress.find { it.@name == topicName }
if (!topic) {
    throw new IllegalStateException("Can't remove topic $topicName as it does not exist in the configuration")
}

topic.replaceNode {}
