package ru.nest.jvg.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.IconButton;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwitchPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.LinearGradientPaint;
import javax.swing.gradient.RadialGradient;
import javax.swing.gradient.RadialGradientPaint;
import javax.swing.plaf.basic.BasicLabelUI;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.action.BasicStrokeAction;
import ru.nest.jvg.action.DrawAction;
import ru.nest.jvg.action.FontAction;
import ru.nest.jvg.action.ScriptAction;
import ru.nest.jvg.action.TransformAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.ImageResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.LinearGradientResource;
import ru.nest.jvg.resource.RadialGradientResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.resource.TransformResource;
import ru.nest.jvg.shape.paint.Painter;

/**
 * 1. Color + 2. LinearGradient + 3. RadialGradient + 4. Image + apply? 5. Stroke + 6. Character attributes - ignore 7. Paragraph attributes - ignore 8. Font + 9. Scripts + 10. Transform + 11. Texture
 * -
 */
public class ResourceManager extends JInternalFrame implements ActionListener, JVGResources.Listener {
	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private SwitchPanel tab = new SwitchPanel();

	private JVGResources resources;

	public ResourceManager(JVGResources resources) {
		if (resources == null) {
			resources = new JVGResources();
		}
		this.resources = resources;

		setTitle(lm.getValue("resources.title", "Resources"));

		JPanel pnlContent = new JPanel();
		pnlContent.setLayout(new BorderLayout());
		setContentPane(pnlContent);

		tab.addTab(lm.getValue("resources.tab.color", "Color"), new JScrollPane(createColorResourcesPanel()));
		tab.addTab(lm.getValue("resources.tab.font", "Font"), new JScrollPane(createFontResourcesPanel()));
		tab.addTab(lm.getValue("resources.tab.transform", "Transformation"), new JScrollPane(createTransformResourcesPanel()));
		tab.addTab(lm.getValue("resources.tab.gradiant.linear", "Linear gradiant"), new JScrollPane(createLinearGradientResourcesPanel()));
		tab.addTab(lm.getValue("resources.tab.gradiant.radial", "Radial gradient"), new JScrollPane(createRadialGradientResourcesPanel()));
		tab.addTab(lm.getValue("resources.tab.stroke", "Stroke"), new JScrollPane(createStrokeResourcesPanel()));
		tab.addTab(lm.getValue("resources.tab.image", "Image"), new JScrollPane(createImageResourcesPanel()));
		tab.addTab(lm.getValue("resources.tab.script", "Script"), new JScrollPane(createScriptResourcesPanel()));
		pnlContent.add(tab, BorderLayout.CENTER);

		JPanel pnlButtons = constractButtonsPanel();
		pnlContent.add(pnlButtons, BorderLayout.SOUTH);

		resources.addListener(this);
	}

	private String checkName(Class<? extends Resource<?>> clazz, String name, Resource<?> resourse) {
		String oldName = resourse != null ? resourse.getName() : null;
		if (oldName != null && oldName.equals(name)) {
			return name;
		} else {
			return checkName(clazz, name);
		}
	}

	private String checkName(Class<? extends Resource> clazz, String name) {
		name = name != null ? name.trim() : "";
		while (true) {
			String message = null;
			if (name.length() == 0) {
				message = "Invalid name";
			} else if (resources.contains(clazz, name)) {
				message = "Name already exists";
			}

			if (message != null) {
				JTextField txt = new JTextField(name);
				int option = JOptionPane.showConfirmDialog(null, txt, message, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.CANCEL_OPTION) {
					return null;
				}
				name = txt.getText();
				continue;
			}

			break;
		}
		return name;
	}

	private String chooseName(String name) {
		JTextField txt = new JTextField(name != null ? name : "");
		int option = JOptionPane.showConfirmDialog(null, txt, "Enter name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.CANCEL_OPTION) {
			return null;
		}
		name = txt.getText();
		return name;
	}

