package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ru.nest.fonts.FontComboBox;
import ru.nest.fonts.Fonts;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.Resource;

public class FontChooser extends AbstractChooserDialog<Font> implements ActionListener {
	private FontComboBox cmbFamily;

	private JComboBox cmbSize;

	private JCheckBox chkBold;

	private JCheckBox chkItalic;

	private JTextField[] txtTransform;

	private ExamplePanel examplePanel;

	private JCheckBox chkAntialias;

	private JCheckBox chkFractionalMetrix;

	public FontChooser(JVGResources resources) {
		this(resources, null);
	}

	public FontChooser(JVGResources resources, Resource<Font> resource) {
		super(resources, resource);
	}

	@Override
	public void init() {
		super.init();
		setTitle(lm.getValue("chooser.font.title", "Font chooser"));
	}

	@Override
	public void setResource(Resource<Font> resource) {
		super.setResource(resource);
		if (resource != null) {
			Font font = resource.getResource();
			if (font != null) {
				for (int i = 0; i < cmbFamily.getItemCount(); i++) {
					Font fontFromList = (Font) cmbFamily.getItemAt(i);
					if (font.getFamily().equals(fontFromList.getFamily())) {
						cmbFamily.setSelectedIndex(i);
						break;
					}
				}

				cmbSize.getEditor().setItem(font.getSize());
				chkBold.setSelected(font.isBold());
				chkItalic.setSelected(font.isItalic());

				AffineTransform transform = font.getTransform();
				double[] matrix = new double[6];
				transform.getMatrix(matrix);
				for (int i = 0; i < 6; i++) {
					txtTransform[i].setText(Double.toString(matrix[i]));
				}

				currentFont = null;
				examplePanel.repaint();
			}
		} else {
			cmbFamily.setSelectedIndex(-1);
			cmbSize.setSelectedIndex(-1);
			chkBold.setSelected(false);
			chkItalic.setSelected(false);
		}
	}

