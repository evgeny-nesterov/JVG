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

public class StrokeLineJoinChooser extends WComboBox<Icon> {
	private static Icon[] strokeJoinIcons = { Images.getImage("stroke-join-bevel.png"), Images.getImage("stroke-join-miter.png"), Images.getImage("stroke-join-round.png") };

	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private String[] descr = { "chooser.outline.linejoin.bevel", "chooser.outline.linejoin.miter", "chooser.outline.linejoin.round" };

	private Action[] actions = new Action[3];

	public StrokeLineJoinChooser() {
		super(strokeJoinIcons);

		actions[0] = new BasicStrokeAction("outline-line-join-bevel", new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL), BasicStrokeAction.TYPE_LINE_JOIN);
		actions[1] = new BasicStrokeAction("outline-line-join-miter", new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER), BasicStrokeAction.TYPE_LINE_JOIN);
		actions[2] = new BasicStrokeAction("outline-line-join-round", new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND), BasicStrokeAction.TYPE_LINE_JOIN);

		setToolTipText(lm.getValue("chooser.outline.linejoin.tooltip", "Stroke join"));
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
