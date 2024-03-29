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
    public void discoverAS70x() throws IOException {
        test(null, null, null, ServerVersion.VERSION_0_0_0);
    }

    @Test
    public void discoverAS71x_EAP60x() throws IOException {
        test(1, 0, null, ServerVersion.VERSION_1_0_0);
        test(1, 1, null, ServerVersion.VERSION_1_1_0);
        test(1, 2, null, ServerVersion.VERSION_1_2_0);
        test(1, 3, null, ServerVersion.VERSION_1_3_0);
    }

    @Test
    public void discoverAS72x_EAP61x() throws IOException {
        test(1, 4, 0, ServerVersion.VERSION_1_4_0);
    }

    @Test
    public void discoverAS73x_EAP62x() throws IOException {
        test(1, 5, 0, ServerVersion.VERSION_1_5_0);
    }

    @Test
    public void discoverAS74x_EAP63x() throws IOException {
        test(1, 6, 0, ServerVersion.VERSION_1_6_0);
    }

    @Test
    public void discoverAS75x_EAP64x() throws IOException {
        test(1, 7, 0, ServerVersion.VERSION_1_7_0);
        test(1, 8, 0, ServerVersion.VERSION_1_8_0);
    }

    @Test
    public void discoverWF800() throws IOException {
        test(2, 0, 0, ServerVersion.VERSION_2_0_0);
    }

    @Test
    public void discoverWF810() throws IOException {
        test(2, 1, 0, ServerVersion.VERSION_2_1_0);
    }

    @Test
    public void discoverWF820() throws IOException {
        test(2, 2, 0, ServerVersion.VERSION_2_2_0);
    }

    @Test
    public void discoverWF900() throws IOException {
        test(3, 0, 0, ServerVersion.VERSION_3_0_0);
    }

    @Test
    public void discoverWF1000() throws IOException {
        test(4, 0, 0, ServerVersion.VERSION_4_0_0);
    }

    @Test(expected = IOException.class)
    @SuppressWarnings("unchecked")
    public void error() throws IOException {
        ModelControllerClient mock = mock(ModelControllerClient.class);
        when(mock.execute(any(ModelNode.class))).thenThrow(IOException.class);

        OnlineServerVersion.discover(mock);
    }
}
