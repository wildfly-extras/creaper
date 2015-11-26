package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class AbstractLoggerCommand implements OnlineCommand, OfflineCommand {
    protected final String category;
    protected final LogLevel level;
    protected final List<String> handlers;
    protected final Boolean useParentHandler;
    protected final String filter;

    protected AbstractLoggerCommand(Builder builder) {
        this.category = builder.category;
        this.level = builder.level;
        this.handlers = builder.handlers;
        this.useParentHandler = builder.useParentHandler;
        this.filter = builder.filter;
    }

    public abstract static class Builder<THIS extends Builder> {
        protected final String category;
        protected LogLevel level;
        protected List<String> handlers;
        protected Boolean useParentHandler;
        protected String filter;

        public Builder(String category) {
            if (category == null || category.equals("")) {
                throw new IllegalArgumentException("category can not be null nor empty string");
            }
            this.category = category;
        }

        public final THIS level(LogLevel level) {
            if (level == null) {
                throw new IllegalArgumentException("level can not be null");
            }
            this.level = level;
            return (THIS) this;
        }

        public final THIS handler(String handler) {
            if (handler == null) {
                throw new IllegalArgumentException("handler can not be null");
            }
            if (this.handlers == null) {
                this.handlers = new ArrayList<String>();
            }
            this.handlers.add(handler);
            return (THIS) this;
        }

        public final THIS handlers(String... handlers) {
            if (handlers == null) {
                throw new IllegalArgumentException("handlers can not be null");
            }
            if (this.handlers == null) {
                this.handlers = new ArrayList<String>();
            }
            this.handlers.addAll(Arrays.asList(handlers));
            return (THIS) this;
        }

        public final THIS useParentHandler(Boolean option) {
            useParentHandler = option;
            return (THIS) this;
        }

        public final THIS filter(String filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter can not be null");
            }
            this.filter = filter;

            return (THIS) this;
        }

        public abstract AbstractLoggerCommand build();
    }
}
