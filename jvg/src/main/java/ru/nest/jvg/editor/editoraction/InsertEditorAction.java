package ru.nest.jvg.editor.editoraction;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;

import javax.swing.ImageIcon;
import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGCopyContext;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;
import ru.nest.jvg.shape.JVGComplexShape;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.CompoundUndoRedo;

public class InsertEditorAction extends EditorAction {
	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(PencilEditorAction.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_insert.png")).getImage(), new Point(15, 15), "pencil");

	private String data;

	public InsertEditorAction(String data) {
		super("insert");
		this.data = data;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane instanceof JVGEditPane) {
			JVGEditPane editorPane = (JVGEditPane) pane;
			setEditorPane(editorPane);

			editorPane.setEditor(this);
			consumeMouseEvents(true);
		}
	}

	@Override
	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		JVGEditPane editorPane = getEditorPane();
		insert(editorPane, new JVGCopyContext(data, -1, -1), adjustedX, adjustedY);
		e.consume();
		editorPane.setEditor(null);
	}

	public static void insert(JVGPane pane, JVGCopyContext ctx, double x, double y) {
		try {
			JVGComponent[] childs = null;
			JVGComponent boundsComponent = null;
			if (ctx.getData() != null) {
				JVGEditorKit editorKit = pane.getEditorKit();
				JVGParser parser = new JVGParser(editorKit.getFactory());
				JVGRoot root = parser.parse(new ByteArrayInputStream(ctx.getData().getBytes()));
				childs = root.getChildren();
				boundsComponent = root;
			} else if (ctx.getURL() != null) {
				JVGComponent c = pane.getEditorKit().getFactory().createComponent(JVGComplexShape.class, ctx.getURL());
				childs = new JVGComponent[] { c };
				boundsComponent = c;
			}

			if (childs != null) {
				int childs_count = childs.length;

				// transform
				Rectangle2D bounds = boundsComponent.getRectangleBounds();

				double dx = bounds.getX() + bounds.getWidth() / 2.0 - x;
				double dy = bounds.getY() + bounds.getHeight() / 2.0 - y;

				JVGUtil.transform(childs, AffineTransform.getTranslateInstance(-dx, -dy));

				// add shape to root component
				CompoundUndoRedo edit = new CompoundUndoRedo("ungroup", pane);

				pane.getSelectionManager().clearSelection();
				pane.getRoot().add(childs);

				for (int i = 0; i < childs_count; i++) {
					JVGComponent child = childs[i];
					if (child instanceof JVGShape) {
						edit.add(new AddUndoRedo(pane, child, null, -1, pane.getRoot(), -1));
						child.setSelected(true, false);
					}
				}

				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}
		} catch (JVGParseException exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public boolean isCustomActionsManager() {
		return false;
	}
}
