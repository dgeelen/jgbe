#include <stdio.h>
#include <stdlib.h>
#include <time.h>
int main(char *argc, int argv) {
	FILE* f=fopen("testrom.gb", "r+");
	if(f) {
		fseek(f, 0x150, SEEK_SET);
		char *buf=new char[512];
		srand(time(NULL));
		int i = 0;
		for (int x = 0; x < 6; ++x) {
			buf[i++]=0x06 + (x*0x08);
			buf[i++]=rand() & 0xff;
		}
		buf[i++]=0x3e;
		buf[i++]=rand() & 0xff;

		buf[i++]=0x21;
		buf[i++]=0xa0;
		buf[i++]=0xff;

		buf[i++]=0x36;
		buf[i++]=rand() & 0xff;

		buf[i++]=rand() & 0xff;
		buf[i++]=rand() & 0xff;
		buf[i++]=rand() & 0xff;

		buf[i++]=0xfc; //Unknow instr
		buf[i++]=0xfc; //Unknow instr
		buf[i++]=0xfc; //Unknow instr
		fwrite(buf, 1, i, f);
		fclose(f);
	}
	else {
		fprintf(stderr ,"Could not open `testrom.gb'\n");
	}
	return 0;
}
