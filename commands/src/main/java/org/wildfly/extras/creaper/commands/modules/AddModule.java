package org.wildfly.extras.creaper.commands.modules;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Adds a module (in the JBoss Modules sense) to the module repository. The necessary directory structure
 * will be created if needed. The JAR files specified as {@code resources} will be copied to the module directory.
 * Unless a {@code module.xml} file was specified as an argument, it will be generated automatically.</p>
 *
 * <p>Example of resulting cli command:</p>
 *
 * <pre>
 * module add --name=name --slot=slot --resources=jars
 *            --resource-delimiter=char --dependencies=modules --properties=properties</p>
 * </pre>
 */
public class AddModule implements OnlineCommand {
    public static final String DEFAULT_SLOT = "main";

    private final char resourceDelimiter;
    private final String moduleName;
    private final String slot;
    private final String mainClass;
    private final String moduleRootDir;
    private final String moduleXml;
    private final List<String> resources;
    private final List<String> dependencies;
    private final List<String> properties;

    private AddModule(Builder builder) {
        this.resourceDelimiter = builder.resourceDelimiter;
        this.moduleName = builder.moduleName;
        this.slot = builder.slot;
        this.mainClass = builder.mainClass;
        this.moduleRootDir = builder.moduleRootDir;
        this.moduleXml = builder.moduleXml;
        this.resources = builder.resources;
        this.dependencies = builder.dependencies;
        this.properties = builder.properties;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.options.isDomain) {
            throw new CommandFailedException("AddModule command isn't supported in domain mode");
        }

        StringBuilder cmd = new StringBuilder("module add");
        cmd.append(" --name=").append(moduleName);
        cmd.append(" --slot=").append(slot);

        if (moduleRootDir != null) {
            cmd.append(" --module-root-dir=").append(moduleRootDir);
        }

        if (moduleXml != null) {
            cmd.append(" --module-xml=").append(moduleXml);
        }

        if (mainClass != null) {
            cmd.append(" --main-class=").append(mainClass);
        }

