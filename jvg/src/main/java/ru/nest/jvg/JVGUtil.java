package ru.nest.jvg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.nest.jvg.shape.JVGShape;

public class JVGUtil {
	public static void transform(JVGComponent[] components, AffineTransform transform) {
		if (components != null) {
			HashSet<JVGShape> shapes = new HashSet<>();
			getShapes(components, shapes);
			transform(shapes, transform);
		}
	}

	public static void transform(HashSet<JVGShape> shapes, AffineTransform transform) {
		HashSet<JVGShape> locked = new HashSet<>();
		transform(shapes, transform, locked);
		unlock(locked);
	}

	public static void transform(HashSet<JVGShape> shapes, AffineTransform transform, HashSet<JVGShape> locked) {
		if (shapes.size() > 0 && !transform.isIdentity()) {
			getRoots(shapes);

			for (JVGShape shapeComponent : shapes) {
				shapeComponent.transform(transform, locked);
			}
		}
	}

	public static void transform(JVGComponent[] components, AffineTransform[] transforms) {
		if (components != null) {
			HashMap<JVGComponent, AffineTransform> map = new HashMap<>();
			for (int i = 0; i < components.length; i++) {
				map.put(components[i], transforms[i]);
			}
			transform(map);
		}
	}

	public static void transform(HashMap<JVGComponent, AffineTransform> components) {
		// transform components
		HashSet<JVGShape> shapes = new HashSet<>();
		HashSet<JVGShape> locked = new HashSet<>();
		for (JVGComponent component : components.keySet()) {
			if (component instanceof JVGShape) {
				JVGShape shapeComponent = (JVGShape) component;
				AffineTransform transform = components.get(component);
				shapeComponent.transformComponent(transform, locked);
			}

			// transform childs
			if (component instanceof JVGContainer) {
				AffineTransform transform = components.get(component);
				JVGContainer container = (JVGContainer) component;

				shapes.clear();
				getShapes(container.children, 0, container.childrenCount, shapes, components);
				transform(shapes, transform, locked);
			}
		}

		// unlock components
		unlock(locked);
	}

	public static void unlock(Set<JVGShape> locked) {
		for (JVGShape s : locked) {
			s.unlock();
		}
	}

	public static void getShapes(JVGComponent[] components, Set<JVGShape> shapes) {
		getShapes(components, shapes, null);
	}

	public static void getShapes(JVGComponent[] components, Set<JVGShape> shapes, Map<?, AffineTransform> exclude) {
		if (components != null) {
			getShapes(components, 0, components.length, shapes, exclude);
		}
	}

	public static void getShapes(JVGComponent[] components, int start, int end, Set<JVGShape> shapes, Map<?, AffineTransform> exclude) {
		if (components != null) {
			for (int i = start; i < end; i++) {
				JVGComponent component = components[i];
				if (exclude == null || !exclude.containsKey(component)) {
					if (component instanceof JVGShape) {
						JVGShape shape = (JVGShape) component;
						shapes.add(shape);
					}

					if (component instanceof JVGContainer) {
						JVGContainer container = (JVGContainer) component;
						getShapes(container.children, 0, container.childrenCount, shapes, exclude);
					}
				}
			}
		}
	}

	public static void transformSelection(JVGPane pane, AffineTransform transform) {
		JVGSelectionModel selectionModel = pane.getSelectionManager();
		if (selectionModel != null) {
			transform(selectionModel.getSelection(), transform);
		}
	}

	public static void getRoots(Map<? extends JVGComponent, ?> components) {
		Iterator<? extends JVGComponent> iter = components.keySet().iterator();
		while (iter.hasNext()) {
			JVGComponent c = iter.next();
			JVGContainer p = c.parent;
			while (p != null) {
				if (components.containsKey(p)) {
					iter.remove();
					break;
				}
				p = p.parent;
			}
		}
	}

	public static void getRoots(Collection<? extends JVGComponent> components) {
		Iterator<? extends JVGComponent> iter = components.iterator();
		while (iter.hasNext()) {
			JVGComponent c = iter.next();
			JVGContainer p = c.parent;
			while (p != null) {
				if (components.contains(p)) {
					iter.remove();
					break;
				}
				p = p.parent;
			}
		}
	}

	public static JVGComponent[] getRoots(JVGComponent[] components) {
		HashSet<JVGComponent> roots = toSet(components);
		getRoots(roots);
		return toArray(roots);
	}

	public static JVGComponent[] getRootsSaveOrder(JVGComponent[] components) {
		ArrayList<JVGComponent> roots = toArrayList(components);
		getRoots(roots);
		return toArray(roots);
	}

	public static HashMap<JVGComponent, JVGComponent> toMap(JVGComponent[] components) {
		HashMap<JVGComponent, JVGComponent> map = new HashMap<>();
		for (int i = 0; i < components.length; i++) {
			JVGComponent component = components[i];
			map.put(component, component);
		}
		return map;
	}

	public static <V> HashSet<V> toSet(V[] components) {
		HashSet<V> set = new HashSet<>();
		for (int i = 0; i < components.length; i++) {
			V component = components[i];
			set.add(component);
		}
		return set;
	}

	public static <V> ArrayList<V> toArrayList(V[] components) {
		ArrayList<V> list = new ArrayList<>();
		for (int i = 0; i < components.length; i++) {
			V component = components[i];
			list.add(component);
		}
		return list;
	}

