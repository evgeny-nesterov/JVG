package javax.swing.timefield;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

public class JSpinField extends JPanel implements ActionListener, AdjustmentListener, KeyListener {
	private static final long serialVersionUID = 1L;

	private int minimum = 0;

	private int maximum = 0;

	private int value = 0;

	private int increment = 1;

	private JTextField editor;

	private JScrollBar scroller;

	public JSpinField() {
		setLayout(new BorderLayout());
		editor = new JTextField("0");
		editor.addActionListener(this);
		editor.addKeyListener(this);
		scroller = new JScrollBar();
		scroller.setPreferredSize(new Dimension(scroller.getPreferredSize().width, 21));
		scroller.setMinimum(minimum);
		scroller.setMaximum(maximum);
		scroller.setValue(maximum - value);
		scroller.setUnitIncrement(increment);
		scroller.setBlockIncrement(increment);
		scroller.setVisibleAmount(0);
		scroller.setRequestFocusEnabled(false);
		scroller.addAdjustmentListener(this);
		add(editor, BorderLayout.CENTER);
		add(scroller, BorderLayout.EAST);
	}

	protected void setValue(int value, boolean updateEditor, boolean updateScroller) {
		int oldValue = this.value;
		if (value < minimum) {
			this.value = minimum;
		} else if (value > maximum) {
			this.value = maximum;
		} else {
			this.value = value;
		}

		if (updateEditor) {
			editor.setText(Integer.toString(this.value));
		}

		if (updateScroller) {
			scroller.removeAdjustmentListener(this);
			scroller.setValue(maximum - this.value);
			scroller.addAdjustmentListener(this);
		}
		firePropertyChange("value", oldValue, this.value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		setValue(value, true, true);
	}

	public int getMinimum() {
		return minimum;
	}

	public void setMinimum(int minimum) {
		int oldMinimum = this.minimum;
		this.minimum = minimum;
		scroller.removeAdjustmentListener(this);
		scroller.setMinimum(minimum);
		scroller.addAdjustmentListener(this);
		firePropertyChange("minimum", oldMinimum, minimum);
	}

	public int getMaximum() {
		return maximum;
	}

	public void setMaximum(int maximum) {
		int oldMaximum = this.maximum;
		this.maximum = maximum;
		scroller.removeAdjustmentListener(this);
		scroller.setMaximum(maximum);
		scroller.addAdjustmentListener(this);
		firePropertyChange("maximum", oldMaximum, maximum);
	}

	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int increment) {
		int oldIncrement = this.increment;
		this.increment = increment;
		scroller.setUnitIncrement(increment);
		scroller.setBlockIncrement(increment);
		firePropertyChange("increment", oldIncrement, increment);
	}

	public JTextField getEditor() {
		return editor;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setValue(Integer.parseInt(editor.getText()), false, true);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		setValue(maximum - e.getValue(), true, false);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	};

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (value != maximum) {
				setValue(value + increment);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (value != minimum) {
				setValue(value - increment);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	};
}
