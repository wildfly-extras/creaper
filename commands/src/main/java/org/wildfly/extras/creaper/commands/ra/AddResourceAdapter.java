package org.wildfly.extras.creaper.commands.ra;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * Adds new resource adapter
 */
public final class AddResourceAdapter implements OnlineCommand {
    private final String id;
    private final String module;
    private final String transactions;

    private AddResourceAdapter(Builder builder) {
        this.id = builder.resourceAdapterId;
        this.module = builder.module;
        switch (builder.transactions) {
            case NONE:
                transactions = TransactionType.NONE.getValue();
                break;
            case XA:
                transactions = TransactionType.XA.getValue();
                break;
            default:
                transactions = TransactionType.NONE.getValue();
                break;
        }
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address address = Address.subsystem("resource-adapters").and("resource-adapter", id);
        ops.add(address, Values.of("transaction-support", transactions).and("module", module).and("slot", "main"));
    }

    public static final class Builder {
        private final String resourceAdapterId;
        private final String module;
        private final TransactionType transactions;

        /**
         * @param resourceAdapterId     name of resource adapter
         * @param resourceAdapterModule module with resource adapter
         * @param transactionSupport    type of transaction
         */
        public Builder(String resourceAdapterId, String resourceAdapterModule, TransactionType transactionSupport) {
            if (resourceAdapterId == null) {
                throw new IllegalArgumentException("resourceAdapterId must be specified");
            }
            if (resourceAdapterModule == null) {
                throw new IllegalArgumentException("resourceAdapterModule must be specified");
            }
            if (transactionSupport == null) {
                throw new IllegalArgumentException("transactionSupport must be specified");
            }

            this.resourceAdapterId = resourceAdapterId;
            this.module = resourceAdapterModule;
            this.transactions = transactionSupport;
        }

        public AddResourceAdapter build() {
            return new AddResourceAdapter(this);
        }
    }

    @Override
    public String toString() {
        return "AddResourceAdapter " + id;
    }
}
