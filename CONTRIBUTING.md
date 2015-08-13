# Contributing

## Issue Tracking

Use [GitHub Issues](https://github.com/wildfly-extras/creaper/issues).

## Git Workflow

The `master` branch is used for developing the latest upcoming version.
If older versions are still being developed, they have their own branches.

All code contributions come in the form of pull requests. This is when code
review happens. Using feature branches for pull requests is recommended.

When merging a pull request, tests and other checks (`checkstyle`, `codenarc`,
`findbugs` etc.) are run. Running them locally before submitting a pull request
is recommended (see below).

History is kept linear, because it's easier to understand. The current volume
of commits allows rebasing and keeping the history clean. If the amount grows
significantly, this can of course be reconsidered.

## Local Workflow

Creaper uses Maven for build. For testing, the Surefire plugin is used
and the tests that need a running application server use Arquillian.
This means that it's enough to run `mvn test` to execute all the tests.
There are some tests that take a long time to execute, so these are
not executed by default. To execute them, activate the `slow-tests` profile
by running `mvn test -Pslow-tests`.

By default, the tests run against WildFly 10. If you want to run the tests
against AS7 or previous WildFly, activate one of the test suite profiles:
`mvn clean test -Pwildfly8`. Available profiles are:

- `as7` (see below)
- `wildfly8`
- `wildfly9`
- `wildfly10` (active by default)

Using the `as7` profile requires access to a Maven repository that, in addition
to all other dependencies, also contains the respective AS7 distribution under
these coordinates: `org.jboss.as:jboss-as-dist:${version.org.jboss.as.jboss-as-everything}`
(see `pom.xml` for version number). The URL of the repository must be specified
using a system property `maven.jboss.ga.repository.url` (again see `pom.xml`).

To run a single test from the test suite, the following Maven invocation works:
`mvn clean test -DfailIfNoTests=false -pl testsuite/standalone/ -Dtest=...`
(possibly with a profile activation `-Pwildfly8`). See
[Surefire documentation](http://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html)
for more details about `-Dtest`.

Running `mvn verify` will take much longer, because it will:

- run static analysis (Checkstyle, CodeNarc and Findbugs)
- generate source and javadoc JARs
- build the uberjar

When developing, `mvn test` is probably enough most of the time. Running
`mvn verify` is good, but it makes sense to do it just before submitting
a pull request.

## Code Style

The Java code must adhere to the Checkstyle ruleset (`checkstyle.xml`) and
the Groovy code must adhere to the CodeNarc ruleset (`codenarc.xml`). Both
these rulesets are derived from common code styles that are widely used.

The most important parts are: 4-space indentation, no tabs, max 120 characters
per line, opening braces always at the end of line and closing braces always
on their own line.

Acronyms and abbreviations are capitalized like regular words, except for
two-letter _acronyms_ (two-letter _abbreviations_ like "ID" and "Mr." are
still capitalized like other words). For example:

- `JdbcDriver`, not `JDBCDriver`
- `SslConfig`, not `SSLConfig`
- `XADataSource`, not `XaDataSource` (acronym)
- `AS7`, not `As7` (acronym)
- `Id`, not `ID` (not an acronym)

For Maven POMs, use the canonical order of XML elements defined by
[POM Code Convention](http://maven.apache.org/developers/conventions/code.html#POM_Code_Convention).

## Guidelines for Commands

The `commands` module contains a set of often-used implementations
of management commands. If you want to decide whether your management
command is suitable for inclusion, refer to the following guidelines:

1. _It should be high-level._ A command that sets a single attribute
   of a single management resource is most probably not very useful.
2. _It should provide very common functionality._ Commands that are
   only seldom used should be kept separately. For example: adding
   a message queue -- absolutely yes, configuring a colocated HA topology
   for HornetQ -- most probably no. Adding a datasource -- absolutely yes,
   configuring a commit markable resource -- most probably no.
3. _It is OK for a command to be highly configurable._ Commands that cover
   very often used functionality can be configurable to also cover only
   seldom used functionality. However, the ability to do corner cases
   shouldn't obscure the ability to do common cases. As an example, see
   the commands for datasources.
4. _Providing both online and offline implementation is highly desirable,
   but not strictly mandatory._ Sometimes, either online or offline
   implementation doesn't even make sense.
5. _Commands shouldn't perform server reload/restart themselves, unless
   the user enables an option to do that._ Some commands will cause the server
   to require reload or restart. The possibility of this happening should be
   properly documented, but the commands shouldn't attempt to perform reload
   or restart themselves. They can expose an option to do that, which the user
   has to enable explicitly (i.e., this _must_ be _opt-in_, never opt-out).
   The reason is that reload or restart is a fairly involed operation that
   the user should be aware of.
