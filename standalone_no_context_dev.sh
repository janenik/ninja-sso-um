# Maven script that compiles application and runs it without context path in developer mode with port 8765 for debugging. 
# Open: http://localhost:8080/
mvn -Dmaven.test.skip=true -Dninja.jvmArgs="-Djava.net.preferIPv6Addresses=true -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8765,suspend=n -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" compile ninja:run 
