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

private static final int Optimise(VECTOR(SimpleAssign) sal) {
	// expand all expressions
	// A = C + 2;
	// B = A + 3;  -> B = (C + 2) + 3;
	// D = C + 5;

	// constant folding etc etc
	// B = (C + 2) + 3; -> B = C + 5;

	// common subexpression elimination
	// B = C + 5;
	// D = B

	return 0;
}
