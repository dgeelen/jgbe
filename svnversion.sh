#!/bin/sh
#
# Generates a svn-like revision number based on the number of commits in the
# git repository. In the olden days, the JGBE source was hosted on Google Code,
# and its developers used git-svn as a bridge. Nowadays JGBE is hosted on
# GitHub, but we still like to have a simple, sequentially increasing revision
# number for users to refer to instead of using a random SHA1 revision.

# When building from CMake we won't necessarily be in the source folder
cd "${1}"

# Determine the base revision number, i.e. the number of commits that are
# pushed to the remote.
SVNVER="$(git rev-list --count origin/master 2> /dev/null)"

# Safe-guard against having an empty SVNVER
SVNVER="${SVNVER:-(unknown)}"

# Determine if we have any local commits, and if-so add a 'G'
if ! git diff --exit-code -s origin/master HEAD 2> /dev/null ; then
	SVNVER="${SVNVER}G"
fi

# Determine if there are any uncommitted changes, and if-so add an 'M'
if ! git diff --exit-code -s HEAD  2> /dev/null ; then
	SVNVER="${SVNVER}M"
fi

# When building from CMake we write the full output line, rather than just
# the svn number. Not sure why this is necessary, seems like it could be done
# entirely in CMake.
if [ -n "${2}" ]; then
	echo "#define JGBE_VERSION_STRING \"${SVNVER}\";"  > "${2}"
else
	echo "${SVNVER}"
fi
