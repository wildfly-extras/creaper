package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

import java.util.Collections;

public class OldExampleParameterizedTransformation implements OfflineCommand {
    private final String parameter;

    public OldExampleParameterizedTransformation(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        ctx.client.apply(XmlTransform.groovy(OldExampleParameterizedTransformation.class)
                .withParameters(Collections.singletonMap("parameter", parameter)));
    }
}
