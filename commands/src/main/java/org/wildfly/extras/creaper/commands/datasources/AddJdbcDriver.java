package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * Command which creates a new JDBC driver in the {@code datasources} subsystem. It needs an existing
 * module with the JDBC driver JAR.
 */
public final class AddJdbcDriver implements OnlineCommand, OfflineCommand {
    private final String driverName;
    private final String driverClass;
    private final String datasourceClass;
    private final String xaDatasourceClass;
    private final String module;
    private final String moduleSlot;

    private AddJdbcDriver(Builder builder) {
        this.driverName = builder.driverName;
        this.driverClass = builder.driverClass;
        this.datasourceClass = builder.datasourceClass;
        this.xaDatasourceClass = builder.xaDatasourceClass;
        this.module = builder.module;
        this.moduleSlot = builder.moduleSlot;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Operations ops = new Operations(ctx.client);

        ops.add(Address.subsystem("datasources").and("jdbc-driver", driverName), Values.empty()
                .and("driver-name", driverName)
                .and("driver-module-name", module)
                .andOptional("module-slot", moduleSlot)
                .andOptional("driver-class-name", driverClass)
                .andOptional("driver-datasource-class-name", datasourceClass)
                .andOptional("driver-xa-datasource-class-name", xaDatasourceClass));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        GroovyXmlTransform transform = GroovyXmlTransform.of(AddJdbcDriver.class)
                .subtree("datasources", Subtree.subsystem("datasources"))
                .parameter("driverName", this.driverName)
                .parameter("module", this.module)
                .parameter("moduleSlot", this.moduleSlot)
                .parameter("driverClass", this.driverClass)
                .parameter("datasourceClass", this.datasourceClass)
                .parameter("xaDatasourceClass", this.xaDatasourceClass)
                .build();
        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddJdbcDriver " + driverName;
    }

    /**
     * Builder for configuration attributes of a JDBC driver in the {@code datasources} susbystem.
     *
     * @see <a href="http://wildscribe.github.io/JBoss EAP/6.2.0/subsystem/datasources/jdbc-driver/">
     *        http://wildscribe.github.io/JBoss EAP/6.2.0/subsystem/datasources/jdbc-driver/</a>
     */
    public static final class Builder {
        private String driverName;
        private String driverClass;
        private String datasourceClass;
        private String xaDatasourceClass;
        private String module;
        private String moduleSlot;

        /**
         * @param driverName Defines the JDBC driver the datasource should use. It is a symbolic name matching
         *                   the name of installed driver. In case the driver is deployed as jar, the name is the name
         *                   of the deployment unit
         * @param module the name of the module from which the driver should be loaded, if it is to be loaded
         *               from the module path
         */
        public Builder(String driverName, String module) {
            if (driverName == null) {
                throw new IllegalArgumentException("Name of the JDBC driver must be specified as non null value");
            }
            if (module == null) {
                throw new IllegalArgumentException("Name of the module must be specified as non null value");
            }

            this.driverName = driverName;
            this.module = module;
        }

        /**
         * The fully qualified class name of the java.sql.Driver implementation
         */
        public Builder driverClass(String driverClass) {
            this.driverClass = driverClass;
            return this;
        }

        /**
         * The fully qualified class name of the javax.sql.DataSource implementation
         */
        public Builder datasourceClass(String datasourceClass) {
            this.datasourceClass = datasourceClass;
            return this;
        }

        /**
         * The fully qualified class name of the javax.sql.XADataSource implementation
         */
        public Builder xaDatasourceClass(String xaDatasourceClass) {
            this.xaDatasourceClass = xaDatasourceClass;
            return this;
        }

        /**
         * The slot of the module from which the driver was loaded, if it was loaded from the module path
         */
        public Builder moduleSlot(String moduleSlot) {
            this.moduleSlot = moduleSlot;
            return this;
        }

        public AddJdbcDriver build() {
            return new AddJdbcDriver(this);
        }
    }
}
