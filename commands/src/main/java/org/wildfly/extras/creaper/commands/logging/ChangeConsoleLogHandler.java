package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class ChangeConsoleLogHandler extends AbstractConsoleLogHandlerCommand {
    private ChangeConsoleLogHandler(Builder builder) {
        super(builder);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ConsoleTarget.CONSOLE == target) {
            ctx.client.version().assertAtLeast(ServerVersion.VERSION_3_0_0,
                    "ConsoleTarget.CONSOLE is only available since WildFly 9");
        }

        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.subsystem("logging").and("console-handler", name);

        if (!ops.exists(handlerAddress)) {
            throw new IllegalStateException(String.format("console handler %s does not exist.", name));
        }

        Batch batch = new Batch();
        if (autoflush != null) {
            batch.writeAttribute(handlerAddress, "autoflush", autoflush);
        }
        if (enabled != null) {
            batch.writeAttribute(handlerAddress, "enabled", enabled);
        }
        if (filter != null) {
            ops.writeAttribute(handlerAddress, "filter-spec", filter);
        }
        if (encoding != null) {
            batch.writeAttribute(handlerAddress, "encoding", encoding);
        }
        if (target != null) {
            batch.writeAttribute(handlerAddress, "target", target.value());
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

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(ChangeConsoleLogHandler.class)
                .subtree("logging", Subtree.subsystem("logging"))
                .parameter("name", name)
                .parameter("autoflush", autoflush == null ? null : String.valueOf(autoflush))
                .parameter("enabled", enabled == null ? null : String.valueOf(enabled))
                .parameter("filter", filter)
                .parameter("encoding", encoding == null ? null : encoding)
                .parameter("target", target == null ? null : target.value())
                .parameter("patternFormatter", patternFormatter)
                .parameter("namedFormatter", namedFormatter)
                .parameter("level", level == null ? null : level.value())
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "ChangeConsoleLogHandler " + name;
    }

    public static final class Builder extends AbstractConsoleLogHandlerCommand.Builder<Builder> {
        public Builder(String name) {
            super(name);
        }

        @Override
        public ChangeConsoleLogHandler build() {
            validate();
            return new ChangeConsoleLogHandler(this);
        }
    }
}
