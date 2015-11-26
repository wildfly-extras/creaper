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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ivan Straka istraka@redhat.com
 */

public class ChangeRootLogger implements OfflineCommand, OnlineCommand {

    private Level level;
    private List<String> handlers = null;
    private String filter;

    private ChangeRootLogger(Builder builder) {
        level = builder.level;
        handlers = builder.handlers;
        filter = builder.filter;
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
    public void apply(OnlineCommandContext ctx) throws Exception {

        Operations ops = new Operations(ctx.client);
        Address rootAddress = Address.subsystem("logging").and("root-logger", "ROOT");

        Batch batch = new Batch();
        if (handlers != null) {
            if (handlers.isEmpty()) {
                batch.undefineAttribute(rootAddress, "handlers");
            } else {
                batch.writeListAttribute(rootAddress, "handlers", handlers.toArray(new String[]{}));
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

    public static final class Builder<THIS extends Builder> {

        private Level level = null;
        private List<String> handlers = null;
        private String filter = null;

        public THIS changeLevel(final Level level) {
            if (level == null) {
                throw new IllegalArgumentException("level can not be null");
            }
            this.level = level;
            return (THIS) this;
        }

        public THIS changeHandler(final String handler) {
            if (handler == null) {
                throw new IllegalArgumentException("handler can not be null");
            }
            if (this.handlers == null) {
                initHandlers();
            }
            this.handlers.add(handler);
            return (THIS) this;
        }

        public THIS changeHandlers(final String... handlers) {
            if (handlers == null) {
                throw new IllegalArgumentException("handlers can not be null");
            }
            if (this.handlers == null) {
                initHandlers();
            }
            this.handlers.addAll(Arrays.asList(handlers));
            return (THIS) this;
        }

        private void initHandlers() {
            this.handlers = new LinkedList<String>();
        }

        /**
         * For example match("a*").
         *
         * @param filter
         * @return
         */
        public THIS changeFilter(final String filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter can not be null");
            }
            this.filter = filter;

            return (THIS) this;
        }

        public ChangeRootLogger build() {
            return new ChangeRootLogger(this);
        }


    }
}
