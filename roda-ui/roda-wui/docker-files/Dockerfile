FROM tomcat:9-jre11
LABEL maintainer="admin@keep.pt" vendor="KEEP SOLUTIONS"

# Install dependencies
RUN set -ex; \
    apt-get -qq update && \
    apt-get -qq -y install software-properties-common; \
    apt-get -qq -y --no-install-recommends install clamdscan cron supervisor zip rsync jq && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY supervisor-conf.d/* /etc/supervisor/conf.d/

COPY clamd.conf /etc/clamav/clamd.conf

# setup remove old ROOT folder from tomcat
RUN set -ex; \
    rm -rf /usr/local/tomcat/webapps/ROOT

# Install web application
COPY /ROOT /usr/local/tomcat/webapps/ROOT

# Fix configuration & do some final cleanup
RUN set -ex; \
    unzip -q /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/roda-core-*.jar config/roda-core.properties && \
    sed -i -e 's/^core.plugins.internal.virus_check.clamav/#&/' -e 's/^core.tools.siegfried.mode/#&/' config/roda-core.properties && \
    echo "\n" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.bin = /usr/bin/clamdscan" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.params = -m --fdpass" >> config/roda-core.properties && \
    echo "core.plugins.internal.virus_check.clamav.get_version = clamdscan --version" >> config/roda-core.properties && \
    echo "core.tools.siegfried.mode = server" >> config/roda-core.properties && \
    zip -q /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/roda-core-*.jar config/roda-core.properties ; \
    apt-get remove -y curl zip && apt-get clean && apt-get autoremove ; \
    rm -rf /var/lib/apt/lists/*

ENV RODA_HOME=/roda

COPY /docker-entrypoint.sh /
COPY /docker-entrypoint.d/* /docker-entrypoint.d/

# Work-around to achieve optional copy
ONBUILD COPY /docker-entrypoint.d/* Dockerfile /docker-entrypoint.d/
ONBUILD RUN rm /docker-entrypoint.d/Dockerfile

ENTRYPOINT ["/docker-entrypoint.sh"]
