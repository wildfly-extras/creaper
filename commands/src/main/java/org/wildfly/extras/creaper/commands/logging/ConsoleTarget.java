package org.wildfly.extras.creaper.commands.logging;

public enum ConsoleTarget {
    /** Since WildFly 9. Will cause an error on earlier AS7/WildFly versions! */
    CONSOLE("console"),
    STDOUT("System.out"),
    STDERR("System.err");

    private final String value;

    ConsoleTarget(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
