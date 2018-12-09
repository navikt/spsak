FROM postgres:10

RUN localedef -i nb_NO -c -f UTF-8 -A /usr/share/locale/locale.alias nb_NO.UTF-8
ENV LANG nb_NO.utf8

COPY initdb.sh /docker-entrypoint-initdb.d/
RUN chmod 0755 /docker-entrypoint-initdb.d/initdb.sh



