package org.wildfly.extras.creaper.commands.logging;


import java.nio.charset.Charset;

abstract class AbstractConsoleLogHandlerCommand extends LogHandlerCommand {

    protected String name;
    protected Boolean autoflush;
    protected Boolean enabled;
    protected Charset encoding;
    protected String filter;
    protected String patternFormatter;
    protected String namedFormatter;
    protected Level level;
    protected Target target;

    protected void setBaseProperties(Builder builder) {
        name = builder.name;
        autoflush = builder.autoflush;
        enabled = builder.enabled;
        encoding = builder.encoding;
        filter = builder.filter;
        patternFormatter = builder.patternFormatter;
        namedFormatter = builder.namedFormatter;
        level = builder.level;
        target = builder.target;
    }

    public abstract static class Builder<THIS extends Builder> {
        protected String name;
        protected Boolean autoflush;
        protected Boolean enabled;
        protected Charset encoding;
        protected String filter;
        protected String patternFormatter;
        protected String namedFormatter;
        protected Level level;
        protected Target target;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name can not be null.");
            }
            this.name = name;
        }

        protected void validate() {
            if (patternFormatter != null && namedFormatter != null) {
                throw new IllegalStateException("You can not define both pattern formater and named formatter.");
            }
        }

        public final THIS setAutoFlush(boolean val) {
            autoflush = val;
            return (THIS) this;
        }

        public final THIS setEnabled(boolean val) {
            enabled = val;
            return (THIS) this;
        }

        public final THIS encoding(Charset encoding) {
            if (encoding == null) {
                throw new IllegalArgumentException("Encoding can not be null.");
            }
            this.encoding = encoding;
            return (THIS) this;
        }

        public final THIS target(Target target) {
            if (target == null) {
                throw new IllegalArgumentException("target can not be null.");
            }
            this.target = target;
            return (THIS) this;
        }

        public final THIS level(Level level) {
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

        public abstract LogHandlerCommand build();

    }
}
