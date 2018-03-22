package org.wildfly.extras.creaper.commands.elytron.credfactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddKerberosSecurityFactory implements OnlineCommand {

    private final String name;
    private final String principal;
    private final List<String> mechanismOIDs;
    private final List<String> mechanismNames;
    private final String path;
    private final String relativeTo;
    private final Integer minimumRemainingLifetime;
    private final Integer requestLifetime;
    private final Boolean server;
    private final Boolean debug;
    private final Boolean obtainKerberosTicket;
    private final Boolean wrapGssCredential;
    private final Boolean required;
    private final Map<String, String> options;
    private final boolean replaceExisting;

    private AddKerberosSecurityFactory(Builder builder) {
        this.name = builder.name;
        this.principal = builder.principal;
        this.mechanismOIDs = builder.mechanismOIDs;
        this.mechanismNames = builder.mechanismNames;
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.minimumRemainingLifetime = builder.minimumRemainingLifetime;
        this.requestLifetime = builder.requestLifetime;
        this.server = builder.server;
        this.debug = builder.debug;
        this.obtainKerberosTicket = builder.obtainKerberosTicket;
        this.wrapGssCredential = builder.wrapGssCredential;
        this.required = builder.required;
        this.options = builder.options;
        // Replace existing
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address kerberosSecurityFactoryAddress = Address.subsystem("elytron").and("kerberos-security-factory", name);
        if (replaceExisting) {
            ops.removeIfExists(kerberosSecurityFactoryAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(kerberosSecurityFactoryAddress, Values.empty()
                .and("name", name)
                .and("principal", principal)
                .and("path", path)
                .andListOptional(String.class, "mechanism-oids", mechanismOIDs)
                .andListOptional(String.class, "mechanism-names", mechanismNames)
                .andOptional("relative-to", relativeTo)
                .andOptional("minimum-remaining-lifetime", minimumRemainingLifetime)
                .andOptional("request-lifetime", requestLifetime)
                .andOptional("server", server)
                .andOptional("debug", debug)
                .andOptional("obtain-kerberos-ticket", obtainKerberosTicket)
                .andOptional("wrap-gss-credential", wrapGssCredential)
                .andOptional("required", required)
                .andObjectOptional("options", Values.fromMap(options)));
    }

    public static final class Builder {

        private final String name;
        private String principal;
        private List<String> mechanismOIDs;
        private List<String> mechanismNames;
        private String path;
        private String relativeTo;
        private Integer minimumRemainingLifetime;
        private Integer requestLifetime;
        private Boolean server;
        private Boolean debug;
        private Boolean obtainKerberosTicket;
        private Boolean wrapGssCredential;
        private Boolean required;
        private Map<String, String> options = new LinkedHashMap<String, String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the kerberos-security-factory must be specified as non empty value");
            }
            this.name = name;
        }

        public Builder principal(String principal) {
            this.principal = principal;
            return this;
        }

        public Builder mechanismOIDs(String... mechanismOIDs) {
            if (mechanismOIDs != null && mechanismOIDs.length > 0) {
                this.mechanismOIDs = Arrays.asList(mechanismOIDs);
            }
            return this;
        }

        public Builder mechanismNames(String... mechanismNames) {
            if (mechanismNames != null && mechanismNames.length > 0) {
                this.mechanismNames = Arrays.asList(mechanismNames);
            }
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public Builder minimumRemainingLifetime(Integer minimumRemainingLifetime) {
            this.minimumRemainingLifetime = minimumRemainingLifetime;
            return this;
        }

        public Builder requestLifetime(Integer requestLifetime) {
            this.requestLifetime = requestLifetime;
            return this;
        }

        public Builder server(Boolean server) {
            this.server = server;
            return this;
        }

        public Builder debug(Boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder obtainKerberosTicket(Boolean obtainKerberosTicket) {
            this.obtainKerberosTicket = obtainKerberosTicket;
            return this;
        }

        public Builder wrapGssCredential(Boolean wrapGssCredential) {
            this.wrapGssCredential = wrapGssCredential;
            return this;
        }

        public Builder required(Boolean required) {
            this.required = required;
            return this;
        }

        public Builder addOption(String name, String value) {
            options.put(name, value);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddKerberosSecurityFactory build() {
            if (principal == null || principal.isEmpty()) {
                throw new IllegalArgumentException("Principal of the kerberos-security-factory must be specified as non empty value");
            }
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Path of the kerberos-security-factory must be specified as non empty value");
            }

            return new AddKerberosSecurityFactory(this);
        }
    }

}
