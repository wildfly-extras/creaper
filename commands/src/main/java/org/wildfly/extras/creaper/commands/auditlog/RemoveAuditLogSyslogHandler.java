package org.wildfly.extras.creaper.commands.auditlog;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class RemoveAuditLogSyslogHandler implements OnlineCommand, OfflineCommand {

    private final String syslogHandlerName;

    public RemoveAuditLogSyslogHandler(String syslogHandlerName) {
        if (syslogHandlerName == null) {
            throw new IllegalArgumentException("Name of the syslog-handler must be specified as non null value");
        }
        if (syslogHandlerName.isEmpty()) {
            throw new IllegalArgumentException("Name of the syslog handler must not be empty value");
        }

        this.syslogHandlerName = syslogHandlerName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Address handlerAddress = Address.coreService("management")
                .and("access", "audit")
                .and("syslog-handler", syslogHandlerName);

        Operations ops = new Operations(ctx.client);
        ops.remove(handlerAddress);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(RemoveAuditLogSyslogHandler.class)
                .subtree("management", Subtree.management())
                .parameter("atrSyslogHandlerName", syslogHandlerName)
                .build());
    }

}
