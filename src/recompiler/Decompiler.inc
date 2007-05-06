#include "../CPU.inc"

#include "CPUMacros.inc"

private VECTOR(SimpleAssign) Decompile(int PC) {
	VECTOR(SimpleAssign) result = new VECTOR(SimpleAssign)();
	boolean done = false;
	int max = 100;
	while (!done) {
		int opcode = FETCH;
		switch (opcode) {
			case 0x00: break;
			case 0xc3: DO_JP_NN; break;                         // JP   (nn)
			case 0x18: DO_JR_D; break;                          // JR   (d)
			CASES_LD_XX
			default: {
				--PC;
				done = true;
			}
		}

		if (PC > 0x4000) done = true;
		if (--max == 0) done = true;
	}
	ASS_S_I(PC, PC);
	return result;
}
