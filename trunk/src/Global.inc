#ifndef GLOBAL_INC
#define GLOBAL_INC

//#define USE_JAVA_MOBILE
//#define USE_JAVA_142
//#define ENABLE_RECOMPILER

#ifdef USE_JAVA_MOBILE
	#define USE_JAVA_142
#endif

#define DEBUG

#define YIELD { \
	try { \
		Thread.sleep(100); /*100?*/ \
	} \
	catch (Exception e) { \
	} \
}

#ifdef NODEBUG
#ifdef DEBUG
#undef DEBUG
#endif
#endif

#define IF_STAT (1<<1)
#define IF_VBLANK (1<<0)

#ifdef DEBUG
	#define ASSERT(cond) \
		if (!(cond)) throw new Error("Assertion failed: " + #cond)
	// java way
	//#define ASSERT(cond) \
	//	assert (cond)
#else //DEBUG
	#define ASSERT(cond)
#endif //DEBUG

#define BUTTON_RIGHT  (1<<0)
#define BUTTON_LEFT   (1<<1)
#define BUTTON_UP     (1<<2)
#define BUTTON_DOWN   (1<<3)
#define BUTTON_A      (1<<4)
#define BUTTON_B      (1<<5)
#define BUTTON_SELECT (1<<6)
#define BUTTON_START  (1<<7)

#ifdef USE_JAVA_MOBILE
	#define PRINTFREEMEM(x)  { System.out.println("in "+x+": Free Memory="+(Runtime.getRuntime().freeMemory()/1024) + "  used = "+((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)); }
#else
	#define PRINTFREEMEM(x)
#endif

#ifdef USE_JAVA_142
	#define STRFORMAT(x, y...) (x)
	#define VECTOR(t) Vector
	#define FOREACH(type, var, vec) \
		for (int foreach = 0; foreach < (vec).size(); ++foreach) \
			for (type var = (type)((vec).elementAt(foreach)); var != null; var = null)
#else
	#define STRFORMAT(x...) String.format(x)
	#define VECTOR(t) Vector<t>
	#define FOREACH(type, var, vec) \
		for (type var : vec)
#endif

#ifdef DEBUG
	#ifdef USE_JAVA_142
		#define PRINTF(x, y...) System.out.print(x)
	#else
		//FIXME
		#define PRINTF(x...) System.out.printf(x)
	#endif
#else
	#define PRINTF(x...) /*System.out.printf(x)*/
#endif

#define PRINTLN(x) System.out.println(x)

/* first one nearly 10% faster! (sometimes :-p)*/
#define MAX(a,b) ((a)<(b)?(b):(a))
//#define MAX(a,b) (Math.max((a), (b))

#define MIN(a,b) ((a)>(b)?(b):(a))
//#define MIN(a,b) (Math.min((a), (b))

#endif//GLOBAL_INC
