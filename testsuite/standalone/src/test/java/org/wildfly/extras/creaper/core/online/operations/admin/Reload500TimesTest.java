package org.wildfly.extras.creaper.core.online.operations.admin;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.test.SlowTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Category(SlowTests.class)
@RunWith(Arquillian.class)
public class Reload500TimesTest {
    @Test
    public void reload() throws IOException, TimeoutException, InterruptedException {
        OnlineManagementClient client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        try {
            Operations ops = new Operations(client);
            Administration admin = new Administration(client);
            for (int i = 0; i < 500; i++) {
                System.out.println("Iteration " + (i + 1));
                System.out.println(ops.whoami());
                admin.reload();
            }
        } finally {
            client.close();
        }
    }

    @Test
    public void reloadInsideCommand() throws IOException, CommandFailedException {
        OnlineCommand reloadCommand = new OnlineCommand() {
            @Override
            public void apply(OnlineCommandContext ctx) throws IOException, TimeoutException, InterruptedException {
                OnlineManagementClient client = ctx.client;
                Operations ops = new Operations(client);
                Administration admin = new Administration(client);
                System.out.println(ops.whoami());
                admin.reload();
            }
        };

        OnlineManagementClient client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        try {
            for (int i = 0; i < 500; i++) {
                System.out.println("Iteration " + (i + 1));
                client.apply(reloadCommand);
            }
        } finally {
            client.close();
        }
    }
}
