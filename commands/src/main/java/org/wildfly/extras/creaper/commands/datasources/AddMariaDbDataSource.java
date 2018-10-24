package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ServerVersion;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MYSQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MYSQL_VALID_CONNECTION_CHECKER;

/**
 * Creates a Maria DB datasource.
 *
 * <p>Connection URL format is expected to be: {@code jdbc:mariadb://localhost:3306/database-name}</p>
 *
 * <p>Applies defaults of datasource settings used for testing JBoss EAP 7.<br>
 * Note: classes of connection checker or exception sorter are the same as for MySQL datasource.
 * </p>
 */
public final class AddMariaDbDataSource extends AddDataSource {
    AddMariaDbDataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ServerVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = MYSQL_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = MYSQL_EXCEPTION_SORTER;
    }

    public static final class Builder extends AddDataSource.Builder<Builder> {
        public Builder(String name) {
            super(name);
        }

        public AddMariaDbDataSource build() {
            check();
            return new AddMariaDbDataSource(this);
        }
    }
}
