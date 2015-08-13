def driver = datasources.datasources.drivers.driver.find { it.@name == driverName }
if (!driver) {
    throw new IllegalStateException("Can't remove JDBC driver $driverName as it does not exist in the configuration")
}

driver.replaceNode {}
