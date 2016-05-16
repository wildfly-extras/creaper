package org.wildfly.extras.creaper.commands.foundation.online;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * <p>Apply a list of CLI operations (a CLI script) read from a file. The file is treated as UTF-8 text. If you already
 * have the script text as a {@code String}, use the {@link CliScript} command.</p>
 *
 * <p>See the documentation of {@link CliScript} for more information about special handling of certain operations.</p>
 */
public final class CliFile implements OnlineCommand {
    private final CharSource source;
    private final String description;

    /**
     * Apply a CLI script from the filesystem ({@code file}).
     * @param file an existing file on the filesystem
     * @throws IllegalArgumentException if {@code file} is {@code null} or doesn't exist
     */
    public CliFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File must be provided");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File doesn't exist: " + file);
        }

        this.source = Files.asByteSource(file).asCharSource(Charsets.UTF_8);
        this.description = file.toString();
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
            throw new IllegalArgumentException("A class of the script must be provided");
        }

        String path = clazz.getSimpleName() + ".cli";
        URL url = Resources.getResource(clazz, path);
        this.source = Resources.asByteSource(url).asCharSource(Charsets.UTF_8);
        this.description = path;
    }

    /**
     * Apply a CLI script from the classpath. The script is loaded from classpath at {@code path}. The resource
     * will be loaded by the {@code resourceLoader} class ({@link Class#getResourceAsStream(String)}).
     * @param resourceLoader class that will be used to load the script from classpath
     * @param path path to the script on classpath (absolute or relative to {@code resourceLoader})
     * @throws IllegalArgumentException if the {@code resourceLoader} or {@code path} is {@code null}
     */
    public CliFile(Class resourceLoader, String path) {
        if (resourceLoader == null) {
            throw new IllegalArgumentException("A class for loading the script must be provided");
        }
        if (path == null) {
            throw new IllegalArgumentException("A path to the script must be provided");
        }

        URL url = Resources.getResource(resourceLoader, path);
        this.source = Resources.asByteSource(url).asCharSource(Charsets.UTF_8);
        this.description = path;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CliException, CommandFailedException {
        ctx.client.apply(new CliScript(source.read(), description));
    }

    @Override
    public String toString() {
        return "CliFile " + description;
    }
}
