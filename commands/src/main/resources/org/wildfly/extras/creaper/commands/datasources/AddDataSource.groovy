// attributes of <datasource>
datasourceAttrs = ['pool-name': poolName]
if (nn(jta)) datasourceAttrs['jta'] = jta
if (nn(jndiName)) datasourceAttrs['jndi-name'] = jndiName
if (nn(enableAfterCreation)) datasourceAttrs['enabled'] = enableAfterCreation
if (nn(useJavaContext)) datasourceAttrs['use-java-context'] = useJavaContext
if (nn(spy)) datasourceAttrs['spy'] = spy
if (nn(useCcm)) datasourceAttrs['use-ccm'] = useCcm
if (nn(connectable)) datasourceAttrs['connectable'] = connectable
if (nn(statisticsEnabled)) datasourceAttrs['statistics-enabled'] = statisticsEnabled

// checks definition
def isPool = nn(maxPoolSize, minPoolSize, prefill, useStrictMinPoolSize, allowMultipleUsers, flushStrategy)
def isValidation = nn(validConnectionCheckerClassName, checkValidConnectionSql, validateOnMatch, backgroundValidation,
        backgroundValidationMillis, useFastFail, staleConnectionCheckerClassName, exceptionSorterClassName)
def isTimeout = nn(setTxQueryTimeout, blockingTimeoutWaitMillis, idleTimeoutMinutes, queryTimeout, useTryLock,
        allocationRetry, allocationRetryWaitMillis)
def isStatement = nn(trackStatements, preparedStatementsCacheSize, sharePreparedStatements)

// closure with XML structure definition
def dsDefinition = {
    datasource(datasourceAttrs) {
        if (nn(connectioUrl)) 'connection-url'(connectioUrl)
        if (nn(driverClass)) 'driver-class'(driverClass)
        if (nn(datasourceClass)) 'datasource-class'(datasourceClass)
        if (nn(connectionProperties)) {
            connectionProperties.each {
                'connection-property'(name: it.key, it.value)
            }
        }
        if (nn(driverName)) 'driver'(driverName)
        if (nn(newConnectionSql)) 'new-connection-sql'(newConnectionSql)
        if (nn(transactionIsolation)) 'transaction-isolation'(transactionIsolation)
        if (nn(urlDelimiter)) 'url-delimiter'(urlDelimiter)
        if (isPool) {
            pool {
                if (nn(minPoolSize)) 'min-pool-size'(minPoolSize)
                if (nn(maxPoolSize)) 'max-pool-size'(maxPoolSize)
                if (nn(prefill)) 'prefill'(prefill)
                if (nn(useStrictMinPoolSize)) 'use-strict-min'(useStrictMinPoolSize)
                if (nn(flushStrategy)) 'flush-strategy'(flushStrategy)
                if (nn(allowMultipleUsers)) 'allow-multiple-users'(allowMultipleUsers)
            }
        }
        if (nn(userName, password, securityDomain)) {
            security {
                if (nn(userName)) 'user-name'(userName)
                if (nn(password)) 'password'(password)
                if (nn(securityDomain)) 'security-domain'(securityDomain)
            }
        }
        if (isValidation) {
            validation {
                if (nn(validConnectionCheckerClassName)) {
                    'valid-connection-checker'('class-name': validConnectionCheckerClassName) {
                        validConnectionCheckerProperties.each {
                            'config-property'(name: it.key, it.value)
                        }
                    }
                }
                if (nn(checkValidConnectionSql)) 'check-valid-connection-sql'(checkValidConnectionSql)
                if (nn(validateOnMatch)) 'validate-on-match'(validateOnMatch)
                if (nn(backgroundValidation)) 'background-validation'(backgroundValidation)
                if (nn(backgroundValidationMillis)) 'background-validation-millis'(backgroundValidationMillis)
                if (nn(useFastFail)) 'use-fast-fail'(useFastFail)
                if (nn(staleConnectionCheckerClassName)) {
                    'stale-connection-checker'('class-name': staleConnectionCheckerClassName) {
                        staleConnectionCheckerProperties.each {
                            'config-property'(name: it.key, it.value)
                        }
                    }
                }
                if (nn(exceptionSorterClassName)) {
                    'exception-sorter'('class-name': exceptionSorterClassName) {
                        exceptionSorterProperties.each {
                            'config-property'(name: it.key, it.value)
                        }
                    }
                }
            }
        }
        if (isTimeout) {
            timeout {
                if (nn(setTxQueryTimeout)) 'set-tx-query-timeout'(setTxQueryTimeout)
                if (nn(blockingTimeoutWaitMillis)) 'blocking-timeout-millis'(blockingTimeoutWaitMillis)
                if (nn(idleTimeoutMinutes)) 'idle-timeout-minutes'(idleTimeoutMinutes)
                if (nn(queryTimeout)) 'query-timeout'(queryTimeout)
                if (nn(useTryLock)) 'use-try-lock'(useTryLock)
                if (nn(allocationRetry)) 'allocation-retry'(allocationRetry)
                if (nn(allocationRetryWaitMillis)) 'allocation-retry-wait-millis'(allocationRetryWaitMillis)
            }
        }
        if (isStatement) {
            statement {
                if (nn(trackStatements)) 'track-statements'(trackStatements)
                if (nn(preparedStatementsCacheSize)) 'prepared-statement-cache-size'(preparedStatementsCacheSize)
                if (nn(sharePreparedStatements)) 'share-prepared-statements'(sharePreparedStatements)
            }
        }
    }
}

// adding or replacing existing datasource
def existingDs = datasources.datasources.datasource.find { it.'@pool-name' == poolName }
if (existingDs && !replaceExisting) {
    throw new IllegalStateException("Datasource $poolName already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (existingDs) {
    existingDs.replaceNode dsDefinition
} else {
    datasources.datasources.appendNode dsDefinition
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
