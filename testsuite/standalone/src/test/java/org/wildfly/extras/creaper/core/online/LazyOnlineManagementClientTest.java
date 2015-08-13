package org.wildfly.extras.creaper.core.online;

import org.wildfly.extras.creaper.core.ManagementClient;
import org.junit.Before;

import java.io.IOException;

public class LazyOnlineManagementClientTest extends OnlineManagementClientTest {
    @Before
    public void connect() throws IOException {
        client = ManagementClient.onlineLazy(OnlineOptions.standalone().localDefault().build());
    }
}
