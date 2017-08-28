package javax.swing.domeditor;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jdom2.Element;

public class DomTreeRenderer extends DefaultTreeCellRenderer {
	private static ImageIcon imgNode = new ImageIcon(DomTreeRenderer.class.getResource("img/node.png"));

	private static ImageIcon imgComponent = new ImageIcon(DomTreeRenderer.class.getResource("img/component.png"));

	private static ImageIcon imgComponents = new ImageIcon(DomTreeRenderer.class.getResource("img/components.gif"));

	private static ImageIcon imgTransform = new ImageIcon(DomTreeRenderer.class.getResource("img/transform.gif"));

	private static ImageIcon imgPaint = new ImageIcon(DomTreeRenderer.class.getResource("img/paint.png"));

	private static ImageIcon imgDraw = new ImageIcon(DomTreeRenderer.class.getResource("img/draw.gif"));

	private static ImageIcon imgForm = new ImageIcon(DomTreeRenderer.class.getResource("img/form.png"));

	private static ImageIcon imgRoot = new ImageIcon(DomTreeRenderer.class.getResource("img/root.png"));

	private static ImageIcon imgText = new ImageIcon(DomTreeRenderer.class.getResource("img/text.gif"));

	public static Icon getIcon(Element e, boolean expanded) {
		String name = e.getName();
		if ("components".equals(name)) {
			return imgComponents;
		} else if ("component".equals(name)) {
			return imgComponent;
		} else if ("transform".equals(name)) {
			return imgTransform;
		} else if ("paint".equals(name)) {
			return imgPaint;
		} else if ("draw".equals(name)) {
			return imgDraw;
		} else if ("form".equals(name)) {
			return imgForm;
		} else if ("jvg".equals(name)) {
			return imgRoot;
		}
		return imgNode;
	}

	public static String getText(Element e, boolean path) {
		return e.getName();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Element e = (Element) value;

		JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		lbl.setIcon(getIcon(e, expanded));
		lbl.setText(getText(e, false));

		return lbl;
	}
}
