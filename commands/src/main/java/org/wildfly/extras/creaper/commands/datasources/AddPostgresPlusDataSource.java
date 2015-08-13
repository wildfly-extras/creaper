package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRES_PLUS_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRES_PLUS_VALID_CONNECTION_CHECKER;

/**
 * Creates a Postgres Plus datasource.
 *
 * <p>Connection URL format: {@code jdbc:edb://localhost:5432/postgres}</p>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_PostgreSQL_Datasource1">
 * Example PostgreSQL Datasource</a>.</p>
 *
 * @see AddPostgreSqlDataSource
 */
public final class AddPostgresPlusDataSource extends AddDataSource {
    AddPostgresPlusDataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = POSTGRES_PLUS_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = POSTGRES_PLUS_EXCEPTION_SORTER;
    }

    public static final class Builder extends AddDataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddPostgresPlusDataSource build() {
            check();
            return new AddPostgresPlusDataSource(this);
        }
    }
}
