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

public final class ChangeBasicTransactionAttributes implements OnlineCommand, OfflineCommand {

    private final Integer timeout;
    private final Boolean enableTtsmStatus;
    private final Boolean journalStoreEnableAsyncIO;
    private final Boolean jts;
    private final String nodeIdentifier;
    private final Boolean statisticsEnabled;
    private final Boolean useJournalStore;

    private final Boolean processIdUuid;
    private final String processIdSocketBinding;
    private final Integer processIdSocketMaxPorts;

    private final String socketBinding;
    private final String statusSocketBinding;
    private final Boolean recoveryListener;

    private final String objectStorePath;
    private final String objectStoreRelativeTo;


    private ChangeBasicTransactionAttributes(Builder builder) {
        timeout = builder.timeout;
        enableTtsmStatus = builder.enableTsmStatus;
        journalStoreEnableAsyncIO = builder.journalStoreEnableAsyncIO;
        jts = builder.jts;
        nodeIdentifier = builder.nodeIdentifier;
        statisticsEnabled = builder.statisticsEnabled;
        useJournalStore = builder.useJournalStore;
        processIdUuid = builder.processIdUuid;
        processIdSocketBinding = builder.processIdSocketBinding;
        processIdSocketMaxPorts = builder.processIdSocketMaxPorts;
        socketBinding = builder.socketBinding;
        statusSocketBinding = builder.statusSocketBinding;
        recoveryListener = builder.recoveryListener;
        objectStorePath = builder.objectStorePath;
        objectStoreRelativeTo = builder.objectStoreRelativeTo;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {

        GroovyXmlTransform transform = GroovyXmlTransform.of(ChangeBasicTransactionAttributes.class)
                .subtree("transactions", Subtree.subsystem("transactions"))

                .parameter("nodeIdentifier", nodeIdentifier)
                .parameter("timeout", timeout)
                .parameter("enableTsmStatus", enableTtsmStatus)
                .parameter("jts", jts)
                .parameter("statisticsEnabled", statisticsEnabled)
                .parameter("useJournalStore", useJournalStore)
                .parameter("journalStoreEnableAsyncIO", journalStoreEnableAsyncIO)

                .parameter("processIdUuid", processIdUuid)
                .parameter("processIdSocketBinding", processIdSocketBinding)
                .parameter("processIdSocketMaxPorts", processIdSocketMaxPorts)

                .parameter("socketBinding", socketBinding)
                .parameter("statusSocketBinding", statusSocketBinding)
                .parameter("recoveryListener", recoveryListener)

                .parameter("objectStorePath", objectStorePath)
                .parameter("objectStoreRelativeTo", objectStoreRelativeTo)

                .parameter("serverVersion", ctx.version)

                .build();

        ctx.client.apply(transform);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address transatcionsAddress = Address.subsystem("transactions");

        Batch batch = new Batch();

        if (nodeIdentifier != null) {
            batch.writeAttribute(transatcionsAddress, "node-identifier", nodeIdentifier);
        }
        if (enableTtsmStatus != null) {
            batch.writeAttribute(transatcionsAddress, "enable-tsm-status", enableTtsmStatus);
        }
        if (timeout != null) {
            batch.writeAttribute(transatcionsAddress, "default-timeout", timeout);
        }

        // cli allows both enable-statistics and statistics-enabled for WildFly 8 and WildFly 9,
        // statistics-enabled is in configuration files since WildFly 8
        if (statisticsEnabled != null) {
            if (ctx.version.lessThan(ServerVersion.VERSION_2_0_0)) {
                batch.writeAttribute(transatcionsAddress, "enable-statistics", statisticsEnabled);
            } else {
                batch.writeAttribute(transatcionsAddress, "statistics-enabled", statisticsEnabled);
            }
        }
        if (jts != null) {
            batch.writeAttribute(transatcionsAddress, "jts", jts);
        }

        if (useJournalStore != null) {
            if (ctx.version.lessThan(ServerVersion.VERSION_4_0_0)) {
                batch.writeAttribute(transatcionsAddress, "use-hornetq-store", useJournalStore);
            } else {
                batch.writeAttribute(transatcionsAddress, "use-journal-store", useJournalStore);

            }
        }
        if (journalStoreEnableAsyncIO != null) {
            if (ctx.version.lessThan(ServerVersion.VERSION_4_0_0)) {
                batch.writeAttribute(transatcionsAddress, "hornetq-store-enable-async-io", journalStoreEnableAsyncIO);
            } else {
                batch.writeAttribute(transatcionsAddress, "journal-store-enable-async-io", journalStoreEnableAsyncIO);
            }
        }
        if (processIdUuid != null || processIdSocketBinding != null) {
            if (processIdUuid != null) {
                batch.writeAttribute(transatcionsAddress, "process-id-uuid", processIdUuid);
            } else {
                // process id sock. binding is not allowed along with enabled uuid therefore it needs to be disabled
                batch.undefineAttribute(transatcionsAddress, "process-id-uuid");
                if (processIdSocketBinding != null) {
                    batch.writeAttribute(transatcionsAddress, "process-id-socket-binding", processIdSocketBinding);
                }
                if (processIdSocketMaxPorts != null) {
                    batch.writeAttribute(transatcionsAddress, "process-id-socket-max-ports", processIdSocketMaxPorts);
                }
            }
        }
        if (socketBinding != null) {
            batch.writeAttribute(transatcionsAddress, "socket-binding", socketBinding);
        }
        if (statusSocketBinding != null) {
            batch.writeAttribute(transatcionsAddress, "status-socket-binding", statusSocketBinding);
        }
        if (recoveryListener != null) {
            batch.writeAttribute(transatcionsAddress, "recovery-listener", recoveryListener);
        }
        if (objectStorePath != null) {
            batch.writeAttribute(transatcionsAddress, "object-store-path", objectStorePath);
        }
        if (objectStoreRelativeTo != null) {
            batch.writeAttribute(transatcionsAddress, "object-store-relative-to", objectStoreRelativeTo);
        }

        ops.batch(batch);

    }

    public static final class Builder {

        private Integer timeout;
        private Boolean enableTsmStatus;
        private Boolean journalStoreEnableAsyncIO;
        private Boolean jts;
        private String nodeIdentifier;
        private Boolean statisticsEnabled;
        private Boolean useJournalStore;

        private Boolean processIdUuid;
        private String processIdSocketBinding;
        private Integer processIdSocketMaxPorts;

        private String socketBinding;
        private String statusSocketBinding;
        private Boolean recoveryListener;

        private String objectStorePath;
        private String objectStoreRelativeTo;

        public Builder timeout(int val) {
            timeout = val;
            return this;
        }

        public Builder enableTsmStatus(boolean val) {
            enableTsmStatus = val;
            return this;
        }

        /**
         * "hornetq-store-enable-async-io" will be used for AS7, WildFly 8 and WildFly 9
         */
        public Builder journalStoreEnableAsyncIO(boolean val) {
            journalStoreEnableAsyncIO = val;
            return this;
        }

        public Builder jts(boolean val) {
            jts = val;
            return this;
        }

        public Builder nodeIdentifier(String val) {
            if (val == null) {
                throw new IllegalArgumentException("Node identifier can not be null!");
            }
            nodeIdentifier = val;
            return this;
        }

        public Builder statisticsEnabled(boolean val) {
            statisticsEnabled = val;
            return this;
        }

        /**
         * "use-hornetq-store" will be used for AS7, WildFly 8 and WildFly 9
         */
        public Builder useJournalStore(boolean val) {
            useJournalStore = val;
            return this;
        }

        public Builder processIdUuid(Boolean val) {
            processIdUuid = val;
            return this;
        }

        public Builder processIdSocketBinding(String val) {
            processIdSocketBinding = val;
            return this;
        }

        public Builder processIdSocketMaxPorts(Integer val) {
            processIdSocketMaxPorts = val;
            return this;
        }

        public Builder socketBinding(String val) {
            socketBinding = val;
            return this;
        }

        public Builder statusSocketBinding(String val) {
            statusSocketBinding = val;
            return this;
        }

        public Builder recoveryListener(Boolean val) {
            recoveryListener = val;
            return this;
        }

        public Builder objectStorePath(String val) {
            objectStorePath = val;
            return this;
        }

        public Builder objectStoreRelativeTo(String val) {
            objectStoreRelativeTo = val;
            return this;
        }

        public ChangeBasicTransactionAttributes build() {
            return new ChangeBasicTransactionAttributes(this);
        }
    }
}
