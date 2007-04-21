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
#define SL_UINT8(x) {\
	if (save) \
		dostream.writeByte((x)&0xff); \
	else \
		(x) = distream.readUnsignedByte(); \
}

#define SL_UINT16(x) {\
	if (save) \
		dostream.writeShort((x)&0xffff); \
	else \
		(x) = distream.readUnsignedShort(); \
}

#define SL_INT SL_INT32
#define SL_INT32(x) {\
	if (save) \
		dostream.writeInt(x); \
	else \
		(x) = distream.readInt(); \
}

#define SL_BOOL(x) {\
	if (save) \
		dostream.writeBoolean(x); \
	else \
		(x) = distream.readBoolean(); \
}

#define SL_ARR(type, var, len) {\
	for (int i = 0; i < len; ++i) \
		SL_ ## type (var[i]); \
}

#define SL_ARR_2D(type, var, len1, len2) {\
	for (int i = 0; i < len1; ++i) \
		for (int j = 0; j < len2; ++j) \
			SL_ ## type (var[i][j]); \
}

/* TODO */
#define SL_OBJ(obj) \
	//(obj).stateSaveLoad(save, dostream, distream);
