import org.wildfly.extras.creaper.core.ServerVersion

// attributes names by version
statisticsEnabledAttrName = serverVersion.lessThan(ServerVersion.VERSION_2_0_0) ? 'enable-statistics' : 'statistics-enabled'
useJournalStoreAttrName = serverVersion.lessThan(ServerVersion.VERSION_4_0_0) ? 'use-hornetq-store' : 'use-journal-store'

if (nn(nodeIdentifier)) {
    transactions.'core-environment'.'@node-identifier' = nodeIdentifier as String
}

if (nn(enableTsmStatus, timeout, statisticsEnabled)) {

    coordinator = [:]
    if (nn(enableTsmStatus)) coordinator['enable-tsm-status'] = enableTsmStatus as String
    if (nn(timeout)) coordinator['default-timeout'] = timeout as String
    if (nn(statisticsEnabled)) coordinator[statisticsEnabledAttrName] = statisticsEnabled as String

    if (transactions.'coordinator-environment'.size() == 0) {
        transactions.appendNode {
            'coordinator-environment'(coordinator)
        }
    }
    if (nn(enableTsmStatus)) {
        transactions.'coordinator-environment'.'@enable-tsm-status' = enableTsmStatus as String
    }
    if (nn(timeout)) {
        transactions.'coordinator-environment'.'@default-timeout' = timeout as String
    }
    if (nn(statisticsEnabled)) {
        transactions.'coordinator-environment'."@${statisticsEnabledAttrName}" = statisticsEnabled as String
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
    transactions.'recovery-environment'.'@socket-binding' = socketBinding as String
}

if (nn(statusSocketBinding)) {
    transactions.'recovery-environment'.'@status-socket-binding' = statusSocketBinding as String
}

if (nn(recoveryListener)) {
    transactions.'recovery-environment'.'@recovery-listener' = recoveryListener as String
}

if (nn(objectStorePath)) {
    objectStore = ['path': objectStorePath]
    if (nn(objectStoreRelativeTo)) objectStore['relative-to'] = objectStoreRelativeTo as String

    if (transactions.'object-store'.size() == 0) {
        transactions.appendNode {
            'object-store'(objectStore)
        }
    } else {
        transactions.'object-store'.'@path' = objectStorePath
        if (nn(objectStoreRelativeTo)) transactions.'object-store'.'@relative-to' = objectStoreRelativeTo as String
    }
}

if (nn(jts)) {
    if (jts && transactions.jts.size() == 0) {
        transactions.appendNode { 'jts'() }
    }
    if (!jts && transactions.jts.size() > 0) {
        transactions.jts.replaceNode {}
    }
}

if (nn(useJournalStore)) {
    journal = [:]
    if (nn(journalStoreEnableAsyncIO)) journal['enable-async-io'] = journalStoreEnableAsyncIO as String

    if (!useJournalStore && transactions[useJournalStoreAttrName].size() > 0) {
        transactions[useJournalStoreAttrName].replaceNode {}
    }
    if (useJournalStore) {
        // jdbc-store is not supposed to be used along with use-journal-store
        if (transactions.'jdbc-store'.size() != 0) {
            transactions.'jdbc-store'.replaceNode {}
        }
        if (transactions[useJournalStoreAttrName].size() == 0) {
            transactions.appendNode {
                "${useJournalStoreAttrName}"(journal)
            }
        } else if (nn(journalStoreEnableAsyncIO)) {
            transactions[useJournalStoreAttrName].'@enable-async-io' = journalStoreEnableAsyncIO as String
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
