def root = new XmlSlurper(false, false).parse(file)

root.foo = {
    bar()
}

new StreamingMarkupBuilder().bindNode(root).writeTo(file.newWriter())
