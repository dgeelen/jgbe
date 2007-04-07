import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.net.*;
import java.awt.image.BufferedImage;

public class Debugger implements ActionListener, ItemListener, KeyListener { //GUI
	public static boolean RIGHT_TO_LEFT = false;
	public JTable regs1;
	public JTable regs2;
	public JTable mem;
	public JTable instrs;
	public JTextField cmds;
	private Disassembler deasm;
	swinggui gui;
	public Debugger(swinggui gui) {
		this.gui=gui;
		deasm= new Disassembler(gui.cartridge, gui.cpu);
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
		JScrollPane scroll;
		contentPane.add( new JLabel("- Registers -"), BorderLayout.LINE_END );
		regs1 = new JTable(1,8);
		regs1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		regs1.setTableHeader(null);
		scroll = new JScrollPane(regs1);
		scroll.setMaximumSize(new Dimension(640, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(640, 19));
		contentPane.add( scroll, BorderLayout.NORTH );

		regs2 = new JTable(1,4);
		regs2.setCellSelectionEnabled(false);
		regs2.setColumnSelectionAllowed(false);
		regs2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		regs2.setTableHeader(null);
		scroll = new JScrollPane(regs2);
		scroll.setMaximumSize(new Dimension(640, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(640, 19));
		contentPane.add( scroll, BorderLayout.NORTH );


		contentPane.add( new JLabel("- Memory -"), BorderLayout.LINE_END );
		mem = new JTable(8,16+2);
		mem.setTableHeader(null);
		scroll = new JScrollPane(mem);
		scroll.setMaximumSize(new Dimension(640, Integer.MAX_VALUE));
		contentPane.add( scroll, BorderLayout.LINE_END );

		contentPane.add( new JLabel("- Instructions -"), BorderLayout.LINE_END );
		instrs = new JTable(16,1);
		scroll = new JScrollPane(instrs);
		scroll.setMaximumSize(new Dimension(640, Integer.MAX_VALUE));
		contentPane.add( scroll, BorderLayout.LINE_END );
		contentPane.add( new JLabel("- Commands -"), BorderLayout.LINE_END );
		cmds = new JTextField();
		contentPane.add( cmds, BorderLayout.LINE_END );
	}

	public void update() {
		updateRegisters();
		updateInstructions();
	}
	private void updateInstructions() {
		int pc=gui.cpu.PC;
		for(int i=0; i<16; ++i) {
			instrs.setValueAt(deasm.disassemble(pc), i,0);
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
		flags += (( gui.cpu.regs[gui.cpu.F] & ( 1 <<3 ) ) == ( 1 <<3 ) )?"1 ":"0 ";
		flags += (( gui.cpu.regs[gui.cpu.F] & ( 1 <<2 ) ) == ( 1 <<2 ) )?"1 ":"0 ";
		flags += (( gui.cpu.regs[gui.cpu.F] & ( 1 <<1 ) ) == ( 1 <<1 ) )?"1 ":"0 ";
		flags += (( gui.cpu.regs[gui.cpu.F] & ( 1 <<0 ) ) == ( 1 <<0 ) )?"1 ":"0 ";
		regs2.setValueAt(flags, 0,3);
	}

	private void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated( true );
		JFrame frame = new JFrame( "JGameBoy Emulator DEBUGGER" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		//Set up the content pane and add swing components to it
		addComponentsToPane( frame.getContentPane() );
		frame.pack();
		frame.setVisible( true );
	}

	public void actionPerformed( ActionEvent e ) {
		JMenuItem source = ( JMenuItem )( e.getSource() );
		System.out.println( "Menu Item source: " + source.getText() + " (an instance of " + getClassName( source ) + ")" );
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