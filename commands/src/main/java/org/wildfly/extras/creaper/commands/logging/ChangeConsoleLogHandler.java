package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

final class ChangeConsoleLogHandler extends AbstractConsoleLogHandlerCommand {

    private ChangeConsoleLogHandler(Builder builder) {
        setBaseProperties(builder);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(ChangeConsoleLogHandler.class)
                .subtree("logging", Subtree.subsystem("logging"))

                .parameter("name", name)
                .parameter("autoflush", autoflush == null ? null : String.valueOf(autoflush))
                .parameter("enabled", enabled == null ? null : String.valueOf(enabled))
                .parameter("filter", filter)
                .parameter("encoding", encoding == null ? null : encoding.displayName())
                .parameter("target", target == null ? null : target.value())
                .parameter("patternFormatter", patternFormatter)
                .parameter("namedFormatter", namedFormatter)
                .parameter("level", level == null ? null : level.value())

                .build();

        ctx.client.apply(transform);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        boolean isSomethingChanged = false;
        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.subsystem("logging").and("console-handler", name);

        if (!ops.exists(handlerAddress)) {
            throw new IllegalStateException(String.format("console handler %s does not exist.", name));
        }

        Batch batch = new Batch();
        if (autoflush != null) {
            isSomethingChanged = true;
            batch.writeAttribute(handlerAddress, "autoflush", autoflush);
        }
        if (enabled != null) {
            isSomethingChanged = true;
            batch.writeAttribute(handlerAddress, "enabled", enabled);
        }
        if (filter != null) {
            isSomethingChanged = true;
            ops.writeAttribute(handlerAddress, "filter-spec", filter);
        }
        if (encoding != null) {
            isSomethingChanged = true;
            batch.writeAttribute(handlerAddress, "encoding", encoding.displayName());
        }
        if (target != null) {
            isSomethingChanged = true;
            batch.writeAttribute(handlerAddress, "target", target.value());
        }
        if (patternFormatter != null) {
            isSomethingChanged = true;
            batch.writeAttribute(handlerAddress, "formatter", patternFormatter);
        }
        if (namedFormatter != null) {
            isSomethingChanged = true;
            batch.writeAttribute(handlerAddress, "named-formatter", namedFormatter);
        }
        if (level != null) {
            isSomethingChanged = true;
            batch.writeAttribute(handlerAddress, "level", level.value());
        }

        if (isSomethingChanged) {
            ops.batch(batch);
        }

    }

    public static final class Builder extends AbstractConsoleLogHandlerCommand.Builder<Builder> {

        public Builder(String name) {
            super(name);
        }

        @Override
        public LogHandlerCommand build() {
            validate();
            return new ChangeConsoleLogHandler(this);
        }
    }
}
