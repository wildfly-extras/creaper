package org.wildfly.extras.creaper.commands.auditlog;

public enum SyslogFacilityType {

    CLOCK_DAEMON("CLOCK_DAEMON"),
    CLOCK_DAEMON2("CLOCK_DAEMON2"),
    FTP_DAEMON("FTP_DAEMON"),
    KERNEL("KERNEL"),
    LOCAL_USE_0("LOCAL_USE_0"),
    LOCAL_USE_1("LOCAL_USE_1"),
    LOCAL_USE_2("LOCAL_USE_2"),
    LOCAL_USE_3("LOCAL_USE_3"),
    LOCAL_USE_4("LOCAL_USE_4"),
    LOCAL_USE_5("LOCAL_USE_5"),
    LOCAL_USE_6("LOCAL_USE_6"),
    LOCAL_USE_7("LOCAL_USE_7"),
    LOG_ALERT("LOG_ALERT"),
    LOG_AUDIT("LOG_AUDIT"),
    LINE_PRINTER("LINE_PRINTER"),
    MAIL_SYSTEM("MAIL_SYSTEM"),
    NETWORK_NEWS("NETWORK_NEWS"),
    NTP("NTP"),
    SECURITY("SECURITY"),
    SECURITY2("SECURITY2"),
    SYSLOGD("SYSLOGD"),
    SYSTEM_DAEMONS("SYSTEM_DAEMONS"),
    USER_LEVEL("USER_LEVEL"),
    UUCP("UUCP");

    private final String value;

    SyslogFacilityType(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
