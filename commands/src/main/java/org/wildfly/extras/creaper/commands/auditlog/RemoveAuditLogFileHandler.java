package org.wildfly.extras.creaper.commands.auditlog;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class RemoveAuditLogFileHandler implements OnlineCommand, OfflineCommand {

    private final String fileHandlerName;

    public RemoveAuditLogFileHandler(String fileHandlerName) {
        if (fileHandlerName == null) {
            throw new IllegalArgumentException("Name of the syslog-handler must be specified as non null value");
        }
        if (fileHandlerName.isEmpty()) {
            throw new IllegalArgumentException("Name of the file handler must not be empty value");
        }

        this.fileHandlerName = fileHandlerName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Address handlerAddress = Address.coreService("management")
                .and("access", "audit")
                .and("file-handler", fileHandlerName);

        Operations ops = new Operations(ctx.client);
        ops.remove(handlerAddress);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(RemoveAuditLogFileHandler.class)
                .subtree("management", Subtree.management())
                .parameter("atrFileHandlerName", fileHandlerName)
                .build());
    }
}
