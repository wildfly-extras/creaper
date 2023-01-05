package org.wildfly.extras.creaper.commands.security;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * Add a new mapping classic mapping module to given security domain.
 */
public final class AddMappingModule implements OnlineCommand, OfflineCommand {

    private final String securityDomainName;
    private final String name;
    private final String code;
    private final String type;
    private final String module;
    private final Map<String, String> moduleOptions;
    private final boolean replaceExisting;

    private AddMappingModule(Builder builder) {
        this.securityDomainName = builder.securityDomainName;
        this.name = builder.name;
        this.code = builder.code;
        this.type = builder.type;
        this.module = builder.module;
        this.moduleOptions = builder.moduleOptions;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CliException, CommandFailedException, IOException,
            TimeoutException, InterruptedException {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Operations ops = new Operations(ctx.client);
        Address mappingClassicAddress = Address.subsystem("security")
                .and("security-domain", securityDomainName)
                .and("mapping", "classic");
        try {
            boolean exists = ops.exists(mappingClassicAddress);
            if (!exists) {
                ops.add(mappingClassicAddress);
            }
        } catch (OperationException e) {
            throw new IOException("Failed to access or create mapping=classic in security domain "
                    + securityDomainName, e);
        }

        Address mappingModuleAddress = mappingClassicAddress.and("mapping-module", name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(mappingModuleAddress);
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing mapping module " + name + " in security domain "
                        + securityDomainName, e);
            }
        }

        ops.add(mappingModuleAddress, Values.empty()
                .andOptional("code", code)
                .andOptional("type", type)
                .andOptional("module", module)
                .andObjectOptional("module-options", Values.fromMap(moduleOptions))
        );
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddMappingModule.class)
                .subtree("securitySubsystem", Subtree.subsystem("security"))
                .parameter("atrSecurityDomainName", securityDomainName)
                .parameter("atrName", name)
                .parameter("atrCode", code)
                .parameter("atrType", type)
                .parameter("atrModule", module)
                .parameter("atrModuleOptions", moduleOptions)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private String securityDomainName;
        private String name;
        private String code;
        private String type;
        private String module;
        private final Map<String, String> moduleOptions = new LinkedHashMap<String, String>();
        private boolean replaceExisting;

        /**
         * In case when you use this constructor then mapping module name is the same as its code.
         */
        public Builder(String code) {
            this(code, code);
        }

        public Builder(String code, String name) {
            if (code == null) {
                throw new IllegalArgumentException("Code of the mapping module must be specified as non null value");
            }
            if (name == null) {
                throw new IllegalArgumentException("Name of the mapping module must be specified as non null value");
            }
            if (code.isEmpty()) {
                throw new IllegalArgumentException("Code of the mapping module must not be empty value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the mapping module must not be empty value");
            }
            this.code = code;
            this.name = name;
        }

        public Builder securityDomainName(String securityDomainName) {
            this.securityDomainName = securityDomainName;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder addModuleOption(String name, String value) {
            moduleOptions.put(name, value);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddMappingModule build() {
            if (securityDomainName == null) {
                throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
            }
            if (securityDomainName.isEmpty()) {
                throw new IllegalArgumentException("Name of the security-domain must not be empty value");
            }
            if (type == null) {
                throw new IllegalArgumentException("Type of the security-domain must be specified as non null value");
            }
            return new AddMappingModule(this);
        }

    }
}
