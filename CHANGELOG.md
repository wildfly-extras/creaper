# Changelog

## 2.1.0 (not yet released)

- dropped explicit dependency on `jboss-logging`
- added support for WildFly 34

## 2.0.3 (2024-09-16)

- added support for WildFly 28 - 33
- updated the namespace detection for the root namespace to support stability level qualifiers
- added JDK 21 CI
- upgraded groovy to 4.0.22

## 2.0.2 (2023-01-18)

- reverted `jboss-logging` -> `wildfly-logging` change
- added JDK 17 CI

## 2.0.1 (2023-01-16)

- brought back `remote` protocol

## 2.0.0 [The Road Runner](https://en.wikipedia.org/wiki/Wile_E._Coyote_and_the_Road_Runner) (2023-01-12)

- switched default protocol to `http-remoting` (still default in WildFly 10)
- switched default port to 9990
- added `remote+http` and `remote+https` protocols, deprecated `http-remoting` and `https-remoting`
- dropped `creaper.wildfly` property
- dropped `remoting` protocol support
- added support for WildFly 25 - WildFly 27
- updated `Batch.whoami` to not declare IOException
- upgraded XMLUnit to 2.9.0 (used only in testsuite)

## 2.0.0-Alpha.4

- compatibility with WildFly Core 20.0.0.Beta3 (WildFly 28)
- removed the workaround for WFCORE-526

## 2.0.0-Alpha.3

- added support for WildFly 21 - WildFly 24

## 2.0.0-Alpha.2

- aligned server dependencies with WildFly 10+ and JBoss EAP 7.0.0+
- upgraded Google Guava to 31.1

## 2.0.0-Alpha.1

- move to JDK 8 as a minimal required JDK version
- support for AS7 officially removed
  - note: the property `creaper.wildfly` and protocol `remoting` with `9999` are still kept as default for now
- support for WildFly 8 and WildFly 9 officially removed
- added `AddUndertowListener.sslContext`
- added `AddModule.moduleRootDir`

## 1.6.2 [The Speedy Gonzales Edition](https://en.wikipedia.org/wiki/Speedy_Gonzales) (2020-11-05)

- added support for WildFly 12 - WildFly 20
- added Elytron commands
- added Address.profile

## 1.6.1 [The Cheela Edition](https://en.wikipedia.org/wiki/Dragon%27s_Egg) (2017-05-25)

- fixed `ReloadToSnapshot` for managed domain

## 1.6.0 [The Wildfire Edition](https://en.wikipedia.org/wiki/The_Andromeda_Strain) (2017-04-20)

- added support for WildFly 11 now that 11.0.0.Alpha1 is released
- added commands for resource adapter manipulation
- added `SnapshotBackup`
- added `ReloadToSnapshot`
- added `AddSslServerIdentity.generateSelfSignedCertHost`
- added `Address.getLastPairValue`
- added better support for `ServerVersion` ranges: `ServerVersion.upTo`,
  `ServerVersion.upToAndIncluding`
  and `ServerVersion.inRange(ServerVersionRange)`; the old method
  `ServerVersion.inRange(ServerVersion, ServerVersion)` is now deprecated
  and scheduled for removal
- fixed `Subtree` to also understand `urn:wildfly:*` XML namespaces
- updated `OnlineManagementClient` to log executed operations
  in JSON format on the TRACE logging level
- updated `RemoveCache` to automatically add
  the `allow-resource-service-restart` header when required
- dependency upgrades

## 1.5.0 [The Emiko Edition](https://en.wikipedia.org/wiki/The_Windup_Girl) (2016-09-30)

- added commands for the `infinispan` subsystem
- added commands for the ORB subsystem (`jacorb` / `iiop-openjdk`)
- improved commands for the `transactions` subsystem
- added support for `double` values to `Values` and `ModelNodeResult`
- fixed `Operations.removeIfExists` in case of a missing host resource
- management versions 4.2.0 and 5.0.0 are now recognized
- improved reliability of some tests
- added Travis CI to the project
- added `settings.xml` to the project

## 1.4.0 [The D && !D Edition](https://en.wikipedia.org/wiki/Luminous_\(story_collection\)) (2016-07-15)

- added `Administration.shutdownGracefully`
- added `ReloadToOriginal`
- added `Logging.logger().define`
- added `Online/OfflineCommand.NOOP`
- improved the `Change*TransactionAttributes` offline commands
  to support older application server versions
- fixed handling of the `reconnect-timeout` attribute
  in `AddAuditLogSyslogHandler`
- fixed resource leak in `OnlineManagementClient` when the client connects
  to a server which is running in different operating mode than expected
- improved javadoc of [Domain]Administration.reload\*
- improved the `Values` class to avoid useless allocations

## 1.3.0 [The Kugelblitz Edition](https://en.wikipedia.org/wiki/Heechee) (2016-06-28)

