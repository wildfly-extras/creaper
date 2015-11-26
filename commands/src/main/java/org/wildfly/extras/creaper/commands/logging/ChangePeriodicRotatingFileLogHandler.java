package org.wildfly.extras.creaper.commands.logging;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class ChangePeriodicRotatingFileLogHandler extends AbstractPeriodicRotatingFileLogHandlerCommand {
    private ChangePeriodicRotatingFileLogHandler(Builder builder) {
        super(builder);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.subsystem("logging").and("periodic-rotating-file-handler", name);

        if (!ops.exists(handlerAddress)) {
            throw new IllegalStateException(String.format("periodic rotating file handler %s does not exist.", name));
        }

        Batch batch = new Batch();
        if (autoflush != null) {
            batch.writeAttribute(handlerAddress, "autoflush", autoflush);
        }
        if (enabled != null) {
            batch.writeAttribute(handlerAddress, "enabled", enabled);
        }
        if (filter != null) {
            batch.writeAttribute(handlerAddress, "filter-spec", filter);
        }
        if (encoding != null) {
            batch.writeAttribute(handlerAddress, "encoding", encoding);
        }
        if (patternFormatter != null) {
            batch.writeAttribute(handlerAddress, "formatter", patternFormatter);
        }
        if (namedFormatter != null) {
            batch.writeAttribute(handlerAddress, "named-formatter", namedFormatter);
        }
        if (level != null) {
            batch.writeAttribute(handlerAddress, "level", level.value());
        }
        if (append != null) {
            batch.writeAttribute(handlerAddress, "append", append);
        }
        if (suffix != null) {
            batch.writeAttribute(handlerAddress, "suffix", suffix);
        }
        if (file != null || fileRelativeTo != null) {
            ModelNode node = new ModelNode();
            ModelNode oldFileNode = ops.readAttribute(handlerAddress, "file").value();

            if (file != null) {
                node.get("path").set(this.file);
            } else {
                node.get("path").set(oldFileNode.get("path"));
            }

            if (fileRelativeTo != null) {
                node.get("relative-to").set(fileRelativeTo);
            } else if (oldFileNode.hasDefined("relative-to")) {
                node.get("relative-to").set(oldFileNode.get("relative-to"));
            }

            batch.writeAttribute(handlerAddress, "file", node);
        }

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(ChangePeriodicRotatingFileLogHandler.class)
                .subtree("logging", Subtree.subsystem("logging"))
                .parameter("name", name)
                .parameter("autoflush", autoflush == null ? null : String.valueOf(autoflush))
                .parameter("enabled", enabled == null ? null : String.valueOf(enabled))
                .parameter("append", append == null ? null : String.valueOf(append))
                .parameter("filter", filter)
                .parameter("encoding", encoding == null ? null : encoding)
                .parameter("patternFormatter", patternFormatter)
                .parameter("namedFormatter", namedFormatter)
                .parameter("level", level == null ? null : level.value())
                .parameter("filePath", file)
                .parameter("fileRelativeTo", fileRelativeTo)
                .parameter("suffix", suffix)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "ChangePeriodicRotatingFileLogHandler " + name;
    }

    public static final class Builder extends AbstractPeriodicRotatingFileLogHandlerCommand.Builder<Builder> {
        /** {@code file} and {@code suffix} can be {@code null} if they don't need to be changed */
        public Builder(String name, String file, String suffix) {
            super(name, file, suffix);
        }

        @Override
        public ChangePeriodicRotatingFileLogHandler build() {
            validate();
            return new ChangePeriodicRotatingFileLogHandler(this);
        }
    }
}
