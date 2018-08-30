FROM ubuntu:xenial
RUN apt-get -y  update && apt-get -y upgrade &&apt-get install -y  openjdk-8-jdk gradle wget build-essential
ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
RUN apt-get  install -y libc6-dev-i386 libc6-i386
#RUN apt-get install -y gcc-mingw-w64-i686 gcc-mingw-w64-x86-64 
ENTRYPOINT ["bash","/entrypoint.sh"]