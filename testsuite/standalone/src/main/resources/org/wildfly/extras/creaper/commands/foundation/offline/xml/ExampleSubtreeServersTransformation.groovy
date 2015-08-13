servers.server.find { it.@name == "server-two" }."socket-bindings"."@port-offset" = "100"

servers << {
    server(name: "server-three", group: "other-server-group") {
        "socket-bindings"("port-offset": "200")
    }
}
