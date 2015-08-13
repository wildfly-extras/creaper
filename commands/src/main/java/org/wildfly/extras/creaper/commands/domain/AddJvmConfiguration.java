package org.wildfly.extras.creaper.commands.domain;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * Command which creates new jvm configuration as child of specified parent
 */
public class AddJvmConfiguration implements OnlineCommand {
    private final Address parentAddress;
    private final String jvmName;
    private final String maxHeapSize;
    private final String heapSize;
    private final String permgenSize;
    private final String maxPermgenSize;
    private final String javaHome;
    private final JvmType type;

    public enum JvmType { SUN, IBM, OTHER }

    private AddJvmConfiguration(Builder builder) {
        this.parentAddress = builder.parentAddress;
        this.jvmName = builder.jvmName;
        this.maxHeapSize = builder.maxHeapSize;
        this.heapSize = builder.heapSize;
        this.permgenSize = builder.permgenSize;
        this.maxPermgenSize = builder.maxPermgenSize;
        this.javaHome = builder.javaHome;
        this.type = builder.type;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CommandFailedException, IOException {
        if (!ctx.client.options().isDomain) {
            throw new CommandFailedException("This command only makes sense in domain");
        }

        Operations ops = new Operations(ctx.client);
        ops.add(parentAddress.and("jvm", jvmName), Values.empty()
                .andOptional("max-heap-size", maxHeapSize)
                .andOptional("heap-size", heapSize)
                .andOptional("permgen-size", permgenSize)
                .andOptional("max-permgen-size", maxPermgenSize)
                .andOptional("java-home", javaHome)
                .andOptional("type", type != null ? type.toString() : null));
    }

    public static final class Builder {
        private String jvmName;
        private String maxHeapSize;
        private String heapSize;
        private String permgenSize;
        private String maxPermgenSize;
        private String javaHome;
        private JvmType type;
        private Address parentAddress;

        /**
         * @param parentAddress parent for jvm configuraton, e.g. {@code server-group} or {@code server-config}
         * @param jvmName name of new created jvm configuration
         * @throws IllegalArgumentException if the {@code parentAddress} or {@code jvmName} is {@code null}
         */
        public Builder(Address parentAddress, String jvmName) {
            if (parentAddress == null) {
                throw new IllegalArgumentException("parentAddress must be specified as non null value");
            }
            if (jvmName == null) {
                throw new IllegalArgumentException("Name of the jvm must be specified as non null value");
            }
            this.parentAddress = parentAddress;
            this.jvmName = jvmName;
        }

        /**
         * The initial heap size allocated by the JVM
         */
        public Builder heapSize(String heapSize) {
            this.heapSize = heapSize;
            return this;
        }

        /**
         * The maximum heap size that can be allocated by the JVM
         */
        public Builder maxHeapSize(String maxHeapSize) {
            this.maxHeapSize = maxHeapSize;
            return this;
        }

        /**
         * The initial permanent generation size
         */
        public Builder permgenSize(String permgenSize) {
            this.permgenSize = permgenSize;
            return this;
        }

        /**
         * The maximum size of the permanent generation
         */
        public Builder maxPermgenSize(String maxPermgenSize) {
            this.maxPermgenSize = maxPermgenSize;
            return this;
        }

        /**
         * The JVM type can be either {@code SUN} or {@code IBM}. Using the {@code OTHER} value is undocumented.
         */
        public Builder type(JvmType type) {
            this.type = type;
            return this;
        }

        /**
         * The java home
         */
        public Builder javaHome(String javaHome) {
            this.javaHome = javaHome;
            return this;
        }

        public AddJvmConfiguration build() {
            return new AddJvmConfiguration(this);
        }
    }
}
