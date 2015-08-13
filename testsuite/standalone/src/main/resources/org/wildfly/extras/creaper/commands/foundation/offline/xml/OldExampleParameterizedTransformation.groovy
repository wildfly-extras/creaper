def root = new XmlSlurper().parse(file)

root.foo = {
    bar(param: parameter)
}

new StreamingMarkupBuilder(useDoubleQuotes: true).bindNode(root).writeTo(file.newWriter())
