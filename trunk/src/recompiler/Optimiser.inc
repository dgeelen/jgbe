/** removes NOP instructions from passed Instruction List */
private static final int removeNOPs(InstructionList il) {
	InstructionFinder f     = new InstructionFinder(il);
	String            pat   = "NOP+"; // Find at least one NOP
	InstructionHandle next  = null;
	int               count = 0;

	for (Iterator iter = f.search(pat); iter.hasNext(); ) {
		InstructionHandle[] match = (InstructionHandle[])iter.next();
		InstructionHandle   first = match[0];
		InstructionHandle   last  = match[match.length - 2]; // match includes 1 nonmatched instr

		/* cant remove NOP if its the last instruction,
		 * we won't know where to redirect gotos etc
		 */
		if((next = last.getNext()) == null)
			break;

		count += match.length-1;

		/* Delete NOPs and redirect any references to them to the following
		 * (non-nop) instruction.
		 */
		try {
			il.delete(first, last);
		} catch(TargetLostException e) {
			InstructionHandle[] targets = e.getTargets();
			for(int i=0; i < targets.length; i++) {
				InstructionTargeter[] targeters = targets[i].getTargeters();

				for(int j=0; j < targeters.length; j++)
					targeters[j].updateTarget(targets[i], next);
			}
		}
	}

	return count;
}

private static final int Optimise(SAList sal) {
	// expand all expressions
	// A = C + 2;
	// B = A + 3;  -> B = (C + 2) + 3;
	// D = C + 5;


	PRINTLN("\n\n------Optimise----\n");

	SAList known = new SAList();

	FOREACH(SimpleAssign, sa, sal) {

		PRINTF("Before: " + sa.asString()+"\n");
		sa.expandVars(known);
		PRINTF("After : " + sa.asString()+"\n");


		boolean b = true;
		for (int kn = 0; b && kn < known.size(); ++kn ) {
			SimpleAssign k = (SimpleAssign)known.elementAt(kn);
			//PRINTF(sa.target.asString() + " ?= " + k.target.asString() + "\n");
			if (sa.target.equals(k.target)) {
				known.setElementAt(sa, kn);
				b = false;
			}
		}
		if (b)
			known.add(sa);
		PRINTLN("-");
		//for (int kn = 0; kn < known.size(); ++kn ) {
		//	SimpleAssign k = (SimpleAssign)known.elementAt(kn);
		//	PRINTF(k.asString() + "\n");
		//}

	}

		for (int kn = 0; kn < sal.size(); ++kn ) {
			SimpleAssign k = (SimpleAssign)sal.elementAt(kn);
			PRINTF(k.asString() + "\n");
		}
		PRINTLN("-");
		for (int kn = 0; kn < known.size(); ++kn ) {
			SimpleAssign k = (SimpleAssign)known.elementAt(kn);
			PRINTF(k.asString() + "\n");
		}

// A = 3
// B = A
// A = 5
// C = A
// A = 7
// 
// B = 3
// C = 5
// A = 7

// permutation finding!
// -loop over all 'simple' permutations (a+b)+c => c+(a+b)
//  -loop over all 'complex' permutations (a+b)+c => a+(b+c)
//   - loop over all 'sub' permutations (a+b)+c => (b+a)+c

	// constant folding etc etc
	// B = (C + 2) + 3; -> B = C + 5;

	// common subexpression elimination
	// B = C + 5;
	// D = B

	return 0;
}
