package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

public class ExampleSubtreeServerGroupsTransformation implements OfflineCommand {
    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        ctx.client.apply(GroovyXmlTransform.of(ExampleSubtreeServerGroupsTransformation.class)
                .subtree("serverGroups", Subtree.serverGroups()).build());
    }
}
