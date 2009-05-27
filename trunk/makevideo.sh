#!/bin/sh

INVID="${1}"
INAUD="${2}"

OUT="${3}"

MPLAYER=`which mplayer`
MENCODER=`which mencoder`

"${MENCODER}" -demuxer rawvideo -rawvideo fps=60:w=320:h=288:format=rgb24 "${INVID}" -audiofile "${INAUD}" -o "${OUT}" -ovc lavc -oac mp3lame -vf harddup -of avi -lavcopts vcodec=libx264
