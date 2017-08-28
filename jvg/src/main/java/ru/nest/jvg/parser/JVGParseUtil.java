package ru.nest.jvg.parser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jdom2.Element;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.strokes.ArrowStroke;
import ru.nest.strokes.TextStroke;

public class JVGParseUtil {
	public final static String ID_PREFIX = "$";

	final static float inchesPerCm = .3936f;

	final static float pixPerInch = Toolkit.getDefaultToolkit().getScreenResolution();

	private final static Map<Integer, String> CAP_TO_STRING = new HashMap<Integer, String>();

	private final static Map<String, Integer> CAP_FROM_STRING = new HashMap<String, Integer>();

	private final static Map<Integer, String> JOIN_TO_STRING = new HashMap<Integer, String>();

	private final static Map<String, Integer> JOIN_FROM_STRING = new HashMap<String, Integer>();
	static {
		// Caps
		CAP_TO_STRING.put(BasicStroke.CAP_BUTT, "butt");
		CAP_TO_STRING.put(BasicStroke.CAP_ROUND, "round");
		CAP_TO_STRING.put(BasicStroke.CAP_SQUARE, "square");

		CAP_FROM_STRING.put("butt", BasicStroke.CAP_BUTT);
		CAP_FROM_STRING.put("round", BasicStroke.CAP_ROUND);
		CAP_FROM_STRING.put("square", BasicStroke.CAP_SQUARE);

		// Joins
		JOIN_TO_STRING.put(BasicStroke.JOIN_BEVEL, "bevel");
		JOIN_TO_STRING.put(BasicStroke.JOIN_MITER, "miter");
		JOIN_TO_STRING.put(BasicStroke.JOIN_ROUND, "round");

		JOIN_FROM_STRING.put("bevel", BasicStroke.JOIN_BEVEL);
		JOIN_FROM_STRING.put("miter", BasicStroke.JOIN_MITER);
		JOIN_FROM_STRING.put("round", BasicStroke.JOIN_ROUND);
	}

	public static String[] getStringArray(String s) {
		return getStringArray(s, ";");
	}

	public static Map<String, String> getMap(String s) {
		String[] params = getStringArray(s, ";");
		if (params != null) {
			Map<String, String> map = new HashMap<String, String>();
			for (String p : params) {
				int index = p.indexOf("=");
				if (index != -1) {
					String parameter = p.substring(0, index).trim().toLowerCase();
					String value = p.substring(index + 1).trim();
					map.put(parameter, value);
				} else {
					map.put(p, "");
				}
			}
			return map;
		} else {
			return null;
		}
	}

	public static String[] getStringArray(String s, String delim) {
		return getStringArray(s, delim, false);
	}

	public static String[] getStringArray(String s, String delim, boolean returnDelims) {
		if (s != null) {
			StringTokenizer t = new StringTokenizer(s, delim, returnDelims);
			String[] array = new String[t.countTokens()];
			if (array.length > 0) {
				int i = 0;
				while (t.hasMoreTokens()) {
					array[i++] = t.nextToken();
				}
			}
			return array;
		} else {
			return null;
		}
	}

	public static String[] getParameters(String line) {
		String[] p = getStringArray(line, ";", true);
		int parametersCount = 1;
		for (int i = 0; i < p.length; i++) {
			if (";".equals(p[i])) {
				parametersCount++;
			}
		}

		String[] parameters = new String[parametersCount];
		int index = 0;
		for (int i = 0; i < p.length; i++) {
			if (";".equals(p[i])) {
				parameters[index++] = i > 0 ? (";".equals(p[i - 1]) ? null : p[i - 1]) : null;
			}
		}
		if (p.length > 0 && !";".equals(p[p.length - 1])) {
			parameters[index] = p[p.length - 1];
		}
		return parameters;
	}

	public static int[] getIntegerArray(String s) {
		return getIntegerArray(s, ";");
	}

