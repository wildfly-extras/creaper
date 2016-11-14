package org.wildfly.extras.creaper.test;


/**
 * Category marker for tests that require WildFly 11 and can't run with prior
 * versions of server.
 *
 * <p>
 * WildFly11Tests category is meant as a "subset" of WildFlyTests. If test is in
 * category WildFly11Tests, it also has to be in category WildFlyTests.
 * </p>
 */
public interface WildFly11Tests {
}
