package ru.nest.jvg.transform;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import ru.nest.jvg.shape.JVGShape;

public class JVGTransform {
	private List<JVGTransformElement> transforms = new ArrayList<JVGTransformElement>(1);

	public JVGTransform() {
	}

	public JVGTransform(AffineTransform transform) {
		addTransformation(new JVGAffineTransformElement(transform));
	}

	public void addTransformation(JVGTransformElement transformElement) {
		if (transformElement instanceof JVGAffineTransformElement) {
			AffineTransform transform = ((JVGAffineTransformElement) transformElement).transform;
			if (!transform.isIdentity()) {
				if (transforms.size() > 0) {
					JVGTransformElement last = transforms.get(transforms.size() - 1);
					if (last instanceof JVGAffineTransformElement) {
						JVGAffineTransformElement t = (JVGAffineTransformElement) last;
						t.transform(transform);
						return;
					}
				}
			} else {
				return;
			}
		}
		transforms.add(transformElement);
	}

	public void transform(JVGShape shape, AffineTransform transform) {
		for (JVGTransformElement t : transforms) {
			t.transform(shape, transform);
		}
	}
}