	public static int[] getIntegerArray(String s, String delim) {
		if (s != null) {
			StringTokenizer t = new StringTokenizer(s, delim, false);
			int[] array = new int[t.countTokens()];
			if (array.length > 0) {
				int i = 0;
				while (t.hasMoreTokens()) {
					array[i++] = Integer.parseInt(t.nextToken());
				}
			}
			return array;
		} else {
			return null;
		}
	}

	public static double[] getDoubleArray(String s) {
		return getDoubleArray(s, ";");
	}

	public static double[] getDoubleArray(String s, String delim) {
		if (s != null) {
			StringTokenizer t = new StringTokenizer(s, delim, false);
			double[] array = new double[t.countTokens()];
			if (array.length > 0) {
				int i = 0;
				while (t.hasMoreTokens()) {
					array[i++] = Double.parseDouble(t.nextToken());
				}
			}
			return array;
		} else {
			return null;
		}
	}

	public static float[] getFloatArray(String s) {
		return getFloatArray(s, ";");
	}

	public static float[] getFloatArray(String s, String delim) {
		if (s != null) {
			StringTokenizer t = new StringTokenizer(s, delim, false);
			float[] array = new float[t.countTokens()];
			if (array.length > 0) {
				int i = 0;
				while (t.hasMoreTokens()) {
					Float v = getFloat(t.nextToken(), null);
					if (v == null) {
						return null;
					}
					array[i++] = v;
				}
				return array;
			}
		}
		return null;
	}

	public static Integer getInteger(String s, Integer defaultValue) {
		try {
			return Integer.parseInt(s);
		} catch (Exception exc) {
			return defaultValue;
		}
	}

	public static double getDouble(String s, double defaultValue) {
		try {
			return Double.parseDouble(s);
		} catch (Exception exc) {
			return defaultValue;
		}
	}

	public static Float getFloat(String s, Float defaultValue) {
		try {
			float koef = 1f;
			char[] c = s.toCharArray();
			for (int i = c.length - 1; i >= 0; i--) {
				if (i > 0) {
					char c2 = Character.toLowerCase(c[i]);
					if (c2 == '%') {
						koef = 1f / 100f;
						s = s.substring(0, i);
						break;
					} else if (i > 1) {
						char c1 = Character.toLowerCase(c[i - 1]);
						if (c1 == 'i' && c2 == 'n') {
							koef = pixPerInch;
							s = s.substring(0, i - 1);
							break;
						}

						if (c1 == 'c' && c2 == 'm') {
							koef = inchesPerCm * pixPerInch;
							s = s.substring(0, i - 1);
							break;
						}

						if (c1 == 'm' && c2 == 'm') {
							koef = inchesPerCm * pixPerInch * .1f;
							s = s.substring(0, i - 1);
							break;
						}

						if (c1 == 'p' && c2 == 't') {
							koef = (1f / 72f) * pixPerInch;
							s = s.substring(0, i - 1);
							break;
						}

						if (c1 == 'p' && c2 == 'c') {
							koef = (1f / 6f) * pixPerInch;
							s = s.substring(0, i - 1);
							break;
						}
					}
				}

				if (c[i] != ' ') {
					break;
				}
			}
			return koef * Float.parseFloat(s);
		} catch (Exception exc) {
			return defaultValue;
		}
	}

	public static Double getDouble(String s, Double defaultValue) {
		if (s != null) {
			try {
				double koef = 1.0;
				char[] c = s.toCharArray();
				for (int i = c.length - 1; i >= 0; i--) {
					if (i > 0) {
						char c2 = Character.toLowerCase(c[i]);
						if (c2 == '%') {
							koef = 1.0 / 100.0;
							s = s.substring(0, i);
							break;
						} else if (i > 1) {
							char c1 = Character.toLowerCase(c[i - 1]);
							if (c1 == 'i' && c2 == 'n') {
								koef = pixPerInch;
								s = s.substring(0, i - 1);
								break;
							}

							if (c1 == 'c' && c2 == 'm') {
								koef = inchesPerCm * pixPerInch;
								s = s.substring(0, i - 1);
								break;
							}

							if (c1 == 'm' && c2 == 'm') {
								koef = inchesPerCm * pixPerInch * .1f;
								s = s.substring(0, i - 1);
								break;
							}

							if (c1 == 'p' && c2 == 't') {
								koef = (1f / 72f) * pixPerInch;
								s = s.substring(0, i - 1);
								break;
							}

							if (c1 == 'p' && c2 == 'c') {
								koef = (1f / 6f) * pixPerInch;
								s = s.substring(0, i - 1);
								break;
							}

							if (c1 == 'p' && c2 == 'x') {
								s = s.substring(0, i - 1);
								break;
							}
						}
					}

					if (c[i] != ' ') {
						break;
					}
				}
				return koef * Double.parseDouble(s);
			} catch (Exception exc) {
			}
		}
		return defaultValue;
	}

