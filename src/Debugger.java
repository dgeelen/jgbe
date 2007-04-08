import javax.swing.*;
import javax.swing.JTable.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.net.*;
import java.awt.image.BufferedImage;
import javax.swing.table.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class Debugger implements ActionListener, ItemListener, KeyListener { //GUI
	public static boolean RIGHT_TO_LEFT = false;
	private static final int aaarg=650;
	public JTable regs1;
	public JTable regs2;
	public JTable mem;
	public JTable instrs;
	public JTextField cmds;
	private Disassembler deasm;
	private int memaddr=0;
	private Color UpdateColor = new Color(255,200,200);
	private TheRunner runner;
	private Thread runthread;
	swinggui gui;
	private Font MonoFont=new Font("Bitstream Vera Sans Mono",0, 12);
	private RDParser parser;
	private int[] oldRegVal;
	public Debugger(swinggui gui) {
		this.gui=gui;
		deasm= new Disassembler(gui.cpu);
		oldRegVal=new int[10];
//		runthread.suspend();
		createAndShowGUI();
		runner = new TheRunner(this);
		runthread = new Thread(runner);
		runthread.start();
		while (runner.getStatus() != 1) {};
		parser=new RDParser();
	}

	public class TheRunner implements Runnable {
		private int status;
		private Debugger dbg;
		private int stopaddr;
		synchronized public int getStatus() {
			return status;
		}
		synchronized public void setStatus(int val) {
			status = val;
		}

		public void setBreakPoint(int addr) {
			if (getStatus()==1)
				stopaddr = addr;
		}

		public TheRunner(Debugger tdbg) { //Pass something
	 		setStatus(0);
			dbg = tdbg;
	 	}

		public void run() {
			while (true) {
				setStatus(1);
				dbg.update();
				while (getStatus() == 1) {
					try {
						Thread.sleep(100);
					} catch (java.lang.InterruptedException e) {
					}
				}
				setStatus(3);
				while (getStatus() == 3) {
					dbg.gui.cpu.nextinstruction();
					if (dbg.gui.cpu.PC == stopaddr) {
						setStatus(0);
					}
					if (dbg.gui.cpu.exception() != 0) {
						setStatus(0);
					}
				}
			}
		}
	}

	public class MyCellRenderer extends DefaultTableCellRenderer { //for coloring cells
		private Color C;
		private int Row=-1;
		public MyCellRenderer(Color c) {
			super();
			this.C=c;
		}
		public MyCellRenderer(Color c, int r) {
			super();
			this.C=c;
			Row=r;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setText(String.valueOf(value));
			if((this.Row==-1)||(this.Row==row)) setBackground(C);
			else setBackground(Color.WHITE);
			return this;
		}
	}

	public void addComponentsToPane( Container contentPane ) {
		// Use BorderLayout. Default empty constructor with no horizontal and vertical
		// gaps
		contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
		if ( RIGHT_TO_LEFT ) {
			contentPane.setComponentOrientation(
				java.awt.ComponentOrientation.RIGHT_TO_LEFT );
		}
		JScrollPane scroll = new JScrollPane(new JLabel("Registers:"));
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 19));
		contentPane.add( scroll, BorderLayout.LINE_END );
		regs1 = new JTable(1,8);
		regs1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		regs1.setTableHeader(null);
		regs1.setFont(MonoFont);
		scroll = new JScrollPane(regs1);
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 19));
		contentPane.add( scroll, BorderLayout.NORTH );

		regs2 = new JTable(1,4);
		regs2.setCellSelectionEnabled(false);
		regs2.setColumnSelectionAllowed(false);
		regs2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		regs2.setTableHeader(null);
		regs2.setFont(MonoFont);
		scroll = new JScrollPane(regs2);
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 19));
		contentPane.add( scroll, BorderLayout.NORTH );

		scroll = new JScrollPane(new JLabel("Memory:"));
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 19));
		contentPane.add( scroll, BorderLayout.LINE_END );
		mem = new JTable(8,8+2);
		mem.setTableHeader(null);
		mem.setFont(MonoFont);
		scroll = new JScrollPane(mem);
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 131));
		contentPane.add( scroll, BorderLayout.LINE_END );

		scroll = new JScrollPane(new JLabel("Instructions:"));
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 19));
		contentPane.add( scroll, BorderLayout.LINE_END );
		instrs = new JTable(16,1);
		instrs.setTableHeader(null);
		TableColumnModel m = instrs.getColumnModel();
		TableColumn c = m.getColumn(0);
		MyCellRenderer r = new MyCellRenderer(new Color(222,222,255), 7);
		r.setFont(MonoFont);
		c.setCellRenderer(r);

		scroll = new JScrollPane(instrs);
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 259));
		contentPane.add( scroll, BorderLayout.LINE_END );
		scroll = new JScrollPane(new JLabel("Commands:"));
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 19));
		contentPane.add( scroll, BorderLayout.LINE_END );
		cmds = new JTextField();
		cmds.addActionListener(this);
		cmds.setFont(MonoFont);
		scroll = new JScrollPane(cmds);
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 20));
		contentPane.add( scroll, BorderLayout.LINE_END );
	}

	synchronized public void update() {
		if (runner.getStatus() == 1) {
			updateRegisters();
			updateMemory();
			updateInstructions();
		}
	}

	public void updateMemory() {
		int m=memaddr;
		for(int i=0; i<8; ++i) {
			mem.setValueAt(String.format("$%04x",m), i,0);
			for(int j=2; j<10; ++j) {
				mem.setValueAt(String.format("$%02x",gui.cpu.read(m++)), i,j);
			}
		}
	}

	private int seekBackOneInstruction(int pc) {
		int j=Math.max(pc-3,0);
		int i=deasm.instructionLength(j);
		if(i==3) return j;
		j=Math.max(pc-2,0);
		i=deasm.instructionLength(j);
		if(i==2) return j;
		j=Math.max(pc-1,0);
		i=deasm.instructionLength(j);
		if(i==1) return j;
		return j;
	}

	private void updateInstructions() {
		int pc=gui.cpu.PC;
		for(int i=0; i<7; ++i) {
			pc=seekBackOneInstruction(pc);
			}
		for(int i=0; i<16; ++i) {
			if (i==7) pc=gui.cpu.PC;
			instrs.setValueAt(deasm.simple_disasm(pc), i,0);
			pc+=deasm.instructionLength(pc);
		}

	}

	private void updateRegisters() { //TODO: Dit moet makkelijker kunnen ...
		TableColumnModel m = regs1.getColumnModel();
		TableColumn c;
		DefaultTableCellRenderer normal=new DefaultTableCellRenderer();
		MyCellRenderer colored = new MyCellRenderer(UpdateColor);

		regs1.setValueAt(String.format("A=$%02x",gui.cpu.regs[gui.cpu.A]), 0,0);
		c = m.getColumn(0);
		c.setCellRenderer( oldRegVal[0]==gui.cpu.regs[gui.cpu.A] ? normal : colored );
		oldRegVal[0]=gui.cpu.regs[gui.cpu.A];

		regs1.setValueAt(String.format("B=$%02x",gui.cpu.regs[gui.cpu.B]), 0,1);
		c = m.getColumn(1);
		c.setCellRenderer( oldRegVal[1]==gui.cpu.regs[gui.cpu.B] ? new DefaultTableCellRenderer() : new MyCellRenderer(UpdateColor) );
		oldRegVal[1]=gui.cpu.regs[gui.cpu.B];

		regs1.setValueAt(String.format("C=$%02x",gui.cpu.regs[gui.cpu.C]), 0,2);
		c = m.getColumn(2);
		c.setCellRenderer( oldRegVal[2]==gui.cpu.regs[gui.cpu.C] ? normal : colored );
		oldRegVal[2]=gui.cpu.regs[gui.cpu.C];

		regs1.setValueAt(String.format("D=$%02x",gui.cpu.regs[gui.cpu.D]), 0,3);
		c = m.getColumn(3);
		c.setCellRenderer( oldRegVal[3]==gui.cpu.regs[gui.cpu.D] ? normal : colored );
		oldRegVal[3]=gui.cpu.regs[gui.cpu.D];

		regs1.setValueAt(String.format("E=$%02x",gui.cpu.regs[gui.cpu.E]), 0,4);
		c = m.getColumn(4);
		c.setCellRenderer( oldRegVal[4]==gui.cpu.regs[gui.cpu.E] ? normal : colored );
		oldRegVal[4]=gui.cpu.regs[gui.cpu.E];

		regs1.setValueAt(String.format("F=$%02x",gui.cpu.regs[gui.cpu.F]), 0,5);
		c = m.getColumn(5);
		c.setCellRenderer( oldRegVal[5]==gui.cpu.regs[gui.cpu.F] ? normal : colored );
		oldRegVal[5]=gui.cpu.regs[gui.cpu.F];

		regs1.setValueAt(String.format("H=$%02x",gui.cpu.regs[gui.cpu.H]), 0,6);
		c = m.getColumn(6);
		c.setCellRenderer( oldRegVal[6]==gui.cpu.regs[gui.cpu.H] ? normal : colored );
		oldRegVal[6]=gui.cpu.regs[gui.cpu.H];

		regs1.setValueAt(String.format("L=$%02x",gui.cpu.regs[gui.cpu.L]), 0,7);
		c = m.getColumn(7);
		c.setCellRenderer( oldRegVal[7]==gui.cpu.regs[gui.cpu.L] ? normal : colored );
		oldRegVal[7]=gui.cpu.regs[gui.cpu.L];

		m = regs2.getColumnModel();
		regs2.setValueAt(String.format("PC=$%04x",gui.cpu.PC), 0,0);
		oldRegVal[8]=gui.cpu.PC;

		regs2.setValueAt(String.format("SP=$%04x",gui.cpu.SP), 0,1);
		c = m.getColumn(1);
		c.setCellRenderer( oldRegVal[9]==gui.cpu.SP ? normal : colored);
		oldRegVal[9]=gui.cpu.SP;

		String flags = "F=";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.ZF_Mask ) == gui.cpu.ZF_Mask )?"Z ":"z ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.NF_Mask ) == gui.cpu.NF_Mask )?"N ":"n ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.HC_Mask ) == gui.cpu.HC_Mask )?"H ":"h ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.CF_Mask ) == gui.cpu.CF_Mask )?"C ":"c ";
		regs2.setValueAt(flags, 0,3);
		c = m.getColumn(3);
		c.setCellRenderer( oldRegVal[5]==gui.cpu.regs[gui.cpu.F] ? new DefaultTableCellRenderer(): new MyCellRenderer(UpdateColor));
	}

	private void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated( true );
		JFrame frame = new JFrame( "JGameBoy Emulator DEBUGGER" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		//Set up the content pane and add swing components to it
		addComponentsToPane( frame.getContentPane() );
		frame.pack();
		frame.setSize(new Dimension(480,640));
		frame.setVisible( true );
		//frame.addKeyListener(this); TODO: Shortcut keys
	}

	public void actionPerformed( ActionEvent e ) {
		JTextField f = ( JTextField )( e.getSource() );
		if(f==cmds) {
			int i=0;
			String s=cmds.getText().trim();
			cmds.selectAll();
			System.out.println("Command='"+s+"'");
			if(s.equals("s")) {
				System.out.println("SingleStep");
				gui.cpu.nextinstruction();
				update();
			}
			if(s.equals("reset")) {
				gui.cpu.reset();
				update();
			}
			if(s.equals("so")) {
				if (runner.getStatus() == 1) {
					runner.setBreakPoint(gui.cpu.PC + deasm.instructionLength(gui.cpu.PC));
					runner.setStatus(2);
					while (runner.getStatus() == 2) {};
				}
			}
			if(s.charAt(0)=='g') {
				if (runner.getStatus() == 1) {
					runner.setStatus(2);
					while (runner.getStatus() != 3) {};
				}
			}
			if(s.charAt(0)=='b') {
				if (runner.getStatus() == 3) {
					runner.setStatus(0);
					while (runner.getStatus() != 1) {};
					update();
				}
			}
			if(s.charAt(0)=='r') {
				try {
					if (runner.getStatus() == 1) {
						String ss = s.substring( s.lastIndexOf(" ") + 1);
						if( ss.charAt(0)=='$' ) {
							runner.setBreakPoint(Integer.parseInt( ss.substring(1), 16 ));
							runner.setStatus(2);
							while (runner.getStatus() == 2) {};
						}
					}
				}
				catch ( NumberFormatException ee ) {
						System.out.println( ee.getMessage() + " is not a valid format for an integer." );
				}
				//memaddr = Integer.valueOf(s.substring( s.lastIndexOf(" "))).intValue();
				update();
			}
			i=s.indexOf("=");
			if(i>-1) { //assignment
				String l = s.substring(0,i).trim();
				updateRegisters(); //sets the variables
				parser.removeVariables();
				parser.addVariable("A", oldRegVal[0]);
				parser.addVariable("B", oldRegVal[1]);
				parser.addVariable("C", oldRegVal[2]);
				parser.addVariable("D", oldRegVal[3]);
				parser.addVariable("E", oldRegVal[4]);
				parser.addVariable("F", oldRegVal[5]);
				parser.addVariable("H", oldRegVal[6]);
				parser.addVariable("L", oldRegVal[7]);
				parser.addVariable("SP", oldRegVal[8]);
				parser.addVariable("PC", oldRegVal[9]);
				parser.addVariable("HL", oldRegVal[7]|(oldRegVal[6]<<8));
				int v = parser.Evaluate(s.substring(i+1).trim());
				if(!parser.parseError) {
					if(l.equals("A")){
						gui.cpu.regs[gui.cpu.A]=v&0xFF;
						update();
					}
					if(l.equals("B")){
						gui.cpu.regs[gui.cpu.B]=v&0xFF;
						update();
					}
					if(l.equals("C")){
						gui.cpu.regs[gui.cpu.C]=v&0xFF;
						update();
					}
					if(l.equals("D")){
						gui.cpu.regs[gui.cpu.D]=v&0xFF;
						update();
					}
					if(l.equals("E")){
						gui.cpu.regs[gui.cpu.E]=v&0xFF;
						update();
					}
					if(l.equals("F")){
						gui.cpu.regs[gui.cpu.F]=v&0xFF;
						update();
					}
					if(l.equals("H")){
						gui.cpu.regs[gui.cpu.H]=v&0xFF;
						update();
					}
					if(l.equals("L")){
						gui.cpu.regs[gui.cpu.L]=v&0xFF;
						update();
					}
					if(l.equals("HL")){
						gui.cpu.regs[gui.cpu.H]=(v>>8)&0xFF;
						gui.cpu.regs[gui.cpu.L]=v&0xFF;
						update();
					}
					if(l.equals("PC")){
						gui.cpu.PC=v&0xFFFF;
						update();
					}
					if(l.equals("SP")){
						gui.cpu.SP=v&0xFFFF;
						update();
					}
					else {
						System.out.println("Assignment to '"+l+"' with v="+v);
					}
				}
			}
			if(s.charAt(0)=='m') {
				memaddr = parser.StrToInt(s.substring(1));
				update();
			}
		}
		else {
			System.out.println( "Action event i an instance of " + getClassName( f ));
		}
	}

	public void itemStateChanged( ItemEvent e ) {
		JMenuItem source = ( JMenuItem )( e.getSource() );
		System.out.println("Menu Item source: " + source.getText() + " (an instance of " + getClassName( source ) + ")"	+ "\n"
		                   + "    State of check Box: " + (( e.getStateChange() == ItemEvent.SELECTED ) ? "selected":"unselected" ));
	}

	public void keyTyped(KeyEvent e) {
		System.out.println("keyTyped");
	}

	public void keyPressed(KeyEvent e) {
		System.out.println("DEBUGGER: keyPressed");
	}

	public void keyReleased(KeyEvent e) {
		System.out.println("DEBUGGER: keyReleased" + e.getKeyCode());
	}

	// Returns the class name, no package info
	protected static String getClassName( Object o ) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf( "." );
		return classString.substring( dotIndex+1 );	//Returns only Class name
	}
}