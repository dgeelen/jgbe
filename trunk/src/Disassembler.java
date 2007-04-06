import java.io.*;

public class Disassembler
{
    private static Cartridge cart;
    private static CPU cpu;
    private static String opcode[];
    private static final String file_name = "instrs.txt";
		private static final char[] whitespace = {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};//FIXME: this is crappy
    public Disassembler(Cartridge cart, CPU cpu) {
      this.cart=cart;
      this.cpu=cpu;
      this.opcode=new String[512];
      BufferedReader reader;
      try {
        FileInputStream fistream = new FileInputStream(file_name);
        reader = new BufferedReader(new InputStreamReader(fistream));
        try {
          for(int i=0; i < 512 ; ++i) {
            opcode[i]=reader.readLine();
            //System.out.println(opcode[i]);
          }
        }
        catch (IOException ioe) {
          System.out.println("ERROR while reading " + file_name + "!");
        }
      }
      catch (FileNotFoundException fnfe) {
        System.out.println("ERROR while loading file " + file_name + "!");
      }
    }

    // Opcode: opcode to disassemble
    // BC:     whether opcode was after BC
    private static int regval(String reg) {
      if(reg.equalsIgnoreCase("A")) return cpu.regs[cpu.A];
      if(reg.equalsIgnoreCase("B")) return cpu.regs[cpu.B];
      if(reg.equalsIgnoreCase("C")) return cpu.regs[cpu.C];
      if(reg.equalsIgnoreCase("D")) return cpu.regs[cpu.D];
      if(reg.equalsIgnoreCase("E")) return cpu.regs[cpu.E];
      if(reg.equalsIgnoreCase("H")) return cpu.regs[cpu.H];
      if(reg.equalsIgnoreCase("L")) return cpu.regs[cpu.L];
      if(reg.equalsIgnoreCase("AF")) return 0x10000|((cpu.regs[cpu.A]<<8)|cpu.regs[cpu.F]);
      if(reg.equalsIgnoreCase("BC")) return 0x10000|((cpu.regs[cpu.B]<<8)|cpu.regs[cpu.C]);
      if(reg.equalsIgnoreCase("DE")) return 0x10000|((cpu.regs[cpu.D]<<8)|cpu.regs[cpu.E]);
      if(reg.equalsIgnoreCase("HL")) return 0x10000|((cpu.regs[cpu.H]<<8)|cpu.regs[cpu.L]);
      if(reg.equalsIgnoreCase("SP")) return 0x10000|(cpu.SP);
      if(reg.equalsIgnoreCase("PC")) return 0x10000|(cpu.PC);
      return -1;
    }

    public static final String disassemble(int PC) {
      int instr=cart.read(PC);
      int immediate=-1;
      int i=-1;
      int j=-1;
      int bytecount=1;
      String op="";
      if(instr==0xcb) {
        op = opcode[instr+0x100];
      }
      else {
        op = opcode[instr];
      }
			String s=op;
      i=op.indexOf("IMM08");
      if(i>-1) {
        immediate= cart.read(PC+1);
        s=String.format(op.substring(0,i)+"$%02x"+op.substring(i+5),immediate);
        bytecount=2;
			}
			i=op.indexOf("IMM16");
			if(i>-1) {
				immediate=(cart.read(PC+2)<<8)|cart.read(PC+1); //little endian
				s=String.format(op.substring(0,i)+"$%04x"+op.substring(i+5),immediate);
				bytecount=3;
			}
			i=op.lastIndexOf(" ");
			if(i>-1) {
				j=op.lastIndexOf(",", i-1);
				if(j>-1) {//2 parts
					//part 1
					int k=op.length()-1;
					if(op.charAt(i+1)=='[') { ++i; k--; }; //TODO:Verify this does what I want
					immediate=regval(op.substring(i+1, k));
					if(immediate>-1) {
						if(immediate>0xffff) {
							s=String.format(s.substring(0,i+1)+"$%04x"+s.substring(i+3),immediate&0xffff);
							}
						else {
							s=String.format(s.substring(0,i+1)+"$%02x"+s.substring(i+2),immediate);
						}
					}
					//part 2
					i=op.lastIndexOf(" ",j);
					if(op.charAt(i+1)=='[') { ++i; j--; }; //TODO:Verify this does what I want
					immediate=regval(op.substring(i+1,j));
					if(immediate>-1) {
						if(immediate>0xffff) {
							s=String.format(s.substring(0,i+1)+"$%04x"+s.substring(i+3),immediate&0xffff);
							}
						else {
							s=String.format(s.substring(0,i+1)+"$%02x"+s.substring(i+2),immediate);
						}
					}
				}
				else { // only 1 part
					immediate=regval(op.substring(i+1));
					if(immediate>-1) {
						if(immediate>0xffff) {
							s=String.format(s.substring(0,i+1)+"$%04x"+s.substring(i+3),immediate&0xffff);
						}
						else {
							s=String.format(s.substring(0,i+1)+"$%02x"+s.substring(i+2),immediate);
						}
					}
				}
			}
			i=s.indexOf("[n]");
			if(i>-1) { //specialcase
				immediate=cart.read(PC+1);
				s=String.format(s.substring(0,i+1)+"$%04x"+s.substring(i+2),immediate);
			}

			String prefix=String.format("$%04x ",PC);
			for(i=0; i<bytecount;  ++i) {
				prefix+=String.format("$%02x ", cart.read(PC+i));
			}
			for(i=0; i<3-bytecount; ++i){
			  prefix+=String.format("    ", cart.read(PC+i));
			}
      return prefix + s + (new String(whitespace, 0, 18 - s.length())) + "// "+op ;
    }

    public static void main(String[] args)
    {
        System.out.println(Disassembler.disassemble(0));
        System.out.println(Disassembler.disassemble(0));
        System.out.println(Disassembler.disassemble(255));
        System.out.println(Disassembler.disassemble(255));
    }
}


/*      if(op.indexOf("IMM16")>-1) {
        //immediate= (cart.read(PC+1)<<8)|cart.read(PC+2);
        immediate=(cart.read(PC+2)<<8)|cart.read(PC+1); //little endian
      }
      if(op.indexOf("IMM08")>-1) {
        immediate= cart.read(PC+1);
      }
      if(op.indexOf("[")>-1) {
        i = op.indexOf("[");
        int j = op.indexOf("]");
        immediate = Math.max(regval(op.substring(i+1,j)), immediate);
        if(immediate == -1){ //One of the special cases (LDH etc)
            if(op.indexOf("LDH")>-1) {
                immediate=0xff00|cart.read(PC+1);
            }
        }
        s=String.format(op.substring(0,i+1)+"$%04x"+op.substring(j),immediate);
      }
      if(immediate>-1) {
        i = op.indexOf("IMM16");
        if(i>-1) {
          s=String.format(op.substring(0,i)+"$%04x"+op.substring(i+5),immediate);
          }
        else {
          i=op.indexOf("IMM08");
          s=String.format(op.substring(0,i)+"$%02x"+op.substring(i+5),immediate);
        }
      }
      if((i=op.indexOf(","))>-1) {
      	int j=op.indexOf(" ");
      	immediate = regval(op.substring(j,i));
				System.out.println(op.substring(j,i)+"= "+immediate);

      }*/
