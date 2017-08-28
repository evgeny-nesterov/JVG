package javax.swing.timefield;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class JCalendar extends JPanel implements PropertyChangeListener, ActionListener {
	private static final long serialVersionUID = 1L;

	private Locale locale;

	private Calendar calendar;

	private String[] monthNames;

	private String[] dayNames;

	private JPanel panel;

	protected JDayChooser dayChooser;

	JYearChooser dd;

	private JLabel year;

	private JLabel month;

	private ArrowButton ly = new ArrowButton(SwingConstants.WEST, 2, 5);

	private ArrowButton lm = new ArrowButton(SwingConstants.WEST, 1, 5);

	private ArrowButton ry = new ArrowButton(SwingConstants.EAST, 2, 5);

	private ArrowButton rm = new ArrowButton(SwingConstants.EAST, 1, 5);

	// private IconButton btn = new IconButton(Images.getImage("Down.png"));
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("ly")) {
			setYear(calendar.get(Calendar.YEAR) - 1);
		} else if (cmd.equals("ry")) {
			setYear(calendar.get(Calendar.YEAR) + 1);
		} else if (cmd.equals("lm")) {
			setMonth(calendar.get(Calendar.MONTH) - 1);

		} else if (cmd.equals("rm")) {
			setMonth(calendar.get(Calendar.MONTH) + 1);
		}
	}

	public JCalendar() {
		init();

		setLayout(new BorderLayout());
		addPropertyChangeListener(this);
		panel = new JPanel(new FlowLayout());
		panel.setBackground(Color.white);
		ly.setActionCommand("ly");
		ry.setActionCommand("ry");
		lm.setActionCommand("lm");
		rm.setActionCommand("rm");
		ly.addActionListener(this);
		ry.addActionListener(this);
		lm.addActionListener(this);
		rm.addActionListener(this);
		panel.add(ly);
		panel.add(lm);
		Dimension dim = month.getMinimumSize();
		dim.setSize(55, dim.getHeight());
		month.setPreferredSize(dim);
		month.setMinimumSize(dim);
		panel.add(month);
		panel.add(year);
		panel.add(rm);
		panel.add(ry);
		add(panel, BorderLayout.NORTH);
		add(dayChooser, BorderLayout.CENTER);

		dayChooser.requestFocus();
	}

	private void init() {
		locale = Locale.getDefault();
		calendar = Calendar.getInstance(locale);
		DateFormatSymbols dfs = new DateFormatSymbols(locale);
		dayNames = dfs.getShortWeekdays();
		monthNames = dfs.getMonths();

		dayChooser = new JDayChooser();
		month = new JLabel();
		year = new JLabel();

		setDate(calendar.getTime());
		dayChooser.paintDays();
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public void setLocale(Locale l) {
		super.setLocale(l);
		Locale oldLocale = locale;
		locale = l;
		firePropertyChange("locale", oldLocale, l);
	}

	public Date getDate() {
		return calendar.getTime();
	}

	public void setTime(long time) {
		calendar.setTimeInMillis(time);
		dayChooser.setValue(calendar.get(Calendar.DAY_OF_MONTH));
		month.setText(monthNames[calendar.get(Calendar.MONTH)]);
		year.setText(Integer.toString(calendar.get(Calendar.YEAR)));
	}

	public void setDate(Date date) {
		Date oldDate = calendar.getTime();
		calendar.setTime(date);
		dayChooser.setValue(calendar.get(Calendar.DAY_OF_MONTH));
		month.setText(monthNames[calendar.get(Calendar.MONTH)]);
		year.setText(Integer.toString(calendar.get(Calendar.YEAR)));
		firePropertyChange("date", oldDate, date);
	}

	public int getDay() {
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public void setDay(int day) {
		int oldDay = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.add(Calendar.DAY_OF_MONTH, day - oldDay);
		int newDay = calendar.get(Calendar.DAY_OF_MONTH);

		if (day != newDay) {
			day = newDay;
		}

		if (day != dayChooser.getValue()) {
			dayChooser.setValue(day);
		}

		firePropertyChange("day", oldDay, day);
	}

	public int getMonth() {
		return calendar.get(Calendar.MONTH);
	}

	public void setMonth(int month) {
		if (month < 0) {
			setYear(calendar.get(Calendar.YEAR) - 1);
			setMonth(11);
		}
		if (month > 11) {
			setYear(calendar.get(Calendar.YEAR) + 1);
			setMonth(0);
		}
		int oldMonth = calendar.get(Calendar.MONTH);
		calendar.add(Calendar.MONTH, month - oldMonth);
		int newMonth = calendar.get(Calendar.MONTH);

		if (month != newMonth) {
			month = newMonth;
		}
		this.month.setText(monthNames[month]);

		firePropertyChange("month", oldMonth, month);
	}

	public int getYear() {
		return calendar.get(Calendar.YEAR);
	}

	public void setYear(int year) {
		int oldyear = calendar.get(Calendar.YEAR);
		calendar.add(Calendar.YEAR, year - oldyear);
		int newYear = calendar.get(Calendar.YEAR);

		if (year != newYear) {
			year = newYear;
		}
		this.year.setText(Integer.toString(year));
		firePropertyChange("year", oldyear, year);
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName = e.getPropertyName();
		if (propertyName.equals("locale")) {
			Date saveDate = calendar.getTime();
			calendar = Calendar.getInstance((Locale) e.getNewValue());
			calendar.setTime(saveDate);
			init();
		} else if (propertyName.equals("date") || propertyName.equals("month") || propertyName.equals("year")) {
			dayChooser.paintDays();
		}
	}

	private Font currentFont = getFont().deriveFont(1, 12);

	private final static Color selectedColor = new Color(0, 0, 0);

	protected class JDayChooser extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;

		private JButton[] days = new JButton[49];

		private JButton selectedDay;

		private Color dayOfWeekBackground = new Color(128, 128, 128);

		private Color dayOfWeekForeground = new Color(220, 220, 220);

		public JDayChooser() {
			setBackground(Color.white);
			setLayout(new GridLayout(7, 7));
			for (int y = 0; y < 7; y++) {
				for (int x = 0; x < 7; x++) {
					final int index = x + 7 * y;
					days[index] = new JButton() {
						private static final long serialVersionUID = 1L;

						@Override
						public void paintComponent(Graphics g) {
							g.setColor(getBackground());
							g.fillRect(0, 0, getWidth(), getHeight());
							super.paintComponent(g);
						}
					};
					days[index].setOpaque(true);
					days[index].setBorderPainted(false);
					days[index].setContentAreaFilled(false);

					if (y == 0) {
						days[index].setBackground(dayOfWeekBackground);
						days[index].setForeground(dayOfWeekForeground);
					} else {
						days[index].addActionListener(this);
					}
					days[index].setFont(days[index].getFont().deriveFont(0, 11));
					days[index].setMargin(new Insets(0, 0, 0, 0));
					days[index].setMargin(new Insets(0, 0, 0, 0));
					days[index].setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
					days[index].setFocusPainted(false);
					days[index].setRequestFocusEnabled(false);
					add(days[index]);
				}
			}
		}

		protected void paintDays() {
			Calendar today = Calendar.getInstance(locale);
			Calendar tmpCalendar = (Calendar) calendar.clone();
			int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
			int day = firstDayOfWeek;
			int i;
			for (i = 0; i < 7; i++) {
				days[i].setText(dayNames[day]);

				if (day < 7) {
					day++;
				} else {
					day -= 6;
				}
			}

			tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);
			int firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
			if (firstDay < 0) {
				firstDay += 7;
			}
			for (i = 0; i < firstDay; i++) {
				days[i + 7].setVisible(false);
				days[i + 7].setText("");
			}

			tmpCalendar.add(Calendar.MONTH, 1);
			Date firstDayInNextMonth = tmpCalendar.getTime();
			tmpCalendar.add(Calendar.MONTH, -1);
			Date date = tmpCalendar.getTime();
			int n = 0;
			while (date.before(firstDayInNextMonth)) {
				days[i + n + 7].setText(Integer.toString(n + 1));
				days[i + n + 7].setVisible(true);
				if (tmpCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && tmpCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
					days[i + n + 7].setFont(currentFont);
				}

				// current day
				if (n + 1 == calendar.get(Calendar.DAY_OF_MONTH)) {
					days[i + n + 7].setBackground(selectedColor);
					days[i + n + 7].setForeground(Color.white);
					selectedDay = days[i + n + 7];
				} else {
					days[i + n + 7].setBackground(Color.white);
					days[i + n + 7].setForeground(Color.black);
				}

				n++;
				tmpCalendar.add(Calendar.DATE, 1);
				date = tmpCalendar.getTime();
			}

			for (int k = n + i + 7; k < 49; k++) {
				days[k].setVisible(false);
				days[k].setText("");
			}
		}

		protected int getValue() {
			if (selectedDay != null) {
				return Integer.parseInt(selectedDay.getText());
			} else {
				return 1;
			}
		}

		protected void setValue(int value) {
			if (selectedDay != null) {
				selectedDay.setBackground(Color.white);
				selectedDay.setForeground(Color.black);
			}

			for (int i = 7; i < 49; i++) {
				if (days[i].getText().equals(Integer.toString(value))) {
					selectedDay = days[i];
					selectedDay.setBackground(selectedColor);
					selectedDay.setForeground(Color.white);
					break;
				}
			}

			if (value != calendar.get(Calendar.DAY_OF_MONTH)) {
				setDay(value);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setValue(Integer.parseInt(((JButton) e.getSource()).getText()));
			Container parent = getParent();
			while (parent != null) {
				if (parent instanceof JPopupMenu) {
					JPopupMenu pop = (JPopupMenu) parent;
					pop.setVisible(false);
					break;
				}
				parent = parent.getParent();
			}
		}
	}

	protected class JYearChooser extends JSpinField {
		private static final long serialVersionUID = 1L;

		private class NumberDocument extends PlainDocument {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				char[] source = str.toCharArray();
				char[] result = new char[source.length];
				int j = 0;
				for (int i = 0; i < result.length; i++) {
					if (Character.isDigit(source[i])) {
						result[j++] = source[i];
					} else {
						Toolkit.getDefaultToolkit().beep();
					}
				}

				super.insertString(offs, new String(result, 0, j), a);
			}
		}

		public JYearChooser() {
			setMinimum(calendar.getMinimum(Calendar.YEAR));
			setMaximum(calendar.getMaximum(Calendar.YEAR));
			getEditor().setDocument(new NumberDocument());
		}

		@Override
		protected void setValue(int value, boolean updateEditor, boolean updateScroller) {
			super.setValue(value, updateEditor, updateScroller);
			if (value != calendar.get(Calendar.YEAR)) {
				setYear(value);
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Calendar");
		frame.getContentPane().add(new JCalendar());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
