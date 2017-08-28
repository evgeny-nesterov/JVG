package javax.swing.multidivider;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class MultiDivider extends JLabel implements MultiDividerListener {
	private static final long serialVersionUID = 1L;
	private int resizeWidth = 3;

	public MultiDivider(MultiDividerModel model) {
		setModel(model);
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				updateKoef();
				positionsChanged();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateKoef();
				positionsChanged();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				updateKoef();
				positionsChanged();
			}
		});
	}

	private int dividerIndex = -1, mx;

	public int getDividerIndex() {
		return dividerIndex;
	}

	private void checkDivider(int mx) {
		synchronized (x) {
			if (x.length > 1) {
				for (int i = 1; i < x.length; i++) {
					if (mx >= x[i] - resizeWidth && mx <= x[i] + resizeWidth) {
						dividerIndex = i;
						this.mx = mx;
						return;
					}
				}
			}
		}

		dividerIndex = -1;
	}

	private int getIndex(int mx) {
		synchronized (x) {
			if (x.length > 0) {
				for (int i = 0; i < x.length - 1; i++) {
					if (mx > x[i] && mx < x[i + 1]) {
						return i;
					}
				}

				if (mx > x[x.length - 1]) {
					return x.length - 1;
				}
			}
		}

		return -1;
	}

	@Override
	public void processMouseMotionEvent(MouseEvent e) {
		super.processMouseMotionEvent(e);

		if (e.getID() == MouseEvent.MOUSE_MOVED) {
			int oldIndex = dividerIndex;
			checkDivider(e.getX());
			if (dividerIndex != oldIndex) {
				model.fireDividerIndexChanged(dividerIndex);
			}
		} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			if (dividerIndex >= 0 && mx != e.getX() && end > start) {
				double delta = (e.getX() - mx) * koef;

				if (delta > 0 && model.getLength(dividerIndex) < delta) {
					delta = model.getLength(dividerIndex);
				} else if (delta < 0 && model.getLength(dividerIndex - 1) + delta < 0) {
					delta = -model.getLength(dividerIndex - 1);
				}

				if (delta != 0) {
					model.setLength(model.getLength(dividerIndex - 1) + delta, dividerIndex - 1);
					model.setLength(model.getLength(dividerIndex) - delta, dividerIndex);

					mx = e.getX();

					model.firePositionsChanged();
				}
			}
		}
	}

	private double start, end, koef;

	private void updateLimits() {
		start = model.getStartValue();
		end = model.getStartValue();
		for (int i = 0; i < model.getCount(); i++) {
			end += model.getLength(i);
		}
		updateKoef();
	}

	private void updateKoef() {
		koef = (end - start) / getWidth();
	}

	private int[] x = new int[0];

	private void updateParts() {
		if (koef > 0) {
			synchronized (x) {
				if (x.length != model.getCount()) {
					x = new int[model.getCount()];
				}

				double pos = model.getStartValue();
				for (int i = 0; i < model.getCount(); i++) {
					x[i] = (int) (pos / koef);
					pos += model.getLength(i);
				}
			}
		}
	}

	private MultiDividerModel model = null;

	public MultiDividerModel getModel() {
		return model;
	}

	public void setModel(MultiDividerModel model) {
		if (this.model != null) {
			this.model.removeListener(this);
		}

		if (model != null) {
			model.addListener(this);
		}

		this.model = model;
		updateLimits();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		synchronized (x) {
			for (int i = 1; i < x.length; i++) {
				g.drawLine(x[i], 0, x[i], getHeight());
			}
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 500, 100);
		f.getContentPane().setLayout(new BorderLayout());

		class P {
			public P(double len) {
				this.len = len;
			}

			double len;

			public double getLength() {
				return len;
			}

			public void setLength(double len) {
				this.len = len;
			}
		};

		class M extends AbstractMultiDividerModel {
			private ArrayList<P> p;

			public ArrayList<P> getP() {
				return p;
			}

			public M(ArrayList<P> p) {
				this.p = p;
			}

			@Override
			public double getStartValue() {
				return 0;
			}

			@Override
			public int getCount() {
				return p.size();
			}

			@Override
			public double getLength(int index) {
				return p.get(index).getLength();
			}

			@Override
			public void setLength(double value, int index) {
				p.get(index).setLength(value);
			}
		};

		ArrayList<P> p = new ArrayList<P>();
		p.add(new P(10));
		p.add(new P(5));
		p.add(new P(7));
		p.add(new P(15));
		M model = new M(p);
		MultiDivider d = new MultiDivider(model);
		d.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					MultiDivider d = (MultiDivider) e.getSource();
					Integer index = Integer.valueOf(d.getIndex(e.getX()));

					JPopupMenu popup = new JPopupMenu();

					JMenuItem menuBreak = new JMenuItem("Разбить");
					menuBreak.putClientProperty("md", d);
					menuBreak.putClientProperty("index", index);
					menuBreak.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							JMenuItem menu = (JMenuItem) e.getSource();
							MultiDivider d = (MultiDivider) menu.getClientProperty("md");
							int index = ((Integer) menu.getClientProperty("index")).intValue();

							M model = (M) d.getModel();
							P P = model.getP().get(index);
							double len = P.getLength() / 2.0;
							P p = new P(len);
							P.setLength(len);

							model.getP().add(index, p);
							model.fireStructureChanged();
						}
					});
					popup.add(menuBreak);

					boolean isLast = false;
					if (index.intValue() > 0) {
						JMenuItem menuJoinLeft = new JMenuItem("Объединить слева");
						menuJoinLeft.putClientProperty("md", d);
						menuJoinLeft.putClientProperty("index", index);
						menuJoinLeft.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								JMenuItem menu = (JMenuItem) e.getSource();
								MultiDivider d = (MultiDivider) menu.getClientProperty("md");
								int index = ((Integer) menu.getClientProperty("index")).intValue();

								M model = (M) d.getModel();
								P Pleft = model.getP().get(index - 1);
								P Pcur = model.getP().get(index);
								Pcur.setLength(Pcur.getLength() + Pleft.getLength());

								model.getP().remove(Pleft);
								model.fireStructureChanged();
							}
						});
						popup.add(menuJoinLeft);
					} else {
						isLast = true;
					}

					if (index.intValue() < d.getModel().getCount() - 1) {
						JMenuItem menuJoinRigth = new JMenuItem("Объединить справа");
						menuJoinRigth.putClientProperty("md", d);
						menuJoinRigth.putClientProperty("index", index);
						menuJoinRigth.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								JMenuItem menu = (JMenuItem) e.getSource();
								MultiDivider d = (MultiDivider) menu.getClientProperty("md");
								int index = ((Integer) menu.getClientProperty("index")).intValue();

								M model = (M) d.getModel();
								P Prigth = model.getP().get(index + 1);
								P Pcur = model.getP().get(index);
								Pcur.setLength(Pcur.getLength() + Prigth.getLength());

								model.getP().remove(Prigth);
								model.fireStructureChanged();
							}
						});
						popup.add(menuJoinRigth);
					} else {
						isLast = true;
					}

					if (isLast && d.getModel().getCount() > 1) {
						JMenuItem menuDelete = new JMenuItem("Удалить");
						menuDelete.putClientProperty("md", d);
						menuDelete.putClientProperty("index", index);
						menuDelete.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								JMenuItem menu = (JMenuItem) e.getSource();
								MultiDivider d = (MultiDivider) menu.getClientProperty("md");
								int index = ((Integer) menu.getClientProperty("index")).intValue();

								M model = (M) d.getModel();
								model.getP().remove(index);
								model.fireStructureChanged();
							}
						});
						popup.add(menuDelete);
					}

					popup.show(d, e.getX(), e.getY());
				}
			}
		});

		f.getContentPane().add(d, BorderLayout.CENTER);
		f.setVisible(true);
	}

	// --- implement MultiDividerListener interface ---
	@Override
	public void structureChanged() {
		updateLimits();
		updateParts();
		repaint();
	}

	@Override
	public void positionsChanged() {
		updateParts();
		repaint();
	}

	@Override
	public void dividerIndexChanged(int index) {
		if (index >= 0) {
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		} else {
			setCursor(Cursor.getDefaultCursor());
		}
	}
}
