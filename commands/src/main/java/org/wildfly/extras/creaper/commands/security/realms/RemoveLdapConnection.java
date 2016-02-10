package org.wildfly.extras.creaper.commands.security.realms;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Remove given LDAP outbound connection.
 *
 * <b>Can move the server to reload-required state!</b>
 */
public final class RemoveLdapConnection implements OnlineCommand, OfflineCommand {

    private final String connectionName;

    public RemoveLdapConnection(String connectionName) {
        if (connectionName == null) {
            throw new IllegalArgumentException("Name of the ldap connection must be specified as non null value");
        }
        if (connectionName.isEmpty()) {
            throw new IllegalArgumentException("Name of the ldap connection must not be empty value");
        }
        this.connectionName = connectionName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        ops.remove(Address.coreService("management").and("ldap-connection", connectionName));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(RemoveLdapConnection.class)
                .subtree("management", Subtree.management())
                .parameter("atrLdapConnectionName", connectionName)
                .build());
    }

}
