package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;

import java.util.List;

/**
 * <p>Takes care of adjusting a management operation for use in managed domain. That is, if the operation is meant
 * for standalone and it's possible to automatically convert it for domain using the default profile or host
 * from {@link OnlineOptions}, this class does exactly that.</p>
 *
 * <p>Operations that are meant for standalone and are convertible for domain usage are:</p>
 * <ul>
 * <li>operations whose addresses start with {@code /subsystem=...} &ndash; {@code /profile=...} is prepended</li>
 * <li>operations whose addresses start with {@code /core-service=...} &ndash; {@code /host=...} is prepended</li>
 * </ul>
 *
 * <p>If an operation is to be adjusted, but default profile/host is not known, an {@link IllegalArgumentException}
 * is thrown.</p>
 */
final class AdjustOperationForDomain {
    private final OnlineOptions options;

    AdjustOperationForDomain(OnlineOptions options) {
        this.options = options;
    }

    /** @return always the {@code operation} object itself, possibly modified */
    ModelNode adjust(ModelNode operation) {
        if (!options.isDomain) {
            return operation;
        }

        if (Constants.COMPOSITE.equals(operation.get(Constants.OP).asString())) {
            ModelNode steps = operation.get(Constants.STEPS);
            if (steps.getType() != ModelType.LIST) {
                throw new IllegalArgumentException("Composite operation steps is not a list: " + steps.asString());
            }

            for (ModelNode step : steps.asList()) {
                adjust(step);
            }
            return operation;
        }

        ModelNode address = operation.get(Constants.OP_ADDR);
        if (address.getType() != ModelType.LIST) {
            throw new IllegalArgumentException("Operation address is not a list: " + address.asString());
        }

        Property prependToAddress = null;
        List<ModelNode> addressElements = address.asList();
        for (ModelNode addressElement : addressElements) {
            if (addressElement.getType() != ModelType.PROPERTY) {
                throw new IllegalArgumentException("Operation address element is not a property: "
                        + addressElement.asString());
            }

            Property property = addressElement.asProperty();
            if (Constants.PROFILE.equals(property.getName()) || Constants.HOST.equals(property.getName())) {
                return operation;
            }

            if (Constants.SUBSYSTEM.equals(property.getName())) {
                checkProfile(operation.asString());
                prependToAddress = new Property(Constants.PROFILE, new ModelNode(options.defaultProfile));
                break;
            }
            if (Constants.CORE_SERVICE.equals(property.getName())) {
                checkHost(operation.asString());
                prependToAddress = new Property(Constants.HOST, new ModelNode(options.defaultHost));
                break;
            }
        }

        if (prependToAddress != null) {
            address.setEmptyList();
            address.add(prependToAddress);
            for (ModelNode addressElement : addressElements) {
                address.add(addressElement);
            }
        }

        return operation;
    }

    /** @return always the {@code operation} object itself, possibly modified */
    Operation adjust(Operation operation) {
        if (!options.isDomain) {
            return operation;
        }

        // this relies on an assumption that Operation.getOperation always returns the underlying ModelNode
        // and not a copy (see also ControllerClientAssumptionTest)
        adjust(operation.getOperation());

        return operation;
    }

    String adjust(String cliOperation) {
        if (!options.isDomain) {
            return cliOperation;
        }

        if (cliOperation.startsWith("/subsystem")) {
            checkProfile(cliOperation);
            return "/profile=" + options.defaultProfile + cliOperation;
        }
        if (cliOperation.startsWith("/core-service")) {
            checkHost(cliOperation);
            return "/host=" + options.defaultHost + cliOperation;
        }

        return cliOperation;
    }

    private void checkProfile(String operation) {
        if (options.defaultProfile == null) {
            throw new IllegalArgumentException("No default profile, can't perform operation in domain: " + operation);
        }
    }

    private void checkHost(String operation) {
        if (options.defaultHost == null) {
            throw new IllegalArgumentException("No default host, can't perform operation in domain: " + operation);
        }
    }
}
