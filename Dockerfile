FROM java:8

EXPOSE 8080

RUN mkdir -p /app

COPY target/scala-2.11/*.jar /app/

WORKDIR /app

CMD java -jar ./akkaHttpAmqpConsumer.jar
# override CMD from your run command, or k8s yaml, or marathon json, etc...

