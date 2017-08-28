package javax.swing.dock;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

public class DockCursors {
	public final static Cursor topCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(DockCursors.class.getResource("img/top_cursor.png")).getImage(), new Point(5, 0), "top");

	public final static Cursor leftCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(DockCursors.class.getResource("img/left_cursor.png")).getImage(), new Point(0, 5), "left");

	public final static Cursor bottomCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(DockCursors.class.getResource("img/bottom_cursor.png")).getImage(), new Point(5, 10), "bottom");

	public final static Cursor rightCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(DockCursors.class.getResource("img/right_cursor.png")).getImage(), new Point(10, 5), "right");

	public final static Cursor tabCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(DockCursors.class.getResource("img/tab_cursor.png")).getImage(), new Point(7, 7), "tab");

	public final static Cursor horInsertCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(DockCursors.class.getResource("img/hor_insert_cursor.png")).getImage(), new Point(13, 7), "hor insert");

	public final static Cursor verInsertCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(DockCursors.class.getResource("img/ver_insert_cursor.png")).getImage(), new Point(7, 13), "ver Insert");
}
