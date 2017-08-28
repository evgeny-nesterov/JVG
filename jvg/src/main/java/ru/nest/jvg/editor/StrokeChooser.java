package ru.nest.jvg.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;

public class StrokeChooser extends AbstractChooserDialog<BasicStroke> implements ActionListener {
	private JTextField txtLineWidth;

	private JTextField txtMiterLimit;

	private JTextField txtDashPhase;

	private JTextField txtDashArray;

	private JComboBox cmbEndCap;

	private JComboBox cmbLineJoin;

	private ExamplePanel examplePanel;

	private JCheckBox chkAntialias;

	public StrokeChooser(JVGResources resources) {
		this(resources, null);
	}

	public StrokeChooser(JVGResources resources, StrokeResource<BasicStroke> resource) {
		super(resources, resource);
	}

	@Override
	public void init() {
		super.init();
		setTitle(lm.getValue("chooser.stroke.title", "Stroke chooser"));
	}

	@Override
	public void setResource(Resource<BasicStroke> resource) {
		super.setResource(resource);
		if (resource != null) {
			if (resource.getResource() != null) {
				BasicStroke stroke = resource.getResource();
				txtLineWidth.setText(Float.toString(stroke.getLineWidth()));
				txtMiterLimit.setText(Float.toString(stroke.getMiterLimit()));
				txtDashPhase.setText(Float.toString(stroke.getDashPhase()));
				cmbEndCap.setSelectedIndex(stroke.getEndCap());
				cmbLineJoin.setSelectedIndex(stroke.getLineJoin());

				if (stroke.getDashArray() != null) {
					String array = "";
					for (float f : stroke.getDashArray()) {
						array += f + "; ";
					}
					txtDashArray.setText(array);
				}
			}
		} else {
			txtLineWidth.setText("1");
		}
	}

	@Override
	protected JPanel constractChooserPanel() {
		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				examplePanel.repaint();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				examplePanel.repaint();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		};

		txtLineWidth = new JTextField();
		cmbEndCap = new WComboBox(new String[] { lm.getValue("chooser.stroke.cap.butt", "Butt"), lm.getValue("chooser.stroke.cap.round", "Round"), lm.getValue("chooser.stroke.cap.square", "Square") });
		cmbEndCap.setBackground(Color.gray);

		cmbLineJoin = new WComboBox(new String[] { lm.getValue("chooser.stroke.join.miter", "Miter"), lm.getValue("chooser.stroke.join.round", "Round"), lm.getValue("chooser.stroke.join.bevel", "Bevel") });
		cmbLineJoin.setBackground(Color.gray);

		txtMiterLimit = new JTextField();
		txtDashArray = new JTextField();
		txtDashPhase = new JTextField();
		chkAntialias = new JCheckBox(lm.getValue("chooser.stroke.antialias", "Antialias"));

		cmbEndCap.addActionListener(this);
		cmbLineJoin.addActionListener(this);
		chkAntialias.addActionListener(this);

		cmbEndCap.setActionCommand("update");
		cmbLineJoin.setActionCommand("update");
		chkAntialias.setActionCommand("update");

		txtLineWidth.getDocument().addDocumentListener(documentListener);
		txtMiterLimit.getDocument().addDocumentListener(documentListener);
		txtDashArray.getDocument().addDocumentListener(documentListener);
		txtDashPhase.getDocument().addDocumentListener(documentListener);

		examplePanel = new ExamplePanel();

