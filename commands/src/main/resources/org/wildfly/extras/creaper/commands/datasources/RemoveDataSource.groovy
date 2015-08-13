def ds = datasources.datasources.datasource.find { it.'@pool-name' == name }
if (!ds) {
    throw new IllegalStateException("Can't remove datasource $name as it does not exist in the configuration")
}

ds.replaceNode {}
