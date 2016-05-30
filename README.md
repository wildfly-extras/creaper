# Creaper

Creaper is a small library for JBoss AS 7 and WildFly management with a slight
bias towards testing. It provides a simple API for managing a running server
(online) or rewriting a configuration XML file of a stopped server (offline).
Note that manipulating the XML files is generally discouraged and should only
be used when absolutely necessary.

Please refer to these WildFly wiki pages for an in-depth description
of the management functionalities:

- [Core Management Concepts](https://docs.jboss.org/author/display/WFLY10/Core+management+concepts)
- [Management API Reference](https://docs.jboss.org/author/display/WFLY10/Management+API+reference)

## Install

Creaper artifacts are available in Maven Central. However, they are primarily
released through the JBoss.org Maven repository and synchronization to Central
can take some time. If you are eager, you can configure the JBoss.org Maven
repository like this:

    <repositories>
        <repository>
            <id>jboss-public-repository-group</id>
            <name>JBoss Public Maven Repository Group</name>
            <url>http://repository.jboss.org/nexus/content/groups/public/</url>
        </repository>
    </repositories>

Then, declare the dependencies like this:

    <dependency>
        <groupId>org.wildfly.extras.creaper</groupId>
        <artifactId>creaper-core</artifactId>
        <version>${version.org.wildfly.extras.creaper}</version>
    </dependency>
    <dependency>
        <groupId>org.wildfly.extras.creaper</groupId>
        <artifactId>creaper-commands</artifactId>
        <version>${version.org.wildfly.extras.creaper}</version>
    </dependency>

The latest and greatest released version is __`1.2.0`__.

Creaper follows [Semantic Versioning 2.0.0](http://semver.org/spec/v2.0.0.html)
(aka _SemVer_) for versioning and also as a compatibility promise.

### JBoss AS 7 / WildFly Client Libraries

__You have to provide both `*-controller-client` and `*-cli` yourself.__
If you want to use commands for patching, you also have to provide
`wildfly-patching`.

Other dependencies might be required if you're using "local" CLI operations,
such as `jms-queue add ...` on WildFly 10 (it was a built-in operation
in previous WildFly versions, but was moved to a separate module due to
messaging changes). These dependencies are not listed here, because there
are no commands in Creaper that use them.

#### JBoss AS 7

Dependencies for `creaper-core`:

    <!-- 7.5.0.Final-redhat-21 is JBoss EAP 6.4.0 -->
    <dependency>
        <groupId>org.jboss.as</groupId>
        <artifactId>jboss-as-controller-client</artifactId>
        <version>7.5.0.Final-redhat-21</version>
    </dependency>
    <dependency>
        <groupId>org.jboss.as</groupId>
        <artifactId>jboss-as-cli</artifactId>
        <version>7.5.0.Final-redhat-21</version>
    </dependency>

Dependencies for `creaper-commands`:

    <dependency>
        <groupId>org.wildfly</groupId>
        <artifactId>wildfly-patching</artifactId>
        <version>7.5.0.Final-redhat-21</version>
    </dependency>

#### WildFly 8

Dependencies for `creaper-core`:

    <dependency>
        <groupId>org.wildfly</groupId>
        <artifactId>wildfly-controller-client</artifactId>
        <version>8.2.1.Final</version>
    </dependency>
    <dependency>
        <groupId>org.wildfly</groupId>
        <artifactId>wildfly-cli</artifactId>
        <version>8.2.1.Final</version>
    </dependency>

Dependencies for `creaper-commands`:

    <dependency>
        <groupId>org.wildfly</groupId>
        <artifactId>wildfly-patching</artifactId>
        <version>8.2.1.Final</version>
    </dependency>

#### WildFly 9 (based on WildFly Core 1)

Dependencies for `creaper-core`:

    <dependency>
        <groupId>org.wildfly.core</groupId>
        <artifactId>wildfly-controller-client</artifactId>
        <version>1.0.2.Final</version>
    </dependency>
    <dependency>
        <groupId>org.wildfly.core</groupId>
        <artifactId>wildfly-cli</artifactId>
        <version>1.0.2.Final</version>
    </dependency>

Dependencies for `creaper-commands`:

    <dependency>
        <groupId>org.wildfly.core</groupId>
        <artifactId>wildfly-patching</artifactId>
        <version>1.0.2.Final</version>
    </dependency>

#### WildFly 10 (based on WildFly Core 2)

Dependencies for `creaper-core`:

    <dependency>
        <groupId>org.wildfly.core</groupId>
        <artifactId>wildfly-controller-client</artifactId>
        <version>2.1.0.Final</version>
    </dependency>
    <dependency>
        <groupId>org.wildfly.core</groupId>
        <artifactId>wildfly-cli</artifactId>
        <version>2.1.0.Final</version>
    </dependency>

    <!-- probably a bug in wildfly-cli dependencies -->
    <!-- not required if the dependency on wildfly-patching below is also added ->
    <dependency>
        <groupId>org.jboss.logmanager</groupId>
        <artifactId>jboss-logmanager</artifactId>
        <version>2.0.3.Final</version>
    </dependency>

Dependencies for `creaper-commands`:

    <dependency>
        <groupId>org.wildfly.core</groupId>
        <artifactId>wildfly-patching</artifactId>
        <version>2.1.0.Final</version>
    </dependency>

### Transitive Dependencies

These are the dependencies that you will get transitively when you depend
on Creaper:

- `creaper-core`:
    - `com.google.guava:guava`
    - `org.jboss.logging:jboss-logging`
- `creaper-commands`:
    - everything from `creaper-core`
    - `org.codehaus.groovy:groovy`
    - `org.codehaus.groovy:groovy-xml`

If you need to bring your own version of some of these libraries, you should
use dependency exclusions.

Specifically for Groovy: this is only needed for offline management
(i.e, XML configuration files manipulation). Creaper only needs `groovy` and
`groovy-xml`, but excluding them and providing `groovy-all` should work too.

## Use

The entrypoint is `org.wildfly.extras.creaper.core.ManagementClient`. From this
class, you can get management clients for both online and offline use.

After reading this document, consider reading the design document as well
(`DESIGN.md`), it can help you understand some of the design constraints
and motivations.

### Online

Build a management client like this:

    ManagementClient.online(OnlineOptions
            .standalone()
            .hostAndPort("localhost", 9999)
            .build()
    );

Instead of specifying host and port directly, it's possible to connect to the
default port of the application server on `localhost`:

    ManagementClient.online(OnlineOptions.standalone().localDefault().build());

There are options for setting up authentication, disabling local auth and even
for setting up a connection timeout. The builder API is strongly typed, so
the IDE will guide you and if your code compiles, you can be sure that you
didn't forget anything mandatory.

This was for standalone mode. For domain, it looks very similar:

    ManagementClient.online(OnlineOptions
            .domain()
            .forProfile("default")
            .forHost("master")
            .build()
            .hostAndPort("localhost", 9999)
            .build()
    );

The `forProfile` and `forHost` methods specify a default profile and a default
host. These defaults will be used to automatically transform a standalone-like
operation to the domain one (e.g. `/subsystem=infinispan:read-resource` will be
transformed to `/profile=default-profile/subsystem-infinispan:read-resource`).
If you don't specify a default profile or a default host, you'll have to use
the full form of the operation.

You can only specify one default profile and one default host. After calling
the first `build`, the rest is exactly the same as for standalone mode.

Once you have a `OnlineManagementClient`, you can use it to perform management
operations:

    ModelNodeResult result = client.execute(":whoami");
    result.assertSuccess();
    System.out.println(result.get("result", "identity", "username"));

You can pass management operations either as a `String` using CLI syntax or
as a `ModelNode`. Return value is `ModelNodeResult`, which you can use as
a plain old `ModelNode` (it is a subclass, in fact), but it offers some nice
utility methods for working with operation results.

Using "local" CLI operations such as `batch`, `if` or `patch` is possible,
using the `executeCli` method. This method, however, doesn't return a result.
It can only signal success (by returning normally) or failure (by throwing
an exception).

    client.executeCli("patch apply .../path/to/patch.zip");

In addition to performing plain old management operations, you can also perform
higher-level management commands:

    client.apply(new CliFile(new File("/tmp/foobar.cli")));

Commands have no return value. Normal return means success, errors are
expressed by throwing an exception. These exceptions are considered fatal;
that is, if an exception happens, the server is in an unknown state and the
only reasonable action is aborting everything and reporting a major fail.

#### WildFly

By default, `OnlineOptions.standalone().localDefault()` configures a management
client that connects to `localhost:9999` using the `remote` protocol, which
makes sense for JBoss AS 7.

If you want to connect to WildFly (any version), you can use the `protocol`
method:

    ManagementClient.online(OnlineOptions
            .standalone()
            .localDefault()
            .protocol(ManagementProtocol.HTTP_REMOTING)
            .build()
    );

This changes the protocol to `http-remoting` and also changes the default port
to `9990`. If you define the port directly (using `hostAndPort`), you still
have to define the correct protocol.

Also, you can use `.protocol(ManagementProtocol.REMOTE)` to switch
the protocol back to `remote`, which is useful when using WildFly client
libraries against an AS7-based server. (It can't work the other way around,
so if you have AS7 client libraries and specify `http-remoting`, you'll get
an exception.)

Alternatively, you can define a system property `creaper.wildfly` (its value
is ignored, the system property just needs to be defined). This switches
the default protocol to `http-remoting` and the default port to `9990`
(in case SSL is configured, this switches the default protocol to
`https-remoting` and the default port to `9993`).

This way, you can run the same code against JBoss AS 7 and WildFly without
the need to rewrite or recompile anything. Just define a system property, and
provide a proper version of client libraries (as explained above).

Note that it would actually be possible to implement handling
of the `creaper.wildfly` system property completely outside of Creaper just
in terms of the `protocol` method.

#### HTTP transport

If you want to use HTTP transport for executing operation (`.protocol(ManagementProtocol.HTTP)`),
you need to provide Apache HttpComponents Client 4 by yourself. Please notice that `reload` operation through HTTP
transport may fail with WildFly 10.0.0.

### Offline

The API closely resembles the online counterpart. "Connecting" to an offline
server looks like this:

    ManagementClient.offline(OfflineOptions
            .standalone()
            .rootDirectory(new File("/tmp/jboss-eap-6.3"))
            .configurationFile("standalone.xml")
            .build()
    );

    ManagementClient.offline(OfflineOptions
            .domain()
            .forProfile("default")
            .build()
            .rootDirectory(new File("/tmp/jboss-eap-6.3"))
            .configurationFile("standalone.xml")
            .build()
    );

This assumes standard application server directory layout. Custom directory
layouts are also supported. For example, you can specify a configuration file
directly:

    ManagementClient.offline(OfflineOptions
            .standalone()
            .configurationFile(new File("/tmp/standalone.xml"))
            .build()
    );

It is not possible to perform management operations against an offline server.
The only thing you can do is manipulate the configuration XML file:

    ConfigurationFileBackup backupRestore = new ConfigurationFileBackup();
    client.apply(
            backupRestore.backup(),
            GroovyXmlTransform.of(SomeClass.class, "/my-transform.groovy"),
            backupRestore.destroy()
    );

This example shows that it's possible to perform more commands at once (this is
of course also possible in the online case). Specifically, this example first
takes a backup of the configuration file, than applies a XML transformation
described by a Groovy script loaded from classpath, and then destroys
the backup (there's of course a command to restore it, too).

### Building Commands

Those 3 commands shown above (`CliFile`, `ConfigurationFileBackup` and
`XmlTransform`) are foundational. They are meant to be used as building blocks
for more higher-level commands, rather than being used directly.

For example, you can have a CLI file that configures a datasource. Instead of
applying a `CliFile` command directly, you should create a command with
a descriptive name:

    public class SetupDatasource implements OnlineCommand {
        @Override
        public void apply(OnlineCommandContext ctx) throws CommandFailedException {
            ctx.client.apply(new CliFile(SetupDatasource.class));
        }
    }

In this example, the CLI file is loaded from classpath. It must be a sibling
of the `SetupDatasource` class and be named `SetupDatasource.cli`.

In the same way, you can create a command for offline usage:

    public class SetupDatasource implements OfflineCommand {
        @Override
        public void apply(OfflineCommandContext ctx) throws CommandFailedException {
            ctx.client.apply(GroovyXmlTransform.of(SetupDatasource.class));
        }
    }

In this example, the Groovy script that does the XML transformation is loaded
from classpath. It must be a sibling of the `SetupDatasource` class and
be named `SetupDatasource.groovy`.

If you have both online and offline implementations of a command, it is
preferrable to merge them into a single class:

    public class SetupDatasource implements OnlineCommand, OfflineCommand {
        @Override
        public void apply(OnlineCommandContext ctx) throws CommandFailedException {
            ctx.client.apply(new CliFile(SetupDatasource.class));
        }

        @Override
        public void apply(OfflineCommandContext ctx) throws CommandFailedException {
            ctx.client.apply(GroovyXmlTransform.of(SetupDatasource.class));
        }

        @Override
        public String toString() {
            return "SetupDatasource";
        }
    }

Note the `toString` method -- it should return a short description useful
for logging purposes. It's not mandatory, but highly recommended at least
for often used commands.

Your new command can be used in the same way the built-in commands are:

    client.apply(new SetupDatasource());

### Common Management Operations

There is a standalone API for performing common management operations
(e.g. `read-attribute`, `write-attribute` etc.). Create an `Operations` object
from an `OnlineManagementClient`:

    OnlineManagementClient client = ...;
    Operations ops = new Operations(client);

Then, you use the `Operations` object to perform operations:

    ModelNodeResult result = ops.readAttribute(
            Address.subsystem("web").and("configuration", "jsp-configuration"), "development"
    );

Some operations have options you can apply:

    ModelNodeResult result = ops.readAttribute(
            Address.subsystem("web").and("configuration", "jsp-configuration"), "development",
            ReadAttributeOption.NOT_INCLUDE_DEFAULTS
    );

Sometimes, it makes sense to apply more options:

    ModelNodeResult result = ops.readResource(
            Address.subsystem("web").and("configuration", "jsp-configuration"),
            ReadResourceOption.NOT_INCLUDE_DEFAULTS, ReadResourceOption.RecursiveDepth.of(1)
    );

You can also perform a _batch_ (composite) operation:

    ops.batch(new Batch()
            .writeAttribute(Address.subsystem("web").and("configuration", "jsp-configuration"),
                    "development", true)
            .writeAttribute(Address.subsystem("web").and("configuration", "jsp-configuration"),
                    "display-source-fragment", true)
    );

### Common Administrative Management Operations

As a part of the API for common management operations, there is also
a standalone API for performing common administrative operations such as
server reload. Its design is similar to the `Operations` class:

    OnlineManagementClient client = ...;
    Administration admin = new Administration(client);

The `Administration` object can be used to perform administrative tasks:

    admin.reload();

These operations typically don't have a return value, and if they do,
it _isn't_ a `ModelNodeResult`. For example, the `reloadIfRequired` operation
returns whether at least 1 server was in fact reloaded:

    boolean reloaded = admin.reloadIfRequired();

All these operations are available both for standalone server and for managed
domain. In addition to that, there are domain-only operations in a separate
class `DomainAdministration`, which is a subclass of `Administration`:

    DomainAdministration admin = new DomainAdministration(client);
    admin.restartAllServers();

### Patterns

#### Consider creating your own factory/holder for \*ManagementClient objects

When you are writing a larger project where you need a `ManagementClient`
on a lot of places (e.g. a testsuite), consider creating a factory/holder
object. Most of the time, you will only need a single configuration of
the client (or a small number of configurations); having a centralized place
makes it easier to do globally-affecting changes (e.g. changing a management
interface hostname or port).

Creaper is not able to provide you with such a factory, because it can't know
the specifics of your project. It only makes it very easy to create a client,
so your factory will be just a few lines of simple code.

#### Consider using a lazy variant of OnlineManagementClient

When you are in an environment that doesn't give you precise lifecycle control
(e.g. Arquillian with WildFly's `@ServerSetup`), you can use a lazy variant
of the `OnlineManagementClient`. You can create such client eagerly, so that
it's instantly available, but it will only be initialized when it is first used.

Creaper doesn't make it the default variant of the `OnlineManagementClient`,
because lazy initialization also means deferred error checking. Error checking
is something that you usually want to do as soon as possible.

## Contribute

Please use [GitHub Issues](https://github.com/wildfly-extras/creaper/issues)
to file bugs, feature requests or any other communication.

For anything more involved, please read `DESIGN.md`, `CONTRIBUTING.md`
and `GOVERNANCE.md` first.

Pull requests are always welcome!
