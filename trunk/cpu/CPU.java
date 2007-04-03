public class CPU
{
    private static final int CARRY8b    = 512;
    private static final int CARRY8b_SHR = 5;
    private static final int ZF = 1<<7;
    private static final int NF = 1<<6;
    private static final int FLAG_REG = 5;
    // Half carry van de 8b regs
    private static final int HALF_CARRY8b = 16;
    private static final int HALF_CARRY8b_SHL = 2;

    private int[] regs = new int[8]; //[A,B,C,D,E,F,H,L]
    private static final int A = 0;
    private static final int B = 1;
    private static final int C = 2;
    private static final int D = 3;
    private static final int E = 4;
    private static final int F = 5;
    private static final int H = 6;
    private static final int L = 7;

    private void inc8b(int reg_index)
    {
        // hcb: half carry before increase
        int hcb = regs[reg_index] & HALF_CARRY8b;
        regs[reg_index] = (++regs[reg_index] & 0xFF);
        // hcb: half carry after increase
        int hca = regs[reg_index] & HALF_CARRY8b;
        // Set halfcarry flag
        regs[FLAG_REG] = regs[FLAG_REG] | ((hca ^hcb) << HALF_CARRY8b_SHL);
        // clear ZF
        regs[FLAG_REG] = regs[FLAG_REG] & ~ZF;
        // Set Z flag
        regs[FLAG_REG] = regs[FLAG_REG] | ((regs[reg_index]==0)?1:0<<ZF);
        // SET n flag
        regs[FLAG_REG] = regs[FLAG_REG] & ~NF;
    }

    private void dec8b(int reg_index)
    {
        // hcb: half carry before increase
        int hcb = regs[reg_index] & HALF_CARRY8b;
        regs[reg_index] = (--regs[reg_index] & 0xFF);
        // hcb: half carry after increase
        int hca = regs[reg_index] & HALF_CARRY8b;
        // Set halfcarry flag
        regs[FLAG_REG] = regs[FLAG_REG] | ((hca ^hcb) << HALF_CARRY8b_SHL);
        // clear ZF
        regs[FLAG_REG] = regs[FLAG_REG] & ~ZF;
        // Set Z flag
        regs[FLAG_REG] = regs[FLAG_REG] | ((regs[reg_index]==0)?1:0<<ZF);
        // SET n flag
        regs[FLAG_REG] = regs[FLAG_REG] | NF;
    }

    private void inc16b()
    {
    }

    private int fetch()
    {
        return 0x00004;
    }

    private boolean excecute(int instr)
    {
        switch(instr)
        {
            case 0x0004:
                // INC B
                inc8b(B);
                break;
            default:
                System.out.println("UNKNOWN INSTRUCTION: " + instr);
                return false;
        }

        return true;
    }
}