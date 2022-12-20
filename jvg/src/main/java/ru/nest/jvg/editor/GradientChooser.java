package ru.nest.jvg.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.IconButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WComboBox;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.gradient.Gradient;
import javax.swing.gradient.LinearGradientPaint;
import javax.swing.gradient.MultipleGradientPaint;

import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.Resource;

public abstract class GradientChooser<G extends Gradient> extends AbstractChooserDialog<G> {
	private GradientPanel gradientPanel;

	private GradientSlider gradientSlider;

	private IconButton btnDelete;

	private JButton btnColor;

	private JTextField txtPos;

	private JSlider opacitySlider;

	private JComboBox cmbCycleMethod;

	public GradientChooser(JVGResources resources) {
		this(resources, null);
	}

	public GradientChooser(JVGResources resources, Resource<G> resource) {
		super(resources, resource);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocation(100, 100);
		pack();
		Util.centrate(this);
	}

	@Override
	public void init() {
		super.init();
		setTitle("");
		setCurrentPos(null);
	}

	@Override
	public void setResource(Resource<G> resource) {
		super.setResource(resource);

		positions.clear();
		if (resource == null) {
			positions.add(new GradPos(0.0f, ColorResource.black));
			positions.add(new GradPos(1.0f, ColorResource.black));
		} else {
			Gradient gradient = resource.getResource();
			for (int i = 0; i < gradient.getFractions().length; i++) {
				float fraction = gradient.getFractions()[i];
				Resource<Color> color = gradient.getColors()[i];
				positions.add(new GradPos(fraction, color));
			}

			if (gradient.getCycleMethod() == MultipleGradientPaint.REPEAT) {
				cmbCycleMethod.setSelectedIndex(1);
			} else if (gradient.getCycleMethod() == MultipleGradientPaint.REFLECT) {
				cmbCycleMethod.setSelectedIndex(2);
			}
		}
	}

	@Override
	protected JPanel constractChooserPanel() {
		positions = new ArrayList<>();

		JPanel pnlContent = new JPanel();
		pnlContent.setLayout(new GridBagLayout());

		pnlContent.add(constractNamePanel(), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		JPanel pnlMainPanel = constractMainPanel();
		pnlContent.add(pnlMainPanel, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

		return pnlContent;
	}

	protected JPanel constractMainPanel() {
		JPanel pnlMain = new JPanel();
		pnlMain.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), lm.getValue("chooser.gradient.colors", "Colors")));
		pnlMain.setLayout(new BorderLayout());

		JPanel pnlGradPanel = constractFullGradPanel();
		pnlMain.add(pnlGradPanel, BorderLayout.CENTER);

		JPanel pnlPropsPanel = constractPropertiesPanel();
		pnlMain.add(pnlPropsPanel, BorderLayout.SOUTH);

