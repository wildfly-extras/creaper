package org.wildfly.extras.creaper.commands.deployments;

import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOY;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;

/**
 * Command which takes care about deploying specified deployment under specified deployment name.
 * <p/>
 * In case of domain it is deployed by default to all server groups
 */
public final class Deploy implements OnlineCommand {

    private final String deploymentName;
    private final InputStream deploymentInputStream;
    private final boolean autoCloseInputStream;
    private final List<String> serverGroups;

    private Deploy(InputStream deploymentInputStream, Builder deployCmdOptions) {
        this.deploymentInputStream = deploymentInputStream;
        this.deploymentName = deployCmdOptions.deploymentName;
        this.autoCloseInputStream = deployCmdOptions.autoCloseInputStream;
        this.serverGroups = deployCmdOptions.serverGroups;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        // deploy and add to server groups
        ModelNode content = new ModelNode();
        content.get(Constants.INPUT_STREAM_INDEX).set(0);

        ModelNode deployRequest;

        if (ctx.options.isDomain) {
            List<String> groups = serverGroups;
            if (serverGroups == null)
                groups = ops.readChildrenNames(Address.root(), Constants.SERVER_GROUP)
                        .stringListValue(Collections.<String>emptyList());

            List<ModelNode> groupDeploymentAddresses = new ArrayList<ModelNode>(groups.size());
            for (String serverGroup : groups) {
                ModelNode mn = new ModelNode();
                mn.add(Constants.SERVER_GROUP, serverGroup);
                mn.add(Constants.DEPLOYMENT, deploymentName);
                groupDeploymentAddresses.add(mn);
            }
            deployRequest = createDeploymentOperation(content,
                    groupDeploymentAddresses.toArray(new ModelNode[groupDeploymentAddresses.size()]));
        } else {
            deployRequest = createDeploymentOperation(content);
            ModelNode deployStep = deployRequest.get(Constants.STEPS).add();
            ModelNode deploymentAddress = new ModelNode();
            deploymentAddress.add(Constants.DEPLOYMENT, deploymentName);
            deployStep.set(getEmptyOperation(DEPLOY, deploymentAddress));
        }

        OperationBuilder builder = new OperationBuilder(deployRequest, autoCloseInputStream);
        builder.addInputStream(deploymentInputStream);
        ctx.client.execute(builder.build());
    }

    private ModelNode createDeploymentOperation(ModelNode content, ModelNode... serverGroupAddresses) {
        ModelNode composite = getEmptyOperation(Constants.COMPOSITE, new ModelNode().setEmptyList());
        ModelNode steps = composite.get(Constants.STEPS);
        ModelNode step1 = steps.add();
        step1.set(getEmptyOperation(Constants.ADD, new ModelNode().add(Constants.DEPLOYMENT, deploymentName)));
        step1.get(Constants.CONTENT).add(content);
        if (serverGroupAddresses != null) {
            for (ModelNode serverGroup : serverGroupAddresses) {
                ModelNode sg = steps.add();
                sg.set(getEmptyOperation(Constants.ADD, serverGroup));
                sg.get(ENABLED).set(true);
            }
        }
        return composite;
    }

    private ModelNode getEmptyOperation(String operationName, ModelNode address) {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(operationName);
        if (address != null) {
            op.get(Constants.OP_ADDR).set(address);
        } else {
            // Just establish the standard structure; caller can fill in address later
            op.get(Constants.OP_ADDR);
        }
        return op;
    }


    /**
     * Builder for creating command for deploying deployment to the server.
     */
    public static final class Builder {
        private File deploymentFile;
        private InputStream deploymentInputStream;
        private final boolean autoCloseInputStream;
        private final String deploymentName;
        private List<String> serverGroups;

        public Builder(File deploymentFile) {
            if (!deploymentFile.exists()) {
                throw new IllegalArgumentException(deploymentFile + " doesn't exist!");
            }
            this.deploymentFile = deploymentFile;
            this.deploymentName = deploymentFile.getName();
            this.autoCloseInputStream = true;
        }

        public Builder(InputStream deploymentInputStream, String deploymentName,
                       boolean autoCloseInputStream) {
            this.deploymentInputStream = deploymentInputStream;
            this.deploymentName = deploymentName;
            this.autoCloseInputStream = autoCloseInputStream;
        }

        public Deploy build() {
            InputStream inputStream = deploymentInputStream;
            if (deploymentInputStream == null && deploymentFile != null) {
                try {
                    inputStream = new FileInputStream(deploymentFile);
                } catch (FileNotFoundException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
            return new Deploy(inputStream, this);
        }


        /**
         * <p>Domain mode only</p>
         * <p>
         *     Adds list of server groups for which the deployment should be applied.
         * </p>
         * <p>
         *     Note you should use either {@code toAllServerGroup} or {@code toServerGroups}, not both.
         * </p>
         */
        public Builder toServerGroups(String... serverGroups) {
            if (this.serverGroups == null && serverGroups != null) {
                this.serverGroups = new ArrayList<String>();
            }
            if (serverGroups != null) {
                this.serverGroups.addAll(Arrays.asList(serverGroups));
            }
            return this;
        }

        /**
         * <p>Domain mode only</p>
         * <p>
         *     Specifies that the deployment should be deployed to all server groups
         * </p>
         * <p>
         *     Note you should use either {@code toAllServerGroup} or {@code toServerGroups}, not both.
         * </p>
         */
        public Builder toAllServerGroups() {
            this.serverGroups = null;
            return this;
        }

    }
}
