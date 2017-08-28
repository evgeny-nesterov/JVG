package javax.swing.layout;

public class Bounds {
	public Bounds() {
	}

	public double x, y, w, h;

	@Override
	public String toString() {
		return "[" + x + ", " + y + ", " + w + ", " + h + "]";
	}
}
