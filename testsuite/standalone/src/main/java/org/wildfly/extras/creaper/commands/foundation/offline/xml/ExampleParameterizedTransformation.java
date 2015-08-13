package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

public class ExampleParameterizedTransformation implements OfflineCommand {
    private final String parameter;

    public ExampleParameterizedTransformation(String parameter) {
        this.parameter = parameter;
    }


    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        ctx.client.apply(GroovyXmlTransform.of(ExampleParameterizedTransformation.class)
                .parameter("parameter", parameter).build());
    }
}
