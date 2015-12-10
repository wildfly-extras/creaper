package org.wildfly.extras.creaper.commands.logging;


import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

final class AddLogCategory extends LogCategoryCommand {

    private boolean replaceExisting;

    private AddLogCategory(Builder builder) {
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

        Values values = Values.empty()
                .and("category", category)
                .andOptional("level", level == null ? null : level.value())
                .andOptional("filter-spec", filter)
                .andOptional("use-parent-handlers", useParentHandler)
                .andListOptional(String.class, "handlers", handlers);

        ops.add(loggerAddress, values);
    }

    public static final class Builder extends LogCategoryCommand.Builder<Builder> {

        private boolean replaceExisting;

        public Builder(String category) {
            super(category);
        }

        public AddLogCategory build() {
            return new AddLogCategory(this);
        }

        /**
         * Set true if replacing of existing node is needed.
         * Default value is false.
         */
        public Builder replaceExisting() {
            replaceExisting = true;
            return this;
        }
    }
}
