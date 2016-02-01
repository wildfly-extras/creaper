def root = new XmlSlurper(false, false).parse(file)

root.foo = {
    bar(param: parameter)
}

new StreamingMarkupBuilder(useDoubleQuotes: true).bindNode(root).writeTo(file.newWriter())
