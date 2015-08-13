package org.wildfly.extras.creaper.commands.domain;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.admin.DomainAdministration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a pair of online commands to backup and then restore running state of servers in domain. The {@code backup}
 * command must be applied before {@code restore}, and both commands can only be applied once. If any one of these rules
 * is violated, an exception is thrown. For special circumstances, when the backup that was already acquired is
 * no longer needed and is not going to be restored, a {@code destroy} command is provided. If there was no backup
 * acquired, the destroy command does nothing.
 */
public final class ServersRunningStateBackup {
    private Map<String, List<String>> runningServersOnHosts; // null <=> backup wasn't acquired, can't restore

    private final OnlineCommand backupPart = new OnlineCommand() {
        @Override
        public void apply(OnlineCommandContext ctx) throws CommandFailedException, IOException {
            if (!ctx.options.isDomain) {
                throw new CommandFailedException(this.toString() + " only makes sense in domain");
            }
            if (ServersRunningStateBackup.this.runningServersOnHosts != null) {
                throw new CommandFailedException("Servers state was already backed up");
            }

            DomainAdministration domainAdministration = new DomainAdministration(ctx.client);
            Map<String, List<String>> runningServers = new HashMap<String, List<String>>();
            for (String host : domainAdministration.hosts()) {
                runningServers.put(host, domainAdministration.allRunningServers());
            }
            ServersRunningStateBackup.this.runningServersOnHosts = runningServers;
        }

        @Override
        public String toString() {
            return "ServersRunningStateBackup.backup";
        }
    };

    private final OnlineCommand restorePart = new OnlineCommand() {
        @Override
        public void apply(OnlineCommandContext ctx) throws CommandFailedException, IOException {
            if (!ctx.options.isDomain) {
                throw new CommandFailedException(this.toString() + " only makes sense in domain");
            }
            if (ServersRunningStateBackup.this.runningServersOnHosts == null) {
                throw new CommandFailedException("There is no servers state backup to restore");
            }

            DomainAdministration domainAdministration = new DomainAdministration(ctx.client);
            for (String host : domainAdministration.hosts()) {
                List<String> servers = domainAdministration.allServers(host);
                for (String server : servers) {
                    if (runningServersOnHosts.containsKey(host) && runningServersOnHosts.get(host).contains(server)) {
                        domainAdministration.startServer(host, server);
                    } else {
                        domainAdministration.stopServer(host, server);
                    }
                }
            }
            ServersRunningStateBackup.this.runningServersOnHosts = null;
        }

        @Override
        public String toString() {
            return "ServersRunningStateBackup.restore";
        }
    };

    private final OnlineCommand destroyPart = new OnlineCommand() {
        @Override
        public void apply(OnlineCommandContext ctx) throws CommandFailedException {
            if (!ctx.options.isDomain) {
                throw new CommandFailedException(this.toString() + " only makes sense in domain");
            }

            ServersRunningStateBackup.this.runningServersOnHosts = null;
        }

        @Override
        public String toString() {
            return "ServersRunningStateBackup.destroy";
        }
    };

    public OnlineCommand backup() {
        return backupPart;
    }

    public OnlineCommand restore() {
        return restorePart;
    }

    public OnlineCommand destroy() {
        return destroyPart;
    }
}