		return pnlMain;
	}

	private JPanel constractPropertiesPanel() {
		JPanel pnlProps = new JPanel();
		pnlProps.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), lm.getValue("chooser.gradient.properties", "Properties")));
		pnlProps.setLayout(new GridBagLayout());

		txtPos = new JTextField(6);
		txtPos.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentPos != null) {
					try {
						float pos = Float.parseFloat(txtPos.getText());
						if (pos >= 0 && pos <= 100) {
							currentPos.fraction = pos / 100.0f;
							sort();
							repaint();
						}
					} catch (NumberFormatException exc) {
					}
				}
			}
		});

		btnColor = new JButton();
		btnColor.setActionCommand("color");
		btnColor.addActionListener(this);
		btnColor.setRequestFocusEnabled(false);
		btnColor.setPreferredSize(new Dimension(30, 20));

		opacitySlider = new JSlider();
		opacitySlider.setMinimum(0);
		opacitySlider.setMaximum(255);
		opacitySlider.setPaintLabels(true);
		opacitySlider.setPaintTicks(true);
		opacitySlider.setPaintTrack(true);
		opacitySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (currentPos != null) {
					Color color = currentPos.color.getResource();
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacitySlider.getValue());
					currentPos.color = new ColorResource(color);
					repaint();
				}
			}
		});

		cmbCycleMethod = new WComboBox(new String[] { lm.getValue("chooser.gradient.cycle-method.no", "No cycle"), lm.getValue("chooser.gradient.cycle-method.repeat", "Repead"), lm.getValue("chooser.gradient.cycle-method.reflect", "Reflect") });
		cmbCycleMethod.setBackground(Color.gray);

		pnlProps.add(new JLabel(lm.getValue("chooser.gradient.offset", "Offset: ")), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		pnlProps.add(txtPos, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 5), 0, 0));
		pnlProps.add(new JLabel(lm.getValue("chooser.gradient.percent", "%")), new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));

		pnlProps.add(new JLabel(lm.getValue("chooser.gradient.color", "Color: ")), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlProps.add(btnColor, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlProps.add(new JLabel(), new GridBagConstraints(1, 1, 10, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		pnlProps.add(new JLabel(lm.getValue("chooser.gradient.opacity", "Opacity: ")), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlProps.add(opacitySlider, new GridBagConstraints(1, 2, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

		pnlProps.add(new JLabel(lm.getValue("chooser.gradient.cycle-method", "Cycle method: ")), new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
		pnlProps.add(cmbCycleMethod, new GridBagConstraints(1, 3, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

		return pnlProps;
	}

	private JPanel constractFullGradPanel() {
		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BorderLayout());

		JPanel pnlManage = constractManagePanel();
		pnlMain.add(pnlManage, BorderLayout.WEST);

		JPanel pnlGrad = constractGradPanel();
		pnlGrad.setPreferredSize(new Dimension(300, pnlManage.getHeight()));
		pnlMain.add(pnlGrad, BorderLayout.CENTER);

		return pnlMain;
	}

	@Override
	protected JPanel constractOptionsPanel() {
		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

		JButton btnOK = new JButton(lm.getValue("chooser.gradient.button.ok", "OK"));
		btnOK.setActionCommand("option-ok");
		btnOK.addActionListener(this);
		pnlOptions.add(btnOK);

		JButton btnCancel = new JButton(lm.getValue("chooser.gradient.button.cancel", "Cancel"));
		btnCancel.setActionCommand("option-cancel");
		btnCancel.addActionListener(this);
		pnlOptions.add(btnCancel);

		return pnlOptions;
	}

	private JPanel constractGradPanel() {
		gradientSlider = new GradientSlider();
		gradientPanel = new GradientPanel();

		JPanel pnlGrad = new JPanel();
		pnlGrad.setBorder(BorderFactory.createEtchedBorder());
		pnlGrad.setLayout(new BorderLayout());
		pnlGrad.add(gradientSlider, BorderLayout.NORTH);
		pnlGrad.add(gradientPanel, BorderLayout.CENTER);
		return pnlGrad;
	}

	private JPanel constractManagePanel() {
		JPanel pnlManage = new JPanel();
		pnlManage.setLayout(new GridBagLayout());

		IconButton btnAdd = new IconButton(new ImageIcon(GradientChooser.class.getResource("img/add.png")));
		btnAdd.setRequestFocusEnabled(false);
		btnAdd.setActionCommand("add");
		btnAdd.addActionListener(this);
		pnlManage.add(btnAdd, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		btnDelete = new IconButton(new ImageIcon(GradientChooser.class.getResource("img/delete.gif")));
		btnDelete.setActionCommand("delete");
		btnDelete.addActionListener(this);
		btnDelete.setRequestFocusEnabled(false);
		pnlManage.add(btnDelete, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));

		pnlManage.add(new JLabel(), new GridBagConstraints(0, 2, 1, 1, 0, 1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 5, 5, 5), 0, 0));

		return pnlManage;
	}

	private List<GradPos> positions;

	public Resource<Color>[] getColors() {
		Resource<Color>[] colors = new Resource[positions.size()];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = positions.get(i).color;
		}
		return colors;
	}

	public float[] getFractions() {
		float[] fractions = new float[positions.size()];
		for (int i = 0; i < fractions.length; i++) {
			fractions[i] = positions.get(i).fraction;
		}
		return fractions;
	}

	private void sort() {
		Collections.sort(positions);
	}

	public void deletePos(GradPos pos) {
		if (pos != null) {
			if (pos == currentPos) {
				setCurrentPos(null);
			}

			positions.remove(pos);
			repaint();
		}
	}

	private void addPos() {
		positions.add(new GradPos());
		sort();
		repaint();
	}

	private void chooseColor(GradPos pos) {
		if (pos != null) {
			JColorChooser colorChooser = new JColorChooser(pos.color.getResource());
			Color c = JColorChooser.showDialog(null, lm.getValue("chooser.color.more.choose", "Choose Color"), pos.color.getResource());
			if (c != null) {
				if (opacitySlider.getValue() != 255) {
					c = new Color(c.getRed(), c.getGreen(), c.getBlue(), opacitySlider.getValue());
				}

				pos.color = new ColorResource(c);
				updateProperties();
				repaint();
			}
		}
	}

	private GradPos currentPos;

	private void setCurrentPos(GradPos currentPos) {
		this.currentPos = currentPos;
		btnDelete.setEnabled(currentPos != null);
		btnColor.setEnabled(currentPos != null);
		opacitySlider.setEnabled(currentPos != null);
		txtPos.setEnabled(currentPos != null);

		updateProperties();
	}

	private void updateProperties() {
		// set offset
		String s = currentPos != null ? Float.toString(currentPos.fraction * 100) : "";
		if (s.length() > 5) {
			s = s.substring(0, 5);
		}
		txtPos.setText(s);

		// set color
		Color color = currentPos != null ? currentPos.color.getResource() : Color.gray;
		Color pureColor = color;
		if (color.getAlpha() != 255) {
			pureColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		}
		btnColor.setBackground(pureColor);

		// set opacity
		opacitySlider.setValue(color.getAlpha());
	}

	class GradientSlider extends JLabel {
		public GradientSlider() {
			setPreferredSize(new Dimension(300, 16));
			setRequestFocusEnabled(true);
			setFocusable(true);
			setOpaque(true);
			setBackground(Color.white);

			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
						mousePos = getPos(e.getX());
						setCurrentPos(mousePos);
						requestFocus();
						repaint();
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					mousePos = null;
				}
			});

			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					if (mousePos != null) {
						mousePos.fraction = posToFraction(e.getX());
						updateProperties();
						sort();

						repaint();
						gradientPanel.repaint();
					}
				}
			});

			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_DELETE) {
						deletePos(currentPos);
					}
				}
			});
		}

		private GradPos mousePos;

		private float posToFraction(int x) {
			float fraction = (x - 10) / (float) (getWidth() - 20);
			if (fraction < 0) {
				fraction = 0;
			} else if (fraction > 1) {
				fraction = 1;
			}
			return fraction;
		}

		private int fractionToPos(float fraction) {
			return 10 + (int) (fraction * (getWidth() - 20));
		}

		public GradPos getPos(int x) {
			for (int i = 0; i < positions.size(); i++) {
				GradPos pos = positions.get(i);
				int gx = fractionToPos(pos.fraction);
				if (x >= gx - 3 && x <= gx + 3) {
					return pos;
				}
			}
			return null;
		}

		private Polygon p = new Polygon(new int[5], new int[5], 5);

		private Stroke selectionStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.CAP_SQUARE, 0f, new float[] { 1f, 1f }, 1);

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			int y = getHeight() - 3;
			for (int i = 0; i < positions.size(); i++) {
				GradPos pos = positions.get(i);
				int x = fractionToPos(pos.fraction);

				p.xpoints[0] = x;
				p.ypoints[0] = y;

				p.xpoints[1] = x - 3;
				p.ypoints[1] = y - 3;

				p.xpoints[2] = x - 3;
				p.ypoints[2] = y - 8;

				p.xpoints[3] = x + 3;
				p.ypoints[3] = y - 8;

				p.xpoints[4] = x + 3;
				p.ypoints[4] = y - 3;

				g.setColor(Color.lightGray);
				g.fillPolygon(p);

				g.setColor(Color.darkGray);
				g.drawPolygon(p);

				if (pos == currentPos) {
					Graphics2D g2d = (Graphics2D) g;
					Stroke oldStroke = g2d.getStroke();
					g2d.setStroke(selectionStroke);

					g.setColor(Color.black);
					g.drawRect(x - 5, y - 10, 10, 11);

					g2d.setStroke(oldStroke);
				}
			}
		}
	}

	class GradientPanel extends JLabel {
		public GradientPanel() {
			setPreferredSize(new Dimension(300, 75));
			setOpaque(true);
			setBackground(Color.white);
		}

		float[] fractions;

		Color[] colors;

		void updateCache() {
			if (colors == null || colors.length != positions.size()) {
				colors = new Color[positions.size()];
				fractions = new float[positions.size()];
			}

			for (int i = 0; i < colors.length; i++) {
				colors[i] = positions.get(i).color.getResource();
				fractions[i] = positions.get(i).fraction;
			}
		}

		private Stroke stroke = new BasicStroke(3.f);

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			g.clipRect(10, 0, getWidth() - 20, getHeight() - 10);

			Graphics2D g2d = (Graphics2D) g;

			Stroke oldStroke = g2d.getStroke();
			g2d.setColor(Color.blue);
			g2d.setStroke(stroke);
			int endPos = getWidth() + getHeight();
			for (int i = 0; i < endPos; i += 7) {
				g.drawLine(i, 0, 0, i);
			}
			g2d.setStroke(oldStroke);

			updateCache();

			if (colors.length > 1) {
				Paint paint = new LinearGradientPaint(0, 0, getWidth() - 1, 0, fractions, colors);
				g2d.setPaint(paint);
				g2d.fillRect(10, 0, getWidth() - 20, getHeight() - 10);
			}
		}
	}

	static class GradPos implements Comparable<GradPos> {
		public GradPos() {
			this(0.0f, ColorResource.black);
		}

		public GradPos(float fraction, Resource<Color> color) {
			this.fraction = fraction;
			this.color = color;
		}

		float fraction;

		Resource<Color> color;

		@Override
		public int compareTo(GradPos pos) {
			if (fraction > pos.fraction) {
				return 1;
			}

			if (fraction < pos.fraction) {
				return -1;
			}

			return 0;
		}
	}

	public MultipleGradientPaint.CycleMethodEnum getCycleMethod() {
		switch (cmbCycleMethod.getSelectedIndex()) {
			case 0:
				return MultipleGradientPaint.NO_CYCLE;

			case 1:
				return MultipleGradientPaint.REPEAT;

			case 2:
				return MultipleGradientPaint.REFLECT;
		}

		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		String cmd = e.getActionCommand();
		if ("add".equals(cmd)) {
			addPos();
		} else if ("delete".equals(cmd)) {
			deletePos(currentPos);
		} else if ("color".equals(cmd)) {
			chooseColor(currentPos);
		}
	}

	protected String format(float value) {
		String text = Float.toString(value);
		if (text.length() > 5) {
			text = text.substring(0, 5);
		}
		return text;
	}
}

