package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Use {@link Values}.</p>
 *
 * <p>Note that even though the {@code Values} class looks <i>very</i> similar, it can't be used
 * in the exact same way. The {@code Parameters} class was intended to be immutable, but it actually isn't;
 * the {@code Values} class <i>is</i>. So this was possible to do with {@code Parameters}:</p>
 *
 * <pre>
 * Parameters params = Parameters.empty();
 * if (aaa != null) params.add("aaa", aaa);
 * if (bbb != null) params.add("bbb", bbb);
 * if (ccc != null) params.add("ccc", ccc);
 * </pre>
 *
 * <p>But it's no longer possible with {@code Values}. Instead, you have to do this:</p>

 * <pre>
 * Values params = Values.empty();
 * if (aaa != null) <b>params =</b> params.add("aaa", aaa);
 * if (bbb != null) <b>params =</b> params.add("bbb", bbb);
 * if (ccc != null) <b>params =</b> params.add("ccc", ccc);
 * </pre>
 */
@Deprecated
public final class Parameters {
    static final Parameters NONE = new Parameters(Collections.<Property>emptyList());

    private final List<Property> parameters;

    public static Parameters empty() {
        return new Parameters();
    }

    public static Parameters of(String parameterName, boolean parameterValue) {
        return new Parameters().and(parameterName, parameterValue);
    }

    public static Parameters of(String parameterName, int parameterValue) {
        return new Parameters().and(parameterName, parameterValue);
    }

    public static Parameters of(String parameterName, long parameterValue) {
        return new Parameters().and(parameterName, parameterValue);
    }

    public static Parameters of(String parameterName, String parameterValue) {
        return new Parameters().and(parameterName, parameterValue);
    }

    public static Parameters of(String parameterName, ModelNode parameterValue) {
        return new Parameters().and(parameterName, parameterValue);
    }

    public static Parameters ofList(String parameterName, boolean... parameterValue) {
        return new Parameters().andList(parameterName, parameterValue);
    }

    public static Parameters ofList(String parameterName, int... parameterValue) {
        return new Parameters().andList(parameterName, parameterValue);
    }

    public static Parameters ofList(String parameterName, long... parameterValue) {
        return new Parameters().andList(parameterName, parameterValue);
    }

    public static Parameters ofList(String parameterName, String... parameterValue) {
        return new Parameters().andList(parameterName, parameterValue);
    }

    public static Parameters ofList(String parameterName, ModelNode... parameterValue) {
        return new Parameters().andList(parameterName, parameterValue);
    }

    private Parameters() {
        this(new ArrayList<Property>());
    }

    private Parameters(List<Property> parameters) {
        this.parameters = parameters;
    }

    public Parameters and(String parameterName, boolean parameterValue) {
        parameters.add(new Property(parameterName, new ModelNode(parameterValue)));
        return this;
    }

    public Parameters and(String parameterName, int parameterValue) {
        parameters.add(new Property(parameterName, new ModelNode(parameterValue)));
        return this;
    }

    public Parameters and(String parameterName, long parameterValue) {
        parameters.add(new Property(parameterName, new ModelNode(parameterValue)));
        return this;
    }

    public Parameters and(String parameterName, String parameterValue) {
        parameters.add(new Property(parameterName, new ModelNode(parameterValue)));
        return this;
    }

    public Parameters and(String parameterName, ModelNode parameterValue) {
        parameters.add(new Property(parameterName, parameterValue));
        return this;
    }

    public Parameters andList(String parameterName, boolean... parameterValue) {
        ModelNode listParameterValue = new ModelNode().setEmptyList();
        for (boolean singleValue : parameterValue) {
            listParameterValue.add(singleValue);
        }

        parameters.add(new Property(parameterName, listParameterValue));
        return this;
    }

    public Parameters andList(String parameterName, int... parameterValue) {
        ModelNode listParameterValue = new ModelNode().setEmptyList();
        for (int singleValue : parameterValue) {
            listParameterValue.add(singleValue);
        }

        parameters.add(new Property(parameterName, listParameterValue));
        return this;
    }

    public Parameters andList(String parameterName, long... parameterValue) {
        ModelNode listParameterValue = new ModelNode().setEmptyList();
        for (long singleValue : parameterValue) {
            listParameterValue.add(singleValue);
        }

        parameters.add(new Property(parameterName, listParameterValue));
        return this;
    }

    public Parameters andList(String parameterName, String... parameterValue) {
        ModelNode listParameterValue = new ModelNode().setEmptyList();
        for (String singleValue : parameterValue) {
            listParameterValue.add(singleValue);
        }

        parameters.add(new Property(parameterName, listParameterValue));
        return this;
    }

    public Parameters andList(String parameterName, ModelNode... parameterValue) {
        ModelNode listParameterValue = new ModelNode().setEmptyList();
        for (ModelNode singleValue : parameterValue) {
            listParameterValue.add(singleValue);
        }

        parameters.add(new Property(parameterName, listParameterValue));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("(");
        boolean first = true;
        for (Property parameter : parameters) {
            if (!first) {
                result.append(", ");
            }
            result.append(parameter.getName()).append("=").append(parameter.getValue().asString());
            first = false;
        }
        result.append(")");
        return result.toString();
    }

    Values toValues() {
        return Values.from(parameters);
    }
}
