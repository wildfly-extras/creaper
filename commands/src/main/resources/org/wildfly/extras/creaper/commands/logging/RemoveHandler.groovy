def handler = logging."${type}".find { it.@name == name }
if (!handler) {
    throw new IllegalStateException("Can't remove $type with name $name as it does not exist in the configuration")
}

handler.replaceNode {}
