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

public final class AddPeriodicRotatingFileHandler extends AbstractPeriodicHandler {
    private final boolean replaceExisting;

    private AddPeriodicRotatingFileHandler(Builder builder) {
        setBaseProperties(builder);
        replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(AddPeriodicRotatingFileHandler.class)
                .subtree("logging", Subtree.subsystem("logging"))

                .parameter("name", name)
                .parameter("autoflush", autoflush == null ? null : String.valueOf(autoflush))
                .parameter("enabled", enabled == null ? null : String.valueOf(enabled))
                .parameter("append", append == null ? null : String.valueOf(append))
                .parameter("filter", filter)
                .parameter("encoding", encoding.displayName())
                .parameter("patternFormatter", patternFormatter)
                .parameter("namedFormatter", namedFormatter)
                .parameter("level", level == null ? null : level.value())
                .parameter("filePath", file)
                .parameter("fileRelativeTo", fileRelativeTo)
                .parameter("suffix", suffix)
                .parameter("replaceExisting", replaceExisting)

                .build();

        ctx.client.apply(transform);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.subsystem("logging").and("periodic-rotating-file-handler", name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(handlerAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing periodic-rotating-file-handler " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("autoflush", autoflush)
                .andOptional("enabled", enabled)
                .andOptional("level", level == null ? null : level.value())
                .andOptional("filter-spec", filter)
                .andOptional("encoding", encoding.displayName())
                .andOptional("named-formatter", namedFormatter)
                .andOptional("formatter", patternFormatter)
                .andOptional("suffix", suffix)
                .andOptional("append", append)
                .andObjectOptional("file",
                        Values.empty()
                                .andOptional("path", file)
                                .andOptional("relative-to", fileRelativeTo));

        ops.add(handlerAddress, values);
    }

    public static final class Builder extends AbstractPeriodicHandler.Builder<Builder> {

        private boolean replaceExisting;

        public Builder(final String name, final String file, final String suffix) {
            super(name, file, suffix);
        }

        @Override
        public AddPeriodicRotatingFileHandler build() {
            validate();
            return new AddPeriodicRotatingFileHandler(this);
        }

        @Override
        public void validate() {
            if (file == null) {
                throw new IllegalArgumentException("file can not be null!");
            }
            super.validate();
        }

        /**
         * Set true if replacing of existing node is needed.
         * Default value is false.
         */
        public Builder setReplaceExisting(boolean value) {
            replaceExisting = value;
            return this;
        }
    }
}
