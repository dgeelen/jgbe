#include "../CPU.inc"

#include "CPUMacros.inc"

private VECTOR(SimpleAssign) Decompile(int PC) {
	VECTOR(SimpleAssign) result = new VECTOR(SimpleAssign)();
	boolean done = false;
	int max = 100;
	int cycles = 0;
	int SPC = PC;
	while (!done) {
		int opcode = FETCH;
		cycles += Tables.cycles[opcode]; /*CYCLE*/
		switch (opcode) {
			case 0x00: break;

			case 0xea: ASS_M1_S(EIMM16, A); break;                  // LD   (nn), A
			case 0xfa: ASS_S_M1(A, EIMM16); break;                  // LD   A   , (nn)
			case 0xe0: ASS_M2_S(ECONST(0xff), EIMM8, A); break;     // LDH  (n) , A
			case 0xf0: ASS_S_M2(A, ECONST(0xff), EIMM8); break;     // LDH  A   , (n)
			case 0xe2: ASS_M2_S(ECONST(0xff), EREG(C), A); break;   // LDH  (C) , A
			case 0xf2: ASS_S_M2(A, ECONST(0xff), EREG(C)); break;   // LDH  A   , (C)

			case 0x02: ASS_M2_S(EREG(B), EREG(C), A); break;        // LD   (BC), A
			case 0x0a: ASS_S_M2(A, EREG(B), EREG(C)); break;        // LD   A   , (BC)
			case 0x12: ASS_M2_S(EREG(D), EREG(E), A); break;        // LD   (DE), A
			case 0x1a: ASS_S_M2(A, EREG(D), EREG(E)); break;        // LD   A   , (DE)
			case 0x70: ASS_M2_S(EREG(H), EREG(L), B); break;        // LD   (HL), B
			case 0x71: ASS_M2_S(EREG(H), EREG(L), C); break;        // LD   (HL), C
			case 0x72: ASS_M2_S(EREG(H), EREG(L), D); break;        // LD   (HL), D
			case 0x73: ASS_M2_S(EREG(H), EREG(L), E); break;        // LD   (HL), E
			case 0x74: ASS_M2_S(EREG(H), EREG(L), H); break;        // LD   (HL), H
			case 0x75: ASS_M2_S(EREG(H), EREG(L), L); break;        // LD   (HL), L
			case 0x77: ASS_M2_S(EREG(H), EREG(L), A); break;        // LD   (HL), A
			case 0x36: ASS_M2_E(EREG(H), EREG(L), EIMM8); break;    // LD   (HL), n

			case 0xf9: ASS_S_E(SP, new FunctionOR(new FunctionSHL(EREG(H), new ConstantExpression(8)), EREG(L)));
			break;                                                  // LD   SP  , HL

			case 0x08: {                                            // LD   (nn), SP
				int t_acc = IMM16;
				ASS_M1_E(ECONST(t_acc), new FunctionSHR(EREG(SP), ECONST(8)));
				ASS_M1_E(ECONST((t_acc+1)&0xffff), new FunctionAND(EREG(SP), ECONST(0xff)));
			}; break;

			case 0x07: {
				//T = (F & FCmask) >> FCshift;
				ASS_V_E( ETMP(0), FSHR(FAND(EREG(F), ECONST(CPU.CF_Mask)),ECONST(CPU.CF_Shift)));
				//), 
				//F = (A & 0x80) >> (7-FCshift)
				ASS_V_E(EREG(A), FSHR(FAND(EREG(A), ECONST(0x80)), ECONST(7-CPU.CF_Shift)));
				//A <<= 1; //A |= T;
				ASS_V_E(EREG(A), F_OR(FSHL(EREG(A), ECONST(1)), ETMP(0)));
			}; break;

			case 0xf3: ASS_V_E(EVBOOL(IME), new ConstantBoolExpression(false)); break;                        // DI
			case 0xfb: ASS_V_E(EVBOOL(IME), new ConstantBoolExpression(true)); break;                        // IE

			case 0x22: { //WRITE_V(R16RHL, A); DO_INC16(HL); break; // LDI  (HL), A
				ASS_V_E(EMEM2(EREG(H),EREG(L)), EREG(A));
				ASS_V_E(EREG(H), FAND(FSHR(FINC(F_OR(FSHL(EREG(H), ECONST(8)), EREG(L)), ECONST(1)), ECONST(8)), ECONST(0xff)));
				ASS_V_E(EREG(L), FAND(FINC(F_OR(FSHL(EREG(H), ECONST(8)), EREG(L)), ECONST(1)), ECONST(0xff)));
			}; break;
			
			case 0x2a: { //A = READ_V(R16RHL); DO_INC16(HL); break; // LDI  A   , (HL)
				ASS_V_E(EREG(A), EMEM2(EREG(H),EREG(L)));
				ASS_V_E(EREG(H), FAND(FSHR(FINC(F_OR(FSHL(EREG(H), ECONST(8)), EREG(L)), ECONST(1)), ECONST(8)), ECONST(0xff)));
				ASS_V_E(EREG(L), FAND(FINC(F_OR(FSHL(EREG(H), ECONST(8)), EREG(L)), ECONST(1)), ECONST(0xff)));
			}; break;

			case 0x32: { //WRITE_V(R16RHL, A); DO_DEC16(HL); break; // LDD  (HL), A
				ASS_V_E(EMEM2(EREG(H),EREG(L)), EREG(A));
				ASS_V_E(EREG(H), FAND(FSHR(FDEC(F_OR(FSHL(EREG(H), ECONST(8)), EREG(L)), ECONST(1)), ECONST(8)), ECONST(0xff)));
				ASS_V_E(EREG(L), FAND(FDEC(F_OR(FSHL(EREG(H), ECONST(8)), EREG(L)), ECONST(1)), ECONST(0xff)));
			}; break;

			case 0x3a: { //A = READ_V(R16RHL); DO_DEC16(HL); break; // LDD  A   , (HL)
				ASS_V_E(EREG(A), EMEM2(EREG(H),EREG(L)));
				ASS_V_E(EREG(H), FAND(FSHR(FDEC(F_OR(FSHL(EREG(H), ECONST(8)), EREG(L)), ECONST(1)), ECONST(8)), ECONST(0xff)));
				ASS_V_E(EREG(L), FAND(FDEC(F_OR(FSHL(EREG(H), ECONST(8)), EREG(L)), ECONST(1)), ECONST(0xff)));
			}; break;

			case 0xc1: DO_POP2(EREG(B), EREG(C)); break;            // POP  BC
			case 0xd1: DO_POP2(EREG(D), EREG(E)); break;            // POP  DE
			case 0xe1: DO_POP2(EREG(H), EREG(L)); break;            // POP  HL
			case 0xf1: DO_POP2(EREG(A), EREG(F)); break;            // POP  AF
			case 0xc5: DO_PUSH2(EREG(B), EREG(C)); break;           // PUSH BC
			case 0xd5: DO_PUSH2(EREG(D), EREG(E)); break;           // PUSH DE
			case 0xe5: DO_PUSH2(EREG(H), EREG(L)); break;           // PUSH HL
			case 0xf5: DO_PUSH2(EREG(A), EREG(F)); break;           // PUSH AF

			case 0xcd: DO_PUSH16(PC+2); DO_JP_NN; break;            // CALL (nn)
			//case 0xc9: DO_RET; break;                               // RET

			case 0xc7: DO_PUSH16(PC); PC = 0x00; break;             // RST  &0
			case 0xcf: DO_PUSH16(PC); PC = 0x08; break;             // RST  &8
			case 0xd7: DO_PUSH16(PC); PC = 0x10; break;             // RST  &10
			case 0xdf: DO_PUSH16(PC); PC = 0x18; break;             // RST  &18
			case 0xe7: DO_PUSH16(PC); PC = 0x20; break;             // RST  &20
			case 0xef: DO_PUSH16(PC); PC = 0x28; break;             // RST  &28
			case 0xf7: DO_PUSH16(PC); PC = 0x30; break;             // RST  &30
			case 0xff: DO_PUSH16(PC); PC = 0x38; break;             // RST  &38

			case 0xc3: DO_JP_NN; break;                             // JP   (nn)
			case 0x18: DO_JR_D; break;                              // JR   (d)


			CASES_LD_XX
			//CASES_ALU_OP(0xb8, 0xfe, CP)                            // CP   x            x = r, imm8, (HL)
			CASES_ALU_OP(0xa0, 0xe6, AND)                           // AND  x            x = r, imm8, (HL)
			CASES_ALU_OP(0xa8, 0xee, XOR)                           // XOR  x            x = r, imm8, (HL)
			CASES_ALU_OP(0xb0, 0xf6, OR)                            // OR   x            x = r, imm8, (HL)
			//CASES_ALU_OP(0x80, 0xC6, ADD)
			//CASES_ALU_OP(0x88, 0xCE, ADC)
			//CASES_ALU_OP(0x90, 0xD6, SUB)
			//CASES_ALU_OP(0x98, 0xDE, SBC)
			default: {
				--PC;
				cycles -= Tables.cycles[opcode]; /*CYCLE*/
				done = true;
			}
		}

		if ((PC & ~0x3fff) != (SPC & ~0x3fff)) done = true;
		//if (!done && (PC > 0x4000)) ASSERT(false);
		if (cycles > max) done = true;
	}
	ASS_S_I(PC, PC);

	ASS_V_E(new ReturnVariable(), ECONST(cycles));
	return result;
}
