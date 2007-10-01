#define ASS_V_E(x, y) \
	result.add( \
		new SimpleAssign( \
			(x), \
			(y) \
		) \
	)

#define ASS_S_E(x, y) \
	result.add( \
		new SimpleAssign( \
			new SimpleVariable(#x), \
			y \
		) \
	)

#define ASS_S_I(x, y) \
	result.add( \
		new SimpleAssign( \
			new SimpleVariable(#x), \
			new ConstantExpression(y) \
		) \
	)

#define ASS_S_V(x, y) \
	result.add( \
		new SimpleAssign( \
			new SimpleVariable(#x), \
			new SimpleVariable(#y) \
		) \
	)

#define ASS_S_M2(x, y, z) \
	result.add( \
		new SimpleAssign( \
			new SimpleVariable(#x), \
			new MemoryVariable(y, z) \
		) \
	)

#define ASS_M2_S(x, y, z) \
	result.add( \
		new SimpleAssign( \
			new MemoryVariable(x, y), \
			new SimpleVariable(#z) \
		) \
	)

#define ASS_S_M1(x, y) \
	result.add( \
		new SimpleAssign( \
			new SimpleVariable(#x), \
			new MemoryVariable(y) \
		) \
	)

#define ASS_M1_S(x, y) \
	result.add( \
		new SimpleAssign( \
			new MemoryVariable(x), \
			new SimpleVariable(#y) \
		) \
	)

#define ASS_M2_E(x, y, z) \
	result.add( \
		new SimpleAssign( \
			new MemoryVariable(x, y), \
			z \
		) \
	)

#define ASS_M1_E(x, y) \
	result.add( \
		new SimpleAssign( \
			new MemoryVariable(x), \
			y \
		) \
	)

#define ECONST(x) new ConstantExpression(x)
#define EREG(x) new SimpleVariable(#x)
#define EVBOOL(x) new SimpleBoolVariable(#x)
#define ECBOOL(x) new ConstantBoolExpression(x)
#define EMEM(x) new MemoryVariable(x)
#define EMEM2(x,y) new MemoryVariable(x, y)
#define ETMP(x) new TemporaryVariable(x)

#define EMEMR(x) new MemoryVariable(EREG(x))

#define EIMM16 ECONST(IMM16)
#define EIMM8 ECONST(IMM8)


#undef READ_F
#undef READ_V
#undef READ_S
#undef WRITE_F
#undef WRITE_V
#undef WRITE_S

#define READ_F(x) CPU.read(x)
#define READ_V(x) CPU.read(x)
#define READ_S(x) CPU.read(x)
#define WRITE_F(x) CPU.write(x)
#define WRITE_V(x) CPU.write(x)
#define WRITE_S(x) CPU.write(x)

#undef DO_LD_16_RI_BC
#undef DO_LD_16_RI_DE
#undef DO_LD_16_RI_HL
#undef DO_LD_16_RI_SP

#define DO_LD_16_RI_BC { ASS_S_I(C, IMM8); ASS_S_I(B, IMM8);}
#define DO_LD_16_RI_DE { ASS_S_I(E, IMM8); ASS_S_I(D, IMM8);}
#define DO_LD_16_RI_HL { ASS_S_I(L, IMM8); ASS_S_I(H, IMM8);}
#define DO_LD_16_RI_SP { ASS_S_I(SP, IMM16); }

#undef DO_LD_RX
#define DO_LD_RX(x, y) { }

#undef DO_LD_RHL
#define DO_LD_RHL(r) ASS_S_M2(r, new SimpleVariable("H"), new SimpleVariable("L"))

#undef DO_LD_RI
#define DO_LD_RI(r, i) { ASS_S_I(r, i); }

#undef DO_LD_RR
#define DO_LD_RR(r1, r2) { ASS_S_V(r1, r2); }

#define DO_XOR_E(e) { \
	ASS_S_E(A, new FunctionXOR(EREG(A), e)); \
	ASS_S_E(F, new FunctionCALCZF(EREG(A))); \
}
#define DO_OR_E(e) { \
	ASS_S_E(A, new FunctionOR(EREG(A), e)); \
	ASS_S_E(F, new FunctionCALCZF(EREG(A))); \
}
#define DO_AND_E(e) { \
	ASS_S_E(A, new FunctionAND(EREG(A), e)); \
	ASS_S_E(F, new FunctionCALCZF(EREG(A))); \
}

#undef CASES_ALU_OP
#define CASES_ALU_OP(base, imm, op) \
	case (imm)   : DO_ ## op ## _E(EIMM8); break; \
	case (base)  : DO_ ## op ## _E(EREG(B)); break; \
	case (base)+1: DO_ ## op ## _E(EREG(C)); break; \
	case (base)+2: DO_ ## op ## _E(EREG(D)); break; \
	case (base)+3: DO_ ## op ## _E(EREG(E)); break; \
	case (base)+4: DO_ ## op ## _E(EREG(H)); break; \
	case (base)+5: DO_ ## op ## _E(EREG(L)); break; \
	case (base)+6: DO_ ## op ## _E(new MemoryVariable(EREG(H), EREG(L))); break; \
	case (base)+7: DO_ ## op ## _E(EREG(A)); break; \

#define DO_PUSH2(e1, e2) { \
	ASS_V_E(EREG(SP), new FunctionDEC(EREG(SP), ECONST(1))); \
	ASS_V_E(EMEMR(SP), e1); \
	ASS_V_E(EREG(SP), new FunctionDEC(EREG(SP), ECONST(1))); \
	ASS_V_E(EMEMR(SP), e2); \
}

#define DO_PUSH16(v) DO_PUSH2(ECONST((v)>>8), ECONST((v)&0xff))

#define DO_POP2(v1, v2) { \
	ASS_V_E(v2, EMEMR(SP)); \
	ASS_V_E(EREG(SP), new FunctionINC(EREG(SP), ECONST(1))); \
	ASS_V_E(v1, EMEMR(SP)); \
	ASS_V_E(EREG(SP), new FunctionINC(EREG(SP), ECONST(1))); \
}

#undef CASES_INCDEC8_MEM
#define CASES_INCDEC8_MEM

#undef DO_INC8
#define DO_INC8(r) { \
	ASS_V_E(EREG(F), FAND(EREG(F), ECONST(CPU.CF_Mask))); \
	ASS_V_E(EREG(r), FAND(FINC(EREG(r), ECONST(1)), ECONST(0xff))); \
  /* F = F | fzero(A) | fHC(A&0fdvkre) */ \
	ASS_V_E(EREG(F), F_OR( \
	                 F_OR(EREG(F), new FunctionCALCZF(EREG(r))), /* ZF */ \
	                 new FunctionIIF(FAND(EREG(r),ECONST(0x0f)),ECONST(0),ECONST(CPU.HC_Mask)) /* HC */ \
	                 )); \
}

#undef DO_DEC8
#define DO_DEC8(r) { \
	ASS_V_E(EREG(F), F_OR(FAND(EREG(F), ECONST(CPU.CF_Mask)), ECONST(CPU.NF_Mask))); \
	ASS_V_E(EREG(F), F_OR(EREG(F), new FunctionIIF(FAND(EREG(r),ECONST(0x0f)),ECONST(0),ECONST(CPU.HC_Mask)))); \
	ASS_V_E(EREG(r), FAND(FDEC(EREG(r), ECONST(1)), ECONST(0xff))); \
  /* F = F | fzero(A) | fHC(A&0fdvkre) */ \
	ASS_V_E(EREG(F), F_OR(EREG(F), new FunctionCALCZF(EREG(r))) ); \
}
// 	r = (((r)-1)&0xff); \
// 	F &= FC; \
// 	F |= Tables.decflag[(r)]; \

