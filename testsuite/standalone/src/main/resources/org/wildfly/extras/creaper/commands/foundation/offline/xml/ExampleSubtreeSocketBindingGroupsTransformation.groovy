socketBindingGroups << {
    "socket-binding-group"(name: "foobar-sockets", "default-interface": "public") {
        "socket-binding"(name: "http", port: "9090")
        "outbound-socket-binding"(name: "mail-smtp") {
            "remote-destination"(host: "localhost", port: "587")
        }
    }
}
