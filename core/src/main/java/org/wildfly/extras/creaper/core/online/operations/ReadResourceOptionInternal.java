package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;

abstract class ReadResourceOptionInternal implements ReadResourceOption {
    abstract void modifyReadResourceOperation(ModelNode op);
}
