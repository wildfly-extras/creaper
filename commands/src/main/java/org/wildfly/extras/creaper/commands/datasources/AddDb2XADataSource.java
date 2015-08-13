package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import java.util.HashMap;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_RECOVERY_PLUGIN;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_STALE_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_XA_DATASOURCE_CLASS;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;

/**
 * Creates a DB2 XA datasource.
 *
 * <p>Standard XA datasource properties to use are:</p>
 * <ul>
 *     <li>{@code ServerName}</li>
 *     <li>{@code PortNumber} (if not set, a default of {@code 50000} will be used)</li>
 *     <li>{@code DatabaseName}</li>
 * </ul>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_IBM_DB2_XA_Datasource1">
 * Example IBM DB2 XA Datasource</a>.</p>
 */
public final class AddDb2XADataSource extends AddXADataSource {
    AddDb2XADataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        backgroundValidation = true;
        backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        validConnectionCheckerClass = DB2_VALID_CONNECTION_CHECKER;
        exceptionSorterClass = DB2_EXCEPTION_SORTER;
        staleConnectionCheckerClass = DB2_STALE_CONNECTION_CHECKER;
        sameRmOverride = false;
        xaDatasourceClass = DB2_XA_DATASOURCE_CLASS;
        recoveryPluginClass = DB2_RECOVERY_PLUGIN;

        if (recoveryPluginProperties == null) recoveryPluginProperties = new HashMap<String, String>();
        if (!recoveryPluginProperties.containsKey("EnableIsValid")) recoveryPluginProperties.put("EnableIsValid", "false");
        if (!recoveryPluginProperties.containsKey("IsValidOverride")) recoveryPluginProperties.put("IsValidOverride", "false");
        if (!recoveryPluginProperties.containsKey("EnableClose")) recoveryPluginProperties.put("EnableClose", "false");

        if (xaDatasourceProperties == null) xaDatasourceProperties = new HashMap<String, String>();
        if (!xaDatasourceProperties.containsKey("PortNumber")) xaDatasourceProperties.put("PortNumber", "50000");
    }

    public static final class Builder extends AddXADataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddDb2XADataSource build() {
            check();
            return new AddDb2XADataSource(this);
        }
    }
}
