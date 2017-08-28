package ru.nest.jvg.resource;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ColorResource extends Resource<Color> {
	private final static TreeMap<String, ColorResource> defaults = new TreeMap<String, ColorResource>();

	public final static Resource<Color> getDefault(String name) {
		if (name != null) {
			return defaults.get(name);
		} else {
			return null;
		}
	}

	public final static ColorResource aliceblue = new ColorResource("aliceblue", new Color(0xf0f8ff), true);

	public final static ColorResource antiquewhite = new ColorResource("antiquewhite", new Color(0xfaebd7), true);

	public final static ColorResource aqua = new ColorResource("aqua", new Color(0x00ffff), true);

	public final static ColorResource aquamarine = new ColorResource("aquamarine", new Color(0x7fffd4), true);

	public final static ColorResource azure = new ColorResource("azure", new Color(0xf0ffff), true);

	public final static ColorResource beige = new ColorResource("beige", new Color(0xf5f5dc), true);

	public final static ColorResource bisque = new ColorResource("bisque", new Color(0xffe4c4), true);

	public final static ColorResource black = new ColorResource("black", Color.black, true);

	public final static ColorResource blanchedalmond = new ColorResource("blanchedalmond", new Color(0xffebcd), true);

	public final static ColorResource blue = new ColorResource("blue", Color.blue, true);

	public final static ColorResource blueviolet = new ColorResource("blueviolet", new Color(0x8a2be2), true);

	public final static ColorResource brown = new ColorResource("brown", new Color(0xa52a2a), true);

	public final static ColorResource burlywood = new ColorResource("burlywood", new Color(0xdeb887), true);

	public final static ColorResource cadetblue = new ColorResource("cadetblue", new Color(0x5f9ea0), true);

	public final static ColorResource chartreuse = new ColorResource("chartreuse", new Color(0x7fff00), true);

	public final static ColorResource chocolate = new ColorResource("chocolate", new Color(0xd2691e), true);

	public final static ColorResource coral = new ColorResource("coral", new Color(0xff7f50), true);

	public final static ColorResource cornflowerblue = new ColorResource("cornflowerblue", new Color(0x6495ed), true);

	public final static ColorResource cornsilk = new ColorResource("cornsilk", new Color(0xfff8dc), true);

	public final static ColorResource crimson = new ColorResource("crimson", new Color(0xdc143c), true);

	public final static ColorResource cyan = new ColorResource("cyan", Color.cyan, true);

	public final static ColorResource darkblue = new ColorResource("darkblue", new Color(0x00008b), true);

	public final static ColorResource darkcyan = new ColorResource("darkcyan", new Color(0x008b8b), true);

	public final static ColorResource darkgoldenrod = new ColorResource("darkgoldenrod", new Color(0xb8860b), true);

	public final static ColorResource darkgray = new ColorResource("darkgray", Color.darkGray, true);

	public final static ColorResource darkgreen = new ColorResource("darkgreen", new Color(0x006400), true);

	public final static ColorResource darkkhaki = new ColorResource("darkkhaki", new Color(0xbdb76b), true);

	public final static ColorResource darkmagenta = new ColorResource("darkmagenta", new Color(0x8b008b), true);

	public final static ColorResource darkolivegreen = new ColorResource("darkolivegreen", new Color(0x556b2f), true);

	public final static ColorResource darkorange = new ColorResource("darkorange", new Color(0xff8c00), true);

	public final static ColorResource darkorchid = new ColorResource("darkorchid", new Color(0x9932cc), true);

	public final static ColorResource darkred = new ColorResource("darkred", new Color(0x8b0000), true);

	public final static ColorResource darksalmon = new ColorResource("darksalmon", new Color(0xe9967a), true);

	public final static ColorResource darkseagreen = new ColorResource("darkseagreen", new Color(0x8fbc8f), true);

	public final static ColorResource darkslateblue = new ColorResource("darkslateblue", new Color(0x483d8b), true);

	public final static ColorResource darkslategray = new ColorResource("darkslategray", new Color(0x2f4f4f), true);

	public final static ColorResource darkturquoise = new ColorResource("darkturquoise", new Color(0x00ced1), true);

	public final static ColorResource darkviolet = new ColorResource("darkviolet", new Color(0x9400d3), true);

	public final static ColorResource deeppink = new ColorResource("deeppink", new Color(0xff1493), true);

	public final static ColorResource deepskyblue = new ColorResource("deepskyblue", new Color(0x00bfff), true);

	public final static ColorResource dimgray = new ColorResource("dimgray", new Color(0x696969), true);

	public final static ColorResource dodgerblue = new ColorResource("dodgerblue", new Color(0x1e90ff), true);

	public final static ColorResource feldspar = new ColorResource("feldspar", new Color(0xd19275), true);

	public final static ColorResource firebrick = new ColorResource("firebrick", new Color(0xb22222), true);

	public final static ColorResource floralwhite = new ColorResource("floralwhite", new Color(0xfffaf0), true);

	public final static ColorResource forestgreen = new ColorResource("forestgreen", new Color(0x228b22), true);

	public final static ColorResource fuchsia = new ColorResource("fuchsia", new Color(0xff00ff), true);

	public final static ColorResource gainsboro = new ColorResource("gainsboro", new Color(0xdcdcdc), true);

	public final static ColorResource ghostwhite = new ColorResource("ghostwhite", new Color(0xf8f8ff), true);

	public final static ColorResource gold = new ColorResource("gold", new Color(0xffd700), true);

	public final static ColorResource goldenrod = new ColorResource("goldenrod", new Color(0xdaa520), true);

	public final static ColorResource gray = new ColorResource("gray", Color.gray, true);

	public final static ColorResource green = new ColorResource("green", Color.green, true);

	public final static ColorResource greenyellow = new ColorResource("greenyellow", new Color(0xadff2f), true);

	public final static ColorResource honeydew = new ColorResource("honeydew", new Color(0xf0fff0), true);

	public final static ColorResource hotpink = new ColorResource("hotpink", new Color(0xff69b4), true);

	public final static ColorResource indianred = new ColorResource("indianred", new Color(0xcd5c5c), true);

	public final static ColorResource indigo = new ColorResource("indigo", new Color(0x4b0082), true);

	public final static ColorResource ivory = new ColorResource("ivory", new Color(0xfffff0), true);

	public final static ColorResource khaki = new ColorResource("khaki", new Color(0xf0e68c), true);

	public final static ColorResource lavender = new ColorResource("lavender", new Color(0xe6e6fa), true);

	public final static ColorResource lavenderblush = new ColorResource("lavenderblush", new Color(0xfff0f5), true);

	public final static ColorResource lawngreen = new ColorResource("lawngreen", new Color(0x7cfc00), true);

	public final static ColorResource lemonchiffon = new ColorResource("lemonchiffon", new Color(0xfffacd), true);

	public final static ColorResource lightblue = new ColorResource("lightblue", new Color(0xadd8e6), true);

	public final static ColorResource lightcoral = new ColorResource("lightcoral", new Color(0xf08080), true);

	public final static ColorResource lightcyan = new ColorResource("lightcyan", new Color(0xe0ffff), true);

	public final static ColorResource lightgoldenrodyellow = new ColorResource("lightgoldenrodyellow", new Color(0xfafad2), true);

	public final static ColorResource lightgray = new ColorResource("lightgray", Color.lightGray, true);

	public final static ColorResource lightgreen = new ColorResource("lightgreen", new Color(0x90ee90), true);

	public final static ColorResource lightpink = new ColorResource("lightpink", new Color(0xffb6c1), true);

	public final static ColorResource lightsalmon = new ColorResource("lightsalmon", new Color(0xffa07a), true);

	public final static ColorResource lightseagreen = new ColorResource("lightseagreen", new Color(0x20b2aa), true);

	public final static ColorResource lightskyblue = new ColorResource("lightskyblue", new Color(0x87cefa), true);

	public final static ColorResource lightslateblue = new ColorResource("lightslateblue", new Color(0x8470ff), true);

	public final static ColorResource lightslategray = new ColorResource("lightslategray", new Color(0x778899), true);

	public final static ColorResource lightsteelblue = new ColorResource("lightsteelblue", new Color(0xb0c4de), true);

	public final static ColorResource lightyellow = new ColorResource("lightyellow", new Color(0xffffe0), true);

	public final static ColorResource lime = new ColorResource("lime", new Color(0x00ff00), true);

	public final static ColorResource limegreen = new ColorResource("limegreen", new Color(0x32cd32), true);

	public final static ColorResource linen = new ColorResource("linen", new Color(0xfaf0e6), true);

	public final static ColorResource magenta = new ColorResource("magenta", Color.magenta, true);

	public final static ColorResource maroon = new ColorResource("maroon", new Color(0x800000), true);

	public final static ColorResource mediumaquamarine = new ColorResource("mediumaquamarine", new Color(0x66cdaa), true);

	public final static ColorResource mediumblue = new ColorResource("mediumblue", new Color(0x0000cd), true);

	public final static ColorResource mediumorchid = new ColorResource("mediumorchid", new Color(0xba55d3), true);

	public final static ColorResource mediumpurple = new ColorResource("mediumpurple", new Color(0x9370d8), true);

	public final static ColorResource mediumseagreen = new ColorResource("mediumseagreen", new Color(0x3cb371), true);

	public final static ColorResource mediumslateblue = new ColorResource("mediumslateblue", new Color(0x7b68ee), true);

	public final static ColorResource mediumspringgreen = new ColorResource("mediumspringgreen", new Color(0x00fa9a), true);

	public final static ColorResource mediumturquoise = new ColorResource("mediumturquoise", new Color(0x48d1cc), true);

	public final static ColorResource mediumvioletred = new ColorResource("mediumvioletred", new Color(0xc71585), true);

	public final static ColorResource midnightblue = new ColorResource("midnightblue", new Color(0x191970), true);

	public final static ColorResource mintcream = new ColorResource("mintcream", new Color(0xf5fffa), true);

	public final static ColorResource mistyrose = new ColorResource("mistyrose", new Color(0xffe4e1), true);

	public final static ColorResource moccasin = new ColorResource("moccasin", new Color(0xffe4b5), true);

	public final static ColorResource navajowhite = new ColorResource("navajowhite", new Color(0xffdead), true);

	public final static ColorResource navy = new ColorResource("navy", new Color(0x000080), true);

	public final static ColorResource oldlace = new ColorResource("oldlace", new Color(0xfdf5e6), true);

	public final static ColorResource olive = new ColorResource("olive", new Color(0x808000), true);

	public final static ColorResource olivedrab = new ColorResource("olivedrab", new Color(0x6b8e23), true);

	public final static ColorResource orange = new ColorResource("orange", Color.orange, true);

	public final static ColorResource orangered = new ColorResource("orangered", new Color(0xff4500), true);

	public final static ColorResource orchid = new ColorResource("orchid", new Color(0xda70d6), true);

	public final static ColorResource palegoldenrod = new ColorResource("palegoldenrod", new Color(0xeee8aa), true);

	public final static ColorResource palegreen = new ColorResource("palegreen", new Color(0x98fb98), true);

	public final static ColorResource paleturquoise = new ColorResource("paleturquoise", new Color(0xafeeee), true);

	public final static ColorResource palevioletred = new ColorResource("palevioletred", new Color(0xd87093), true);

	public final static ColorResource papayawhip = new ColorResource("papayawhip", new Color(0xffefd5), true);

	public final static ColorResource peachpuff = new ColorResource("peachpuff", new Color(0xffdab9), true);

	public final static ColorResource peru = new ColorResource("peru", new Color(0xcd853f), true);

	public final static ColorResource pink = new ColorResource("pink", Color.pink, true);

	public final static ColorResource plum = new ColorResource("plum", new Color(0xdda0dd), true);

	public final static ColorResource powderblue = new ColorResource("powderblue", new Color(0xb0e0e6), true);

	public final static ColorResource purple = new ColorResource("purple", new Color(0x800080), true);

	public final static ColorResource red = new ColorResource("red", Color.red, true);

	public final static ColorResource rosybrown = new ColorResource("rosybrown", new Color(0xbc8f8f), true);

	public final static ColorResource royalblue = new ColorResource("royalblue", new Color(0x4169e1), true);

	public final static ColorResource saddlebrown = new ColorResource("saddlebrown", new Color(0x8b4513), true);

	public final static ColorResource salmon = new ColorResource("salmon", new Color(0xfa8072), true);

	public final static ColorResource sandybrown = new ColorResource("sandybrown", new Color(0xf4a460), true);

	public final static ColorResource seagreen = new ColorResource("seagreen", new Color(0x2e8b57), true);

	public final static ColorResource seashell = new ColorResource("seashell", new Color(0xfff5ee), true);

	public final static ColorResource sienna = new ColorResource("sienna", new Color(0xa0522d), true);

	public final static ColorResource silver = new ColorResource("silver", new Color(0xc0c0c0), true);

	public final static ColorResource skyblue = new ColorResource("skyblue", new Color(0x87ceeb), true);

	public final static ColorResource slateblue = new ColorResource("slateblue", new Color(0x6a5acd), true);

	public final static ColorResource slategray = new ColorResource("slategray", new Color(0x708090), true);

	public final static ColorResource snow = new ColorResource("snow", new Color(0xfffafa), true);

	public final static ColorResource springgreen = new ColorResource("springgreen", new Color(0x00ff7f), true);

	public final static ColorResource steelblue = new ColorResource("steelblue", new Color(0x4682b4), true);

	public final static ColorResource tan = new ColorResource("tan", new Color(0xd2b48c), true);

	public final static ColorResource teal = new ColorResource("teal", new Color(0x008080), true);

	public final static ColorResource thistle = new ColorResource("thistle", new Color(0xd8bfd8), true);

	public final static ColorResource tomato = new ColorResource("tomato", new Color(0xff6347), true);

	public final static ColorResource turquoise = new ColorResource("turquoise", new Color(0x40e0d0), true);

	public final static ColorResource violet = new ColorResource("violet", new Color(0xee82ee), true);

	public final static ColorResource violetred = new ColorResource("violetred", new Color(0xd02090), true);

	public final static ColorResource wheat = new ColorResource("wheat", new Color(0xf5deb3), true);

	public final static ColorResource white = new ColorResource("white", Color.white, true);

	public final static ColorResource whitesmoke = new ColorResource("whitesmoke", new Color(0xf5f5f5), true);

	public final static ColorResource yellow = new ColorResource("yellow", Color.yellow, true);

	public final static ColorResource yellowgreen = new ColorResource("yellowgreen", new Color(0x9acd32), true);

	public final static ColorResource transparent = new ColorResource("transparent", new Color(0, 0, 0, 0), true);

	public ColorResource(Color color) {
		setResource(color);
	}

	public ColorResource(String name, Color color) {
		this(name, color, false);
		setResource(color);
	}

	public ColorResource(String name, Color color, boolean isDefault) {
		super(name);

		setResource(color);
		if (isDefault) {
			synchronized (defaults) {
				defaults.put(name, this);
			}
		}
	}

	private Color color;

	@Override
	public Color getResource() {
		return color;
	}

	@Override
	public void setResource(Color color) {
		this.color = color;
	}

	@Override
	public boolean equals(Object o) {
		if (color != null && o instanceof ColorResource) {
			ColorResource c = (ColorResource) o;
			return color.equals(c.color);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return color != null ? color.hashCode() : -1;
	}

	public static void main(String[] arg) {
		JFrame f = new JFrame();

		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

		for (Iterator<String> iter = defaults.keySet().iterator(); iter.hasNext();) {
			String name = iter.next();

			JLabel l = new JLabel(name);
			l.setPreferredSize(new Dimension(200, 20));
			l.setOpaque(true);
			l.setBackground(defaults.get(name).getResource());
			pnl.add(l);
		}

		f.setContentPane(new JScrollPane(pnl));

		f.setBounds(200, 200, 600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
