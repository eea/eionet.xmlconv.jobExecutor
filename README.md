## Installation Guide 
- Create application-dev.properties file by copying the contents of application-prod.properties file. 

- Set integer rancher.jobExecutor.type according to the enum JobExecutorType: 0 for Light, 1 for Heavy, 2 for Unknown, 3 for Sync_fme, 4 for Async_fme

- If you want to connect jobExecutor with the database 
1. Create a database connection using docker command <br>
$ docker run --name jobExecutor -p 3314:3306 -e MYSQL_ROOT_PASSWORD=yourPassword mysql:8.0.28
2. Set existing property spring.autoconfigure.exclude with no value
<pre>
    spring.autoconfigure.exclude=
</pre>
3. Connect jobExecutor with the database setting the following properties
<pre>
spring.datasource.url=jdbc:mysql://localhost:3314/jobExecutor?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourPassword
</pre>

### Instructions to run application locally
- dev profile (matches application-dev.properties file) is activated by default, so run <br>
$ mvn clean install

- or if you want to use prod profile (matches application-prod.properties), specify it at build time using the -P flag <br>
$ mvn -Pprod clean install

- Add an environment variable logFilePath with value a path to a folder where log files will be stored. The variable is used in logback-spring.xml.

- Add an environment variable queryLogRetentionDays with value the duration in days for which log files will be retained. Log files older than the specified duration will be deleted. The variable is used in logback-spring.xml.

- JobExecutorApplication -> right click -> Run

## Docker commands and actions
### Build image and deploy to a docker repo 
inside app root directory 

- run dockerhub.sh file

- or run the following commands after changing build time

$ docker build -t eworxeea/xmlconv_job_executor:2020-27-10T1500 .  <br>
$ docker push eworxeea/xmlconv_job_executor:2020-27-10T1500   

