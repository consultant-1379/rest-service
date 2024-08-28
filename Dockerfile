#
# COPYRIGHT Ericsson 2023
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

FROM armdocker.rnd.ericsson.se/proj-esoa-so/so-base-openjdk17:1.3.2-1

COPY eric-esoa-rest-service-jar/target/*.jar eric-esoa-rest-service.jar
COPY set_log_level.sh /set_log_level.sh
COPY entryPoint.sh /entryPoint.sh
EXPOSE 8080
ENV JAVA_OPTS ""
ENV LOADER_PATH ""

ARG user=appuser
ARG uid=140021
ARG gid=140021

RUN echo "${user}:x:${uid}:${gid}:UI-user:/:/bin/bash" >> /etc/passwd \
    && sed -i 's/^\(root.*\):.*/\1:\/bin\/false/' /etc/passwd && sed -i 's/^root:/root:!/' /etc/shadow

RUN chmod +x /entryPoint.sh
ENTRYPOINT ["sh", "-c", "/entryPoint.sh $JAVA_OPTS"]
