#!/bin/sh
SVNVER=`svnversion 2> /dev/null`

if [ "${SVNVER}" = "exported" ]; then
	SVNVER=`git-svn find-rev HEAD 2> /dev/null`
	GITDIFF=`git-diff HEAD | cat`
	if [ "${GITDIFF}" != "" ]; then
		SVNVER=${SVNVER}m
	fi
fi

if [ "${SVNVER}" = "" ]; then
	SVNVER="unknown"
fi

echo $SVNVER