	@Override
	protected JPanel constractChooserPanel() {
		cmbFamily = new FontComboBox();
		cmbFamily.setBackground(Color.gray);
		cmbFamily.setActionCommand("update");
		cmbFamily.addActionListener(this);

		cmbSize = new WComboBox(new Integer[] { 8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 24, 28, 32, 36, 40 });
		cmbSize.setBackground(Color.gray);
		cmbSize.setSelectedItem(12);
		cmbSize.setEditable(true);
		cmbSize.setActionCommand("update");
		cmbSize.addActionListener(this);

		chkBold = new JCheckBox(lm.getValue("chooser.font.bold", "Bold"));
		chkBold.setActionCommand("update");
		chkBold.addActionListener(this);

		chkItalic = new JCheckBox(lm.getValue("chooser.font.italic", "Italic"));
		chkItalic.setActionCommand("update");
		chkItalic.addActionListener(this);

		chkAntialias = new JCheckBox(lm.getValue("chooser.font.antialias", "Antialias"));
		chkAntialias.setActionCommand("update");
		chkAntialias.addActionListener(this);

		chkFractionalMetrix = new JCheckBox(lm.getValue("chooser.font.fractional-metrix", "Fractional metrix"));
		chkFractionalMetrix.setActionCommand("update");
		chkFractionalMetrix.addActionListener(this);

		txtTransform = new JTextField[6];
		for (int i = 0; i < 6; i++) {
			txtTransform[i] = new JTextField(4);
			switch (i) {
				case 0:
				case 3:
					txtTransform[i].setText("1");
					break;

				default:
					txtTransform[i].setText("0");
					break;
			}
			txtTransform[i].getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					currentFont = null;
					examplePanel.repaint();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					currentFont = null;
					examplePanel.repaint();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
				}
			});
		}

		examplePanel = new ExamplePanel();

		// build panel
		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new GridBagLayout());

		pnlMain.add(constractNamePanel(), new GridBagConstraints(0, 0, 5, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		pnlMain.add(new JLabel(lm.getValue("chooser.font.family", "Family: ")), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlMain.add(cmbFamily, new GridBagConstraints(1, 1, 4, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlMain.add(new JLabel(lm.getValue("chooser.font.size", "Size: ")), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlMain.add(cmbSize, new GridBagConstraints(1, 2, 4, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlMain.add(chkBold, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(chkItalic, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlMain.add(new JLabel(lm.getValue("chooser.font.transform", "Transform: ")), new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlMain.add(txtTransform[0], new GridBagConstraints(1, 5, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtTransform[2], new GridBagConstraints(2, 5, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtTransform[4], new GridBagConstraints(3, 5, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtTransform[1], new GridBagConstraints(1, 6, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtTransform[3], new GridBagConstraints(2, 6, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtTransform[5], new GridBagConstraints(3, 6, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		JPanel pnlPreview = new JPanel();
		pnlPreview.setLayout(new GridBagLayout());
		pnlPreview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), lm.getValue("chooser.font.preview", "Preview")));
		pnlMain.add(pnlPreview, new GridBagConstraints(0, 7, 5, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

		pnlPreview.add(examplePanel, new GridBagConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		pnlPreview.add(chkAntialias, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
		pnlPreview.add(chkFractionalMetrix, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));

		return pnlMain;
	}

	public String getFontFamily() {
		Font font = (Font) cmbFamily.getSelectedItem();
		if (font == null) {
			font = (Font) cmbFamily.getItemAt(0);
		}
		return font.getFamily();
	}

	public int getFontSize() {
		try {
			String value = cmbSize.getEditor().getItem().toString();
			return Integer.parseInt(value);
		} catch (NumberFormatException exc) {
		}

		Integer size = (Integer) cmbSize.getSelectedItem();
		if (size == null) {
			size = 12;
		}
		return size;
	}

	public int getFontStyle() {
		int type = Font.PLAIN;
		if (chkBold.isSelected()) {
			type |= Font.BOLD;
		}
		if (chkItalic.isSelected()) {
			type |= Font.ITALIC;
		}
		return type;
	}

	public AffineTransform getTransform() {
		try {
			float[] massive = new float[6];
			for (int i = 0; i < 6; i++) {
				massive[i] = Float.parseFloat(txtTransform[i].getText());
			}
			return new AffineTransform(massive);
		} catch (NumberFormatException exc) {
		}
		return null;
	}

	private Font currentFont = null;

	class ExamplePanel extends JLabel {
		public ExamplePanel() {
			setOpaque(true);
			setBackground(Color.white);
			setBorder(BorderFactory.createLineBorder(Color.black));
			setPreferredSize(new Dimension(0, 150));
		}

		private String[] text = { "Aa Bb Cc Dd Ee Ff Gg Hh Ii Jj", "Kk Ll Mm Nn Oo Pp Qq Rr Ss Tt", "Vv Uu Ww Xx Yy Zz", "1 2 3 4 5 6 7 8 9 0", "+ - * / \\ \" \' ! # $ % ^ & ( ) { } | < > . , ? ~ ` : ; ¹" };

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			if (currentFont == null) {
				currentFont = createFont();
			}

			Graphics2D g2d = (Graphics2D) g;
			if (chkAntialias.isSelected()) {
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			}
			if (chkFractionalMetrix.isSelected()) {
				g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			}

			g.setColor(Color.black);
			g.setFont(currentFont);
			FontMetrics fm = g.getFontMetrics();
			int h = fm.getHeight();

			int maxWidth = 0;
			for (int i = 0; i < text.length; i++) {
				maxWidth = Math.max(maxWidth, fm.stringWidth(text[i]));
			}

			int x = (getWidth() - maxWidth) / 2;
			int y = (getHeight() + h * (1 - text.length)) / 2;
			for (int i = 0; i < text.length; i++) {
				g.drawString(text[i], x, y);
				y += h;
			}

			if (chkFractionalMetrix.isSelected()) {
				g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			}
			if (chkAntialias.isSelected()) {
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
		}
	}

	private Font createFont() {
		Font font = Fonts.getFont(getFontFamily(), getFontStyle(), getFontSize());
		AffineTransform transform = getTransform();
		if (transform != null) {
			font = font.deriveFont(transform);
		}
		return font;
	}

	@Override
	public Resource<Font> createResource() {
		return new FontResource(createFont());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		String cmd = e.getActionCommand();
		if ("update".equals(cmd)) {
			currentFont = null;
			examplePanel.repaint();
		}
	}

	public static void main(String[] args) {
		Util.installDefaultFont(Fonts.getFont("Dialog", 0, 11));
		FontChooser f = new FontChooser(new JVGResources());
		f.setVisible(true);
	}
}
