package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.Constants;

public interface ReadResourceOption {
    ReadResourceOption INCLUDE_DEFAULTS = new ReadResourceOptionInternal() {
        @Override
        void modifyReadResourceOperation(ModelNode op) {
            op.get(Constants.INCLUDE_DEFAULTS).set(true);
        }
    };

    ReadResourceOption NOT_INCLUDE_DEFAULTS = new ReadResourceOptionInternal() {
        @Override
        void modifyReadResourceOperation(ModelNode op) {
            op.get(Constants.INCLUDE_DEFAULTS).set(false);
        }
    };

    ReadResourceOption RECURSIVE = new ReadResourceOptionInternal() {
        @Override
        void modifyReadResourceOperation(ModelNode op) {
            op.get(Constants.RECURSIVE).set(true);
        }
    };

    ReadResourceOption NOT_RECURSIVE = new ReadResourceOptionInternal() {
        @Override
        void modifyReadResourceOperation(ModelNode op) {
            op.get(Constants.RECURSIVE).set(false);
        }
    };

    ReadResourceOption INCLUDE_RUNTIME = new ReadResourceOptionInternal() {
        @Override
        void modifyReadResourceOperation(ModelNode op) {
            op.get(Constants.INCLUDE_RUNTIME).set(true);
        }
    };

    ReadResourceOption NOT_INCLUDE_RUNTIME = new ReadResourceOptionInternal() {
        @Override
        void modifyReadResourceOperation(ModelNode op) {
            op.get(Constants.INCLUDE_RUNTIME).set(false);
        }
    };

    final class RecursiveDepth {
        private RecursiveDepth() {} // avoid instantiation

        public static ReadResourceOption of(final int depth) {
            return new ReadResourceOptionInternal() {
                @Override
                void modifyReadResourceOperation(ModelNode op) {
                    op.get(Constants.RECURSIVE_DEPTH).set(depth);
                }
            };
        }
    }
}

