//#define USE_JAVA_MOBILE
//#define USE_JAVA_142

#ifdef USE_JAVA_MOBILE
	#define USE_JAVA_142
#endif

#define DEBUG

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
	#define STRFORMAT(x...) ("")
	#define VECTOR(t) Vector
#else
	#define STRFORMAT(x...) String.format(x)
	#define VECTOR(t) Vector<t>
#endif

#ifdef DEBUG
	#ifdef USE_JAVA_142
		#define PRINTF(x...) /*System.out.printf(x)*/
	#else
		//FIXME
		#define PRINTF(x...) System.out.printf(x)
	#endif
#else
	#define PRINTF(x...) /*System.out.printf(x)*/
#endif

/* first one nearly 10% faster! (sometimes :-p)*/
#define MAX(a,b) ((a)<(b)?(b):(a))
//#define MAX(a,b) (Math.max((a), (b))

#define MIN(a,b) ((a)>(b)?(b):(a))
//#define MIN(a,b) (Math.min((a), (b))

/* save state macro's here */
#define SL_MAGIX (0x4a374a53)                // JGBS = <J>ava <G>ame<B>oy <S>tate in ascii hex
#define SL_SAVE_VERSION     4                // version we save, change when changing savestate format
#define SL_LOAD_MIN_VERSION 0                // oldest version we can handle
#define SL_LOAD_MAX_VERSION SL_SAVE_VERSION  // newest version we can handle, normally the same as what we save

#define SAVELOAD_FUNCTION \
	protected final void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream) throws IOException

#define SL_SAVE (save)
#define SL_LOAD (!save)


#define SL_UINT8(var) {\
	if (SL_SAVE) \
		dostream.writeByte((var)&0xff); \
	else \
		(var) = distream.readUnsignedByte(); \
}

#define SL_UINT16(var) {\
	if (SL_SAVE) \
		dostream.writeShort((var)&0xffff); \
	else \
		(var) = distream.readUnsignedShort(); \
}

#define SL_INT SL_INT32
#define SL_INT32(var) {\
	if (SL_SAVE) \
		dostream.writeInt(var); \
	else \
		(var) = distream.readInt(); \
}

#define SL_BOOL(var) {\
	if (SL_SAVE) \
		dostream.writeBoolean(var); \
	else \
		(var) = distream.readBoolean(); \
}

#define SL_ARR(type, var, len) {\
	for (int sl_i = 0; sl_i < (len); ++sl_i) \
		SL_ ## type (var[sl_i]); \
}

#define SL_ARR_2D(type, var, len1, len2) {\
	for (int sl_i = 0; sl_i < (len1); ++sl_i) \
		for (int sl_j = 0; sl_j < (len2); ++sl_j) \
			SL_ ## type (var[sl_i][sl_j]); \
}

#define SL_IF_VERSION(min, max) \
	if ( ((min == -1) || (min <= version)) && ((max == -1) || (version <= max)) )

#define SL_SKIP(type, len) { \
	int tempskip = 0; \
	for (int sl_i = 0; sl_i < (len); ++sl_i) \
		SL_ ## type (tempskip); \
}

#define SL_OBJ(obj) \
	(obj).stateSaveLoad(save, version, dostream, distream);
