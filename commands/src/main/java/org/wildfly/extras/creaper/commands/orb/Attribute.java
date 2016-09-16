package org.wildfly.extras.creaper.commands.orb;


/**
 * Definition of attribute transition state. As builder defines
 * it could be
 * <ul>
 *   <li>having value - current value will be redefined</li>
 *   <li>having no value (being null) - leaving current value where it is</li>
 *   <li>undefined value - current value will be undefined</li>
 * </ul>
 */
final class Attribute<T> {
    private final T value;
    private final boolean isToUndefine;

    private Attribute(T value) {
        this.value = value;
        this.isToUndefine = false;
    }

    private Attribute(T value, boolean isToUndefine) {
        this.value = value;
        this.isToUndefine = isToUndefine;
    }

    public static <T> Attribute<T> noValue() {
        return new Attribute<T>(null);
    }

    public static <T> Attribute<T> undefine() {
        return new Attribute<T>(null, true);
    }

    public static Attribute<String> undefineString() {
        return new Attribute<String>(null, true);
    }

    public static <T> Attribute<T> of(T referenceValue) {
        return new Attribute<T>(referenceValue);
    }

    public T get() {
        return value;
    }

    /**
     * Defines if the attribute is expected to be undefined.
     */
    public boolean isUndefine() {
        return isToUndefine;
    }

    /**
     * Defines there is a value for this attribute.<br/>
     * It does <b>not</b> consider if this attribute should or should not be undefined.
     */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * Defines there is no value for this attribute.<br/>
     * It does <b>not</b> consider if this attribute should or should not be undefined.
     */
    public boolean hasNoValue() {
        return value == null;
    }

    /**
     * Declares there is no value and attribute state is not ordering
     * for attribute to be undefined.<br/>
     * Basically this attribute is empty in state 'nothing to do'.
     */
    public boolean isAbsent() {
        return hasNoValue() && !isUndefine();
    }
}
