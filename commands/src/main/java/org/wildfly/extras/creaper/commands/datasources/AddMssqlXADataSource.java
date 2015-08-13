package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import java.util.HashMap;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MSSQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MSSQL_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MSSQL_XA_DATASOURCE_CLASS;

/**
 * Creates an MS SQL XA datasource.
 *
 * <p>Standard XA datasource properties to use are:</p>
 * <ul>
 *     <li>{@code ServerName}</li>
 *     <li>{@code PortNumber} (if not set, a default of {@code 1433} will be used)</li>
 *     <li>{@code DatabaseName}</li>
 *     <li>{@code SelectMethod} (if not set, a default of {@code cursor} will be used)</li>
 * </ul>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_Microsoft_SQLServer_XA_Datasource">
 * Example Microsoft SQLServer XA Datasource</a>.</p>
 */
public final class AddMssqlXADataSource extends AddXADataSource {
    AddMssqlXADataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = MSSQL_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null && serverVersion.greaterThan(ManagementVersion.VERSION_1_7_0))
            // implementation of MS SQL exception sorter was not available in previous versions
            exceptionSorterClass = MSSQL_EXCEPTION_SORTER;
        if (sameRmOverride == null) sameRmOverride = true;
        if (xaDatasourceClass == null) xaDatasourceClass = MSSQL_XA_DATASOURCE_CLASS;

        if (xaDatasourceProperties == null) xaDatasourceProperties = new HashMap<String, String>();
        if (!xaDatasourceProperties.containsKey("PortNumber")) xaDatasourceProperties.put("PortNumber", "1433");
        if (!xaDatasourceProperties.containsKey("SelectMethod")) xaDatasourceProperties.put("SelectMethod", "cursor");
    }

    public static final class Builder extends AddXADataSource.Builder {
        public Builder(String name) {
            super(name);

        }

        public AddMssqlXADataSource build() {
            check();
            return new AddMssqlXADataSource(this);
        }
    }
}
