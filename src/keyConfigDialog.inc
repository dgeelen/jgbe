#ifndef USE_JAVA_MOBILE
public class keyConfigDialog implements ComponentListener, ActionListener, KeyListener {
	private JDialog dialog;
	private JFrame owner;
	private Font fnt=new Font("Bitstream Vera Sans Mono",0, 24);

	VECTOR(JTextField) editboxes = new VECTOR(JTextField)();
	JTextField tbox;
	JLabel tlabel;
	#define INPUTBOX(c, x) tbox = new JTextField();\
	                    tlabel = new JLabel(x);\
	                    editboxes.add(tbox);\
	                    c.add(tlabel);\
	                    c.add(tbox);\
	                    tbox.addKeyListener(this);\
	                    tbox.addActionListener(this)

	int[] keyMap;
	public keyConfigDialog(JFrame o, int[] km) {
		owner = o;
		keyMap= km;
		JScrollPane scroll;
		dialog= new JDialog(owner, "Key Bindings", true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					dialog.setVisible(false);
			}
		});

		JTabbedPane tabPane = new JTabbedPane();
		JPanel player1Keys = new JPanel();
			GridLayout player1KeysGL = new GridLayout(8,2);
			player1Keys.setLayout(player1KeysGL);
			INPUTBOX(player1Keys, "Up");
			INPUTBOX(player1Keys, "Down");
			INPUTBOX(player1Keys, "Left");
			INPUTBOX(player1Keys, "Right");
			INPUTBOX(player1Keys, "A");
			INPUTBOX(player1Keys, "B");
			INPUTBOX(player1Keys, "Start");
			INPUTBOX(player1Keys, "Select");
		JPanel player2Keys = new JPanel();
			GridLayout player2KeysGL = new GridLayout(8,2);
			player2Keys.setLayout(player2KeysGL);
			INPUTBOX(player2Keys, "Up");
			INPUTBOX(player2Keys, "Down");
			INPUTBOX(player2Keys, "Left");
			INPUTBOX(player2Keys, "Right");
			INPUTBOX(player2Keys, "A");
			INPUTBOX(player2Keys, "B");
			INPUTBOX(player2Keys, "Start");
			INPUTBOX(player2Keys, "Select");
		JPanel shortCuts = new JPanel();
		shortCuts.add(new JLabel("- TODO -"));
		tabPane.add("Player 1", player1Keys);
		tabPane.add("Player 2", player2Keys);
		tabPane.add("Shortcuts", shortCuts);
		scroll = new JScrollPane(tabPane);
		scroll.setPreferredSize(new Dimension(480,320));
		dialog.add(scroll);

		//Fill all TextFields with text
		FOREACH(JTextField, tf, editboxes) {
			int keyCode = keyMap[foreach]&0xff;
			int keyModifiers = keyMap[foreach]>>8;
			tf.setText(KeyEvent.getKeyText(keyCode) + (keyModifiers>0 ? "+"+KeyEvent.getKeyModifiersText(keyModifiers) : ""));
		}


		dialog.addComponentListener(this);
		dialog.addKeyListener(this);
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(true);
		dialog.pack();
		Dimension d = owner.getSize();
		Point     p = new Point();
		p.setLocation((owner.getLocation().getX()+(d.getWidth()/2)) - (dialog.getWidth()/2), (owner.getLocation().getY()+d.getHeight()/2)-(dialog.getHeight()/2));
		dialog.setLocation(p);

		dialog.addWindowListener( new WindowAdapter() {
			public void windowOpened( WindowEvent e ){
				((JTextField)editboxes.firstElement()).requestFocus();
			}
		});
	}

	public int[] getKeyCodes() {
		dialog.setVisible( true );
		return null;
	}

	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {}

	public void actionPerformed( ActionEvent e ) {}

	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		PRINTLN("KeyTyped: "+e.getKeyCode()+" Modifiers:"+e.getModifiers());
		int keyCode = e.getKeyCode();
		int keyModifiers = e.getModifiers();
		int keyState = keyCode | (keyModifiers << 8);
		if((keyCode != KeyEvent.VK_ALT) && (keyCode != KeyEvent.VK_CONTROL) && (keyCode != KeyEvent.VK_SHIFT)) {
			FOREACH(JTextField, tf, editboxes) {
				if(tf.hasFocus()) {
					tf.setText(KeyEvent.getKeyText(keyCode) + (keyModifiers>0 ? "+"+KeyEvent.getKeyModifiersText(keyModifiers) : ""));
					((JTextField)editboxes.elementAt(Math.min(foreach+1, editboxes.size()))).requestFocus();
					keyMap[foreach] = keyState;
				}
			}
		}
		else {
			PRINTLN("Key is one of CTRL, ALT or SHIFT");
		}
	}
	public void keyPressed(KeyEvent e) {}
}
#endif

