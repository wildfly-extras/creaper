package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ChangeRootLogger implements OfflineCommand, OnlineCommand {
    private final LogLevel level;
    private final List<String> handlers;
    private final String filter;

    private ChangeRootLogger(Builder builder) {
        this.level = builder.level;
        this.handlers = builder.handlers;
        this.filter = builder.filter;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address rootAddress = Address.subsystem("logging").and("root-logger", "ROOT");

        Batch batch = new Batch();
        if (handlers != null) {
            if (handlers.isEmpty()) {
                batch.undefineAttribute(rootAddress, "handlers");
            } else {
                batch.writeListAttribute(rootAddress, "handlers", handlers.toArray(new String[handlers.size()]));
            }
        }
        if (level != null) {
            batch.writeAttribute(rootAddress, "level", level.value());
        }
        if (filter != null) {
            batch.writeAttribute(rootAddress, "filter-spec", filter);
        }

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(ChangeRootLogger.class)
                .subtree("logging", Subtree.subsystem("logging"))
                .parameter("handlers", handlers)
                .parameter("filter", filter)
                .parameter("level", level == null ? null : level.value())
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "ChangeRootLogger";
    }

    public static final class Builder {
        private LogLevel level;
        private List<String> handlers;
        private String filter;

        public Builder level(LogLevel level) {
            if (level == null) {
                throw new IllegalArgumentException("level can not be null");
            }
            this.level = level;
            return this;
        }

        public Builder handler(String handler) {
            if (handler == null) {
                throw new IllegalArgumentException("handler can not be null");
            }
            if (this.handlers == null) {
                this.handlers = new ArrayList<String>();
            }
            this.handlers.add(handler);
            return this;
        }

        public Builder handlers(String... handlers) {
            if (handlers == null) {
                throw new IllegalArgumentException("handlers can not be null");
            }
            if (this.handlers == null) {
                this.handlers = new ArrayList<String>();
            }
            this.handlers.addAll(Arrays.asList(handlers));
            return this;
        }

        public Builder filter(String filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter can not be null");
            }
            this.filter = filter;
            return this;
        }

        public ChangeRootLogger build() {
            return new ChangeRootLogger(this);
        }
    }
}
