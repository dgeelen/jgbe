#define FN NF_Mask
#define FH HC_Mask
#define FC CF_Mask
#define FZ ZF_Mask

#define ZFLAG(n) ( (n) != 0 ? 0 : FZ )

#define RNRB 0
#define RNRC 1
#define RNRD 2
#define RNRE 3
#define RNRH 4
#define RNRL 5
#define RNRF 6
#define RNRA 7

#define RNRBC 0
#define RNRDE 1
#define RNRHL 2
#define RNRSP 3

#define RNUM(r) RNR ## r

#define R16RAF ((A<<8)|F)
#define R16RBC ((B<<8)|C)
#define R16RDE ((D<<8)|E)
#define R16RHL ((H<<8)|L)
#define R16RSP (SP)

#define R16WAF(val) { A = val >> 8; F = val & 0xFF; }
#define R16WBC(val) { B = val >> 8; C = val & 0xFF; }
#define R16WDE(val) { D = val >> 8; E = val & 0xFF; }
#define R16WHL(val) { H = val >> 8; L = val & 0xFF; }
#define R16WSP(val) { SP = val; }

#define R16WAF_V(val) { A = (t_w16=val) >> 8; F = t_w16 & 0xFF; }
#define R16WBC_V(val) { B = (t_w16=val) >> 8; C = t_w16 & 0xFF; }
#define R16WDE_V(val) { D = (t_w16=val) >> 8; E = t_w16 & 0xFF; }
#define R16WHL_V(val) { H = (t_w16=val) >> 8; L = t_w16 & 0xFF; }
#define R16WSP_V(val) { SP = val; }

#define R16W(r, val) R16W ## r ## (val)
#define R16W_V(r, val) R16W ## r ## _V(val)
#define R16R(r) R16R ## r

#define TVARS \
	int t_mm[]; \
	int t_mi; \
	int t_w16; \
	int t_acc; \
	int t_vol; \
	int t_mask;

// use when you know the memmap will fail
#define READ_S(index) (read(index))
#define WRITE_S(index, val) write(index, val)

//#define INLINE_MEMMAP

#ifdef INLINE_MEMMAP
// dont use with expressions with side effects
#define READ_F(index)\
( ((t_mm=rMemMap[index>>12]) == null) \
	? READ_S(index) \
	: t_mm[index&0x0FFF] \
)
#define WRITE_F(index, val) \
{ if ((t_mm=wMemMap[index>>12]) == null) \
		WRITE_S(index, val); \
	else \
		t_mm[index&0x0FFF] = val; \
}

// use when the expression has side effects
#define READ_V(index) \
( ((t_mm=rMemMap[(t_mi=index)>>12]) == null) \
	? READ_S(t_mi) \
	: t_mm[t_mi&0x0FFF] \
)
#define WRITE_V(index, val) \
{ if ((t_mm=wMemMap[(t_mi=index)>>12]) == null) \
		WRITE_S(t_mi, val); \
	else \
		t_mm[t_mi&0x0FFF]  = val;\
}
#else
#define READ_F READ_S
#define READ_V READ_S
#define WRITE_F WRITE_S
#define WRITE_V WRITE_S
#endif

#define FETCH (READ_V(PC++))
#define IMM8 (FETCH)
// hope java evaluates left-to-right
#define IMM16 (FETCH|(FETCH<<8))
#define POP (READ_V(SP++)|(READ_V(SP++)<<8))

#define PUSH(val) { \
	SP=(SP-1)&0xffff; WRITE_F(SP, (val)>>8); \
	SP=(SP-1)&0xffff; WRITE_F(SP, (val)&0xff); \
}

#define PUSH_V(val) { \
	SP=(SP-1)&0xffff; WRITE_F(SP, (t_w16=val)>>8); \
	SP=(SP-1)&0xffff; WRITE_F(SP, (t_w16)&0xff); \
}

#define PUSH_OLD(val) \
{ \
   SP=(SP-1)&0xffff; \
   write( SP, ( (val)>>8 )&0xff ); \
   SP=(SP-1)&0xffff; \
   write( SP, (val)&0xff ); \
}



