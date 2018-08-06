FROM postgresql:10

RUN localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8

ENV TZ Asia/Shanghai
ENV LANG zh_CN.UTF-8

COPY init.sql /docker-entrypoint-initdb.d/

EXPOSE 5432
