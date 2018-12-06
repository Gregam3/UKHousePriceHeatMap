## Run Locally

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

## Launch To server

1. Master and Development should automatically be pushed to the docker repository found at https://hub.docker.com/u/asegroup1/
2. Branch can be manually pushed by runing a travis build on any non master or development branch or by following the insturctions below
3. Running `mvn package`
4. `docker build -t asegroup1/backend-branch:latest .`
5. `docker push asegroup1/backend-branch:latest`
6. repositories can be launched on the server by running `docker run -d -p 8000:8080 --env-file enviromentVariables.list --name branch asegroup1/backend-branch:latest`

edit