#define DO_LD_RX(r, val) { r = val; }
#define DO_JP_NN { PC = IMM16; }
#define DO_JR_D { PC += 1+((READ_F(PC))^0x80)-0x80; }
#define DO_CALL_NN { PUSH(PC+2); DO_JP_NN; }
#define DO_RET { PC = POP; }

#define SKIP_JR { --cycles; ++PC; }
#define SKIP_JP { --cycles; PC+=2; }
#define SKIP_CALL { cycles-=3; PC+=2; }
#define SKIP_RET  { cycles-=3; }

#define DO_INC8(r) { \
	++(r); \
	(r) &= 0xff; \
	F &= FC; \
	F |= Tables.incflag[(r)]; \
}

#define DO_DEC8(r) { \
	--(r); \
	(r) &= 0xff; \
	F &= FC; \
	F |= Tables.decflag[(r)]; \
}

#define CASES_INCDEC8_R(r) \
	case (0x04+(RNUM(r)<<3)): DO_INC8(r); break; \
	case (0x05+(RNUM(r)<<3)): DO_DEC8(r); break; \
	

#define CASES_INCDEC8 \
	CASES_INCDEC8_R(B) \
	CASES_INCDEC8_R(C) \
	CASES_INCDEC8_R(D) \
	CASES_INCDEC8_R(E) \
	CASES_INCDEC8_R(H) \
	CASES_INCDEC8_R(L) \
	CASES_INCDEC8_R(A) \
	case 0x34: { t_acc = READ_V(R16RHL); DO_INC8(t_acc); WRITE_V(R16RHL, t_acc); }; break; \
	case 0x35: { t_acc = READ_V(R16RHL); DO_DEC8(t_acc); WRITE_V(R16RHL, t_acc); }; break; \
	

#define DO_ADD_T(r, n) { \
	F = (((( A &0x0f )+((n)&0x0f ) )>0xf ) ? HC_Mask : 0); \
	(r) = A + (n); \
	F |= ( (r)>0xff ) ? CF_Mask : 0; \
	(r) &= 0xff; \
	F |= ZFLAG(r); \
}

#define DO_SUB_T(r, n) { \
	F = FN | \
		(((( A &0x0f )-((n)&0x0f ) )<0 ) ? HC_Mask : 0); \
	(r) = A - (n); \
	F |= ( (r)<0 ) ? CF_Mask : 0; \
	(r) &= 0xff; \
	F |= ZFLAG(r); \
}

#define DO_CP(n) DO_SUB_T(t_acc, n)
#define DO_SUB(n) DO_SUB_T(A, n);
#define DO_ADD(n) DO_ADD_T(A, n);

#define DO_ADD_HL(v1, v2) { \
	F &= FZ; \
	L += v2; \
	H += v1; \
	if (L > 0xff) { \
		L &= 0xff; \
		++H; \
		F |= FH; \
	} \
	if (H > 0xff) { \
		H &= 0xff; \
		F |= FC; \
	} \
}

/*
	#define CP(n) { \
W(acc) = (un16)A - (un16)(n); \
F = FN \
| (ZFLAG(LB(acc))) \
| (FH & ((A ^ (n) ^ LB(acc)) << 1)) \
| ((un8)(-(n8)HB(acc)) << 4); }
*/

#define DO_JR_COND(c) { if (c) DO_JR_D else SKIP_JR; };
#define DO_JP_COND(c) { if (c) DO_JP_NN else SKIP_JP; };
#define DO_CALL_COND(c) { if (c) DO_CALL_NN else SKIP_CALL; };
#define DO_RET_COND(c) { if (c) DO_RET else SKIP_RET; };

#define DO_INC16(r) { R16W ## r ## _V((R16R ## r + 1) & 0xffff); }

#define DO_DEC16(r) { R16W ## r ## _V((R16R ## r - 1) & 0xffff); }

#define CASES_INCDEC16_R(r) \
	case (0x03+(RNUM(r)<<4)): DO_INC16(r); break; \
	case (0x0b+(RNUM(r)<<4)): DO_DEC16(r); break; \
	

