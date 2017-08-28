package javax.swing.multidivider;

public interface MultiDividerListener {
	public void structureChanged();

	public void positionsChanged();

	public void dividerIndexChanged(int index);
}
