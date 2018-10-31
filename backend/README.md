# Server Running Instructions

1. Install Maven
	1. Download maven: https://maven.apache.org/download.cgi
	2. Install maven: https://maven.apache.org/install.html
2. Call `mvn spring-boot:run` in the backend folder
3. Launch Swagger UI: http://localhost:8080/swagger-ui.html#/

## Launching to server example
1. Fill in the enviroment variable details in the `\backend\src\main\resources\application.properties`
2. Call `mvn package` in the backend folder
3. Copy the .jar file from the target folder to the server
4. install the app as a service `sudo ln -s /home/ec2-user/development/locationReciever-3.0.jar /etc/init.d/locationReciever` https://docs.spring.io/spring-boot/docs/1.3.0.BUILD-SNAPSHOT/reference/htmlsingle/#deployment-initd-service
5. enable access to the service `sudo chmod 755 /etc/init.d/locationReciever` and `sudo chown root:root /etc/init.d/locationReciever` https://askubuntu.com/a/335251
6. Start the server using `sudo /etc/init.d/locationReciever start`