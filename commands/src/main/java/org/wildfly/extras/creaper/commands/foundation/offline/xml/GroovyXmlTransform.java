package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.XmlUtil;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An offline command that runs a XML transformation on the configuration file which the offline management client
 * is connected to. The transformation is defined by a Groovy script loaded from classpath.
 */
public final class GroovyXmlTransform implements OfflineCommand {
    private final TransformationScript script;
    private final Map<String, Subtree> subtrees;
    private final Map<String, Object> parameters;
    private final boolean entireFile;

    /**
     * A convenience shortcut for a case when the script is stored alongside the {@code clazz} on the classpath,
     * has the same name and the {@code .groovy} extension.
     *
     * @param clazz the class that will be used for loading the script and also for discovering its name
     *
     * @see #of(Class, String)
     */
    public static Builder of(Class clazz) {
        return of(clazz, clazz.getSimpleName() + ".groovy");
    }

    /**
     * Creates a {@code GroovyXmlTransform} from a script loaded from classpath at {@code path}. The resource
     * will be loaded by the {@code resourceLoader} class ({@link Class#getResourceAsStream(String)}).
     * @param resourceLoader class that will be used to load the script from classpath
     * @param path path to the script on classpath (absolute or relative to {@code resourceLoader})
     */
    public static Builder of(Class resourceLoader, String path) {
        if (resourceLoader == null) {
            throw new IllegalArgumentException("A class for loading the script must be provided");
        }
        if (path == null) {
            throw new IllegalArgumentException("A path to the script must be provided");
        }

        return new Builder(new TransformationScript(resourceLoader, path));
    }

    private GroovyXmlTransform(TransformationScript script, Map<String, Subtree> subtrees,
                               Map<String, Object> parameters, boolean entireFile) {
        if (entireFile && !subtrees.isEmpty()) {
            throw new IllegalArgumentException("The 'entireFile' mode is only possible without subtrees");
        }
        if (entireFile && parameters.containsKey("file")) {
            throw new IllegalArgumentException("Parameter 'file' is reserved in 'entireFile' mode");
        }
        if (!entireFile && subtrees.isEmpty() && parameters.containsKey("root")) {
            throw new IllegalArgumentException("Parameter 'root' is reserved when no subtree is specified");
        }

        Set<String> keysIntersection = new HashSet<String>(subtrees.keySet());
        keysIntersection.retainAll(parameters.keySet());
        if (!keysIntersection.isEmpty()) {
            throw new IllegalArgumentException("Collision between subtrees and parameters: " + keysIntersection);
        }

        this.script = script;
        this.subtrees = subtrees;
        this.parameters = parameters;
        this.entireFile = entireFile;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        URL url = script.url();
        if (url == null) {
            throw new CommandFailedException("Couldn't load " + script);
        }

        GroovyCodeSource codeSource;
        try {
            codeSource = new GroovyCodeSource(url);
        } catch (IOException e) {
            throw new CommandFailedException(e);
        }

        Class clazz = GroovyHolder.GROOVY.parseClass(codeSource);
        if (!Script.class.isAssignableFrom(clazz)) {
            throw new CommandFailedException("Not a valid Groovy script: " + script);
        }

        Script loadedScript;
        try {
            loadedScript = (Script) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new CommandFailedException(e);
        } catch (IllegalAccessException e) {
            throw new CommandFailedException(e);
        }

        for (Map.Entry<String, ?> parameter : parameters.entrySet()) {
            loadedScript.setProperty(parameter.getKey(), parameter.getValue());
        }

        if (entireFile) {
            loadedScript.setProperty("file", ctx.configurationFile);
            loadedScript.run();
        } else {
            try {
                GPathResult root = new XmlSlurper(false, false).parse(ctx.configurationFile);
                if (subtrees.isEmpty()) {
                    loadedScript.setProperty("root", root);
                } else {
                    for (Subtree subtree : subtrees.values()) {
                        subtree.addIfMissing(root, ctx.options);
                    }
                    // two things happening here:
                    // 1. nodes might have been added, but to a wrong place (fixing it post facto is easier than adding
                    //    them to the correct location in the first place)
                    // 2. XmlSlurper can't see the changes it made, so we need to serialize and reparse here
                    String fixedXml = FirstLevelXmlElementOrder.fix(XmlUtil.serialize(root));
                    root = new XmlSlurper(false, false).parseText(fixedXml);

                    for (Map.Entry<String, Subtree> subtree : subtrees.entrySet()) {
                        loadedScript.setProperty(subtree.getKey(), subtree.getValue().locate(root, ctx.options));
                    }
                }

                loadedScript.run();

                Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(ctx.configurationFile), "utf-8"));
                XmlUtil.serialize(root, writer);
                writer.close();
            } catch (Exception e) {
                throw new CommandFailedException(e);
            }
        }
    }

    @Override
    public String toString() {
        return "GroovyXmlTransform " + script.toString();
    }

    // ---

    public static final class Builder {
        private final TransformationScript script;
        private final Map<String, Subtree> subtrees = new HashMap<String, Subtree>();
        private final Map<String, Object> parameters = new HashMap<String, Object>();
        private boolean entireFile = false;

        private Builder(TransformationScript script) {
            this.script = script;
        }

        /**
         * The transformation script will have a variable named after {@code name} with a value corresponding to
         * a specific {@code subtree} of the configuration file. If this method is never called, the script will have
         * a variable named {@code root} with a value corresponding to the root node of the configuration file
         * (that is, never calling {@code subtree} is equivalent to a single call
         * {@code .subtree("root", Subtree.root())}).
         *
         * @see Subtree
         */
        public Builder subtree(String name, Subtree subtree) {
            subtrees.put(name, subtree);
            return this;
        }

        /**
         * The transformation script will have a variable named after {@code name} with a value of {@code value}.
         * If multiple parameters with the same name are set, the last one wins. If there is a collision with
         * a name of a {@link #subtree(String, Subtree) subtree}, an {@link IllegalArgumentException} will be thrown
         * from {@link #build()}.
         */
        public Builder parameter(String name, Object value) {
            parameters.put(name, value);
            return this;
        }

        /** Same as {@link #parameter(String, Object)} invoked for all entries of the {@code parameters} map. */
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * <p>The transformation script will have a variable {@code file} containing the {@link java.io.File File}
         * of the transformed configuration file. The script is also responsible for both reading the file and writing
         * it back. This is mostly useful for backward compatibility and special purposes. <b>Most often, you don't
         * want this!</b></p>
         *
         * <p>This is <b>not</b> compatible with using {@link #subtree(String, Subtree) subtrees} and trying to use
         * both together will lead to an {@link IllegalArgumentException} from {@link #build()}. On the other hand,
         * using {@link #parameter(String, Object) parameters} is entirely possible.</p>
         */
        Builder entireFile() {
            this.entireFile = true;
            return this;
        }

        public GroovyXmlTransform build() {
            return new GroovyXmlTransform(script, subtrees, parameters, entireFile);
        }
    }
}
