package satis.iface.graph;

public interface DinamicPlotModel extends PlotModel {
	public int getMarkerIndex();

	public double getMarkerX();

	public double getMarkerY();

	public double getRealMarkerX();

	public double getRealMarkerY();

	public boolean isDrawMarker();

	public void setDrawMarker(boolean isDrawMarker);

	public void proectMarker(int markerX, int markerY, int screenWidth, int screenHeight, boolean isUpdate);
}
