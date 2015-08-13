management."management-interfaces" << {
    "http-interface"("security-realm": "ManagementRealm") {
        socket(interface: "management", port: "\${jboss.management.http.port:9990}")
    }
}
