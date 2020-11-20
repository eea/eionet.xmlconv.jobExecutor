## Installation Guide
Create application-dev.properties file by copying the contents of application-prod.properties file. 

### Instructions to run application locally
- Specify profile and build application <br>

You can either select the profile by maven profiles tab and run <br>
$ mvn clean install

or specify profile at build time using the -P flag along with the profile id (dev or prod) <br>
e.g. mvn -Pdev clean install 

- JobExecutorApplication -> right click -> Run

## Docker commands and actions
### Build image and deploy to a docker repo 
#### First way
-inside app root directory run dockerhub.sh file

#### Second way
- inside app root directory run the following commands after changing build time

$ docker build -t eworxeea/xmlconv_job_executor:2020-27-10T1500 .  <br>
$ docker push eworxeea/xmlconv_job_executor:2020-27-10T1500   