	public static JVGComponent[] toArray(Map<JVGComponent, Object> map) {
		JVGComponent[] components = new JVGComponent[map.size()];
		int i = 0;
		for (JVGComponent c : map.keySet()) {
			components[i++] = c;
		}
		return components;
	}

	public static JVGComponent[] toArray(Collection<JVGComponent> set) {
		JVGComponent[] components = new JVGComponent[set.size()];
		int i = 0;
		for (JVGComponent c : set) {
			components[i++] = c;
		}
		return components;
	}

	public static BufferedImage getGridImage(Color color, BasicStroke strokeEven, BasicStroke strokeOdd, int increment) {
		float[] dash = strokeEven.getDashArray();
		int dashlen = 0;
		for (int i = 0; i < dash.length; i++) {
			dashlen += dash[i];
		}

		int size = dashlen * increment;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(color);
		for (int i = 0; i < size; i += increment) {
			if ((i / increment) % 2 == 0) {
				g.setStroke(strokeEven);
			} else {
				g.setStroke(strokeOdd);
			}
			g.drawLine(i, 0, i, size);
			g.drawLine(0, i, size, i);
		}
		g.dispose();
		return img;
	}

	public static <V extends JVGComponent> V[] getComponents(Class<V> clazz, JVGComponent[] components) {
		V[] c = null;
		int count = 0;
		for (JVGComponent component : components) {
			if (clazz.isAssignableFrom(component.getClass())) {
				count++;
			}
		}

		if (count > 0) {
			c = (V[]) Array.newInstance(clazz, count);
			int index = 0;
			for (JVGComponent component : components) {
				if (clazz.isAssignableFrom(component.getClass())) {
					c[index++] = (V) component;
				}
			}
		}

		return c;
	}

	public static <C extends JVGComponent> Rectangle2D getBounds(C[] shapes) {
		Rectangle2D totalBounds = null;
		for (C shape : shapes) {
			Rectangle2D bounds = shape.getRectangleBounds();
			if (totalBounds == null) {
				totalBounds = (Rectangle2D) bounds.clone();
			} else {
				Rectangle2D.union(totalBounds, bounds, totalBounds);
			}
		}
		return totalBounds;
	}

	public static JVGComponent[] find(JVGContainer root, String pattern, boolean matchCase, boolean wholeWord, boolean regexp) {
		List<JVGComponent> result = new ArrayList<>();
		find(root, pattern, matchCase, wholeWord, regexp, result);

		JVGComponent[] array = new JVGComponent[result.size()];
		result.toArray(array);
		return array;
	}

	public static void find(JVGContainer root, String pattern, boolean matchCase, boolean wholeWord, boolean regexp, List<JVGComponent> result) {
		if (!regexp) {
			find(root, pattern, matchCase, wholeWord, result);
		} else {
			findByRegexp(root, pattern, matchCase, wholeWord, result);
		}
	}

	public static void find(JVGContainer root, String pattern, boolean matchCase, boolean wholeWord, List<JVGComponent> result) {
		int childs_count = root.childrenCount;
		JVGComponent childs[] = root.children;
		for (int i = childs_count - 1; i >= 0; i--) {
			JVGComponent c = childs[i];
			String name = c.getName();
			if (name != null) {
				if (!matchCase) {
					name = name.toLowerCase();
					pattern = pattern.toLowerCase();
				}

				if ((!wholeWord && name.indexOf(pattern) >= 0) || (wholeWord && name.equals(pattern))) {
					result.add(c);
				}
			}

			if (c instanceof JVGContainer) {
				find((JVGContainer) c, pattern, matchCase, wholeWord, result);
			}
		}
	}

	public static JVGComponent findByID(JVGContainer root, Long id) {
		int childs_count = root.childrenCount;
		JVGComponent childs[] = root.children;
		for (int i = childs_count - 1; i >= 0; i--) {
			JVGComponent c = childs[i];
			Long curId = c.getId();
			if (id.equals(curId)) {
				return c;
			}

			if (c instanceof JVGContainer) {
				JVGComponent result = findByID((JVGContainer) c, id);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	public static void findByRegexp(JVGContainer root, String pattern, boolean matchCase, boolean wholeWord, List<JVGComponent> result) {
		Pattern p = Pattern.compile(pattern, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
		find(root, p, matchCase, wholeWord, result);
	}

	private static void find(JVGContainer root, Pattern pattern, boolean matchCase, boolean wholeWord, java.util.List<JVGComponent> result) {
		int childs_count = root.childrenCount;
		JVGComponent childs[] = root.children;
		for (int i = childs_count - 1; i >= 0; i--) {
			JVGComponent c = childs[i];
			String name = c.getName();
			if (name != null) {
				Matcher matcher = pattern.matcher(name);
				if (matcher.find()) {
					if (!matchCase) {
						name = name.toLowerCase();
					}

					if (!wholeWord || (wholeWord && matcher.start() == 0 && matcher.end() == name.length())) {
						result.add(c);
					}
				}
			}

			if (c instanceof JVGContainer) {
				find((JVGContainer) c, pattern, matchCase, wholeWord, result);
			}
		}
	}

	public <C extends JVGComponent> void getAllComponents(JVGComponent[] components, Collection<C> result, Class<C> type) {
		if (components != null) {
			int size = components.length;
			for (int i = 0; i < size; i++) {
				JVGComponent component = components[i];
				if (type.isAssignableFrom(component.getClass())) {
					result.add((C) component);
				}

				if (component instanceof JVGContainer) {
					JVGContainer container = (JVGContainer) component;
					getAllComponents(container.getChildren(), result, type);
				}
			}
		}
	}
}
