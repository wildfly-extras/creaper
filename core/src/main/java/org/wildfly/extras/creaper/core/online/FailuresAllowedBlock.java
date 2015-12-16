package org.wildfly.extras.creaper.core.online;

import java.io.Closeable;

/**
 * <p>The only function this interface exposes is the ability to end a block of code started by
 * {@link OnlineManagementClient#allowFailures()}:</p>
 *
 * <pre>
 * FailuresAllowedBlock allowFailures = client.allowFailures();
 * try {
 *     ModelNodeResult result = client.execute(...);
 *     ...
 * } finally {
 *     allowFailures.close();
 * }
 * </pre>
 *
 * <p>If using Java 7 and above, a try-with-resources block will make the code shorter:</p>
 *
 * <pre>
 * try (FailuresAllowedBlock allowFailures = client.allowFailures()) {
 *     ModelNodeResult result = client.execute(...);
 *     ...
 * }
 * </pre>
 */
public interface FailuresAllowedBlock extends Closeable {
}
