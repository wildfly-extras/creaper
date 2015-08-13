package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

import java.io.IOException;

public class ExampleSubtreePathsAndSystemPropertiesTransformation implements OfflineCommand {
    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        ctx.client.apply(
                new ExampleSubtreePathsTransformation(),
                new ExampleSubtreeSystemPropertiesTransformation()
        );
    }
}
