package org.wildfly.extras.creaper.commands.datasources;

final class DatasourceConstants {
    private DatasourceConstants() {} // avoid instantiation

    static final int DEFAULT_BACKGROUND_VALIDATION_TIME = 60000;

    static final String NULL_EXCEPTION_SORTER = "org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter";

    static final String MSSQL_VALID_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker";
    static final String MSSQL_EXCEPTION_SORTER = "org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLExceptionSorter";
    static final String MSSQL_XA_DATASOURCE_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerXADataSource";

    static final String POSTGRESQL_VALID_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker";
    static final String POSTGRESQL_EXCEPTION_SORTER = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter";
    static final String POSTGRESQL_XA_DATASOURCE_CLASS = "org.postgresql.xa.PGXADataSource";

    static final String MYSQL_VALID_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker";
    static final String MYSQL_EXCEPTION_SORTER = "org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter";
    static final String MYSQL_XA_DATASOURCE_CLASS = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";

    static final String POSTGRES_PLUS_VALID_CONNECTION_CHECKER = POSTGRESQL_VALID_CONNECTION_CHECKER;
    static final String POSTGRES_PLUS_EXCEPTION_SORTER = POSTGRESQL_EXCEPTION_SORTER;
    static final String POSTGRES_PLUS_XA_DATASOURCE_CLASS = "com.edb.xa.PGXADataSource";

    static final String ORACLE_VALID_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker";
    static final String ORACLE_STALE_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker";
    static final String ORACLE_EXCEPTION_SORTER = "org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter";
    static final String ORACLE_XA_DATASOURCE_CLASS = "oracle.jdbc.xa.client.OracleXADataSource";

    static final String SYBASE_VALID_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker";
    static final String SYBASE_STALE_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker";
    static final String SYBASE_EXCEPTION_SORTER = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter";
    static final String SYBASE_XA_DATASOURCE_CLASS = "com.ibm.db2.jdbc.DB2XADataSource";

    static final String DB2_VALID_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker";
    static final String DB2_STALE_CONNECTION_CHECKER = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker";
    static final String DB2_EXCEPTION_SORTER = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter";
    static final String DB2_XA_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2XADataSource";
    static final String DB2_RECOVERY_PLUGIN = "org.jboss.jca.core.recovery.ConfigurableRecoveryPlugin";
}
