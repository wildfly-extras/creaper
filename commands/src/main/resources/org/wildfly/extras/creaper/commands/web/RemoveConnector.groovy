def connectorToDel = web.connector.find { it.@name == connectorName }
if (connectorToDel) {
    connectorToDel.replaceNode {}
} else {
    throw new IllegalStateException("Connector ${connectorName} not found => can't remove it")
}
