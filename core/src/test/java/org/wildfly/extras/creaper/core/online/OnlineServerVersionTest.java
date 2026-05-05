package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ManagementVersionPart;
import org.junit.Test;
import org.wildfly.extras.creaper.core.ServerVersion;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OnlineServerVersionTest {
    private ModelControllerClient mockFor(Integer major, Integer minor, Integer micro) throws IOException {
        ModelNode result = new ModelNode();
        result.get(Constants.OUTCOME).set(Constants.SUCCESS);
        ModelNode resultValue = result.get(Constants.RESULT);
        if (major != null) {
            resultValue.get(ManagementVersionPart.MAJOR.attributeName()).set(major);
        }
        if (minor != null) {
            resultValue.get(ManagementVersionPart.MINOR.attributeName()).set(minor);
        }
        if (micro != null) {
            resultValue.get(ManagementVersionPart.MICRO.attributeName()).set(micro);
        }

        ModelControllerClient mock = mock(ModelControllerClient.class);
        when(mock.execute(any(ModelNode.class))).thenReturn(result);

        return mock;
    }

    private void test(Integer major, Integer minor, Integer micro, ServerVersion expected) throws IOException {
        ModelControllerClient client = mockFor(major, minor, micro);
        ServerVersion actual = OnlineServerVersion.discover(client);
        assertEquals(expected, actual);
    }

    @Test
    public void discoverWF27() throws IOException {
        test(20, 0, 0, ServerVersion.VERSION_20_0_0);
    }

    @Test
    public void discoverWF28() throws IOException {
        test(21, 0, 0, ServerVersion.VERSION_21_0_0);
    }

    @Test
    public void discoverWF33() throws IOException {
        test(26, 0, 0, ServerVersion.VERSION_26_0_0);
    }

    @Test
    public void discoverUnknown() throws IOException {
        test(99, 0, 0, ServerVersion.from(99, 0, 0));
    }

    @Test(expected = IOException.class)
    @SuppressWarnings("unchecked")
    public void error() throws IOException {
        ModelControllerClient mock = mock(ModelControllerClient.class);
        when(mock.execute(any(ModelNode.class))).thenThrow(IOException.class);

        OnlineServerVersion.discover(mock);
    }
}
