
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
		il.append(new GETSTATIC(cp.addFieldref("CPU", name, Type.INT.getSignature())));
	};
	public void doAssembleStore(InstructionList il, ConstantPoolGen cp) {
		il.append(new PUTSTATIC(cp.addFieldref("CPU", name, Type.INT.getSignature())));
	};
}

class MemoryVariable extends Variable {
	private Expression laddr, haddr;
	public MemoryVariable(Expression ahaddr, Expression aladdr) {
		laddr = aladdr;
		haddr = ahaddr;
	}
	public Expression getLAddr() { return laddr; };
	public Expression getHAddr() { return haddr; };
	public String asString() { return "MEM["+haddr.asString()+","+laddr.asString()+"]"; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {};
	public void doAssembleStore(InstructionList il, ConstantPoolGen cp) {};
}

class Function {
	private Function() {};
}

abstract class Expression {
	abstract public String asString();
	abstract public void doAssembleValue(InstructionList il, ConstantPoolGen cp);
}

class FunctionExpression extends Expression {
	private Function func;
	private Expression[] args;
	public FunctionExpression(Function afunc, Expression[] aargs) {
		func = afunc;
		args = aargs;
	}
	public Function getFunc() { return func; };
	public Expression[] getArgs() { return args; };
	public String asString() { return " func "; };
	public void doAssembleValue(InstructionList il, ConstantPoolGen cp) {};
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
