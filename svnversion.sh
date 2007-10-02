#!/bin/sh
SVNVER=`svnversion 2> /dev/null`

if [ "${SVNVER}" = "exported" ]; then
	GITHEAD=`git-rev-parse HEAD 2> /dev/null`
	GITHEAD_ORIG="${GITHEAD}"
	SVNVER=
fi

if [ "${GITHEAD}" != "" ]; then
	SVNVER=`git-svn find-rev ${GITHEAD} 2> /dev/null`

	while [ \( "${SVNVER}" = "" \) -a \( "${GITHEAD}" != "" \) ]
	do
		GITHEAD=`git-rev-parse ${GITHEAD}^ 2> /dev/null 1> /dev/null && git-rev-parse ${GITHEAD}^ 2> /dev/null`
		SVNVER=`git-svn find-rev ${GITHEAD} 2> /dev/null`
#echo $GITHEAD
	done

	echo $ISGIT
	if [ \( "${SVNVER}" != "" \) -a \( "${GITHEAD}" != "${GITHEAD_ORIG}" \) ]; then
		SVNVER=${SVNVER}G
	fi
	GITDIFF=`git-diff HEAD | cat`
	if [ "${GITDIFF}" != "" ]; then
		SVNVER=${SVNVER}M
	fi
fi

if [ "${SVNVER}" = "" ]; then
	SVNVER="unknown"
fi

echo $SVNVER
