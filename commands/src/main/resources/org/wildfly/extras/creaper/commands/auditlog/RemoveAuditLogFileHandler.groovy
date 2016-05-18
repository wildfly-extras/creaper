def handlerToRemove = management.'audit-log'.'handlers'.'file-handler'.find { it.'@name' == atrFileHandlerName }

if (handlerToRemove) {
    handlerToRemove.replaceNode {}
} else {
    throw new IllegalStateException("File handler $handlerToRemove not found => can't be removed")
}
