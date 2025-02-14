package ru.nest.jvg.editor;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.RadialGradientResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.swing.gradient.RadialGradient;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class RadialGradientChooser extends GradientChooser<RadialGradient> {
	public RadialGradientChooser(JVGResources resources) {
		this(resources, null);
	}

	public RadialGradientChooser(JVGResources resources, Resource<RadialGradient> gradientResource) {
		super(resources, gradientResource);
	}

	@Override
	public void setResource(Resource<RadialGradient> resource) {
		super.setResource(resource);
		if (resource != null) {
			RadialGradient gradient = resource.getResource();

			txtCX.setText(format(100 * gradient.getCX()));
			txtCY.setText(format(100 * gradient.getCY()));
			txtFX.setText(format(100 * gradient.getFX()));
			txtFY.setText(format(100 * gradient.getFY()));
			txtR.setText(format(100 * gradient.getR()));
		}
	}

	@Override
	protected JPanel constractChooserPanel() {
		JPanel pnlContent = super.constractChooserPanel();

		JPanel pnlVector = constractVectorPanel();
		pnlContent.add(pnlVector, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

		return pnlContent;
	}

	private JTextField txtCX;

	private JTextField txtCY;

	private JTextField txtFX;

	private JTextField txtFY;

	private JTextField txtR;

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

		txtCX = new JTextField();
		txtCY = new JTextField();
		txtFX = new JTextField();
		txtFY = new JTextField();
		txtR = new JTextField();

		txtCX.setHorizontalAlignment(SwingConstants.RIGHT);
		txtCY.setHorizontalAlignment(SwingConstants.RIGHT);
		txtFX.setHorizontalAlignment(SwingConstants.RIGHT);
		txtFY.setHorizontalAlignment(SwingConstants.RIGHT);
		txtR.setHorizontalAlignment(SwingConstants.RIGHT);

		txtCX.setPreferredSize(new Dimension(60, 16));
		txtCY.setPreferredSize(new Dimension(60, 16));
		txtFX.setPreferredSize(new Dimension(60, 16));
		txtFY.setPreferredSize(new Dimension(60, 16));
		txtR.setPreferredSize(new Dimension(60, 16));

		txtCX.getDocument().addDocumentListener(documentListener);
		txtCY.getDocument().addDocumentListener(documentListener);
		txtFX.getDocument().addDocumentListener(documentListener);
		txtFY.getDocument().addDocumentListener(documentListener);
		txtR.getDocument().addDocumentListener(documentListener);

		JPanel pnlVector = new JPanel();
		pnlVector.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Vector"));
		pnlVector.setLayout(new GridBagLayout());

		JLabel lblCX = new JLabel("cx=");
		JLabel lblCY = new JLabel("cy=");
		JLabel lblFX = new JLabel("fx=");
		JLabel lblFY = new JLabel("fy=");
		JLabel lblR = new JLabel("r=");

		lblCX.setForeground(Color.red);
		lblCY.setForeground(Color.red);
		lblFX.setForeground(Color.blue);
		lblFY.setForeground(Color.blue);
		lblR.setForeground(Color.green);

		pnlVector.add(lblCX, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		pnlVector.add(txtCX, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 2), 0, 0));
		pnlVector.add(new JLabel("%"), new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));

		pnlVector.add(lblCY, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlVector.add(txtCY, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 2), 0, 0));
		pnlVector.add(new JLabel("%"), new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlVector.add(lblFX, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlVector.add(txtFX, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 2), 0, 0));
		pnlVector.add(new JLabel("%"), new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlVector.add(lblFY, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlVector.add(txtFY, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 2), 0, 0));
		pnlVector.add(new JLabel("%"), new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlVector.add(lblR, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlVector.add(txtR, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlVector.add(new JLabel(" %"), new GridBagConstraints(2, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlVector.add(pnlDiagramm, new GridBagConstraints(3, 0, 1, 6, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));

		// add fillers
		pnlVector.add(new JLabel(""), new GridBagConstraints(4, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pnlVector.add(new JLabel(""), new GridBagConstraints(0, 100, 1, 1, 0, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		return pnlVector;
	}

	class LinearVectorPanel extends VectorPanel {
		@Override
		public void setPoint(int point, String xText, String yText) {
			switch (point) {
				case 1:
					txtCX.setText(xText);
					txtCY.setText(yText);
					break;

				case 2:
					txtFX.setText(xText);
					txtFY.setText(yText);
					break;

				case 3:
					txtR.setText(yText);
					break;
			}
		}

		@Override
		protected String format(float value) {
			if (point == 3) {
				value = Math.abs(100 * getCY() - value);
			}

			return super.format(value);
		}

		@Override
		public int getPoint(int x, int y) {
			if (containsPoint(x, y, getCX(), getCY())) {
				return 1;
			}

			if (containsPoint(x, y, getFX(), getFY())) {
				return 2;
			}

			if (containsPoint(x, y, getCX(), getCY() - getR())) {
				return 3;
			}

			return 0;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			int cx = (int) (getWidth() * getCX());
			int cy = (int) (getWidth() * getCY());
			int fx = (int) (getHeight() * getFX());
			int fy = (int) (getHeight() * getFY());
			int r = (int) (getHeight() * getR());
			int rx = cx;
			int ry = (int) (getHeight() * (getCY() - getR()));

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(Color.black);
			g.drawLine(cx, cy, rx, ry);
			g.drawArc(cx - r, cy - r, 2 * r, 2 * r, 0, 360);

			g.setColor(Color.blue);
			g.drawRect(fx - 3, fy - 3, 6, 6);

			g.setColor(Color.red);
			g.drawRect(cx - 3, cy - 3, 6, 6);

			g.setColor(Color.green);
			g.drawRect(rx - 3, ry - 3, 6, 6);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	public float getCX() {
		try {
			return Float.parseFloat(txtCX.getText()) / 100.0f;
		} catch (NumberFormatException exc) {
			return 0.5F;
		}
	}

	public float getCY() {
		try {
			return Float.parseFloat(txtCY.getText()) / 100.0f;
		} catch (NumberFormatException exc) {
			return 0.5F;
		}
	}

	public float getFX() {
		try {
			return Float.parseFloat(txtFX.getText()) / 100.0f;
		} catch (NumberFormatException exc) {
			return 0.5F;
		}
	}

	public float getFY() {
		try {
			return Float.parseFloat(txtFY.getText()) / 100.0f;
		} catch (NumberFormatException exc) {
			return 0.5F;
		}
	}

	public float getR() {
		try {
			return Math.abs(Float.parseFloat(txtR.getText()) / 100.0f);
		} catch (NumberFormatException exc) {
			return 0.5F;
		}
	}

	@Override
	public Resource<RadialGradient> createResource() {
		RadialGradient gradient = new RadialGradient(getFractions(), getColors(), getCycleMethod(), getCX(), getCY(), getFX(), getFY(), getR());
		return new RadialGradientResource(gradient);
	}

	public static void main(String[] args) {
		Util.installDefaultFont(Fonts.getFont("Dialog", 0, 11));
		GradientChooser<?> chooser = new RadialGradientChooser(new JVGResources());
		chooser.setVisible(true);
	}
}