	private ResourcePanel<ColorResource> createColorResourcesPanel() {
		ResourcePanel<ColorResource> pnl = new ResourcePanel<>(ColorResource.class, new ResourceProxy<ColorResource>() {
			@Override
			public ColorResource show(ColorResource resource) {
				Color color = resource != null ? resource.getResource() : Color.white;
				JColorChooser chooser = new JColorChooser(color);
				color = JColorChooser.showDialog(null, "Color", color);
				if (color != null) {
					String name = chooseName(resource != null ? resource.getName() : null);
					name = checkName(LinearGradientResource.class, name, resource);
					if (name == null) {
						return null;
					}

					if (resource == null) {
						resource = new ColorResource(color);
					} else {
						resource.setResource(color);
					}
					resource.setName(name);
					return resource;
				}
				return null;
			}

			@Override
			public void apply(ColorResource resource) {
				DrawAction action = new DrawAction(resource.getName(), resource, Painter.FILL);
				action.actionPerformed(null);
			}

			@Override
			public void paint(Graphics2D g, ColorResource resource, int x, int y, int w, int h) {
				g.setColor(resource.getResource());
				g.fillRect(x, y, w, h);
			}
		});
		return pnl;
	}

	private ResourcePanel<FontResource> createFontResourcesPanel() {
		ResourcePanel<FontResource> pnl = new ResourcePanel<>(FontResource.class, new ResourceProxy<FontResource>() {
			@Override
			public FontResource show(FontResource resource) {
				FontChooser chooser = new FontChooser(resources, resource);
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					return (FontResource) chooser.applyResource();
				} else {
					return null;
				}
			}

			@Override
			public void apply(FontResource resource) {
				FontAction action = new FontAction("font-" + resource.getName(), resource);
				action.actionPerformed(null);
			}

			@Override
			public void paint(Graphics2D g, FontResource resource, int x, int y, int w, int h) {
			}
		});
		return pnl;
	}

	private ResourcePanel<TransformResource> createTransformResourcesPanel() {
		ResourcePanel<TransformResource> pnl = new ResourcePanel<>(TransformResource.class, new ResourceProxy<TransformResource>() {
			@Override
			public TransformResource show(TransformResource resource) {
				TransformChooser chooser = new TransformChooser(resources, resource);
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					return (TransformResource) chooser.applyResource();
				} else {
					return null;
				}
			}

			@Override
			public void apply(TransformResource resource) {
				TransformAction action = new TransformAction("transform", resource.getResource());
				action.actionPerformed(null);
			}

			@Override
			public void paint(Graphics2D g, TransformResource resource, int x, int y, int w, int h) {
			}
		});
		return pnl;
	}

	private ResourcePanel<ScriptResource> createScriptResourcesPanel() {
		ResourcePanel<ScriptResource> pnl = new ResourcePanel<>(ScriptResource.class, new ResourceProxy<ScriptResource>() {
			@Override
			public ScriptResource show(ScriptResource resource) {
				Script script = ScriptAction.chooseScript(null, resource);
				if (script != null) {
					String name = chooseName(resource != null ? resource.getName() : null);
					name = checkName(LinearGradientResource.class, name, resource);
					if (name != null) {
						if (resource == null) {
							resource = script.getData();
						} else {
							resource.setResource(script.getData().getResource());
						}
						resource.setName(name);
					}
				}
				return resource;
			}

			@Override
			public void apply(ScriptResource resource) {
				if (resource != null) {
					ScriptAction action = new ScriptAction(resource);
					action.actionPerformed(null);
				}
			}

			@Override
			public void paint(Graphics2D g, ScriptResource resource, int x, int y, int w, int h) {
			}
		});
		return pnl;
	}

	private ResourcePanel<LinearGradientResource> createLinearGradientResourcesPanel() {
		ResourcePanel<LinearGradientResource> pnl = new ResourcePanel<>(LinearGradientResource.class, new ResourceProxy<LinearGradientResource>() {
			@Override
			public LinearGradientResource show(LinearGradientResource resource) {
				LinearGradientChooser chooser = new LinearGradientChooser(resources, resource);
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					return (LinearGradientResource) chooser.applyResource();
				} else {
					return null;
				}
			}

			@Override
			public void apply(LinearGradientResource resource) {
				DrawAction action = new DrawAction(resource.getName(), resource, Painter.FILL);
				action.doAction();
			}

			@Override
			public void paint(Graphics2D g, LinearGradientResource resource, int x, int y, int w, int h) {
				Paint oldPaint = g.getPaint();
				LinearGradient gradient = resource.getResource();
				if (gradient.getFractions().length >= 2) {
					LinearGradientPaint paint = new LinearGradientPaint(gradient, x, y, w, h);
					g.setPaint(paint);
					g.fillRect(x, y, w, h);
					g.setPaint(oldPaint);
				}
			}
		});

		return pnl;
	}

	private ResourcePanel<RadialGradientResource> createRadialGradientResourcesPanel() {
		ResourcePanel<RadialGradientResource> pnl = new ResourcePanel<>(RadialGradientResource.class, new ResourceProxy<RadialGradientResource>() {
			@Override
			public RadialGradientResource show(RadialGradientResource resource) {
				RadialGradientChooser chooser = new RadialGradientChooser(resources, resource);
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					return (RadialGradientResource) chooser.applyResource();
				} else {
					return null;
				}
			}

			@Override
			public void apply(RadialGradientResource resource) {
				DrawAction action = new DrawAction(resource.getName(), resource, Painter.FILL);
				action.actionPerformed(null);
			}

			@Override
			public void paint(Graphics2D g, RadialGradientResource resource, int x, int y, int w, int h) {
				Paint oldPaint = g.getPaint();
				RadialGradient gradient = resource.getResource();
				RadialGradientPaint paint = new RadialGradientPaint(gradient, x, y, w, h);
				g.setPaint(paint);
				g.fillArc(x, y, w, h, 0, 360);
				g.setPaint(oldPaint);
			}
		});

		return pnl;
	}

	private ResourcePanel<ImageResource> createImageResourcesPanel() {
		ResourcePanel<ImageResource> pnl = new ResourcePanel<>(ImageResource.class, new ResourceProxy<ImageResource>() {
			@Override
			public ImageResource show(ImageResource resource) {
				ImageChooser chooser = new ImageChooser(resources, resource);
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					return (ImageResource) chooser.applyResource();
				} else {
					return null;
				}
			}

			@Override
			public void apply(ImageResource resource) {
			}

			@Override
			public void paint(Graphics2D g, ImageResource resource, int x, int y, int w, int h) {
				Icon icon = resource.getResource();
				float scaleX = w / (float) icon.getIconWidth();
				float scaleY = h / (float) icon.getIconHeight();
				g.scale(scaleX, scaleY);

				resource.getResource().paintIcon(null, g, x, y);

				g.scale(1 / scaleX, 1 / scaleY);
			}
		});
		return pnl;
	}

	private ResourcePanel<?> createStrokeResourcesPanel() {
		ResourcePanel<?> pnl = new ResourcePanel<>(StrokeResource.class, new ResourceProxy<StrokeResource>() {
			@Override
			public StrokeResource show(StrokeResource resource) {
				StrokeChooser chooser = new StrokeChooser(resources, resource);
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					return (StrokeResource) chooser.applyResource();
				} else {
					return null;
				}
			}

			@Override
			public void apply(StrokeResource resource) {
				BasicStrokeAction action = new BasicStrokeAction(resource);
				action.actionPerformed(null);
			}

			@Override
			public void paint(Graphics2D g, StrokeResource resource, int x, int y, int w, int h) {
				x += 2;
				y += 2;
				w -= 4;
				h -= 4;

				Stroke oldStroke = g.getStroke();
				g.setStroke(resource.getResource());
				g.setColor(Color.black);
				g.drawRect(x, y, w, h);
				g.setStroke(oldStroke);
			}
		});
		return pnl;
	}

	private JPanel constractButtonsPanel() {
		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		IconButton btnAdd = new IconButton(new ImageIcon(ResourceManager.class.getResource("img/add.png")), 4);
		btnAdd.setRequestFocusEnabled(false);
		btnAdd.setActionCommand("add");
		btnAdd.addActionListener(this);
		pnlButtons.add(btnAdd);

		IconButton btnDublicate = new IconButton(new ImageIcon(ResourceManager.class.getResource("img/duplicate.png")), 4);
		btnDublicate.setRequestFocusEnabled(false);
		btnDublicate.setActionCommand("dublicate");
		btnDublicate.addActionListener(this);
		pnlButtons.add(btnDublicate);

		IconButton btnProperties = new IconButton(new ImageIcon(ResourceManager.class.getResource("img/properties.png")), 4);
		btnProperties.setRequestFocusEnabled(false);
		btnProperties.setActionCommand("properties");
		btnProperties.addActionListener(this);
		pnlButtons.add(btnProperties);

		IconButton btnApply = new IconButton(new ImageIcon(ResourceManager.class.getResource("img/apply.gif")), 4);
		btnApply.setRequestFocusEnabled(false);
		btnApply.setActionCommand("apply");
		btnApply.addActionListener(this);
		pnlButtons.add(btnApply);

		IconButton btnDelete = new IconButton(new ImageIcon(ResourceManager.class.getResource("img/delete.gif")), 4);
		btnDelete.setActionCommand("delete");
		btnDelete.addActionListener(this);
		btnDelete.setRequestFocusEnabled(false);
		pnlButtons.add(btnDelete);

		return pnlButtons;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("add".equals(cmd)) {
			ResourcePanel<?> resourcePanel = getCurrentResourcePanel();
			if (resourcePanel != null) {
				resourcePanel.add();
			}
		} else if ("delete".equals(cmd)) {
			ResourcePanel<?> resourcePanel = getCurrentResourcePanel();
			if (resourcePanel != null) {
				resourcePanel.delete();
			}
		} else if ("dublicate".equals(cmd)) {
			ResourcePanel<?> resourcePanel = getCurrentResourcePanel();
			if (resourcePanel != null) {
				resourcePanel.dublicate();
			}
		} else if ("properties".equals(cmd)) {
			ResourcePanel<?> resourcePanel = getCurrentResourcePanel();
			if (resourcePanel != null) {
				resourcePanel.properties();
			}
		} else if ("apply".equals(cmd)) {
			ResourcePanel<?> resourcePanel = getCurrentResourcePanel();
			if (resourcePanel != null) {
				resourcePanel.apply();
			}
		}
	}

	public ResourcePanel<?> getCurrentResourcePanel() {
		int index = tab.getSelectedIndex();
		return index >= 0 ? panels.get(index) : null;
	}

	private List<ResourcePanel<?>> panels = new ArrayList<>();

	private Map<Class<?>, ResourcePanel<?>> panels_hash = new HashMap<>();

	class ResourcePanel<V extends Resource> extends JList {
		private Class<V> clazz;

		private ResourceListModel model = new ResourceListModel();

		private ResourceProxy<V> proxy;

		public ResourcePanel(Class<V> clazz, ResourceProxy<V> propShower) {
			this.clazz = clazz;
			this.proxy = propShower;

			setModel(model);
			setOpaque(false);
			setFixedCellHeight(-1);
			setCellRenderer(new ResourceListRenderer());
			getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						model.update(e.getFirstIndex());
						model.update(e.getLastIndex());
					}
				}
			});

			panels.add(this);
			panels_hash.put(clazz, this);
		}

		class ResourceListModel extends AbstractListModel {
			@Override
			public int getSize() {
				return resources.getResourcesCount(clazz);
			}

			@Override
			public Object getElementAt(int index) {
				return resources.getResource(clazz, index);
			}

			public void delete(int index) {
				V resource = resources.getResource(clazz, index);
				if (resources.removeResource(resource) != null) {
					resourceDeleted(index);
				}
			}

			public int getIndex(Resource<?> resource) {
				for (int i = 0; i < getSize(); i++) {
					if (resource == getElementAt(i)) {
						return i;
					}
				}
				return -1;
			}

			public void delete(Resource<?> resource) {
				int index = getIndex(resource);
				if (index != -1) {
					delete(index);
				}
			}

			public void resourceDeleted(int index) {
				model.fireIntervalRemoved(this, index, index);
				repaint();
			}

			public void add(Resource<?> resource) {
				resources.addResource(resource);
				int index = getSize() - 1;
				model.fireIntervalAdded(this, index, index);
				repaint();
			}

			public void add(int index, Resource<?> resource) {
				resources.addResource(index, resource);
				resourceAdded(index);
			}

			public void resourceAdded(int index) {
				model.fireIntervalAdded(this, index, index);
				repaint();
			}

			public void update(int index) {
				if (index != -1) {
					model.fireContentsChanged(this, index, index);
					repaint();
				}
			}

			public void update(Resource<?> resource) {
				int index = getIndex(resource);
				update(index);
			}
		}

		class ResourceListRenderer extends JLabel implements ListCellRenderer, Icon {
			public ResourceListRenderer() {
				setFont(getFont().deriveFont(Font.PLAIN, 12f));
				setBackground(selectedColor);
				setUI(new BasicLabelUI() {
					@Override
					public Dimension getPreferredSize(JComponent c) {
						Dimension size = super.getPreferredSize(c);
						if (isSelected) {
							size.height = 40;
						} else {
							size.height = 20;
						}
						return size;
					}
				});
			}

			private Color selectedColor = new Color(220, 255, 220);

			private Border selectedBorder = BorderFactory.createLineBorder(new Color(150, 200, 150), 1);

			private int index;

			private boolean isSelected;

			private Dimension size1 = new Dimension(200, 20);

			private Dimension size2 = new Dimension(200, 40);

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				this.index = index;
				this.isSelected = isSelected;

				setOpaque(isSelected);
				setBorder(isSelected ? selectedBorder : null);
				setPreferredSize(isSelected ? size1 : size2);

				V resource = (V) value;
				setText(resource.getName());
				setIcon(this);
				return this;
			}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				if (index != -1) {
					V resource = resources.getResource(clazz, index);

					Graphics2D g2d = (Graphics2D) g;
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					proxy.paint(g2d, resource, x + 1, y + 2, getIconWidth() - 2, getIconHeight() - 4);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				}
			}

			@Override
			public int getIconWidth() {
				return isSelected ? 40 : 20;
			}

			@Override
			public int getIconHeight() {
				return isSelected ? 40 : 20;
			}
		}

		public void add() {
			V resource = proxy.show(null);
			if (resource != null) {
				model.add(resource);
				getSelectionModel().setSelectionInterval(model.getSize() - 1, model.getSize() - 1);
			}
		}

		public void delete() {
			int selectedIndex = getSelectedIndex();
			if (selectedIndex != -1) {
				model.delete(selectedIndex);
				if (selectedIndex == model.getSize()) {
					selectedIndex--;
				}
				getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
			}
		}

		public void dublicate() {
			int selectedIndex = getSelectedIndex();
			if (selectedIndex != -1) {
				V resource = resources.getResource(clazz, selectedIndex);
				String name = chooseName("");
				name = checkName(clazz, name);
				if (name == null) {
					return;
				}
				resource = (V) resource.clone();
				resource.setName(name);

				model.add(selectedIndex + 1, resource);
				getSelectionModel().setSelectionInterval(selectedIndex + 1, selectedIndex + 1);
			}
		}

		public void properties() {
			int selectedIndex = getSelectedIndex();
			if (selectedIndex != -1) {
				V resource = resources.getResource(clazz, selectedIndex);
				proxy.show(resource);
			}
		}

		public void apply() {
			int selectedIndex = getSelectedIndex();
			if (selectedIndex != -1) {
				V resource = resources.getResource(clazz, selectedIndex);
				proxy.apply(resource);
			}
		}

		@Override
		public void paintComponent(Graphics g) {
			Util.paintFormBackground(g, getWidth(), getHeight());
			super.paintComponent(g);
		}
	}

	class ResourceProxy<V extends Resource> {
		public V show(V resource) {
			return null;
		}

		public void apply(V resource) {
		}

		public void paint(Graphics2D g, V resource, int x, int y, int w, int h) {
		}
	}

	@Override
	public void resourceAdded(Resource<?> resource) {
		ResourcePanel<?> panel = panels_hash.get(resource.getClass());
		int index = panel.model.getIndex(resource);
		panel.model.resourceAdded(index);
	}

	@Override
	public void resourceRemoved(Resource<?> resource) {
		ResourcePanel<?> panel = panels_hash.get(resource.getClass());
		int index = panel.model.getIndex(resource);
		panel.model.resourceDeleted(index);
	}

	public static void main(String[] args) {
		Util.installDefaultFont(Fonts.getFont("Dialog", 0, 11));

		ResourceManager manager = new ResourceManager(new JVGResources());
		manager.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		manager.setBounds(500, 100, 150, 300);
		manager.setVisible(true);
	}
}
