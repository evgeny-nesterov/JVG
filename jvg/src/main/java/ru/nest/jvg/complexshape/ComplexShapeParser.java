package ru.nest.jvg.complexshape;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import ru.nest.expression.ExpressionContext;
import ru.nest.expression.ExpressionParser;
import ru.nest.expression.Function;
import ru.nest.expression.NumberValue;
import ru.nest.expression.Trace;
import ru.nest.expression.Value;
import ru.nest.jvg.actionarea.JVGAbstractConnectionActionArea;
import ru.nest.jvg.actionarea.JVGCoordinateActionArea;
import ru.nest.jvg.actionarea.JVGCoordinateConnectionActionArea;
import ru.nest.jvg.geom.CoordinablePath;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.geom.coord.VarCoordinate;
import ru.nest.jvg.parser.JVGParseUtil;

public class ComplexShapeParser {
	private static Map<String, Function> functions = new HashMap<>();

	private static Map<String, Element> cache = new HashMap<>();

	private ExpressionParser expParser;

	private ExpressionContext expContext;

	private URL url;

	private Map<String, Coordinable> coordinates;

	private Map<String, NumberValue> arguments;

	private String name;

	private String description;

	private CoordinablePath path;

	private boolean isFill = true;

	private List<JVGCoordinateActionArea> actions;

	private List<Coordinable> mainCoordinates;

	private List<Coordinable> boundsPoints;

	private Point2D pointOnAdd = null;

	private List<JVGCoordinateConnectionActionArea> connections;

	public enum ConnectionType {
		client, server
	}

	public ComplexShapeParser() {
		coordinates = new HashMap<>();
		arguments = new HashMap<>();
		mainCoordinates = new ArrayList<>();

		expParser = new ExpressionParser();
		expContext = expParser.getContext();
		expContext.addFunctions(functions);
	}

	static {
		functions.put("veclen", new Function() {
			@Override
			public double getValue(Value[] arguments, Trace trace) {
				double dx = trace.getValue(arguments[0]) - trace.getValue(arguments[2]);
				double dy = trace.getValue(arguments[1]) - trace.getValue(arguments[3]);
				return Math.sqrt(dx * dx + dy * dy); // 6
			}

			@Override
			public int getArgumentsCount() {
				return 4;
			}
		});

		functions.put("orth", new Function() {
			@Override
			public double getValue(Value[] arguments, Trace trace) {
				double x1 = trace.getValue(arguments[0]) - trace.getValue(arguments[4]);
				double y1 = trace.getValue(arguments[1]) - trace.getValue(arguments[5]);
				double x3 = trace.getValue(arguments[0]) - trace.getValue(arguments[2]);
				double y3 = trace.getValue(arguments[1]) - trace.getValue(arguments[3]);

				double sign = x1 * y3 - x3 * y1 > 0 ? 1 : -1;

				double v1_sq = x1 * x1 + y1 * y1;
				double v3_sq = x3 * x3 + y3 * y3;
				double v1_v3 = x1 * x3 + y1 * y3;
				return v3_sq != 0 ? (sign * Math.sqrt(v1_sq - v1_v3 * v1_v3 / v3_sq)) : 0; // 24
			}

			@Override
			public int getArgumentsCount() {
				return 6;
			}
		});

		functions.put("ortl", new Function() {
			@Override
			public double getValue(Value[] arguments, Trace trace) {
				double x1 = trace.getValue(arguments[0]) - trace.getValue(arguments[4]);
				double y1 = trace.getValue(arguments[1]) - trace.getValue(arguments[5]);
				double x3 = trace.getValue(arguments[0]) - trace.getValue(arguments[2]);
				double y3 = trace.getValue(arguments[1]) - trace.getValue(arguments[3]);

				double v3 = Math.sqrt(x3 * x3 + y3 * y3);
				double v1_v3 = x1 * x3 + y1 * y3;
				return v3 != 0 ? v1_v3 / v3 : 0; // 14
			}

			@Override
			public int getArgumentsCount() {
				return 6;
			}
		});

		functions.put("angle", new Function() {
			@Override
			public double getValue(Value[] arguments, Trace trace) {
				double x1 = trace.getValue(arguments[0]);
				double y1 = trace.getValue(arguments[1]);
				double x2 = trace.getValue(arguments[2]);
				double y2 = trace.getValue(arguments[3]);

				double a = Math.sqrt(x1 * x1 + y1 * y1);
				double b = Math.sqrt(x2 * x2 + y2 * y2);
				double a_b = x1 * x2 + y1 * y2;
				double cos = (a != 0 && b != 0) ? a_b / (a * b) : 1;
				return Math.acos(cos); // 17
			}

			@Override
			public int getArgumentsCount() {
				return 4;
			}
		});

		functions.put("intersection", new Function() {
			@Override
			public double getValue(Value[] arguments, Trace trace) {
				double x1 = trace.getValue(arguments[0]);
				double y1 = trace.getValue(arguments[1]);
				double x2 = trace.getValue(arguments[2]);
				double y2 = trace.getValue(arguments[3]);

				double x3 = trace.getValue(arguments[4]);
				double y3 = trace.getValue(arguments[5]);
				double x4 = trace.getValue(arguments[6]);
				double y4 = trace.getValue(arguments[7]);

				double x43 = x4 - x3;
				double y43 = y4 - y3;
				double x13 = x1 - x3;
				double y13 = y1 - y3;
				double x21 = x2 - x1;
				double y21 = y2 - y1;

				double u = (x43 * y13 - y43 * x13) / (y43 * x21 - x43 * y21);
				double x = 0, y = 0;
				if (u != 0) {
					x = x1 + u * x21;
					y = y1 + u * y21;
				}

				long encodedX = Float.floatToRawIntBits((float) x);
				long encodedY = Float.floatToRawIntBits((float) y);
				return (encodedX << 32) + encodedY;
			}

			@Override
			public int getArgumentsCount() {
				return 8;
			}
		});

		functions.put("getX", new Function() {
			@Override
			public double getValue(Value[] arguments, Trace trace) {
				long value = (long) trace.getValue(arguments[0]);
				int encodedX = (int) (value >> 32);
				return Float.intBitsToFloat(encodedX);
			}

			@Override
			public int getArgumentsCount() {
				return 1;
			}
		});

		functions.put("getY", new Function() {
			@Override
			public double getValue(Value[] arguments, Trace trace) {
				long value = (long) trace.getValue(arguments[0]);
				int encodedY = (int) (value & 0xFFFFFFFF);
				return Float.intBitsToFloat(encodedY);
			}

			@Override
			public int getArgumentsCount() {
				return 1;
			}
		});
	}

