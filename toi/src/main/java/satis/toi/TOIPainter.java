package satis.toi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;

import satis.toi.factory.TOIDefaultFactory;

import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codecimpl.BMPImageEncoder;
import com.sun.media.jai.codecimpl.JPEGImageEncoder;
import com.sun.media.jai.codecimpl.PNGImageEncoder;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;

public class TOIPainter {
	private List<TOIObject> objects;

	private TOIColor colorRenderer = new TOIDefaultColor();

	public TOIPainter(List<TOIObject> objects) {
		this.objects = objects;
	}

	public TOIPainter(String doc) throws Exception {
		this.objects = new TOIBuilder(new TOIDefaultFactory()).load(doc);
	}

	public TOIPainter(Reader r) throws Exception {
		this.objects = new TOIBuilder(new TOIDefaultFactory()).load(r);
	}

	public void paint(Graphics g, int width, int height) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, width, height);

		TOIPaintContext ctx = new TOIPaintContext();
		ctx.setColorRenderer(colorRenderer);

		for (TOIObject o : objects) {
			o.paint(g2d, g2d, ctx);
		}
	}

	public byte[] exportImage(String format, int width, int height) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		exportImage(format, width, height, bos);
		return bos.toByteArray();
	}

	public void exportImage(String format, int width, int height, OutputStream os) throws IOException {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		paint(g, width, height);

		ImageEncoder enc = null;
		if (format.equals("bmp")) {
			BMPEncodeParam param = new BMPEncodeParam();
			enc = new BMPImageEncoder(os, param);
		} else if (format.equals("png")) {
			PNGEncodeParam param = PNGEncodeParam.getDefaultEncodeParam(img);
			param.setBitDepth(8);
			enc = new PNGImageEncoder(os, param);
		} else if (format.equals("jpg") || format.equals("jpeg")) {
			float quality = 1f;
			JPEGEncodeParam param = new JPEGEncodeParam();
			param.setQuality(quality);
			enc = new JPEGImageEncoder(os, param);
		} else if (format.equals("tif") || format.equals("tiff")) {
			TIFFEncodeParam param = new TIFFEncodeParam();
			param.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
			enc = new TIFFImageEncoder(os, param);
		}

		enc.encode(img);
		os.flush();
		os.close();
	}

	public TOIColor getColorRenderer() {
		return colorRenderer;
	}

	public void setColorRenderer(TOIColor colorRenderer) {
		this.colorRenderer = colorRenderer;
	}

	public static void main(String[] args) {
		try {
			TOIPainter p = new TOIPainter(new FileReader("C:/Users/john/Documents/toi3.xml"));
			p.exportImage("bmp", 390, 293, new FileOutputStream("C:/Users/john/Documents/toi3.bmp"));
			p.exportImage("png", 390, 293, new FileOutputStream("C:/Users/john/Documents/toi3.png"));
			p.exportImage("jpg", 390, 293, new FileOutputStream("C:/Users/john/Documents/toi3.jpg"));
			p.exportImage("tif", 390, 293, new FileOutputStream("C:/Users/john/Documents/toi3.tif"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
