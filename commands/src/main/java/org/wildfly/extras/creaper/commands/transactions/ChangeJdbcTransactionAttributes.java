package org.wildfly.extras.creaper.commands.transactions;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * JDBC options are available for EAP 6.2.0 and higher in transaction subsystem.
 *
 * <p>Please note that <b>WildFly 9 has a bug</b>: if the journal store is enabled, it isn't automatically disabled
 * when enabling the JDBC store. Moreover, disabling the journal store properly requires server restart,
 * which is why this command doesn't do that under the hood. All other versions (WildFly 8 and previous,
 * WildFly 10 and later) are fine.</p>
 */
public final class ChangeJdbcTransactionAttributes implements OfflineCommand, OnlineCommand {

    private final Boolean useJdbcStore;
    private final Boolean actionStoreDropTable;
    private final String actionStoreTablePrefix;
    private final Boolean communicationStoreDropTable;
    private final String communicationStoreTablePrefix;
    private final String stateStoreTablePrefix;
    private final String storeDatasource;
    private final Boolean stateStoreDropTable;

    private ChangeJdbcTransactionAttributes(Builder builder) {
        useJdbcStore = builder.useJdbcStore;
        actionStoreDropTable = builder.actionStoreDropTable;
        actionStoreTablePrefix = builder.actionStoreTablePrefix;
        communicationStoreDropTable = builder.communicationStoreDropTable;
        communicationStoreTablePrefix = builder.communicationStoreTablePrefix;
        stateStoreTablePrefix = builder.stateStoreTablePrefix;
        storeDatasource = builder.storeDatasource;
        stateStoreDropTable = builder.stateStoreDropTable;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.version.assertAtLeast(ServerVersion.VERSION_1_5_0,
                "JDBC options are available for EAP 6.2.0 and higher");

        GroovyXmlTransform transform = GroovyXmlTransform.of(ChangeJdbcTransactionAttributes.class)
                .subtree("transactions", Subtree.subsystem("transactions"))

                .parameter("useJdbcStore", useJdbcStore != null ? String.valueOf(useJdbcStore) : null)
                .parameter("storeDatasource", storeDatasource)
                .parameter("actionStoreDropTable",
                        actionStoreDropTable != null ? String.valueOf(actionStoreDropTable) : null)
                .parameter("actionStoreTablePrefix", actionStoreTablePrefix)
                .parameter("communicationStoreDropTable",
                        communicationStoreDropTable != null ? String.valueOf(communicationStoreDropTable) : null)
                .parameter("communicationStoreTablePrefix", communicationStoreTablePrefix)
                .parameter("stateStoreDropTable",
                        stateStoreDropTable != null ? String.valueOf(stateStoreDropTable) : null)
                .parameter("stateStoreTablePrefix", stateStoreTablePrefix)

                .build();

        ctx.client.apply(transform);

    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        ctx.version.assertAtLeast(ServerVersion.VERSION_1_5_0,
                "JDBC options are available for EAP 6.2.0 and higher");

        Batch batch = new Batch();
        Operations ops = new Operations(ctx.client);
        Address address = Address.subsystem("transactions");

        if (useJdbcStore != null) {
            batch.writeAttribute(address, "use-jdbc-store", useJdbcStore);
        }
        if (storeDatasource != null) {
            batch.writeAttribute(address, "jdbc-store-datasource", storeDatasource);
        }
        if (actionStoreDropTable != null) {
            batch.writeAttribute(address, "jdbc-action-store-drop-table", actionStoreDropTable);
        }
        if (actionStoreTablePrefix != null) {
            batch.writeAttribute(address, "jdbc-action-store-table-prefix", actionStoreTablePrefix);
        }
        if (communicationStoreDropTable != null) {
            batch.writeAttribute(address, "jdbc-communication-store-drop-table", communicationStoreDropTable);
        }
        if (communicationStoreTablePrefix != null) {
            batch.writeAttribute(address, "jdbc-communication-store-table-prefix", communicationStoreTablePrefix);
        }
        if (stateStoreDropTable != null) {
            batch.writeAttribute(address, "jdbc-state-store-drop-table", stateStoreDropTable);
        }
        if (actionStoreTablePrefix != null) {
            batch.writeAttribute(address, "jdbc-state-store-table-prefix", stateStoreTablePrefix);
        }

        ops.batch(batch);
    }

    /**
     * JDBC options are available for EAP 6.2.0 and higher in transaction subsystem.
     *
     * <p>Please note that <b>WildFly 9 has a bug</b>: if the journal store is enabled, it isn't automatically disabled
     * when enabling the JDBC store. Moreover, disabling the journal store properly requires server restart,
     * which is why this command doesn't do that under the hood. All other versions (WildFly 8 and previous,
     * WildFly 10 and later) are fine.</p>
     */
    public static final class Builder {

        protected Boolean useJdbcStore;
        protected String storeDatasource;
        protected Boolean actionStoreDropTable;
        protected String actionStoreTablePrefix;
        protected Boolean communicationStoreDropTable;
        protected String communicationStoreTablePrefix;
        protected Boolean stateStoreDropTable;
        protected String stateStoreTablePrefix;


        public Builder useJdbcStore(boolean value) {
            useJdbcStore = value;
            return this;
        }

        public Builder storeDatasource(String value) {
            storeDatasource = value;
            return this;
        }

        public Builder actionStoreDropTable(boolean value) {
            actionStoreDropTable = value;
            return this;
        }

        public Builder actionStoreTablePrefix(String value) {
            actionStoreTablePrefix = value;
            return this;
        }

        public Builder communicationStoreDropTable(boolean value) {
            communicationStoreDropTable = value;
            return this;
        }

        public Builder communicationStoreTablePrefix(String value) {
            communicationStoreTablePrefix = value;
            return this;
        }

        public Builder stateStoreDropTable(boolean value) {
            stateStoreDropTable = value;
            return this;
        }

        public Builder stateStoreTablePrefix(String value) {
            stateStoreTablePrefix = value;
            return this;
        }

        public ChangeJdbcTransactionAttributes build() {
            validate();
            return new ChangeJdbcTransactionAttributes(this);
        }

        private void validate() {
            if (useJdbcStore != null && useJdbcStore && (storeDatasource == null || storeDatasource.isEmpty())) {
                throw new IllegalArgumentException("if JDBC store is enabled, datasource must be defined");
            }
        }
    }
}
