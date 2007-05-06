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

#undef CASE_LD_RHL
#define CASE_LD_RHL(r)

#undef DO_LD_RI
#define DO_LD_RI(r, i) { ASS_S_I(r, i); }

#undef DO_LD_RR
#define DO_LD_RR(r1, r2) { ASS_S_V(r1, r2); }

#undef PUSH


