#define REF_REG(name) cp.addFieldref("CPU", name, Type.INT.getSignature())
#define REF_CPUVAR_BOOL(name) cp.addFieldref("CPU", name, Type.BOOLEAN.getSignature())
#define REF_READ cp.addMethodref("CPU", "read", Type.getMethodSignature(Type.INT, new Type[] { Type.INT }))
#define REF_WRITE cp.addMethodref("CPU", "write_fast1", Type.getMethodSignature(Type.VOID, new Type[] { Type.INT , Type.INT }))

#define SAList VECTOR(SimpleAssign)

abstract class Variable extends Expression {
	abstract public void doAssembleStore(InstructionList il, ConstantPoolGen cp);
	public Expression expandVars(SAList sal, Int ret) {
		FOREACH(SimpleAssign, sa, sal) {
			if (equals(sa.target)) {
				++ret.value;
				return sa.value;
			}
		}
		return this;
	}
}


class SimpleVariable extends Variable {
	private String name;
	private int bitmask;
	public SimpleVariable(String aname) {
		name = aname;
		bitmask = 0xff;
		if (name.equals("PC")) bitmask = 0xffff;
		if (name.equals("SP")) bitmask = 0xffff;
	}
	String getName() { return name; };
	public String asString() { return name; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		il.append(new GETSTATIC(REF_REG(name)));
	};
	public void doAssembleStore(InstructionList il, ConstantPoolGen cp) {
		il.append(new PUTSTATIC(REF_REG(name)));
	};
	public boolean equals(Object v) {
		if (!(v instanceof SimpleVariable))
			return false;
		return name.equals(((SimpleVariable)v).name);
	}
	public int bitMask() {
		return bitmask;
	}
}

class SimpleBoolVariable extends Variable {
	private String name;
	public SimpleBoolVariable(String aname) {
		name = aname;
	}
	String getName() { return name; };
	public String asString() { return name; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		il.append(new GETSTATIC(REF_CPUVAR_BOOL(name)));
	};
	public void doAssembleStore(InstructionList il, ConstantPoolGen cp) {
		il.append(new PUTSTATIC(REF_CPUVAR_BOOL(name)));
	};
	public boolean equals(Object v) {
		if (!(v instanceof SimpleBoolVariable))
			return false;
		return name.equals(((SimpleBoolVariable)v).name);
	}
	public int bitMask() {
		return 1;
	}
}

class TemporaryVariable extends Variable {
	private int number;
	public TemporaryVariable(int n) {
		number = n;
	}
	int getNumber() { return number; };
	public String asString() { return "TEMP_"+number; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		il.append(new ILOAD(number));
	};
	public void doAssembleStore(InstructionList il, ConstantPoolGen cp) {
		il.append(new ISTORE(number));
	};
	public boolean equals(Object v) {
		if (!(v instanceof TemporaryVariable))
			return false;
		return number == (((TemporaryVariable)v).number);
	}
	public int bitMask() {
		return 0xffff;
	}
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
	public boolean equals(Object v) {
		if (!(v instanceof MemoryVariable))
			return false;
		return addr.equals(((MemoryVariable)v).addr);
	}
	public int bitMask() {
		return 0xff;
	}
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
	public boolean equals(Object v) {
		return (v instanceof ReturnVariable);
	}
	public int bitMask() {
		return 0xffff;
	}
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
	public int bitMask() {
		return CPU.ZF_Mask;
	}
}

class FunctionIIF extends FunctionExpression {
	public String funcName() { return "IIF"; };
	public FunctionIIF(Expression expr0, Expression expr1, Expression expr2) {
		super(new Expression[] { expr0, expr1, expr2 } );
	};
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);

		IFNE ti1 = new IFNE(null);
		il.append(ti1);

		args[2].doAssembleValue(il, cp);

		GOTO ti2 = new GOTO(null);
		il.append(ti2);

		InstructionHandle th1;
		th1 = il.append(InstructionConstants.NOP);

		args[1].doAssembleValue(il, cp);

		InstructionHandle th2;
		th2 = il.append(InstructionConstants.NOP);

		ti1.setTarget(th1);
		ti2.setTarget(th2);
	}
	public int bitMask() {
		return args[1].bitMask() | args[2].bitMask();
	}
}

