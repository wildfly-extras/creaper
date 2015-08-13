package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

public class ExampleSubtreeSubsystemInProfileTransformation implements OfflineCommand {
    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        ctx.client.apply(GroovyXmlTransform.of(ExampleSubtreeSubsystemInProfileTransformation.class, "ExampleSubtreeSubsystemTransformation.groovy")
                .subtree("datasources", Subtree.subsystemInProfile("foobar", "datasources"))
                .subtree("ee", Subtree.subsystemInProfile("foobar", "ee"))
                .subtree("infinispan", Subtree.subsystemInProfile("foobar", "infinispan"))
                .build());
    }
}
