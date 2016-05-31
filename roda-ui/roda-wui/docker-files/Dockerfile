FROM tomcat:8.0-jre8
MAINTAINER hsilva@keep.pt

RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Install dependencies
RUN curl https://bintray.com/user/downloadSubjectPublicKey?username=bintray | \
 apt-key add - && echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | \
 tee -a /etc/apt/sources.list && apt-get update && \
 apt-get install clamav clamav-daemon siegfried curl supervisor -y

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY clamd.conf /etc/clamav/clamd.conf
RUN mkdir -p /var/run/clamav && chown clamav /var/run/clamav

RUN freshclam

ADD /roda-wui-2.0.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

VOLUME /root/.roda

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
