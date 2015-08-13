// closure with XML structure definition
def qDefinitionHornetQ = {
    'jms-queue'(name: name) {
        if (entries != null) {
            entries.each {
                'entry'(name: it)
            }
        }
        if (selector != null) 'selector'(selector)
        if (durable != null) 'durable'(durable)
    }
}
qAttrs = [name: name]
if (nn(entriesString)) qAttrs['entries'] = entriesString
if (nn(durable)) qAttrs['durable'] = durable
if (nn(selector)) qAttrs['selector'] = selector

def qDefinitionActiveMQ = {
    'jms-queue'(qAttrs) {}
}

def queueAddress = null
if (messagingActivemq) {
    queueAddress = messagingActivemq.server.'jms-queue'
} else if (messagingHornetq) {
    queueAddress = messagingHornetq.'hornetq-server'.'jms-destinations'.'jms-queue'
}
if (!queueAddress) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

// adding or replacing existing queue
def isExistingQueue = queueAddress.find { it.@name == name }
if (isExistingQueue && !replaceExisting) {
    throw new IllegalStateException("Queue $name already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (isExistingQueue) {
    if (messagingHornetq) {
        isExistingQueue.replaceNode qDefinitionHornetQ
    } else if (messagingActivemq) {
        isExistingQueue.replaceNode qDefinitionActiveMQ
    }
} else {
    if (messagingHornetq) {
        messagingHornetq.'hornetq-server'.'jms-destinations'.appendNode qDefinitionHornetQ
    } else if (messagingActivemq) {
        messagingActivemq.server.appendNode qDefinitionActiveMQ
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
