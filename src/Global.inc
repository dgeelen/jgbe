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

/* save state macro's here */
#define SL_MAGIX (0x4a374a53)                // JGBS = <J>ava <G>ame<B>oy <S>tate in ascii hex
#define SL_SAVE_VERSION     2                // version we save, change when changing savestate format
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
