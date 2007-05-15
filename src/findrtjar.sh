#!/bin/bash

testdir()
{
  if [ -f "$1/rt.jar" ]
  then
    echo "RTJARPATH := $1" >> Makefile.config
    echo "$1/rt.jar"
    exit
  fi
}

#manual test cached path
if [ -f "$1/rt.jar" ]
then
  echo "$1/rt.jar"
  exit
fi

JAVALINK=`which java`
JAVAFILE=
while [ "${JAVALINK}" != "" ]
do
	JAVAFILE=${JAVALINK}
	JAVALINK=`readlink ${JAVAFILE}`
done

TD=`echo $JAVAFILE | sed 's:/bin/[^/]*$:/lib:'`

testdir "$TD"

TD=`find /cygdrive/c/Program\ Files/Java/ -name rt.jar | sort | tail -n 1 | sed s:/rt\.jar$::`

testdir "$TD"

echo errorrtjarnotfound
