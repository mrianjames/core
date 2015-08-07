#!/bin/bash
WHEREAMI=`pwd`
#This file is to automate deployments of core to run examples.
DEPLOYMENT_FOLDER="${WHEREAMI}/../../core/"

echo Building package
mvn clean assembly:assembly

VERSION=`ls target/com.oaktree.core*distribution.tar.gz | head -1 | awk '{print substr($0,25,length($0)-44)}'`
TARFILE=com.oaktree.core-$VERSION-distribution.tar
FILE=$TARFILE.gz

echo Deploying target/$FILE to $DEPLOYMENT_FOLDER
rm -rf $DEPLOYMENT_FOLDER/*
cp target/$FILE $DEPLOYMENT_FOLDER
cd ${DEPLOYMENT_FOLDER}
PWD=`pwd`
echo I am in $PWD
gunzip $FILE
tar -xvf $TARFILE
cd com.oaktree.core-$VERSION
cd scripts
chmod u+x *

