jvms.jvm.find { it.@name == "default" }.heap.@size = "128m"

jvms << {
    jvm(name: "foobar") {
        heap(size: "1024m", "max-size": "2048m")
        permgen(size: "512m", "max-size": "512m")
        "jvm-options" {
            option(value: "-server")
            option(value: "-XX:+UseConcMarkSweepGC")
        }
    }
}
