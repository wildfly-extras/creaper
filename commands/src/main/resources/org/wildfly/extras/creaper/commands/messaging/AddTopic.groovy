// closure with XML structure definition
def tDefinitionHornetQ = {
    'jms-topic'(name: name) {
        if (entries != null) {
            entries.each {
                'entry'(name: it)
            }
        }
    }
}

tAttrs = [name: name]
if (nn(entriesString)) tAttrs['entries'] = entriesString

def tDefinitionActiveMQ = {
    'jms-topic'(tAttrs) {}
}

def topicAddress = null
if (messagingActivemq) {
    topicAddress = messagingActivemq.server.'jms-topic'
} else if ( messagingHornetq) {
    topicAddress = messagingHornetq.'hornetq-server'.'jms-destinations'.'jms-topic'
}
if (!topicAddress) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

// adding or replacing existing topic
def isExistingTopic = topicAddress.find { it.@name == name }
if (isExistingTopic && !replaceExisting) {
    throw new IllegalStateException("Topic $name already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (isExistingTopic) {
     if (messagingHornetq) {
        isExistingTopic.replaceNode tDefinitionHornetQ
    } else if (messagingActivemq) {
        isExistingTopic.replaceNode tDefinitionActiveMQ
    }
} else {
    if (messagingHornetq) {
        messagingHornetq.'hornetq-server'.'jms-destinations'.appendNode tDefinitionHornetQ
    } else if (messagingActivemq) {
        messagingActivemq.server.appendNode tDefinitionActiveMQ
    }
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
