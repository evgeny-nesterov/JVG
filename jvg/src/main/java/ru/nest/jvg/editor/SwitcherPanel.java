package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ru.nest.fonts.Fonts;

public class SwitcherPanel extends JList {
	public SwitcherPanel() {
		setModel(model);
		setOpaque(false);
		setCellRenderer(new SwitcherListRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int index = getSelectedIndex();
					if (index >= 0) {
						JVGPaneInternalFrame frame = list.get(index);
						frame.toFront();
					}
				}
			}
		});
	}

	private List<JVGPaneInternalFrame> list = new ArrayList<JVGPaneInternalFrame>();

	private SwitcherListModel model = new SwitcherListModel();

	class SwitcherListModel extends AbstractListModel {
		@Override
		public int getSize() {
			return list.size();
		}

		@Override
		public Object getElementAt(int i) {
			return list.get(i).getTitle();
		}

		public void add(JVGPaneInternalFrame frame) {
			list.add(frame);
			fireIntervalAdded(SwitcherPanel.this, list.size(), list.size());
		}

		public void remove(JVGPaneInternalFrame frame) {
			int index = list.indexOf(frame);
			if (index >= 0) {
				list.remove(index);
				fireIntervalRemoved(SwitcherPanel.this, index, index);
			}
		}

		public void update(JVGPaneInternalFrame frame) {
			int index = list.indexOf(frame);
			if (index >= 0) {
				fireContentsChanged(SwitcherPanel.this, index, index);
			}
		}
	};

	class SwitcherListRenderer extends JLabel implements ListCellRenderer {
		public SwitcherListRenderer() {
			setOpaque(false);
			setFont(Fonts.getFont("Dialog", Font.BOLD, 10));
		}

		private boolean isSelected;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			this.isSelected = isSelected;
			setText(value != null ? value.toString() : "");
			return this;
		}

		private Color selectedColor = new Color(180, 230, 180);

		private Color selectedBorderColor = new Color(150, 200, 150);

		@Override
		public void paint(Graphics g) {
			if (isSelected) {
				g.setColor(selectedColor);
				g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

				g.setColor(selectedBorderColor);
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			}

			super.paint(g);
		}
	}

	public void addSchema(JVGPaneInternalFrame frame) {
		model.add(frame);
		selectSchema(frame);
	}

	public void removeSchema(JVGPaneInternalFrame frame) {
		model.remove(frame);
	}

	public void updateSchema(JVGPaneInternalFrame frame) {
		model.update(frame);
	}

	public void selectSchema(JVGPaneInternalFrame frame) {
		int index = list.indexOf(frame);
		if (index >= 0) {
			setSelectedIndex(index);
			ensureIndexIsVisible(index);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		Util.paintFormBackground(g, getWidth(), getHeight());
		super.paintComponent(g);
	}
}
