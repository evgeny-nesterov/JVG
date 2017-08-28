package satis.iface.graph.axis;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.VerticalLabelUI;

import satis.iface.graph.grid.Grid;

public class AxisTitle extends JLabel {
	private static final long serialVersionUID = 1L;

	public AxisTitle(String title, int type) {
		setText(title);
		setOpaque(false);
		setBackground(Color.white);
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);

		if (type == Grid.Y_AXIS) {
			setUI(new VerticalLabelUI(false));
		} else {
		}

		enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
	}

	public static void main(String[] arg) {
		JFrame f = new JFrame();

		AxisTitle xTitle = new AxisTitle("<HTML><BODY><CENTER>Axis<BR><FONT color=ff0000 size=3>ordinate</FONT></CENTER></BODY></HTML>", Grid.Y_AXIS);
		xTitle.setOpaque(true);
		xTitle.setBackground(Color.white);

		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(xTitle, BorderLayout.WEST);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocation(200, 200);
		f.setMinimumSize(new Dimension(0, 0));
		f.getContentPane().setMinimumSize(new Dimension(0, 0));
		f.pack();
		f.setVisible(true);
	}
}
