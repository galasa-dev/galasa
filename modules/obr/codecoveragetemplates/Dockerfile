ARG dockerRepository

FROM ${dockerRepository}/library/httpd:alpine
ARG branch

RUN rm -v /usr/local/apache2/htdocs/*

COPY httpd.conf /usr/local/apache2/conf/httpd.conf

RUN sed -i "s/--branchname--/${branch}/"    /usr/local/apache2/conf/httpd.conf

COPY index.html /usr/local/apache2/htdocs/

COPY image /usr/local/apache2/htdocs/
