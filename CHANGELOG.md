# Changelog

## 0.8.2

- added commands for messaging (both HornetQ and ActiveMQ Artemis)
- fixed  `DomainAdministration.start|stopServer`

## 0.8.1

- `Online|OfflineCommand.apply` now declares `throws Exception`
  instead of the arbitrary set of exceptions declared previously
- moved AS7 dependencies from EAP 6.3 to EAP 6.4
- the test suite now runs against WildFly 10 by default (it's still
  possible to run it against all supported AS7 / WildFly versions)
- fixed links to documentation in datasources commands
- fixed `toString` in `ConfigurationFileBackup` commands
- minor stylistic improvements

## 0.8.0

- added commands for the Undertow subsystem
- fixed commands for datasources so that they work on WildFly too
- added `Headers`
- fixed `DomainAdministrationOperations.allRunningServers`
- silenced the CLI, so that it only prints messages through a logger
- heavily refactored the test suite to support running against AS7
  and all WildFly versions
    - using JUnit categories to accomodate for special needs of certain tests
      (manual tests, slow tests, AS7-only or WildFly-only tests)
    - using Maven profiles to allow running against different server versions

## 0.7.0

- added `Operations.headers()` and `SingleOperation.headers()`
- added `Command`
- the `connect` CLI operation must not be accepted

## 0.6.2

- WildFly 10.0.0 version is now recognized (though .Final was not released yet)
- added `OnlineOptions.serverType()`

## 0.6.1

- fixed ordering of autocreated XML elements

## 0.6.0

- added a detailed Checkstyle ruleset (big change for contributors)
- added a detailed CodeNarc ruleset for checking Groovy code
- added support for WildFly:
    - `OnlineOptions.localDefault` now switches port to `9990` when
      the system property `creaper.wildfly` is defined
    - added workaround for WFCORE-623
- added and expanded commands for datasources, online and offline:
    - `AddDataSource`, `AddXADataSource`
    - `RemoveDataSource`, `RemoveXADataSource`
    - DB2: `AddDb2DataSource`, `AddDb2XADataSource`
    - MS SQL: `AddMssqlDataSource`, `AddMssqlXADataSource`
    - MySQL: `AddMysqlDataSource`, `AddMysqlXADataSource`
    - Oracle: `AddOracleDataSource`, `AddOracleXADataSource`
    - Postgres Plus: `AddPostgresPlusDataSource`, `AddPostgresPlusXADataSource`
    - PostgreSQL: `AddPostgreSqlDataSource`, `AddPostgreSqlXADataSource`
    - Sybase: `AddSybaseDataSource`, `AddSybaseXADataSource`
- added `AddJdbcDriver` and `RemoveJdbcDriver`
- added `EnableWebNatives` and `DisableWebNatives`
- added offline commands for adding a web connector and its ssl configuration
- added `RemoveConnector`
- added `OnlineManagementClient.serverVersion()`
- `[Online|Offline]CommandContext.currentVersion` renamed to `serverVersion`;
  the old names are still available, but deprecated and scheduled for removal
- added `Values.fromMap`

## 0.5.0

- added the `Values` class as a replacement for the `Parameters` class,
  which is still available, but deprecated and scheduled for removal
- added `PropertiesFileAuth` for working with auth `.properties` files
- added online command for removing a basic datasource
- depends on Guava

## 0.4.0

- WildFly 9.0.0 version is now recognized (though .Final was not released yet)
- added `GroovyXmlTransform` for Groovy-based unified standalone/domain
  XML transformations; the old `XmlTransform` class is still available,
  but deprecated and scheduled for removal
- added patching-related operations and commands
- added online commands for adding basic and XA datasources
- added `ServersRunningStateBackup` for backing up/restoring
  the state of servers in domain
- `BackupRestore` renamed to `ConfigurationFileBackup`; the old name
  is still available, but deprecated and scheduled for removal
- added `SingleOperation` to the `Operations` API
- added `isReloadRequired` and `isRestartRequired` to the `Administration` API
- added `waitUntilRunning` to the `Administration` API
  and `waitUntilServersRunning` to the `DomainAdministration` API
- added read-only methods `serverGroups`, `hosts`, `allRunningServers`
  and `allServers` to the `DomainAdministration` API
- added state-manipulating methods `startServer`, `stopServer`
  and `removeServer` to the `DomainAdministration` API
- added a workaround for WFCORE-526
- fixed auto-adjusting of standalone operations for domain in batch operations
- fixed `Operations.exists` and `Operations.removeIfExists` in managed domain
- `OfflineOptions.domain[Profile|Host]` renamed to `default[Profile|Host]`;
  the old names are still available, but deprecated and scheduled for removal

## 0.3.0

- building with Java 8 is now supported
- WildFly 8.2.0.Final version is now recognized
- really removed `ManagementClient.wrap()`; this should have been done
  in 0.2.0, but the removal there was only partial
- no more `can't find jboss-cli.xml` warnings
- added `ManagementClient.onlineLazy`
- `OnlineManagementClient` implements `Closeable`
- added `OnlineManagementClient.reconnect()`
- `OnlineManagementClient` now has an `execute` method that takes
  `org.jboss.as.controller.client.Operation`
- `OnlineManagementClient` now checks if it is really connected to a standalone
  server or a domain controller, as defined by the `OnlineOptions`
- the `OnlineManagementClient` now automatically adjusts operations for domain;
  this is done by prepending `/profile=...` to `/subsystem=...` addresses and
  `/host=...` to `/core-service=...` addresses, other addresses are kept intact
- `OnlineManegementClient` exposes the `OnlineOptions` it was created with;
  the `OnlineOptions` are also available from the `OnlineCommandContext`
- `OfflineManegementClient` exposes the `OfflineOptions` it was created with;
  the `OfflineOptions` are also available from the `OfflineCommandContext`
- `OnlineOptions.domain[Profile|Host]` renamed to `default[Profile|Host]`;
  the old names are still available, but deprecated and scheduled for removal
- the `Operations` API supports parameters for the `add` operation
- the `Operations` API supports writing list-valued attributes
- added `Operations.invoke`
- added `Operations.readChildrenNames`
- added `Operations.exists`
- added `Operations.removeIfExists`
- added `[NOT_]INCLUDE_RUNTIME` options to `ReadResourceOption`
- new `Administration` API added as a part of the `Operations` API;
  currently, it only handles server reload and restart
- `ModelNodeResult` has various `*value()` methods for accessing the result,
  optionally with a default value if the result is undefined
- `ModelNodeResult` can access headers and results of operations in domain
- `ModelNodeResult` can access results of batch steps
- added online commands for adding a web connector and its ssl configuration
- added domain-only online commands for adding a server group,
  a server config, and a JVM config
- constructors of the `*CommandContext` classes are no longer `public`;
  this is technically a breaking change, but shouldn't affect anyone

## 0.2.0

- removed `ManagementClient.wrap()`, replaced
  by `OnlineOptions.standalone|domain().wrap()`
- removed `new Operations(ModelControllerClient)`, no replacement provided
- `OfflineOptions` now support custom directory layouts
- removed dependency on `groovy-all`, which contains `groovy-nio` compiled
  with Java 7; replaced by depending on `groovy` and `groovy-xml`

## 0.1.1

- removed `localAS7` and `localWildFly` from `OnlineOptions`, replaced
  by `localDefault`

## 0.1.0

- initial release
