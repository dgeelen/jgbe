#ifndef USE_JAVA_MOBILE
public class keyConfigDialog implements ComponentListener, ActionListener, KeyListener {
	private JDialog dialog;
	private JFrame owner;
	private DrawingArea DA;
	private Font fnt=new Font("Bitstream Vera Sans Mono",0, 24);

	public class DrawingArea extends JPanel{
		private static final long serialVersionUID = 1L; // prevent warning
		public DrawingArea() { super(); }
		public String txt="";
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			FontMetrics fm=getFontMetrics(g.getFont());
			int textWidth=fm.stringWidth(txt);
			int textHeight=fm.getHeight();
			g.drawString(txt, (getWidth()/2)-(textWidth/2), (getHeight()/2)-(textHeight/2) );
		}
	}

	int[] keyCodes;
	int nextKey;
	public keyConfigDialog(JFrame o) {
		owner = o;
		JScrollPane scroll;
		dialog= new JDialog(owner, "Key Bindings", true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					dialog.setVisible(false);
			}
		});

		keyCodes=new int[8];
		nextKey=0;

		DA = new DrawingArea();
		DA.setFont(fnt);
		DA.txt = "Press the 'spacebar' to begin calibration";
// 		DA.addKeyListener(this);
		scroll  = new JScrollPane(DA);
		scroll.setPreferredSize(new Dimension(480, 240));
		dialog.add(scroll,BorderLayout.CENTER);

		dialog.addComponentListener(this);
		dialog.addKeyListener(this);
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(false);
		dialog.pack();
		Dimension d = owner.getSize();
		Point     p = new Point();
		p.setLocation((owner.getLocation().getX()+(d.getWidth()/2)) - (dialog.getWidth()/2), (owner.getLocation().getY()+d.getHeight()/2)-(dialog.getHeight()/2));
		dialog.setLocation(p);

	}

	public int[] getKeyCodes() {
			dialog.setVisible( true );

			return null;
	}

	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {}

	public void actionPerformed( ActionEvent e ) { }

	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		PRINTLN("KeyTyped: "+e.getKeyCode()+" nextKey="+nextKey);
		if(nextKey>0) {
			switch(nextKey) {
				case 1:
					keyCodes[nextKey-1]=e.getKeyCode();
					DA.txt = "Press 'down'";
					DA.repaint();
					break;
				case 2:
					keyCodes[nextKey-1]=e.getKeyCode();
					DA.txt = "Press 'left'";
					DA.repaint();
					break;
				case 3:
					keyCodes[nextKey-1]=e.getKeyCode();
					DA.txt = "Press 'right'";
					DA.repaint();
					break;
				case 4:
					keyCodes[nextKey-1]=e.getKeyCode();
					DA.txt = "Press 'A'";
					DA.repaint();
					break;
				case 5:
					keyCodes[nextKey-1]=e.getKeyCode();
					DA.txt = "Press 'B'";
					DA.repaint();
					break;
				case 6:
					keyCodes[nextKey-1]=e.getKeyCode();
					DA.txt = "Press 'Start'";
					DA.repaint();
					break;
				case 7:
					keyCodes[nextKey-1]=e.getKeyCode();
					DA.txt = "Press 'Select'";
					DA.repaint();
					break;
				case 8:
					keyCodes[nextKey-1]=e.getKeyCode();
					DA.txt = "All done :)";
					DA.repaint();
					break;
				default:
			}
			++nextKey;
		}
		else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			DA.txt = "Press 'up'";
			DA.repaint();
			++nextKey;
		}
	}
	public void keyPressed(KeyEvent e) {}
}
#endif

