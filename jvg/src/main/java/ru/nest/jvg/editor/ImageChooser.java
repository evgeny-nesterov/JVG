package ru.nest.jvg.editor;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.resource.ImageResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.Resource;
import ru.nest.swing.file.ImageExplorerPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageChooser extends AbstractChooserDialog<Icon> implements ActionListener {
	private ImageExplorerPanel imagePanel;

	public ImageChooser(JVGResources resources) {
		this(resources, null);
	}

	public ImageChooser(JVGResources resources, Resource<Icon> resource) {
		super(resources, resource);
	}

	@Override
	public void init() {
		super.init();
		setTitle(lm.getValue("chooser.image.title", "Image chooser"));
	}

	@Override
	public void setResource(Resource<Icon> resource) {
		super.setResource(resource);
		if (resource instanceof ImageResource<?> && resource.getResource() != null) {
			ImageResource<Icon> iresource = (ImageResource<Icon>) resource;
			String path = iresource.getSource();
			try {
				URL url = new URL(path);
				imagePanel.getExplorer().setSelectedFile(new File(url.getFile()));
			} catch (MalformedURLException exc) {
				exc.printStackTrace();
			}
		}
	}

	@Override
	protected JPanel constractChooserPanel() {
		imagePanel = new ImageExplorerPanel();
		imagePanel.setPreferredSize(new Dimension(600, 300));
		imagePanel.getSplit().setDividerLocation(300);
		imagePanel.getExplorer().getSplit().setDividerLocation(150);

		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BorderLayout());
		pnlMain.add(constractNamePanel(), BorderLayout.NORTH);
		pnlMain.add(imagePanel, BorderLayout.CENTER);
		return pnlMain;
	}

	@Override
	public Resource<Icon> createResource() {
		File file = imagePanel.getExplorer().getSelectedFile();
		if (file != null && file.isFile()) {
			try {
				return (Resource) new ImageResource<ImageIcon>(file);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		Util.installDefaultFont(Fonts.getFont("Dialog", 0, 11));
		ImageChooser f = new ImageChooser(new JVGResources());
		f.setVisible(true);
	}
}
