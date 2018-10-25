package org.wildfly.extras.creaper.commands.elytron.tls;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommand;

abstract class AbstractAddSSLContext implements OnlineCommand, OfflineCommand {

    protected final String name;
    protected final String cipherSuiteFilter;
    protected final List<String> protocols;
    protected final String keyManager;
    protected final String trustManager;
    protected final String providers;
    protected final String providerName;
    protected final boolean replaceExisting;

    protected AbstractAddSSLContext(Builder builder) {
        this.name = builder.name;
        this.cipherSuiteFilter = builder.cipherSuiteFilter;
        this.protocols = builder.protocols;
        this.keyManager = builder.keyManager;
        this.trustManager = builder.trustManager;
        this.providers = builder.providers;
        this.providerName = builder.providerName;
        this.replaceExisting = builder.replaceExisting;
    }

    protected String joinList(List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        Iterator<String> iterator = list.iterator();
        StringBuilder sb = new StringBuilder(iterator.next());
        while (iterator.hasNext()) {
            sb.append(" ").append(iterator.next());
        }
        return sb.toString();
    }

    abstract static class Builder<THIS extends Builder> {

        protected final String name;
        protected String cipherSuiteFilter;
        protected List<String> protocols;
        protected String keyManager;
        protected String trustManager;
        private boolean replaceExisting;
        protected String providers;
        protected String providerName;

        Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the ssl-context must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the ssl-context must not be empty value");
            }
            this.name = name;
        }

        public final THIS protocols(String... protocols) {
            if (protocols != null && protocols.length > 0) {
                this.protocols = Arrays.asList(protocols);
            }
            return (THIS) this;
        }

        public final THIS cipherSuiteFilter(String cipherSuiteFilter) {
            this.cipherSuiteFilter = cipherSuiteFilter;
            return (THIS) this;
        }

        public final THIS keyManager(String keyManager) {
            this.keyManager = keyManager;
            return (THIS) this;
        }

        public final THIS trustManager(String trustManager) {
            this.trustManager = trustManager;
            return (THIS) this;
        }

        public final THIS replaceExisting() {
            this.replaceExisting = true;
            return (THIS) this;
        }

        public final THIS providers(String providers) {
            this.providers = providers;
            return (THIS) this;
        }

        public final THIS providerName(String providerName) {
            this.providerName = providerName;
            return (THIS) this;
        }

        public abstract AbstractAddSSLContext build();
    }

}
