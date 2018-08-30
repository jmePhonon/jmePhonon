FROM ubuntu:xenial
RUN apt-get -y  update && apt-get -y upgrade &&apt-get install -y  openjdk-8-jdk wget build-essential
ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
RUN apt-get  install -y libc6-dev-i386 libc6-i386
RUN wget https://downloads.gradle.org/distributions/gradle-4.10-bin.zip -O /tmp/gradle.zip
RUN apt-get install -y unzip
RUN if [ "`sha256sum /tmp/gradle.zip | cut -d' ' -f1`" != "248cfd92104ce12c5431ddb8309cf713fe58de8e330c63176543320022f59f18" ];\
    then \
        echo "Error. This version of gradle is corrupted."; \
        exit 1;\
    fi && \
    mkdir -p /tmp/gradle && \
    unzip -q -d /tmp/gradle /tmp/gradle.zip &&\
    cp -Rf /tmp/gradle/gradle-*/* / &&\
    rm -Rf /tmp/gradle && rm -f /tmp/gradle.zip && \
    echo "Installed gradle `gradle -v`"
    #RUN apt-get install -y gcc-mingw-w64-i686 gcc-mingw-w64-x86-64 
ENTRYPOINT ["bash","/entrypoint.sh"]