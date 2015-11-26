package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommand;

import java.nio.charset.Charset;

abstract class AbstractConsoleLogHandlerCommand implements OnlineCommand, OfflineCommand {
    protected final String name;
    protected final Boolean autoflush;
    protected final Boolean enabled;
    protected final String encoding;
    protected final String filter;
    protected final String patternFormatter;
    protected final String namedFormatter;
    protected final LogLevel level;
    protected final ConsoleTarget target;

    protected AbstractConsoleLogHandlerCommand(Builder builder) {
        this.name = builder.name;
        this.autoflush = builder.autoflush;
        this.enabled = builder.enabled;
        this.encoding = builder.encoding;
        this.filter = builder.filter;
        this.patternFormatter = builder.patternFormatter;
        this.namedFormatter = builder.namedFormatter;
        this.level = builder.level;
        this.target = builder.target;
    }

    public abstract static class Builder<THIS extends Builder> {
        protected String name;
        protected Boolean autoflush;
        protected Boolean enabled;
        protected String encoding;
        protected String filter;
        protected String patternFormatter;
        protected String namedFormatter;
        protected LogLevel level;
        protected ConsoleTarget target;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name can not be null.");
            }
            this.name = name;
        }

        protected void validate() {
            if (patternFormatter != null && namedFormatter != null) {
                throw new IllegalStateException("You can not define both pattern formater and named formatter.");
            }
        }

        public final THIS autoFlush(boolean autoflush) {
            this.autoflush = autoflush;
            return (THIS) this;
        }

        public final THIS enabled(boolean enabled) {
            this.enabled = enabled;
            return (THIS) this;
        }

        public final THIS encoding(Charset encoding) {
            if (encoding == null) {
                throw new IllegalArgumentException("Encoding can not be null.");
            }
            this.encoding = encoding.name();
            return (THIS) this;
        }

        public final THIS encoding(String encoding) {
            if (encoding == null) {
                throw new IllegalArgumentException("Encoding can not be null.");
            }
            this.encoding = encoding;
            return (THIS) this;
        }

        public final THIS target(ConsoleTarget target) {
            if (target == null) {
                throw new IllegalArgumentException("Target can not be null.");
            }
            this.target = target;
            return (THIS) this;
        }

        public final THIS level(LogLevel level) {
            if (level == null) {
                throw new IllegalArgumentException("Level can not be null.");
            }
            this.level = level;
            return (THIS) this;
        }

        public final THIS filter(String filter) {
            if (filter == null) {
                throw new IllegalArgumentException("Filter can not be null.");
            }
            this.filter = filter;
            return (THIS) this;
        }

        public final THIS namedFormatter(String namedFormatter) {
            if (namedFormatter == null) {
                throw new IllegalArgumentException("Named formatter can not be null.");
            }
            this.namedFormatter = namedFormatter;
            return (THIS) this;
        }

        public final THIS patternFormatter(String patternFormatter) {
            if (patternFormatter == null) {
                throw new IllegalArgumentException("pattern formatter can not be null.");
            }
            this.patternFormatter = patternFormatter;
            return (THIS) this;
        }

        public abstract AbstractConsoleLogHandlerCommand build();
    }
}
