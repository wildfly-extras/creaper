package org.wildfly.extras.creaper.commands.foundation.online;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class CliTest {
    protected OnlineManagementClient client;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
    }

    @After
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    private void assertStillValid() throws IOException {
        new Operations(client).whoami().assertSuccess();
    }

    @Test
    public void cliScript_good() {
        try {
            client.apply(new CliScript(":whoami"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void cliScript_bad() throws IOException {
        try {
            client.apply(new CliScript(":write-attribute(name=management-major-version, value=42)"));
            fail();
        } catch (CommandFailedException e) {
            // expected
        }

        assertStillValid();
    }

    @Test
    public void cliFile_fromClass_good() {
        try {
            client.apply(new GoodCliFile());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void cliFile_fromClass_bad() throws IOException {
        try {
            client.apply(new BadCliFile());
            fail();
        } catch (CommandFailedException e) {
            // expected
        }

        assertStillValid();
    }

    @Test
    public void cliFile_fromPath_good() {
        try {
            client.apply(new CliFile(CliTest.class, "GoodCliFile.cli"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void cliFile_fromPath_bad() throws IOException {
        try {
            client.apply(new CliFile(CliTest.class, "BadCliFile.cli"));
            fail();
        } catch (CommandFailedException e) {
            // expected
        }

        assertStillValid();
    }
}
