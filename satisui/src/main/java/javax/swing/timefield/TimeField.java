package javax.swing.timefield;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

@SuppressWarnings("serial")
public class TimeField extends JSpinner implements PropertyChangeListener {
	private JCalendar calendarPane = new JCalendar();

	private boolean showTime;

	public TimeField(long time) {
		super(new SpinnerDateModel());
		init(time, true);
	}

	public TimeField(long time, boolean showTime) {
		super(new SpinnerDateModel());
		init(time, showTime);
	}

	public TimeField(long time, String format) {
		super(new SpinnerDateModel());
		init(time, format);
	}

	private void init(long time, boolean showTime) {
		this.showTime = showTime;

		String format;
		if (showTime) {
			format = "yyyy/MM/dd  HH:mm:ss";
		} else {
			format = "yyyy/MM/dd";
		}
		init(time, format);
	}

	private int mousePos = -1;

	private void init(long time, String format) {
		Border border = BorderFactory.createLineBorder(new Color(220, 220, 220));
		calendarPane.setBorder(border);
		calendarPane.addPropertyChangeListener(this);

		final JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(this, format);
		setEditor(timeEditor);
		final JFormattedTextField txt = (JFormattedTextField) timeEditor.getComponent(0);
		txt.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat(format)) {
			public void install(JFormattedTextField ftf) {
				super.install(ftf);
				ftf.setCaretPosition(mousePos != -1 ? mousePos : 8);
			}
		}));
		txt.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					int pos = txt.viewToModel(e.getPoint());
					txt.setCaretPosition(pos);
				}
			}

			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					mousePos = txt.viewToModel(e.getPoint());
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					mousePos = -1;
				}
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
					showPopup();
				}
			}
		});

		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				long time = getTime();
				calendarPane.firePropertyChange("time", 0, time);
			}
		});

		setTime(time);
	}

	public void setTime(long time) {
		long oldTime = getTime();
		setValue(new Date(time));
		calendarPane.firePropertyChange("time", oldTime, time);
	}

	public Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance(getLocale());
		calendar.setTimeInMillis(getTime());
		return calendar;
	}

	public long getTime() {
		if (showTime) {
			return ((Date) getValue()).getTime();
		} else {
			return getDate();
		}
	}

	public long getDate() {
		Date time = (Date) getValue();
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTimeInMillis();
	}

	JPopupMenu popup = null;

	public void showPopup() {
		calendarPane.setTime(getTime());
		if (popup == null) {
			popup = new JPopupMenu();
			popup.setLayout(new BorderLayout());
			popup.add(calendarPane, BorderLayout.CENTER);
		}
		popup.show(this, 0, getHeight());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("date") || evt.getPropertyName().equals("day") || evt.getPropertyName().equals("month") || evt.getPropertyName().equals("year")) {
			Calendar calendar = getCalendar();
			calendar.set(calendarPane.getYear(), calendarPane.getMonth(), calendarPane.getDay());
			setValue(calendar.getTime());
		}
	}

	public JCalendar getCalendarComponent() {
		return calendarPane;
	}

	public static void main(String[] s) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new FlowLayout());
		f.getContentPane().add(new TimeField(System.currentTimeMillis()), BorderLayout.CENTER);
		f.getContentPane().add(new TimeField(System.currentTimeMillis()), BorderLayout.CENTER);
		f.pack();
		f.setLocation(700, 300);
		f.setVisible(true);
	}
}
