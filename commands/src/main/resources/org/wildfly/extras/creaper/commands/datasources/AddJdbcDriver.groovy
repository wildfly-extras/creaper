def isExistingDriver = datasources.datasources.drivers.driver.any { it.'@name' == driverName }
if (isExistingDriver) {
    throw new IllegalStateException("Driver $driverName already exists in configuration. Use different name.")
}

// if module slot defined then it goes after colon to module name
def moduleNameCombined = module
if (moduleSlot != null) moduleNameCombined += ':' << moduleSlot

// driver xml structure construction
def driverDefinition = {
    driver(name: driverName, module: moduleNameCombined) {
        if (driverClass != null) 'driver-class'(driverClass)
        if (datasourceClass != null) 'datasource-class'(datasourceClass)
        if (xaDatasourceClass != null) 'xa-datasource-class'(xaDatasourceClass)
    }
}

def isExistingDrivers = datasources.datasources.drivers.any { it.name() == 'drivers' }
if (isExistingDrivers) {
    // configuration already contains <drivers> tag
    datasources.datasources.drivers.appendNode driverDefinition
} else {
    // <drivers> tag does not exist - let's create it
    datasources.datasources.appendNode {
        drivers(driverDefinition)
    }
}
