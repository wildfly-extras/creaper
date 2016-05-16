package org.wildfly.extras.creaper.commands.foundation.online;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.io.CharSource;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

/**
 * <p>Apply a list of CLI operations (a CLI script), provided as a {@code String}. If you want to read the script
 * from a file or a classpath resource, use the {@link CliFile} command.</p>
 *
 * <p>The {@code connect} operations in the script are handled specially: bare {@code connect} operations without
 * arguments are simply ignored, as we are already connected, and {@code connect host:port} operations (with an argument
 * that specifies the host and port to connect to) are considered a failure. The script is scanned for the forbidden
 * {@code connect} operations <i>before</i> is it executed, so if this error happens, no operation from the script
 * has been performed yet.</p>
 */
public final class CliScript implements OnlineCommand {
    private final String script;
    private final String description;

    /**
     * Apply the given CLI {@code script}.
     * @param script the text of the CLI script
     */
    public CliScript(String script) {
        this(script, null);
    }

    /**
     * Apply the given CLI {@code script}.
     * @param script the text of the CLI script
     * @param description human-readable description of the script to use for {@code toString}; may be {@code null}
     */
    CliScript(String script, String description) {
        if (script == null) {
            throw new IllegalArgumentException("The script text must be provided");
        }

        this.script = script;
        this.description = description;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Iterable<String> lines = CharSource.wrap(script).readLines();
        lines = Iterables.filter(lines, Predicates.not(Predicates.containsPattern("^\\s*connect\\s*$")));

        if (Iterables.any(lines, Predicates.containsPattern("^\\s*connect"))) {
            throw new CommandFailedException("The script contains an unsupported 'connect' operation");
        }

        for (String line : lines) {
            ctx.client.executeCli(line.trim());
        }
    }

    @Override
    public String toString() {
        if (description == null) {
            return "CliScript";
        } else {
            return "CliScript " + description;
        }
    }
}
