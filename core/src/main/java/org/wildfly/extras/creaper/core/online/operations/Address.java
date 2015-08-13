package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>An address in the management tree. It is a sequence of string pairs ({@code key=value}), possibly empty. This
 * class is immutable and its only public API consists of various ways of <i>creating</i> an address.</p>
 *
 * <p>There are some factory methods for obtaining an initial address:</p>
 * <ul>
 * <li>{@code Address.root()} &ndash; empty address, corresponding to the root of the management tree ({@code /})
 * </li>
 * <li>{@code Address.host("foo")} &ndash; address of host {@code foo} in managed domain ({@code /host=foo})</li>
 * <li>{@code Address.subsystem("foo")} &ndash; address of subsystem {@code foo} ({@code /subsystem=foo})</li>
 * <li>{@code Address.coreService("foo")} &ndash; address of core service {@code foo} ({@code /core-service=foo})</li>
 * <li>{@code Address.of("foo", "bar")} &ndash; address of {@code /foo=bar}</li>
 * </ul>
 *
 * <p>Once you have an initial address, you can chain:</p>
 * <ul>
 * <li>{@code address.and("foo", "bar")} &ndash; address of {@code .../foo=bar}</li>
 * </ul>
 *
 * <p>So creating an address of {@code /foo=bar/baz=quux} looks like this:
 * {@code Address.of("foo", "bar").and("baz", "quux")}</p>
 */
public final class Address {
    private final List<StringPair> address;

    public static Address root() {
        return new Address(Collections.<StringPair>emptyList());
    }

    public static Address host(String host) {
        return Address.of(Constants.HOST, host);
    }

    public static Address subsystem(String subsystemName) {
        return Address.of(Constants.SUBSYSTEM, subsystemName);
    }

    public static Address coreService(String coreServiceName) {
        return Address.of(Constants.CORE_SERVICE, coreServiceName);
    }

    public static Address of(String key, String value) {
        return new Address(Collections.singletonList(new StringPair(key, value)));
    }

    private Address(List<StringPair> address) {
        this.address = Collections.unmodifiableList(address);
    }

    public Address and(String key, String value) {
        List<StringPair> newAddress = new ArrayList<StringPair>(this.address);
        newAddress.add(new StringPair(key, value));
        return new Address(newAddress);
    }

    ModelNode toModelNode() {
        ModelNode result = new ModelNode();
        result.setEmptyList();
        for (StringPair pair : address) {
            result.add(pair.key, pair.value);
        }
        return result;
    }

    @Override
    public String toString() {
        if (address.isEmpty()) {
            return "/";
        }

        StringBuilder result = new StringBuilder();
        for (StringPair addressElement : address) {
            result.append("/").append(addressElement.key).append("=").append(addressElement.value);
        }
        return result.toString();
    }

    private static final class StringPair {
        private final String key;
        private final String value;

        private StringPair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
