package javax.swing.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.Util;
import javax.swing.WindowConstants;

import ru.nest.jvg.editor.ImageViewPanel;

public class ImageExplorerPanel extends JPanel implements ExplorerPanel.Listener {
	public final static String[] IMAGE_EXTENTIONS = { "bmp", "jpg", "jpeg", "tif", "tiff", "gif", "png" };

	public static ImageIcon imgFileBMP = new ImageIcon(ExplorerPanel.class.getResource("../img/filebmp.gif"));

	public static ImageIcon imgFileJPG = new ImageIcon(ExplorerPanel.class.getResource("../img/filejpg.gif"));

	public static ImageIcon imgFileGIF = new ImageIcon(ExplorerPanel.class.getResource("../img/filegif.gif"));

	public static ImageIcon imgFilePNG = new ImageIcon(ExplorerPanel.class.getResource("../img/filepng.gif"));

	public static ImageIcon imgFileTIF = new ImageIcon(ExplorerPanel.class.getResource("../img/filetif.gif"));

	public static ImageIcon[] IMAGE_FILES_ICONS = { imgFileBMP, imgFileJPG, imgFileJPG, imgFileTIF, imgFileTIF, imgFileGIF, imgFilePNG };

	public final static HashSet<String> IMAGE_EXTENTIONS_HASH = new HashSet<>();

	public final static HashMap<String, Icon> IMAGE_FILE_ICONS_MAP = new HashMap<>();
	static {
		for (int i = 0; i < IMAGE_EXTENTIONS.length; i++) {
			IMAGE_EXTENTIONS_HASH.add(IMAGE_EXTENTIONS[i]);
			IMAGE_FILE_ICONS_MAP.put(IMAGE_EXTENTIONS[i], IMAGE_FILES_ICONS[i]);
		}
	}

	private ImageViewPanel imagePanel;

	private ImageInfoPanel infoPanel;

	public ImageExplorerPanel() {
		explorer = new ExplorerPanel();
		explorer.setPreferredSize(new Dimension(400, 400));
		explorer.addListener(this);
		explorer.getList().setFilter(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String ext = Util.getFileExtention(name);
				if (!IMAGE_EXTENTIONS_HASH.contains(ext)) {
					return false;
				}

				File file = name != null ? new File(dir, name) : dir;
				return !file.isDirectory();
			}
		});
		explorer.getList().setIconByExtention(new IconByExtention() {
			@Override
			public Icon getIcon(String extention) {
				return IMAGE_FILE_ICONS_MAP.get(extention);
			}
		});

		infoPanel = new ImageInfoPanel();
		// infoPanel.setPreferredSize(new Dimension(100, 20));
		infoPanel.setBorder(BorderFactory.createLoweredBevelBorder());

		imagePanel = new ImageViewPanel();
		JPanel pnlImg = new JPanel();
		pnlImg.setBorder(BorderFactory.createLoweredBevelBorder());
		pnlImg.setLayout(new BorderLayout());
		pnlImg.add(imagePanel, BorderLayout.CENTER);

		split = new JSplitPane();
		split.setDividerLocation(300);
		split.setDividerSize(3);
		split.setLeftComponent(explorer);
		split.setRightComponent(pnlImg);

		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);
		add(infoPanel, BorderLayout.SOUTH);

		explorer.clearSelection();
	}

	private JSplitPane split;

	public JSplitPane getSplit() {
		return split;
	}

	private ExplorerPanel explorer;

	public ExplorerPanel getExplorer() {
		return explorer;
	}

	@Override
	public void fileSelected(File file) {
		Icon icon = null;
		if (file != null && file.isFile()) {
			try {
				icon = new ImageIcon(file.toURI().toURL());
			} catch (MalformedURLException exc) {
				exc.printStackTrace();
			}
		}
		imagePanel.getImagePanel().setImage(icon);
		infoPanel.setImage(file != null && !file.isDirectory() ? file : null, icon);
	}

	public static void main(String[] args) {
		ru.nest.jvg.editor.Util.installDefaultFont();

		ImageExplorerPanel ex = new ImageExplorerPanel();

		JFrame f = new JFrame();
		f.setResizable(false);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setBounds(300, 100, 800, 400);
		f.setContentPane(ex);
		f.setVisible(true);
	}
}

