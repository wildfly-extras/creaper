package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

final class GroovyHolder {
    private GroovyHolder() {} // avoid instantiation

    static final GroovyClassLoader GROOVY = createGroovy();

    private static GroovyClassLoader createGroovy() {
        ImportCustomizer importCustomizer = new ImportCustomizer().addStarImports("groovy.xml");

        CompilerConfiguration compilerConfiguration = new CompilerConfiguration()
                .addCompilationCustomizers(importCustomizer);

        return new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), compilerConfiguration);
    }
}
