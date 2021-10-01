package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommand;

import java.nio.charset.Charset;

abstract class AbstractPeriodicRotatingFileLogHandlerCommand implements OnlineCommand, OfflineCommand {
    protected final String name;
    protected final Boolean autoflush;
    protected final Boolean enabled;
    protected final Boolean append;
    protected final String encoding;
    protected final String filter;
    protected final String patternFormatter;
    protected final String namedFormatter;
    protected final LogLevel level;
    protected final String suffix;
    protected final String fileRelativeTo;
    protected final String file;

    protected AbstractPeriodicRotatingFileLogHandlerCommand(Builder builder) {
        this.name = builder.name;
        this.autoflush = builder.autoflush;
        this.enabled = builder.enabled;
        this.encoding = builder.encoding;
        this.filter = builder.filter;
        this.patternFormatter = builder.patternFormatter;
        this.namedFormatter = builder.namedFormatter;
        this.level = builder.level;
        this.file = builder.file;
        this.fileRelativeTo = builder.fileRelativeTo;
        this.append = builder.append;
        this.suffix = builder.suffix;
    }

    public abstract static class Builder<THIS extends Builder> {
        protected String name;
        protected Boolean autoflush;
        protected Boolean enabled;
        protected Boolean append;
        protected String encoding;
        protected String filter;
        protected String patternFormatter;
        protected String namedFormatter;
        protected LogLevel level;
        protected String suffix;
        protected String fileRelativeTo;
        protected String file;

        Builder(String name, String file, String suffix) {
            if (name == null) {
                throw new IllegalArgumentException("name can not be null.");
            }
            this.name = name;
            this.file = file;
            this.suffix = suffix;
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

        public final THIS append(boolean append) {
            this.append = append;
            return (THIS) this;
        }

        public final THIS encoding(String encoding) {
            if (encoding == null) {
                throw new IllegalArgumentException("Encoding can not be null.");
            }
            this.encoding = encoding;
            return (THIS) this;
        }

        public final THIS encoding(Charset encoding) {
            if (encoding == null) {
                throw new IllegalArgumentException("Encoding can not be null.");
            }
            this.encoding = encoding.name();
            return (THIS) this;
        }

        public final THIS level(LogLevel level) {
            if (level == null) {
                throw new IllegalArgumentException("level can not be null.");
            }
            this.level = level;
            return (THIS) this;
        }

        public final THIS filter(String filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter can not be null.");
            }
            this.filter = filter;
            return (THIS) this;
        }

        public final THIS namedFormatter(String namedFormatter) {
            if (namedFormatter == null) {
                throw new IllegalArgumentException("named formatter can not be null.");
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

        public final THIS fileRelativeTo(String fileRelativeTo) {
            if (fileRelativeTo == null) {
                throw new IllegalArgumentException("file relative to can not be null.");
            }
            this.fileRelativeTo = fileRelativeTo;
            return (THIS) this;
        }

        public abstract AbstractPeriodicRotatingFileLogHandlerCommand build();
    }
}
