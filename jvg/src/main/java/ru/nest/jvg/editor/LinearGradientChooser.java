package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.gradient.LinearGradient;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.LinearGradientResource;
import ru.nest.jvg.resource.Resource;

public class LinearGradientChooser extends GradientChooser<LinearGradient> {
	public LinearGradientChooser(JVGResources resources) {
		this(resources, null);
	}

	public LinearGradientChooser(JVGResources resources, Resource<LinearGradient> resource) {
		super(resources, resource);
	}

	@Override
	public void setResource(Resource<LinearGradient> resource) {
		super.setResource(resource);
		if (resource != null) {
			LinearGradient gradient = resource.getResource();
			txtX1.setText(format(100 * gradient.getX1()));
			txtY1.setText(format(100 * gradient.getY1()));
			txtX2.setText(format(100 * gradient.getX2()));
			txtY2.setText(format(100 * gradient.getY2()));
		}
	}

	@Override
	protected JPanel constractChooserPanel() {
		JPanel pnlContent = super.constractChooserPanel();

		JPanel pnlVector = constractVectorPanel();
		pnlContent.add(pnlVector, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

		return pnlContent;
	}

	private JTextField txtX1;

	private JTextField txtX2;

	private JTextField txtY1;

	private JTextField txtY2;

	private JPanel constractVectorPanel() {
		final VectorPanel pnlDiagramm = new LinearVectorPanel();
		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				pnlDiagramm.repaint();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				pnlDiagramm.repaint();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				pnlDiagramm.repaint();
			}
		};

		txtX1 = new JTextField();
		txtX2 = new JTextField();
		txtY1 = new JTextField();
		txtY2 = new JTextField();

		txtX1.setHorizontalAlignment(SwingConstants.RIGHT);
		txtX2.setHorizontalAlignment(SwingConstants.RIGHT);
		txtY1.setHorizontalAlignment(SwingConstants.RIGHT);
		txtY2.setHorizontalAlignment(SwingConstants.RIGHT);

		txtX1.setPreferredSize(new Dimension(60, 16));
		txtX2.setPreferredSize(new Dimension(60, 16));
		txtY1.setPreferredSize(new Dimension(60, 16));
		txtY2.setPreferredSize(new Dimension(60, 16));

		txtX1.setMinimumSize(new Dimension(60, 16));
		txtX2.setMinimumSize(new Dimension(60, 16));
		txtY1.setMinimumSize(new Dimension(60, 16));
		txtY2.setMinimumSize(new Dimension(60, 16));

		txtX1.getDocument().addDocumentListener(documentListener);
		txtX2.getDocument().addDocumentListener(documentListener);
		txtY1.getDocument().addDocumentListener(documentListener);
		txtY2.getDocument().addDocumentListener(documentListener);

		JPanel pnlVector = new JPanel();
		pnlVector.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Vector"));
		pnlVector.setLayout(new GridBagLayout());

		JLabel lblX1 = new JLabel("x1=");
		JLabel lblX2 = new JLabel("x2=");
		JLabel lblY1 = new JLabel("y1=");
		JLabel lblY2 = new JLabel("y2=");

		lblX1.setForeground(Color.red);
		lblY1.setForeground(Color.red);
		lblX2.setForeground(Color.blue);
		lblY2.setForeground(Color.blue);

		pnlVector.add(lblX1, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		pnlVector.add(txtX1, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 2), 0, 0));
		pnlVector.add(new JLabel("%"), new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));

		pnlVector.add(lblY1, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlVector.add(txtY1, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 2), 0, 0));
		pnlVector.add(new JLabel("%"), new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlVector.add(lblX2, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlVector.add(txtX2, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 2), 0, 0));
		pnlVector.add(new JLabel("%"), new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlVector.add(lblY2, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlVector.add(txtY2, new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 2), 0, 0));
		pnlVector.add(new JLabel("%"), new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlVector.add(pnlDiagramm, new GridBagConstraints(3, 0, 1, 5, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));

		// add fillers
		pnlVector.add(new JLabel(""), new GridBagConstraints(4, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pnlVector.add(new JLabel(""), new GridBagConstraints(0, 12, 1, 1, 0, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		return pnlVector;
	}

	class LinearVectorPanel extends VectorPanel {
		@Override
		public void setPoint(int point, String xText, String yText) {
			if (point == 1) {
				txtX1.setText(xText);
				txtY1.setText(yText);
			} else {
				txtX2.setText(xText);
				txtY2.setText(yText);
			}
		}

		@Override
		public int getPoint(int x, int y) {
			if (containsPoint(x, y, getX1(), getY1())) {
				return 1;
			}

			if (containsPoint(x, y, getX2(), getY2())) {
				return 2;
			}

			return 0;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			int x1 = (int) (getWidth() * getX1());
			int x2 = (int) (getWidth() * getX2());
			int y1 = (int) (getHeight() * getY1());
			int y2 = (int) (getHeight() * getY2());

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(Color.black);
			g.drawLine(x1, y1, x2, y2);

			g.setColor(Color.red);
			g.drawRect(x1 - 3, y1 - 3, 6, 6);

			g.setColor(Color.blue);
			g.drawRect(x2 - 3, y2 - 3, 6, 6);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	public float getX1() {
		try {
			return Float.parseFloat(txtX1.getText()) / 100.0f;
		} catch (NumberFormatException exc) {
			return 0.0F;
		}
	}

	public float getX2() {
		try {
			return Float.parseFloat(txtX2.getText()) / 100.0f;
		} catch (NumberFormatException exc) {
			return 1.0F;
		}
	}

	public float getY1() {
		try {
			return Float.parseFloat(txtY1.getText()) / 100.0f;
		} catch (NumberFormatException exc) {
			return 0.0F;
		}
	}

	public float getY2() {
		try {
			return Float.parseFloat(txtY2.getText()) / 100.0f;
		} catch (NumberFormatException exc) {
			return 0.0F;
		}
	}

	@Override
	protected Resource<LinearGradient> createResource() {
		LinearGradient gradient = new LinearGradient(getFractions(), getColors(), getCycleMethod(), getX1(), getY1(), getX2(), getY2());;
		return new LinearGradientResource(gradient);
	}

	public static void main(String[] args) {
		Util.installDefaultFont(Fonts.getFont("Dialog", 0, 11));
		GradientChooser<?> chooser = new LinearGradientChooser(new JVGResources());
		chooser.setVisible(true);
	}
}
