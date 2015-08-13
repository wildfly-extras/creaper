package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

import java.io.IOException;
import java.util.Map;

/** @deprecated use {@link GroovyXmlTransform} */
public final class XmlTransform implements OfflineCommand {
    private GroovyXmlTransform.Builder builder;

    public static XmlTransform groovy(Class clazz) {
        return new XmlTransform(GroovyXmlTransform.of(clazz).entireFile());
    }

    public XmlTransform(Class resourceLoader, String path) {
        this(GroovyXmlTransform.of(resourceLoader, path).entireFile());
    }

    private XmlTransform(GroovyXmlTransform.Builder builder) {
        this.builder = builder;
    }

    public XmlTransform withParameters(Map<String, ?> parameters) {
        if (parameters.containsKey("file")) {
            throw new IllegalArgumentException("The parameter 'file' is reserved");
        }

        builder.parameters((Map<String, Object>) parameters);
        return this;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        builder.build().apply(ctx);
    }

    @Override
    public String toString() {
        return "XmlTransform (deprecated)";
    }
}
