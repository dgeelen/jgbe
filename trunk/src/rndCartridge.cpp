#include <stdio.h>
#include <stdlib.h>
#include <time.h>
int main(char *argc, int argv) {
	FILE* f=fopen("testrom.gb", "r+");
	if(f) {
		char *buf=new char[512];
		fseek(f, 0x100, SEEK_SET);
		buf[0]=0;
		buf[1]=0xc3;
		buf[2]=0x50;
		buf[3]=0x01;
		fwrite(buf, 1, 4, f);
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

		if (rand()&1)
			buf[i++]=0x3f; // toggle carry

		//buf[i++]=0xcb; //Unknow instr
		//buf[i++]=0x80+(rand() & 0x1f);
		
		//buf[i++]=0xf8;
		buf[i++]=rand() & 0xff;
		buf[i++]=rand() & 0xff;
		buf[i++]=rand() & 0xff;

		for (;i < 500;)
			buf[i++]=0xfc; //Unknow instr
		fseek(f, 0x150, SEEK_SET);
		fwrite(buf, 1, i, f);
		fclose(f);
	}
	else {
		fprintf(stderr ,"Could not open `testrom.gb'\n");
	}
	return 0;
}
