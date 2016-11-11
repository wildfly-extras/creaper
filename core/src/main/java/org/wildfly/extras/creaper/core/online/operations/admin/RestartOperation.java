package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

interface RestartOperation {
    ModelNodeResult perform(Operations ops, Address address) throws IOException;
}