	public static Long getLong(String s, Long defaultValue) {
		try {
			return Long.parseLong(s);
		} catch (Exception exc) {
			return defaultValue;
		}
	}

	public static String getValue(double value) {
		if ((int) value == value) {
			return Integer.toString((int) value);
		} else {
			return Double.toString(value);
		}
	}

	public static String getValue(float value) {
		if ((int) value == value) {
			return Integer.toString((int) value);
		} else {
			return Float.toString(value);
		}
	}

	public static String getValue(float[] array, String delim) {
		String value = "";
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				value += getValue(array[i]);
				if (i != array.length - 1) {
					value += delim;
				}
			}
		}
		return value;
	}

	private static int hexToInt(int c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else if (c >= 'a' && c <= 'f') {
			return 10 + c - 'a';
		} else if (c >= 'A' && c <= 'F') {
			return 10 + c - 'A';
		}
		throw new NumberFormatException("0x" + (char) c);
	}

	private static int hexToInt(char c1, char c2) {
		return (hexToInt(c1) << 4) + hexToInt(c2);
	}

	private final static Pattern rgbPattern = Pattern.compile("rgb\\((\\d+),(\\d+),(\\d+)\\)", Pattern.CASE_INSENSITIVE);

	public final static Color getColor(String value, Color defaultValue) {
		if (value != null && value.length() > 0) {
			try {
				if (value.charAt(0) == '#') {
					if (value.length() == 7) {
						int r = hexToInt(value.charAt(1), value.charAt(2));
						int g = hexToInt(value.charAt(3), value.charAt(4));
						int b = hexToInt(value.charAt(5), value.charAt(6));
						return new Color(r, g, b);
					} else if (value.length() == 9) {
						int r = hexToInt(value.charAt(1), value.charAt(2));
						int g = hexToInt(value.charAt(3), value.charAt(4));
						int b = hexToInt(value.charAt(5), value.charAt(6));
						int a = hexToInt(value.charAt(7), value.charAt(8));
						return new Color(r, g, b, a);
					} else if (value.length() == 4) {
						int r = hexToInt(value.charAt(1), value.charAt(1));
						int g = hexToInt(value.charAt(2), value.charAt(2));
						int b = hexToInt(value.charAt(3), value.charAt(3));
						return new Color(r, g, b);
					} else if (value.length() == 5) {
						int r = hexToInt(value.charAt(1), value.charAt(1));
						int g = hexToInt(value.charAt(2), value.charAt(2));
						int b = hexToInt(value.charAt(3), value.charAt(3));
						int a = hexToInt(value.charAt(4), value.charAt(4));
						return new Color(r, g, b, a);
					} else {
						return null;
					}
				}

				if (value.startsWith("rgb(") && value.endsWith("")) {
					value = value.substring(4, value.length() - 1);
					int[] rgb = getIntegerArray(value, " ,");
					if (rgb.length != 3 && rgb.length != 4) {
						return null;
					}
					for (int i = 0; i < rgb.length; i++) {
						if (rgb[i] < 0 || rgb[i] > 255) {
							return null;
						}
					}
					if (rgb.length == 4) {
						return new Color(rgb[0], rgb[1], rgb[2], rgb[3]);
					} else {
						return new Color(rgb[0], rgb[1], rgb[2]);
					}
				}

				Resource<Color> resource = ColorResource.getDefault(value);
				if (resource != null) {
					return resource.getResource();
				}
			} catch (Exception exc) {
			}
		}
		return defaultValue;
	}

	public static String getColor(Color color) {
		String value = getString(Integer.toHexString(color.getRed()), 2, '0') + getString(Integer.toHexString(color.getGreen()), 2, '0') + getString(Integer.toHexString(color.getBlue()), 2, '0');
		int alfa = color.getAlpha();
		if (alfa != 0xFF) {
			value = getString(Integer.toHexString(alfa), 2, '0') + value;
		}
		return '#' + value;
	}

	public static String getString(String s, int len, char fillChar) {
		while (s.length() < len) {
			s = fillChar + s;
		}
		return s;
	}

	/**
	 * Basic Format: <width> ; <dash array (A1,A2,...,An)>
	 * 
	 * @param stroke
	 *            Stroke
	 * @return String value
	 */
	public static String getStroke(Stroke stroke) {
		if (stroke instanceof BasicStroke) {
			BasicStroke s = (BasicStroke) stroke;
			float width = s.getLineWidth();
			int cap = s.getEndCap();
			int join = s.getLineJoin();
			float miterlimit = s.getMiterLimit();
			float dash[] = s.getDashArray();
			float dash_phase = s.getDashPhase();

			String value = "type=basic;";
			if (width != 1.0f) {
				value += "width=" + width + ";";
			}

			if (cap != BasicStroke.CAP_SQUARE) {
				value += "cap=" + CAP_TO_STRING.get(cap) + ";";
			}

			if (join != BasicStroke.JOIN_MITER) {
				value += "join=" + JOIN_TO_STRING.get(join) + ";";
			}

			if (miterlimit != 10.0f) {
				value += "miterlimit=" + miterlimit + ";";
			}

			if (dash != null && !(dash != null && dash.length == 1 && dash[0] == 1)) {
				value += "dash-array=" + getValue(dash, ",") + ";";
			}

			if (dash_phase != 0.0f) {
				value += "dash-phase=" + dash_phase + ";";
			}

			if (value.length() > 0) {
				return value;
			}
		} else if (stroke instanceof TextStroke) {
			TextStroke s = (TextStroke) stroke;
			String text = s.getText();
			Font font = s.getFont();
			boolean stretch = s.isStretchToFit();
			boolean repeat = s.isRepeat();

			if (text.length() > 0) {
				String value = "type=text;";
				String fontValue = getFont(font);
				if (fontValue != null) {
					value += fontValue + ";";
				}
				if (!stretch) {
					value += "stretch=no;";
				}
				if (repeat) {
					value += "repeat=yes;";
				}
				value += "text=" + text;
				return value;
			}
		} else if (stroke instanceof ArrowStroke) {
			String value = "type=arrow";
			ArrowStroke a = (ArrowStroke) stroke;
			switch (a.getType()) {
				case ArrowStroke.DIRECTION_BACK:
					value += ";dir=back";
					break;
				case ArrowStroke.DIRECTION_BOTH:
					value += ";dir=both";
					break;
				case ArrowStroke.DIRECTION_NONE:
					value += ";dir=none";
					break;
			}
			if (a.getWidth() != 14) {
				value += ";width=" + Double.toString(a.getWidth());
			}
			if (a.getArrowWidth() != 12) {
				value += ";arrow-width=" + Double.toString(a.getArrowWidth());
			}
			if (a.getArrowLength() != 6) {
				value += ";arrow-length=" + Double.toString(a.getArrowLength());
			}
			return value;
		}
		return null;
	}

	public static Stroke getStroke(String value) {
		Map<String, String> parameters = getMap(value);
		Stroke stroke = null;
		if (parameters != null) {
			String type = parameters.get("type");
			if ("text".equals(type)) {
				String text = parameters.get("text");
				if (text != null && text.length() > 0) {
					Font font = getFont(parameters, FontResource.DEFAULT_FONT);
					boolean stretch = !"no".equals(parameters.get("stretch"));
					boolean repeat = "yes".equals(parameters.get("repeat"));
					stroke = new TextStroke(text, font, stretch, repeat);
				}
			} else if ("arrow".equals(type)) {
				int dir = ArrowStroke.DIRECTION_DIRECT;
				String dirValue = parameters.get("dir");
				if ("back".equals(dirValue)) {
					dir = ArrowStroke.DIRECTION_BACK;
				} else if ("both".equals(dirValue)) {
					dir = ArrowStroke.DIRECTION_BOTH;
				} else if ("none".equals(dirValue)) {
					dir = ArrowStroke.DIRECTION_NONE;
				}
				double width = getDouble(parameters.get("width"), 14);
				double arrowWidth = getDouble(parameters.get("arrow-width"), 12);
				double arrowLength = getDouble(parameters.get("arrow-length"), 6);
				stroke = new ArrowStroke(width, arrowWidth, arrowLength, dir);
			} else {
				// basic type on default
				boolean notDefault = false;

				float width = getFloat(parameters.get("width"), 1.0f);
				notDefault |= width != 1.0f;

				int cap = BasicStroke.CAP_SQUARE;
				if (CAP_FROM_STRING.containsKey(parameters.get("cap"))) {
					cap = CAP_FROM_STRING.get(parameters.get("cap"));
				}
				notDefault |= cap != BasicStroke.CAP_SQUARE;

				int join = BasicStroke.JOIN_MITER;
				if (JOIN_FROM_STRING.containsKey(parameters.get("join"))) {
					join = JOIN_FROM_STRING.get(parameters.get("join"));
				}
				notDefault |= join != BasicStroke.JOIN_MITER;

				float miterlimit = getFloat(parameters.get("miterlimit"), 10.0f);
				notDefault |= miterlimit != 10.0f;

				float[] dash = getFloatArray(parameters.get("dash-array"), ",");
				notDefault |= dash != null && !(dash.length == 1 && dash[0] == 1.0f);

				float dash_phase = getFloat(parameters.get("dash-phase"), 0.0f);
				notDefault |= dash_phase != 0.0f;

				if (notDefault) {
					stroke = new BasicStroke(width, cap, join, miterlimit, dash, dash_phase);
				}
			}
		}
		return stroke;
	}

	public static boolean getBoolean(String value) {
		return "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
	}

	public static void getPath(String value, MutableGeneralPath path) throws JVGParseException {
		String[] array = getStringArray(value, " ");
		if (array == null) {
			return;
		}

		int index = 0;
		while (index < array.length) {
			if (array[index].length() != 1) {
				throw new JVGParseException("Bad path format: curve type=" + array[index]);
			}

			char type = Character.toLowerCase(array[index++].charAt(0));
			try {
				switch (type) {
					case 'm':
						path.moveTo(getDouble(array[index++], null), getDouble(array[index++], null));
						break;

					case 'l':
						path.lineTo(getDouble(array[index++], null), getDouble(array[index++], null));
						break;

					case 'q':
						path.quadTo(getDouble(array[index++], null), getDouble(array[index++], null), getDouble(array[index++], null), getDouble(array[index++], null));
						break;

					case 'c':
						path.curveTo(getDouble(array[index++], null), getDouble(array[index++], null), getDouble(array[index++], null), getDouble(array[index++], null), getDouble(array[index++], null), getDouble(array[index++], null));
						break;

					case 'x':
						path.closePath();
						break;

					default:
						throw new JVGParseException("Bad path format: curve type=" + array[index - 1]);
				}
			} catch (NumberFormatException exc) {
				throw new JVGParseException("Bad path format: " + exc.getMessage());
			}
		}
	}

	public static String getFont(Font font) {
		return getFont(font.getFamily(), font.isBold(), font.isItalic(), font.getSize());
	}

	public static String getFont(String family, boolean isBold, boolean isItalic, int size) {
		String font = "";
		if (!FontResource.DEFAULT_FAMILY.equals(family)) {
			font = "font-family=" + family;
		}

		String style = "";
		if (isBold && isItalic) {
			style = "bold,italic";
		} else if (isBold) {
			style = "bold";
		} else if (isItalic) {
			style = "italic";
		}
		if (style.length() > 0) {
			font += ";font-style=" + style;
		}

		if (size != 12) {
			font += ";font-size=" + size;
		}

		if (font.length() > 0) {
			return font;
		} else {
			return null;
		}
	}

	public static Font getFont(String value, Font defaultValue) {
		return getFont(getMap(value), defaultValue);
		// Format is 'name;style1, ..., styleN;size'
		//		try {
		//			String[] params = getParameters(value);
		//			String name = params[0] != null ? params[0].trim() : FontResource.DEFAULT_FAMILY;
		//
		//			int style = Font.PLAIN;
		//			int size = 12;
		//
		//			if(params.length > 0) {
		//				String[] styles = getStringArray(params[1], ",");
		//				if(styles != null) {
		//					for(int i = 0; i < styles.length; i++) {
		//						if("bold".equalsIgnoreCase(styles[i])) {
		//							style |= Font.BOLD;
		//						} else if("italic".equalsIgnoreCase(styles[i])) {
		//							style |= Font.ITALIC;
		//						}
		//					}
		//				}
		//
		//				if(params.length > 1) {
		//					size = Integer.parseInt(params[2]);
		//				}
		//			}
		//			return Fonts.getFont(name, style, size);
		//		} catch(Exception exc) {
		//			exc.printStackTrace();
		//		}
	}

	public static Font getFont(Map<String, String> parameters, Font defaultValue) {
		try {
			String name = parameters.get("font-family");
			if (name == null) {
				name = FontResource.DEFAULT_FAMILY;
			} else {
				name = name.trim();
			}

			int style = Font.PLAIN;
			int size = 12;

			String[] styles = getStringArray(parameters.get("font-style"), ",");
			if (styles != null) {
				for (int i = 0; i < styles.length; i++) {
					if ("bold".equalsIgnoreCase(styles[i])) {
						style |= Font.BOLD;
					} else if ("italic".equalsIgnoreCase(styles[i])) {
						style |= Font.ITALIC;
					}
				}
			}

			if (parameters.containsKey("font-size")) {
				size = Integer.parseInt(parameters.get("font-size"));
			}
			return Fonts.getFont(name, style, size);
		} catch (Exception exc) {
			exc.printStackTrace();
			return defaultValue;
		}
	}

	public static void setBorder(Element e, Border border) {
		// type
		if (border instanceof LineBorder) {
			LineBorder b = (LineBorder) border;
			e.setAttribute("border-type", "line");
			if (b.getThickness() != 1) {
				e.setAttribute("border-width", Integer.toString(b.getThickness()));
			}
			if (!Color.black.equals(b.getLineColor())) {
				e.setAttribute("border-color", JVGParseUtil.getColor(b.getLineColor()));
			}
		} else {
			e.setAttribute("border-type", "empty");
			Insets i = border.getBorderInsets(null);
			if (i.top != 0 || i.left != 0 || i.bottom != 0 || i.right != 0) {
				e.setAttribute("border-insets", i.top + "," + i.left + "," + i.bottom + "," + i.right);
			}
		}
	}

	public static Border getBorder(Element e) {
		String type = e.getAttributeValue("border-type");
		if ("line".equals(type)) {
			int width = JVGParseUtil.getInteger(e.getAttributeValue("border-width"), 1);
			Color color = JVGParseUtil.getColor(e.getAttributeValue("border-color"), Color.black);
			return new LineBorder(color, width);
		} else if ("empty".equals(type)) {
			String insets = e.getAttributeValue("border-insets");
			if (insets != null) {
				String[] i = insets.split(",");
				if (i.length == 4) {
					int i1 = JVGParseUtil.getInteger(i[0], 0);
					int i2 = JVGParseUtil.getInteger(i[1], 0);
					int i3 = JVGParseUtil.getInteger(i[2], 0);
					int i4 = JVGParseUtil.getInteger(i[3], 0);
					if (i1 != 0 || i2 != 0 || i3 != 0 || i4 != 0) {
						return new EmptyBorder(i1, i2, i3, i4);
					}
				}
			}
		}
		return null;
	}
}
