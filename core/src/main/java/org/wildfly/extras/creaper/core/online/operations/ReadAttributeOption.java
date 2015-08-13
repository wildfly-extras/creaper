package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.Constants;

public interface ReadAttributeOption {
    ReadAttributeOption INCLUDE_DEFAULTS = new ReadAttributeOptionInternal() {
        @Override
        void modifyReadAttributeOperation(ModelNode op) {
            op.get(Constants.INCLUDE_DEFAULTS).set(true);
        }
    };

    ReadAttributeOption NOT_INCLUDE_DEFAULTS = new ReadAttributeOptionInternal() {
        @Override
        void modifyReadAttributeOperation(ModelNode op) {
            op.get(Constants.INCLUDE_DEFAULTS).set(false);
        }
    };
}

