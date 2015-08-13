package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRESQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRESQL_VALID_CONNECTION_CHECKER;

/**
 * Creates a PostgreSQL datasource.
 *
 * <p>Connection URL format: {@code jdbc:postgresql://localhost:5432/database-name}</p>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_PostgreSQL_Datasource1">
 * Example PostgreSQL Datasource</a>.</p>
 */
public final class AddPostgreSqlDataSource extends AddDataSource {
    AddPostgreSqlDataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = POSTGRESQL_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = POSTGRESQL_EXCEPTION_SORTER;
    }

    public static final class Builder extends AddDataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddPostgreSqlDataSource build() {
            check();
            return new AddPostgreSqlDataSource(this);
        }
    }
}
