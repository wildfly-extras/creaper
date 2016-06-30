package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * <p>Utility for reloading the server to its original configuration state. This <b>only</b> makes sense
 * when the server was started using {@code --read-only-server-config}.</p>
 *
 * <p> Currently, managed domain <b>is not supported</b>.</p>
 *
 * <p>For more information about how the server supports this feature, see
 * <a href="http://lists.jboss.org/pipermail/jboss-as7-dev/2012-July/006280.html">http://lists.jboss.org/pipermail/jboss-as7-dev/2012-July/006280.html</a>.</p>
 */
public final class ReloadToOriginal {
    private final OnlineManagementClient client;
    private final int timeoutInSeconds;

    public ReloadToOriginal(OnlineManagementClient client) throws IOException {
        this(client, Administration.DEFAULT_TIMEOUT);
    }

    public ReloadToOriginal(OnlineManagementClient client, int timeoutInSeconds) throws IOException {
        if (!client.options().isStandalone) {
            throw new IllegalStateException("ReloadToOriginal is only supported for a standalone server");
        }
        if (client.version().lessThan(ServerVersion.VERSION_1_4_0)) {
            throw new IllegalStateException("ReloadToOriginal requires at least JBoss AS 7.2.0 (EAP 6.1.0)");
        }

        this.client = client;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    // what would be required to support managed domain:
    // - figure out how --use-current-domain-config and --use-current-host-config work
    // - remove the check for standalone server from the constructor
    // - the commandUsedToStartTheServer method is ready
    // - the checkReloadToOriginalMakesSense method is ready
    // - the perform method is almost ready, need to add a call to DomainAdministrationOperations
    // - need to add a perform(String host) method variant that would be specific to managed domain
    // - need to change RestartOperation.RELOAD_TO_ORIGINAL -- I currently don't know how exactly,
    //   and a bigger refactoring will likely be required

    /**
     * Returns the Java command the server / host controller was started with. It's read from the platform MBean
     * server via the management interface.
     *
     * @param host should be {@code null} for standalone server or for default host in managed domain;
     * should only be non-{@code null} if a specific host in managed domain is checked
     */
    private static String commandUsedToStartTheServer(OnlineManagementClient client, String host) throws IOException {
        Address baseAddress = Address.root();
        if (host != null) {
            baseAddress = Address.host(host);
        }

        ModelNodeResult systemProperties = new Operations(client).readAttribute(
                baseAddress.and(Constants.CORE_SERVICE, Constants.PLATFORM_MBEAN).and("type", "runtime"),
                "system-properties"
        );
        systemProperties.assertDefinedValue();

        // the "sun.java.command" system property exists at least on OpenJDK, Oracle Java and IBM Java
        return systemProperties.value().get("sun.java.command").asString();
    }

    /**
     * Checks if the server / host controller was started using the {@code --read-only-*-config} parameter. If not,
     * reloading to original doesn't make sense.
     *
     * @param host should be {@code null} for standalone server or for default host in managed domain;
     * should only be non-{@code null} if a specific host in managed domain is checked
     */
    private static void checkReloadToOriginalMakesSense(OnlineManagementClient client, String host) throws IOException {
        // the system property shouldn't be documented, it's only meant as a workaround for potential issues
        if (System.getProperty("creaper.reloadToOriginal.skipCheck") != null) {
            return;
        }

        String command = commandUsedToStartTheServer(client, host);

        if (client.options().isStandalone && !command.contains("--read-only-server-config")) {
            throw new IllegalStateException("Reloading to original configuration doesn't make sense, the server must be started with --read-only-server-config=standalone*.xml");
        }

        if (client.options().isDomain
                && !command.contains("--read-only-domain-config")
                && !command.contains("--read-only-host-config")) {
            throw new IllegalStateException("Reloading to original configuration doesn't make sense, the host contoller must be started with --read-only-domain-config=domain.xml and/or --read-only-host-config=host*.xml");
        }
    }

    /**
     * Performs the reload from original configuration.
     */
    // in managed domain, this would apply to the default host
    public void perform() throws InterruptedException, TimeoutException, IOException {
        checkReloadToOriginalMakesSense(client, null);

        new StandaloneAdministrationOperations(client, timeoutInSeconds)
                .performRestartOperation(RestartOperation.RELOAD_TO_ORIGINAL);
    }
}
