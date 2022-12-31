package ru.nest.jvg;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.im.InputContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.UndoableEditEvent;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;

import ru.nest.jvg.action.AlignmentAction;
import ru.nest.jvg.editor.clipboard.JVGClipboardContext;
import ru.nest.jvg.editor.clipboard.JVGClipboardPanel;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;
import ru.nest.jvg.shape.JVGComplexShape;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.RemoveUndoRedo;

public class JVGTransferHandler extends TransferHandler implements UIResource {
	private transient JVGComponent[] exportComponents;

	protected DataFlavor getImportFlavor(DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {
			if (JVGCopyContext.class.equals(flavors[i].getRepresentationClass())) {
				return flavors[i];
			}
		}
		return null;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent comp) {
		if (comp instanceof JVGPane) {
			JVGPane exportPane = (JVGPane) comp;
			exportComponents = exportPane.getSelectionManager().getSelection();
			Arrays.sort(exportComponents, new ComponentOrderComparator());

			if (exportComponents == null || exportComponents.length == 0) {
				exportComponents = exportPane.getRoot().getChildren();
			}

			if (exportComponents != null && exportComponents.length > 0) {
				return new JVGTransferable(exportComponents);
			}
		} else if (comp instanceof JVGClipboardPanel) {
			JVGClipboardPanel p = (JVGClipboardPanel) comp;
			JVGClipboardContext ctx = (JVGClipboardContext) p.getSelectedValue();
			return new JVGTransferable(ctx.getData(), ctx.getWidth(), ctx.getHeight());
		}
		return null;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if (action == MOVE && exportComponents != null) {
			JVGPane pane = (JVGPane) source;
			removeComponents(pane, exportComponents);
			source.repaint();
			exportComponents = null;
		}
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		boolean imported = false;
		if (comp instanceof JVGPane) {
			JVGPane pane = (JVGPane) comp;
			DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors());
			if (importFlavor != null) {
				try {
					InputContext ic = pane.getInputContext();
					if (ic != null) {
						ic.endComposition();
					}

					Object transferObject = t.getTransferData(importFlavor);
					if (!(transferObject instanceof JVGCopyContext)) {
						throw new IllegalArgumentException("getTransferData() returned null");
					}

					handleImport(pane, (JVGCopyContext) transferObject);
					imported = true;
				} catch (UnsupportedFlavorException exc) {
				} catch (IOException exc) {
				}
			}
		}
		return imported;
	}

	protected void handleImport(JVGPane pane, JVGCopyContext ctx) {
		JVGParser parser = new JVGParser(pane.getEditorKit().getFactory());
		try {
			JVGComponent[] childs = null;
			if (ctx.getData() != null) {
				JVGRoot root = parser.parse(ctx.getData());
				childs = root.getChildren();
			} else if (ctx.getComplexURL() != null) {
				JVGComponent c = pane.getEditorKit().getFactory().createComponent(JVGComplexShape.class, ctx.getComplexURL());
				childs = new JVGComponent[] { c };
			}

			if (childs != null) {
				pane.getRoot().add(childs);

				JVGShape[] shapes = JVGUtil.getComponents(JVGShape.class, childs);

				JVGSelectionModel selectionManager = pane.getSelectionManager();
				if (selectionManager != null) {
					selectionManager.clearSelection();
					selectionManager.addSelection(shapes);
				}

				AlignmentAction.alignCurrentArea(pane, shapes, AlignmentAction.CENTER);

				CompoundUndoRedo edit = new CompoundUndoRedo("paste", pane);
				for (int i = 0; i < childs.length; i++) {
					edit.add(new AddUndoRedo(pane, childs[i], null, -1, pane.getRoot(), -1));
				}
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}
		} catch (JVGParseException exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		return getImportFlavor(flavors) != null;
	}

	private transient Comparator<JVGComponent> orderComparator = new ComponentOrderComparator();

	void removeComponents(JVGPane pane, JVGComponent[] components) {
		if (components != null && components.length > 0) {
			Arrays.sort(components, orderComparator);

			CompoundUndoRedo edit = new CompoundUndoRedo("remove-selected", pane);
			for (int i = 0; i < components.length; i++) {
				JVGComponent c = components[i];
				JVGContainer p = c.getParent();
				if (p != null) {
					edit.add(new RemoveUndoRedo(pane, c, p, p.getChildIndex(c)));
					p.remove(c);
				}
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}
		}
	}
}
