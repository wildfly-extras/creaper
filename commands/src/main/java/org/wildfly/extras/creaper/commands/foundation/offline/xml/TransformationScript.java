package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import java.io.InputStream;
import java.net.URL;

final class TransformationScript {
    private final Class resourceLoader;
    private final String path;

    TransformationScript(Class resourceLoader, String path) {
        this.resourceLoader = resourceLoader;
        this.path = path;
    }

    URL url() {
        return resourceLoader.getResource(path);
    }

    InputStream openInputStream() {
        return resourceLoader.getResourceAsStream(path);
    }

    @Override
    public String toString() {
        return path;
    }
}
