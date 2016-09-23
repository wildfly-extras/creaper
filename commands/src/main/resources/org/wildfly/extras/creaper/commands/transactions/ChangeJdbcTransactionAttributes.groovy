def changeAction = nn(actionStoreDropTable, actionStoreTablePrefix)
def changeCommunication = nn(communicationStoreDropTable, communicationStoreTablePrefix)
def changeState = nn(stateStoreDropTable, stateStoreTablePrefix)

actionAttribs = [:]
if (nn(actionStoreDropTable)) {
    actionAttribs['drop-table'] = actionStoreDropTable
} else {
    actionAttribs['drop-table'] = "false"
}
if (nn(actionStoreTablePrefix)) {
    actionAttribs['table-prefix'] = actionStoreTablePrefix
}

communicationAttribs = [:]
if (nn(communicationStoreDropTable)) {
    communicationAttribs['drop-table'] = communicationStoreDropTable
} else {
    communicationAttribs['drop-table'] = "false"
}
if (nn(communicationStoreTablePrefix)) {
    communicationAttribs['table-prefix'] = communicationStoreTablePrefix
}

stateAttribs = [:]
if (nn(stateStoreDropTable)) {
    stateAttribs['drop-table'] = stateStoreDropTable
} else {
    stateAttribs['drop-table'] = "false"
}
if (nn(stateStoreTablePrefix)) {
    stateAttribs['table-prefix'] = stateStoreTablePrefix
}

jdbcAttribs = [:]
if (nn(storeDatasource)) {
    jdbcAttribs['datasource-jndi-name'] = storeDatasource
}

def newJdbcStore = {
    'jdbc-store'(jdbcAttribs) {
        if (changeAction) 'action'(actionAttribs)
        if (changeCommunication) 'communication'(communicationAttribs)
        if (changeState) 'state'(stateAttribs)
    }
}

if (transactions.'jdbc-store'.size() == 0) {
    if (useJdbcStore == "true") {
        // jdbc-store node does not exists and should be created (new-one)
        // use-journal-store/use-hornetq-store is not supposed to be used along with jdbc-store
        if (transactions.'use-journal-store'.size() != 0) {
            transactions.'use-journal-store'.replaceNode {}
        }
        if (transactions.'use-hornetq-store'.size() != 0) {
            transactions.'use-hornetq-store'.replaceNode {}
        }
        transactions.appendNode newJdbcStore
    }
} else {
    if (useJdbcStore == "false") {
        // jdbc-store node exists and should be deleted
        transactions.'jdbc-store'.replaceNode {}
    } else {
        // jdbc-store node exists and using jdbc store is set to true or null (not-changing)
        // use-journal-store is not supposed to be used along with jdbc-store
        if (transactions.'use-journal-store'.size() != 0) {
            transactions.'use-journal-store'.replaceNode {}
        }
        if (nn(storeDatasource)) {
            transactions.'jdbc-store'.'@datasource-jndi-name' = storeDatasource
        }
        // communication node (something need to be changed)
        if (changeAction) {
            // new node
            if (transactions.'jdbc-store'.action.size() == 0) {
                transactions.'jdbc-store'.appendNode {
                    'action'(actionAttribs)
                }
            } else {
                if (nn(actionStoreDropTable)) {
                    transactions.'jdbc-store'.action.'@drop-table' = actionStoreDropTable
                }
                if (nn(actionStoreTablePrefix)) {
                    if (actionStoreTablePrefix == "") {
                        transactions.'jdbc-store'.action.each { it.attributes().remove('table-prefix') }
                    } else {
                        transactions.'jdbc-store'.action.'@table-prefix' = actionStoreTablePrefix
                    }
                }
            }
        }
        // state node (something need to be changed)
        if (changeCommunication) {
            // new node
            if (transactions.'jdbc-store'.communication.size() == 0) {
                transactions.'jdbc-store'.appendNode {
                    'communication'(communicationAttribs)
                }
            } else {
                if (nn(communicationStoreDropTable)) {
                    transactions.'jdbc-store'.communication.'@drop-table' = communicationStoreDropTable
                }
                if (nn(communicationStoreTablePrefix)) {
                    if (communicationStoreTablePrefix == "") {
                        transactions.'jdbc-store'.communication.each { it.attributes().remove('table-prefix') }
                    } else {
                        transactions.'jdbc-store'.communication.'@table-prefix' = communicationStoreTablePrefix
                    }
                }
            }
        }
        // state node (something need to be changed)
        if (changeState) {
            // new node
            if (transactions.'jdbc-store'.state.size() == 0) {
                transactions.'jdbc-store'.appendNode {
                    'state'(stateAttribs)
                }
            } else {
                if (nn(stateStoreDropTable)) {
                    transactions.'jdbc-store'.state.'@drop-table' = stateStoreDropTable
                }
                if (nn(stateStoreTablePrefix)) {
                    if (stateStoreTablePrefix == "") {
                        transactions.'jdbc-store'.state.each { it.attributes().remove('table-prefix') }
                    } else {
                        transactions.'jdbc-store'.state.'@table-prefix' = stateStoreTablePrefix
                    }
                }
            }
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
