#include "pasoundline.h"
#include <assert.h>
#include <stdio.h>
#include <math.h>
#include "portaudio.h"

static int singleton_lock = 0;

#define NUM_SECONDS   (5)
#define SAMPLE_RATE   (44100)
#define FRAMES_PER_BUFFER  (64)

#ifndef M_PI
#define M_PI  (3.14159265)
#endif

#define TABLE_SIZE   (200)

#define BUFSIZE 2048
#define BUFMASK 2047

typedef unsigned char uint8;
struct PAInfo {
	PaStreamParameters outputParameters;
	PaStream *stream;
	uint8 data[BUFSIZE];
	int cpos;
	int cmax;
};

static void pa_init();
static void pa_write(uint8* d, int len);
static int pa_callback( const void *inputBuffer, void *outputBuffer,
                        unsigned long framesPerBuffer,
                        const PaStreamCallbackTimeInfo* timeInfo,
                        PaStreamCallbackFlags statusFlags,
                         void *userData );

static struct PAInfo pa;
void Java_PASoundLine_start(JNIEnv* env, jobject this)
{
	assert(singleton_lock == 0);
	++singleton_lock;
	assert(singleton_lock == 1);
	pa_init();
}

jint Java_PASoundLine_write(JNIEnv* env, jobject this, jbyteArray b, jint off, jint len)
{
	assert(singleton_lock == 1);
	jbyte *buffer = (*env)->GetByteArrayElements(env, b, NULL);
	pa_write(buffer+off, len);
	(*env)->ReleaseByteArrayElements(env, b, buffer, 0);
}

void pa_init()
{
	pa.cpos = pa.cmax = 0;
	PaError err;

	printf("PortAudio Test: output sine wave. SR = %d, BufSize = %d\n", SAMPLE_RATE, FRAMES_PER_BUFFER);
	printf("1\n");

	err = Pa_Initialize();
	if( err != paNoError ) goto error;

	pa.outputParameters.device = Pa_GetDefaultOutputDevice(); /* default output device */
	pa.outputParameters.channelCount = 2;       /* stereo output */
	pa.outputParameters.sampleFormat = paInt8; /* 32 bit floating point output */
	pa.outputParameters.suggestedLatency = Pa_GetDeviceInfo( pa.outputParameters.device )->defaultLowOutputLatency;
	pa.outputParameters.hostApiSpecificStreamInfo = NULL;

	err = Pa_OpenStream(
						&pa.stream,
						NULL, /* no input */
						&pa.outputParameters,
						SAMPLE_RATE,
						FRAMES_PER_BUFFER,
						paClipOff,      /* we won't output out of range samples so don't bother clipping them */
						pa_callback,
						(void*)&pa );

	if( err != paNoError ) goto error;

	err = Pa_StartStream( pa.stream );
	if( err != paNoError ) goto error;
	
	return;
	
error:
	Pa_Terminate();
	fprintf( stderr, "An error occured while using the portaudio stream\n" );
	fprintf( stderr, "Error number: %d\n", err );
	fprintf( stderr, "Error message: %s\n", Pa_GetErrorText( err ) );
	return ;//err;
}

static void pa_write(uint8* d, int len)
{
	int i;
	for (i=0; i<len; i++ )
	{
		do {} while (pa.cpos == ((pa.cmax+1)&BUFMASK));
		pa.data[pa.cmax++] = *d++;
		pa.cmax &= BUFMASK;
	}
}

static int pa_callback( const void *inputBuffer, void *outputBuffer,
                            unsigned long framesPerBuffer,
                            const PaStreamCallbackTimeInfo* timeInfo,
                            PaStreamCallbackFlags statusFlags,
                            void *userData )
{
	assert(&pa == userData);
	uint8 *out = (uint8*)outputBuffer;
	unsigned long i;

	(void) timeInfo; /* Prevent unused variable warnings. */
	(void) statusFlags;
	(void) inputBuffer;

	for( i=0; i<framesPerBuffer*2; i++ )
	{
		if (pa.cpos != pa.cmax) {
			*out++ = pa.data[pa.cpos++];
			pa.cpos &= BUFMASK;
		} else
			*out++ = 0;
	}

	return paContinue;
}
