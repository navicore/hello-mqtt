FROM java:8-alpine

MAINTAINER Ed Sweeney <ed@onextent.com>

RUN mkdir -p /app

COPY target/scala-2.12/*.jar /app/

WORKDIR /app

ENTRYPOINT ["java","-cp", "/app/HelloMqtt.jar"]
