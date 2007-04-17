//#define JMGBE

#ifdef DEBUG
	#define PRINTF(x...) System.out.printf(x)
#else
	#ifdef JMGBE
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
