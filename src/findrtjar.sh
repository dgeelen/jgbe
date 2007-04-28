#!/bin/sh
JAVALINK=`which java`
JAVAFILE=
while [ "${JAVALINK}" != "" ]
do
	JAVAFILE=${JAVALINK}
	JAVALINK=`readlink ${JAVAFILE}`
done

echo $JAVAFILE | sed 's:/bin/[^/]*$:/lib/rt.jar:'