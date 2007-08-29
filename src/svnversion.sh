#!/bin/sh
SVNVER=`svnversion 2> /dev/null`

if [ "${SVNVER}" = "exported" ]; then
	SVNVER=`git-svn find-rev HEAD 2> /dev/null`
fi

if [ "${SVNVER}" = "" ]; then
	SVNVER="unknown"
fi

echo $SVNVER
