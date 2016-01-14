if (nn(nodeIdentifier)) {
    transactions.'core-environment'.'@node-identifier' = nodeIdentifier
}

if (nn(enableTsmStatus, timeout, statisticsEnabled)) {

    coordinator = [:]
    if (nn(enableTsmStatus)) coordinator['enable-tsm-status'] = enableTsmStatus
    if (nn(timeout)) coordinator['default-timeout'] = timeout
    if (nn(statisticsEnabled)) coordinator['statistics-enabled'] = statisticsEnabled

    if (transactions.'coordinator-environment'.size() == 0) {
        transactions.appendNode {
            'coordinator-environment'(coordinator)
        }
    }
    if (nn(enableTsmStatus)) {
        transactions.'coordinator-environment'.'@enable-tsm-status' = enableTsmStatus
    }
    if (nn(timeout)) {
        transactions.'coordinator-environment'.'@default-timeout' = timeout
    }
    if (nn(statisticsEnabled)) {
        transactions.'coordinator-environment'.'@statistics-enabled' = statisticsEnabled
    }
}

if (nn(processIdUuid)) {
    transactions.'core-environment'.'process-id'.replaceNode {
        'process-id' {
            'uuid'()
        }
    }
}

if (nn(processIdSocketBinding)) {
    transactions.'core-environment'.'process-id'.replaceNode {
        'process-id' {
            'socket'('socket-binding': processIdSocketBinding, 'socket-process-id-max-ports': processIdSocketMaxPorts)
        }
    }
}

if (nn(socketBinding)) {
    transactions.'recovery-environment'.'@socket-binding' = socketBinding
}

if (nn(statusSocketBinding)) {
    transactions.'recovery-environment'.'@status-socket-binding' = statusSocketBinding
}

if (nn(recoveryListener)) {
    transactions.'recovery-environment'.'@recovery-listener' = recoveryListener
}

if (nn(objectStorePath)) {
    objectStore = ['path': objectStorePath]
    if (nn(objectStoreRelativeTo)) objectStore['relative-to'] = objectStoreRelativeTo

    if (transactions.'object-store'.size() == 0) {
        transactions.appendNode {
            'object-store'(objectStore)
        }
    } else {
        transactions.'object-store'.'@path' = objectStorePath
        if (nn(objectStoreRelativeTo)) transactions.'object-store'.'@relative-to' = objectStoreRelativeTo
    }
}

if (nn(jts)) {
    if (jts == "true" && transactions.jts.size() == 0) {
        transactions.appendNode { 'jts'() }
    }
    if (jts == "false" && transactions.jts.size() > 0) {
        transactions.jts.replaceNode {}
    }
}

if (nn(useJournalStore)) {

    journal = [:]
    if (nn(journalStoreEnableAsyncIO)) journal['enable-async-io'] = journalStoreEnableAsyncIO

    if (useJournalStore == "false" && transactions.'use-journal-store'.size() > 0) {
        transactions.'use-journal-store'.replaceNode {}
    }
    if (useJournalStore == "true") {
        if (transactions.'use-journal-store'.size() == 0) {
            transactions.appendNode {
                'use-journal-store'(journal)
            }
        } else if (nn(journalStoreEnableAsyncIO)) {
            transactions.'use-journal-store'.'@enable-async-io' = journalStoreEnableAsyncIO
        }

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