#define CASES_INCDEC16 \
	CASES_INCDEC16_R(BC) \
	CASES_INCDEC16_R(DE) \
	CASES_INCDEC16_R(HL) \
	CASES_INCDEC16_R(SP) \
	


#define CASE_LD_RR(r1, r2) \
	case (0x40+(RNUM(r1)<<3)+(RNUM(r2))): DO_LD_RX(r1, r2); break;

#define CASE_LD_RI(r) \
	case (0x06+(RNUM(r)<<3)): DO_LD_RX(r, IMM8); break;

#define CASE_LD_16_RI(r) \
	case (0x01+(RNUM(r)<<4)): R16W_V(r, IMM16); break;

#define CASES_LD_16(r) \
	CASE_LD_16_RI(r) \

#define CASE_LD_RHL(r) \
	case (0x46+(RNUM(r)<<3)): (r) = READ_V(R16RHL); break;

#define CASES_LD_RX(r) \
	CASE_LD_RR(r, B) \
	CASE_LD_RR(r, C) \
	CASE_LD_RR(r, D) \
	CASE_LD_RR(r, E) \
	CASE_LD_RR(r, H) \
	CASE_LD_RR(r, L) \
	CASE_LD_RR(r, A) \
	CASE_LD_RI(r) \
	CASE_LD_RHL(r) \
	

#define CASES_LD_XX \
	CASES_LD_RX(B) \
	CASES_LD_RX(C) \
	CASES_LD_RX(D) \
	CASES_LD_RX(E) \
	CASES_LD_RX(H) \
	CASES_LD_RX(L) \
	CASES_LD_RX(A) \
	CASES_LD_16(BC) \
	CASES_LD_16(DE) \
	CASES_LD_16(HL) \
	CASES_LD_16(SP) \
	

#define DO_XOR_N(n) { A ^= (n); F = ZFLAG(A); }
#define DO_XOR_V(n) DO_XOR_N(n)

#define DO_OR_N(n) { A |= (n); F = ZFLAG(A); }
#define DO_OR_V(n) DO_OR_N(n)

#define DO_AND_N(n) { A &= (n); F = FH | ZFLAG(A); }
#define DO_AND_V(n) DO_AND_N(n)

#define DO_CP_N(n) DO_CP(n)
#define DO_CP_V(n) { t_vol = (n); DO_CP(t_vol); }

#define DO_ADD_N(n) DO_ADD(n)
#define DO_ADD_V(n) { t_vol = (n); DO_ADD(t_vol); }

#define DO_SUB_N(n) DO_SUB(n)
#define DO_SUB_V(n) { t_vol = (n); DO_SUB(t_vol); }

#define DO_SBC_V(n) { t_vol = (n)+((F>>CF_Shift)&1); DO_SUB(t_vol); }
#define DO_SBC_N(n) DO_SBC_V(n)

#define DO_ADC_V(n) { t_vol = (n)+((F>>CF_Shift)&1); DO_ADD(t_vol); }
#define DO_ADC_N(n) DO_ADC_V(n)

#define CASES_ALU_OP(base, imm, op) \
	case (imm)   : DO_ ## op ## _V(FETCH); break; \
	case (base)  : DO_ ## op ## _N(B); break; \
	case (base)+1: DO_ ## op ## _N(C); break; \
	case (base)+2: DO_ ## op ## _N(D); break; \
	case (base)+3: DO_ ## op ## _N(E); break; \
	case (base)+4: DO_ ## op ## _N(H); break; \
	case (base)+5: DO_ ## op ## _N(L); break; \
	case (base)+6: DO_ ## op ## _V(READ_V(R16RHL)); break; \
	case (base)+7: DO_ ## op ## _N(A); break; \
	

#define DO_SET_R(bitnum, r) { (r) |= (1 << bitnum); }
#define DO_SET_HL(bitnum) { WRITE_V(R16RHL, READ_V(R16RHL) | (1 << bitnum)); }

#define DO_RES_R(bitnum, r) { (r) &= ~(1 << bitnum); }
#define DO_RES_HL(bitnum) { WRITE_V(R16RHL, READ_V(R16RHL) & ~(1 << bitnum)); }

