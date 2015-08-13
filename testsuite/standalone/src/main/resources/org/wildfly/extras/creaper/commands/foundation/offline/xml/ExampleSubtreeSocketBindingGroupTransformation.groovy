socketBindingGroup."socket-binding".find { it.@name == "http" }.@port = "10080"
socketBindingGroup."outbound-socket-binding".find { it.@name == "mail-smtp" }."remote-destination".@port = "587"

socketBindingGroup."socket-binding" + {
    "socket-binding"(name: "https", port: "10443")
}
