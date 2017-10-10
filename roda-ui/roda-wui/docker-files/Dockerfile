FROM tomcat:8.0-jre8
LABEL maintainer="admin@keep.pt"

# Install dependencies
RUN curl -s https://bintray.com/user/downloadSubjectPublicKey?username=bintray | \
    apt-key add - && echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | \
    tee -a /etc/apt/sources.list && apt-get -qq update && \
    apt-get -qq -y --no-install-recommends install clamav clamav-daemon clamdscan siegfried supervisor zip \
    libav-tools imagemagick sox libsox-fmt-all ghostscript libgs-dev \
    libreoffice-common unoconv libreoffice-writer libreoffice-draw libreoffice-calc libreoffice-impress cpio libgcrypt20 libnss3 libreoffice-pdfimport \
    exiftool python-jpylyzer mediainfo && apt-get clean && rm -rf /var/lib/apt/lists/*

ADD supervisord.conf /etc/supervisor/conf.d/supervisord.conf
ADD clamd.conf /etc/clamav/clamd.conf

# setup clamav, siegfried & remove old ROOT folder from tomcat
RUN mkdir -p /var/run/clamav && chown clamav /var/run/clamav && freshclam ; \
	sf -update ; \
	rm -rf /usr/local/tomcat/webapps/ROOT

# Install web application
ADD /ROOT /usr/local/tomcat/webapps/ROOT
ADD roda-cli.sh /usr/local/bin/roda-cli.sh

# Fix configuration & do some final cleanup
RUN unzip -q /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/roda-core-*.jar config/roda-core.properties && \
    sed -i -e 's/^core.plugins.internal.virus_check.clamav/#&/' -e 's/^core.tools.siegfried.mode/#&/' config/roda-core.properties && \
    echo "\n" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.bin = /usr/bin/clamdscan" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.params = -m --fdpass" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.get_version = clamdscan --version" >> config/roda-core.properties && \
    echo "core.tools.siegfried.mode = server" >> config/roda-core.properties && \
    zip /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/roda-core-*.jar config/roda-core.properties ; \
    apt-get remove -y curl zip && apt-get clean && apt-get autoremove ; \
    rm -rf /var/lib/apt/lists/*

ENV RODA_HOME=/roda
ADD setenv.sh /usr/local/tomcat/bin/setenv.sh
VOLUME /roda

ADD /docker-entrypoint.sh /
ADD /docker-entrypoint.d/* /docker-entrypoint.d/
ONBUILD ADD /docker-entrypoint.d/* /docker-entrypoint.d/

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["catalina.sh", "run"]
