package ru.nest.jvg;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;

import javax.swing.plaf.UIResource;

import ru.nest.jvg.parser.DocumentFormat;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGParseException;

public class JVGTransferable implements Transferable, UIResource {
	public final static DataFlavor JVGFLAVOR = new DataFlavor(JVGCopyContext.class, "JVG");

	public final static DataFlavor[] flavors = new DataFlavor[] { JVGFLAVOR, DataFlavor.stringFlavor };

	public JVGTransferable(JVGComponent[] components) {
		if (components != null && components.length > 0) {
			components = JVGUtil.getRootsSaveOrder(components);

			JVGBuilder build = JVGBuilder.create(DocumentFormat.jvg);
			try {
				xml = build.build(components, "UTF8");
				Rectangle2D totalBounds = JVGUtil.getBounds(components);
				width = (int) totalBounds.getWidth();
				height = (int) totalBounds.getHeight();
			} catch (JVGParseException exc) {
				xml = "";
			}
		}
	}

	public JVGTransferable(String xml, int width, int height) {
		this.xml = xml;
		this.width = width;
		this.height = height;
	}

	public JVGTransferable(URL url) {
		this.url = url;
		this.width = -1;
		this.height = -1;
	}

	private URL url;

	private String xml;

	private int width, height;

	@Override
	public Object getTransferData(DataFlavor flavor) {
		if (JVGCopyContext.class.equals(flavor.getRepresentationClass())) {
			if (xml != null) {
				return new JVGCopyContext(xml, width, height);
			} else {
				return new JVGCopyContext(url);
			}
		} else if (DataFlavor.stringFlavor == flavor) {
			return xml != null ? xml : url;
		} else {
			//throw new UnsupportedFlavorException(flavor);
			return null;
		}
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] flavors = getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}
}
