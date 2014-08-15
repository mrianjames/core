#!/bin/bash
#MAC - run this within boot2docker shell.
#make a clean build of the product.
#mvn clean assembly:assembly
if [ -z "$1" ] ; then
    mvn assembly:assembly
fi
OAKTREE_CORE=com.oaktree.core-2.0.6-SNAPSHOT
OAKTREE_CORE_PACKAGE=${OAKTREE_CORE}-distribution.tar.gz
DOCKERFILE=Dockerfile
#This script will produce a docker file to take our assembly, build the image and upload to std repo.
echo "FROM mrianjames/developer" > ${DOCKERFILE}

echo "# File Author / Maintainer" >> ${DOCKERFILE}
echo "MAINTAINER IJ" >> ${DOCKERFILE}

echo "# Create the app directory" >> ${DOCKERFILE}
echo "RUN mkdir -p /app/oaktree" >> ${DOCKERFILE}

echo "ADD target/${OAKTREE_CORE_PACKAGE} /app/oaktree" >> ${DOCKERFILE}
echo "WORKDIR /app/oaktree" >> ${DOCKERFILE}
#echo "RUN gunzip ${OAKTREE_CORE_PACKAGE}" >> ${DOCKERFILE}
echo "WORKDIR /app/oaktree/${OAKTREE_CORE}/scripts" >> ${DOCKERFILE}

##################### INSTALLATION END #####################

# Expose the default port
#EXPOSE 27017

# Default port to execute the entrypoint (MongoDB)
#CMD ["--port 27017"]

# Set default container command
#ENTRYPOINT usr/bin/mongod

docker build -t mrianjames/oaktree_core .
docker push mrianjames/oaktree_core
