package org.wildfly.extras.creaper.commands.web;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

import java.io.IOException;

/**
 * Command which disables natives in the Web subsystem.
 *
 * <p>{@code OnlineCommand} is not implemented as changing from natives to Java and back doesn't work
 * with the Web subsystem (restart is required).
 * See <a href="https://bugzilla.redhat.com/show_bug.cgi?id=1115443">Red Hat Bugzilla 1115443</a> for details.</p>.
 */
public final class DisableWebNatives implements OfflineCommand {
    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform disableNatives = GroovyXmlTransform
                .of(DisableWebNatives.class)
                .subtree("web", Subtree.subsystem("web"))
                .build();
        ctx.client.apply(disableNatives);
    }
}
