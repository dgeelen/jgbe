#define REF_REG(name) cp.addFieldref("CPU", name, Type.INT.getSignature())
#define REF_READ cp.addMethodref("CPU", "read", Type.getMethodSignature(Type.INT, new Type[] { Type.INT }))
#define REF_WRITE cp.addMethodref("CPU", "write_fast1", Type.getMethodSignature(Type.VOID, new Type[] { Type.INT , Type.INT }))

abstract class Variable extends Expression {
	abstract public void doAssembleStore(InstructionList il, ConstantPoolGen cp);
}

class SimpleVariable extends Variable {
	private String name;
	public SimpleVariable(String aname) {
		name = aname;
	}
	String getName() { return name; };
	public String asString() { return name; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		il.append(new GETSTATIC(REF_REG(name)));
	};
	public void doAssembleStore(InstructionList il, ConstantPoolGen cp) {
		il.append(new PUTSTATIC(REF_REG(name)));
	};
}

class MemoryVariable extends Variable {
	private Expression addr;
	public MemoryVariable(Expression ahaddr, Expression aladdr) {
		addr = new FunctionOR(new FunctionSHL(ahaddr, new ConstantExpression(8)), aladdr);
	};
	public MemoryVariable(Expression aaddr) {
		addr = aaddr;
	};
	public Expression getAddr() { return addr; };
	public String asString() { return "MEM["+addr.asString()+"]"; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		addr.doAssembleValue(il, cp);
		il.append(new INVOKESTATIC(REF_READ));
	};
	public void doAssembleStore(InstructionList il, ConstantPoolGen cp) {
		addr.doAssembleValue(il, cp);
		il.append(new INVOKESTATIC(REF_WRITE));
	};
}

class ReturnVariable extends Variable {
	public ReturnVariable() { };
	public String asString() { return "return"; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		ASSERT(false);
	};
	public void doAssembleStore(InstructionList il, ConstantPoolGen cp) {
		il.append(InstructionConstants.IRETURN);
	};
}

class FunctionCALCZF extends FunctionExpression {
	public String funcName() { return "calczf"; };
	public FunctionCALCZF(Expression expr) {
		super(new Expression[] { expr } );
	};
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);

		IFNE ti1 = new IFNE(null);
		il.append(ti1);

		il.append(new PUSH(cp, CPU.ZF_Mask));

		GOTO ti2 = new GOTO(null);
		il.append(ti2);

		InstructionHandle th1;
		th1 = il.append(new PUSH(cp, 0));

		InstructionHandle th2;
		th2 = il.append(InstructionConstants.NOP);

		ti1.setTarget(th1);
		ti2.setTarget(th2);
	}
}

class BinaryFunction extends FunctionExpression {
	private String fname;
	private Instruction instr;
	public String funcName() { return fname; };
	public BinaryFunction(String afname, Instruction ainstr, Expression expr1, Expression expr2) {
		super(new Expression[] { expr1, expr2 } );
		fname = afname;
		instr = ainstr;
	}
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);
		args[1].doAssembleValue(il, cp);
		il.append(instr);
	}
}

class FunctionAND extends BinaryFunction {
	public FunctionAND(Expression expr1, Expression expr2) {
		super("and", InstructionConstants.IAND, expr1, expr2);
	}
}

class FunctionOR extends BinaryFunction {
	public FunctionOR(Expression expr1, Expression expr2) {
		super("or", InstructionConstants.IOR, expr1, expr2);
	}
}

class FunctionXOR extends BinaryFunction {
	public FunctionXOR(Expression expr1, Expression expr2) {
		super("xor", InstructionConstants.IXOR, expr1, expr2);
	}
}

class FunctionDEC extends BinaryFunction {
	public FunctionDEC(Expression expr1, Expression expr2) {
		super("dec", InstructionConstants.ISUB, expr1, expr2);
	}
}

class FunctionINC extends BinaryFunction {
	public FunctionINC(Expression expr1, Expression expr2) {
		super("inc", InstructionConstants.IADD, expr1, expr2);
	}
}

class FunctionSHL extends BinaryFunction {
	public FunctionSHL(Expression expr1, Expression expr2) {
		super("shl", InstructionConstants.ISHL, expr1, expr2);
	}
}

class FunctionSHR extends BinaryFunction {
	public FunctionSHR(Expression expr1, Expression expr2) {
		super("shr", InstructionConstants.ISHR, expr1, expr2);
	}
}

abstract class Expression {
	abstract public String asString();
	abstract public void doAssembleValue(InstructionList il, ConstantPoolGen cp);
}

abstract class FunctionExpression extends Expression {
	abstract String funcName();
	protected Expression[] args;
	protected FunctionExpression(Expression[] aargs) {
		args = aargs;
	}
	protected FunctionExpression() {
		args = new Expression[] { };
	}
	public Expression[] getArgs() { return args; };
	public String asString() {
		StringBuilder s = new StringBuilder() ;
		s.append(funcName());
		s.append("(");
		for (int i = 0; ;) {
			s.append(args[i].asString());
			if (++i == args.length)
				break;
		  s.append(" , ");
		}
		s.append(")");
		return s.toString();
	};
}

class ConstantExpression extends Expression {
	private int value;
	public ConstantExpression(int avalue) {
		value = avalue;
	};
	public int getValue() { return value; };
	public String asString() { return "" + value; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		il.append(new PUSH(cp, value));
	};
}

/** Assignment into which the GB asm is transformed into first */
class SimpleAssign {
	private Variable target;
	private Expression value;
	public SimpleAssign(Variable atarget, Expression avalue) {
		target = atarget;
		value = avalue;
	}
	public Variable getTarget() { return target; };
	public Expression getValue() { return value; };
	public String asString() { return target.asString() + " <- " + value.asString(); };
	public void doAssemble(InstructionList il, ConstantPoolGen cp) {
		value.doAssembleValue(il, cp);
		target.doAssembleStore(il, cp);
	}
}

