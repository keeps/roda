FROM tomcat:8-jre8
LABEL maintainer="admin@keep.pt" vendor="KEEP SOLUTIONS"

# Install gosu
ENV GOSU_VERSION 1.10
RUN set -ex; \
	\
	fetchDeps=' \
		ca-certificates \
		wget \
	'; \
	apt-get update; \
	apt-get install -y --no-install-recommends $fetchDeps; \
	rm -rf /var/lib/apt/lists/*; \
	\
	dpkgArch="$(dpkg --print-architecture | awk -F- '{ print $NF }')"; \
	wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch"; \
	wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch.asc"; \
	\
# verify the signature
	export GNUPGHOME="$(mktemp -d)"; \
	gpg --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4; \
	gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu; \
	rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc; \
	\
	chmod +x /usr/local/bin/gosu; \
# verify that the binary works
	gosu nobody true;

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
VOLUME /roda

ADD /docker-entrypoint.sh /
ADD /docker-entrypoint.d/* /docker-entrypoint.d/
ONBUILD ADD /docker-entrypoint.d/* /docker-entrypoint.d/

ENTRYPOINT ["/docker-entrypoint.sh"]
