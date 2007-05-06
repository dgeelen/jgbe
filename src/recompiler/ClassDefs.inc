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


class FunctionAND extends FunctionExpression {
	public String funcName() { return "and"; };
	public FunctionAND(Expression expr1, Expression expr2) {
		super(new Expression[] { expr1, expr2 } );
	}
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);
		args[1].doAssembleValue(il, cp);
		il.append(InstructionConstants.IAND);
	}
}

class FunctionDEC extends FunctionExpression {
	public String funcName() { return "dec"; };
	public FunctionDEC(Expression expr1, Expression expr2) {
		super(new Expression[] { expr1, expr2 } );
	}
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);
		args[1].doAssembleValue(il, cp);
		il.append(InstructionConstants.ISUB);
	}
}

class FunctionINC extends FunctionExpression {
	public String funcName() { return "inc"; };
	public FunctionINC(Expression expr1, Expression expr2) {
		super(new Expression[] { expr1, expr2 } );
	}
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);
		args[1].doAssembleValue(il, cp);
		il.append(InstructionConstants.IADD);
	}
}

class FunctionOR extends FunctionExpression {
	public String funcName() { return "or"; };
	public FunctionOR(Expression expr1, Expression expr2) {
		super(new Expression[] { expr1, expr2 } );
	}
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);
		args[1].doAssembleValue(il, cp);
		il.append(InstructionConstants.IOR);
	}
}

class FunctionXOR extends FunctionExpression {
	public String funcName() { return "xor"; };
	public FunctionXOR(Expression expr1, Expression expr2) {
		super(new Expression[] { expr1, expr2 } );
	}
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);
		args[1].doAssembleValue(il, cp);
		il.append(InstructionConstants.IXOR);
	}
}

class FunctionSHL extends FunctionExpression {
	public String funcName() { return "shl"; };
	public FunctionSHL(Expression expr1, Expression expr2) {
		super(new Expression[] { expr1, expr2 } );
	}
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);
		args[1].doAssembleValue(il, cp);
		il.append(InstructionConstants.ISHL);
	}
}

class FunctionSHR extends FunctionExpression {
	public String funcName() { return "shr"; };
	public FunctionSHR(Expression expr1, Expression expr2) {
		super(new Expression[] { expr1, expr2 } );
	}
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {
		args[0].doAssembleValue(il, cp);
		args[1].doAssembleValue(il, cp);
		il.append(InstructionConstants.ISHR);
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

