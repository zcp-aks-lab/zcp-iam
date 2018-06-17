FROM java:8
ENV SPRING_ACTIVE_PROFILE default
ADD dist/zcp-iam-0.0.1-SNAPSHOT.jar app.jar
VOLUME /tmp
VOLUME /target
#RUN apt-get update && \
#    apt-get install -y net-tools
RUN bash -c 'touch /app.jar'
#CMD echo 'test'
#EXPOSE 8081
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=${SPRING_ACTIVE_PROFILE}","-jar","/app.jar"]

#docker run -d --rm --name skb-account-service \
#           -p 8081:8081 \
#           -e SPRING_ACTIVE_PROFILE=dev \
#           -e REDIS_HOST=169.56.83.54 \
#           -e REDIS_PASSWORD=redis1234 \
#           -e REDIS_PORT=30011 \
#           -e RABBITMQ_HOST=169.56.83.54 \
#           -e RABBITMQ_PORT=31244 \
#           -e RABBITMQ_USERNAME=kks \
#           -e RABBITMQ_PASSWORD=kang7045 \
#           kilsoo75/skb-account-service:0.9 