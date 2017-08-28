package ru.nest.toi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataListener;

import ru.nest.toi.objects.TOIArrowPathElement;

public class ArrowDirectionChooseButton extends JComboBox<Integer> {
	private static final long serialVersionUID = -8368367020792916109L;

	public ArrowDirectionChooseButton() {
		setModel(new ComboBoxModel<Integer>() {
			int selected;

			@Override
			public void removeListDataListener(ListDataListener l) {
			}

			@Override
			public int getSize() {
				return 4;
			}

			@Override
			public Integer getElementAt(int index) {
				return index;
			}

			@Override
			public void addListDataListener(ListDataListener l) {
			}

			@Override
			public void setSelectedItem(Object selected) {
				this.selected = (Integer) selected;
			}

			@Override
			public Object getSelectedItem() {
				return selected;
			}
		});
		setRenderer(new ListCellRenderer<Integer>() {
			DirectionLabel l = new DirectionLabel();

			@Override
			public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index, boolean isSelected, boolean cellHasFocus) {
				l.setDirection(value);
				l.isSelected = isSelected;
				l.cellHasFocus = cellHasFocus;
				return l;
			}
		});

		setPreferredSize(new Dimension(70, 18));
		setMinimumSize(new Dimension(50, 18));
		setMaximumSize(new Dimension(50, 18));
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					showPopup();
				}
			}
		});
		setBorder(new LineBorder(Color.lightGray));
		setToolTipText("Выбор направления стрелок");
		setSelectedItem(TOIArrowPathElement.DIRECTION_DIRECT);
	}

	public int getDirection() {
		return (Integer) getSelectedItem();
	}

	public void setDirection(int direction) {
		setDirection(direction, true);
	}

	public void setDirection(int direction, boolean event) {
		if (direction != getDirection()) {
			ActionListener[] ls = getActionListeners();
			if (!event) {
				for (ActionListener l : ls) {
					removeActionListener(l);
				}
			}
			setSelectedItem(direction);
			if (!event) {
				for (ActionListener l : ls) {
					addActionListener(l);
				}
			}
		}
	}

	class DirectionLabel extends JLabel {
		private static final long serialVersionUID = 5120064660351456243L;

		int direction;

		boolean isSelected;

		boolean cellHasFocus;

		public void setDirection(int direction) {
			this.direction = direction;
		}

		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			if (cellHasFocus || isSelected) {
				g.setColor(new Color(200, 200, 255));
				g.fillRect(0, 0, getWidth(), getHeight());
			}

			int x1 = 5;
			int x2 = getWidth() - 10;
			int y = getHeight() / 2;

			Shape shape = new Rectangle(x1, y - 2, x2 - x1, 4);
			Area a1 = null;
			if (direction == TOIArrowPathElement.DIRECTION_BACK || direction == TOIArrowPathElement.DIRECTION_BOTH) {
				Shape arrow = new Polygon(new int[] { x1 - 4, x1, x1 }, new int[] { y, y - 4, y + 4 }, 3);
				a1 = new Area(arrow);
			}
			Area a2 = null;
			if (direction == TOIArrowPathElement.DIRECTION_DIRECT || direction == TOIArrowPathElement.DIRECTION_BOTH) {
				Shape arrow = new Polygon(new int[] { x2 + 4, x2, x2 }, new int[] { y, y - 4, y + 4 }, 3);
				a2 = new Area(arrow);
			}

			if (a1 != null || a2 != null) {
				Area a = new Area(shape);
				if (a1 != null) {
					a.add(a1);
				}
				if (a2 != null) {
					a.add(a2);
				}
				shape = a;
			}

			g2d.setColor(Color.lightGray);
			g2d.fill(shape);
			g2d.setColor(Color.black);
			g2d.draw(shape);
		}

		public Dimension getPreferredSize() {
			return new Dimension(50, 18);
		}
	}
}
