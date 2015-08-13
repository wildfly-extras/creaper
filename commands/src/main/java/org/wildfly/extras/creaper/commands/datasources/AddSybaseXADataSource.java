package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.core.ManagementVersion;

import java.util.HashMap;

import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DEFAULT_BACKGROUND_VALIDATION_TIME;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_STALE_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_XA_DATASOURCE_CLASS;

/**
 * Creates a Sybase XA datasource.
 *
 * <p>Standard XA datasource properties to use are:</p>
 * <ul>
 *     <li>{@code ServerName}</li>
 *     <li>{@code PortNumber} (if not set, a default of {@code 4100} will be used)</li>
 *     <li>{@code DatabaseName}</li>
 *     <li>{@code NetworkProtocol} (if not set, a default of {@code Tds} will be used)</li>
 * </ul>
 *
 * <p>Applies defaults from
 * <a href="https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Administration_and_Configuration_Guide/index.html#Example_Sybase_XA_Datasource">
 * Example Sybase XA Datasource</a>.</p>
 */
public final class AddSybaseXADataSource extends AddXADataSource {
    AddSybaseXADataSource(Builder builder) {
        super(builder);
    }

    @Override
    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        if (backgroundValidation == null) backgroundValidation = true;
        if (backgroundValidationMillis == null) backgroundValidationMillis = DEFAULT_BACKGROUND_VALIDATION_TIME;
        if (validConnectionCheckerClass == null) validConnectionCheckerClass = SYBASE_VALID_CONNECTION_CHECKER;
        if (exceptionSorterClass == null) exceptionSorterClass = SYBASE_EXCEPTION_SORTER;
        if (staleConnectionCheckerClass == null) staleConnectionCheckerClass = SYBASE_STALE_CONNECTION_CHECKER;
        if (xaDatasourceClass == null) xaDatasourceClass = SYBASE_XA_DATASOURCE_CLASS;
        if (sameRmOverride == null) sameRmOverride = false;

        if (xaDatasourceProperties == null) xaDatasourceProperties = new HashMap<String, String>();
        if (!xaDatasourceProperties.containsKey("NetworkProtocol")) xaDatasourceProperties.put("NetworkProtocol", "Tds");
        if (!xaDatasourceProperties.containsKey("PortNumber")) xaDatasourceProperties.put("PortNumber", "4100");
    }

    public static final class Builder extends AddXADataSource.Builder {
        public Builder(String name) {
            super(name);
        }

        public AddSybaseXADataSource build() {
            check();
            return new AddSybaseXADataSource(this);
        }
    }
}
