def datasourceSecurity = datasources.datasources.datasource.find { it."@jndi-name" == "java:jboss/datasources/ExampleDS" }.security
datasourceSecurity."user-name" = "my-user-name"
datasourceSecurity.password = "my-password"

ee."annotation-property-replacement" = "true"

def webCacheContainer = infinispan."cache-container".find { it.@name == "web" }
webCacheContainer."@default-cache" = "dist"
webCacheContainer."replicated-cache".@mode = "SYNC"
webCacheContainer."replicated-cache".@batching = "false"
webCacheContainer."distributed-cache".@mode = "SYNC"
webCacheContainer."distributed-cache".@batching = "false"
