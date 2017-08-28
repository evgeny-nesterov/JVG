package javax.swing.pie;

import java.awt.Color;

public interface PieModel {
	public int getCount();

	public double getValue(int index);

	public String getName(int index);

	public int getIndex(String name);

	public Color getColor(int index);

	public boolean isOpen(int index);
}
