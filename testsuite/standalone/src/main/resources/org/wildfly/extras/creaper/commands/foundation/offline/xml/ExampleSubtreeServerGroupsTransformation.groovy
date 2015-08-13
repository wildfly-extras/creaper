serverGroups << {
    "server-group"(name: "foobar-server-group", profile: "full") {
        jvm(name: "default") {
            heap(size: "1000m", "max-size": "1000m")
            permgen("max-size": "256m")
        }
        "socket-binding-group"(ref: "foobar-sockets")
    }
}
