package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

public class GUIUtils {
	/** the active colour for text */
	private static Color defaultActiveTextColour;

	/** the active colour for backgrounds */
	private static Color defaultActiveBackgroundColour;

	/** the inactive colour for backgrounds */
	private static Color defaultInactiveBackgroundColour;

	/** True if checked for windows yet. */
	private static boolean checkedWindows;

	/** True if running on Windows. */
	private static boolean isWindows;

	/** True if checked for mac yet. */
	private static boolean checkedMac;

	/** True if running on Mac. */
	private static boolean isMac;

	/**
	 * Convenience method for consistent border colour.
	 * 
	 * @return the system default border colour
	 */
	public static Color getDefaultBorderColour() {
		return UIManager.getColor("controlShadow");
	}

	/**
	 * Returns true if running on Mac.
	 */
	public static boolean isMac() {
		if (!checkedMac) {
			String osName = System.getProperty("os.name");
			if (osName != null && osName.indexOf("Mac") != -1) {
				isMac = true;
			}
			checkedMac = true;
		}
		return isMac;
	}

	/**
	 * Returns true if running on Windows.
	 */
	public static boolean isWindows() {
		if (!checkedWindows) {
			String osName = System.getProperty("os.name");
			if (osName != null && osName.indexOf("Windows") != -1) {
				isWindows = true;
			}
			checkedWindows = true;
		}
		return isWindows;
	}

	/**
	 * Returns whether the current applied look and feel is the MetalLookAndFeel
	 * 
	 * @return true | false
	 */
	public static boolean isMetalLookAndFeel() {
		return UIManager.getLookAndFeel() instanceof MetalLookAndFeel;
	}

	/**
	 * Returns whether the current applied look and feel is the MotifLookAndFeel
	 * 
	 * @return true | false
	 */
	public static boolean isMotifLookAndFeel() {
		return UIManager.getLookAndFeel() instanceof MotifLookAndFeel;
	}

	/**
	 * Returns whether the current applied look and feel is the WindowsLookAndFeel
	 * 
	 * @return true | false
	 */
	public static boolean isWindowsLookAndFeel() {
		return UIManager.getLookAndFeel() instanceof WindowsLookAndFeel;
	}

	/**
	 * Returns true if we're using the Ocean Theme under the MetalLookAndFeel.
	 */
	public static boolean usingOcean() {
		if (isMetalLookAndFeel()) {
			MetalLookAndFeel laf = (MetalLookAndFeel) UIManager.getLookAndFeel();
			return (MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme);
		}
		return false;
	}

	public static Color getDefaultInactiveBackgroundColour() {
		if (defaultInactiveBackgroundColour == null) {
			defaultInactiveBackgroundColour = UIManager.getColor("control");
		}
		return defaultInactiveBackgroundColour;
	}

	public static Color getInverse(Color colour) {
		int red = 255 - colour.getRed();
		int green = 255 - colour.getGreen();
		int blue = 255 - colour.getBlue();
		return new Color(red, green, blue);
	}

	public static Color getDefaultActiveBackgroundColour() {
		if (defaultActiveBackgroundColour == null) {
			if (!isWindowsLookAndFeel()) {
				Color color = UIManager.getColor("activeCaptionBorder");
				if (color == null) {
					color = UIManager.getColor("controlShadow");
				}
				defaultActiveBackgroundColour = getBrighter(color, 0.85);
			} else {
				defaultActiveBackgroundColour = UIManager.getColor("controlLtHighlight");
			}
		}
		return defaultActiveBackgroundColour;
	}

	public static Color getDefaultActiveTextColour() {
		if (defaultActiveTextColour == null) {
			if (!isWindowsLookAndFeel()) {
				defaultActiveTextColour = UIManager.getColor("activeCaptionText");
				if (defaultActiveTextColour == null) {
					// default to black text
					defaultActiveTextColour = Color.BLACK;
				}
			} else {
				defaultActiveTextColour = UIManager.getColor("controlText");
			}
		}
		return defaultActiveTextColour;
	}

	public static Color getDarker(Color color, double factor) {
		return new Color(Math.max((int) (color.getRed() * factor), 0), Math.max((int) (color.getGreen() * factor), 0), Math.max((int) (color.getBlue() * factor), 0));
	}

	public static Color getBrighter(Color color, double factor) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();

		int i = (int) (1.0 / (1.0 - factor));
		if (r == 0 && g == 0 && b == 0) {
			return new Color(i, i, i);
		}
		if (r > 0 && r < i)
			r = i;
		if (g > 0 && g < i)
			g = i;
		if (b > 0 && b < i)
			b = i;

