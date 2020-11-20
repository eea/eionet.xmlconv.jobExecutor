## Instructions to run application locally

JobExecutorApplication -> right click -> Run

### Installation Guide
Create application-dev.properties file by copying the contents of application-prod.properties.

### Build the jar
#### You can either select the profile by maven profiles tab and run 
$ mvn clean install

#### or use the -P flag along with the profile id (dev or prod)
e.g. mvn -Pdev clean install 

## Docker commands and actions

### Build image and deploy to a docker repo 
#### First way
-inside app root directory run dockerhub.sh file

#### Second way
- inside app root directory run the following commands after changing build time

$ docker build -t eworxeea/xmlconv_job_executor:2020-27-10T1500 .  <br>
$ docker push eworxeea/xmlconv_job_executor:2020-27-10T1500   

