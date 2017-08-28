package javax.swing.multidivider;

public interface MultiDividerModel {
	public double getStartValue();

	public int getCount();

	public double getLength(int index);

	public void setLength(double value, int index);

	public void addListener(MultiDividerListener listener);

	public void removeListener(MultiDividerListener listener);

	public void fireStructureChanged();

	public void firePositionsChanged();

	public void fireDividerIndexChanged(int index);
}
