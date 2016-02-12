package org.wildfly.extras.creaper.commands.security.realms;

import java.util.Arrays;
import java.util.List;

/**
 * Helper class which holds information about Keytab.
 */
public final class KerberosKeytab {

    private final String principal;
    private final String path;
    private final String relativeTo;
    private final List<String> forHosts;
    private final Boolean debug;

    private KerberosKeytab(Builder builder) {
        this.principal = builder.principal;
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.forHosts = builder.forHosts;
        this.debug = builder.debug;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getPath() {
        return path;
    }

    public String getRelativeTo() {
        return relativeTo;
    }

    public List<String> getForHosts() {
        return forHosts;
    }

    public Boolean getDebug() {
        return debug;
    }

    public static final class Builder {

        private String principal;
        private String path;
        private String relativeTo;
        private List<String> forHosts;
        private Boolean debug;

        public Builder principal(String principal) {
            this.principal = principal;
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

        /**
         * Add hosts related to this keytab. In case when this keytab is relevant for all hosts then you can set "*" as
         * host in parameter of this method or rather use {@code validForAllHosts()} method of this builder.
         */
        public Builder validForHosts(String... forHosts) {
            if (forHosts != null && forHosts.length > 0) {
                this.forHosts = Arrays.asList(forHosts);
            }
            return this;
        }

        /**
         * Indicate that this keytab is relevant for all hosts.
         */
        public Builder validForAllHosts() {
            this.validForHosts("*");
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public KerberosKeytab build() {
            if (principal == null) {
                throw new IllegalArgumentException("Principal of the kerberos keytab must be specified as non null value");
            }
            if (principal.isEmpty()) {
                throw new IllegalArgumentException("Principal of the kerberos keytab must not be empty value");
            }
            return new KerberosKeytab(this);
        }

    }

}