abstract class VectorPanel extends JPanel {
	protected int point = 0;

	public VectorPanel() {
		setBackground(Color.white);
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
		setPreferredSize(new Dimension(150, 150));
		setMinimumSize(new Dimension(150, 150));
		setMaximumSize(new Dimension(150, 150));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					point = getPoint(e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				point = 0;
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				movePoint(e.getX(), e.getY());
			}
		});
	}

	protected void movePoint(int mx, int my) {
		if (point != 0) {
			float x = 100 * mx / (float) getWidth();
			if (x < 0) {
				x = 0;
			} else if (x > 100) {
				x = 100;
			}

			float y = 100 * my / (float) getHeight();
			if (y < 0) {
				y = 0;
			} else if (y > 100) {
				y = 100;
			}

			String xText = format(x);
			String yText = format(y);

			setPoint(point, xText, yText);
		}
	}

	protected String format(float value) {
		String text = Float.toString(value);
		if (text.length() > 5) {
			text = text.substring(0, 5);
		}
		return text;
	}

	public abstract void setPoint(int point, String xText, String yText);

	public abstract int getPoint(int x, int y);

	public boolean containsPoint(int x, int y, float xPoint, float yPoint) {
		int px = (int) (getWidth() * xPoint);
		int py = (int) (getHeight() * yPoint);
		return x >= px - 3 && x <= px + 3 && y >= py - 3 && y <= py + 3;
	}
}
