package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.CenterLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeListener;
import javax.swing.file.ExplorerPanel;

import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.SeekableOutputStream;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codecimpl.BMPCodec;
import com.sun.media.jai.codecimpl.BMPImageEncoder;
import com.sun.media.jai.codecimpl.JPEGCodec;
import com.sun.media.jai.codecimpl.JPEGImageEncoder;
import com.sun.media.jai.codecimpl.PNGCodec;
import com.sun.media.jai.codecimpl.PNGImageEncoder;
import com.sun.media.jai.codecimpl.TIFFCodec;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.parser.DocumentFormat;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;

public class Util {
	static {
		ImageIcon imgCollapsed = new ImageIcon(ExplorerPanel.class.getResource("/javax/swing/img/collapsed.gif"));
		ImageIcon imgExpanded = new ImageIcon(ExplorerPanel.class.getResource("/javax/swing/img/expanded.gif"));

		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		defaults.put("Tree.hash", Color.darkGray);
		defaults.put("Tree.lineTypeDashed", Boolean.TRUE);
		defaults.put("Tree.paintLines", Boolean.TRUE);
		defaults.put("Tree.line", Color.yellow);
		defaults.put("Tree.collapsedIcon", imgCollapsed);
		defaults.put("Tree.expandedIcon", imgExpanded);
	}

	public static void centrate(Window w) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		GraphicsConfiguration gc = w.getGraphicsConfiguration();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

