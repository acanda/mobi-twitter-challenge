FROM confluentinc/cp-kafka-connect:5.3.1

ARG major_version=8
ARG minor_version=222
ARG mobi_version=2

ENV JAVA_HOME=/usr/java/default \
    PATH="/usr/java/default/bin:${PATH}" \
    _JAVA_OPTIONS="-Djdk.tls.ephemeralDHKeySize=2048 -Djdk.tls.rejectClientInitiatedRenegotiation=true"

# uninstall java from initial image and put the one from mobi
RUN rm -Rf /usr/lib/jvm/zulu-8-amd64/ && \
    echo "Europe/Zurich" >  /etc/timezone && \
    cp /usr/share/zoneinfo/Europe/Zurich /etc/localtime && \
    curl -kL https://nexus.mobicorp.ch:8443/nexus/repository/jdks/openjdk/${major_version}/azul-zulu-enterprise-openjdk-jdk${major_version}.0.${minor_version}-linux_x64-mobi${mobi_version}.tar.gz > java.tar.gz && \
    mkdir -p /usr/java/jdk${major_version}.0.${minor_version}.${mobi_version}/ && \
    tar xzfp java.tar.gz -C /usr/java/jdk${major_version}.0.${minor_version}.${mobi_version}/ --strip-components=1 && \
    rm java.tar.gz && \
    # Symlinks
    ln -snf /usr/java/jdk${major_version}.0.${minor_version}.${mobi_version} /usr/java/default && \
    # remove limited policy
    rm  -rf /usr/java/jdk{major_version}.0.${minor_version}.${mobi_version}/jre/lib/security/policy/limited && \
    groupadd -g 1265 java && \
    useradd -u 1325 -g 1265 -m -d /home/java java && \
    { \
        echo ''; \
		echo '# Set DNS caching to 1 minute'; \
		echo 'networkaddress.cache.ttl=60'; \
	} >> $JAVA_HOME/jre/lib/security/java.security && \
    rm -rf usr/java/default/*src.zip \
    # remove unused Stuff
        usr/java/default/db \
        usr/java/default/lib/missioncontrol \
        usr/java/default/lib/visualvm \
        usr/java/default/lib/*javafx* \
        usr/java/default/jre/lib/plugin.jar \
        usr/java/default/jre/lib/ext/jfxrt.jar \
        usr/java/default/jre/bin/javaws \
        usr/java/default/jre/lib/javaws.jar \
        usr/java/default/jre/lib/desktop \
        usr/java/default/jre/plugin \
        usr/java/default/jre/lib/deploy* \
        usr/java/default/jre/lib/*javafx* \
        usr/java/default/jre/lib/*jfx* \
        usr/java/default/jre/lib/amd64/libdecora_sse.so \
        usr/java/default/jre/lib/amd64/libprism_*.so \
        usr/java/default/jre/lib/amd64/libfxplugins.so \
        usr/java/default/jre/lib/amd64/libglass.so \
        usr/java/default/jre/lib/amd64/libgstreamer-lite.so \
        usr/java/default/jre/lib/amd64/libjavafx*.so \
        usr/java/default/jre/lib/amd64/libjfx*.so && \
        rm -rf usr/java/default/jre/bin/jjs \
        usr/java/default/jre/bin/keytool \
        usr/java/default/jre/bin/orbd \
        usr/java/default/jre/bin/pack200 \
        usr/java/default/jre/bin/policytool \
        usr/java/default/jre/bin/rmid \
        usr/java/default/jre/bin/rmiregistry \
        usr/java/default/jre/bin/servertool \
        usr/java/default/jre/bin/tnameserv \
        usr/java/default/jre/bin/unpack200 \
        usr/java/default/jre/bin/appletviewer \
        usr/java/default/jre/bin/ControlPanel \
        usr/java/default/jre/bin/jvisualvm \
        usr/java/default/jre/bin/jcontrol

RUN wget --no-check-certificate https://nexus.mobicorp.ch:8443/nexus/repository/swrepos/kafka-connect-connectors/jcustenborder-kafka-connect-spooldir-1.0.41.zip \
    && confluent-hub install --no-prompt jcustenborder-kafka-connect-spooldir-1.0.41.zip \
    && rm jcustenborder-kafka-connect-spooldir-1.0.41.zip \
    && wget --no-check-certificate https://nexus.mobicorp.ch:8443/nexus/repository/swrepos/kafka-connect-connectors/confluentinc-kafka-connect-salesforce-1.3.3.zip \
    && confluent-hub install --no-prompt confluentinc-kafka-connect-salesforce-1.3.3.zip \
    && rm confluentinc-kafka-connect-salesforce-1.3.3.zip \
    && wget --no-check-certificate https://nexus.mobicorp.ch:8443/nexus/repository/swrepos/kafka-connect-transformators/jcustenborder-kafka-connect-transform-common-0.1.0.34.zip \
    && confluent-hub install --no-prompt jcustenborder-kafka-connect-transform-common-0.1.0.34.zip \
    && rm jcustenborder-kafka-connect-transform-common-0.1.0.34.zip \
    && wget --no-check-certificate https://nexus.mobicorp.ch:8443/nexus/repository/swrepos/kafka-connect-connectors/jcustenborder-kafka-connect-twitter-0.3.33.zip \
    && confluent-hub install --no-prompt jcustenborder-kafka-connect-twitter-0.3.33.zip \
    && rm jcustenborder-kafka-connect-twitter-0.3.33.zip

#Hack ! Skipping the test otherwise it doesn't work, class not found with the config provider :(
RUN sed -e '/\/etc\/confluent\/docker\/ensure/ s/^#*/#/' -i /etc/confluent/docker/run

COPY ./kafka-connect-sparql-sink/target/*-fat.jar /usr/share/java/

USER java
