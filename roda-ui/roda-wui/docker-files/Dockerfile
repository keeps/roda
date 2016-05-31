FROM tomcat:8.0-jre8
MAINTAINER hsilva@keep.pt

RUN rm -rf /usr/local/tomcat/webapps/ROOT

RUN apt-get update && apt-get install curl supervisor -y && curl https://bintray.com/user/downloadSubjectPublicKey?username=bintray | apt-key add - && echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | tee -a /etc/apt/sources.list && apt-get update && apt-get install clamav clamav-daemon siegfried -y

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY clamd.conf /etc/clamav/clamd.conf

RUN freshclam

ADD /roda-wui-2.0.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

VOLUME /root/.roda

CMD ["/usr/bin/supervisord"]