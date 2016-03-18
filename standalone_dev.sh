# Maven script that compiles application and runs it with context path '/sso' in developer mode with port 8765 for debugging. 
# Open: http://localhost:8080/sso
mvn -Dmaven.test.skip=true -Dninja.jvmArgs="-Djava.net.preferIPv6Addresses=true -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8765,suspend=n -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" -Dninja.context=/sso compile ninja:run 
