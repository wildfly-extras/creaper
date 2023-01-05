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
 * Add a new authorization classic policy module to given security domain.
 */
public final class AddAuthorizationModule implements OnlineCommand, OfflineCommand {

    private final String securityDomainName;
    private final String name;
    private final String code;
    private final String flag;
    private final String module;
    private final Map<String, String> moduleOptions;
    private final boolean replaceExisting;

    private AddAuthorizationModule(Builder builder) {
        this.securityDomainName = builder.securityDomainName;
        this.name = builder.name;
        this.code = builder.code;
        this.flag = builder.flag;
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
        Address authorizationClassicAddress = Address.subsystem("security")
                .and("security-domain", securityDomainName)
                .and("authorization", "classic");
        try {
            boolean exists = ops.exists(authorizationClassicAddress);
            if (!exists) {
                ops.add(authorizationClassicAddress);
            }
        } catch (OperationException e) {
            throw new IOException("Failed to access or create authorization=classic in security domain "
                    + securityDomainName, e);
        }

        Address authorizationModuleAddress = authorizationClassicAddress.and("policy-module", name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(authorizationModuleAddress);
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing authorization module " + name + " in security domain "
                        + securityDomainName, e);
            }
        }

        ops.add(authorizationModuleAddress, Values.empty()
                .andOptional("code", code)
                .andOptional("flag", flag)
                .andOptional("module", module)
                .andObjectOptional("module-options", Values.fromMap(moduleOptions))
        );
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddAuthorizationModule.class)
                .subtree("securitySubsystem", Subtree.subsystem("security"))
                .parameter("atrSecurityDomainName", securityDomainName)
                .parameter("atrName", name)
                .parameter("atrCode", code)
                .parameter("atrFlag", flag)
                .parameter("atrModule", module)
                .parameter("atrModuleOptions", moduleOptions)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private String securityDomainName;
        private String name;
        private String code;
        private String flag;
        private String module;
        private final Map<String, String> moduleOptions = new LinkedHashMap<String, String>();
        private boolean replaceExisting;

        /**
         * In case when you use this constructor then authorization module name is the same as its code.
         */
        public Builder(String code) {
            this(code, code);
        }

        public Builder(String code, String name) {
            if (code == null) {
                throw new IllegalArgumentException("Code of the authorization module must be specified as non null value");
            }
            if (name == null) {
                throw new IllegalArgumentException("Name of the authorization module must be specified as non null value");
            }
            if (code.isEmpty()) {
                throw new IllegalArgumentException("Code of the authorization module must not be empty value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the authorization module must not be empty value");
            }
            this.code = code;
            this.name = name;
        }

        public Builder securityDomainName(String securityDomainName) {
            this.securityDomainName = securityDomainName;
            return this;
        }

        public Builder flag(String flag) {
            this.flag = flag;
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

        public AddAuthorizationModule build() {
            if (securityDomainName == null) {
                throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
            }
            if (securityDomainName.isEmpty()) {
                throw new IllegalArgumentException("Name of the security-domain must not be empty value");
            }
            if (flag == null) {
                throw new IllegalArgumentException("Flag of the security-domain must be specified as non null value");
            }
            return new AddAuthorizationModule(this);
        }

    }
}
