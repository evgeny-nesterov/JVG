package javax.swing.outlook;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JOutlookPane extends JPanel {
	private static final long serialVersionUID = 1L;

	protected List<OutlookTab> tabs = new ArrayList<OutlookTab>();

	private OutlookTab selectedTab = null;

	protected ChangeEvent changeEvent = null;

	private int headerHeight = 18;

	private boolean pressed;

	private int my;

	private OutlookTab t1, t2;

	protected JPanel pnlGlass;

	public JOutlookPane() {
		pnlGlass = new JPanel();
		pnlGlass.setOpaque(false);
		pnlGlass.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					pressed = true;
					my = e.getY();
					if (getComponentCount() > 0) {
						setComponentZOrder(pnlGlass, 0);
						revalidate();
					}
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				if (getComponentCount() > 0) {
					setComponentZOrder(pnlGlass, getComponentCount() - 1);
					revalidate();
				}
				repaint();
			}
		});
		pnlGlass.addMouseMotionListener(new MouseAdapter() {
			private Cursor c1 = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);

			private Cursor c2 = Cursor.getDefaultCursor();

			@Override
			public void mouseMoved(MouseEvent e) {
				if (pressed) {
					return;
				}

				int index = -1;
				if (tabs.size() > 1) {
					for (int i = 0; i < tabs.size() - 1; i++) {
						OutlookTab t1 = tabs.get(i);
						OutlookTab t2 = tabs.get(i + 1);
						if (t1.getComponent().isShowing() && t2.getHeader().isShowing()) {
							if (e.getY() > t1.getComponent().getY() && e.getY() < t2.getHeader().getY()) {
								index = i;
								break;
							}
						}
					}
				}

				if (index != -1) {
					t1 = tabs.get(index);
					t2 = tabs.get(index + 1);
					if (!t1.getComponent().isVisible()) {
						for (int i = index; i >= 0; i--) {
							t1 = tabs.get(i);
							if (t1.getComponent().isVisible()) {
								break;
							}
						}
					}
					if (!t2.getComponent().isVisible()) {
						for (int i = index + 2; i < tabs.size(); i++) {
							t2 = tabs.get(i);
							if (t2.getComponent().isVisible()) {
								break;
							}
						}
					}
					if (!t1.getComponent().isVisible() || !t2.getComponent().isVisible()) {
						t1 = t2 = null;
					}
				} else {
					t1 = t2 = null;
				}

				Cursor cursor = index != -1 && index != tabs.size() ? c1 : c2;
				if (cursor != pnlGlass.getCursor()) {
					pnlGlass.setCursor(cursor);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!pressed) {
					pnlGlass.setCursor(Cursor.getDefaultCursor());
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (pressed && t1 != null && t2 != null) {
					int delta = e.getY() - my;
					if (delta != 0) {
						double koef = (t1.getWeight() + t2.getWeight()) / (t1.getComponent().getHeight() + t2.getComponent().getHeight());
						if (koef == 0) {
							return;
						}

						double deltaWeight = delta * koef;
						if (t1.getWeight() + deltaWeight < 0) {
							deltaWeight = -t1.getWeight();
						} else if (t2.getWeight() - deltaWeight < 0) {
							deltaWeight = t2.getWeight();
						}

						t1.addWeight(deltaWeight);
						t2.addWeight(-deltaWeight);
						revalidate();
						repaint();

						delta = (int) (deltaWeight / koef);
						my += delta;
					}
				}
			}
		});

		setLayout(new OutlookLayout());
		setBorder(new EmptyBorder(2, 2, 2, 2));
		add(pnlGlass);
	}

	public OutlookTab addTab(Component header, final JComponent comp) {
		final OutlookTab tab = new OutlookTab(header, comp);
		tabs.add(tab);

		comp.setCursor(Cursor.getDefaultCursor());

		header.setCursor(Cursor.getDefaultCursor());
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int maximizedIndex = indexOfMaximized();
				int currentIndex = indexOfComponent(comp);
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 1) {
						if (maximizedIndex != -1 && currentIndex != maximizedIndex) {
							setMaximized(currentIndex);
						}
						setSelectedTab(tab);
					} else if (e.getClickCount() == 2) {
						if (e.isControlDown()) {
							if (maximizedIndex == -1) {
								setMaximized(currentIndex);
							} else {
								clearMaximized();
							}
						} else {
							if (maximizedIndex == -1) {
								tab.setVisible(!tab.isVisible());
								revalidate();
								repaint();
							}
						}
					}
				}
			}
		});

		if (indexOfMaximized() != -1) {
			tab.setMaximized(false);
		}

		add(header);
		add(comp);
		add(pnlGlass);
		return tab;
	}

	public int indexOfComponent(Component comp) {
		for (int i = 0; i < tabs.size(); i++) {
			if (tabs.get(i).comp == comp) {
				return i;
			}
		}
		return -1;
	}

	public int indexOfHeader(Component header) {
		for (int i = 0; i < tabs.size(); i++) {
			if (tabs.get(i).header == header) {
				return i;
			}
		}
		return -1;
	}

	public Component getComponentAt(int index) {
		return tabs.get(index).comp;
	}

	public double getComponentWeightAt(int index) {
		return tabs.get(index).weight;
	}

	public Component getHeaderAt(int index) {
		return tabs.get(index).header;
	}

	public boolean isComponentMaximizedAt(int index) {
		return tabs.get(index).isMaximized;
	}

	public void setSelectedIndex(int index) {
		if (index >= 0 && index < tabs.size()) {
			setSelectedTab(tabs.get(index));
		}
	}

	public void setSelectedComponent(Component c) {
		for (int i = 0; i < tabs.size(); i++) {
			if (c == tabs.get(i).getComponent()) {
				setSelectedTab(tabs.get(i));
				break;
			}
		}
	}

	public int indexOfMaximized() {
		for (int i = 0; i < tabs.size(); i++) {
			if (tabs.get(i).isMaximized) {
				return i;
			}
		}
		return -1;
	}

	public void clearMaximized() {
		setMaximized(-1);
	}

	public void setMaximized(Component comp) {
		int index = indexOfComponent(comp);
		if (index != -1) {
			setMaximized(index);
		}
	}

	public void setMaximized(int index) {
		boolean maximized = index >= 0 && index < tabs.size();
		if (maximized) {
			for (int i = 0; i < tabs.size(); i++) {
				tabs.get(i).setMaximized(i == index);
			}
		} else {
			for (int i = 0; i < tabs.size(); i++) {
				tabs.get(i).isMaximized = false;
				tabs.get(i).setVisible(tabs.get(i).isVisible());
			}
		}
		revalidate();
		repaint();
	}

	public int getTabsCount() {
		return tabs.size();
	}

	public void setSelectedTab(OutlookTab selectedTab) {
		if (this.selectedTab != selectedTab) {
			this.selectedTab = selectedTab;
			Object[] listeners = listenerList.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ChangeListener.class) {
					if (changeEvent == null) {
						changeEvent = new ChangeEvent(this);
					}
					((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
				}
			}
			repaint();
		}
	}

	public OutlookTab getSelectedTab() {
		return selectedTab;
	}

	public int getSelectedIndex() {
		return selectedTab != null ? tabs.indexOf(selectedTab) : -1;
	}

	private Stroke selectionStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] { 1f, 1f }, 0f);

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (selectedTab != null) {
			int y = selectedTab.header.getY();
			int h = selectedTab.header.getHeight() - 2;
			if (selectedTab.comp.isVisible()) {
				h += selectedTab.comp.getHeight();
			}

			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.lightGray);
			g2d.drawRect(0, y, getWidth() - 1, h);
			g2d.setStroke(selectionStroke);
			g2d.setColor(Color.darkGray);
			g2d.drawRect(0, y, getWidth() - 1, h);
		}
	}

	public OutlookTab addTab(String title, Icon icon, JComponent comp) {
		JLabel l = new JLabel(title, icon, SwingConstants.LEFT);
		l.setBorder(BorderFactory.createRaisedBevelBorder());
		l.setOpaque(true);
		return addTab(l, comp);
	}

	public void removeTab(OutlookTab tab) {
		tabs.remove(tab);
		remove(tab.getHeader());
		remove(tab.getComponent());
	}

	@Override
	public void removeAll() {
		tabs.clear();
		super.removeAll();
		add(pnlGlass);
	}

	public OutlookTab getTabAt(int index) {
		return tabs.get(index);
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(ChangeListener.class);
	}

	public int getHeaderHeight() {
		return headerHeight;
	}

	public void setHeaderHeight(int headerHeight) {
		this.headerHeight = headerHeight;
	}

	public static void main(String[] args) {
		JOutlookPane p = new JOutlookPane();
		p.addTab("title 1", null, new JScrollPane(new JTextArea()));
		p.addTab("title 2", null, new JScrollPane(new JTextArea()));
		p.addTab("title 3", null, new JScrollPane(new JTextArea()));

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(p);
		f.setBounds(600, 200, 300, 600);
		f.setVisible(true);
	}
}
