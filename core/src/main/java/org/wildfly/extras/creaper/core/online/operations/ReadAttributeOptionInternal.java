package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;

abstract class ReadAttributeOptionInternal implements ReadAttributeOption {
    abstract void modifyReadAttributeOperation(ModelNode op);
}
