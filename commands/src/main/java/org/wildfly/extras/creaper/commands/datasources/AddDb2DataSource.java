package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_STALE_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;

/**
 * Creates a DB2 datasource.
 *
 * <p>Connection URL format: {@code jdbc:db2://localhost:50000/database-name}</p>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_IBM_DB2_Datasource">
 * Example IBM DB2 Datasource</a>.</p>
 */
public final class AddDb2DataSource extends AddDataSource {
    AddDb2DataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = DB2_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = DB2_EXCEPTION_SORTER;
        if (staleConnectionCheckerClass == null) staleConnectionCheckerClass = DB2_STALE_CONNECTION_CHECKER;
    }

    public static final class Builder extends AddDataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddDb2DataSource build() {
            check();
            return new AddDb2DataSource(this);
        }
    }
}