		w.setLocation(screenInsets.left + (screenSize.width - screenInsets.left - screenInsets.right - w.getWidth()) / 2, screenInsets.top + (screenSize.height - screenInsets.top - screenInsets.bottom - w.getHeight()) / 2);
	}

	public static void installDefaultFont() {
		installDefaultFont(Fonts.getFont("Dialog", 0, 11));
	}

	public static void installDefaultFont(Font defaultFont) {
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();

		defaults.put("MenuItem.font", defaultFont);
		defaults.put("CheckBoxMenuItem.font", defaultFont);
		defaults.put("Menu.font", defaultFont);
		defaults.put("Label.font", defaultFont);
		defaults.put("Button.font", defaultFont);
		defaults.put("ToggleButton.font", defaultFont);
		defaults.put("CheckBox.font", defaultFont);
		defaults.put("RadioButton.font", defaultFont);
		defaults.put("ComboBox.font", defaultFont);
		defaults.put("TabbedPane.font", defaultFont);
		defaults.put("TitleBorder.font", defaultFont);
		defaults.put("Tree.font", defaultFont);
		defaults.put("List.font", defaultFont);
		defaults.put("Table.font", defaultFont);
		defaults.put("DesktopPane.font", defaultFont);
	}

	public static HashMap<String, String> getArguments(String[] args) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (args != null) {
			for (int i = 0; i < args.length; i += 2) {
				map.put(args[i], args[i + 1]);
			}
		}
		return map;
	}

	public static String getArgument(Map<String, String> args, String... params) {
		for (String param : params) {
			if (args.containsKey(param)) {
				return args.get(param);
			}
		}
		return null;
	}

	public static String getStringArgument(Map<String, String> args, String defaultValue, String... params) {
		String value = getArgument(args, params);
		return value != null ? value : defaultValue;
	}

	public static JFrame showSchema(JVGPane pane) {
		JPanel pnl = new JPanel();
		pnl.setBackground(Color.darkGray);
		pnl.setLayout(new CenterLayout());
		pnl.add(pane);

		final JFrame f = new JFrame();
		f.setIconImage(new ImageIcon(JVGEditor.class.getResource("img/preview.gif")).getImage());
		f.setContentPane(new JScrollPane(pnl));
		f.setLocation(0, 0);
		f.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setVisible(true);

		pane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					f.setVisible(false);
					f.dispose();
					f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
				}
			}
		});
		return f;
	}

	public static void selectWithoutEvent(JComboBox box, int index) {
		ActionListener[] listeners = box.getActionListeners();
		for (ActionListener listener : listeners) {
			box.removeActionListener(listener);
		}

		box.setSelectedIndex(index);

		for (ActionListener listener : listeners) {
			box.addActionListener(listener);
		}
	}

	public static void selectWithoutEvent(AbstractButton btn, boolean selected) {
		ActionListener[] listeners = btn.getActionListeners();
		for (ActionListener listener : listeners) {
			btn.removeActionListener(listener);
		}

		ChangeListener[] changeListeners = btn.getChangeListeners();
		for (ChangeListener listener : changeListeners) {
			btn.removeChangeListener(listener);
		}

		ItemListener[] itemListeners = btn.getItemListeners();
		for (ItemListener listener : itemListeners) {
			btn.removeItemListener(listener);
		}

		btn.setSelected(selected);

		for (ItemListener listener : itemListeners) {
			btn.addItemListener(listener);
		}

		for (ChangeListener listener : changeListeners) {
			btn.addChangeListener(listener);
		}

		for (ActionListener listener : listeners) {
			btn.addActionListener(listener);
		}
	}

	public static SnapshotData getSnapshot(JVGComponent component, int width, int height, int insets) throws JVGParseException {
		return getSnapshot(new JVGComponent[] { component }, width, height, insets);
	}

	public static SnapshotData getSnapshot(JVGComponent[] components, int width, int height, int insets) throws JVGParseException {
		JVGBuilder build = JVGBuilder.create(DocumentFormat.jvg);
		String data = build.build(components, "UTF8");
		return getSnapshot(new ByteArrayInputStream(data.getBytes()), width, height, insets);
	}

	public static SnapshotData getSnapshot(InputStream is, int width, int height, int insets) {
		SnapshotData data = null;
		try {
			JVGPane pane = new JVGPane();

			JVGEditorKit editorKit = pane.getEditorKit();
			editorKit.setFactory(JVGFactory.createDefault());
			JVGParser parser = new JVGParser(editorKit.getFactory());
			JVGRoot root = parser.parse(is);

			pane.setOpaque(false);
			pane.setRoot(root);

			Rectangle2D bounds = root.getRectangleBounds();

			ImageIcon img = getSnapshot(pane, width, height, insets, bounds);
			data = new SnapshotData(img, (int) bounds.getWidth(), (int) bounds.getHeight(), pane);
		} catch (JVGParseException exc) {
			exc.printStackTrace();
		}
		return data;
	}

	public static ImageIcon getSnapshot(JVGEditPane p, int width, int height, int insets) {
		ImageIcon img = null;

		try {
			JVGRoot root = p.getRoot();
			Rectangle2D bounds = new Rectangle2D.Double(0, 0, p.getDocumentSize().getWidth(), p.getDocumentSize().getHeight());

			JVGPane pane = new JVGPane();
			pane.setOpaque(false);
			pane.setRoot(root);

			img = getSnapshot(pane, width, height, insets, bounds);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return img;
	}

	public static ImageIcon getSnapshot(JVGPane pane, int width, int height, int insets, Rectangle2D bounds) {
		ImageIcon img = null;

		try {
			double x = bounds.getX() - insets;
			double y = bounds.getY() - insets;
			double w = bounds.getWidth() + 2 * insets;
			double h = bounds.getHeight() + 2 * insets;
			double scaleX = width / w;
			double scaleY = height / h;
			double scale = Math.min(scaleX, scaleY);

			AffineTransform transform = AffineTransform.getTranslateInstance(-x - w / 2.0, -y - h / 2.0);
			transform.preConcatenate(AffineTransform.getScaleInstance(scale, scale));
			transform.preConcatenate(AffineTransform.getTranslateInstance(width / 2.0, height / 2.0));

			pane.setTransform(transform);
			pane.setSize(new Dimension(width, height));

			BufferedImage bi = new BufferedImage(pane.getWidth(), pane.getHeight(), BufferedImage.TYPE_INT_ARGB);
			pane.print(bi.getGraphics());

			img = new ImageIcon(bi);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return img;
	}

	private static GradientPaint paintGradiaent = new GradientPaint(0, 0, new Color(240, 255, 240), 150, 150, new Color(200, 250, 200));

	public static void paintFormBackground(Graphics g, int w, int h) {
		// Graphics2D g2d = (Graphics2D) g;
		// Paint oldPaint = g2d.getPaint();
		// g2d.setPaint(paintGradiaent);
		// g2d.fillRect(0, 0, w, h);
		// g2d.setPaint(oldPaint);
	}

	public static void drawGlassRect(Graphics g, int w, int h) {
		GradientPaint paintGradiaent1 = new GradientPaint(0, 0, new Color(255, 255, 255), 0, h / 2, new Color(220, 220, 220));
		GradientPaint paintGradiaent2 = new GradientPaint(0, h / 2, new Color(210, 210, 210), 0, h, new Color(220, 220, 220));

		Graphics2D g2d = (Graphics2D) g;
		Paint oldPaint = g2d.getPaint();
		g2d.setPaint(paintGradiaent1);
		g2d.fillRect(0, 0, w, h / 2);
		g2d.setPaint(paintGradiaent2);
		g2d.fillRect(2, h / 2, w, h);
		g2d.setPaint(oldPaint);
	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).trim().toLowerCase();
		}

		return ext;
	}

	public static File saveImage(File file, Image image) throws Exception {
		RenderedImage rimg = null;
		if (image instanceof RenderedImage) {
			rimg = (RenderedImage) image;
		} else {
			BufferedImage bimg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
			bimg.getGraphics().drawImage(image, 0, 0, null);
			rimg = bimg;
		}
		return saveImage(file, rimg);
	}

	public static File saveImage(File file, RenderedImage image) throws Exception {
		String ext = getExtension(file);
		if (ext == null) {
			ext = "png";
			file = new File(file.getPath() + "." + ext);
		}

		OutputStream os = new SeekableOutputStream(new RandomAccessFile(file, "rw"));

		if (ext.equals("bmp")) {
			BMPCodec codec = new BMPCodec();
			BMPEncodeParam param = new BMPEncodeParam();
			BMPImageEncoder enc = (BMPImageEncoder) ImageCodec.createImageEncoder("bmp", os, param);
			enc.encode(image);
		} else if (ext.equals("png")) {
			PNGEncodeParam.RGB param = new PNGEncodeParam.RGB();
			param.setBitDepth(8);
			param.setTransparentRGB(new int[] { 255, 255, 255 });
			PNGImageEncoder enc = (PNGImageEncoder) (PNGCodec.createImageEncoder("png", os, param));
			enc.encode(image);
		} else if (ext.equals("jpg") || ext.equals("jpeg")) {
			JComboBox boxQuality = new JComboBox();
			boxQuality.addItem("Высокое");
			boxQuality.addItem("Среднее");
			boxQuality.addItem("Низкое");
			boxQuality.setSelectedIndex(1);

			if (JOptionPane.showConfirmDialog(null, boxQuality, "Выберите качество", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
				double quality = 1.0;
				if (boxQuality.getSelectedIndex() == 1) {
					quality = 0.7;
				} else if (boxQuality.getSelectedIndex() == 2) {
					quality = 0.5;
				}

				JPEGCodec codec = new JPEGCodec();
				JPEGEncodeParam param = new JPEGEncodeParam();
				param.setQuality((float) quality);
				JPEGImageEncoder enc = (JPEGImageEncoder) ImageCodec.createImageEncoder("jpeg", os, param);
				enc.encode(image);
			}
		} else if (ext.equals("tif") || ext.equals("tiff")) {
			TIFFCodec codec = new TIFFCodec();
			TIFFEncodeParam param = new TIFFEncodeParam();
			param.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
			TIFFImageEncoder enc = (TIFFImageEncoder) ImageCodec.createImageEncoder("tiff", os, param);
			enc.encode(image);
		}

		os.flush();
		os.close();

		return file;
	}
}
