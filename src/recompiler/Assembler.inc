// this is included in the CPURecompiler class

	/** provide acces to ClassLoader.defineClass(), which is protected */
	private class ByteClassLoader extends ClassLoader {
		public Class byteClass(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}
	}
	private ByteClassLoader loader = new ByteClassLoader();

	/** id counter to ensure classname uniqueness */
	public static long blockidcounter = 0;

	/** test for testing */
	private CPURunBlock Assemble(VECTOR(SimpleAssign) assigns) { // TODO args...
		//PRINTLN("Creating class 'block_" + blockidcounter +"'");

		String classname = "block_" + (blockidcounter++);

		ClassGen cg = new ClassGen(classname,
		                           "java.lang.Object", "", Constants.ACC_PUBLIC,
		                           new String[] {"CPURunBlock"} );

		// That's important, otherwise newInstance() won't work
		cg.addEmptyConstructor(Constants.ACC_PUBLIC);

		InstructionList    il      = new InstructionList();
		ConstantPoolGen    cp      = cg.getConstantPool();
		InstructionFactory factory = new InstructionFactory(cg);

		MethodGen mg = new MethodGen(Constants.ACC_PUBLIC|Constants.ACC_FINAL, Type.INT,
		                             null, null, "execute", "foo", il, cp);

		FOREACH(SimpleAssign, sa, assigns) {
			sa.doAssemble(il, cp);
		}

		int rn = removeNOPs(il);
		//PRINTLN("NOPs removed: " + rn);

		mg.stripAttributes(true);
		mg.setMaxStack();
		mg.setMaxLocals();
		cg.addMethod(mg.getMethod()); 

		byte[] bytes = cg.getJavaClass().getBytes();

		try {
			cg.getJavaClass().dump("/tmp/tclass.class");
			return (CPURunBlock)(loader.byteClass(classname, bytes).newInstance());
		}
		catch (Exception e) {
			e.printStackTrace();
			PRINTLN("exception instantiation recompiled block");
			return null;
		}
	}

	/** stub which assembles for block at PC=$2cbb in Bionic Commando */
	private CPURunBlock Assemble() { // TODO args...
		PRINTLN("Creating class 'block_" + blockidcounter);

		String classname = "block_" + (blockidcounter++);

		ClassGen cg = new ClassGen(classname,
		                           "java.lang.Object", "", Constants.ACC_PUBLIC,
		                           new String[] {"CPURunBlock"});

		// That's important, otherwise newInstance() won't work
		cg.addEmptyConstructor(Constants.ACC_PUBLIC);

		InstructionList    il      = new InstructionList();
		ConstantPoolGen    cp      = cg.getConstantPool();
		InstructionFactory factory = new InstructionFactory(cg);

		int pcref = cp.addFieldref("CPU", "PC", Type.INT.getSignature());
		int href = cp.addFieldref("CPU", "H", Type.INT.getSignature());
		int lref = cp.addFieldref("CPU", "L", Type.INT.getSignature());
		int aref = cp.addFieldref("CPU", "A", Type.INT.getSignature());
		int fref = cp.addFieldref("CPU", "F", Type.INT.getSignature());

		int memread = cp.addMethodref("CPU", "read", Type.getMethodSignature(Type.INT, new Type[] { Type.INT }));

		MethodGen mg = new MethodGen(Constants.ACC_PUBLIC|Constants.ACC_FINAL, Type.INT, null, null, "execute", "foo", il, cp);

		il.append(new PUSH(cp, 0xc590));
		il.append(new INVOKESTATIC(memread));

		//il.append(new ISTORE(0));
		il.append(InstructionConstants.DUP);

		il.append(new PUSH(cp, 0xc5));
		il.append(new PUTSTATIC(href));

		il.append(new PUSH(cp, 0x40));
		il.append(new PUTSTATIC(lref));

		//il.append(new ILOAD(0));
		il.append(new PUTSTATIC(aref));

		//il.append(new ILOAD(0));
		il.append(new PUSH(cp, 0xc540));
		il.append(new INVOKESTATIC(memread));

		IF_ICMPGE tif = new IF_ICMPGE(null);
		il.append(tif);

		il.append(new PUSH(cp, 0x2cc6));
		il.append(new PUTSTATIC(pcref));

		il.append(new PUSH(cp, CPU.CF_Mask));
		il.append(new PUTSTATIC(fref));

		il.append(new PUSH(cp, 12));
		il.append(InstructionConstants.IRETURN);

		InstructionHandle target =
			il.append(new PUSH(cp, 0x2cc4));
		il.append(new PUTSTATIC(pcref));

		il.append(new PUSH(cp, 0));
		il.append(new PUTSTATIC(fref));

		il.append(new PUSH(cp, 11));
		il.append(InstructionConstants.IRETURN);

		tif.setTarget(target);

		mg.stripAttributes(true);
		mg.setMaxStack();
		mg.setMaxLocals();
		cg.addMethod(mg.getMethod());


		byte[] bytes = cg.getJavaClass().getBytes();

		try {
			cg.getJavaClass().dump("/tmp/tclass.class");
			return (CPURunBlock)(loader.byteClass(classname, bytes).newInstance());
		}
		catch (Exception e) {
			e.printStackTrace();
			PRINTLN("exception instantiation recompiled block");
			return null;
		}
	}
