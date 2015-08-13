package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_STALE_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_VALID_CONNECTION_CHECKER;

/**
 * Creates an Oracle datasource.
 *
 * <p>Connection URL format: {@code jdbc:oracle:thin:@localhost:1521:XE}</p>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_Oracle_Datasource">
 * Example Oracle Datasource</a>.</p>
 */
public final class AddOracleDataSource extends AddDataSource {
    AddOracleDataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = ORACLE_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = ORACLE_EXCEPTION_SORTER;
        if (staleConnectionCheckerClass == null) staleConnectionCheckerClass = ORACLE_STALE_CONNECTION_CHECKER;
    }

    public static final class Builder extends AddDataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddOracleDataSource build() {
            check();
            return new AddOracleDataSource(this);
        }
    }
}
