# First stage: JDK 11 with modules required for Spring Boot
FROM debian:stretch-slim as packager

# source JDK distribution names
ENV JDK_VERSION="11.0.2"
ENV JDK_URL="https://download.java.net/java/GA/jdk11/9/GPL/openjdk-${JDK_VERSION}_linux-x64_bin.tar.gz"
ENV JDK_HASH="99be79935354f5c0df1ad293620ea36d13f48ec3ea870c838f20c504c9668b57"
ENV JDK_HASH_FILE="${JDK_ARJ_FILE}.sha2"
ENV JDK_ARJ_FILE="openjdk-${JDK_VERSION}.tar.gz"
# target JDK installation names
ENV OPT="/opt"
ENV JKD_DIR_NAME="jdk-${JDK_VERSION}"
ENV JAVA_HOME="${OPT}/${JKD_DIR_NAME}"
ENV JAVA_MINIMAL="${OPT}/java-minimal"

# downlodad JDK to the local file
ADD "$JDK_URL" "$JDK_ARJ_FILE"

# FIX for buildagent docker v17.06 (fixed in 17.06.1 #89) https://github.com/docker/docker-ce/releases/tag/v17.06.1-ce
# Use parameter --build-arg workaround="true" on buildagent
ARG workaround=false
RUN { \
        if [ "$workaround" = "true" ] ; then \
          echo "FIX for docker 17.06" ; \
          rm -rf "$JDK_ARJ_FILE" ; \
          apt update ; \
          apt install -y wget ; \
          wget --output-document="$JDK_ARJ_FILE" "$JDK_URL" ; \
        else \
          echo "Nothing to do" ; \
        fi \
    }

# verify downloaded file hashsum
RUN { \
        echo "Verify downloaded JDK file $JDK_ARJ_FILE:" && \
        echo "$JDK_HASH $JDK_ARJ_FILE" > "$JDK_HASH_FILE" && \
        sha256sum -c "$JDK_HASH_FILE" ; \
    }
# extract JDK and add to PATH
RUN { \
        echo "Unpack downloaded JDK to ${JAVA_HOME}/:" && \
        mkdir -p "$OPT" && \
        tar xf "$JDK_ARJ_FILE" -C "$OPT" ; \
    }
ENV PATH="$PATH:$JAVA_HOME/bin"

RUN { \
        java --version ; \
        echo "jlink version:" && \
        jlink --version ; \
    }

# build modules distribution
RUN jlink \
    --verbose \
    --add-modules \
        java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
        # java.naming - javax/naming/NamingException
        # java.desktop - java/beans/PropertyEditorSupport
        # java.management - javax/management/MBeanServer
        # java.security.jgss - org/ietf/jgss/GSSException
        # java.instrument - java/lang/instrument/IllegalClassFormatException
    --compress 2 \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --output "$JAVA_MINIMAL"

# Second stage, add only our minimal "JRE" distr and our app
FROM debian:stretch-slim

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="$PATH:$JAVA_HOME/bin"

COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"
COPY "build/libs/feedback-0.0.1-SNAPSHOT.jar" "/app.jar"

EXPOSE 8080
EXPOSE 27017
#CMD [ "-jar", "/app.jar" ]
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar
