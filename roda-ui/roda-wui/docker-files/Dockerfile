FROM tomcat:8.0-jre8
MAINTAINER hsilva@keep.pt

# Install dependencies
RUN curl -s https://bintray.com/user/downloadSubjectPublicKey?username=bintray | \
    apt-key add - && echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | \
    tee -a /etc/apt/sources.list && apt-get -qq update && \
    apt-get -qq -y install clamav clamav-daemon siegfried curl supervisor zip

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY clamd.conf /etc/clamav/clamd.conf
RUN mkdir -p /var/run/clamav && chown clamav /var/run/clamav && freshclam

# Install web application
RUN rm -rf /usr/local/tomcat/webapps/ROOT
ADD /ROOT /usr/local/tomcat/webapps/ROOT

# Fix configuration
RUN unzip -q /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/roda-core-*.jar config/roda-core.properties && \
    sed -i -e 's/^core.plugins.internal.virus_check.clamav/#&/' -e 's/^core.tools.siegfried.mode/#&/' config/roda-core.properties && \
    echo "\n" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.bin = /usr/bin/clamdscan" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.params = -m --fdpass" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.get_version = clamdscan --version" >> config/roda-core.properties && \
    echo "core.tools.siegfried.mode = server" >> config/roda-core.properties && \
    zip /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/roda-core-*.jar config/roda-core.properties

VOLUME /root/.roda

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
