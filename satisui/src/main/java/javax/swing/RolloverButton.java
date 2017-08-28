package javax.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class RolloverButton extends JButton implements MouseListener {
	private static final long serialVersionUID = 1L;

	private String toolTip;

	private String imagePath;

	private String selectImagePath;

	private ImageIcon image;

	private ImageIcon selectImage;

	private boolean selectionEnabled;

	private static Insets buttonInsets;

	/**
	 * Creates a new RolloverButton with an associated image and tool tip.
	 * 
	 * @param img
	 *            the ImageIcon to be displayed on the button
	 * @param tip
	 *            the button's tool tip
	 */
	public RolloverButton(ImageIcon img, String tip) {
		super(img);
		toolTip = tip;
		init();
	}

	/**
	 * Creates a new RolloverButton with an associated image and tool tip.
	 * 
	 * @param a
	 *            the Action to be associated with this button
	 * @param tip
	 *            the button's tool tip
	 */
	public RolloverButton(Action a, String tip) {
		super(a);
		toolTip = tip;
		init();
	}

	/**
	 * Creates a new RolloverButton with an associated image and tool tip.
	 * 
	 * @param label
	 *            the button's label
	 * @param tip
	 *            the button's tool tip
	 */
	public RolloverButton(String label, String tip, int h, int w) {
		super(label);
		toolTip = tip;
		init();
		setButtonSize(h, w);
	}

	/**
	 * Creates a new RolloverButton with an associated image and tool tip.
	 * 
	 * @param label
	 *            the button's label
	 * @param tip
	 *            the button's tool tip
	 */
	public RolloverButton(ImageIcon img, String tip, int h, int w) {
		super(img);
		toolTip = tip;
		init();
		setButtonSize(h, w);
	}

	/**
	 * Creates a new RolloverButton with an associated image and tool tip.
	 * 
	 * @param imgPath
	 *            the path relative to this class of the button icon image
	 * @param tip
	 *            the button's tool tip
	 */
	public RolloverButton(String imgPath, String tip) {
		this(imgPath, tip, -1);
	}

	/**
	 * Creates a new RolloverButton with an associated image and tool tip.
	 * 
	 * @param imgPath
	 *            the path relative to this class of the button icon image
	 * @param tip
	 *            the button's tool tip
	 */
	public RolloverButton(String imgPath, String tip, int size) {
		super();
		setButtonIcon(imgPath);
		selectImagePath = null;
		toolTip = tip;
		init();
	}

	public RolloverButton() {
		init();
	}

	static {
		buttonInsets = new Insets(1, 1, 1, 1);
	}

	/**
	 * Initialises the state of the button.
	 */
	private void init() {
		selectionEnabled = true;
		setMargin(buttonInsets);
		setToolTipText(toolTip);
		setBorderPainted(false);
		setContentAreaFilled(false);
		addMouseListener(this);
	}

	/**
	 * Resets the buttons rollover state.
	 */
	public void reset() {
		mouseOver = false;
		setBorderPainted(false);
		setContentAreaFilled(false);
	}

	private void setButtonSize(int height, int width) {
		setPreferredSize(new Dimension(width, height));
		setMaximumSize(new Dimension(width, height));
	}

	/**
	 * Sets the image associated with the button.
	 * 
	 * @param path
	 *            the path relative to this class of the button icon image
	 */
	public void setButtonIcon(String path) {
		image = new ImageIcon(RolloverButton.class.getResource(path));
		setIcon(image);
	}

	public void enableSelectionRollover(boolean enable) {
		selectionEnabled = enable;
	}

	public boolean isSelectionRolloverEnabled() {
		return selectionEnabled;
	}

	/** indicates a current rollover */
	private boolean mouseOver;

	/**
	 * Paints the button's borders as the mouse pointer enters.
	 * 
	 * @param e
	 *            the MouseEvent that created this event
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		if (isEnabled() && isSelectionRolloverEnabled()) {
			mouseOver = true;
			setBorderPainted(true);
			setContentAreaFilled(true);
		}
	}

	/**
	 * Override the <code>isFocusable()</code> method of <code>Component</code> (JDK1.4) to return false so the button never maintains the focus.
	 * 
	 * @return false
	 */
	@Override
	public boolean isFocusable() {
		return false;
	}

	/**
	 * Sets the button's borders unpainted as the mouse pointer exits.
	 * 
	 * @param e
	 *            the MouseEvent that created this event
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		mouseOver = false;
		setBorderPainted(false);
		setContentAreaFilled(false);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public JToolTip createToolTip() {
		JToolTip tip = new PaintedButtonToolTip();
		tip.setComponent(this);
		return tip;
	}

	class PaintedButtonToolTip extends JToolTip {
		private static final long serialVersionUID = 1L;

		@Override
		public void updateUI() {
			setUI(new AcceleratorToolTipUI());
		}
	}
}
