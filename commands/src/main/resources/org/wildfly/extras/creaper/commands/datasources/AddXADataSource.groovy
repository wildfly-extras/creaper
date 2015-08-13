// attributes of <xa-datasource>
xaDatasourceAttrs = ['pool-name': poolName]
if (nn(jndiName)) xaDatasourceAttrs['jndi-name'] = jndiName
if (nn(enableAfterCreation)) xaDatasourceAttrs['enabled'] = enableAfterCreation
if (nn(useJavaContext)) xaDatasourceAttrs['use-java-context'] = useJavaContext
if (nn(spy)) xaDatasourceAttrs['spy'] = spy
if (nn(useCcm)) xaDatasourceAttrs['use-ccm'] = useCcm
if (nn(statisticsEnabled)) xaDatasourceAttrs['statistics-enabled'] = statisticsEnabled
// attributtes of <recovery>
recoveryAttrs = [:]
if (nn(noRecovery)) recoveryAttrs['no-recovery'] = noRecovery

// checks definition
def isXaPool = nn(maxPoolSize, minPoolSize, prefill, useStrictMinPoolSize, allowMultipleUsers, flushStrategy,
        sameRmOverride, interleaving, noTxSeparatePool, padXid, wrapXaResource)
def isRecovery = nn(noRecovery, recoveryUsername, recoveryPassword, recoverySecurityDomain, recoveryPluginClass)
def isValidation = nn(validConnectionCheckerClass, checkValidConnectionSql, validateOnMatch, backgroundValidation,
        backgroundValidationMillis, useFastFail, staleConnectionCheckerClass, exceptionSorterClass)
def isTimeout = nn(setTxQueryTimeout, blockingTimeoutWaitMillis, idleTimeoutMinutes, queryTimeout, useTryLock,
        allocationRetry, allocationRetryWaitMillis)
def isStatement = nn(trackPreparedStatements, preparedStatementsCacheSize, sharePreparedStatements)

// closure with XML structure
def xaDsDefinition = {
    'xa-datasource'(xaDatasourceAttrs) {
        xaDatasourceProperties.each {
            'xa-datasource-property'(name: it.key, it.value)
        }

        if (nn(xaDatasourceClass)) 'xa-datasource-class'(xaDatasourceClass)
        if (nn(driverName)) 'driver'(driverName)
        if (nn(urlDelimiter)) 'url-delimiter'(urlDelimiter)
        if (nn(newConnectionSql)) 'new-connection-sql'(newConnectionSql)
        if (nn(transactionIsolation)) 'transaction-isolation'(transactionIsolation)

        if (isXaPool) {
            'xa-pool' {
                if (nn(minPoolSize)) 'min-pool-size'(minPoolSize)
                if (nn(maxPoolSize)) 'max-pool-size'(maxPoolSize)
                if (nn(prefill)) 'prefill'(prefill)
                if (nn(useStrictMinPoolSize)) 'use-strict-min'(useStrictMinPoolSize)
                if (nn(flushStrategy)) 'flush-strategy'(flushStrategy)
                if (nn(allowMultipleUsers)) 'allow-multiple-users'(allowMultipleUsers)
                if (nn(sameRmOverride)) 'is-same-rm-override'(sameRmOverride)
                if (nn(interleaving)) 'interleaving'(interleaving)
                if (nn(noTxSeparatePool)) 'no-tx-separate-pools'(noTxSeparatePool)
                if (nn(padXid)) 'pad-xid'(padXid)
                if (nn(wrapXaResource)) 'wrap-xa-resource'(wrapXaResource)
            }
        }

        if (nn(username, password, securityDomain)) {
            security {
                if (nn(username)) 'user-name'(username)
                if (nn(password)) 'password'(password)
                if (nn(securityDomain)) 'security-domain'(securityDomain)
            }
        }

        if (isRecovery) {
            recovery(recoveryAttrs) {
                if (nn(recoveryUsername, recoveryPassword, recoverySecurityDomain)) {
                    'recover-credential' {
                        if (nn(recoveryUsername)) 'user-name'(recoveryUsername)
                        if (nn(recoveryPassword)) 'password'(recoveryPassword)
                        if (nn(recoverySecurityDomain)) 'security-domain'(recoverySecurityDomain)
                    }
                }
                if (nn(recoveryPluginClass)) {
                    'recover-plugin'('class-name': recoveryPluginClass) {
                        recoveryPluginProperties.each {
                            'config-property'(name: it.key, it.value)
                        }
                    }
                }
            }
        }

        if (isValidation) {
            validation {
                if (nn(validConnectionCheckerClass)) {
                    'valid-connection-checker'('class-name': validConnectionCheckerClass) {
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
                if (nn(staleConnectionCheckerClass)) {
                    'stale-connection-checker'('class-name': staleConnectionCheckerClass) {
                        staleConnectionCheckerProperties.each {
                            'config-property'(name: it.key, it.value)
                        }
                    }
                }
                if (nn(exceptionSorterClass)) {
                    'exception-sorter'('class-name': exceptionSorterClass) {
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
                if (nn(xaResourceTimeout)) 'xa-resource-timeout'(xaResourceTimeout)
            }
        }

        if (isStatement) {
            statement {
                if (nn(trackPreparedStatements)) 'track-statements'(trackPreparedStatements)
                if (nn(preparedStatementsCacheSize)) 'prepared-statement-cache-size'(preparedStatementsCacheSize)
                if (nn(sharePreparedStatements)) 'share-prepared-statements'(sharePreparedStatements)
            }
        }
    }
}

// adding or replacing existing datasource
def existingDs = datasources.datasources.'xa-datasource'.find { it.'@pool-name' == poolName }
if (existingDs.asBoolean() && !replaceExisting) {
    throw new IllegalStateException("XA-Datasource $poolName already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (existingDs.asBoolean()) {
    existingDs.replaceNode xaDsDefinition
} else {
    datasources.datasources.appendNode xaDsDefinition
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
