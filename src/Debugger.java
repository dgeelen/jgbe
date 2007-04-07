import javax.swing.*;
import javax.swing.JTable.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.net.*;
import java.awt.image.BufferedImage;

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
	swinggui gui;
	public Debugger(swinggui gui) {
		this.gui=gui;
		deasm= new Disassembler(gui.cpu);
		createAndShowGUI();
		update();
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
		scroll = new JScrollPane(regs1);
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 19));
		contentPane.add( scroll, BorderLayout.NORTH );

		regs2 = new JTable(1,4);
		regs2.setCellSelectionEnabled(false);
		regs2.setColumnSelectionAllowed(false);
		regs2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		regs2.setTableHeader(null);
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
		instrs.setFont(new Font("Bitstream Vera Sans Mono",0, 12));
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
		scroll = new JScrollPane(cmds);
		scroll.setMaximumSize(new Dimension(aaarg, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(aaarg, 20));
		contentPane.add( scroll, BorderLayout.LINE_END );
	}

	public void update() {
		updateRegisters();
		updateMemory();
		updateInstructions();
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

	private void updateRegisters() {
		regs1.setValueAt(String.format("A=$%02x",gui.cpu.regs[gui.cpu.A]), 0,0);
		regs1.setValueAt(String.format("B=$%02x",gui.cpu.regs[gui.cpu.B]), 0,1);
		regs1.setValueAt(String.format("C=$%02x",gui.cpu.regs[gui.cpu.C]), 0,2);
		regs1.setValueAt(String.format("D=$%02x",gui.cpu.regs[gui.cpu.D]), 0,3);
		regs1.setValueAt(String.format("E=$%02x",gui.cpu.regs[gui.cpu.E]), 0,4);
		regs1.setValueAt(String.format("F=$%02x",gui.cpu.regs[gui.cpu.F]), 0,5);
		regs1.setValueAt(String.format("H=$%02x",gui.cpu.regs[gui.cpu.H]), 0,6);
		regs1.setValueAt(String.format("L=$%02x",gui.cpu.regs[gui.cpu.L]), 0,7);
		regs2.setValueAt(String.format("PC=$%04x",gui.cpu.PC), 0,0);
		regs2.setValueAt(String.format("SP=$%04x",gui.cpu.SP), 0,1);
		//regs2.setValueAt(String.format("F=$%02x",gui.cpu.regs[gui.cpu.F]), 0,4);
		String flags = "F=";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.ZF_Mask ) == gui.cpu.ZF_Mask )?"Z ":"z ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.NF_Mask ) == gui.cpu.NF_Mask )?"N ":"n ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.HC_Mask ) == gui.cpu.HC_Mask )?"H ":"h ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.CF_Mask ) == gui.cpu.CF_Mask )?"C ":"c ";
		regs2.setValueAt(flags, 0,3);
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
			String s=cmds.getText().trim();
			cmds.selectAll();
			System.out.println("Command='"+s+"'");
			if(s.equals("s")) {
				System.out.println("SingleStep");
				gui.cpu.nextinstruction();
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