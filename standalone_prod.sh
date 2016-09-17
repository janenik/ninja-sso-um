# Maven script that compiles application and runs it with context path '/sso' in production mode. 
# Open: http://localhost:8080/sso
mvn clean install -Dmaven.test.skip=true && java -Dninja.mode=prod -Dninja.context=/sso  -jar target/ninja-sso-um-1.0-SNAPSHOT.jar
