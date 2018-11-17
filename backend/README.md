## With Docker
1. Install Maven (See Above)
2. Install Docker 
	1. For Windows
		1. Follow instructions at https://docs.docker.com/docker-for-windows/install/ (Note for some version of winodws the docker tool box is needed)
	2. For Linux 
		1. Follow instructions at https://docs.docker.com/install/linux/docker-ce/ubuntu/
3. Fill in the enviroment variable details in the `\backend\src\main\resources\application.properties`
4. Call `mvn package` in the backend folder
5. Call `docker build -t <<tagName>>:<<tagVersion>> .` replacing `<<tagName>>:<<tagVersion>>` with Somthing relevant.
6. Call `docker run -p 8080:8080 <<tagName>>:<<tagVersion>>` This will run the application on port http://localhost:8080

## Launching to server example
1. Fill in the environment variable details in the `\backend\src\main\resources\application.properties`
2. Call `mvn package` in the backend folder
3. Copy the .jar file from the target folder to the server
4. install the app as a service `sudo ln -s /home/ec2-user/development/locationReciever-3.0.jar /etc/init.d/locationReciever` https://docs.spring.io/spring-boot/docs/1.3.0.BUILD-SNAPSHOT/reference/htmlsingle/#deployment-initd-service
5. enable access to the service `sudo chmod 755 /etc/init.d/locationReciever` and `sudo chown root:root /etc/init.d/locationReciever` https://askubuntu.com/a/335251
6. Start the server using `sudo /etc/init.d/locationReciever start`

## With Docker
1. Fill in the enviroment variable details in the `\backend\src\main\resources\application.properties`
2. Call `mvn package` in the backend folder
3. Call `docker build -t backend/<<BranchName>>:<BranchVersion>> .` replacing `<<BranchName>>` with the first two parts of the branch name and `<<BranchVersion>>` with the branch version (you can use `latest` if you are unsure).
4. Call `docker save backend/<<BranchName>>:<BranchVersion>> > backend-<<BranchName>>:<BranchVersion>>.tar`
5. Copy the tar file to the server
6. Load the application to the servers docker application using `docker image load -i backend-<<BranchName>>:<BranchVersion>>.tar`
7. Run the image in a container using `docker run -p <<portNo>>:8080 backend/<<BranchName>>:<BranchVersion>>` Replacing `<<portNo>>` with the port you wish to expose the application on. Note only three ports are available 80(Reserved for the master launches), 8080(reserved for development launches) and 8000(For testing out branches on the server).
6. Start the server using `sudo /etc/init.d/locationReciever start`