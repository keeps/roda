FROM java:8
MAINTAINER hsilva@keep.pt
EXPOSE 8080

ADD /roda-wui-2.0.0-SNAPSHOT.war //

RUN wget http://mirrors.fe.up.pt/pub/apache/tomcat/tomcat-8/v8.0.30/bin/apache-tomcat-8.0.30.tar.gz
RUN tar xzf apache-tomcat-8.0.30.tar.gz && rm apache-tomcat-8.0.30.tar.gz
RUN rm -rf /apache-tomcat-8.0.30/webapps/*
RUN mv /roda-wui-2.0.0-SNAPSHOT.war /apache-tomcat-8.0.30/webapps/ROOT.war
