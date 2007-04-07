import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.net.*;
import java.awt.image.BufferedImage;

public class Debugger implements ActionListener, ItemListener, KeyListener { //GUI
	public static boolean RIGHT_TO_LEFT = false;
	public JTable regs;
	public JTable mem;
	public JTable instrs;
	public JTextField cmds;
	swinggui gui;
	public Debugger(swinggui gui) {
		this.gui=gui;
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
		contentPane.add( new JLabel("- Registers -"), BorderLayout.LINE_END );
		regs = new JTable(2,8);
		System.out.println("REGSIZE="+regs.getSize());
		System.out.println("REGh="+regs.getHeight());

		regs.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scroll = new JScrollPane(regs);
		scroll.setMaximumSize(new Dimension(555, Integer.MAX_VALUE));
		//scroll.setMaximumSize(new Dimension(555, Integer.MAX_VALUE));
		scroll.setPreferredSize(new Dimension(555, regs.getHeight()));
		System.out.println("REGSIZE="+regs.getSize());
		System.out.println("REGh="+regs.getHeight());
		contentPane.add( scroll, BorderLayout.NORTH );
		contentPane.add( new JLabel("- Memory -"), BorderLayout.LINE_END );
		mem = new JTable(8,16+2);
		contentPane.add( mem, BorderLayout.LINE_END );
		contentPane.add( new JLabel("- Instructions -"), BorderLayout.LINE_END );
		instrs = new JTable(16,5);
		contentPane.add( instrs, BorderLayout.LINE_END );
		contentPane.add( new JLabel("- Commands -"), BorderLayout.LINE_END );
		cmds = new JTextField();
		contentPane.add( cmds, BorderLayout.LINE_END );
	}

	public void update() {
		updateRegisters();
	}
	private void updateRegisters() {
		regs.setValueAt(String.format("A=$%02x",gui.cpu.regs[gui.cpu.A]), 0,0);
		regs.setValueAt(String.format("B=$%02x",gui.cpu.regs[gui.cpu.B]), 0,1);
		regs.setValueAt(String.format("C=$%02x",gui.cpu.regs[gui.cpu.C]), 0,2);
		regs.setValueAt(String.format("D=$%02x",gui.cpu.regs[gui.cpu.D]), 0,3);
		regs.setValueAt(String.format("E=$%02x",gui.cpu.regs[gui.cpu.E]), 0,4);
		regs.setValueAt(String.format("F=$%02x",gui.cpu.regs[gui.cpu.F]), 0,5);
		regs.setValueAt(String.format("H=$%02x",gui.cpu.regs[gui.cpu.H]), 0,6);
		regs.setValueAt(String.format("L=$%02x",gui.cpu.regs[gui.cpu.L]), 0,7);
		regs.setValueAt(String.format("PC=$%04x",gui.cpu.PC), 1,0);
		regs.setValueAt(String.format("SP=$%04x",gui.cpu.SP), 1,1);
		regs.setValueAt(String.format("F=$%02x",gui.cpu.regs[gui.cpu.F]), 1,6);
		String flags = "";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.ZF_Mask ) == gui.cpu.ZF_Mask )?"Z ":"z ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.NF_Mask ) == gui.cpu.NF_Mask )?"N ":"n ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.HC_Mask ) == gui.cpu.HC_Mask )?"H ":"h ";
		flags += (( gui.cpu.regs[gui.cpu.F] & gui.cpu.CF_Mask ) == gui.cpu.CF_Mask )?"C ":"c ";
		regs.setValueAt(flags, 1,7);
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