package ru.nest.xmleditor;

import java.awt.Color;
import java.awt.HeadlessException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BasicTextEditor;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class XMLEditor extends BasicTextEditor implements DocumentListener, XMLHandler, Runnable {
	public XMLEditor() {
		super();

		StyleConstants.setForeground(defaultAttr, Color.black);
		StyleConstants.setItalic(defaultAttr, false);
		StyleConstants.setBold(defaultAttr, false);

		StyleConstants.setForeground(tagAttr, new Color(0, 0, 80));
		StyleConstants.setBold(tagAttr, true);

		StyleConstants.setForeground(servCharAttr, Color.black);

		StyleConstants.setForeground(paramNameAttr, new Color(150, 60, 30));
		StyleConstants.setItalic(paramNameAttr, true);

		StyleConstants.setForeground(paramValueAttr, new Color(90, 90, 220));
		StyleConstants.setForeground(textAttr, Color.black);
		StyleConstants.setForeground(commentAttr, new Color(50, 200, 50));

		StyleConstants.setBackground(errorAttr, new Color(255, 150, 150));

		getDocument().addDocumentListener(this);
	}

	private Thread thread = null;

	@Override
	public void addNotify() {
		super.addNotify();

		thread = new Thread(this);
		thread.start();

		updater = new Updater();
		updater.start();
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		thread = null;
		updater = null;
	}

	private SimpleAttributeSet defaultAttr = new SimpleAttributeSet();

	private SimpleAttributeSet tagAttr = new SimpleAttributeSet();

	private SimpleAttributeSet servCharAttr = new SimpleAttributeSet();

	private SimpleAttributeSet paramNameAttr = new SimpleAttributeSet();

	private SimpleAttributeSet paramValueAttr = new SimpleAttributeSet();

	private SimpleAttributeSet textAttr = new SimpleAttributeSet();

	private SimpleAttributeSet commentAttr = new SimpleAttributeSet();

	private SimpleAttributeSet errorAttr = new SimpleAttributeSet();

	// document listener
	@Override
	public void insertUpdate(DocumentEvent e) {
		// addRequest(e.getOffset(), e.getLength(), defaultAttr, true);
		update();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		update();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	// xml handler
	@Override
	public void serviceChar(int offset) {
		addRequest(offset, 1, servCharAttr, true);
	}

	@Override
	public void tag(boolean open, int offset, int length) {
		addRequest(offset, length, tagAttr, true);
	}

	@Override
	public void spaces(int offset, int length) {
		addRequest(offset, length, defaultAttr, true);
	}

	@Override
	public void attributeName(int offset, int length) {
		addRequest(offset, length, paramNameAttr, true);
	}

	@Override
	public void attributeValue(int offset, int length) {
		addRequest(offset, length, paramValueAttr, true);
	}

	@Override
	public void comment(int offset, int length) {
		addRequest(offset, length, commentAttr, true);
	}

	@Override
	public void text(int offset, int length) {
		addRequest(offset, length, textAttr, true);
	}

	@Override
	public void cdataName(int offset, int length) {
		addRequest(offset, length, tagAttr, true);
	}

	@Override
	public void cdata(int offset, int length) {
		addRequest(offset, length, textAttr, true);
	}

	@Override
	public void instructionTag(int offset, int length) {
		addRequest(offset, length, tagAttr, true);
	}

	@Override
	public void error(String message, int offset, int length) throws XMLParseException {
		addRequest(offset, length, errorAttr, false);
	}

	@Override
	public void startParseXML() {
	}

	@Override
	public void stopParseXML() {
	}

	private List<Request> requests = new ArrayList<>();

	private void addRequest(int offset, int length, AttributeSet attr, boolean replace) {
		Request request = getRequest();
		synchronized (requests) {
			request.set(offset, length, attr, replace);
			requests.add(request);
			requests.notify();
		}
	}

	@Override
	public void run() {
		while (thread != null) {
			Request request = null;
			synchronized (requests) {
				if (requests.size() > 0) {
					request = requests.remove(0);
				} else {
					try {
						requests.wait(1000);
					} catch (InterruptedException exc) {
					}
				}
			}

			if (request != null) {
				getStyledDocument().setCharacterAttributes(request.offset, request.length, request.attr, true);
				putRequest(request);
			}
		}
	}

	private ArrayList<Request> cache = new ArrayList<>();

	private Request getRequest() {
		synchronized (cache) {
			if (cache.size() > 0) {
				return cache.remove(0);
			} else {
				return new Request();
			}
		}
	}

	private void putRequest(Request request) {
		synchronized (cache) {
			cache.add(request);
		}
	}

	private void putRequests(List<Request> requests) {
		synchronized (cache) {
			cache.addAll(requests);
		}
	}

	class Request {
		public void set(int offset, int length, AttributeSet attr, boolean replace) {
			this.offset = offset;
			this.length = length;
			this.attr = attr;
			this.replace = replace;
		}

		int offset;

		int length;

		AttributeSet attr;

		boolean replace;
	}

	// ---
	private ArrayList<Boolean> update = new ArrayList<>();

	private Updater updater = null;

	class Updater extends Thread {
		@Override
		public void run() {
			while (updater != null) {
				boolean isUpdate;
				synchronized (update) {
					isUpdate = update.size() > 0;
					if (isUpdate) {
						int time = 0;
						int totalTime = 0;
						while (time < 100 && totalTime < 400) {
							if (update.size() > 0) {
								update.clear();
								time = 0;
							}

							try {
								update.wait(10);
								time += 10;
								totalTime += 10;
							} catch (InterruptedException exc) {
							}
						}
					} else {
						try {
							update.wait(1000);
						} catch (InterruptedException exc) {
						}
					}
				}

				// update if need
				if (isUpdate) {
					synchronized (requests) {
						putRequests(requests);
						requests.clear();
					}

					try {
						TextInputStream is = new TextInputStream();
						XMLParser parser = new XMLParser(is, XMLEditor.this);
						parser.parse();
						error = null;
					} catch (XMLParseException exc) {
						addRequest(exc.getOffset(), exc.getLength(), errorAttr, false);
						error = exc;
					}
				}
			}
		}
	}

	public void update() {
		synchronized (update) {
			update.add(Boolean.TRUE);
			update.notify();
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		if (!"style change".equals(e.getEdit().getPresentationName())) {
			super.undoableEditHappened(e);
		}
	}

	private XMLParseException error = null;

	public XMLParseException getError() {
		return error;
	}

	class TextInputStream extends InputStream {
		private StyledDocument doc = getStyledDocument();

		private Segment text = new Segment();

		private int desiredLength;

		private int length;

		private int offset = 0;

		public TextInputStream() {
			length = doc.getLength();
			desiredLength = length;
			text.setPartialReturn(true);
		}

		private void next() {
			try {
				doc.getText(offset, desiredLength, text);
				desiredLength -= text.count;
				text.first();
			} catch (BadLocationException exc) {
				exc.printStackTrace();
			}
		}

		@Override
		public int read() {
			if (offset < length) {
				if (text.current() == CharacterIterator.DONE) {
					next();
				}

				char c = text.current();
				text.next();
				offset++;
				return c;
			} else {
				return -1;
			}
		}
	}

	public void setText(URL url) {
		try {
			InputStream is = url.openStream();
			setText(is);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	public void setText(InputStream is) {
		try {
			getEditorKit().read(is, getDocument(), 0);
		} catch (BadLocationException exc) {
			exc.printStackTrace();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public void setText(String text) {
		super.setText(text);
	}

	public static void main(String[] srgs) {
		try {
			XMLEditor e = new XMLEditor();
			e.setText(XMLParser.class.getResourceAsStream("/ru/nest/jvg/xml/test.xml"));
			e.activateUndoRedo();

			JFrame f = new JFrame();
			f.setBounds(200, 100, 800, 600);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setContentPane(new JScrollPane(e));
			f.setVisible(true);
		} catch (HeadlessException exc) {
			exc.printStackTrace();
		}
	}
}