	public void parse(File file) throws Exception {
		String path = file.getPath();
		if (cache.containsKey(path)) {
			parse(cache.get(path), null);
		} else {
			parse(new FileInputStream(file), path);
		}
	}

	public URL getURL() {
		return url;
	}

	public void parse(URL url) throws Exception {
		this.url = url;
		String path = url.getPath();
		if (cache.containsKey(path)) {
			parse(cache.get(path), null);
		} else {
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(5000);
			conn.connect();
			parse(conn.getInputStream(), path);
		}
	}

	public void parse(String s) throws Exception {
		parse(new StringReader(s), null);
	}

	public void parse(InputStream is, String key) throws Exception {
		parse(new InputStreamReader(is, "UTF8"), key);
	}

	public void parse(Reader reader, String key) throws Exception {
		if (cache.containsKey(key)) {
			parse(cache.get(key), null);
		} else {
			SAXBuilder builder = new SAXBuilder();
			Document xml = builder.build(reader);
			Element rootElement = xml.getRootElement();
			parse(rootElement, key);
		}
	}

	private void parse(Element rootElement, String key) throws Exception {
		if (key != null) {
			cache.put(key, rootElement);
		}

		parseInfo(rootElement.getChild("info"));
		parseVariables(rootElement.getChild("variables"));
		parsePath(rootElement.getChild("path"));
		parseManage(rootElement.getChild("manage"));
		parseBounds(rootElement.getChild("bounds"));
		parsePointOnAdd(rootElement.getChild("point-on-add"));
		parseConnections(rootElement.getChild("connections"));
	}

	public Map<String, NumberValue> getArguments() {
		return arguments;
	}

	private void parseInfo(Element e) {
		if (e != null) {
			name = e.getChildText("name");
			description = e.getChildText("descr");
		}
	}

	public void setArgumentValue(String name, double value) {
		NumberValue v = arguments.get(name);
		if (v != null) {
			v.setValue(value);
		}
	}

	private Value getValue(String exp) throws Exception {
		if (exp != null) {
			// check on pure number
			try {
				return new NumberValue(Double.parseDouble(exp));
			} catch (NumberFormatException exc) {
			}

			Value value = expContext.getVariable(exp);
			if (value != null) {
				return value;
			} else {
				return expParser.load(null, exp);
			}
		} else {
			return null;
		}
	}

