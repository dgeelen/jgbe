#!/bin/bash

./sdlgnuboy --no-fullscreen testrom.gb &> log2.txt
aoss java romtester testrom.gb -log log.txt > /dev/null

while diff log.txt log2.txt > /dev/null ; do
	./rndCartridge > /dev/null
	./sdlgnuboy --no-fullscreen testrom.gb &> log2.txt
	aoss java romtester testrom.gb -log log.txt > /dev/null
	echo -n .
done
echo
cp testrom.gb testrom1.gb
diff -u log.txt log2.txt
cat /dev/urandom > /dev/dsp