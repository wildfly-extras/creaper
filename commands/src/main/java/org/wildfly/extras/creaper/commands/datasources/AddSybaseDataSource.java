package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_VALID_CONNECTION_CHECKER;

/**
 * Creates a Sybase datasource.
 *
 * <p>Connection URL format: {@code jdbc:sybase:Tds:localhost:5000/database-name?JCONNECT_VERSION=6}</p>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_Sybase_Datasource">
 * Example Sybase Datasource</a>.</p>
 */
public final class AddSybaseDataSource extends AddDataSource {
    AddSybaseDataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = SYBASE_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = SYBASE_EXCEPTION_SORTER;
    }

    public static final class Builder extends AddDataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddSybaseDataSource build() {
            check();
            return new AddSybaseDataSource(this);
        }
    }
}