	private double getValue(String exp, double defaultValue) {
		if (exp != null) {
			try {
				return Double.parseDouble(exp);
			} catch (Exception exc) {
				try {
					return expParser.load(null, exp).getValue();
				} catch (Exception exc1) {
				}
			}
		}
		return defaultValue;
	}

	private void parseVariables(Element e) throws Exception {
		Iterator<Element> iter = e.getChildren("var").iterator();
		while (iter.hasNext()) {
			Element varElement = iter.next();
			String id = varElement.getAttributeValue("id");
			if (id != null) {
				// Value minValue =
				// getValue(varElement.getAttributeValue("min"));
				// Value maxValue =
				// getValue(varElement.getAttributeValue("max"));

				Value get;
				ArrayList<NumberValue> set = null;
				ArrayList<Value> function = null;
				NumberValue argument = null;
				boolean transformable = false;

				Element getElement = varElement.getChild("get");
				if (getElement != null) {
					String expression = getElement.getText();
					get = expParser.load(id, expression);
				} else {
					double defaultValue = getValue(varElement.getAttributeValue("value"), 0);
					get = new NumberValue(defaultValue);
					transformable = true;

					String argumentValue = varElement.getAttributeValue("argument");
					if (argumentValue != null) {
						if (JVGParseUtil.getBoolean(argumentValue)) {
							arguments.put(id, (NumberValue) get);
						}
					}
				}

				expContext.addVariable(id, get);
				argument = new NumberValue(0);
				expContext.addVariable("$", argument);

				Iterator<Element> set_iter = varElement.getChildren("set").iterator();
				while (set_iter.hasNext()) {
					Element setElement = set_iter.next();
					String param = setElement.getAttributeValue("param");
					String expression = setElement.getText();
					if (param != null && expression != null) {
						if (set == null) {
							set = new ArrayList<>();
							function = new ArrayList<>();
						}

						set.add((NumberValue) expContext.getVariable(param));
						Value f = expParser.load(null, expression);
						function.add(f);
					}
				}

				Coordinable coord = new VarCoordinate(get, set, function, argument, transformable);
				coordinates.put(id, coord);
			}
		}
	}

	public CoordinablePath getPath() {
		return path;
	}

	public boolean isFill() {
		return isFill;
	}

	private void parsePath(Element e) {
		String fillValue = e.getAttributeValue("fill");
		if (fillValue != null) {
			isFill = JVGParseUtil.getBoolean(fillValue);
		}

		path = new CoordinablePath();
		Iterator<Element> iter = e.getChildren().iterator();
		while (iter.hasNext()) {
			Element element = iter.next();
			String name = element.getName();
			if ("moveto".equals(name)) {
				String x1 = element.getAttributeValue("x");
				String y1 = element.getAttributeValue("y");
				path.moveTo(coordinates.get(x1), coordinates.get(y1));
			} else if ("lineto".equals(name)) {
				String x1 = element.getAttributeValue("x");
				String y1 = element.getAttributeValue("y");
				path.lineTo(coordinates.get(x1), coordinates.get(y1));
			} else if ("quadto".equals(name)) {
				String x1 = element.getAttributeValue("x1");
				String y1 = element.getAttributeValue("y1");
				String x2 = element.getAttributeValue("x2");
				String y2 = element.getAttributeValue("y2");
				path.quadTo(coordinates.get(x1), coordinates.get(y1), coordinates.get(x2), coordinates.get(y2));
			} else if ("curveto".equals(name)) {
				String x1 = element.getAttributeValue("x1");
				String y1 = element.getAttributeValue("y1");
				String x2 = element.getAttributeValue("x2");
				String y2 = element.getAttributeValue("y2");
				String x3 = element.getAttributeValue("x3");
				String y3 = element.getAttributeValue("y3");
				path.curveTo(coordinates.get(x1), coordinates.get(y1), coordinates.get(x2), coordinates.get(y2), coordinates.get(x3), coordinates.get(y3));
			} else if ("close".equals(name)) {
				path.closePath();
			}
		}
	}

	public JVGCoordinateActionArea[] getActions() {
		if (actions != null) {
			JVGCoordinateActionArea[] array = new JVGCoordinateActionArea[actions.size()];
			int size = actions.size();
			for (int i = 0; i < size; i++) {
				array[i] = actions.get(i);
			}
			return array;
		} else {
			return null;
		}
	}

	public Coordinable[] getCoordinates() {
		if (mainCoordinates != null) {
			Coordinable[] coordinates = new Coordinable[mainCoordinates.size()];
			int size = mainCoordinates.size();
			for (int i = 0; i < size; i++) {
				coordinates[i] = mainCoordinates.get(i);
			}
			return coordinates;
		} else {
			return null;
		}
	}

