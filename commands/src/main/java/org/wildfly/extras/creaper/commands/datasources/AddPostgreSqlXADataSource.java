package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import java.util.HashMap;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRESQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRESQL_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRESQL_XA_DATASOURCE_CLASS;

/**
 * Creates a PostgreSQL XA datasource.
 *
 * <p>Standard XA datasource properties to use are:</p>
 * <ul>
 *     <li>{@code ServerName}</li>
 *     <li>{@code PortNumber} (if not set, a default of {@code 5432} will be used)</li>
 *     <li>{@code DatabaseName}</li>
 * </ul>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_PostgreSQL_XA_Datasource">
 * Example PostgreSQL XA Datasource</a>.</p>
 */
public final class AddPostgreSqlXADataSource extends AddXADataSource {
    AddPostgreSqlXADataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = POSTGRESQL_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = POSTGRESQL_EXCEPTION_SORTER;
        if (xaDatasourceClass == null) xaDatasourceClass = POSTGRESQL_XA_DATASOURCE_CLASS;

        if (xaDatasourceProperties == null) xaDatasourceProperties = new HashMap<String, String>();
        if (!xaDatasourceProperties.containsKey("PortNumber")) xaDatasourceProperties.put("PortNumber", "5432");
    }

    public static final class Builder extends AddXADataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddPostgreSqlXADataSource build() {
            check();
            return new AddPostgreSqlXADataSource(this);
        }
    }
}
