package org.wildfly.extras.creaper.commands.datasources;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.jboss.as.controller.client.helpers.ClientConstants.OUTCOME;
import static org.jboss.as.controller.client.helpers.ClientConstants.RESULT;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_RECOVERY_PLUGIN;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_STALE_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.DB2_XA_DATASOURCE_CLASS;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MSSQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MSSQL_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MSSQL_XA_DATASOURCE_CLASS;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MYSQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MYSQL_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.MYSQL_XA_DATASOURCE_CLASS;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_STALE_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.ORACLE_XA_DATASOURCE_CLASS;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRESQL_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRESQL_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRESQL_XA_DATASOURCE_CLASS;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRES_PLUS_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRES_PLUS_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.POSTGRES_PLUS_XA_DATASOURCE_CLASS;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_EXCEPTION_SORTER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_STALE_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_VALID_CONNECTION_CHECKER;
import static org.wildfly.extras.creaper.commands.datasources.DatasourceConstants.SYBASE_XA_DATASOURCE_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AddDataSourceForKnownDatabaseOnlineTest {
    private Operations ops;
    private Administration administration;
    private OnlineManagementClient client;

    // we are working with existing H2 database but adding properties of specific database
    // it's bit like hack but we want to test existence of predefined properties and not of
    // the varying url etc.
    private static final String TEST_DATASOURCE_NAME = "TestDS";
    private static final String TEST_DATASOURCE_JNDI = "java:/" + TEST_DATASOURCE_NAME;
    private static final Address TEST_DATASOURCE_ADDRESS =
            Address.subsystem("datasources").and("data-source", TEST_DATASOURCE_NAME);
    private static final Address TEST_XA_DATASOURCE_ADDRESS =
            Address.subsystem("datasources").and("xa-data-source", TEST_DATASOURCE_NAME);
    private static final String DS_URL = "jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;";
    private static final String DRIVER_NAME = "h2";

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_DATASOURCE_ADDRESS);
            ops.removeIfExists(TEST_XA_DATASOURCE_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }


    @Test
    public void addMssqlDataSource() throws CommandFailedException, IOException, OperationException {
        createDatasource(new AddMssqlDataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_DATASOURCE_ADDRESS));
        assertAttribute("valid-connection-checker-class-name", MSSQL_VALID_CONNECTION_CHECKER);
        if (client.serverVersion().greaterThanOrEqualTo(ManagementVersion.VERSION_1_7_0)) {
            assertAttribute("exception-sorter-class-name", MSSQL_EXCEPTION_SORTER);
        }
        assertAttribute("background-validation", true);
        assertAttributeExists("background-validation-millis");
    }

    @Test
    public void addMssqlXaDataSource() throws CommandFailedException, IOException, OperationException {
        createXADatasource(new AddMssqlXADataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
        assertXAAttribute("valid-connection-checker-class-name", MSSQL_VALID_CONNECTION_CHECKER);
        assertXAAttribute("background-validation", true);
        assertXAAttributeExists("background-validation-millis");
        assertXAAttribute("xa-datasource-class", MSSQL_XA_DATASOURCE_CLASS);
        assertTrue("The xa datasource property 'PortNumber' should exist",
                ops.exists(TEST_XA_DATASOURCE_ADDRESS.and("xa-datasource-properties", "PortNumber")));
    }

    @Test
    public void addPostgresqlDataSource() throws CommandFailedException, IOException, OperationException {
        createDatasource(new AddPostgreSqlDataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_DATASOURCE_ADDRESS));
        assertAttribute("valid-connection-checker-class-name", POSTGRESQL_VALID_CONNECTION_CHECKER);
        assertAttribute("exception-sorter-class-name", POSTGRESQL_EXCEPTION_SORTER);
        assertAttribute("background-validation", true);
        assertAttributeExists("background-validation-millis");
    }

    @Test
    public void addPostgresqlXaDataSource() throws CommandFailedException, IOException, OperationException {
        createXADatasource(new AddPostgreSqlXADataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
        assertXAAttribute("valid-connection-checker-class-name", POSTGRESQL_VALID_CONNECTION_CHECKER);
        assertXAAttribute("exception-sorter-class-name", POSTGRESQL_EXCEPTION_SORTER);
        assertXAAttribute("background-validation", true);
        assertXAAttributeExists("background-validation-millis");
        assertXAAttribute("xa-datasource-class", POSTGRESQL_XA_DATASOURCE_CLASS);
        assertTrue("The xa datasource property 'PortNumber' should exist",
                ops.exists(TEST_XA_DATASOURCE_ADDRESS.and("xa-datasource-properties", "PortNumber")));
    }

    @Test
    public void addPostgresPlusDataSource() throws CommandFailedException, IOException, OperationException {
        createDatasource(new AddPostgresPlusDataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_DATASOURCE_ADDRESS));
        assertAttribute("valid-connection-checker-class-name", POSTGRES_PLUS_VALID_CONNECTION_CHECKER);
        assertAttribute("exception-sorter-class-name", POSTGRES_PLUS_EXCEPTION_SORTER);
        assertAttribute("background-validation", true);
        assertAttributeExists("background-validation-millis");
    }

    @Test
    public void addPostgresPlusXaDataSource() throws CommandFailedException, IOException, OperationException {
        createXADatasource(new AddPostgresPlusXADataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
        assertXAAttribute("valid-connection-checker-class-name", POSTGRES_PLUS_VALID_CONNECTION_CHECKER);
        assertXAAttribute("exception-sorter-class-name", POSTGRES_PLUS_EXCEPTION_SORTER);
        assertXAAttribute("background-validation", true);
        assertXAAttributeExists("background-validation-millis");
        assertXAAttribute("xa-datasource-class", POSTGRES_PLUS_XA_DATASOURCE_CLASS);
        assertTrue("The xa datasource property 'PortNumber' should exist",
                ops.exists(TEST_XA_DATASOURCE_ADDRESS.and("xa-datasource-properties", "PortNumber")));
    }

    @Test
    public void addMysqlDataSource() throws CommandFailedException, IOException, OperationException {
        createDatasource(new AddMysqlDataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_DATASOURCE_ADDRESS));
        assertAttribute("valid-connection-checker-class-name", MYSQL_VALID_CONNECTION_CHECKER);
        assertAttribute("exception-sorter-class-name", MYSQL_EXCEPTION_SORTER);
        assertAttribute("background-validation", true);
        assertAttributeExists("background-validation-millis");
    }

    @Test
    public void addMysqlXaDataSource() throws CommandFailedException, IOException, OperationException {
        createXADatasource(new AddMysqlXADataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
        assertXAAttribute("valid-connection-checker-class-name", MYSQL_VALID_CONNECTION_CHECKER);
        assertXAAttribute("exception-sorter-class-name", MYSQL_EXCEPTION_SORTER);
        assertXAAttribute("background-validation", true);
        assertXAAttributeExists("background-validation-millis");
        assertXAAttribute("xa-datasource-class", MYSQL_XA_DATASOURCE_CLASS);
        assertTrue("The xa datasource property 'PortNumber' should exist",
                ops.exists(TEST_XA_DATASOURCE_ADDRESS.and("xa-datasource-properties", "PortNumber")));
    }

    @Test
    public void addOracleDataSource() throws CommandFailedException, IOException, OperationException {
        createDatasource(new AddOracleDataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_DATASOURCE_ADDRESS));
        assertAttribute("valid-connection-checker-class-name", ORACLE_VALID_CONNECTION_CHECKER);
        assertAttribute("stale-connection-checker-class-name", ORACLE_STALE_CONNECTION_CHECKER);
        assertAttribute("exception-sorter-class-name", ORACLE_EXCEPTION_SORTER);
        assertAttribute("background-validation", true);
        assertAttributeExists("background-validation-millis");
    }

    @Test
    public void addOracleXaDataSource() throws CommandFailedException, IOException, OperationException {
        createXADatasource(new AddOracleXADataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
        assertXAAttribute("valid-connection-checker-class-name", ORACLE_VALID_CONNECTION_CHECKER);
        assertXAAttribute("stale-connection-checker-class-name", ORACLE_STALE_CONNECTION_CHECKER);
        assertXAAttribute("exception-sorter-class-name", ORACLE_EXCEPTION_SORTER);
        assertXAAttribute("background-validation", true);
        assertXAAttributeExists("background-validation-millis");
        assertXAAttribute("same-rm-override", false);
        assertXAAttribute("no-tx-separate-pool", true);
        assertXAAttribute("xa-datasource-class", ORACLE_XA_DATASOURCE_CLASS);
    }

    @Test
    public void addSybaseDataSource() throws CommandFailedException, IOException, OperationException {
        createDatasource(new AddSybaseDataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_DATASOURCE_ADDRESS));
        assertAttribute("valid-connection-checker-class-name", SYBASE_VALID_CONNECTION_CHECKER);
        assertAttribute("exception-sorter-class-name", SYBASE_EXCEPTION_SORTER);
        assertAttribute("background-validation", true);
        assertAttributeExists("background-validation-millis");
    }

    @Test
    public void addSybaseXaDataSource() throws CommandFailedException, IOException, OperationException {
        createXADatasource(new AddSybaseXADataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
        assertXAAttribute("valid-connection-checker-class-name", SYBASE_VALID_CONNECTION_CHECKER);
        assertXAAttribute("stale-connection-checker-class-name", SYBASE_STALE_CONNECTION_CHECKER);
        assertXAAttribute("exception-sorter-class-name", SYBASE_EXCEPTION_SORTER);
        assertXAAttribute("background-validation", true);
        assertXAAttributeExists("background-validation-millis");
        assertXAAttribute("same-rm-override", false);
        assertXAAttribute("xa-datasource-class", SYBASE_XA_DATASOURCE_CLASS);
        assertTrue("The xa datasource property 'PortNumber' should exist",
                ops.exists(TEST_XA_DATASOURCE_ADDRESS.and("xa-datasource-properties", "PortNumber")));
    }

    @Test
    public void addDb2DataSource() throws CommandFailedException, IOException, OperationException {
        createDatasource(new AddDb2DataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_DATASOURCE_ADDRESS));
        assertAttribute("valid-connection-checker-class-name", DB2_VALID_CONNECTION_CHECKER);
        assertAttribute("exception-sorter-class-name", DB2_EXCEPTION_SORTER);
        assertAttribute("stale-connection-checker-class-name", DB2_STALE_CONNECTION_CHECKER);
        assertAttribute("background-validation", true);
        assertAttributeExists("background-validation-millis");
    }

    @Test
    public void addDb2XaDataSource() throws CommandFailedException, IOException, OperationException {
        createXADatasource(new AddDb2XADataSource.Builder(TEST_DATASOURCE_NAME));

        assertTrue("The data source should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
        assertXAAttribute("valid-connection-checker-class-name", DB2_VALID_CONNECTION_CHECKER);
        assertXAAttribute("stale-connection-checker-class-name", DB2_STALE_CONNECTION_CHECKER);
        assertXAAttribute("exception-sorter-class-name", DB2_EXCEPTION_SORTER);
        assertXAAttribute("background-validation", true);
        assertXAAttributeExists("background-validation-millis");
        assertXAAttribute("same-rm-override", false);
        assertXAAttribute("xa-datasource-class", DB2_XA_DATASOURCE_CLASS);
        assertXAAttribute("recovery-plugin-class-name", DB2_RECOVERY_PLUGIN);
        assertTrue("The xa datasource property 'PortNumber' should exist",
                ops.exists(TEST_XA_DATASOURCE_ADDRESS.and("xa-datasource-properties", "PortNumber")));
        assertEquals("Recovery plugin property 'EnableIsValid' should match", false,
                ops.readAttribute(TEST_XA_DATASOURCE_ADDRESS, "recovery-plugin-properties")
                        .get(RESULT, "EnableIsValid").asBoolean());
        assertEquals("Recovery plugin property 'IsValidOverride' should match", false,
                ops.readAttribute(TEST_XA_DATASOURCE_ADDRESS, "recovery-plugin-properties")
                        .get(RESULT, "IsValidOverride").asBoolean());
        assertEquals("Recovery plugin property 'EnableClose' should match", false,
                ops.readAttribute(TEST_XA_DATASOURCE_ADDRESS, "recovery-plugin-properties")
                        .get(RESULT, "EnableClose").asBoolean());
    }

    void createDatasource(AddDataSource.Builder builder) throws CommandFailedException {
        AddDataSource command = builder
                .jndiName(TEST_DATASOURCE_JNDI)
                .driverName(DRIVER_NAME)
                .connectionUrl(DS_URL)
                .build();
        client.apply(command);
    }

    void createXADatasource(AddXADataSource.Builder builder) throws CommandFailedException {
        AddXADataSource command = builder
                .jndiName(TEST_DATASOURCE_JNDI)
                .driverName(DRIVER_NAME)
                .build();
        client.apply(command);
    }

    void assertAttributeExists(String name) throws IOException {
        assertAttributeExists(name, TEST_DATASOURCE_ADDRESS);
    }

    void assertAttributeExists(String name, Address address) throws IOException {
        assertEquals("Attribute '" + name + "' should exist", ClientConstants.SUCCESS,
                ops.readAttribute(address, name).get(OUTCOME).asString());
    }

    void assertAttribute(String name, String expectedValue) throws IOException {
        assertAttribute(name, expectedValue, TEST_DATASOURCE_ADDRESS);
    }

    void assertAttribute(String name, Boolean expectedValue) throws IOException {
        assertAttribute(name, expectedValue, TEST_DATASOURCE_ADDRESS);
    }

    void assertAttribute(String name, String expectedValue, Address address) throws IOException {
        assertAttributeExists(name, address);
        assertEquals("Attribute '" + name + "' should match", expectedValue,
                ops.readAttribute(address, name).get(RESULT).asString());
    }

    void assertAttribute(String name, Boolean expectedValue, Address address) throws IOException {
        assertAttributeExists(name, address);
        ops.readAttribute(address, name).assertDefinedValue("Attribute '" + name + "' should be defined");
        assertEquals("Attribute '" + name + "' should match", expectedValue,
                ops.readAttribute(address, name).get(RESULT).asBoolean());
    }

    void assertXAAttributeExists(String name) throws IOException {
        assertAttributeExists(name, TEST_XA_DATASOURCE_ADDRESS);
    }

    void assertXAAttribute(String name, String expectedValue) throws IOException {
        assertAttribute(name, expectedValue, TEST_XA_DATASOURCE_ADDRESS);
    }

    void assertXAAttribute(String name, Boolean expectedValue) throws IOException {
        assertAttribute(name, expectedValue, TEST_XA_DATASOURCE_ADDRESS);
    }
}
