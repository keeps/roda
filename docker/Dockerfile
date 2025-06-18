FROM eclipse-temurin:21-jre-jammy
LABEL maintainer="admin@keep.pt" vendor="KEEP SOLUTIONS"

# Install dependencies & Remove python dependency
RUN set -ex; \
    apt-get -qq update && \
    apt-get -qq -y install software-properties-common && \
    apt-get -qq -y --no-install-recommends install clamdscan jq rsync && \
    apt-get clean && rm -rf /var/lib/apt/lists/* && \
	DEBIAN_FRONTEND=noninteractive \
	apt-get autoremove -y --purge "*python*"

COPY ./docker-files/clamd.conf /etc/clamav/clamd.conf

# Copy the Roda WUI WAR file
COPY ./target/roda-wui-*.war  /KEEPS/bin/

ENV RODA_HOME=/roda \
    LDAP_SERVER_URL=ldap://openldap \
    LDAP_SERVER_PORT=1389 \
    SIEGFRIED_MODE=server \
    SIEGFRIED_SERVER_URL=http://siegfried:5138 \
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/roda_core_db \
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

WORKDIR "$RODA_HOME"

ENTRYPOINT ["/docker-entrypoint.sh"]
