def root = new XmlSlurper().parse(file)

root.foo = {
    bar()
}

new StreamingMarkupBuilder().bindNode(root).writeTo(file.newWriter())