#define DO_BIT_R(bitnum, r) { F = (F & FC) | FH | ZFLAG((r) & (1 << bitnum)); }
#define DO_BIT_HL(bitnum) { DO_BIT_R(bitnum, READ_V(R16RHL)); }

#define CASES_CB_BITNUM_N(base, op, bitnum) \
	case (base)+(bitnum<<3)+0: DO_ ## op ## _R(bitnum, B); break; \
	case (base)+(bitnum<<3)+1: DO_ ## op ## _R(bitnum, C); break; \
	case (base)+(bitnum<<3)+2: DO_ ## op ## _R(bitnum, D); break; \
	case (base)+(bitnum<<3)+3: DO_ ## op ## _R(bitnum, E); break; \
	case (base)+(bitnum<<3)+4: DO_ ## op ## _R(bitnum, H); break; \
	case (base)+(bitnum<<3)+5: DO_ ## op ## _R(bitnum, L); break; \
	case (base)+(bitnum<<3)+7: DO_ ## op ## _R(bitnum, A); break; \
	case (base)+(bitnum<<3)+6: DO_ ## op ## _HL(bitnum); break; \

#define CASES_CB_BITNUM(base, op) \
	CASES_CB_BITNUM_N(base, op, 0) \
	CASES_CB_BITNUM_N(base, op, 1) \
	CASES_CB_BITNUM_N(base, op, 2) \
	CASES_CB_BITNUM_N(base, op, 3) \
	CASES_CB_BITNUM_N(base, op, 4) \
	CASES_CB_BITNUM_N(base, op, 5) \
	CASES_CB_BITNUM_N(base, op, 6) \
	CASES_CB_BITNUM_N(base, op, 7) \
	

#define DO_SHOP_R(op, r) { \
	t_acc = (r) | ((F&FC)<<4); \
	F = ShTables. op ## _flag[t_acc]; \
	(r) = ShTables. op ## _val[t_acc]; \
}
#define DO_SHOP_HL(op) { \
	t_acc = READ_V(R16RHL) | ((F&FC)<<4); \
	F = ShTables. op ## _flag[t_acc]; \
	WRITE_V(R16RHL, ShTables. op ## _val[t_acc]); \
}

#define CASES_CB_SHOP(base, op) \
	case (base)+0: DO_SHOP_R(op, B); break; \
	case (base)+1: DO_SHOP_R(op, C); break; \
	case (base)+2: DO_SHOP_R(op, D); break; \
	case (base)+3: DO_SHOP_R(op, E); break; \
	case (base)+4: DO_SHOP_R(op, H); break; \
	case (base)+5: DO_SHOP_R(op, L); break; \
	case (base)+7: DO_SHOP_R(op, A); break; \
	case (base)+6: DO_SHOP_HL(op); break; \
	

#define CASES_CB_SWAP \
	case 0x30+0: B = Tables.swap[B]; F &= FZ; break; \
	case 0x30+1: C = Tables.swap[C]; F &= FZ; break; \
	case 0x30+2: D = Tables.swap[D]; F &= FZ; break; \
	case 0x30+3: E = Tables.swap[E]; F &= FZ; break; \
	case 0x30+4: H = Tables.swap[H]; F &= FZ; break; \
	case 0x30+5: L = Tables.swap[L]; F &= FZ; break; \
	case 0x30+7: A = Tables.swap[A]; F &= FZ; break; \
	case 0x30+6: WRITE_V(R16RHL, Tables.swap[READ_V(R16RHL)]); F &= FZ; break; \
	

#define ADDSP(n) { \
DW(acc) = (un32)SP + (un32)(n8)(n); \
F = (FH & (((SP>>8) ^ ((n)>>8) ^ HB(acc)) << 1)) \
| (acc.b[HI][LO] << 4); \
SP = W(acc); }

#define LDHLSP(n) { \
DW(acc) = (un32)SP + (un32)(n8)(n); \
F = (FH & (((SP>>8) ^ ((n)>>8) ^ HB(acc)) << 1)) \
| (acc.b[HI][LO] << 4); \
HL = W(acc); }

