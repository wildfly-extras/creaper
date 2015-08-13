package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.ManagementVersionPart;

import java.io.IOException;

final class OnlineManagementVersion {
    private OnlineManagementVersion() {} // avoid instantiation

    /**
     * Returns the management version of the server which the {@code client} is connected to.
     * @throws IOException if an I/O error occurs during any management operation
     */
    static ManagementVersion discover(ModelControllerClient client) throws IOException {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        op.get(Constants.OP_ADDR).setEmptyList();
        // this would be preferrable, as it means transferring less data, but it is not supported on older versions
        //op.get(Constants.ATTRIBUTES_ONLY).set(true);

        ModelNodeResult result = new ModelNodeResult(client.execute(op));
        result.assertSuccess();

        ModelNode resultValue = result.get(Constants.RESULT);
        return ManagementVersion.from(
                readPart(resultValue, ManagementVersionPart.MAJOR),
                readPart(resultValue, ManagementVersionPart.MINOR),
                readPart(resultValue, ManagementVersionPart.MICRO)
        );
    }

    /** Returns the value of the {@code part} from the management model or 0 if it doesn't exist. */
    private static int readPart(ModelNode result, ManagementVersionPart part) throws IOException {
        if (result.hasDefined(part.attributeName())) {
            return result.get(part.attributeName()).asInt();
        } else {
            return 0;
        }
    }
}
