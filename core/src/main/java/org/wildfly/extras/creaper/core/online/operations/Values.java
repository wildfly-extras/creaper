package org.wildfly.extras.creaper.core.online.operations;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A list of named {@link ModelNode} values. It is often used as parameters of a management operation, but it can
 * for example also represent an arbitrary {@code ModelNode} of type {@link org.jboss.dmr.ModelType#OBJECT object}.
 * It is a sequence of string-keyed pairs ({@code key=value}), possibly empty. This class is immutable and its only API
 * consists of various ways of <i>creating</i> a values list.</p>
 *
 * <p>There are some factory methods for obtaining an initial named values list:</p>
 *
 * <ul>
 * <li>{@code Values.empty()} &ndash; an empty list of named values</li>
 * <li>{@code Values.of("foo", value)} &ndash; a single-element list of named values containing a scalar value
 *     named {@code foo} (scalar = atomic value, e.g. {@code boolean}, {@code int} or {@code String})</li>
 * <li>{@code Values.ofList("foo", value1, value2, ...)} &ndash; a single-element list of named values containing
 *     a list named {@code foo}</li>
 * <li>{@code Values.ofObject("foo", object)} &ndash; a single-element list of named values containing
 *     an object named {@code foo} (the object is represented by an instance of the {@code Values} class)</li>
 * </ul>
 *
 * <p>Once you have an initial list of values, you can chain:</p>
 *
 * <ul>
 * <li>{@code values.and("foo", value)} &ndash; a new list of named values containing all the values in the original
 *     list and one new scalar value named {@code foo}</li>
 * <li>{@code values.andList("foo", value1, value2, ...)} &ndash; a new list of named values containing all the values
 *     in the original list and one new list named {@code foo}</li>
 * <li>{@code values.andObject("foo", object)} &ndash; a new list of named values containing all the values
 *     in the original list and one new object named {@code foo} (the object is represented by an instance
 *     of the {@code Values} class)</li>
 * </ul>
 *
 * <p> If you want to apply a method {@code and|andList|andObject} only if the value is not {@code null},
 * you can use the {@code andOptional|andListOptional|andObjectOptional} variant.</p>
 *
 * <p>To sum up: creating a named value list of {@code foo=bar, baz=quux} looks like this:</p>
 *
 * <pre>
 * Values.of("foo", "bar").and("baz", "quux")
 * </pre>
 *
 * <p>And creating a named value list of {@code foo=[bar, baz, quux], answer=42, a=&#123;b=>c, d=>e&#125;}
 * looks like this:</p>
 *
 * <pre>
 * Values.ofList("foo", "bar", "baz", "quux")
 *       .and("answer", 42)
 *       .andObject("a", Values.of("b", "c").and("d", "e"))
 * </pre>
 */
public final class Values {
    static final Values NONE = new Values();

    private final List<Property> namedValues;

    public static Values empty() {
        return new Values();
    }

    public static Values of(String name, boolean value) {
        return new Values().and(name, value);
    }

    public static Values of(String name, int value) {
        return new Values().and(name, value);
    }

    public static Values of(String name, long value) {
        return new Values().and(name, value);
    }

    public static Values of(String name, String value) {
        return new Values().and(name, value);
    }

    public static Values of(String name, ModelNode value) {
        return new Values().and(name, value);
    }

    public static Values ofList(String name, boolean... value) {
        return new Values().andList(name, value);
    }

    public static Values ofList(String name, int... value) {
        return new Values().andList(name, value);
    }

    public static Values ofList(String name, long... value) {
        return new Values().andList(name, value);
    }

    public static Values ofList(String name, String... value) {
        return new Values().andList(name, value);
    }

    public static Values ofList(String name, ModelNode... value) {
        return new Values().andList(name, value);
    }

    public static Values ofObject(String name, Values object) {
        return new Values().andObject(name, object);
    }

    /** @return {@code null} if {@code map} is {@code null} or empty; a filled {@code Values} object otherwise */
    public static Values fromMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        List<Property> properties = new ArrayList<Property>(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            properties.add(new Property(entry.getKey(), new ModelNode(entry.getValue())));
        }
        return new Values(properties);
    }

    // only for conversion from Parameters, remove when Parameters are removed
    @Deprecated
    static Values from(List<Property> propertyList) {
        return new Values(propertyList);
    }

    private Values() {
        this.namedValues = Collections.emptyList();
    }

    private Values(List<Property> namedValues) {
        this.namedValues = Collections.unmodifiableList(namedValues);
    }

    public Values and(String name, boolean value) {
        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, new ModelNode(value)));
        return new Values(newList);
    }

    public Values and(String name, int value) {
        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, new ModelNode(value)));
        return new Values(newList);
    }

    public Values and(String name, long value) {
        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, new ModelNode(value)));
        return new Values(newList);
    }

    public Values and(String name, String value) {
        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, new ModelNode(value)));
        return new Values(newList);
    }

    public Values and(String name, ModelNode value) {
        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, value));
        return new Values(newList);
    }

    public Values andOptional(String name, Boolean value) {
        if (value == null) return this;
        return and(name, value);
    }

    public Values andOptional(String name, Integer value) {
        if (value == null) return this;
        return and(name, value);
    }

    public Values andOptional(String name, Long value) {
        if (value == null) return this;
        return and(name, value);
    }

    public Values andOptional(String name, String value) {
        if (value == null) return this;
        return and(name, value);
    }

    public Values andOptional(String name, ModelNode value) {
        if (value == null) return this;
        return and(name, value);
    }

    public Values andList(String name, boolean... value) {
        ModelNode listValue = new ModelNode().setEmptyList();
        for (boolean singleValue : value) {
            listValue.add(singleValue);
        }

        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, listValue));
        return new Values(newList);
    }

    public Values andList(String name, int... value) {
        ModelNode listValue = new ModelNode().setEmptyList();
        for (int singleValue : value) {
            listValue.add(singleValue);
        }

        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, listValue));
        return new Values(newList);
    }

    public Values andList(String name, long... value) {
        ModelNode listValue = new ModelNode().setEmptyList();
        for (long singleValue : value) {
            listValue.add(singleValue);
        }

        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, listValue));
        return new Values(newList);
    }

    public Values andList(String name, String... value) {
        ModelNode listValue = new ModelNode().setEmptyList();
        for (String singleValue : value) {
            listValue.add(singleValue);
        }

        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, listValue));
        return new Values(newList);
    }

    public Values andList(String name, ModelNode... value) {
        ModelNode listValue = new ModelNode().setEmptyList();
        for (ModelNode singleValue : value) {
            listValue.add(singleValue);
        }

        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, listValue));
        return new Values(newList);
    }

    /**
     * @param clazz type of elements in the {@code value} list; must be one of {@code Boolean.class},
     * {@code Integer.class}, {@code Long.class}, {@code String.class} or {@code ModelNode.class}
     * @throws IllegalArgumentException if {@code clazz} is not one of the known types
     * @throws ClassCastException if some elements of the {@code value} list are not of type {@code clazz}
     * @throws ArrayStoreException if some elements of the {@code value} list are not of type {@code clazz}
     */
    @SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
    public <T> Values andList(Class<T> clazz, String name, List<T> value) {
        if (clazz == Boolean.class) {
            return andList(name, Booleans.toArray((List<Boolean>) value));
        } else if (clazz == Integer.class) {
            return andList(name, Ints.toArray((List<Integer>) value));
        } else if (clazz == Long.class) {
            return andList(name, Longs.toArray((List<Long>) value));
        } else if (clazz == String.class) {
            return andList(name, value.toArray(new String[value.size()]));
        } else if (clazz == ModelNode.class) {
            return andList(name, value.toArray(new ModelNode[value.size()]));
        } else {
            throw new IllegalArgumentException("Only List<Boolean>, List<Integer>, List<Long>, List<String> and List<ModelNode> are supported");
        }
    }

    /**
     * @param clazz type of elements in the {@code value} list; must be one of {@code Boolean.class},
     * {@code Integer.class}, {@code Long.class}, {@code String.class} or {@code ModelNode.class}
     * @throws IllegalArgumentException if {@code clazz} is not one of the known types
     */
    public <T> Values andListOptional(Class<T> clazz, String name, List<T> value) {
        if (value == null) return this;
        return andList(clazz, name, value);
    }

    public Values andObject(String name, Values value) {
        ModelNode objectValue = new ModelNode().setEmptyObject();
        for (Property property : value.namedValues) {
            objectValue.get(property.getName()).set(property.getValue());
        }

        List<Property> newList = new ArrayList<Property>(namedValues);
        newList.add(new Property(name, objectValue));
        return new Values(newList);
    }

    public Values andObjectOptional(String name, Values value) {
        if (value == null) return this;
        return andObject(name, value);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("(");
        boolean first = true;
        for (Property namedValue : namedValues) {
            if (!first) {
                result.append(", ");
            }
            result.append(namedValue.getName()).append("=").append(namedValue.getValue().asString());
            first = false;
        }
        result.append(")");
        return result.toString();
    }

    void addToModelNode(ModelNode modelNode) {
        for (Property namedValue : namedValues) {
            modelNode.get(namedValue.getName()).set(namedValue.getValue());
        }
    }

    // only for tests
    int size() {
        return namedValues.size();
    }
}