abstract class BinaryFunction extends FunctionExpression {
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
	public int bitMask() {
		return args[1].bitMask() & args[2].bitMask();
	}
}
#define FAND(x,y) (new FunctionAND((x),(y)))

class FunctionOR extends BinaryFunction {
	public FunctionOR(Expression expr1, Expression expr2) {
		super("or", InstructionConstants.IOR, expr1, expr2);
	}
	public int bitMask() {
		return args[1].bitMask() & args[2].bitMask();
	}
}
#define F_OR(x,y) (new FunctionOR((x),(y)))

class FunctionXOR extends BinaryFunction {
	public FunctionXOR(Expression expr1, Expression expr2) {
		super("xor", InstructionConstants.IXOR, expr1, expr2);
	}
	public int bitMask() {
		return args[1].bitMask() | args[2].bitMask();
	}
}

class FunctionDEC extends BinaryFunction {
	public FunctionDEC(Expression expr1, Expression expr2) {
		super("dec", InstructionConstants.ISUB, expr1, expr2);
	}
	public int bitMask() {
		return 0xffff; // TODO: take into account arguments width
	}
}
#define FDEC(x,y) (new FunctionDEC((x),(y)))

class FunctionINC extends BinaryFunction {
	public FunctionINC(Expression expr1, Expression expr2) {
		super("inc", InstructionConstants.IADD, expr1, expr2);
	}
	public int bitMask() {
		return 0xffff; // TODO: take into account arguments width
	}
}
#define FINC(x,y) (new FunctionINC((x),(y)))

class FunctionSHL extends BinaryFunction {
	public FunctionSHL(Expression expr1, Expression expr2) {
		super("shl", InstructionConstants.ISHL, expr1, expr2);
	}
	public int bitMask() {
		if (args[1] instanceof ConstantExpression)
			return args[0].bitMask() << ((ConstantExpression)args[1]).value;
		return 0xffff;
	}
}
#define FSHL(x,y) (new FunctionSHL((x),(y)))

class FunctionSHR extends BinaryFunction {
	public FunctionSHR(Expression expr1, Expression expr2) {
		super("shr", InstructionConstants.ISHR, expr1, expr2);
	}
	public int bitMask() {
		if (args[1] instanceof ConstantExpression)
			return args[0].bitMask() >> ((ConstantExpression)args[1]).value;
		return 0xffff;
	}
}
#define FSHR(x,y) (new FunctionSHR((x),(y)))

abstract class Expression {
	abstract public String asString();
	abstract public void doAssembleValue(InstructionList il, ConstantPoolGen cp);
	abstract public Expression expandVars(SAList sal, Int res);
	abstract public int bitMask();
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
	public Expression expandVars(SAList sal, Int res) {
		for (int i = 0; i < args.length; ++i) {
			args[i] = args[i].expandVars(sal, res);
		}
		return this;
	}
	public boolean equals(Object v) {
		if (!(v instanceof FunctionExpression))
			return false;
		FunctionExpression o = (FunctionExpression)v;
		if (args.length != o.args.length)
			return false;
		for (int i = 0; i < args.length; ++i)
			if (!args[i].equals(o.args[i]))
				return false;
		return true;
	}
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
	public boolean equals(Object v) {
		if (!(v instanceof ConstantExpression))
			return false;
		return value == (((ConstantExpression)v).value);
	}
	public Expression expandVars(SAList sal, Int res) {
		return this;
	}
	public int bitMask() {
		return value;
	}
}

class ConstantBoolExpression extends Expression {
	private boolean value;
	public ConstantBoolExpression(boolean avalue) {
		value = avalue;
	};
	public boolean getValue() { return value; };
	public String asString() { return "" + value; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		il.append(new PUSH(cp, value));
	};
	public boolean equals(Object v) {
		if (!(v instanceof ConstantBoolExpression))
			return false;
		return value == (((ConstantBoolExpression)v).value);
	}
	public Expression expandVars(SAList sal, Int res) {
		return this;
	}
	public int bitMask() {
		return value ? 1 : 0;
	}
}

class Int {
	public int value;
	public Int(int val) {
		value = val;
	}
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
	public int expandVars(SAList sal) {
		Int ret = new Int(0);
		if (target instanceof MemoryVariable) {
			MemoryVariable mv = (MemoryVariable)target;
			mv.addr = mv.addr.expandVars(sal, ret);
		}
		value = value.expandVars(sal, ret);
		return ret.value;
	}
}