#define RLCA(r) { (r) = ((r)>>7) | ((r)<<1); \
F = (((r)&0x01)<<4); }

#define RRCA(r) { (r) = ((r)<<7) | ((r)>>1); \
F = (((r)&0x80)>>3); }

#define RLA(r) { \
LB(acc) = (((r)&0x80)>>3); \
(r) = ((r)<<1) | ((F&FC)>>4); \
F = LB(acc); }

#define RRA(r) { \
LB(acc) = (((r)&0x01)<<4); \
(r) = ((r)>>1) | ((F&FC)<<3); \
F = LB(acc); }

#define RLC(r) { RLCA(r); F |= ZFLAG(r); }
#define RRC(r) { RRCA(r); F |= ZFLAG(r); }
#define RL(r) { RLA(r); F |= ZFLAG(r); }
#define RR(r) { RRA(r); F |= ZFLAG(r); }

#define SLA(r) { \
LB(acc) = (((r)&0x80)>>3); \
(r) <<= 1; \
F = ZFLAG((r)) | LB(acc); }

#define SRA(r) { \
LB(acc) = (((r)&0x01)<<4); \
(r) = (un8)(((n8)(r))>>1); \
F = ZFLAG((r)) | LB(acc); }

#define SRL(r) { \
LB(acc) = (((r)&0x01)<<4); \
(r) >>= 1; \
F = ZFLAG((r)) | LB(acc); }

#define CPL(r) { \
(r) = ~(r); \
F |= (FH|FN); }

#define SCF { F = (F & (FZ)) | FC; }

#define CCF { F = (F & (FZ|FC)) ^ FC; }

#define DAA { \
A += (LB(acc) = Tables.daa[((((int)F)&0x70)<<4) | A]); \
F = (F & (FN)) | ZFLAG(A) | Tables.daa_carry[LB(acc)>>2]; }

#define SWAP(r) { \
(r) = Tables.swap[(r)]; \
F = ZFLAG((r)); }

#define BIT(n,r) { F = (F & FC) | ZFLAG(((r) & (1 << (n)))) | FH; }
#define RES(n,r) { (r) &= ~(1 << (n)); }
#define SET(n,r) { (r) |= (1 << (n)); }

#define CB_REG_CASES(r, n) \
case 0x00|(n): RLC(r); break; \
case 0x08|(n): RRC(r); break; \
case 0x10|(n): RL(r); break; \
case 0x18|(n): RR(r); break; \
case 0x20|(n): SLA(r); break; \
case 0x28|(n): SRA(r); break; \
case 0x30|(n): SWAP(r); break; \
case 0x38|(n): SRL(r); break; \
case 0x40|(n): BIT(0, r); break; \
case 0x48|(n): BIT(1, r); break; \
case 0x50|(n): BIT(2, r); break; \
case 0x58|(n): BIT(3, r); break; \
case 0x60|(n): BIT(4, r); break; \
case 0x68|(n): BIT(5, r); break; \
case 0x70|(n): BIT(6, r); break; \
case 0x78|(n): BIT(7, r); break; \
case 0x80|(n): RES(0, r); break; \
case 0x88|(n): RES(1, r); break; \
case 0x90|(n): RES(2, r); break; \
case 0x98|(n): RES(3, r); break; \
case 0xA0|(n): RES(4, r); break; \
case 0xA8|(n): RES(5, r); break; \
case 0xB0|(n): RES(6, r); break; \
case 0xB8|(n): RES(7, r); break; \
case 0xC0|(n): SET(0, r); break; \
case 0xC8|(n): SET(1, r); break; \
case 0xD0|(n): SET(2, r); break; \
case 0xD8|(n): SET(3, r); break; \
case 0xE0|(n): SET(4, r); break; \
case 0xE8|(n): SET(5, r); break; \
case 0xF0|(n): SET(6, r); break; \
case 0xF8|(n): SET(7, r); break;

#define PRE_INT ( DI, PUSH(PC) )
#define THROW_INT(n) ( (IF &= ~(1<<(n))), (PC = 0x40+((n)<<3)) )
