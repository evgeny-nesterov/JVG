package javax.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.VolatileImage;

/**
 * Add to the constructor to update image: enableEvents(AWTEvent.COMPONENT_EVENT_MASK); setBorder(...) doesn't work; childs has to be painted
 * separately: paintComponents(g);
 **/
public abstract class BufferedImagePanel extends JComponent {
	private static final long serialVersionUID = 1L;

	public final static Insets nullInsets = new Insets(0, 0, 0, 0);

	private ImageCapabilities capabilities = new ImageCapabilities(true);

	private VolatileImage img = null;

	private boolean isRepaintImage = true;

	public void repaintImage() {
		isRepaintImage = true;
	}

	public boolean isRepaintImage() {
		return isRepaintImage;
	}

	@Override
	public void processComponentEvent(ComponentEvent e) {
		super.processComponentEvent(e);

		if (e.getID() == ComponentEvent.COMPONENT_RESIZED || e.getID() == ComponentEvent.COMPONENT_SHOWN) {
			updateOnResizing();
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();

		if (img != null) {
			img.flush();
			img = null;
		}
	}

	public void updateOnResizing() {
		try {
			createImage();
			isRepaintImage = true;
		} catch (Exception exc) {
		}
		repaint();
	}

	private void createImage() throws Exception {
		try {
			if (img != null) {
				img.flush();
				img = null;
			}
			img = createVolatileImage(getWidth(), getHeight(), capabilities);
			return;
		} catch (Exception exc) {
		}
	}

	@Override
	public void paint(Graphics g) {
		boolean contentsLost = false;
		try {
			if (img == null) {
				createImage();
			}

			if (img != null) {
				do {
					int valCode = img.validate(getGraphicsConfiguration());
					if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
						createImage();
					}

					boolean updateImage = isRepaintImage || contentsLost || valCode == VolatileImage.IMAGE_RESTORED;
					Graphics imgG = img.getGraphics();
					if (updateImage) {
						imgG.setClip(0, 0, getWidth(), getHeight());
						imgG.setColor(getBackground());
						imgG.fillRect(0, 0, getWidth(), getHeight());
						paintImage(imgG);

						isRepaintImage = false;
					}

					imgG.dispose();
					g.drawImage(img, 0, 0, this);

					contentsLost = img.contentsLost();
				} while (contentsLost);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void scrollImage(int dx, int dy) {
		if (dx != 0 || dy != 0) {
			if (dx == 0) {
				scrollImageVertical(dy);
			} else if (dy == 0) {
				scrollImageHorizontal(dx);
			} else {
				isRepaintImage = true;
				repaint();
			}
		}
	}

	private void scrollImageVertical(int dy) {
		if (dy == 0) {
			return;
		}

		int w = getWidth();
		int h = getHeight();

		Graphics imgG = img.getGraphics();
		imgG.setColor(getBackground());
		if (dy < 0) {
			imgG.copyArea(0, -dy, w, h + dy, 0, dy);
			imgG.setClip(0, h + dy, w, -dy);
			imgG.fillRect(0, h + dy, w, -dy);
		} else {
			imgG.copyArea(0, 0, w, h - dy, 0, dy);
			imgG.setClip(0, 0, w, dy);
			imgG.fillRect(0, 0, w, dy);
		}

		paintImage(imgG);

		isRepaintImage = false;
		repaint();
	}

	private void scrollImageHorizontal(int dx) {
		if (dx == 0) {
			return;
		}

		int w = getWidth();
		int h = getHeight();

		Graphics imgG = img.getGraphics();
		imgG.setColor(getBackground());
		if (dx < 0) {
			imgG.copyArea(-dx, 0, w + dx, h, dx, 0);
			imgG.setClip(w + dx, 0, -dx, h);
			imgG.fillRect(w + dx, 0, -dx, h);
		} else {
			imgG.copyArea(0, 0, w - dx, h, dx, 0);
			imgG.setClip(0, 0, dx, h);
			imgG.fillRect(0, 0, dx, h);
		}

		paintImage(imgG);

		isRepaintImage = false;
		repaint();
	}

	// --- Here image is painted ---
	public abstract void paintImage(Graphics g);

	public static void main(String[] args) {
		BufferedImagePanel p = new BufferedImagePanel() {
			private static final long serialVersionUID = 1L;
			int x, y, dx, dy;
			{
				setBackground(Color.white);
				addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						x = e.getX();
						y = e.getY();
					}
				});
				addMouseMotionListener(new MouseMotionAdapter() {
					@Override
					public void mouseDragged(MouseEvent e) {
						int DX = e.getX() - x;
						dx += DX;
						int DY = 0; // e.getY() - y;
						dy += DY;
						scrollImage(DX, DY);

						x = e.getX();
						y = e.getY();
					}
				});
			}

			Stroke s = new BasicStroke(1, 0, 0, 1f, new float[] { 5, 5 }, 0);

			@Override
			public void paintImage(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(s);
				g.setColor(Color.black);
				for (int i = 0; i < getHeight(); i += 3) {
					g.drawLine(dx, i + dy, getWidth() + dx, i + dy);
				}
			}
		};
		p.setDoubleBuffered(true);

		JFrame f = new JFrame();
		f.setContentPane(p);
		f.setBounds(100, 100, 800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
