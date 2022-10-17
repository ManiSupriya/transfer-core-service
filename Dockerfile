FROM  mashrequae.azurecr.io/jdk8-jre-hardened-font:v1

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN mkdir -p /opt/appdynamics && \
    mkdir -p /usr/images/transfer-core-service && \
    chown -R appuser:appgroup /opt && \
    chown -R appuser:appgroup /usr/images/transfer-core-service

# COPY /src/main/resources/JSONUATCert.crt $JAVA_HOME/jre/lib/security

RUN \
    cd $JAVA_HOME/jre/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias efmuat.mashreqbank.com -file JSONUATCert.cer

RUN \
    cd $JAVA_HOME/jre/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias ciam.mashreqbank.com -file mashreq_root_ca_certificate.cer

ENV TZ=${TZ:-Asia/Dubai}
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

USER appuser

# Do not change any of these
COPY target/*.jar /app.jar
CMD java $JAVA_OPTS \
    -Dappdynamics.jvm.shutdown.mark.node.as.historical=true \
    -Dappdynamics.agent.uniqueHostId=$(sed -rn '1s#.*/##; 1s/(.{12}).*/\1/p' /proc/self/cgroup) \
    -javaagent:/opt/appd/javaagent.jar \
    -jar /app.jar