- added support for HTTP/HTTPS transport (`ManagementProtocol.HTTP[S]`)
- added support for configuring SSL (`OnlineOptions.ssl`)
- added commands for audit logging
- added commands for security realm SSL configuration
- added `RemoveSocketBinding`
- fixed `AddHttpsSecurityRealm` for cases when truststore path is not required
- the `OnlineOptions.connectionTimeout` method now accepts values `<= 0`

## 1.2.0 [The Avernus Edition](https://en.wikipedia.org/wiki/Helliconia) (2016-05-18)

- improved waiting for server: `OnlineOptions.connectionTimeout`
  is now also used when connecting to the server fails
- changed default boot timeout: 20 seconds for standalone server
  and 2 minutes for managed domain
- added `CliScript`
- added `Address.extension`
- added `Administration.shutdown` and `DomainAdministration.shutdown*`

## 1.1.0 [The Wintermute Edition](https://en.wikipedia.org/wiki/Neuromancer) (2016-04-25)

- added `OnlineOptions.bootTimeout`
- added commands for authorization in security realms
- added commands for configuring JDBC in the `transactions` subsystem
- updated `GroovyXmlTransform` to understand new XML element `<host-excludes>`
- removed debugging code in the `Administration` class

## 1.0.0 [The Ez|Ra Edition](https://en.wikipedia.org/wiki/Embassytown) (2016-03-15)

- removed deprecated features
- made all the management client dependencies `<scope>provided</scope>`,
  so that the user always has to provide a correct version
- removed the uberjar
- upgraded Groovy to 2.4.6

### Migration from 0.x

1. upgrade to 0.9.6; it should be 100% backwards compatible (except of
   correcting some design issues), so that should be seamless
2. if you use the uberjar, move to a proper dependency management scheme
3. make sure you don't use any features that are deprecated
   (use your IDE or the Java compiler to help with that); all the deprecated
   elements have a javadoc with migration instructions
4. make sure you provide management client dependencies yourself,
   because you will no longer get them transitively (see `README.md`);
   once you do that, you can remove the dependency exclusions
   for `jboss-as-controller-client`, `jboss-as-cli` and `wildfly-patching`
5. upgrade to 1.0.0

## 0.9.6 [The Lindblad Ring Edition](https://en.wikipedia.org/wiki/Pushing_Ice) (2016-02-24)

- if `OnlineOptions.forHost` wasn't called and `OnlineOptions.defaultHost`
  is therefore `null`, operations against addresses `/core-service=...`
  are now performed as-is instead of throwing an exception
- `OfflineOptions.defaultHost` and `.forHost` make no sense because
  an offline client always works with a single file, so they are now
  deprecated and scheduled for removal
- updated `GroovyXmlTransform` to understand new XML elements added in WildFly
- offline commands for security now check the configuration file version
- fixes in commands for security realms

## 0.9.5 [The Heptapod B Edition](https://en.wikipedia.org/wiki/Story_of_Your_Life) (2016-02-17)

- `ManagementVersion` renamed to `ServerVersion`,
  `OnlineManagementClient.serverVersion()` renamed to `version()` and
  `OnlineCommandContext.serverVersion` renamed to `version`;
  the old names are still available, but deprecated and scheduled for removal
  (this is a fix of a critical design flaw that required constant updates
  to the `ManagementVersion` enum to be able to work with new server versions)
- added `OfflineManagementClient.version()` and `OfflineCommandContext.version`
  (server version discovery in offline finally implemented; this now requires
  that all XML files used with `OfflineManegementClient` are AS7/WildFly server
  configuration files, which is technically a breaking change, even though
  it was always the intent and the number of affected users should be low)
- added commands for the security subsystem
- added commands for security realms
- added commands for the transactions subsystem
- added commands for managing deployments
- added `Address.deployment`
- management version 1.8.0 is now recognized
- upgraded Guava to 19.0, see https://github.com/google/guava/wiki/Release19
- the test suite now uses the WildFly 10 final release

## 0.9.4 [The Rorschach Edition](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)) (2016-02-03)

