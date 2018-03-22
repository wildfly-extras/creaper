/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extras.creaper.commands.elytron.credentialstore;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;

public class AbstractCredentialStoreOnlineTest extends AbstractElytronOnlineTest {

    protected boolean aliasExists(Address credentialStore, String alias) throws IOException {
        Assert.assertNotNull(alias);
        ModelNodeResult result = ops.invoke("read-aliases", credentialStore);
        assertTrue("read-aliases operation must be successful.", result.isSuccess());
        ModelNode modelNode = result.value();
        if (!result.isSuccess() || modelNode.asList().isEmpty()) {
            return false;
        }
        List<ModelNode> aliasList = modelNode.asList();
        for (ModelNode aliasName : aliasList) {
            if (alias.equals(aliasName.asString())) {
                return true;
            }
        }

        return false;
    }

}
