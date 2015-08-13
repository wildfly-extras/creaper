package org.wildfly.extras.creaper.commands.datasources;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>A generic command for creating new XA datasource. For well-known databases, it's preferred to use the subclasses,
 * because they apply some common configuration. This class is supposed to be used for unknown databases or when
 * absolute control is desired.</p>
 *
 * <p><b>Note that the datasources are always created as <i>disabled</i>, unless {@link Builder#enableAfterCreate()}
 * is used!</b></p>
 *
 * @see AddDb2XADataSource
 * @see AddMssqlXADataSource
 * @see AddMysqlXADataSource
 * @see AddOracleXADataSource
 * @see AddPostgresPlusXADataSource
 * @see AddPostgreSqlXADataSource
 * @see AddSybaseXADataSource
 */
public class AddXADataSource implements OnlineCommand, OfflineCommand {
    private final String name;
    private final boolean enableAfterCreation;
    private final boolean replaceExisting;

    // parameters that can be modified by subclasses
    protected Integer allocationRetry;
    protected Integer allocationRetryWaitMillis;
    protected Boolean allowMultipleUsers;
    protected Boolean backgroundValidation;
    protected Integer backgroundValidationMillis;
    protected Integer blockingTimeoutWaitMillis;
    protected String checkValidConnectionSql;
    protected String driverName;
    protected String exceptionSorterClass;
    protected Map<String, String> exceptionSorterProperties;
    protected PoolFlushStrategy flushStrategy;
    protected Integer idleTimeoutMinutes;
    protected Boolean interleaving;
    protected String jndiName;
    protected Integer maxPoolSize;
    protected Integer minPoolSize;
    protected String newConnectionSql;
    protected Boolean noRecovery;
    protected Boolean noTxSeparatePool;
    protected Boolean padXid;
    protected String password;
    protected Boolean prefill;
    protected Boolean useStrictMinPoolSize;
    protected Integer preparedStatementsCacheSize;
    protected Integer queryTimeout;
    protected String reauthPluginClass;
    protected Map<String, String> reauthPluginProperties;
    protected String recoveryUsername;
    protected String recoveryPassword;
    protected String recoverySecurityDomain;
    protected String recoveryPluginClass;
    protected Map<String, String> recoveryPluginProperties;
    protected Boolean sameRmOverride;
    protected String securityDomain;
    protected Boolean setTxQueryTimeout;
    protected Boolean sharePreparedStatements;
    protected Boolean spy;
    protected String staleConnectionCheckerClass;
    protected Map<String, String> staleConnectionCheckerProperties;
    protected Boolean statisticsEnabled;
    protected TrackStatementType trackPreparedStatements;
    protected TransactionIsolation transactionIsolation;
    protected String urlDelimiter;
    protected String urlSelectorStrategyClass;
    protected Boolean useCcm;
    protected Boolean useFastFailAllocation;
    protected Boolean useJavaContext;
    protected Integer useTryLock;
    protected String username;
    protected String validConnectionCheckerClass;
    protected Map<String, String> validConnectionCheckerProperties;
    protected Boolean validateOnMatch;
    protected Boolean wrapXaResource;
    protected String xaDatasourceClass;
    protected Integer xaResourceTimeout;
    protected Map<String, String> xaDatasourceProperties;

    protected AddXADataSource(Builder builder) {
        this.name = builder.name;
        this.enableAfterCreation  = builder.enableAfterCreation;
        this.replaceExisting = builder.replaceExisting;

        this.allocationRetry = builder.allocationRetry;
        this.allocationRetryWaitMillis = builder.allocationRetryWaitMillis;
        this.allowMultipleUsers = builder.allowMultipleUsers;
        this.backgroundValidation = builder.backgroundValidation;
        this.backgroundValidationMillis = builder.backgroundValidationMillis;
        this.blockingTimeoutWaitMillis = builder.blockingTimeoutWaitMillis;
        this.checkValidConnectionSql = builder.checkValidConnectionSql;
        this.driverName = builder.driverName;
        this.exceptionSorterClass = builder.exceptionSorterClass;
        this.exceptionSorterProperties  = builder.exceptionSorterProperties;
        this.flushStrategy = builder.flushStrategy;
        this.idleTimeoutMinutes = builder.idleTimeoutMinutes;
        this.interleaving = builder.interleaving;
        this.jndiName = builder.jndiName;
        this.maxPoolSize = builder.maxPoolSize;
        this.minPoolSize = builder.minPoolSize;
        this.newConnectionSql = builder.newConnectionSql;
        this.noRecovery = builder.noRecovery;
        this.noTxSeparatePool = builder.noTxSeparatePool;
        this.padXid = builder.padXid;
        this.password = builder.password;
        this.prefill = builder.prefill;
        this.useStrictMinPoolSize = builder.useStrictMinPoolSize;
        this.preparedStatementsCacheSize = builder.preparedStatementsCacheSize;
        this.queryTimeout = builder.queryTimeout;
        this.reauthPluginClass = builder.reauthPluginClass;
        this.reauthPluginProperties  = builder.reauthPluginProperties;
        this.recoveryUsername = builder.recoveryUsername;
        this.recoveryPassword = builder.recoveryPassword;
        this.recoverySecurityDomain = builder.recoverySecurityDomain;
        this.recoveryPluginClass = builder.recoveryPluginClass;
        this.recoveryPluginProperties  = builder.recoveryPluginProperties;
        this.sameRmOverride = builder.sameRmOverride;
        this.securityDomain = builder.securityDomain;
        this.setTxQueryTimeout = builder.setTxQueryTimeout;
        this.sharePreparedStatements = builder.sharePreparedStatements;
        this.spy = builder.spy;
        this.staleConnectionCheckerClass = builder.staleConnectionCheckerClass;
        this.staleConnectionCheckerProperties  = builder.staleConnectionCheckerProperties;
        this.statisticsEnabled = builder.statisticsEnabled;
        this.trackPreparedStatements = builder.trackPreparedStatements;
        this.transactionIsolation = builder.transactionIsolation;
        this.urlDelimiter = builder.urlDelimiter;
        this.urlSelectorStrategyClass = builder.urlSelectorStrategyClass;
        this.useCcm = builder.useCcm;
        this.useFastFailAllocation = builder.useFastFailAllocation;
        this.useJavaContext = builder.useJavaContext;
        this.useTryLock = builder.useTryLock;
        this.username = builder.username;
        this.validConnectionCheckerClass = builder.validConnectionCheckerClass;
        this.validConnectionCheckerProperties  = builder.validConnectionCheckerProperties;
        this.validateOnMatch = builder.validateOnMatch;
        this.wrapXaResource = builder.wrapXaResource;
        this.xaDatasourceClass = builder.xaDatasourceClass;
        this.xaResourceTimeout = builder.xaResourceTimeout;
        this.xaDatasourceProperties  = builder.xaDatasourceProperties;
    }

    @Override
    public final void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        modifyIfNeeded(ctx.serverVersion);

        Operations ops = new Operations(ctx.client);

        Address dsAddress = Address.subsystem("datasources").and("xa-data-source", name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(dsAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing XA datasource " + name, e);
            }
        }

        Values values = Values.empty()
            .andOptional("allocation-retry", allocationRetry)
            .andOptional("allocation-retry-wait-millis", allocationRetryWaitMillis)
            .andOptional("allow-multiple-users", allowMultipleUsers)
            .andOptional("background-validation", backgroundValidation)
            .andOptional("background-validation-millis", backgroundValidationMillis)
            .andOptional("blocking-timeout-wait-millis", blockingTimeoutWaitMillis)
            .andOptional("check-valid-connection-sql", checkValidConnectionSql)
            .andOptional("driver-name", driverName)
            .andOptional("exception-sorter-class-name", exceptionSorterClass)
            .andObjectOptional("exception-sorter-properties", Values.fromMap(exceptionSorterProperties))
            .andOptional("idle-timeout-minutes", idleTimeoutMinutes)
            .andOptional("interleaving", interleaving)
            .andOptional("jndi-name", jndiName)
            .andOptional("max-pool-size", maxPoolSize)
            .andOptional("min-pool-size", minPoolSize)
            .andOptional("new-connection-sql", newConnectionSql)
            .andOptional("no-recovery", noRecovery)
            .andOptional("no-tx-separate-pool", noTxSeparatePool)
            .andOptional("pad-xid", padXid)
            .andOptional("password", password)
            .andOptional("pool-prefill", prefill)
            .andOptional("pool-use-strict-min", useStrictMinPoolSize)
            .andOptional("prepared-statements-cache-size", preparedStatementsCacheSize)
            .andOptional("query-timeout", queryTimeout)
            .andOptional("reauth-plugin-class-name", reauthPluginClass)
            .andObjectOptional("reauth-plugin-properties", Values.fromMap(reauthPluginProperties))
            .andOptional("recovery-password", recoveryPassword)
            .andOptional("recovery-plugin-class-name", recoveryPluginClass)
            .andObjectOptional("recovery-plugin-properties", Values.fromMap(recoveryPluginProperties))
            .andOptional("recovery-security-domain", recoverySecurityDomain)
            .andOptional("recovery-username", recoveryUsername)
            .andOptional("same-rm-override", sameRmOverride)
            .andOptional("security-domain", securityDomain)
            .andOptional("set-tx-query-timeout", setTxQueryTimeout)
            .andOptional("share-prepared-statements", sharePreparedStatements)
            .andOptional("spy", spy)
            .andOptional("stale-connection-checker-class-name", staleConnectionCheckerClass)
            .andObjectOptional("stale-connection-checker-properties", Values.fromMap(staleConnectionCheckerProperties))
            .andOptional("statistics-enabled", statisticsEnabled)
            .andOptional("url-delimiter", urlDelimiter)
            .andOptional("url-selector-strategy-class-name", urlSelectorStrategyClass)
            .andOptional("use-ccm", useCcm)
            .andOptional("use-fast-fail", useFastFailAllocation)
            .andOptional("use-java-context", useJavaContext)
            .andOptional("use-try-lock", useTryLock)
            .andOptional("user-name", username)
            .andOptional("valid-connection-checker-class-name", validConnectionCheckerClass)
            .andObjectOptional("valid-connection-checker-properties", Values.fromMap(validConnectionCheckerProperties))
            .andOptional("validate-on-match", validateOnMatch)
            .andOptional("wrap-xa-resource", wrapXaResource)
            .andOptional("xa-datasource-class", xaDatasourceClass)
            .andOptional("xa-resource-timeout", xaResourceTimeout)
            .and("enabled", enableAfterCreation); // enough to enable/disable on WildFly, and AS7 can handle it too
        if (flushStrategy != null) values = values.and("flush-strategy", flushStrategy.value());
        if (transactionIsolation != null) values = values.and("transaction-isolation", transactionIsolation.value());
        if (trackPreparedStatements != null) values = values.and("track-statements", trackPreparedStatements.value());

        Batch batch = new Batch();
        batch.add(dsAddress, values);

        if (xaDatasourceProperties != null) {
            for (Map.Entry<String, String> entry : xaDatasourceProperties.entrySet()) {
                batch.add(dsAddress.and("xa-datasource-properties", entry.getKey()),
                        Values.of("value", entry.getValue()));
            }
        }

        if (enableAfterCreation && ctx.serverVersion.lessThan(ManagementVersion.VERSION_2_0_0)) {
            // AS7 needs this to actually enable the datasource, because the "enabled" attribute in fact doesn't work
            //
            // for WildFly, the "enabled" attribute works fine and this must not be called (enabling twice is an error)
            batch.invoke("enable", dsAddress);
        }

        ops.batch(batch);
    }

    @Override
    public final void apply(OfflineCommandContext ctx) throws CommandFailedException {
        modifyIfNeeded(ctx.serverVersion);

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddXADataSource.class)
                .subtree("datasources", Subtree.subsystem("datasources"))

                .parameter("poolName", name)
                .parameter("enableAfterCreation", enableAfterCreation)
                .parameter("replaceExisting", replaceExisting)
                .parameter("allocationRetry", allocationRetry)
                .parameter("allocationRetryWaitMillis", allocationRetryWaitMillis)
                .parameter("allowMultipleUsers", allowMultipleUsers)
                .parameter("backgroundValidation", backgroundValidation)
                .parameter("backgroundValidationMillis", backgroundValidationMillis)
                .parameter("blockingTimeoutWaitMillis", blockingTimeoutWaitMillis)
                .parameter("checkValidConnectionSql", checkValidConnectionSql)
                .parameter("driverName", driverName)
                .parameter("exceptionSorterClass", exceptionSorterClass)
                .parameter("exceptionSorterProperties", exceptionSorterProperties)
                .parameter("flushStrategy", flushStrategy == null ? null : flushStrategy.value())
                .parameter("idleTimeoutMinutes", idleTimeoutMinutes)
                .parameter("interleaving", interleaving)
                .parameter("jndiName", jndiName)
                .parameter("maxPoolSize", maxPoolSize)
                .parameter("minPoolSize", minPoolSize)
                .parameter("newConnectionSql", newConnectionSql)
                .parameter("noRecovery", noRecovery)
                .parameter("noTxSeparatePool", noTxSeparatePool)
                .parameter("padXid", padXid)
                .parameter("password", password)
                .parameter("prefill", prefill)
                .parameter("useStrictMinPoolSize", useStrictMinPoolSize)
                .parameter("preparedStatementsCacheSize", preparedStatementsCacheSize)
                .parameter("queryTimeout", queryTimeout)
                .parameter("reauthPluginClass", reauthPluginClass)
                .parameter("reauthPluginProperties", reauthPluginProperties)
                .parameter("recoveryUsername", recoveryUsername)
                .parameter("recoveryPassword", recoveryPassword)
                .parameter("recoverySecurityDomain", recoverySecurityDomain)
                .parameter("recoveryPluginClass", recoveryPluginClass)
                .parameter("recoveryPluginProperties", recoveryPluginProperties)
                .parameter("sameRmOverride", sameRmOverride)
                .parameter("securityDomain", securityDomain)
                .parameter("setTxQueryTimeout", setTxQueryTimeout)
                .parameter("sharePreparedStatements", sharePreparedStatements)
                .parameter("spy", spy)
                .parameter("staleConnectionCheckerClass", staleConnectionCheckerClass)
                .parameter("staleConnectionCheckerProperties", staleConnectionCheckerProperties)
                .parameter("statisticsEnabled", statisticsEnabled)
                .parameter("trackPreparedStatements",
                        trackPreparedStatements == null ? null : trackPreparedStatements.value())
                .parameter("transactionIsolation", transactionIsolation == null ? null : transactionIsolation.value())
                .parameter("urlDelimiter", urlDelimiter)
                .parameter("urlSelectorStrategyClass", urlSelectorStrategyClass)
                .parameter("useCcm", useCcm)
                .parameter("useFastFail", useFastFailAllocation)
                .parameter("useJavaContext", useJavaContext)
                .parameter("useTryLock", useTryLock)
                .parameter("username", username)
                .parameter("validConnectionCheckerClass", validConnectionCheckerClass)
                .parameter("validConnectionCheckerProperties", validConnectionCheckerProperties)
                .parameter("validateOnMatch", validateOnMatch)
                .parameter("wrapXaResource", wrapXaResource)
                .parameter("xaDatasourceClass", xaDatasourceClass)
                .parameter("xaResourceTimeout", xaResourceTimeout)
                .parameter("xaDatasourceProperties", xaDatasourceProperties)

                .build();

        ctx.client.apply(transform);
    }

    protected void modifyIfNeeded(ManagementVersion serverVersion) {
        // designed for override
    }

    @Override
    public final String toString() {
        return "AddXADataSource " + name;
    }

    /**
     * Builder for configuration attributes of an XA datasource.
     *
     * @see <a href="http://wildscribe.github.io/JBoss%20EAP/6.2.0/subsystem/datasources/xa-data-source/">
     *        http://wildscribe.github.io/JBoss%20EAP/6.2.0/subsystem/datasources/xa-data-source/</a>
     */
    public static class Builder {
        private String name;
        private boolean enableAfterCreation = false;
        private boolean replaceExisting = false;

        private Integer allocationRetry;
        private Integer allocationRetryWaitMillis;
        private Boolean allowMultipleUsers;
        private Boolean backgroundValidation;
        private Integer backgroundValidationMillis;
        private Integer blockingTimeoutWaitMillis;
        private String checkValidConnectionSql;
        private String driverName;
        private String exceptionSorterClass;
        private Map<String, String> exceptionSorterProperties = new HashMap<String, String>();
        private PoolFlushStrategy flushStrategy;
        private Integer idleTimeoutMinutes;
        private Boolean interleaving;
        private String jndiName;
        private Integer maxPoolSize;
        private Integer minPoolSize;
        private String newConnectionSql;
        private Boolean noRecovery;
        private Boolean noTxSeparatePool;
        private Boolean padXid;
        private String password;
        private Boolean prefill;
        private Boolean useStrictMinPoolSize;
        private Integer preparedStatementsCacheSize;
        private Integer queryTimeout;
        private String reauthPluginClass;
        private Map<String, String> reauthPluginProperties = new HashMap<String, String>();
        private String recoveryUsername;
        private String recoveryPassword;
        private String recoverySecurityDomain;
        private String recoveryPluginClass;
        private Map<String, String> recoveryPluginProperties = new HashMap<String, String>();
        private Boolean sameRmOverride;
        private String securityDomain;
        private Boolean setTxQueryTimeout;
        private Boolean sharePreparedStatements;
        private Boolean spy;
        private String staleConnectionCheckerClass;
        private Map<String, String> staleConnectionCheckerProperties = new HashMap<String, String>();
        private Boolean statisticsEnabled;
        private TrackStatementType trackPreparedStatements;
        private TransactionIsolation transactionIsolation;
        private String urlDelimiter;
        private String urlSelectorStrategyClass;
        private Boolean useCcm;
        private Boolean useFastFailAllocation;
        private Boolean useJavaContext;
        private Integer useTryLock;
        private String username;
        private String validConnectionCheckerClass;
        private Map<String, String> validConnectionCheckerProperties = new HashMap<String, String>();
        private Boolean validateOnMatch;
        private Boolean wrapXaResource;
        private String xaDatasourceClass;
        private Integer xaResourceTimeout;
        private Map<String, String> xaDatasourceProperties = new HashMap<String, String>();

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the xa-data-source must be specified as non null value");
            }
            this.name = name;
        }

        /**
         * Defines the JDBC driver the datasource should use. It is a symbolic name matching the the name of installed
         * driver. In case the driver is deployed as jar, the name is the name of deployment unit.
         */
        public final Builder driverName(String driverName) {
            this.driverName = driverName;
            return this;
        }

        /**
         * Specifies the JNDI name for the datasource.
         */
        public final Builder jndiName(String jndiName) {
            this.jndiName = jndiName;
            return this;
        }

        /**
         *  Specify the user name and password used when creating a new connection.
         */
        public final Builder usernameAndPassword(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        /**
         * Defines whether the connector should be started on startup.
         */
        public final Builder enableAfterCreate() {
            this.enableAfterCreation = true;
            return this;
        }

        /**
         * If xa datasource with the given pool-name exists it will be replaced
         * with this newly created datasource.
         */
        public final Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        /**
         * The fully qualified name of the {@code javax.sql.XADataSource} implementation.
         */
        public final Builder xaDatasourceClass(String xaDatasourceClass) {
            this.xaDatasourceClass = xaDatasourceClass;
            return this;
        }

        /**
         * Specifies the maximum number of connections for a pool. No more connections will be created in each sub-pool.
         */
        public final Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        /**
         * Specifies the minimum number of connections for a pool.
         */
        public final Builder minPoolSize(int minPoolSize) {
            this.minPoolSize = minPoolSize;
            return this;
        }

        /**
         * Adds {@code xa-data-source} property defined for {@code XADataSource} implementation class.
         */
        public final Builder addXaDatasourceProperty(String name, String value) {
            xaDatasourceProperties.put(name, value);
            return this;
        }

        /**
         * Adds {@code xa-data-source} property defined for {@code XADataSource} implementation class.
         */
        public final Builder addXaDatasourceProperty(String name, boolean value) {
            xaDatasourceProperties.put(name, Boolean.toString(value));
            return this;
        }

        /**
         * Adds set of {@code xa-data-source} properties defined for {@code XADataSource} implementation class.
         */
        public final Builder addXaDatasourceProperties(Map<String, String> xaDatasourceProperties) {
            this.xaDatasourceProperties.putAll(xaDatasourceProperties);
            return this;
        }

        /**
         * The allocation retry element indicates the number of times that allocating
         * a connection should be tried before throwing an exception.
         */
        public final Builder allocationRetry(Integer allocationRetry) {
            this.allocationRetry = allocationRetry;
            return this;
        }

        /**
         * Specifies if multiple users will access the datasource through the getConnection(user, password)
         * method and hence if the internal pool type should account for that
         */
        public final Builder allowMultipleUsers(Boolean allowMultipleUsers) {
            this.allowMultipleUsers = allowMultipleUsers;
            return this;
        }

        /**
         * The allocation retry wait millis element indicates the time in milliseconds
         * to wait between retrying to allocate a connection
         */
        public final Builder allocationRetryWaitMillis(Integer allocationRetryWaitMillis) {
            this.allocationRetryWaitMillis = allocationRetryWaitMillis;
            return this;
        }

        /**
         * Connections should be validated on a background thread (versus being validated prior to use).
         *
         * <p>Typically exclusive to use of {@link #validateOnMatch}.</p>
         */
        public final Builder backgroundValidation(Boolean backgroundValidation) {
            this.backgroundValidation = backgroundValidation;
            return this;
        }

        /**
         * Amount of time that background validation will run.
         */
        public final Builder backgroundValidationMillis(Integer backgroundValidationMillis) {
            this.backgroundValidationMillis = backgroundValidationMillis;
            return this;
        }

        /**
         * The blocking-timeout-millis element indicates the maximum time in
         * milliseconds to block while waiting for a connection before throwing an exception.
         */
        public final Builder blockingTimeoutWaitMillis(Integer blockingTimeoutWaitMillis) {
            this.blockingTimeoutWaitMillis = blockingTimeoutWaitMillis;
            return this;
        }

        /**
         * SQL statement to check validity of a pool connection.
         * May be used when connection is taken from pool to use.
         */
        public final Builder checkValidConnectionSql(String checkValidConnectionSql) {
            this.checkValidConnectionSql = checkValidConnectionSql;
            return this;
        }

        /**
         * org.jboss.jca.adapters.jdbc.ExceptionSorter
         */
        public final Builder exceptionSorterClass(String exceptionSorterClass) {
            this.exceptionSorterClass = exceptionSorterClass;
            return this;
        }

        /**
         * Property for {@link #exceptionSorterClass}
         */
        public final Builder addExceptionSorterProperty(String name, String value) {
            exceptionSorterProperties.put(name, value);
            return this;
        }

        /**
         * Property for {@link #exceptionSorterClass}
         */
        public final Builder addExceptionSorterProperty(String name, boolean value) {
            exceptionSorterProperties.put(name, Boolean.toString(value));
            return this;
        }

        /**
         * How poool should be flushed. There is predefined strategies by JCA.
         * See {@link PoolFlushStrategy}.
         */
        public final Builder flushStrategy(PoolFlushStrategy flushStrategy) {
            this.flushStrategy = flushStrategy;
            return this;
        }

        /**
         * The idle-timeout-minutes elements indicates the maximum time in minutes
         * a connection may be idle before being closed.
         */
        public final Builder idleTimeoutMinutes(Integer idleTimeoutMinutes) {
            this.idleTimeoutMinutes = idleTimeoutMinutes;
            return this;
        }

        /**
         * An element to enable interleaving for XA connection factories
         */
        public final Builder interleaving(boolean interleaving) {
            this.interleaving = interleaving;
            return this;
        }

        /**
         * SQL statement to execute whenever a connection is added to the JCA connection pool.
         */
        public final Builder newConnectionSql(String newConnectionSql) {
            this.newConnectionSql = newConnectionSql;
            return this;
        }

        /**
         * Specify if the xa-datasource should be excluded from recovery.
         */
        public final Builder noRecovery(Boolean noRecovery) {
            this.noRecovery = noRecovery;
            return this;
        }

        /**
         * Oracle does not like XA connections getting used both inside and outside a JTA transaction.
         * To workaround the problem you can create separate sub-pools for the different contexts
         */
        public final Builder noTxSeparatePool(Boolean noTxSeparatePool) {
            this.noTxSeparatePool = noTxSeparatePool;
            return this;
        }

        /**
         * Should the Xid be padded
         */
        public final Builder padXid(Boolean padXid) {
            this.padXid = padXid;
            return this;
        }

        /**
         *  Whether to attempt to prefill the connection pool.
         */
        public final Builder prefill(Boolean prefill) {
            this.prefill = prefill;
            return this;
        }

        /**
         * If the {@link #minPoolSize} should be considered a strictly.
         */
        public final Builder useStrictMinPoolSize(Boolean useStrictMinPoolSize) {
            this.useStrictMinPoolSize = useStrictMinPoolSize;
            return this;
        }

        /**
         * The number of prepared statements per connection in an LRU cache
         */
        public final Builder preparedStatementsCacheSize(Integer preparedStatementsCacheSize) {
            this.preparedStatementsCacheSize = preparedStatementsCacheSize;
            return this;
        }

        /**
         * Any configured query timeout in seconds.
         */
        public final Builder queryTimeout(Integer queryTimeout) {
            this.queryTimeout = queryTimeout;
            return this;
        }

        /**
         * Setting reauth plugin class name.
         */
        public final Builder reauthPluginClass(String reauthPluginClass) {
            this.reauthPluginClass = reauthPluginClass;
            return this;
        }

        /**
         * Property for {@link #reauthPluginClass(String)}
         */
        public final Builder addReauthPluginProperty(String name, String value) {
            reauthPluginProperties.put(name, value);
            return this;
        }

        /**
         * Property for {@link #reauthPluginClass(String)}
         */
        public final Builder addReauthPluginProperty(String name, boolean value) {
            reauthPluginProperties.put(name, Boolean.toString(value));
            return this;
        }

        /**
         *  Specify the user name and password used when creating
         *  a new connection during recovery.
         */
        public final Builder recoveryUsernameAndPassword(String username, String password) {
            this.recoveryUsername = username;
            this.recoveryPassword = password;
            return this;
        }

        /**
         * Setting plugin class name for recovery extension plugin used in spi (core.spi.xa).
         */
        public final Builder recoveryPluginClass(String recoveryPluginClass) {
            this.recoveryPluginClass = recoveryPluginClass;
            return this;
        }

        /**
         * Property for {@link #recoveryPluginClass(String)}
         */
        public final Builder addRecoveryPluginProperty(String name, String value) {
            recoveryPluginProperties.put(name, value);
            return this;
        }

        /**
         * Property for {@link #recoveryPluginClass(String)}
         */
        public final Builder addRecoveryPluginProperty(String name, boolean value) {
            recoveryPluginProperties.put(name, Boolean.toString(value));
            return this;
        }

        /**
         * Security domain name to be used for authentication to datasource
         * during recovery.
         */
        public final Builder recoverySecurityDomain(String securityDomain) {
            this.recoverySecurityDomain = securityDomain;
            return this;
        }

        /**
          * The is-same-rm-override element allows one to unconditionally
          * set whether the javax.transaction.xa.XAResource.isSameRM(XAResource) returns true or false.
         */
        public final Builder sameRmOverride(Boolean isSameRmOverride) {
            this.sameRmOverride = isSameRmOverride;
            return this;
        }

        /**
         * Security domain name to be used for authentication to datasource.
         */
        public final Builder securityDomain(String securityDomain) {
            this.securityDomain = securityDomain;
            return this;
        }

        /**
         * Whether to set the query timeout based on the time remaining until
         * transaction timeout, any configured query timeout will be used if there is no transaction.
         */
        public final Builder setTxQueryTimeout(Boolean setTxQueryTimeout) {
            this.setTxQueryTimeout = setTxQueryTimeout;
            return this;
        }

        /**
         * Whether to share prepare statements, i.e. whether asking for same
         * statement twice without closing uses the same underlying prepared statement.
         */
        public final Builder sharePreparedStatements(Boolean sharePreparedStatements) {
            this.sharePreparedStatements = sharePreparedStatements;
            return this;
        }

        /**
         * An org.jboss.jca.adapters.jdbc.ExceptionSorter.
         */
        public final Builder spy(Boolean spy) {
            this.spy = spy;
            return this;
        }

        /**
         * An org.jboss.jca.adapters.jdbc.StaleConnectionChecker.
         */
        public final Builder staleConnectionCheckerClass(String staleConnectionCheckerClass) {
            this.staleConnectionCheckerClass = staleConnectionCheckerClass;
            return this;
        }

        /**
         * Property for {@link #staleConnectionCheckerClass}
         */
        public final Builder addStaleConnectionCheckerProperty(String name, String value) {
            staleConnectionCheckerProperties.put(name, value);
            return this;
        }

        /**
         * Property for {@link #staleConnectionCheckerClass}
         */
        public final Builder addStaleConnectionCheckerProperty(String name, boolean value) {
            staleConnectionCheckerProperties.put(name, Boolean.toString(value));
            return this;
        }

        /**
         * Sets whether runtime statistics are enabled or not.
         */
        public final Builder statisticsEnabled(Boolean statisticsEnabled) {
            this.statisticsEnabled = statisticsEnabled;
            return this;
        }

        /**
         * Whether to check for unclosed statements when a connection is returned
         * to the pool and result sets are closed when a statement is closed/return
         * to the prepared statement cache.
         */
        public final Builder trackPreparedStatements(TrackStatementType trackPreparedStatements) {
            this.trackPreparedStatements = trackPreparedStatements;
            return this;
        }

        /**
         * Defines isolation level for connections created under this datasource.
         */
        public final Builder transactionIsolation(TransactionIsolation transactionIsolation) {
            this.transactionIsolation = transactionIsolation;
            return this;
        }

        /**
         * Specifies the delimeter for URLs in connection-url for HA datasources
         */
        public final Builder urlDelimiter(String urlDelimiter) {
            this.urlDelimiter = urlDelimiter;
            return this;
        }

        /**
         * A class that implements org.jboss.jca.adapters.jdbc.URLSelectorStrategy
         */
        public final Builder urlSelectorStrategyClass(String urlSelectorStrategyClass) {
            this.urlSelectorStrategyClass = urlSelectorStrategyClass;
            return this;
        }

        /**
         * Enable the use of a cached connection manager.
         */
        public final Builder useCcm(Boolean useCcm) {
            this.useCcm = useCcm;
            return this;
        }

        /**
         * Whether fail a connection allocation on the first connection if it
         * is invalid (true) or keep trying until the pool is exhausted of all potential connections (false).
         */
        public final Builder useFastFailAllocation(Boolean useFastFailAllocation) {
            this.useFastFailAllocation = useFastFailAllocation;
            return this;
        }

        /**
         * Setting this to {@code false} will bind the datasource into global JNDI.
         */
        public final Builder useJavaContext(Boolean useJavaContext) {
            this.useJavaContext = useJavaContext;
            return this;
        }

        /**
         * Any configured timeout for internal locks on the resource adapter
         * objects in seconds.
         */
        public final Builder useTryLock(Integer useTryLock) {
            this.useTryLock = useTryLock;
            return this;
        }

        /**
         * An org.jboss.jca.adapters.jdbc.ValidConnectionChecker.
         */
        public final Builder validConnectionCheckerClass(String validConnectionCheckerClass) {
            this.validConnectionCheckerClass = validConnectionCheckerClass;
            return this;
        }

        /**
         * Property for {@link #validConnectionCheckerClass}
         */
        public final Builder addValidConnectionCheckerProperty(String name, String value) {
            validConnectionCheckerProperties.put(name, value);
            return this;
        }

        /**
         * Property for {@link #validConnectionCheckerClass}
         */
        public final Builder addValidConnectionCheckerProperty(String name, boolean value) {
            validConnectionCheckerProperties.put(name, Boolean.toString(value));
            return this;
        }

        /**
         * Validation will be done on connection factory attempt to match a managed connection for a given set.
         *
         * <p>Typically exclusive to use of {@link #backgroundValidation}.</p>
         */
        public final Builder validateOnMatch(Boolean validateOnMatch) {
            this.validateOnMatch = validateOnMatch;
            return this;
        }

        /**
         * Should the XAResource instances be wrapped in a org.jboss.tm.XAResourceWrapper instance.
         */
        public final Builder wrapXaResource(Boolean wrapXaResource) {
            this.wrapXaResource = wrapXaResource;
            return this;
        }

        /**
         * Passed to XAResource.setTransactionTimeout().
         * Default is zero which does not invoke the setter. In seconds.
         */
        public final Builder xaResourceTimeout(Integer xaResourceTimeout) {
            this.xaResourceTimeout = xaResourceTimeout;
            return this;
        }

        public AddXADataSource build() {
            check();
            return new AddXADataSource(this);
        }

        protected final void check() {
            if (jndiName == null) {
                throw new IllegalArgumentException("jndiName must be specified as non null value");
            }
            if (driverName == null) {
                throw new IllegalArgumentException("driverName must be specified as non null value");
            }
            if (minPoolSize != null && minPoolSize < 0) {
                throw new IllegalArgumentException("minPoolSize must be greater than 0 but it's set to "
                        + minPoolSize);
            }
            if (maxPoolSize != null && maxPoolSize < 0) {
                throw new IllegalArgumentException("maxPoolSize must be greater than 0 but it's set to "
                        + maxPoolSize);
            }
            if (maxPoolSize != null && minPoolSize != null && minPoolSize > maxPoolSize) {
                throw new IllegalArgumentException("maxPoolSize has to be greater than minPoolSize but they are set to "
                        + minPoolSize + " and " + maxPoolSize);
            }
            if (backgroundValidationMillis != null && backgroundValidationMillis < 0) {
                throw new IllegalArgumentException("backgroundValidationMilis has to be greater than 0 but it's set to "
                        + backgroundValidationMillis);
            }
            if (queryTimeout != null && queryTimeout < 0) {
                throw new IllegalArgumentException("queryTimeout has to be greater than 0 but it's set to "
                        + queryTimeout);
            }
            if (useTryLock != null && useTryLock < 0) {
                throw new IllegalArgumentException("useTryLock has to be greater than 0 but it's set to "
                        + useTryLock);
            }
            if (allocationRetry != null && allocationRetry < 0) {
                throw new IllegalArgumentException("allocationRetry has to be greater than 0 but it's set to "
                        + allocationRetry);
            }
            if (allocationRetryWaitMillis != null && allocationRetryWaitMillis < 0) {
                throw new IllegalArgumentException("allocationRetryWaitMillis has to be greater than 0 but it's set to "
                        + allocationRetryWaitMillis);
            }
            if (preparedStatementsCacheSize != null && preparedStatementsCacheSize < 0) {
                throw new IllegalArgumentException("preparedStatementCacheSize has to be greater than 0 but it's set to "
                        + preparedStatementsCacheSize);
            }
            if (securityDomain != null && username != null) {
                throw new IllegalArgumentException("Setting username is invalid in combination with securityDomain");
            }
            if (recoverySecurityDomain != null && recoveryUsername != null) {
                throw new IllegalArgumentException("Setting recoveryUsername is invalid in combination with recoverySecurityDomain");
            }
        }
    }
}
