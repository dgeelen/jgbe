import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

public class ColorSelector extends JFrame
  implements MouseListener, ChangeListener, ActionListener {

	public static final long serialVersionUID = 1;

	private JPanel pnlColor  = new JPanel();
	private JPanel pnlSelect = new JPanel();
	private JPanel pnlTest   = new JPanel();

	private String[] optionStr = {"foreground ", "background 1.", "background 2."};
	private ButtonGroup group = new ButtonGroup();
	private JRadioButton[] buttons = new JRadioButton[12];

	private String[] colorStr = {"Red", "Green", "Blue"};
	private JSlider[] sliders = new JSlider[3];

	private int[][] foreground  = new int[4][3];
	private int[][] background1 = new int[4][3];
	private int[][] background2 = new int[4][3];

	private VideoController vc;

	public ColorSelector (VideoController vc) {
		this.vc = vc;

		setLayout(new BorderLayout());
		setSize (450, 450);
		setResizable( false );
		addMouseListener (this);

		initSelectPanel();
		add(pnlSelect, BorderLayout.WEST);
		initColorPanel();
		add(pnlColor, BorderLayout.CENTER);
		add(pnlTest, BorderLayout.EAST);
	}

	private void initSelectPanel() {
		pnlSelect.setLayout(new GridLayout(12, 2));

		for (int i = 0; i < 12; ++i) {
			String lblText = optionStr[(int)(i / 4)] + (i % 4 + 1);
			pnlSelect.add(new Label(lblText));

			buttons[i] = new JRadioButton();
			buttons[i].addActionListener( this );
			group.add(buttons[i]);
			pnlSelect.add(buttons[i]);
		}

		buttons[0].setSelected(true);
	}

	private void initColorPanel() {
		pnlColor.setLayout(new GridLayout(3, 3));

		for (int i = 0; i < 3; ++i) {
			sliders[i] = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
			sliders[i].addChangeListener(this);
			pnlColor.add(new JLabel(colorStr[i]));
			pnlColor.add(sliders[i]);
		}
	}

	private void setTestColor(int r, int g, int b) {
		pnlTest.setBackground(new Color(r, g, b));
	}

	public void mouseClicked (MouseEvent e) {
		System.out.printf("(%d, %d)\n", e.getX(), e.getY());
	}

	public void stateChanged(ChangeEvent e) {
		int r = sliders[0].getValue();
		int g = sliders[1].getValue();
		int b = sliders[2].getValue();

		if ( buttons[0].isSelected() ) {
			foreground[0][0] = r;
			foreground[0][1] = g;
			foreground[0][2] = b;
		}
		else if ( buttons[1].isSelected() ) {
			foreground[1][0] = r;
			foreground[1][1] = g;
			foreground[1][2] = b;
		}
		else if ( buttons[2].isSelected() ) {
			foreground[2][0] = r;
			foreground[2][1] = g;
			foreground[2][2] = b;
		}
		else if ( buttons[3].isSelected() )  {
			foreground[3][0] = r;
			foreground[3][1] = g;
			foreground[3][2] = b;
		}
		else if ( buttons[4].isSelected() ) {
			background1[0][0] = r;
			background1[0][1] = g;
			background1[0][2] = b;
		}
		else if ( buttons[5].isSelected() ) {
			background1[1][0] = r;
			background1[1][1] = g;
			background1[1][2] = b;
		}
		else if ( buttons[6].isSelected() ) {
			background1[2][0] = r;
			background1[2][1] = g;
			background1[2][2] = b;
		}
		else if ( buttons[7].isSelected() ) {
			background1[3][0] = r;
			background1[3][1] = g;
			background1[3][2] = b;
		}
		else if ( buttons[8].isSelected() ) {
			background2[0][0] = r;
			background2[0][1] = g;
			background2[0][2] = b;
		}
		else if ( buttons[9].isSelected() ) {
			background2[1][0] = r;
			background2[1][1] = g;
			background2[1][2] = b;
		}
		else if ( buttons[10].isSelected() ) {
			background2[2][0] = r;
			background2[2][1] = g;
			background2[2][2] = b;
		}
		else if ( buttons[11].isSelected() ) {
			background2[3][0] = r;
			background2[3][1] = g;
			background2[3][2] = b;
		}

		vc.setGrayShades( foreground, background1, background2 );

		setTestColor(r, g, b);
	}

	public void actionPerformed(ActionEvent e) {
		if ( e.getSource().equals( buttons[0] ) ) {
			setTestColor( foreground[0][0], foreground[0][1], foreground[0][2] );
			sliders[0].setValue(foreground[0][0]);
			sliders[1].setValue(foreground[0][1]);
			sliders[2].setValue(foreground[0][2]);
		}
		else if ( e.getSource().equals( buttons[1] ) ) {
			setTestColor( foreground[1][0], foreground[1][1], foreground[1][2] );
			sliders[0].setValue(foreground[1][0]);
			sliders[1].setValue(foreground[1][1]);
			sliders[2].setValue(foreground[1][2]);
		}
		else if ( e.getSource().equals( buttons[2] ) ) {
			setTestColor( foreground[2][0], foreground[2][1], foreground[2][2] );
			sliders[0].setValue(foreground[2][0]);
			sliders[1].setValue(foreground[2][1]);
			sliders[2].setValue(foreground[2][2]);
		}
		else if ( e.getSource().equals( buttons[3] ) ) {
			setTestColor( foreground[3][0], foreground[3][1], foreground[3][2] );
			sliders[0].setValue(foreground[3][0]);
			sliders[1].setValue(foreground[3][1]);
			sliders[2].setValue(foreground[3][2]);
		}
		else if ( e.getSource().equals( buttons[4] ) ) {
			setTestColor( background1[0][0], background1[0][1], background1[0][2] );
		}
		else if ( e.getSource().equals( buttons[5] ) ) {
			setTestColor( background1[1][0], background1[1][1], background1[1][2] );
		}
		else if ( e.getSource().equals( buttons[6] ) ) {
			setTestColor( background1[2][0], background1[2][1], background1[2][2] );
		}
		else if ( e.getSource().equals( buttons[7] ) ) {
			setTestColor( background1[3][0], background1[3][1], background1[3][2] );
		}
		else if ( e.getSource().equals( buttons[8] ) ) {
			setTestColor( background2[0][0], background2[0][1], background2[0][2] );
		}
		else if ( e.getSource().equals( buttons[9] ) ) {
			setTestColor( background2[1][0], background2[1][1], background2[1][2] );
		}
		else if ( e.getSource().equals( buttons[10] ) ) {
			setTestColor( background2[2][0], background2[2][1], background2[2][2] );
		}
		else if ( e.getSource().equals( buttons[11] ) ) {
			setTestColor( background2[3][0], background2[3][1], background2[3][2] );
		}
	}

	public void setDefault(int[][] fore, int[][] back1, int[][] back2) {
		foreground  = fore;
		background1 = back1;
		background2 = back2;

		if ( buttons[0].isSelected() ) {
			sliders[0].setValue(fore[0][0]);
			sliders[1].setValue(fore[0][1]);
			sliders[2].setValue(fore[0][2]);
		}
		else if ( buttons[1].isSelected() ) {
			sliders[0].setValue(fore[1][0]);
			sliders[1].setValue(fore[1][1]);
			sliders[2].setValue(fore[1][2]);
		}
		else if ( buttons[2].isSelected() ) {
			sliders[0].setValue(fore[2][0]);
			sliders[1].setValue(fore[2][1]);
			sliders[2].setValue(fore[2][2]);
		}
		else if ( buttons[3].isSelected() )  {
		}
		else if ( buttons[4].isSelected() ) {
			sliders[0].setValue(back1[0][0]);
			sliders[1].setValue(back1[0][1]);
			sliders[2].setValue(back1[0][2]);
		}
		else if ( buttons[5].isSelected() ) {
			sliders[0].setValue(back1[1][0]);
			sliders[1].setValue(back1[1][1]);
			sliders[2].setValue(back1[1][2]);
		}
		else if ( buttons[6].isSelected() ) {
			sliders[0].setValue(back1[2][0]);
			sliders[1].setValue(back1[2][1]);
			sliders[2].setValue(back1[2][2]);
		}
		else if ( buttons[7].isSelected() ) {
			sliders[0].setValue(back1[3][0]);
			sliders[1].setValue(back1[3][1]);
			sliders[2].setValue(back1[3][2]);
		}
		else if ( buttons[8].isSelected() ) {
			sliders[0].setValue(back2[0][0]);
			sliders[1].setValue(back2[0][1]);
			sliders[2].setValue(back2[0][2]);
		}
		else if ( buttons[9].isSelected() ) {
			sliders[0].setValue(back2[1][0]);
			sliders[1].setValue(back2[1][1]);
			sliders[2].setValue(back2[1][2]);
		}
		else if ( buttons[10].isSelected() ) {
			sliders[0].setValue(back2[2][0]);
			sliders[1].setValue(back2[2][1]);
			sliders[2].setValue(back2[2][2]);
		}
		else if ( buttons[11].isSelected() ) {
			sliders[0].setValue(back2[3][0]);
			sliders[1].setValue(back2[3][1]);
			sliders[2].setValue(back2[3][2]);
		}
	}

	public void mousePressed (MouseEvent e) {}
	public void mouseReleased (MouseEvent e) {}
	public void mouseExited (MouseEvent e) {}
	public void mouseEntered (MouseEvent e) {}
}