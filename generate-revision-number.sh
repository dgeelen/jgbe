#!/bin/sh

(\
OLDVER=`cat ${1}/revision_number.inc 2> /dev/null || true`; \
NEWVER="#define JGBE_VERSION_STRING \"`sh svnversion.sh`\""; \
if [ "${OLDVER}" = "${NEWVER}" ] ; then \
	true; \
else \
	echo "[version      ] Updating"; \
	echo "${NEWVER}" > "${1}/revision_number.inc"; \
fi \
)
