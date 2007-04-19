//#define USE_JAVA_142


#ifdef USE_JAVA_142
	#define PRINTFREEMEM(x)  { System.out.println("in "+x+": Free Memory="+(Runtime.getRuntime().freeMemory()/1024) + "  used = "+((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)); }
#else
	#define PRINTFREEMEM(x)
#endif

#ifdef DEBUG
	#define PRINTF(x...) System.out.printf(x)
#else
	#ifdef USE_JAVA_142
		#define PRINTF(x...) /*System.out.printf(x)*/
	#else
		//FIXME
		#define PRINTF(x...) System.out.printf(x)
	#endif
#endif

/* first one nearly 10% faster! (sometimes :-p)*/
#define MAX(a,b) ((a)<(b)?(b):(a))
//#define MAX(a,b) (Math.max((a), (b))

#define MIN(a,b) ((a)>(b)?(b):(a))
//#define MIN(a,b) (Math.min((a), (b))
