package org.wildfly.extras.creaper.commands.logging;


import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * <p>
 * Command adds log category.
 * </p>
 */

public class AddLogCategory extends ManipulateLogCategory {

    private boolean replaceExisting;

    protected AddLogCategory(Builder builder) {
        setBaseProperties(builder);
        replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(AddLogCategory.class)
                .subtree("logging", Subtree.subsystem("logging"))

                .parameter("category", category)
                .parameter("handlers", handlers)
                .parameter("filter", filter)
                .parameter("level", level == null ? null : level.value())
                .parameter("useParentHandler", useParentHandler == null ? null : String.valueOf(useParentHandler))
                .parameter("replaceExisting", replaceExisting)

                .build();

        ctx.client.apply(transform);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address loggerAddress = Address.subsystem("logging").and("logger", category);

        if (replaceExisting) {
            try {
                ops.removeIfExists(loggerAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing logger " + category, e);
            }
        }

        if (handlers != null && handlers.size() > 0) {
            Values handlersValues = Values.empty();
            for (String handler : handlers) {
                handlersValues.andOptional("handler", handler);
            }
        }


        Values values = Values.empty()
                .and("category", category)
                .andOptional("level", level == null ? null : level.value())
                .andOptional("filter-spec", filter)
                .andOptional("use-parent-handlers", useParentHandler)
                .andListOptional(String.class, "handlers", handlers);


        Batch batch = new Batch();
        batch.add(loggerAddress, values);

        ops.batch(batch);
    }

    public static final class Builder extends ManipulateLogCategory.Builder<Builder> {

        private boolean replaceExisting = false;

        public Builder(final String category) {
            super(category);
        }

        public AddLogCategory build() {
            return new AddLogCategory(this);
        }

        /**
         * Set true if replacing of existing node is needed.
         * Default value is false.
         */
        public Builder setReplaceExisting(final boolean value) {
            replaceExisting = value;
            return this;
        }
    }
}
