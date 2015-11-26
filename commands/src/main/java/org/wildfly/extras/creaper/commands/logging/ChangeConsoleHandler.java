package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * @author Ivan Straka istraka@redhat.com
 */

public class ChangeConsoleHandler extends ManipulateConsoleHandler{

    private ChangeConsoleHandler(Builder builder) {
        setBaseProperties(builder);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(ChangeConsoleHandler.class)
                .subtree("logging", Subtree.subsystem("logging"))

                .parameter("name", name)
                .parameter("autoflush", autoflush == null ? null : String.valueOf(autoflush))
                .parameter("enabled", enabled == null ? null : String.valueOf(enabled))
                .parameter("filter", filter)
                .parameter("encoding", encoding.displayName())
                .parameter("target", target == null? null : target.value())
                .parameter("patternFormatter", patternFormatter)
                .parameter("namedFormatter", namedFormatter)
                .parameter("level", level == null ? null : level.value())

                .build();

        ctx.client.apply(transform);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
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
            batch.writeAttribute(handlerAddress, "encoding", encoding.displayName());
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

    public static final class Builder extends ManipulateConsoleHandler.Builder<Builder> {

        public Builder(String name) {
            super(name);
        }

        @Override
        public ChangeConsoleHandler build() {
            validate();
            return new ChangeConsoleHandler(this);
        }
    }
}
