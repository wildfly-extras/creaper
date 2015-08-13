package org.wildfly.extras.creaper.test;

/** Category marker for tests that need to control application server lifecycle manually. */
public interface ManualTests {
    String ARQUILLIAN_CONTAINER = "jboss-manual";
}
