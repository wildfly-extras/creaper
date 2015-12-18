package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import java.util.HashMap;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MYSQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MYSQL_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MARIADB_XA_DATASOURCE_CLASS;
/**
 * Creates a Maria DB XA datasource.
 *
 * <p>Standard XA datasource properties to use are:</p>
 * <ul>
 *     <li>{@code ServerName}</li>
 *     <li>{@code PortNumber} (if not set, a default of {@code 3306} will be used)</li>
 *     <li>{@code DatabaseName}</li>
 * </ul>
 *
 * <p>Applies defaults from testing JBoss EAP 7.<br/>
 * Note: classes for connection checker and exception sorter are the same as for MySQL datasource</p>
 */
public final class AddMariaDbXADataSource extends AddXADataSource {
    AddMariaDbXADataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = MYSQL_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = MYSQL_EXCEPTION_SORTER;
        if (xaDatasourceClass == null) xaDatasourceClass = MARIADB_XA_DATASOURCE_CLASS;

        if (xaDatasourceProperties == null) xaDatasourceProperties = new HashMap<String, String>();
        if (!xaDatasourceProperties.containsKey("PortNumber")) xaDatasourceProperties.put("PortNumber", "3306");
    }

    public static final class Builder extends AddXADataSource.Builder<Builder> {
        public Builder(String name) {
            super(name);
        }

        public AddMariaDbXADataSource build() {
            check();
            return new AddMariaDbXADataSource(this);
        }
    }
}
