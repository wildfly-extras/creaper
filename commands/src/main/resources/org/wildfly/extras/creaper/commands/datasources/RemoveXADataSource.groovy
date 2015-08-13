def ds = datasources.datasources.'xa-datasource'.find { it.'@pool-name' == name }
if (!ds) {
    throw new IllegalStateException("Can't remove XA datasource $name as it does not exist in the configuration")
}

ds.replaceNode {}
