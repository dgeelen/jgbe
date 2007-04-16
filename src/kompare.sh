#!/bin/bash

rm -rf log.txt log2.txt && touch log.txt log2.txt
while diff log.txt log2.txt > /dev/null ; do
	./rndCartridge > /dev/null
	./sdlgnuboy --no-fullscreen testrom.gb &> log2.txt
	aoss java romtester testrom.gb -log log.txt > /dev/null
	echo -n .
done

cp testrom.gb testrom1.gb
diff -u log.txt log2.txt
cat /dev/urandom > /dev/dsp