		JPanel pnlName = constractNamePanel();
		pnlName.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createEtchedBorder()));

		JPanel pnlStroke = new JPanel();
		pnlStroke.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10), BorderFactory.createEtchedBorder()));
		pnlStroke.setLayout(new GridBagLayout());

		pnlStroke.add(new JLabel(lm.getValue("chooser.stroke.line-width", "Line width: ")), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlStroke.add(txtLineWidth, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));

		pnlStroke.add(new JLabel(lm.getValue("chooser.stroke.end-cap", "End cap: ")), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlStroke.add(cmbEndCap, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlStroke.add(new JLabel(lm.getValue("chooser.stroke.line-join", "Line join: ")), new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlStroke.add(cmbLineJoin, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlStroke.add(new JLabel(lm.getValue("chooser.stroke.miter-limit", "Miter limit: ")), new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlStroke.add(txtMiterLimit, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlStroke.add(new JLabel(lm.getValue("chooser.stroke.dash-array", "Dash array: ")), new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlStroke.add(txtDashArray, new GridBagConstraints(1, 5, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlStroke.add(new JLabel(lm.getValue("chooser.stroke.dash-phase", "Dash phase: ")), new GridBagConstraints(0, 6, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlStroke.add(txtDashPhase, new GridBagConstraints(1, 6, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlStroke.add(examplePanel, new GridBagConstraints(0, 100, 100, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		pnlStroke.add(chkAntialias, new GridBagConstraints(0, 101, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BorderLayout());
		pnlMain.add(pnlName, BorderLayout.NORTH);
		pnlMain.add(pnlStroke, BorderLayout.CENTER);

		return pnlMain;
	}

	public BasicStroke getStroke() {
		return new BasicStroke(getStrokeWidth(), getCap(), getJoin(), getMiterlimit(), getDash(), getDashPhase());
	}

	@Override
	public Resource<BasicStroke> createResource() {
		return new StrokeResource<BasicStroke>(getStroke());
	}

	public int getCap() {
		int cap = cmbEndCap.getSelectedIndex();
		return cap != -1 ? cap : BasicStroke.CAP_SQUARE;
	}

	public int getJoin() {
		int join = cmbLineJoin.getSelectedIndex();
		return join != -1 ? join : BasicStroke.JOIN_MITER;
	}

	public float getStrokeWidth() {
		try {
			return Float.parseFloat(txtLineWidth.getText());
		} catch (NumberFormatException ex) {
			return 1.0F;
		}
	}

	public float getMiterlimit() {
		try {
			return Float.parseFloat(txtMiterLimit.getText());
		} catch (NumberFormatException ex) {
			return 10.0F;
		}
	}

	public float getDashPhase() {
		try {
			return Float.parseFloat(txtDashPhase.getText());
		} catch (NumberFormatException ex) {
			return 0.0F;
		}
	}

	public float[] getDash() {
		float[] dash = null;
		try {
			StringTokenizer tokenizer = new StringTokenizer(txtDashArray.getText(), ";", false);
			int size = tokenizer.countTokens();
			if (size > 0) {
				dash = new float[size];

				int index = 0;
				while (tokenizer.hasMoreElements()) {
					dash[index++] = Float.parseFloat(tokenizer.nextToken());
				}
				return dash;
			}
		} catch (Exception exc) {
		}
		return null;
	}

	class ExamplePanel extends JLabel {
		public ExamplePanel() {
			setOpaque(true);
			setBackground(Color.white);
			setBorder(BorderFactory.createEtchedBorder());
			setPreferredSize(new Dimension(150, 150));

			Font font = Fonts.getFont("Dialog", Font.BOLD, 80);
			GlyphVector gv = font.createGlyphVector(new FontRenderContext(null, false, false), "Aa Bb");
			shapeLetter = gv.getOutline();
			Rectangle letterBounds = shapeLetter.getBounds();
			letterWidth = letterBounds.width;
			letterHeight = letterBounds.height;
		}

		private Shape shapeLetter;

		private int letterWidth, letterHeight;

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(getStroke());
			g2d.setColor(Color.black);

			if (chkAntialias.isSelected()) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}

			g2d.drawRect(10, 10, getWidth() - 20, getHeight() - 20);

			g2d.translate((getWidth() - letterWidth) / 2, (getHeight() + letterHeight) / 2);
			g2d.draw(shapeLetter);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		String cmd = e.getActionCommand();
		if ("update".equals(cmd)) {
			examplePanel.repaint();
		}
	}

	public static void main(String[] args) {
		Util.installDefaultFont(Fonts.getFont("Dialog", 0, 11));
		StrokeChooser chooser = new StrokeChooser(new JVGResources());
		chooser.setVisible(true);
	}
}