	public Coordinable[] getBoundsPoints() {
		int size = boundsPoints.size();
		if (boundsPoints != null && size > 2) {
			// Bounds points
			Coordinable[] coords = new Coordinable[size];
			for (int i = 0; i < size; i++) {
				coords[i] = boundsPoints.get(i);
			}
			return coords;
		} else {
			size = mainCoordinates.size();
			if (mainCoordinates != null && mainCoordinates.size() > 2) {
				// Manage points
				Coordinable[] coords = new Coordinable[size];
				for (int i = 0; i < size; i++) {
					coords[i] = mainCoordinates.get(i);
				}
				return coords;
			} else {
				return null;
			}
		}
	}

	private void parseManage(Element e) {
		if (e != null) {
			actions = new ArrayList<>();
			Iterator<Element> iter = e.getChildren("point").iterator();
			while (iter.hasNext()) {
				Element pointElement = iter.next();
				String x = pointElement.getAttributeValue("x");
				String y = pointElement.getAttributeValue("y");

				if (x != null && y != null) {
					Coordinable coordX = coordinates.get(x);
					Coordinable coordY = coordinates.get(y);

					if (coordX != null && coordY != null) {
						mainCoordinates.add(coordX);
						mainCoordinates.add(coordY);

						actions.add(new JVGCoordinateActionArea(coordX, coordY));
					}
				}
			}
		}
	}

	private void parseBounds(Element e) {
		if (e != null) {
			boundsPoints = new ArrayList<>();
			Iterator<Element> iter = e.getChildren("point").iterator();
			while (iter.hasNext()) {
				Element pointElement = iter.next();
				String x = pointElement.getAttributeValue("x");
				String y = pointElement.getAttributeValue("y");

				if (x != null && y != null) {
					Coordinable coordX = coordinates.get(x);
					Coordinable coordY = coordinates.get(y);

					if (coordX != null && coordY != null) {
						mainCoordinates.add(coordX);
						mainCoordinates.add(coordY);

						boundsPoints.add(coordX);
						boundsPoints.add(coordY);
					}
				}
			}
		}
	}

	public Point2D getPointOnAdd() {
		return pointOnAdd;
	}

	private void parsePointOnAdd(Element e) throws Exception {
		if (e != null) {
			Value x = getValue(e.getAttributeValue("x"));
			Value y = getValue(e.getAttributeValue("y"));
			if (x != null && y != null) {
				pointOnAdd = new Point2D.Double(x.getValue(), y.getValue());
			}
		}
	}

	public static void main(String[] args) {
		try {
			new ComplexShapeParser().parse(ComplexShapeParser.class.getResource("shapes/Arrow.xml"));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public JVGCoordinateConnectionActionArea[] getConnections() {
		if (connections != null) {
			JVGCoordinateConnectionActionArea[] array = new JVGCoordinateConnectionActionArea[connections.size()];
			int size = connections.size();
			for (int i = 0; i < size; i++) {
				array[i] = connections.get(i);
			}
			return array;
		} else {
			return null;
		}
	}

	private void parseConnections(Element e) throws Exception {
		if (e != null) {
			connections = new ArrayList<>();
			Iterator<Element> iter = e.getChildren("connection").iterator();
			while (iter.hasNext()) {
				Element pointElement = iter.next();
				String x = pointElement.getAttributeValue("x");
				String y = pointElement.getAttributeValue("y");
				String typeValue = pointElement.getAttributeValue("type");

				if (x != null && y != null && typeValue != null) {
					String[] types = JVGParseUtil.getStringArray(typeValue.toLowerCase(), ";");

					boolean canBeClient = true;
					Coordinable cx = coordinates.get(x);
					canBeClient &= cx != null;
					if (cx == null) {
						Value xValue = getValue(x);
						if (xValue != null) {
							cx = new VarCoordinate(xValue);
						}
					}

					Coordinable cy = coordinates.get(y);
					canBeClient &= cy != null;
					if (cy == null) {
						Value yValue = getValue(y);
						if (yValue != null) {
							cy = new VarCoordinate(yValue);
						}
					}

					if (cx != null && cy != null) {
						int type = 0;
						for (String t : types) {
							if (canBeClient && ConnectionType.client.name().equals(t)) {
								type |= JVGAbstractConnectionActionArea.CLIENT;
							}
							if (ConnectionType.server.name().equals(t)) {
								type |= JVGAbstractConnectionActionArea.SERVER;
							}
						}
						if (type == 0) {
							continue;
						}

						connections.add(new JVGCoordinateConnectionActionArea(type, cx, cy));
					}
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
