package ru.nest.fonts;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Fonts {
	private static Map<String, Font> fonts = new HashMap<>();

	private static Font[] fontsArray;

	private static String[] fontnames = { "dialog", "monospaced", "pixel_8pt.ttf", "andlso.ttf", "arial.ttf", "Arimo-Regular.ttf", "BKANT.ttf", "BOOKOS.ttf", "browa.ttf", "calibri.ttf", "Candara.ttf", "CENTURY.ttf", "consola.ttf", "constan.ttf", "corbel.ttf", "cordiau.ttf", "cour.ttf", "Curlz___.ttf", "daunpenh.ttf", "david.ttf", "DejaVuSans-ExtraLight.ttf", "DejaVuSans.ttf", "DejaVuSansCondensed.ttf", "DejaVuSansMono.ttf", "DejaVuSerif.ttf", "DejaVuSerifCondensed.ttf", "dokchamp.ttf", "Engr.ttf", "Erasdemi.ttf", "Eraslght.ttf", "estre.ttf", "euphemia.ttf", "Eurosti.ttf", "Felixti.ttf", "Frabk.ttf", "Fradm.ttf", "FRADMCN.ttf", "framd.ttf", "Framdcn.ttf", "frank.ttf", "FREESCPT.ttf", "GARA.ttf", "gautami.ttf", "georgia.ttf", "gisha.ttf", "GOTHIC.ttf", "HATTEN.ttf", "himalaya.ttf",
			"impact.ttf", "iskpota.ttf", "ITCBlkad.ttf", "andlso.ttf", "arial.ttf", "Arimo-Regular.ttf", "BKANT.ttf", "BOOKOS.ttf", "browa.ttf", "calibri.ttf", "Candara.ttf", "CENTURY.ttf", "consola.ttf", "constan.ttf", "corbel.ttf", "cordiau.ttf", "cour.ttf", "Curlz___.ttf", "daunpenh.ttf", "david.ttf", "DejaVuSans-ExtraLight.ttf", "DejaVuSans.ttf", "DejaVuSansCondensed.ttf", "DejaVuSansMono.ttf", "DejaVuSerif.ttf", "DejaVuSerifCondensed.ttf", "dokchamp.ttf", "Engr.ttf", "Erasdemi.ttf", "Eraslght.ttf", "estre.ttf", "euphemia.ttf", "Eurosti.ttf", "Felixti.ttf", "Frabk.ttf", "Fradm.ttf", "FRADMCN.ttf", "framd.ttf", "Framdcn.ttf", "frank.ttf", "FREESCPT.ttf", "GARA.ttf", "gautami.ttf", "georgia.ttf", "gisha.ttf", "GOTHIC.ttf", "HATTEN.ttf", "himalaya.ttf", "impact.ttf", "iskpota.ttf",
			"ITCBlkad.ttf", "ITCEdscr.ttf", "ITCKRIST.ttf", "Jokerman.ttf", "JUICE___.ttf", "kalinga.ttf", "kartika.ttf", "latha.ttf", "leelawad.ttf", "Lsans.ttf", "lucon.ttf", "lvnm.ttf", "l_10646.ttf", "Maian.ttf", "mangal.ttf", "matisse_.ttf", "micross.ttf", "MISTRAL.ttf", "monbaiti.ttf", "moolbor.ttf", "mriam.ttf", "msuighur.ttf", "msyi.ttf", "MTCORSVA.ttf", "nrkis.ttf", "nyala.ttf", "OCRAExt.ttf", "pala.ttf", "PAPYRUS.ttf", "Per_____.ttf", "plantc.ttf", "PRISTINA.ttf", "raavi.ttf", "Rock.ttf", "rod.ttf", "segoepr.ttf", "segoesc.ttf", "segoeui.ttf", "shruti.ttf", "simpo.ttf", "sylfaen.ttf", "tahoma.ttf", "TEMPSITC.ttf", "times.ttf", "trado.ttf", "trebuc.ttf", "tunga.ttf", "upcdl.ttf", "upcel.ttf", "upcfl.ttf", "upcil.ttf", "upckl.ttf", "upcll.ttf", "verdana.ttf", "vrinda.ttf" };

	static {
		try {
			long t = System.currentTimeMillis();
			fontsArray = new Font[fontnames.length + 2];

			fontsArray[0] = new Font("Dialog", Font.PLAIN, 1);
			fonts.put(fontsArray[0].getName().toLowerCase(), fontsArray[0]);

			fontsArray[1] = new Font("Monospaced", Font.PLAIN, 1);
			fonts.put(fontsArray[1].getName().toLowerCase(), fontsArray[1]);

			for (int i = 2; i < fontnames.length; i++) {
				fontsArray[i] = registerFont(Fonts.class.getResourceAsStream("ttf/" + fontnames[i]), fontnames[i]);
			}
			System.out.println("fonts registered for " + (System.currentTimeMillis() - t));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static Font registerFont(InputStream is, String name) {
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			fonts.put(font.getName().toLowerCase(), font);
		} catch (Throwable e) {
			System.out.println("bad font: " + name);
		}
		return font;
	}

	public static Font getFont(String name) {
		if (fonts.containsKey(name.toLowerCase())) {
			return fonts.get(name.toLowerCase());
		} else {
			Font font = new Font(name, Font.PLAIN, 12);
			fonts.put(font.getName().toLowerCase(), font);
			System.out.println("register font: " + font.getName());
			return font;
		}
	}

	public static Font getPixelFont() {
		return getFont("DPix_8pt");
	}

	public static Font getFont(String name, int style, int size) {
		return getFont(name).deriveFont(style, size);
	}

	public static Font[] getFonts() {
		return fontsArray;
	}
}
