package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_STALE_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_XA_DATASOURCE_CLASS;

/**
 * Creates an Oracle XA datasource.
 *
 * <p>Standard XA datasource properties to use are:</p>
 * <ul>
 *     <li>{@code URL}</li>
 * </ul>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_Oracle_XA_Datsource1">
 * Example Oracle XA Datasource</a>.</p>
 */
public final class AddOracleXADataSource extends AddXADataSource {
    AddOracleXADataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = ORACLE_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = ORACLE_EXCEPTION_SORTER;
        if (staleConnectionCheckerClass == null) staleConnectionCheckerClass = ORACLE_STALE_CONNECTION_CHECKER;
        if (sameRmOverride == null) sameRmOverride = false;
        if (noTxSeparatePool == null) noTxSeparatePool = true;
        if (xaDatasourceClass == null) xaDatasourceClass = ORACLE_XA_DATASOURCE_CLASS;
    }

    public static final class Builder extends AddXADataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddOracleXADataSource build() {
            check();
            return new AddOracleXADataSource(this);
        }
    }
}
