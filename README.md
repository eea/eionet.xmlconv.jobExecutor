##Instructions to run application locally

JobExecutorApplication -> right click -> Run

## Docker commands and actions

### Build image and deploy to a docker repo 
####First way
-inside app root directory run dockerhub.sh file

####Second way
-inside app root directory run the following commands

$ mvn clean install <br>
$ docker build -t eworxeea/xmlconv_job_executor:2020-27-10T1500 .  <br>
$ docker push eworxeea/xmlconv_job_executor:2020-27-10T1500   <br>

