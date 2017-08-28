package ru.nest.jvg;

import java.util.Comparator;

public class ComponentOrderComparator implements Comparator<JVGComponent> {
	@Override
	public int compare(JVGComponent o1, JVGComponent o2) {
		if (o1 == o2) {
			return 0;
		}

		JVGContainer p1 = o1.getParent();
		while (p1 != null) {
			JVGContainer p2 = o2.getParent();
			while (p2 != null) {
				if (p1 == p2) {
					int i1 = p1.getChildIndex(o1);
					int i2 = p1.getChildIndex(o2);
					return i1 - i2;
				}
				p2 = p2.getParent();
			}
			p1 = p1.getParent();
		}

		return 0;
	}
}
