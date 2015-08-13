domainController.local = {
    remote(host: "\${jboss.domain.master.address}", port: "\${jboss.domain.master.port:9999}", "security-realm": "ManagementRealm")
}
