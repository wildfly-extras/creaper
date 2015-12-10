package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommand;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Class encapsulate commands for configuring loggers in logging subsystem.
 * Class also provides static methods for obtaining factories that provides methods for configuring handler of specific
 * type. E.G. LogHandlerCommand.console().add("handler").build()
 */
public abstract class LogCategoryCommand implements OnlineCommand, OfflineCommand {

    protected String category;
    protected Level level;
    protected List<String> handlers = null;
    protected Boolean useParentHandler;
    protected String filter;

    /**
     * @return builder for command that add logger category of given category
     */
    public static AddLogCategory.Builder add(String category) {
        return new AddLogCategory.Builder(category);
    }

    /**
     * @return builder for command that change logger category of given category
     */
    public static ChangeLogCategory.Builder change(String category) {
        return new ChangeLogCategory.Builder(category);
    }

    /**
     * @return command that remove logger category of given name
     */
    public static RemoveLogCategory remove(String category) {
        return new RemoveLogCategory(category);
    }

    protected final void setBaseProperties(Builder builder) {
        category = builder.category;
        level = builder.level;
        handlers = builder.handlers;
        useParentHandler = builder.useParentHandler;
        filter = builder.filter;
    }

    public abstract static class Builder<THIS extends Builder> {

        protected final String category;
        protected Level level = null;
        protected List<String> handlers = null;
        protected Boolean useParentHandler;
        protected String filter = null;

        public Builder(String category) {
            if (category == null || category.equals("")) {
                throw new IllegalArgumentException("category can not be null, nor empty string");
            }
            this.category = category;
        }

        public final THIS level(Level level) {
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
                initHandlers();
            }
            this.handlers.add(handler);
            return (THIS) this;
        }

        public final THIS handlers(String... handlers) {
            if (handlers == null) {
                throw new IllegalArgumentException("handlers can not be null");
            }
            if (this.handlers == null) {
                initHandlers();
            }
            this.handlers.addAll(Arrays.asList(handlers));
            return (THIS) this;
        }

        public final THIS setUseParentHandler(Boolean option) {
            useParentHandler = option;
            return (THIS) this;
        }

        private void initHandlers() {
            this.handlers = new LinkedList<String>();
        }

        public final THIS filter(String filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter can not be null");
            }
            this.filter = filter;

            return (THIS) this;
        }

        public abstract LogCategoryCommand build();
    }
}
