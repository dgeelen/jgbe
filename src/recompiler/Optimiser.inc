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
