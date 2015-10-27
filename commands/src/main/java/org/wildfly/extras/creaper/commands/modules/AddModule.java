package org.wildfly.extras.creaper.commands.modules;

import com.google.common.base.Joiner;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>The command is used to add modules</p>
 *
 * <p>The corresponding to the module name directory structure will be created in the AS module repository.
 * The JAR files specified as resources will be copied to the module's directory. Unless module.xml file
 * was specified as an argument, it will be automatically generated.</p>
 *
 * <p>Example of resulting cli command:
 * module add --name=name --slot=slot --resources=jars
 * --resource-delimiter=char --dependencies=modules --properties=properties</p>
 */
public class AddModule implements OnlineCommand {
    public static final String DEFAULT_SLOT = "main";
    private final char resourceDelimiter;
    private final String moduleName;
    private final String slot;
    private final String mainClass;
    private final String moduleXml;
    private final List<String> resources;
    private final List<String> dependencies;
    private final List<String> properties;

    private AddModule(Builder builder) {
        this.resourceDelimiter = builder.resourceDelimiter;
        this.moduleName = builder.moduleName;
        this.slot = builder.slot;
        this.mainClass = builder.mainClass;
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
        cmd.append(" --name=" + moduleName);
        cmd.append(" --slot=" + slot);

        if (moduleXml != null)
            cmd.append(" --module-xml=" + moduleXml);

        if (mainClass != null)
            cmd.append(" --main-class=" + mainClass);

        Joiner resourcesJoiner = Joiner.on(File.pathSeparatorChar);
        // resource-delimiter was added in WF 8, WFLY-1871
        if (ctx.serverVersion.greaterThanOrEqualTo(ManagementVersion.VERSION_2_0_0)) {
            cmd.append(" --resource-delimiter=" + resourceDelimiter);
            resourcesJoiner = Joiner.on(resourceDelimiter);
        }

        if (!resources.isEmpty()) {
            cmd.append(" --resources=" + resourcesJoiner.join(resources).replaceAll(" ", "\\\\ "));
        }

        Joiner defaultJoiner = Joiner.on(",");
        if (!dependencies.isEmpty()) {
            cmd.append(" --dependencies=" + defaultJoiner.join(dependencies));
        }

        if (!properties.isEmpty()) {
            cmd.append(" --properties=" + defaultJoiner.join(properties));
        }
        ctx.client.executeCli(cmd.toString());
    }

    @Override
    public String toString() {
        return "AddModule " + moduleName;
    }

    public static final class Builder {
        private char resourceDelimiter = File.pathSeparatorChar;
        private final String moduleName;
        private final String slot;
        private String moduleXml;
        private String mainClass;
        private List<String> resources;
        private List<String> dependencies;
        private List<String> properties;

        /**
         * @param moduleName the name of the module to be added
         */
        public Builder(String moduleName) {
            this(moduleName, DEFAULT_SLOT);
        }

        /**
         * @param moduleName the name of the module to be added
         * @param slot       specifies a slot which should be created. If this argument is not specified, "main" slot
         *                   is assumed
         */
        public Builder(String moduleName, String slot) {
            if (moduleName == null)
                throw new NullPointerException("moduleName cannot be null");
            if (slot == null)
                throw new NullPointerException("slot cannot be null");
            this.moduleName = moduleName;
            this.slot = slot;
            this.resources = new ArrayList<String>();
            this.dependencies = new ArrayList<String>();
            this.properties = new ArrayList<String>();
        }

        /**
         * Add resource (jar) to module, specified file will be copied to new module
         */
        public Builder resource(File file) {
            if (file == null)
                throw new NullPointerException("resource file cannot be null");
            resources.add(file.getAbsolutePath());
            return this;
        }

        /**
         * sets resource delimiter, if not set {@link File#separatorChar} is used
         * allowed is only String with length 1, whitespaces aren't allowed
         * This option is ignored if server version is less then WildFly 8 or EAP7
         */
        public Builder resourceDelimiter(String delimiter) {
            if (delimiter == null) {
                throw new NullPointerException("resource delimiter cannot be null");
            }
            if (delimiter.length() != 1)
                throw new IllegalArgumentException("resource delimiter lenght was "
                        + delimiter.length() + ", allowed is 1");
            char delimiterChar = delimiter.charAt(0);
            if (Character.isWhitespace(delimiterChar))
                throw new IllegalArgumentException("resource delimiter cannot be whitespace");
            this.resourceDelimiter = delimiterChar;
            return this;
        }

        /**
         * module name module being added depends on. NOTE: this argument makes sense only when the module.xml
         * file is generated, i.e. when the {@link #moduleXml(File)} isn't specified
         */
        public Builder dependency(String dependency) {
            if (dependency == null)
                throw new NullPointerException("dependency name cannot be null");
            dependencies.add(dependency);
            return this;
        }

        /**
         * module.xml file which should be used for the added module. The file will be copied to the created
         * module's directory. If this argument is not specified, module.xml file will be generated in the
         * new created module's directory.
         */
        public Builder moduleXml(File moduleXml) {
            if (moduleXml == null)
                throw new NullPointerException("moduleXml file cannot be null");
            this.moduleXml = moduleXml.getAbsolutePath();
            return this;
        }

        /**
         * add property. NOTE: this argument makes sense only when the module.xml
         * file is generated, i.e. when the {@link #moduleXml(File)} isn't specified
         */
        public Builder property(String name, String value) {
            if (name == null)
                throw new NullPointerException("property name cannot be null");
            if (value == null)
                throw new NullPointerException("property value cannot be null");
            properties.add(name + "=" + value);
            return this;
        }

        /**
         * a fully qualified class name that declares the modules main method.
         * NOTE: this argument makes sense only when the module.xml
         * file is generated, i.e. when the {@link #moduleXml(File)} isn't specified
         */
        public Builder mainClass(String mainClass) {
            if (mainClass == null)
                throw new NullPointerException("mainClass cannot be null");
            this.mainClass = mainClass;
            return this;
        }

        public AddModule build() {
            return new AddModule(this);
        }
    }
}
