package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;

import java.io.IOException;

/**
 * This is <b>not</b> supposed to be used directly (hence the long name and not {@code public}). It's here mainly
 * to ensure that the set of common operations provided by {@link Operations} and {@link Batch} is always consistent.
 */
interface SharedCommonOperations<T> {
    T whoami() throws IOException;

    T readAttribute(Address address, String attributeName, ReadAttributeOption... options) throws IOException;

    T writeAttribute(Address address, String attributeName, boolean attributeValue) throws IOException;

    T writeAttribute(Address address, String attributeName, int attributeValue) throws IOException;

    T writeAttribute(Address address, String attributeName, long attributeValue) throws IOException;

    T writeAttribute(Address address, String attributeName, String attributeValue) throws IOException;

    T writeAttribute(Address address, String attributeName, ModelNode attributeValue) throws IOException;

    T writeListAttribute(Address address, String attributeName, boolean... attributeValue) throws IOException;

    T writeListAttribute(Address address, String attributeName, int... attributeValue) throws IOException;

    T writeListAttribute(Address address, String attributeName, long... attributeValue) throws IOException;

    T writeListAttribute(Address address, String attributeName, String... attributeValue) throws IOException;

    T writeListAttribute(Address address, String attributeName, ModelNode... attributeValue) throws IOException;

    T undefineAttribute(Address address, String attributeName) throws IOException;

    T readResource(Address address, ReadResourceOption... options) throws IOException;

    T readChildrenNames(Address address, String childType) throws IOException;

    T add(Address address) throws IOException;

    @Deprecated
    T add(Address address, Parameters parameters) throws IOException;

    T add(Address address, Values parameters) throws IOException;

    T remove(Address address) throws IOException;

    T invoke(String operationName, Address address) throws IOException;

    @Deprecated
    T invoke(String operationName, Address address, Parameters parameters) throws IOException;

    T invoke(String operationName, Address address, Values parameters) throws IOException;
}
