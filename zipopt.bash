#!/bin/bash

P7Z="${3}"
if [ "x${1}" == "x--7zip" ]
then
	P7Z="${2}"
	shift
	shift
fi
P7Z=`which "${P7Z}" 2> /dev/null || which 7za 2> /dev/null`
echo Using 7zip at: ${P7Z}
echo Input: ${1}
echo Output: ${2}

#create workdir and copy over the input file
CURDIR=`pwd`
WORKDIR=`mktemp -d -t zipopt.XXXXXXXXXX` || exit
cp "${1}" "${WORKDIR}/input.zip"
cd "${WORKDIR}"

#echo "-mm=Deflate -mx=9 -mpass=15" >> methods.txt
#echo "-mm=Deflate -mfb=32 -mpass=7" >> methods.txt
#echo "-mm=Deflate -mfb=32 -mpass=10" >> methods.txt
#echo "-mm=Deflate -mfb=32 -mpass=15" >> methods.txt

#echo "-mm=Deflate -mfb=64 -mpass=7" >> methods.txt
echo "-mm=Deflate -mfb=64 -mpass=10" >> methods.txt
echo "-mm=Deflate -mfb=64 -mpass=15" >> methods.txt

#echo "-mm=Deflate -mfb=128 -mpass=7" >> methods.txt
echo "-mm=Deflate -mfb=128 -mpass=10" >> methods.txt
echo "-mm=Deflate -mfb=128 -mpass=15" >> methods.txt

#echo "-mm=Deflate -mfb=256 -mpass=7" >> methods.txt
#echo "-mm=Deflate -mfb=256 -mpass=10" >> methods.txt
#echo "-mm=Deflate -mfb=256 -mpass=15" >> methods.txt

#echo "-mm=Deflate -mfb=258 -mpass=7" >> methods.txt
echo "-mm=Deflate -mfb=258 -mpass=10" >> methods.txt
echo "-mm=Deflate -mfb=258 -mpass=15" >> methods.txt

#echo "-mx=9 -mm=Copy" >> methods.txt
#echo "-mx=9 -mm=Deflate64" >> methods.txt
#echo "-mx=9 -mm=BZip2" >> methods.txt
#echo "-mm=Deflate64 -mfb=257 -mpass=15" >> methods.txt

#cat methods.txt

# do some work
"${P7Z}" l input.zip -slt | sed -n 's:^Path = ::p' > files.txt
#cat files.txt
cat files.txt | while read -r
do
	# don't use 'read line' as it chokes on wome whitespaces
	line="${REPLY}"
	echo file: "${line}"
	#find best method
	mkdir temp
	cd temp
	"${P7Z}" x ../input.zip -i!"${line}" -so > file.dat
	cat ../methods.txt | while read method
	do
		"${P7Z}" a "method ${method}.zip" ${method} file.dat	
	done
	rm file.dat
	BESTMETHOD=`ls -rS | head -n 1 | sed "s:^method ::" | sed "s:\.zip$::"`
	echo "Best method:${BESTMETHOD} (${line})" >> ../mres.txt
	cd ..
	rm -r temp
	
	mkdir temp
	cd temp
	"${P7Z}" x ../input.zip -i!"${line}"
	"${P7Z}" a ../result.zip ${BESTMETHOD} "${line}"
	cd ..
	rm -r temp
done
echo --------
cat mres.txt
echo --------
ls -l input.zip result.zip
echo --------

#copy over result file and remove temp work dir
cd "${CURDIR}"
cp "${WORKDIR}/result.zip" "${2}"
rm -r "$WORKDIR"
