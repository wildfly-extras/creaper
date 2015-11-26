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
 * @author Ivan Straka istraka@redhat.com
 */

public class AddConsoleHandler extends ManipulateConsoleHandler {
    private final boolean replaceExisting;

    private AddConsoleHandler(Builder builder) {
        setBaseProperties(builder);
        replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(AddConsoleHandler.class)
                .subtree("logging", Subtree.subsystem("logging"))

                .parameter("name", name)
                .parameter("autoflush", autoflush == null ? null : String.valueOf(autoflush))
                .parameter("enabled", enabled == null ? null : String.valueOf(enabled))
                .parameter("filter", filter)
                .parameter("encoding", encoding.displayName())
                .parameter("target", target == null ? null : target.value())
                .parameter("patternFormatter", patternFormatter)
                .parameter("namedFormatter", namedFormatter)
                .parameter("level", level == null ? null : level.value())
                .parameter("replaceExisting", replaceExisting)

                .build();

        ctx.client.apply(transform);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.subsystem("logging").and("console-handler", name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(handlerAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing console-handler " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("autoflush", autoflush)
                .andOptional("enabled", enabled)
                .andOptional("level", level == null ? null : level.value())
                .andOptional("filter-spec", filter)
                .andOptional("encoding", encoding.displayName())
                .andOptional("target", target == null ? null : target.value())
                .andOptional("named-formatter", namedFormatter)
                .andOptional("formatter", patternFormatter);


        Batch batch = new Batch();
        batch.add(handlerAddress, values);

        ops.batch(batch);
    }

    public static final class Builder extends ManipulateConsoleHandler.Builder<Builder> {

        private boolean replaceExisting = false;

        public Builder(String name) {
            super(name);
        }

        @Override
        public AddConsoleHandler build() {
            validate();
            return new AddConsoleHandler(this);
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
