package ru.nest.jvg.editor;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.WComboBox;

import ru.nest.jvg.action.BasicStrokeAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;

public class StrokeEndCapChooser extends WComboBox<Icon> {
	private static Icon[] strokeCapIcons = { Images.getImage("stroke-cap-butt.png"), Images.getImage("stroke-cap-square.png"), Images.getImage("stroke-cap-round.png") };

	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private String[] descr = { "chooser.outline.endcap.butt", "chooser.outline.endcap.square", "chooser.outline.endcap.round" };

	private Action[] actions = new Action[3];

	public StrokeEndCapChooser() {
		super(strokeCapIcons);

		actions[0] = new BasicStrokeAction("outline-end-cap-butt", new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL), BasicStrokeAction.TYPE_END_CAP);
		actions[1] = new BasicStrokeAction("outline-end-cap-square", new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL), BasicStrokeAction.TYPE_END_CAP);
		actions[2] = new BasicStrokeAction("outline-end-cap-round", new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL), BasicStrokeAction.TYPE_END_CAP);

		setToolTipText(lm.getValue("chooser.outline.endcap.tooltip", "Stroke cap"));
		setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel lbl = (JLabel) super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
				if (index != -1) {
					lbl.setToolTipText(lm.getValue(descr[index]));
				} else {
					lbl.setToolTipText("");
				}
				lbl.setIcon((Icon) value);
				return lbl;
			}
		});
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (getSelectedIndex() != -1) {
					actions[getSelectedIndex()].actionPerformed(e);
				}
			}
		});
	}
}
