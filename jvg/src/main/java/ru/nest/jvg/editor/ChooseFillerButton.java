package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.RadialGradient;
import javax.swing.menu.WMenuItem;
import javax.swing.menu.WSeparator;

import ru.nest.jvg.action.DrawAction;
import ru.nest.jvg.action.JVGAction;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.LinearGradientResource;
import ru.nest.jvg.resource.RadialGradientResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.Texture;
import ru.nest.jvg.resource.TextureResource;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.LinearGradientDraw;
import ru.nest.jvg.shape.paint.RadialGradientDraw;
import ru.nest.jvg.shape.paint.TextureDraw;

public abstract class ChooseFillerButton extends AbstractChooseColorButton {
	private int painterType;

	protected JVGAction createAction(Resource<?> resource) {
		if (resource != null) {
			if (resource.getResource() instanceof Color) {
				return new DrawAction("solid-filler", resource, painterType);
			} else if (resource.getResource() instanceof LinearGradient) {
				return new DrawAction("linear-gradient-filler", resource, painterType);
			} else if (resource.getResource() instanceof RadialGradient) {
				return new DrawAction("radial-gradient-filler", resource, painterType);
			} else if (resource instanceof TextureResource) {
				return new DrawAction("texture-filler", resource, painterType);
			}
		}
		// no fill
		return new DrawAction(painterType);
	}

	protected Draw<?> createDraw(Resource<?> resource) {
		if (resource instanceof ColorResource) {
			return new ColorDraw((ColorResource) resource);
		} else if (resource instanceof LinearGradientResource) {
			return new LinearGradientDraw((LinearGradientResource) resource);
		} else if (resource instanceof RadialGradientResource) {
			return new RadialGradientDraw((RadialGradientResource) resource);
		} else if (resource instanceof TextureResource) {
			return new TextureDraw((TextureResource) resource);
		}
		return null;
	}

	public abstract <T> Resource<T> getEditorResource(Class<T> clazz);

	public ChooseFillerButton(final JVGResources resources, int painterType, ImageIcon icon, Draw<?> draw) {
		this.painterType = painterType;
		init(resources, icon, draw);
	}

	public ChooseFillerButton(final JVGResources resources, ImageIcon icon, Draw<?> draw) {
		init(resources, icon, draw);
	}

	private void init(final JVGResources resources, ImageIcon icon, Draw<?> draw) {
		init(createAction(null), icon, draw);

		final Action actionNoFill = createAction(null);
		if (actionNoFill != null) {
			WMenuItem menuNoFill = new WMenuItem(lm.getValue("chooser.fill.no", "No fill"));
			menuNoFill.addActionListener(actionNoFill);
			menuNoFill.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setCurrentAction(actionNoFill, null);
				}
			});
			getPopup().add(menuNoFill, new GridBagConstraints(0, 0, 6, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}

		getPopup().add(new WSeparator(), new GridBagConstraints(0, 1, 6, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		getPopup().add(new WSeparator(), new GridBagConstraints(0, 10, 6, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		WMenuItem menuLinearGradient = new WMenuItem(lm.getValue("chooser.fill.gradient.linear", "Linear gradient"));
		menuLinearGradient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Resource<LinearGradient> oldResource = getEditorResource(LinearGradient.class);
				LinearGradientChooser chooser = new LinearGradientChooser(resources, oldResource);
				chooser.setLocation(300, 150);
				chooser.pack();
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					Resource<LinearGradient> resource = chooser.getResource();
					Resource<LinearGradient> newResource = chooser.createResource();
					if (oldResource != resource) {
						if (resource == null || resource.getName() == null) {
							resource = newResource;
						}
					} else {
						if (resource != null) {
							newResource.setName(resource.getName());
						}
						resource = newResource;
					}
					setFiller(createAction(resource), new LinearGradientDraw(newResource));
				}
			}
		});
		getPopup().add(menuLinearGradient, new GridBagConstraints(0, 20, 6, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		WMenuItem menuRadialGradient = new WMenuItem(lm.getValue("chooser.fill.gradient.radial", "Radial gradient"));
		menuRadialGradient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Resource<RadialGradient> oldResource = getEditorResource(RadialGradient.class);
				RadialGradientChooser chooser = new RadialGradientChooser(resources, getEditorResource(RadialGradient.class));
				chooser.setLocation(300, 150);
				chooser.pack();
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					Resource<RadialGradient> resource = chooser.getResource();
					Resource<RadialGradient> newResource = chooser.createResource();
					if (oldResource != resource) {
						if (resource == null || resource.getName() == null) {
							resource = newResource;
						}
					} else {
						if (resource != null) {
							newResource.setName(resource.getName());
						}
						resource = newResource;
					}
					setFiller(createAction(resource), new RadialGradientDraw(newResource));
				}
			}
		});
		getPopup().add(menuRadialGradient, new GridBagConstraints(0, 21, 6, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		WMenuItem menuTexture = new WMenuItem(lm.getValue("chooser.fill.texture", "Texture"));
		menuTexture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Resource<Texture> oldResource = getEditorResource(Texture.class);
				ImageChooser chooser = new ImageChooser(resources, getEditorResource(Icon.class));
				chooser.pack();
				Util.centrate(chooser);
				chooser.setVisible(true);
				if (chooser.getOption() == AbstractChooserDialog.OPTION_OK) {
					Resource<Icon> img = chooser.getResource();
					Resource<Icon> newimg = chooser.createResource();

					Resource<Texture> resource = img != null ? new TextureResource(new Texture(img)) : null;
					Resource<Texture> newResource = img != null ? new TextureResource(new Texture(newimg)) : null;
					if (oldResource != resource) {
						if (resource == null || resource.getName() == null) {
							resource = newResource;
						}
					} else {
						if (resource != null) {
							newResource.setName(resource.getName());
						}
						resource = newResource;
					}
					setFiller(createAction(resource), new TextureDraw(newResource));
				}
			}
		});
		getPopup().add(menuTexture, new GridBagConstraints(0, 22, 6, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	}

	@Override
	public void setFiller(Resource resource) {
		JVGAction action = createAction(resource);
		setFiller(action, createDraw(resource));
	}

	private void setFiller(JVGAction action, Draw<?> draw) {
		setCurrentAction(action, draw);
		action.doAction();
	}

	@Override
	protected AbstractButton createColorButton(Draw draw) {
		return new ColorButton(draw, createAction(((ColorDraw) draw).getResource()));
	}

	@Override
	public AbstractButton createChooseButton() {
		return new WMenuItem();
	}
}
