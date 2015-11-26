package org.wildfly.extras.creaper.commands.logging;


import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class ChangeLogger extends AbstractLoggerCommand {
    private ChangeLogger(Builder builder) {
        super(builder);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address loggerAddress = Address.subsystem("logging").and("logger", category);

        if (!ops.exists(loggerAddress)) {
            throw new IllegalStateException("Log category %s does not exist: " + category);
        }

        Batch batch = new Batch();
        if (filter != null) {
            batch.writeAttribute(loggerAddress, "filter-spec", filter);
        }
        if (level != null) {
            batch.writeAttribute(loggerAddress, "level", level.value());
        }
        if (useParentHandler != null) {
            batch.writeAttribute(loggerAddress, "use-parent-handlers", useParentHandler);
        }
        if (level != null) {
            batch.writeAttribute(loggerAddress, "level", level.value());
        }
        if (handlers != null) {
            if (handlers.isEmpty()) {
                batch.undefineAttribute(loggerAddress, "handlers");
            } else {
                batch.writeListAttribute(loggerAddress, "handlers", handlers.toArray(new String[handlers.size()]));
            }
        }

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(ChangeLogger.class)
                .subtree("logging", Subtree.subsystem("logging"))
                .parameter("category", category)
                .parameter("handlers", handlers)
                .parameter("filter", filter)
                .parameter("level", level == null ? null : level.value())
                .parameter("useParentHandler", useParentHandler == null ? null : String.valueOf(useParentHandler))
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "ChangeLogger " + category;
    }

    public static final class Builder extends AbstractLoggerCommand.Builder<Builder> {
        public Builder(String category) {
            super(category);
        }

        public ChangeLogger build() {
            return new ChangeLogger(this);
        }
    }
}
