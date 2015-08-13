package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MSSQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MSSQL_VALID_CONNECTION_CHECKER;

/**
 * Creates an MS SQL datasource.
 *
 * <p>Connection URL format: {@code jdbc:sqlserver://localhost:1433;DatabaseName=database-name}</p>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_Microsoft_SQLServer_Datasource1">
 * Example Microsoft SQLServer Datasource</a>.</p>
 */
public final class AddMssqlDataSource extends AddDataSource {
    AddMssqlDataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = MSSQL_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null && serverVersion.greaterThanOrEqualTo(ManagementVersion.VERSION_1_7_0))
            // implementation of MS SQL exception sorter was not available in previous versions
            exceptionSorterClass = MSSQL_EXCEPTION_SORTER;
    }

    public static final class Builder extends AddDataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddMssqlDataSource build() {
            check();
            return new AddMssqlDataSource(this);
        }
    }
}
