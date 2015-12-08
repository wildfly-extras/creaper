def logger = logging.logger.find { it.@category == category }
if (!logger) {
    throw new IllegalStateException("Can't remove logger $category as it does not exist in the configuration")
}

logger.replaceNode {}
