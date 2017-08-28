package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.TransformResource;

public class TransformChooser extends AbstractChooserDialog<AffineTransform> {
	private JTextField[] txtMatrix;

	private ExamplePanel examplePanel;

	public TransformChooser(JVGResources resources) {
		this(resources, null);
	}

	public TransformChooser(JVGResources resources, Resource<AffineTransform> resource) {
		super(resources, resource);
	}

	@Override
	public void init() {
		super.init();
		setTitle(lm.getValue("chooser.transform.title", "Transform chooser"));
	}

	@Override
	public void setResource(Resource<AffineTransform> resource) {
		super.setResource(resource);
		if (resource != null) {
			double[] matrix = new double[6];
			resource.getResource().getMatrix(matrix);
			for (int i = 0; i < 6; i++) {
				txtMatrix[i].setText(Double.toString(matrix[i]));
			}
		} else {
			txtMatrix[0].setText("1");
			txtMatrix[1].setText("0");
			txtMatrix[2].setText("0");
			txtMatrix[3].setText("1");
			txtMatrix[4].setText("0");
			txtMatrix[5].setText("0");
		}
	}

	@Override
	protected JPanel constractChooserPanel() {
		txtMatrix = new JTextField[6];
		for (int i = 0; i < 6; i++) {
			txtMatrix[i] = new JTextField(12);
			txtMatrix[i].setFont(Fonts.getFont("Dialog", Font.PLAIN, 11));
			txtMatrix[i].setPreferredSize(new Dimension(130, 18));
			txtMatrix[i].setMinimumSize(new Dimension(130, 18));
			txtMatrix[i].setHorizontalAlignment(SwingConstants.RIGHT);
			txtMatrix[i].getDocument().addDocumentListener(new DocumentListener() {
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
			});
		}
		examplePanel = new ExamplePanel();

		JPanel pnlName = constractNamePanel();
		pnlName.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createEtchedBorder()));

		JPanel pnlTransform = new JPanel();
		pnlTransform.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createEtchedBorder()));
		pnlTransform.setLayout(new GridBagLayout());

		JButton btnIdentity = new JButton(lm.getValue("chooser.transform.to-identity", "To identity"));
		btnIdentity.addActionListener(this);
		btnIdentity.setActionCommand("to-identity");
		btnIdentity.setRequestFocusEnabled(false);

		final JTextField txtRotateAngle = new JTextField("0");
		txtRotateAngle.setPreferredSize(new Dimension(130, 18));
		txtRotateAngle.setMinimumSize(new Dimension(130, 18));
		txtRotateAngle.setHorizontalAlignment(SwingConstants.RIGHT);

		final JTextField txtRotateX = new JTextField("0");
		txtRotateX.setPreferredSize(new Dimension(130, 18));
		txtRotateX.setMinimumSize(new Dimension(130, 18));
		txtRotateX.setHorizontalAlignment(SwingConstants.RIGHT);

		final JTextField txtRotateY = new JTextField("0");
		txtRotateY.setPreferredSize(new Dimension(130, 18));
		txtRotateY.setMinimumSize(new Dimension(130, 18));
		txtRotateY.setHorizontalAlignment(SwingConstants.RIGHT);

		JButton btnRotate = new JButton(lm.getValue("chooser.transform.rotate", "Rotate"));
		btnRotate.setRequestFocusEnabled(false);
		btnRotate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double angle = Double.parseDouble(txtRotateAngle.getText()) * Math.PI / 180.0;
					double rx = Double.parseDouble(txtRotateX.getText());
					double ry = Double.parseDouble(txtRotateY.getText());

					AffineTransform transform = getTransform();
					Point2D.Double centerPoint = new Point2D.Double(examplePanel.rectX + examplePanel.edgeSize / 2.0, examplePanel.rectY + examplePanel.edgeSize / 2.0);
					transform.transform(centerPoint, centerPoint);
					rx += centerPoint.getX();
					ry += centerPoint.getY();

					transform.preConcatenate(AffineTransform.getRotateInstance(angle, rx, ry));
					setTransform(transform);
				} catch (NumberFormatException exc) {
				}
			}
		});

		final JTextField txtScaleX = new JTextField("1");
		txtScaleX.setPreferredSize(new Dimension(130, 18));
		txtScaleX.setMinimumSize(new Dimension(130, 18));
		txtScaleX.setHorizontalAlignment(SwingConstants.RIGHT);

		final JTextField txtScaleY = new JTextField("1");
		txtScaleY.setPreferredSize(new Dimension(130, 18));
		txtScaleY.setMinimumSize(new Dimension(130, 18));
		txtScaleY.setHorizontalAlignment(SwingConstants.RIGHT);

		JButton btnScale = new JButton(lm.getValue("chooser.transform.scale", "Scale"));
		btnScale.setRequestFocusEnabled(false);
		btnScale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double scaleX = Double.parseDouble(txtScaleX.getText());
					double scaleY = Double.parseDouble(txtScaleY.getText());

					double cx = examplePanel.rectX + examplePanel.edgeSize / 2.0;
					double cy = examplePanel.rectY + examplePanel.edgeSize / 2.0;

					AffineTransform transform = getTransform();
					transform.concatenate(AffineTransform.getTranslateInstance(cx, cy));
					transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
					transform.concatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					setTransform(transform);
				} catch (NumberFormatException exc) {
				}
			}
		});

		final JTextField txtShearX = new JTextField("0");
		txtShearX.setPreferredSize(new Dimension(130, 18));
		txtShearX.setMinimumSize(new Dimension(130, 18));
		txtShearX.setHorizontalAlignment(SwingConstants.RIGHT);

		final JTextField txtShearY = new JTextField("0");
		txtShearY.setPreferredSize(new Dimension(130, 18));
		txtShearY.setMinimumSize(new Dimension(130, 18));
		txtShearY.setHorizontalAlignment(SwingConstants.RIGHT);

		JButton btnShear = new JButton(lm.getValue("chooser.transform.shear", "Shear"));
		btnShear.setRequestFocusEnabled(false);
		btnShear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double shearX = Double.parseDouble(txtShearX.getText());
					double shearY = Double.parseDouble(txtShearY.getText());

					double cx = examplePanel.rectX + examplePanel.edgeSize / 2.0;
					double cy = examplePanel.rectY + examplePanel.edgeSize / 2.0;

					AffineTransform transform = getTransform();
					transform.concatenate(AffineTransform.getTranslateInstance(cx, cy));
					transform.concatenate(AffineTransform.getShearInstance(shearX, shearY));
					transform.concatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					setTransform(transform);
				} catch (NumberFormatException exc) {
				}
			}
		});

		// build panel
		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new GridBagLayout());

		pnlMain.add(constractNamePanel(), new GridBagConstraints(0, 0, 5, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		pnlMain.add(new JLabel(lm.getValue("chooser.transform.transform", "Transform: ")), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlMain.add(txtMatrix[0], new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtMatrix[2], new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtMatrix[4], new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtMatrix[1], new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtMatrix[3], new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtMatrix[5], new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlMain.add(btnIdentity, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlMain.add(btnRotate, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlMain.add(txtRotateAngle, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtRotateX, new GridBagConstraints(2, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtRotateY, new GridBagConstraints(3, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlMain.add(btnScale, new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlMain.add(txtScaleX, new GridBagConstraints(1, 5, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtScaleY, new GridBagConstraints(2, 5, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		pnlMain.add(btnShear, new GridBagConstraints(0, 6, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		pnlMain.add(txtShearX, new GridBagConstraints(1, 6, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
		pnlMain.add(txtShearY, new GridBagConstraints(2, 6, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

		JPanel pnlPreview = new JPanel();
		pnlPreview.setLayout(new GridBagLayout());
		pnlPreview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), lm.getValue("chooser.transform.preview", "Preview")));
		pnlPreview.add(examplePanel, new GridBagConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		pnlMain.add(pnlPreview, new GridBagConstraints(0, 10, 5, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		return pnlMain;
	}

	private AffineTransform getTransform() {
		float[] matrix = new float[6];
		for (int i = 0; i < 6; i++) {
			try {
				matrix[i] = Float.parseFloat(txtMatrix[i].getText());
			} catch (NumberFormatException exc) {
				switch (i) {
					case 0:
					case 3:
						matrix[i] = 1f;
						break;

					case 1:
					case 2:
					case 4:
					case 5:
						matrix[i] = 0f;
						break;
				}
			}
		}
		return new AffineTransform(matrix);
	}

	@Override
	public Resource<AffineTransform> createResource() {
		return new TransformResource(getTransform());
	}

	public void setTransform(AffineTransform transform) {
		if (transform != null) {
			if (transform.getDeterminant() == 0) {
				return;
			}

			double[] matrix = new double[6];
			transform.getMatrix(matrix);
			for (int i = 0; i < 6; i++) {
				txtMatrix[i].setText(Double.toString(matrix[i]));
				txtMatrix[i].setCaretPosition(0);
			}
		}
		examplePanel.repaint();
	}

	class ExamplePanel extends JLabel {
		private final static int ON_AREA = 0;

		private final static int ON_SHAPE = 1;

		private final static int ON_ROTATION = 2;

		private final static int ON_SHEAR_N = 4;

		private final static int ON_SHEAR_W = 5;

		private final static int ON_SHEAR_S = 6;

		private final static int ON_SHEAR_E = 7;

		private final static int ON_SCALE_NW = 8;

		private final static int ON_SCALE_NE = 9;

		private final static int ON_SCALE_SW = 10;

		private final static int ON_SCALE_SE = 11;

		private boolean entered = false;

		public double edgeSize = 70;

		public double rectX = -35;

		public double rectY = -35;

		private int transformType;

		private int mx, my;

		private Color color = new Color(0, 0, 200);

		private Color transformedColor = new Color(100, 100, 200, 160);

		private Color transformedBorderColor = new Color(100, 100, 200);

		private AffineTransform transform = new AffineTransform();

		private Color smallColor = new Color(200, 200, 200, 180);

		private boolean pressed = false;

		public ExamplePanel() {
			setOpaque(true);
			setBackground(Color.white);
			setBorder(BorderFactory.createLineBorder(Color.black));
			setPreferredSize(new Dimension(300, 300));
			setMinimumSize(new Dimension(0, 300));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					entered = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					entered = false;
					repaint();
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
						mx = e.getX();
						my = e.getY();
						pressed = true;
						transformType = getManageType(mx, getHeight() - my);
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					transformType = ON_AREA;
					pressed = false;
					repaint();
				}
			});

			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					int transformType = getManageType(e.getX(), getHeight() - e.getY());
					if (transformType != ON_AREA) {
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					} else {
						setCursor(Cursor.getDefaultCursor());
					}

					if (ExamplePanel.this.transformType != transformType) {
						ExamplePanel.this.transformType = transformType;
						repaint();
					}
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					if (!pressed) {
						return;
					}

					if (transformType == ON_AREA) {
						int dx = e.getX() - mx;
						int dy = my - e.getY();
						transform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
					} else {
						AffineTransform transform = getTransform();
						Point2D.Double mousePoint = new Point2D.Double(e.getX(), getHeight() - e.getY());
						try {
							ExamplePanel.this.transform.inverseTransform(mousePoint, mousePoint);
							transform.inverseTransform(mousePoint, mousePoint);
						} catch (Exception exc) {
						}

						if (transformType == ON_SHAPE) {
							int dx = e.getX() - mx;
							int dy = my - e.getY();
							transform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
						} else if (transformType == ON_ROTATION) {
							Point2D.Double centerPoint = new Point2D.Double(rectX + edgeSize / 2.0, rectY + edgeSize / 2.0);
							Point2D.Double rotatePoint = new Point2D.Double(rectX + edgeSize / 2.0, rectY + edgeSize + 20);

							double x1 = rotatePoint.getX() - centerPoint.getX();
							double y1 = rotatePoint.getY() - centerPoint.getY();
							double x2 = mousePoint.getX() - centerPoint.getX();
							double y2 = mousePoint.getY() - centerPoint.getY();

							double r1 = Math.sqrt(x1 * x1 + y1 * y1);
							double r2 = Math.sqrt(x2 * x2 + y2 * y2);
							if (r1 == 0 && r2 == 0) {
								return;
							}

							double alfa = Math.acos((x1 * x2 + y1 * y2) / (r1 * r2));
							if (x1 * y2 - x2 * y1 < 0) {
								alfa = -alfa;
							}
							transform.concatenate(AffineTransform.getRotateInstance(alfa, centerPoint.getX(), centerPoint.getY()));
						} else if (transformType == ON_SCALE_NW || transformType == ON_SCALE_NE || transformType == ON_SCALE_SW || transformType == ON_SCALE_SE) {
							double x = examplePanel.rectX;
							double y = examplePanel.rectY;
							double newW = 0;
							double newH = 0;
							switch (transformType) {
								case ON_SCALE_NW:
									x += edgeSize;
									y += edgeSize;
									newW = x - mousePoint.getX();
									newH = y - mousePoint.getY();
									break;
								case ON_SCALE_SW:
									x += edgeSize;
									newW = x - mousePoint.getX();
									newH = mousePoint.getY() - y;
									break;
								case ON_SCALE_SE:
									newW = mousePoint.getX() - x;
									newH = mousePoint.getY() - y;
									break;
								case ON_SCALE_NE:
									y += edgeSize;
									newW = mousePoint.getX() - x;
									newH = y - mousePoint.getY();
									break;
							}

							if (newW <= 2 || newH <= 2) {
								return;
							}

							double scaleX = newW / edgeSize;
							double scaleY = newH / edgeSize;
							transform.concatenate(AffineTransform.getTranslateInstance(x, y));
							transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
							transform.concatenate(AffineTransform.getTranslateInstance(-x, -y));
						} else if (transformType == ON_SHEAR_N || transformType == ON_SHEAR_W || transformType == ON_SHEAR_S || transformType == ON_SHEAR_E) {
							double cx = rectX + edgeSize / 2.0;
							double cy = rectY + edgeSize / 2.0;

							switch (transformType) {
								case ON_SHEAR_N:
									double shear = (cx - mousePoint.getX()) / edgeSize;
									transform.concatenate(AffineTransform.getTranslateInstance(cx, rectY + edgeSize));
									transform.concatenate(AffineTransform.getShearInstance(shear, 0));
									transform.concatenate(AffineTransform.getTranslateInstance(-cx, -rectY - edgeSize));
									break;
								case ON_SHEAR_W:
									shear = (cy - mousePoint.getY()) / edgeSize;
									transform.concatenate(AffineTransform.getTranslateInstance(rectX + edgeSize, cy));
									transform.concatenate(AffineTransform.getShearInstance(0, shear));
									transform.concatenate(AffineTransform.getTranslateInstance(-rectX - edgeSize, -cy));
									break;
								case ON_SHEAR_S:
									shear = (mousePoint.getX() - cx) / edgeSize;
									transform.concatenate(AffineTransform.getTranslateInstance(cx, rectY));
									transform.concatenate(AffineTransform.getShearInstance(shear, 0));
									transform.concatenate(AffineTransform.getTranslateInstance(-cx, -rectY));
									break;
								case ON_SHEAR_E:
									shear = (mousePoint.getY() - cy) / edgeSize;
									transform.concatenate(AffineTransform.getTranslateInstance(rectX, cy));
									transform.concatenate(AffineTransform.getShearInstance(0, shear));
									transform.concatenate(AffineTransform.getTranslateInstance(-rectX, -cy));
									break;
							}
						}
						setTransform(transform);
					}
					repaint();

					mx = e.getX();
					my = e.getY();
				}
			});

			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					if (getWidth() != 0) {
						transform = AffineTransform.getTranslateInstance(getWidth() / 2, getHeight() / 2);
						repaint();
						removeComponentListener(this);
					}
				}
			});
		}

		private boolean contains(int mx, int my, Point2D p) {
			return mx >= p.getX() - 3 && mx <= p.getX() + 3 && my >= p.getY() - 3 && my <= p.getY() + 3;
		}

		private int getManageType(int mx, int my) {
			double x = rectX;
			double y = rectY;
			double w = edgeSize;
			double h = edgeSize;
			Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);

			AffineTransform transform = getTransform();
			transform.preConcatenate(ExamplePanel.this.transform);

			Point2D.Double rotatePoint = new Point2D.Double(x + w / 2, y + h + 20);
			transform.transform(rotatePoint, rotatePoint);
			if (contains(mx, my, rotatePoint)) {
				return ON_ROTATION;
			}

			Point2D.Double scalePoint = new Point2D.Double(x, y);
			transform.transform(scalePoint, scalePoint);
			if (contains(mx, my, scalePoint)) {
				return ON_SCALE_NW;
			}

			scalePoint = new Point2D.Double(x + w, y);
			transform.transform(scalePoint, scalePoint);
			if (contains(mx, my, scalePoint)) {
				return ON_SCALE_NE;
			}

			scalePoint = new Point2D.Double(x, y + h);
			transform.transform(scalePoint, scalePoint);
			if (contains(mx, my, scalePoint)) {
				return ON_SCALE_SW;
			}

			scalePoint = new Point2D.Double(x + w, y + h);
			transform.transform(scalePoint, scalePoint);
			if (contains(mx, my, scalePoint)) {
				return ON_SCALE_SE;
			}

			Point2D.Double shearPoint = new Point2D.Double(x + w / 2.0, y);
			transform.transform(shearPoint, shearPoint);
			if (contains(mx, my, shearPoint)) {
				return ON_SHEAR_N;
			}

			shearPoint = new Point2D.Double(x, y + h / 2.0);
			transform.transform(shearPoint, shearPoint);
			if (contains(mx, my, shearPoint)) {
				return ON_SHEAR_W;
			}

			shearPoint = new Point2D.Double(x + w / 2, y + h);
			transform.transform(shearPoint, shearPoint);
			if (contains(mx, my, shearPoint)) {
				return ON_SHEAR_S;
			}

			shearPoint = new Point2D.Double(x + w, y + h / 2.0);
			transform.transform(shearPoint, shearPoint);
			if (contains(mx, my, shearPoint)) {
				return ON_SHEAR_E;
			}

			Shape transformedRect = transform.createTransformedShape(rect);
			if (transformedRect.contains(mx, my)) {
				return ON_SHAPE;
			}
			return ON_AREA;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			paintShape(g2d, entered || pressed);

			// draw view
			int w = getWidth();
			int h = getHeight();
			double overheadKoef = 0.5;
			int viewWidth, viewHeight;
			if (w > h) {
				viewHeight = h / 3;
				if (viewHeight < 30) {
					return;
				}
				viewWidth = viewHeight * 3 / 2;
			} else {
				viewWidth = h / 3;
				if (viewWidth < 30) {
					return;
				}
				viewHeight = viewWidth * 2 / 3;
			}
			double scale = overheadKoef * Math.min(viewWidth / (double) w, viewHeight / (double) h);
			double iw = scale * w;
			double ih = scale * h;
			double ix = (viewWidth - iw) / 2;
			double iy = (viewHeight - ih) / 2;

			g2d.setColor(smallColor);
			g2d.fillRect(0, 0, viewWidth, viewHeight);
			g2d.setColor(Color.black);
			g2d.drawRect(0, 0, viewWidth, viewHeight);
			g2d.setColor(Color.darkGray);
			g2d.draw(new Rectangle2D.Double(ix, iy, iw, ih));
			g2d.clipRect(0, 0, viewWidth, viewHeight);

			g2d.translate(ix, iy);
			g2d.scale(scale, scale);

			paintShape(g2d, false);
		}

		private void paintShape(Graphics2D g, boolean managersActive) {
			double w = edgeSize;
			double h = edgeSize;
			double x = rectX;
			double y = rectY;

			AffineTransform transform = getTransform();
			if (transform != null) {
				transform.preConcatenate(this.transform);
			} else {
				transform = new AffineTransform(this.transform);
			}
			transform.preConcatenate(AffineTransform.getTranslateInstance(0, -getHeight() / 2));
			transform.preConcatenate(AffineTransform.getScaleInstance(1, -1));
			transform.preConcatenate(AffineTransform.getTranslateInstance(0, getHeight() / 2));

			AffineTransform initialTransform = new AffineTransform(this.transform);
			initialTransform.preConcatenate(AffineTransform.getTranslateInstance(0, -getHeight() / 2));
			initialTransform.preConcatenate(AffineTransform.getScaleInstance(1, -1));
			initialTransform.preConcatenate(AffineTransform.getTranslateInstance(0, getHeight() / 2));

			// draw initial shape
			g.transform(initialTransform);
			g.setColor(color);
			g.fillRect((int) x, (int) y, (int) w, (int) h);
			try {
				g.transform(initialTransform.createInverse());
			} catch (NoninvertibleTransformException exc) {
			}

			// draw transformed shape
			g.transform(transform);
			g.setColor(transformedColor);
			g.fillRect((int) x, (int) y, (int) w, (int) h);
			g.setColor(transformedBorderColor);
			g.drawRect((int) x, (int) y, (int) w, (int) h);
			try {
				g.transform(transform.createInverse());
			} catch (NoninvertibleTransformException exc) {
			}

			// draw manage points
			if (managersActive) {
				Point2D.Double rotatePoint = new Point2D.Double(x + w / 2.0, y + h + 20);
				transform.transform(rotatePoint, rotatePoint);

				Point2D.Double scalePoint_nw = new Point2D.Double(x, y);
				transform.transform(scalePoint_nw, scalePoint_nw);

				Point2D.Double scalePoint_ne = new Point2D.Double(x + w, y);
				transform.transform(scalePoint_ne, scalePoint_ne);

				Point2D.Double scalePoint_sw = new Point2D.Double(x, y + h);
				transform.transform(scalePoint_sw, scalePoint_sw);

				Point2D.Double scalePoint_se = new Point2D.Double(x + w, y + h);
				transform.transform(scalePoint_se, scalePoint_se);

				Point2D.Double shearPoint_n = new Point2D.Double(x + w / 2.0, y);
				transform.transform(shearPoint_n, shearPoint_n);

				Point2D.Double shearPoint_w = new Point2D.Double(x, y + h / 2.0);
				transform.transform(shearPoint_w, shearPoint_w);

				Point2D.Double shearPoint_s = new Point2D.Double(x + w / 2.0, y + h);
				transform.transform(shearPoint_s, shearPoint_s);

				Point2D.Double shearPoint_e = new Point2D.Double(x + w, y + h / 2.0);
				transform.transform(shearPoint_e, shearPoint_e);

				int l = 16;
				g.setColor(Color.red);
				switch (transformType) {
					case ON_SCALE_NW:
						drawArrow(g, scalePoint_nw, l, 0);
						drawArrow(g, scalePoint_nw, -l, 0);
						drawArrow(g, scalePoint_nw, 0, l);
						drawArrow(g, scalePoint_nw, 0, -l);
						break;
					case ON_SCALE_NE:
						drawArrow(g, scalePoint_ne, l, 0);
						drawArrow(g, scalePoint_ne, -l, 0);
						drawArrow(g, scalePoint_ne, 0, l);
						drawArrow(g, scalePoint_ne, 0, -l);
						break;
					case ON_SCALE_SW:
						drawArrow(g, scalePoint_sw, l, 0);
						drawArrow(g, scalePoint_sw, -l, 0);
						drawArrow(g, scalePoint_sw, 0, l);
						drawArrow(g, scalePoint_sw, 0, -l);
						break;
					case ON_SCALE_SE:
						drawArrow(g, scalePoint_se, l, 0);
						drawArrow(g, scalePoint_se, -l, 0);
						drawArrow(g, scalePoint_se, 0, l);
						drawArrow(g, scalePoint_se, 0, -l);
						break;
					case ON_SHEAR_N:
						drawArrow(g, shearPoint_n, scalePoint_nw, scalePoint_ne, l);
						drawArrow(g, shearPoint_n, scalePoint_nw, scalePoint_ne, -l);
						break;
					case ON_SHEAR_W:
						drawArrow(g, shearPoint_w, scalePoint_nw, scalePoint_sw, l);
						drawArrow(g, shearPoint_w, scalePoint_nw, scalePoint_sw, -l);
						break;
					case ON_SHEAR_S:
						drawArrow(g, shearPoint_s, scalePoint_sw, scalePoint_se, l);
						drawArrow(g, shearPoint_s, scalePoint_sw, scalePoint_se, -l);
						break;
					case ON_SHEAR_E:
						drawArrow(g, shearPoint_e, scalePoint_ne, scalePoint_se, l);
						drawArrow(g, shearPoint_e, scalePoint_ne, scalePoint_se, -l);
						break;
					case ON_ROTATION:
						break;
				}

				g.setColor(Color.red);
				g.fillRect((int) rotatePoint.getX() - 2, (int) rotatePoint.getY() - 2, 5, 5);

				g.fillRect((int) scalePoint_nw.getX() - 2, (int) scalePoint_nw.getY() - 2, 5, 5);
				g.fillRect((int) scalePoint_ne.getX() - 2, (int) scalePoint_ne.getY() - 2, 5, 5);
				g.fillRect((int) scalePoint_sw.getX() - 2, (int) scalePoint_sw.getY() - 2, 5, 5);
				g.fillRect((int) scalePoint_se.getX() - 2, (int) scalePoint_se.getY() - 2, 5, 5);

				g.fillRect((int) shearPoint_n.getX() - 2, (int) shearPoint_n.getY() - 2, 5, 5);
				g.fillRect((int) shearPoint_w.getX() - 2, (int) shearPoint_w.getY() - 2, 5, 5);
				g.fillRect((int) shearPoint_s.getX() - 2, (int) shearPoint_s.getY() - 2, 5, 5);
				g.fillRect((int) shearPoint_e.getX() - 2, (int) shearPoint_e.getY() - 2, 5, 5);
			}
		}

		private int[] xarray = new int[3];

		private int[] yarray = new int[3];

		private void drawArrow(Graphics2D g, Point2D p, Point2D p1, Point2D p2, int length) {
			double dx = p1.getX() - p2.getX();
			double dy = p1.getY() - p2.getY();
			double r = Math.sqrt(dx * dx + dy * dy);
			double koef = length / r;
			dx *= koef;
			dy *= koef;
			drawArrow(g, p, dx, dy);
		}

		private void drawArrow(Graphics2D g, Point2D p, double dx, double dy) {
			int x1 = (int) p.getX();
			int y1 = (int) p.getY();
			int x2 = (int) (p.getX() + dx);
			int y2 = (int) (p.getY() + dy);
			double r = Math.sqrt(dx * dx + dy * dy);
			if (r != 0) {
				double koef = 8.0 / r;
				double dx_ = dx * koef;
				double dy_ = dy * koef;
				double x_ = x2 - dx_;
				double y_ = y2 - dy_;
				double dxp = -dy_ / 2.0;
				double dyp = dx_ / 2.0;
				xarray[0] = x2;
				xarray[1] = (int) (x_ + dxp);
				xarray[2] = (int) (x_ - dxp);
				yarray[0] = y2;
				yarray[1] = (int) (y_ + dyp);
				yarray[2] = (int) (y_ - dyp);
				g.fillPolygon(xarray, yarray, 3);
			}
		}
	}

	public void toIdentity() {
		setTransform(new AffineTransform());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		String cmd = e.getActionCommand();
		if ("update".equals(cmd)) {
			examplePanel.repaint();
		} else if ("to-identity".equals(cmd)) {
			toIdentity();
		}
	}

	public static void main(String[] args) {
		Util.installDefaultFont(Fonts.getFont("Dialog", 0, 11));
		TransformChooser chooser = new TransformChooser(new JVGResources());
		chooser.setVisible(true);
	}
}
