package javax.swing.pie;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class DefaultPieModel implements PieModel {
	private List<Piece> pieces = new ArrayList<Piece>();

	public Piece addPiece(Piece p) {
		pieces.add(p);
		return p;
	}

	public void removePiece(Piece p) {
		pieces.remove(p);
	}

	@Override
	public int getCount() {
		return pieces.size();
	}

	public Piece getPiece(int index) {
		return pieces.get(index);
	}

	@Override
	public double getValue(int index) {
		return pieces.get(index).getValue();
	}

	@Override
	public String getName(int index) {
		return pieces.get(index).getName();
	}

	@Override
	public Color getColor(int index) {
		return pieces.get(index).getColor();
	}

	@Override
	public boolean isOpen(int index) {
		return pieces.get(index).isOpen();
	}

	@Override
	public int getIndex(String name) {
		if (name != null) {
			int len = pieces.size();
			for (int i = 0; i < len; i++) {
				Piece p = pieces.get(i);
				if (name.equals(p.getName())) {
					return i;
				}
			}
		}
		return -1;
	}
}