		return new Color(Math.min((int) (r / factor), 255), Math.min((int) (g / factor), 255), Math.min((int) (b / factor), 255));
	}

	// Cached Access to Icons
	// ***********************************************************

	private static Icon checkBoxIcon;

	private static Icon checkBoxMenuItemIcon;

	private static Icon radioButtonMenuItemIcon;

	private static Icon menuArrowIcon;

	private static Icon expandedTreeIcon;

	private static Icon collapsedTreeIcon;

	/**
	 * Answers an <code>Icon</code> used for <code>JCheckBox</code>es.
	 */
	/*
	 * static Icon getCheckBoxIcon() { if (checkBoxIcon == null) { checkBoxIcon = new CheckBoxIcon(); } return checkBoxIcon; }
	 */

	/**
	 * Answers an <code>Icon</code> used for <code>JCheckButtonMenuItem</code>s.
	 */
	static Icon getCheckBoxMenuItemIcon() {
		if (checkBoxMenuItemIcon == null) {
			checkBoxMenuItemIcon = new CheckBoxMenuItemIcon();
		}
		return checkBoxMenuItemIcon;
	}

	/**
	 * Answers an <code>Icon</code> used for <code>JRadioButtonMenuItem</code>s.
	 */
	/*
	 * static Icon getRadioButtonMenuItemIcon() { if (radioButtonMenuItemIcon == null) { radioButtonMenuItemIcon = new RadioButtonMenuItemIcon();
	 * } return radioButtonMenuItemIcon; }
	 */

	/**
	 * Answers an <code>Icon</code> used for arrows in <code>JMenu</code>s.
	 */
	/*
	 * static Icon getMenuArrowIcon() { if (menuArrowIcon == null) { menuArrowIcon = new MenuArrowIcon(); } return menuArrowIcon; }
	 */

	/**
	 * Answers an <code>Icon</code> used in <code>JTree</code>s.
	 */
	public static Icon getExpandedTreeIcon() {
		if (expandedTreeIcon == null) {
			expandedTreeIcon = new ExpandedTreeIcon();
		}
		return expandedTreeIcon;
	}

	/**
	 * Answers an <code>Icon</code> used in <code>JTree</code>s.
	 */
	public static Icon getCollapsedTreeIcon() {
		if (collapsedTreeIcon == null) {
			collapsedTreeIcon = new CollapsedTreeIcon();
		}
		return collapsedTreeIcon;
	}

	private static class CheckBoxMenuItemIcon implements Icon, UIResource, Serializable {
		private static final long serialVersionUID = 1L;
		private static final int SIZE = 13;

		@Override
		public int getIconWidth() {
			return SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			JMenuItem b = (JMenuItem) c;
			if (b.isSelected()) {
				drawCheck(g, x, y + 1);
			}
		}
	}

	// Helper method utilized by the CheckBoxIcon and the CheckBoxMenuItemIcon.
	private static void drawCheck(Graphics g, int x, int y) {
		g.translate(x, y);
		g.drawLine(3, 5, 3, 5);
		g.fillRect(3, 6, 2, 2);
		g.drawLine(4, 8, 9, 3);
		g.drawLine(5, 8, 9, 4);
		g.drawLine(5, 9, 9, 5);
		g.translate(-x, -y);
	}

	/**
	 * The minus sign button icon used in trees
	 */
	private static class ExpandedTreeIcon implements Icon, Serializable {
		private static final long serialVersionUID = 1L;

		protected static final int SIZE = 9;

		protected static final int HALF_SIZE = 4;

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color backgroundColor = c.getBackground();

			g.setColor(backgroundColor != null ? backgroundColor : Color.white);
			g.fillRect(x, y, SIZE - 1, SIZE - 1);
			g.setColor(Color.gray);
			g.drawRect(x, y, SIZE - 1, SIZE - 1);
			g.setColor(Color.black);
			g.drawLine(x + 2, y + HALF_SIZE, x + (SIZE - 3), y + HALF_SIZE);
		}

		@Override
		public int getIconWidth() {
			return SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}
	}

	/**
	 * The plus sign button icon used in trees.
	 */
	private static class CollapsedTreeIcon extends ExpandedTreeIcon {
		private static final long serialVersionUID = 1L;

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			g.drawLine(x + HALF_SIZE, y + 2, x + HALF_SIZE, y + (SIZE - 3));
		}
	}

	private GUIUtils() {
	}

	public static Rectangle getVisibleBoundsOnScreen(JComponent component) {
		Rectangle visibleRect = component.getVisibleRect();
		Point onScreen = visibleRect.getLocation();
		SwingUtilities.convertPointToScreen(onScreen, component);
		visibleRect.setLocation(onScreen);
		return visibleRect;
	}

	public static Vector<String> getSystemFonts() {
		GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font[] tempFonts = gEnv.getAllFonts();

		char dot = '.';
		int dotIndex = 0;

		char[] fontNameChars = null;
		String fontName = null;
		Vector<String> fontNames = new Vector<String>();

		for (int i = 0; i < tempFonts.length; i++) {

			fontName = tempFonts[i].getFontName();
			dotIndex = fontName.indexOf(dot);

			if (dotIndex == -1) {
				fontNames.add(fontName);
			} else {
				fontNameChars = fontName.substring(0, dotIndex).toCharArray();
				fontNameChars[0] = Character.toUpperCase(fontNameChars[0]);

				fontName = new String(fontNameChars);

				if (!fontNames.contains(fontName)) {
					fontNames.add(fontName);
				}

			}

		}

		Collections.sort(fontNames);
		return fontNames;
	}

	public static void fillDottedRect(Graphics g, Color color, int x, int y, int w, int h) {
		Shape clip = g.getClip();
		g.setClip(x, y, w, h);
		if (w < h) {
			BufferedImage img = new BufferedImage(w, 1, BufferedImage.TYPE_INT_ARGB);
			Graphics ig = img.getGraphics();
			ig.setColor(color);
			for (int i = 0; i < w; i += 2) {
				ig.drawLine(i, 0, i, 0);
			}

			for (int i = 0; i < h; i++) {
				g.drawImage(img, x + (i % 2), y + i, null);
			}
		} else {
			BufferedImage img = new BufferedImage(1, h, BufferedImage.TYPE_INT_ARGB);
			Graphics ig = img.getGraphics();
			ig.setColor(color);
			for (int i = 0; i < h; i += 2) {
				ig.drawLine(0, i, 0, i);
			}

			for (int i = 0; i < w; i++) {
				g.drawImage(img, x + i, y + (i % 2), null);
			}
		}
		g.setClip(clip);
	}
}
