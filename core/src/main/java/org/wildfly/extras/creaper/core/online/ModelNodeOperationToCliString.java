package org.wildfly.extras.creaper.core.online;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

import java.util.List;

/** Note that this does <b>not</b> always produce a valid CLI string, it's just an approximation for logging! */
final class ModelNodeOperationToCliString {
    private ModelNodeOperationToCliString() {} // avoid instantiation

    // the implementation here is not very efficient with string handling, but it shouldn't pose a problem

    static String convert(ModelNode op) {
        try {
            String operation = op.get(Constants.OP).asString();

            if (!Constants.COMPOSITE.equals(operation)) {
                return convertSingleOperation(op);
            } else {
                StringBuilder result = new StringBuilder("composite: ");
                boolean first = true;
                for (ModelNode singleOp : op.get(Constants.STEPS).asList()) {
                    if (!first) {
                        result.append(", ");
                    }
                    first = false;

                    result.append(convertSingleOperation(singleOp));
                }
                return result.toString();
            }
        } catch (Exception e) {
            // just a safety measure if something goes wrong (either bad operation,
            // which will be rejected by the server later on, or an error in this class)
            return op.asString();
        }
    }

    private static String convertSingleOperation(ModelNode op) {
        try {
            String operation = op.get(Constants.OP).asString();
            List<Property> address = op.get(Constants.OP_ADDR).asPropertyList();
            return convertAddress(address) + ":" + operation + convertParameters(op);
        } catch (Exception e) {
            return op.asString();
        }
    }

    private static String convertAddress(List<Property> address) {
        if (address.isEmpty()) {
            return "/";
        }

        StringBuilder result = new StringBuilder();
        for (Property element : address) {
            result.append("/").append(element.getName()).append("=").append(element.getValue().asString());
        }
        return result.toString();
    }

    private static String convertParameters(ModelNode op) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Property parameter : op.asPropertyList()) {
            String name = parameter.getName();
            if (Constants.OP.equals(name)
                    || Constants.OP_ADDR.equals(name)
                    || Constants.OPERATION_HEADERS.equals(name)) {
                continue;
            }

            if (!first) {
                result.append(", ");
            }
            first = false;

            result.append(name).append("=").append(convertValue(parameter.getValue()));
        }

        if (result.length() > 0) {
            return "(" + result.toString() + ")";
        }
        return "";
    }

    private static String convertValue(ModelNode value) {
        switch (value.getType()) {
            case LIST:
                StringBuilder result = new StringBuilder("[");
                boolean first = true;
                for (ModelNode element : value.asList()) {
                    if (!first) {
                        result.append(", ");
                    }
                    first = false;

                    result.append(convertValue(element));
                }
                result.append("]");
                return result.toString();
            case OBJECT:
                result = new StringBuilder("{");
                first = true;
                for (Property element : value.asPropertyList()) {
                    if (!first) {
                        result.append(", ");
                    }
                    first = false;

                    result.append(element.getName()).append(" => ").append(convertValue(element.getValue()));
                }
                result.append("}");
                return result.toString();
            case BYTES:
                return "<bytes>";
            default:
                return value.asString();
        }
    }
}