// class ImagePanel extends JLabel
// {
// public ImagePanel()
// {
// setOpaque(true);
// setBackground(Color.white);
// setRequestFocusEnabled(true);
// setFocusable(true);
//
// addKeyListener(new KeyAdapter()
// {
// public void keyPressed(KeyEvent e)
// {
// if(e.getKeyCode() == KeyEvent.VK_PLUS)
// {
// scale(scale * 1.1f);
// }
// else if(e.getKeyCode() == KeyEvent.VK_MINUS)
// {
// scale(scale / 1.1f);
// }
// }
// });
//
// addComponentListener(new ComponentAdapter()
// {
// public void componentShown(ComponentEvent e)
// {
// normalize();
// }
// });
// }
//
//
// private File file;
// public File getFile()
// {
// return file;
// }
//
//
// private ImageIcon image;
// public ImageIcon getImage()
// {
// return image;
// }
//
//
// public void setImage(File file)
// {
// try
// {
// image = new ImageIcon(file.toURL());
// this.file = file;
// normalize();
// }
// catch(MalformedURLException exc)
// {
// exc.printStackTrace();
// }
// }
//
//
// public void normalize()
// {
// if(image != null)
// {
// float w = image.getIconWidth();
// float h = image.getIconHeight();
// float scaleX = getWidth() / w;
// float scaleY = getHeight() / h;
// scale = Math.min(scaleX, scaleY);
// if(scale > 1)
// {
// scale = 1;
// }
//
// int scaledWidth = (int)(scale * w);
// int scaledHeight = (int)(scale * h);
// tx = (getWidth() - scaledWidth) / 2;
// ty = (getHeight() - scaledHeight) / 2;
//
// repaint();
// }
// }
//
//
// public void clearImage()
// {
// file = null;
// image = null;
// repaint();
// }
//
//
// private int tx;
// private int ty;
// public void translate(int tx, int ty)
// {
// this.tx += tx;
// this.ty += ty;
// repaint();
// }
//
//
// private float scale = 1;
// public void scale(float scale)
// {
// this.scale = scale;
// repaint();
// }
//
//
// public void paint(Graphics g)
// {
// super.paint(g);
//
// if(scale == 0)
// {
// normalize();
// if(scale == 0)
// {
// return;
// }
// }
//
// if(image != null)
// {
// Graphics2D g2d = (Graphics2D)g;
// g2d.translate(tx, ty);
// g2d.scale(scale, scale);
//
// g.drawImage(image.getImage(), 0, 0, null);
// }
// }
// }

class ImageInfoPanel extends JPanel {
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat(System.getProperty("image.info.panel.date-format", "dd.MM.yyyy HH:mm"));

	private JLabel lblIcon;

	private JLabel lblName;

	private JLabel lblWxH;

	private JLabel lblFileSize;

	private JLabel lblChangeTime;

	public ImageInfoPanel() {
		lblIcon = new JLabel(" ");
		lblName = new JLabel();
		lblWxH = new JLabel();
		lblFileSize = new JLabel();
		lblChangeTime = new JLabel();

		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(lblIcon);
		add(lblName);
		add(getDescr(System.getProperty("image.info.panel.image-size", " Image size: ")));
		add(lblWxH);
		add(getDescr(System.getProperty("image.info.panel.file-size", " File size: ")));
		add(lblFileSize);
		add(getDescr(System.getProperty("image.info.panel.change-time", " Change time: ")));
		add(lblChangeTime);
	}

	private JLabel getDescr(String text) {
		JLabel lbl = new JLabel(text);
		lbl.setForeground(Color.gray);
		return lbl;
	}

	private File file;

	public File getSource() {
		return file;
	}

	private Icon image;

	public Icon getImage() {
		return image;
	}

	public void setImage(File file, Icon image) {
		if (file != null && image != null) {
			this.file = file;
			this.image = image;

			lblIcon.setIcon(ImageExplorerPanel.IMAGE_FILE_ICONS_MAP.get(Util.getFileExtention(file.getName())));
			lblName.setText(file.getName());
			lblWxH.setText(image.getIconWidth() + " x " + image.getIconHeight());
			lblFileSize.setText(getFileSize(file.length()));
			lblChangeTime.setText(dateFormat.format(new Date(file.lastModified())));
		} else {
			this.file = null;
			this.image = null;

			lblIcon.setIcon(null);
			lblName.setText("");
			lblWxH.setText("");
			lblFileSize.setText("");
			lblChangeTime.setText("");
		}
	}

	public static String getFileSize(long size) {
		if (size < 1000) {
			return size + " " + System.getProperty("image.info.panel.bytes", "bytes");
		} else if (size < 1000000) {
			return size / 1000.0 + " " + System.getProperty("image.info.panel.kb", "KB");
		} else if (size < 1000000) {
			return size / 1000000.0 + " " + System.getProperty("image.info.panel.mb", "MB");
		}
		return "";
	}
}
