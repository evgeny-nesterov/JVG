package ru.nest.toi;

import java.awt.geom.PathIterator;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import ru.nest.toi.objects.TOIArrowPathElement;
import ru.nest.toi.objects.TOIGroup;
import ru.nest.toi.objects.TOIImage;
import ru.nest.toi.objects.TOIMultiArrowPath;
import ru.nest.toi.objects.TOIPath;
import ru.nest.toi.objects.TOIText;
import ru.nest.toi.objects.TOITextPath;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class TOIBuilder {
	private TOIFactory factory;

	public TOIBuilder(TOIFactory factory) {
		this.factory = factory;
	}

	public List<TOIObject> load(String doc) throws Exception {
		return load(new StringReader(doc));
	}

	public List<TOIObject> load(Reader r) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(r);
		return load(doc.getRootElement());
	}

	public List<TOIObject> load(Element e) throws Exception {
		List<Element> list = e.getChildren();
		return load(list);
	}

	public List<TOIObject> load(List<Element> list) throws Exception {
		List<TOIObject> objects = new ArrayList<>();
		for (Element p : list) {
			String objectType = p.getName();
			TOIObject o = null;
			if ("path".equals(objectType)) {
				TOIPath path = null;
				String pathType = p.getAttributeValue("type");
				if ("arrow".equals(pathType)) {
					TOIMultiArrowPath arrowpath = factory.create(TOIMultiArrowPath.class);
					parseCommon(arrowpath, p);
					path = arrowpath;

					// arrow size
					String arrowSizesValue = p.getAttributeValue("sizes");
					if (arrowSizesValue != null) {
						String[] arrowSizes = arrowSizesValue.split(";");
						if (arrowSizes.length == 3) {
							try {
								double width = Double.parseDouble(arrowSizes[0]);
								double arrowWidth = Double.parseDouble(arrowSizes[1]);
								double arrowLength = Double.parseDouble(arrowSizes[2]);
								arrowpath.setWidth(width);
								arrowpath.setArrowWidth(arrowWidth);
								arrowpath.setArrowLength(arrowLength);
							} catch (Exception exc) {
							}
						}
					}

					// elements
					String elementsValue = p.getAttributeValue("elements");
					String[] elements = elementsValue.split(" ");
					for (int j = 0; j < elements.length; j += 2) {
						Double percentPos = null;
						try {
							percentPos = Double.parseDouble(elements[j]);
						} catch (Exception exc) {
						}

						if (percentPos != null) {
							TOIArrowPathElement element = arrowpath.addElement(percentPos);
							try {
								element.setId(Long.parseLong(elements[j + 1]));
							} catch (Exception exc) {
							}
						}
					}
				} else if ("singlearrow".equals(pathType)) {
					TOIArrowPathElement arrowpath = factory.create(TOIArrowPathElement.class);
					parseCommon(arrowpath, p);
					path = arrowpath;

					// arrow size
					String arrowSizesValue = p.getAttributeValue("sizes");
					if (arrowSizesValue != null) {
						String[] arrowSizes = arrowSizesValue.split(";");
						if (arrowSizes.length == 3) {
							try {
								double width = Double.parseDouble(arrowSizes[0]);
								double arrowWidth = Double.parseDouble(arrowSizes[1]);
								double arrowLength = Double.parseDouble(arrowSizes[2]);
								arrowpath.setWidth(width);
								arrowpath.setArrowWidth(arrowWidth);
								arrowpath.setArrowLength(arrowLength);
							} catch (Exception exc) {
							}
						}
					}

					// direction
					String directionValue = p.getAttributeValue("direction");
					if ("back".equals(directionValue)) {
						arrowpath.setDirection(TOIArrowPathElement.DIRECTION_BACK);
					} else if ("both".equals(directionValue)) {
						arrowpath.setDirection(TOIArrowPathElement.DIRECTION_BOTH);
					} else if ("none".equals(directionValue)) {
						arrowpath.setDirection(TOIArrowPathElement.DIRECTION_NONE);
					}
				} else if ("text".equals(pathType)) {
					TOITextPath textpath = factory.create(TOITextPath.class);
					parseCommon(textpath, p);
					path = textpath;
				}

				// geom
				String geomValue = p.getAttributeValue("geom");
				String[] geom = geomValue.split(" ");
				for (int j = 0; j < geom.length; j++) {
					if (geom[j].equals("M") && j + 2 < geom.length) {
						path.getPath().moveTo(Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]));
					} else if (geom[j].equals("L") && j + 2 < geom.length) {
						path.getPath().lineTo(Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]));
					} else if (geom[j].equals("Q") && j + 4 < geom.length) {
						path.getPath().quadTo(Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]));
					} else if (geom[j].equals("C") && j + 6 < geom.length) {
						path.getPath().curveTo(Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]), Double.parseDouble(geom[++j]));
					} else if (geom[j].equals("X")) {
						path.getPath().closePath();
					}
				}

				o = path;
			} else if ("text".equals(objectType)) {
				TOIText text = factory.create(TOIText.class);
				parseCommon(text, p);
				o = text;
			} else if ("img".equals(objectType)) {
				byte[] data = new BASE64Decoder().decodeBuffer(p.getText());
				String descr = p.getAttributeValue("descr");

				TOIImage image = factory.create(TOIImage.class);
				parseCommon(image, p);
				image.setData(data, descr);
				o = image;
			} else if ("group".equals(objectType)) {
				TOIGroup group = factory.create(TOIGroup.class);
				parseCommon(group, p);
				group.setCombinePathes("yes".equals(p.getAttributeValue("combine")));
				group.setObjects(load(p.getChildren()));
				o = group;
			}
			objects.add(o);
		}
		return objects;
	}

	private void parseCommon(TOIObject o, Element p) {
		try {
			o.setId(Long.parseLong(p.getAttributeValue("id")));
		} catch (Exception exc) {
		}
		o.setText(p.getText());
		o.transform(TOIUtil.getTransform(p.getAttributeValue("transform")));
		o.setFont(TOIUtil.getFont(p.getAttributeValue("font")));
		o.setColor(TOIUtil.getColor(p.getAttributeValue("color")));
	}

	public String export(List<TOIObject> objects) throws Exception {
		StringWriter w = new StringWriter();
		export(w, objects);
		return w.toString();
	}

	public String export(TOIObject object) throws Exception {
		List<TOIObject> objects = new ArrayList<>();
		objects.add(object);

		StringWriter w = new StringWriter();
		export(w, objects);
		return w.toString();
	}

	public void export(Writer w, List<TOIObject> objects) throws Exception {
		Element rootElement = new Element("toi");
		export(rootElement, objects);

		Document doc = new Document(rootElement);
		XMLOutputter out = new XMLOutputter();
		out.output(doc, w);
		w.flush();
		w.close();
	}

	public void export(Element rootElement, List<TOIObject> objects) {
		for (TOIObject o : objects) {
			Element objectElement = null;
			if (o instanceof TOIPath) {
				TOIPath p = (TOIPath) o;
				objectElement = new Element("path");
				rootElement.addContent(objectElement);

				// type
				if (o instanceof TOIMultiArrowPath) {
					TOIMultiArrowPath a = (TOIMultiArrowPath) o;
					objectElement.setAttribute("type", "arrow");

					// arrow sizes
					if (a.getWidth() != 14 || a.getArrowWidth() != 12 || a.getArrowLength() != 6) {
						objectElement.setAttribute("sizes", a.getWidth() + ";" + a.getArrowWidth() + ";" + a.getArrowLength());
					}

					// elements
					String elements = "";
					for (TOIArrowPathElement element : a.getElements()) {
						elements += element.getPercentPos() + " ";
						if (element.getId() != null) {
							elements += element.getId() + " ";
						} else {
							elements += "- ";
						}
					}
					objectElement.setAttribute("elements", elements.trim());
				} else if (o instanceof TOIArrowPathElement) {
					TOIArrowPathElement a = (TOIArrowPathElement) o;
					objectElement.setAttribute("type", "singlearrow");

					// arrow sizes
					if (a.getWidth() != 14 || a.getArrowWidth() != 12 || a.getArrowLength() != 6) {
						objectElement.setAttribute("sizes", a.getWidth() + ";" + a.getArrowWidth() + ";" + a.getArrowLength());
					}

					// direction
					switch (a.getDirection()) {
						case TOIArrowPathElement.DIRECTION_DIRECT:
							// default
							break;
						case TOIArrowPathElement.DIRECTION_BACK:
							objectElement.setAttribute("direction", "back");
							break;
						case TOIArrowPathElement.DIRECTION_BOTH:
							objectElement.setAttribute("direction", "both");
							break;
						case TOIArrowPathElement.DIRECTION_NONE:
							objectElement.setAttribute("direction", "none");
							break;
					}
				} else if (o instanceof TOITextPath) {
					objectElement.setAttribute("type", "text");
				}

				// geom
				String geom = "";
				int coordIndex = 0;
				int[] curvesizes = { 2, 2, 4, 6, 0 };
				for (int i = 0; i < p.getPath().numTypes; i++) {
					switch (p.getPath().pointTypes[i]) {
						case PathIterator.SEG_MOVETO:
							geom += "M ";
							break;
						case PathIterator.SEG_LINETO:
							geom += "L ";
							break;
						case PathIterator.SEG_QUADTO:
							geom += "Q ";
							break;
						case PathIterator.SEG_CUBICTO:
							geom += "C ";
							break;
						case PathIterator.SEG_CLOSE:
							geom += "X ";
							break;
					}

					int curvesize = curvesizes[p.getPath().pointTypes[i]];
					for (int j = 0; j < curvesize; j++) {
						geom += p.getPath().doubleCoords[coordIndex] + " ";
						coordIndex++;
					}
				}
				objectElement.setAttribute("geom", geom.trim());
			} else if (o instanceof TOIText) {
				TOIText t = (TOIText) o;
				objectElement = new Element("text");
				objectElement.setAttribute("transform", TOIUtil.getTransform(t.getTransform()));
				rootElement.addContent(objectElement);
			} else if (o instanceof TOIImage) {
				TOIImage i = (TOIImage) o;
				objectElement = new Element("img");
				objectElement.setAttribute("descr", i.getDescr());
				objectElement.setAttribute("transform", TOIUtil.getTransform(i.getTransform()));
				objectElement.setText(new BASE64Encoder().encode(i.getData()));
				rootElement.addContent(objectElement);
			} else if (o instanceof TOIGroup) {
				TOIGroup g = (TOIGroup) o;
				objectElement = new Element("group");
				objectElement.setAttribute("combine", g.isCombinePathes() ? "yes" : "no");
				rootElement.addContent(objectElement);

				export(objectElement, g.getObjects());
			}

			// common
			if (o.getId() != null) {
				objectElement.setAttribute("id", o.getId().toString());
			}
			if (o.getColor() != null) {
				objectElement.setAttribute("color", TOIUtil.getColor(o.getColor()));
			}
			if (o.getText() != null && o.getText().length() != 0) {
				objectElement.setText(o.getText());
			}
			if (o.getFont() != null) {
				objectElement.setAttribute("font", TOIUtil.getFont(o.getFont()));
			}
		}
	}
}
