#!/bin/bash
#run me from scripts folder.
JAVA="java"
ROOT=`pwd`/..
echo ROOT: ${ROOT}
LIB="lib"
PROPERTIES="properties"
SCRIPTS="scripts"
cd ${ROOT}/
JMX_OPTIONS=""
MEMORY_OPTION="-Xms2048m -Xmx2048m"
GC_OPTIONS=""
SGC_OPTIONS="-XX:-UsePerfData -XX:MaxTenuringThreshold=0 -XX:SurvivorRatio=20000 -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 -XX:+ParallelRefProcEnabled -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled  -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:ParallelGCThreads=7 -XX:NewSize=128M -XX:MaxNewSize=128M -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:CMSMarkStackSize=8M -XX:CMSMarkStackSizeMax=32M -XX:+UseLargePages -XX:+DisableExplicitGC "
PARAMS="1000000 10 false"
CLASSPATH="${ROOT}/${PROPERTIES}:${ROOT}/${SCRIPTS}:${ROOT}/${LIB}/com.oaktree.core_2.0.6-SNAPSHOT.jar:${ROOT}/${LIB}/commons-math-1.2.jar:${ROOT}/${LIB}/slf4j-api-1.7.5.jar:${ROOT}/${LIB}/slf4j-oaktree-1.0.14.jar"
echo "****************************************"

JAVA_VERSION=`${JAVA} -version`
echo Starting Container Performance Test
echo Using Java ${JAVA_VERSION} from ${JAVA}
echo "****************************************"

${JAVA} -server -classpath ${CLASSPATH} ${MEMORY_OPTION} ${GC_OPTIONS} ${JMX_OPTIONS} com.oaktree.core.container.TestContainerPerformance ${PARAMS}
echo "****************************************"
