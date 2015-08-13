package org.wildfly.extras.creaper.commands.foundation.online;

import com.google.common.base.Charsets;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteSource;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Apply a list of CLI operations (a CLI script) read from a file. The file is treated as UTF-8 text.</p>
 *
 * <p>The {@code connect} operations in the script are handled specially: bare {@code connect} operations without
 * arguments are simply ignored, as we are already connected, and {@code connect host:port} operations (with an argument
 * that specifies the host and port to connect to) are considered a failure. The script is scanned for the forbidden
 * {@code connect} operations <i>before</i> is it executed, so if this error happens, no operation from the script
 * has been performed yet.</p>
 */
public final class CliFile implements OnlineCommand {
    // exactly one is always null and the other is always non-null
    private final File file;
    private final Class clazz;

    /**
     * Apply a CLI script from the filesystem ({@code file}).
     * @param file an existing file on the filesystem
     * @throws IllegalArgumentException if {@code file} is {@code null} or the file doesn't exist
     */
    public CliFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File must be set");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File doesn't exist: " + file);
        }

        this.file = file;
        this.clazz = null;
    }

    /**
     * Apply a CLI script from the classpath. The script must be stored in a file with the same name as
     * the {@code clazz} and have the {@code .cli} extension. On the classpath, the script file must live along
     * the class file.
     * @param clazz the class that will be used for loading the script and also for discovering its name
     * @throws IllegalArgumentException if the {@code clazz} is {@code null}
     */
    public CliFile(Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class must be set");
        }

        this.file = null;
        this.clazz = clazz;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CliException, CommandFailedException {
        Iterable<String> lines = new CliFileByteSource().asCharSource(Charsets.UTF_8).readLines();
        lines = Iterables.filter(lines, Predicates.not(Predicates.containsPattern("^\\s*connect\\s*$")));

        if (Iterables.any(lines, Predicates.containsPattern("^\\s*connect"))) {
            throw new CommandFailedException("The script contains an unsupported 'connect' operation: "
                    + (file != null ? file : (clazz.getSimpleName() + ".cli")));
        }

        for (String line : lines) {
            ctx.client.executeCli(line.trim());
        }
    }

    @Override
    public String toString() {
        return "CliFile " + (file != null ? file : (clazz.getSimpleName() + ".cli"));
    }

    // ---

    private final class CliFileByteSource extends ByteSource {
        @Override
        public InputStream openStream() throws IOException {
            if (file != null) {
                return new FileInputStream(file);
            } else {
                return clazz.getResourceAsStream(clazz.getSimpleName() + ".cli");
            }
        }
    }
}
