package org.wildfly.extras.creaper.core.offline;

import org.wildfly.extras.creaper.core.ManagementClient;

import java.io.File;
import java.io.IOException;

/**
 * This basically follows the builder pattern, but ensures on the type level that everything that must be set is
 * actually set. That means that the IDE guides you through the configuration and once the code compiles, you can be
 * sure you didn't forget anything important. The downside is that the implementation is a bit involved and exposes
 * quite a number of public types.
 */
public final class OfflineOptions {
    public final boolean isStandalone;

    public final boolean isDomain;
    public String defaultProfile;
    public String defaultHost;
    /** @deprecated use {@code defaulProfile} instead, this will be removed before 1.0 */
    public final String domainProfile;
    /** @deprecated use {@code defaulhost} instead, this will be removed before 1.0 */
    public final String domainHost;

    private final File configurationDirectory; // can be null if configurationFile is specified directly
    public final File configurationFile;

    private OfflineOptions(Data data) {
        this.isStandalone = data.isStandalone;

        this.isDomain = data.isDomain;
        this.defaultProfile = data.defaultProfile;
        this.defaultHost = data.defaultHost;
        this.domainProfile = data.defaultProfile;
        this.domainHost = data.defaultHost;

        this.configurationDirectory = data.configurationDirectory;
        this.configurationFile = data.configurationFile;
    }

    /**
     * Returns the directory in which the {@link #configurationFile} resides, if it is known. This can be useful for
     * accessing other files in that directory, mainly the {@code <application|mgmt>-<users|roles>.properties}.
     * If the {@code configurationFile} was specified directly, and not via {@code rootDirectory} or
     * {@code baseDirectory}, then the configuration directory is unknown and this method will throw an exception.
     */
    public File configurationDirectory() {
        if (configurationDirectory == null) {
            throw new IllegalStateException("Configuration directory not set");
        }

        return configurationDirectory;
    }

    /**
     * A single shared instance of this class is passed through the entire builder invocation chain to accumulate
     * all the options.
     */
    private static final class Data {
        private boolean isStandalone;

        private boolean isDomain;
        private String defaultProfile;
        private String defaultHost;

        private File configurationDirectory;
        private File configurationFile;
    }

    /** Connect to a standalone server. */
    public static RootDirectoryOfflineOptions standalone() {
        Data data = new Data();
        data.isStandalone = true;
        return new RootDirectoryOfflineOptions(data);
    }

    /** Connect to a domain controller. */
    public static DomainOfflineOptions domain() {
        Data data = new Data();
        data.isDomain = true;
        return new DomainOfflineOptions(data);
    }

    public static final class DomainOfflineOptions {
        private final Data data;

        private DomainOfflineOptions(Data data) {
            this.data = data;
        }

        /** Apply profile configuration changes to the {@code profile}. Optional, can only be called once.  */
        public DomainOfflineOptions forProfile(String profile) {
            assert data.isDomain;

            if (data.defaultProfile != null) {
                throw new IllegalStateException("Profile was already set (" + data.defaultProfile + ")");
            }

            data.defaultProfile = profile;
            return this;
        }

        /** Apply host configuration changes to the {@code host}. Optional, can only be called once.  */
        public DomainOfflineOptions forHost(String host) {
            assert data.isDomain;

            if (data.defaultHost != null) {
                throw new IllegalStateException("Host was already set (" + data.defaultHost + ")");
            }

            data.defaultHost = host;
            return this;
        }

        /** You want to call this in order to configure other options: configuration file name, etc. */
        public RootDirectoryOfflineOptions build() {
            return new RootDirectoryOfflineOptions(data);
        }
    }

    public static final class RootDirectoryOfflineOptions {
        private final Data data;

        private RootDirectoryOfflineOptions(Data data) {
            this.data = data;
        }

        /**
         * Apply configuration changes to the application server living in {@code rootDirectory}.
         * The {@code rootDirectory} corresponds to the {@code jboss.home.dir} system property.
         */
        public ConfigurationFileOfflineOptions rootDirectory(File rootDirectory) {
            if (rootDirectory == null) {
                throw new IllegalArgumentException("Root directory must be set");
            }

            if (data.isStandalone) {
                data.configurationDirectory = new File(rootDirectory, "standalone/configuration");
            } else if (data.isDomain) {
                data.configurationDirectory = new File(rootDirectory, "domain/configuration/");
            } else {
                throw new AssertionError();
            }

            return new ConfigurationFileOfflineOptions(data);
        }

        /**
         * Apply configuration changes to configuration files living in {@code baseDirectory}.
         * For standalone server, the {@code baseDirectory} corresponds to the {@code jboss.server.base.dir} system
         * property ({@code $&#123;jboss.home.dir&#125;/standalone} by default). For managed domain, it corresponds to
         * the {@code jboss.domain.base.dir} system property ({@code $&#123;jboss.home.dir&#125;/domain} by default).
         */
        public ConfigurationFileOfflineOptions baseDirectory(File baseDirectory) {
            if (baseDirectory == null) {
                throw new IllegalArgumentException("Base directory must be set");
            }

            data.configurationDirectory = new File(baseDirectory, "configuration");

            return new ConfigurationFileOfflineOptions(data);
        }

        /**
         * Apply configuration changes to the specified {@code configurationFile}.
         */
        public OptionalOfflineOptions configurationFile(File configurationFile) {
            if (configurationFile == null) {
                throw new IllegalArgumentException("Configuration file must be set");
            }

            data.configurationFile = configurationFile;
            return new OptionalOfflineOptions(data);
        }
    }

    public static final class ConfigurationFileOfflineOptions {
        private final Data data;

        public ConfigurationFileOfflineOptions(Data data) {
            this.data = data;
        }

        /**
         * Apply configuration changes to the configuration file with given {@code fileName}. The file is found
         * in the {@code configuration} directory under the specified {@code baseDirectory} or under
         * {@code rootDirectory/<standalone|domain>}.
         * @param fileName just a file name (like {@code standalone-ha.xml}), <b>not</b> a path
         */
        public OptionalOfflineOptions configurationFile(String fileName) {
            if (fileName == null || fileName.isEmpty()) {
                throw new IllegalArgumentException("Configuration file name must be set");
            }

            if (data.configurationDirectory != null) {
                data.configurationFile = new File(data.configurationDirectory, fileName);
            } else {
                throw new AssertionError();
            }

            return new OptionalOfflineOptions(data);
        }
    }

    public static final class OptionalOfflineOptions {
        private final Data data;

        private OptionalOfflineOptions(Data data) {
            this.data = data;
        }

        /** Build the final {@code OfflineOptions}. */
        public OfflineOptions build() {
            return new OfflineOptions(data);
        }
    }

    // ---

    private OfflineManagementClient createManagementClient() throws IOException {
        return new OfflineManagementClientImpl(this);
    }

    static {
        ManagementClient.OfflineClientFactory.set(new ManagementClient.OfflineClientFactory() {
            @Override
            protected OfflineManagementClient create(OfflineOptions options) throws IOException {
                return options.createManagementClient();
            }
        });
    }
}
