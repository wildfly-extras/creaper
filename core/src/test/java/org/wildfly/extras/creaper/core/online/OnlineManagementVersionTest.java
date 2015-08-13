package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.ManagementVersionPart;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OnlineManagementVersionTest {
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

    private void test(Integer major, Integer minor, Integer micro, ManagementVersion expected) throws IOException {
        ModelControllerClient client = mockFor(major, minor, micro);
        ManagementVersion actual = OnlineManagementVersion.discover(client);
        assertEquals(expected, actual);
    }

    @Test
    public void discoverAS70x() throws IOException {
        test(null, null, null, ManagementVersion.VERSION_0_0_0);
    }

    @Test
    public void discoverAS71x_EAP60x() throws IOException {
        test(1, 0, null, ManagementVersion.VERSION_1_0_0);
        test(1, 1, null, ManagementVersion.VERSION_1_1_0);
        test(1, 2, null, ManagementVersion.VERSION_1_2_0);
        test(1, 3, null, ManagementVersion.VERSION_1_3_0);
    }

    @Test
    public void discoverAS72x_EAP61x() throws IOException {
        test(1, 4, 0, ManagementVersion.VERSION_1_4_0);
    }

    @Test
    public void discoverAS73x_EAP62x() throws IOException {
        test(1, 5, 0, ManagementVersion.VERSION_1_5_0);
    }

    @Test
    public void discoverAS74x_EAP63x() throws IOException {
        test(1, 6, 0, ManagementVersion.VERSION_1_6_0);
    }

    @Test
    public void discoverAS75x_EAP64x() throws IOException {
        test(1, 7, 0, ManagementVersion.VERSION_1_7_0);
    }

    @Test
    public void discoverWF800() throws IOException {
        test(2, 0, 0, ManagementVersion.VERSION_2_0_0);
    }

    @Test
    public void discoverWF810() throws IOException {
        test(2, 1, 0, ManagementVersion.VERSION_2_1_0);
    }

    @Test
    public void discoverWF820() throws IOException {
        test(2, 2, 0, ManagementVersion.VERSION_2_2_0);
    }

    @Test
    public void discoverWF900() throws IOException {
        test(3, 0, 0, ManagementVersion.VERSION_3_0_0);
    }

    @Test(expected = IOException.class)
    @SuppressWarnings("unchecked")
    public void error() throws IOException {
        ModelControllerClient mock = mock(ModelControllerClient.class);
        when(mock.execute(any(ModelNode.class))).thenThrow(IOException.class);

        OnlineManagementVersion.discover(mock);
    }
}
