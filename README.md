## Installation Guide 
Create application-dev.properties file by copying the contents of application-prod.properties file. 

### Instructions to run application locally
- dev profile (matches application-dev.properties file) is activated by default, so run <br>
$ mvn clean install

- or if you want to use prod profile (matches application-prod.properties), specify it at build time using the -P flag <br>
$ mvn -Pprod clean install

- JobExecutorApplication -> right click -> Run

## Docker commands and actions
### Build image and deploy to a docker repo 
inside app root directory 

- run dockerhub.sh file

- or run the following commands after changing build time

$ docker build -t eworxeea/xmlconv_job_executor:2020-27-10T1500 .  <br>
$ docker push eworxeea/xmlconv_job_executor:2020-27-10T1500   