        Joiner resourcesJoiner = Joiner.on(File.pathSeparatorChar);
        // resource-delimiter was added in WF 8, WFLY-1871
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_2_0_0)) {
            cmd.append(" --resource-delimiter=").append(resourceDelimiter);
            resourcesJoiner = Joiner.on(resourceDelimiter);
        }

        if (!resources.isEmpty()) {
            cmd.append(" --resources=").append(resourcesJoiner.join(resources).replaceAll(" ", "\\\\ "));
        }

        Joiner defaultJoiner = Joiner.on(",");
        if (!dependencies.isEmpty()) {
            cmd.append(" --dependencies=").append(defaultJoiner.join(dependencies));
        }

        if (!properties.isEmpty()) {
            cmd.append(" --properties=").append(defaultJoiner.join(properties));
        }

        ctx.client.executeCli(cmd.toString());

        if (moduleXml == null && mainClass != null) {
            // module.xml was generated automatically, it might contain an error in the main-class element
            fixMainClassInModuleXml(ctx.client);
        }
    }

    // this is a workaround for WFCORE-1082
    private void fixMainClassInModuleXml(OnlineManagementClient client) throws CliException, IOException {
        ModelNodeResult jbossHomeResult = client.execute(":resolve-expression(expression=${jboss.home.dir})");
        if (jbossHomeResult.hasDefinedValue()) {
            String jbossHome = jbossHomeResult.stringValue();
            File modulesDir = new File(jbossHome, "modules");
            File targetModuleDir = new File(modulesDir, moduleName.replace('.', File.separatorChar)
                    + File.separatorChar + (slot == null ? "main" : slot));
            File moduleXml = new File(targetModuleDir, "module.xml");

            // it's critical that these two arrays are of the same length!
            // the trick used here to achieve that is to insert an extra space into the second one
            byte[] badBytes = "<main-class value=".getBytes(Charsets.US_ASCII);
            byte[] goodBytes = "<main-class  name=".getBytes(Charsets.US_ASCII);

            byte[] moduleXmlContent = Files.toByteArray(moduleXml);
            int indexOfBadSequence = Bytes.indexOf(moduleXmlContent, badBytes);
            if (indexOfBadSequence >= 0) {
                System.arraycopy(goodBytes, 0, moduleXmlContent, indexOfBadSequence, goodBytes.length);
            }

            Files.write(moduleXmlContent, moduleXml);
        }
    }

    @Override
    public String toString() {
        return "AddModule " + moduleName;
    }

    public static final class Builder {
        private final String moduleName;
        private final String slot;
        private char resourceDelimiter = File.pathSeparatorChar;
        private String moduleXml;
        private String moduleRootDir;
        private String mainClass;
        private final List<String> resources;
        private final List<String> dependencies;
        private final List<String> properties;

        /**
         * @param moduleName the name of the module to be added (assumes the {@code main} slot)
         */
        public Builder(String moduleName) {
            this(moduleName, DEFAULT_SLOT);
        }

        /**
         * @param moduleName the name of the module to be added
         * @param slot       specifies a slot which should be created
         */
        public Builder(String moduleName, String slot) {
            if (moduleName == null) {
                throw new IllegalArgumentException("moduleName cannot be null");
            }
            if (slot == null) {
                throw new IllegalArgumentException("slot cannot be null");
            }

            this.moduleName = moduleName;
            this.slot = slot;
            this.resources = new ArrayList<String>();
            this.dependencies = new ArrayList<String>();
            this.properties = new ArrayList<String>();
        }

        /**
         * Adds resource (jar) to module, specified file will be copied to new module.
         */
        public Builder resource(File file) {
            if (file == null) {
                throw new IllegalArgumentException("resource file cannot be null");
            }
            resources.add(file.getAbsolutePath());
            return this;
        }

        /**
         * Sets resource delimiter. If not set, {@link File#separatorChar} is used. Only {@code String}s with length 1
         * are allowed. Whitespaces are not allowed. This option is ignored if server version is less then WildFly 8.
         */
        public Builder resourceDelimiter(String delimiter) {
            if (delimiter == null) {
                throw new IllegalArgumentException("resource delimiter cannot be null");
            }
            if (delimiter.length() != 1) {
                throw new IllegalArgumentException("resource delimiter lenght was "
                        + delimiter.length() + ", but only strings of length 1 are allowed");
            }

            char delimiterChar = delimiter.charAt(0);
            if (Character.isWhitespace(delimiterChar)) {
                throw new IllegalArgumentException("resource delimiter cannot be whitespace");
            }
            this.resourceDelimiter = delimiterChar;
            return this;
        }

        /**
         * Name of a module that the new module depends on. NOTE: this argument only makes sense when
         * the {@code module.xml} file is generated, i.e. when the {@link #moduleXml(File)} isn't specified.
         */
        public Builder dependency(String dependency) {
            if (dependency == null) {
                throw new IllegalArgumentException("dependency name cannot be null");
            }
            dependencies.add(dependency);
            return this;
        }

        /**
         * Use this argument if you have defined an external JBoss EAP module directory to use instead of the default
         * EAP_HOME/modules/ directory.
         *
         * @see <a href="https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.3/html-single/configuration_guide/index#idm140645108782400">
         * EAP Configuration guide
         * </a>
         */
        public Builder moduleRootDir(File moduleRootDir) {
            if (moduleRootDir == null) {
                throw new IllegalArgumentException("moduleRootDir file cannot be null");
            }
            this.moduleRootDir = moduleRootDir.getAbsolutePath();
            return this;
        }

        /**
         * The {@code module.xml} file which should be used for the added module. The file will be copied
         * to the created module's directory. If this argument is not specified, {@code module.xml} file
         * will be generated automatically.
         */
        public Builder moduleXml(File moduleXml) {
            if (moduleXml == null) {
                throw new IllegalArgumentException("moduleXml file cannot be null");
            }
            this.moduleXml = moduleXml.getAbsolutePath();
            return this;
        }

        /**
         * Adds a property. NOTE: this argument only makes sense when the {@code module.xml} file is generated,
         * i.e. when the {@link #moduleXml(File)} isn't specified.
         */
        public Builder property(String name, String value) {
            if (name == null) {
                throw new NullPointerException("property name cannot be null");
            }
            if (value == null) {
                throw new NullPointerException("property value cannot be null");
            }
            properties.add(name + "=" + value);
            return this;
        }

        /**
         * A fully qualified class name that declares the module's {@code main} method. NOTE: this argument
         * only makes sense when the {@code module.xml} file is generated, i.e. when the {@link #moduleXml(File)}
         * isn't specified.
         */
        public Builder mainClass(String mainClass) {
            if (mainClass == null) {
                throw new NullPointerException("mainClass cannot be null");
            }
            this.mainClass = mainClass;
            return this;
        }

        public AddModule build() {
            return new AddModule(this);
        }
    }
}
