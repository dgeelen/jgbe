import java.io.*;

public class Disassembler
{
    private static Cartridge cart;
    private static CPU cpu;
    private static String opcode[];
    private static final String file_name = "instrs.txt";

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
      if(reg.equalsIgnoreCase("AF")) return (cpu.regs[cpu.A]<<8)|cpu.regs[cpu.F];
      if(reg.equalsIgnoreCase("BC")) return (cpu.regs[cpu.B]<<8)|cpu.regs[cpu.C];
      if(reg.equalsIgnoreCase("DE")) return (cpu.regs[cpu.D]<<8)|cpu.regs[cpu.E];
      if(reg.equalsIgnoreCase("HL")) return (cpu.regs[cpu.H]<<8)|cpu.regs[cpu.L];
      if(reg.equalsIgnoreCase("SP")) return cpu.regs[cpu.SP];
      if(reg.equalsIgnoreCase("PC")) return cpu.regs[cpu.PC];
      return -1;
    }

    public static final String disassemble(int PC) {
      String op = opcode[cart.read(PC)];
      System.out.println("PC+1="+cart.read(PC+1)+" PC+2="+cart.read(PC+2)+" PC+3="+cart.read(PC+3));
      String s="";
      int immediate=-1;
      if(op.indexOf("IMM16")>-1) {
        //immediate= (cart.read(PC+1)<<8)|cart.read(PC+2);
        immediate=(cart.read(PC+2)<<8)|cart.read(PC+1); //little endian
      }
      if(op.indexOf("IMM08")>-1) {
        immediate= cart.read(PC+1);
      }
      System.out.println("immediate="+immediate);
      if(op.indexOf("[")>-1) {
        int i = op.indexOf("[");
        int j = op.indexOf("]");
        immediate = Math.max(regval(op.substring(i+1,j)), immediate);
        s=String.format(op.substring(0,i+1)+"$%04x"+op.substring(j),immediate);
      }
      else if(immediate>-1) {
        int i = op.indexOf("IMM16");
        if(i>-1) {
          s=String.format(op.substring(0,i)+"$%04x"+op.substring(i+5),immediate);
          }
        else {
          i=op.indexOf("IMM08");
          s=String.format(op.substring(0,i)+"$%02x"+op.substring(i+5),immediate);
        }
      }
      else {
        s = op;
      }
      return s  ;
    }

    public static void main(String[] args)
    {
        System.out.println(Disassembler.disassemble(0));
        System.out.println(Disassembler.disassemble(0));
        System.out.println(Disassembler.disassemble(255));
        System.out.println(Disassembler.disassemble(255));
    }
}