- management version 4.1.0 is now recognized
- added more commands for messaging (both HornetQ and ActiveMQ Artemis)
- `OfflineOptions.default[Profile|Host]` are now `final`
  (should always have been, but it's a breaking change technically)
- added `RELEASE_PROCEDURE.md`

## 0.9.3 [The Dilemma Prison Edition](https://en.wikipedia.org/wiki/The_Quantum_Thief) (2016-01-15)

- added commands for the logging subsystem (see entrypoint class `Logging`)
- added `AddMariaDb[XA]DataSource`
- added `OnlineManagementClient.allowFailures` to avoid exceptions
  when operations are executed from commands and failures are expected
  (e.g. `Operations.exists` or `Operations.removeIfExists`)
- setting a truststore is no longer mandatory in `AddHttpsSecurityRealm`
- the `Add[XA]DataSource` commands now reload the server if required
  when `replaceExisting()` is used
- fixed `Administration.reload` when the server is in `restart-required`
- fixed a failure when `Operations.batch` was passed an empty `Batch`
- added a workaround for WFCORE-1082 to `AddModule`
- `ManagementProtocol.REMOTING` and `HTTP_REMOTE` renamed to `REMOTE`
  and `HTTP_REMOTING`; the old names are still available, but deprecated
  and scheduled for removal
- the test suite now uses latest WildFly 10 pre-release (was blocked
  by wrong patching test)

## 0.9.2 [The Sophotech Edition](https://en.wikipedia.org/wiki/The_Golden_Oecumene) (2015-12-04)

- added command `AddSocketBinding`
- added `ReadResourceOption.ATTRIBUTES_ONLY`
- added `Add[XA]DataSource.managedConnectionPool`

## 0.9.1 [The Great Ship Edition](https://en.wikipedia.org/wiki/Marrow_\(novel\)) (2015-11-23)

- added more commands for messaging (both HornetQ and ActiveMQ Artemis)
- added commands for adding and removing modules
- `OnlineMamagementClient.executeCli` now supports the `reload` operation
  (only without `--xxx` options)
- `ServerType` renamed to `ManagementProtocol` and `OnlineOptions.serverType`
  renamed to `protocol`; the old names are still available, but deprecated
  and scheduled for removal
- intermediate builder methods in the datasource commands now return
  the correct builder type
- improved deprecations (all deprecated elements now have both
  the `@Deprecated` annotation and the `@deprecated` javadoc tag)

## 0.9.0 [The Demosthenes Edition](https://en.wikipedia.org/wiki/Ender's_Game) (2015-10-26)

- first opensource release under the WildFly Extras umbrella
- no changes since 0.8.2

---

## 0.8.2 [The Webster Edition](https://en.wikipedia.org/wiki/City_\(novel\)) (2015-09-25)

- added commands for messaging (both HornetQ and ActiveMQ Artemis)
- fixed `DomainAdministration.start|stopServer`

## 0.8.1 [The Grokking Edition](https://en.wikipedia.org/wiki/Stranger_in_a_Strange_Land) (2015-08-05)

- `Online|OfflineCommand.apply` now declares `throws Exception`
  instead of the arbitrary set of exceptions declared previously
- moved AS7 dependencies from EAP 6.3 to EAP 6.4
- the test suite now runs against WildFly 10 by default (it's still
  possible to run it against all supported AS7 / WildFly versions)
- fixed links to documentation in datasources commands
- fixed `toString` in `ConfigurationFileBackup` commands
- minor stylistic improvements

## 0.8.0 [The Hiro Protagonist Edition](https://en.wikipedia.org/wiki/Snow_Crash) (2015-07-30)

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

## 0.7.0 [The Gömeršaül Edition](http://mycelium.argenite.org/) (2015-05-29)

- added `Operations.headers()` and `SingleOperation.headers()`
- added `Command`
- the `connect` CLI operation must not be accepted

## 0.6.2 [The BuSab Edition](https://en.wikipedia.org/wiki/Bureau_of_Sabotage) (2015-05-19)

- WildFly 10.0.0 version is now recognized (though .Final was not released yet)
- added `OnlineOptions.serverType()`

## 0.6.1 [The Shabda-Oud Edition](http://en.wikipedia.org/wiki/Metabarons) (2015-05-06)

- fixed ordering of autocreated XML elements

## 0.6.0 [The Shrike Edition](https://en.wikipedia.org/wiki/Hyperion_Cantos) (2015-04-24)

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

## 0.5.0 [The Prime Radiant Edition](http://en.wikipedia.org/wiki/Psychohistory_\(fictional\)) (2015-04-01)

- added the `Values` class as a replacement for the `Parameters` class,
  which is still available, but deprecated and scheduled for removal
- added `PropertiesFileAuth` for working with auth `.properties` files
- added online command for removing a basic datasource
- depends on Guava

## 0.4.0 [The Full of Stars Edition](https://en.wikipedia.org/wiki/2001:_A_Space_Odyssey_\(novel\)) (2015-02-23)

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

## 0.3.0 [The Sonic Screwdriver Edition](https://en.wikipedia.org/wiki/Sonic_screwdriver) (2014-12-19)

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

## 0.2.0 (2014-09-30)

- removed `ManagementClient.wrap()`, replaced
  by `OnlineOptions.standalone|domain().wrap()`
- removed `new Operations(ModelControllerClient)`, no replacement provided
- `OfflineOptions` now support custom directory layouts
- removed dependency on `groovy-all`, which contains `groovy-nio` compiled
  with Java 7; replaced by depending on `groovy` and `groovy-xml`

## 0.1.1 (2014-09-24)

- removed `localAS7` and `localWildFly` from `OnlineOptions`, replaced
  by `localDefault`

## 0.1.0 (2014-09-23)

- initial release
