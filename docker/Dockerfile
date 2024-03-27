FROM tomcat:10-jre21
LABEL maintainer="admin@keep.pt" vendor="KEEP SOLUTIONS"

# Install dependencies
RUN set -ex; \
    apt-get -qq update && \
    apt-get -qq -y install software-properties-common; \
    apt-get -qq -y --no-install-recommends install clamdscan zip rsync jq && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Remove python dependency
RUN \
	DEBIAN_FRONTEND=noninteractive \
	apt-get autoremove -y --purge *python*

COPY ./docker-files/clamd.conf /etc/clamav/clamd.conf

# setup remove old ROOT folder from tomcat
RUN set -ex; \
    rm -rf /usr/local/tomcat/webapps/ROOT

# Install web application
COPY ./target/  /usr/local/tomcat/webapps/ROOT

# Fix configuration & do some final cleanup
RUN mkdir -p /usr/local/tomcat/conf/Catalina/localhost
RUN chmod -R a+rwx /usr/local/tomcat/conf/Catalina

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

ENV RODA_HOME=/roda \
    SIEGFRIED_MODE=server \
    SIEGFRIED_SERVER_URL=http://siegfried:5138 \
    RODA_USER="roda" \
    RODA_UID="1000" \
    RODA_GROUP="roda" \
    RODA_GID="1000"

COPY ./docker-files/docker-entrypoint.sh /
COPY ./docker-files/docker-entrypoint.d/* /docker-entrypoint.d/

RUN set -ex; \
  groupadd -r --gid "$RODA_GID" "$RODA_GROUP"; \
  useradd -r --uid "$RODA_UID" --gid "$RODA_GID" "$RODA_USER"; \
  mkdir -p -m0770 "$RODA_HOME/data"; \
  mkdir -p -m0770 "$RODA_HOME/config"; \
  chown -R "$RODA_USER:0" "$RODA_HOME"

VOLUME "$RODA_HOME/data"

USER "$RODA_UID"

ENTRYPOINT ["/docker-entrypoint.sh"]
