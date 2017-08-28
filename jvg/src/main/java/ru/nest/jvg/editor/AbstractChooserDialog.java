package ru.nest.jvg.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.IconButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.Resource;

public abstract class AbstractChooserDialog<V> extends JDialog implements ActionListener {
	public final static int OPTION_OK = 0;

	public final static int OPTION_CANCEL = 1;

	private JTextField txtName;

	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	protected JVGResources resources;

	public AbstractChooserDialog(JVGResources resources, Resource<V> resource) {
		init();
		setResources(resources);
		setResource(resource);
	}

	public void init() {
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		txtName = new JTextField(20);

		JPanel pnlContent = new JPanel();
		pnlContent.setLayout(new BorderLayout());
		setContentPane(pnlContent);

		pnlContent.add(constractChooserPanel(), BorderLayout.CENTER);
		pnlContent.add(constractOptionsPanel(), BorderLayout.SOUTH);
		pack();
		Util.centrate(this);
	}

	// apply data to resource or create a new one
	protected abstract Resource<V> createResource();

	private Resource<V> resource;

	public Resource<V> applyResource() {
		Resource<V> resource = createResource();
		if (this.resource != null) {
			this.resource.setResource(resource.getResource());
			resource = this.resource;
		}
		return resource;
	}

	public Resource<V> getResource() {
		return resource;
	}

	public void setResource(Resource<V> resource) {
		if (resource != null && resource.getName() != null) {
			this.resource = resource;
			txtName.setText(resource.getName());
		} else {
			this.resource = null;
			txtName.setText("");
		}
	}

	protected JPanel pnlResources;

	protected JComboBox cmbResources;

	private boolean ignoreResourcesEvent = false;

	private IconButton btnSave;

	protected JPanel constractNamePanel() {
		btnSave = new IconButton(new ImageIcon(AbstractChooserDialog.class.getResource("img/save.gif")));
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				storeResource();
			}
		});

		JPanel pnlName = new JPanel();
		pnlName.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		pnlName.add(new JLabel(lm.getValue("chooser.name", "Name: ")));
		pnlName.add(txtName);
		pnlName.add(btnSave);

		cmbResources = new JComboBox();
		cmbResources.setPreferredSize(new Dimension(100, 18));
		cmbResources.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!ignoreResourcesEvent) {
					setResource((Resource<V>) cmbResources.getSelectedItem());
				}
			}
		});

		pnlResources = new JPanel();
		pnlResources.setLayout(new BorderLayout());
		pnlResources.add(new JLabel(lm.getValue("chooser.resources", "Resources: ")), BorderLayout.WEST);
		pnlResources.add(cmbResources, BorderLayout.CENTER);
		pnlResources.setVisible(false);

		JPanel pnl = new JPanel();
		pnl.setLayout(new GridBagLayout());
		pnl.setBorder(BorderFactory.createEtchedBorder());
		pnl.add(pnlName, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pnl.add(pnlResources, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		return pnl;
	}

	private void setResources(JVGResources resources) {
		ignoreResourcesEvent = true;
		try {
			this.resources = resources;
			cmbResources.removeAllItems();
			pnlResources.setVisible(resources != null);
			btnSave.setVisible(resources != null);
			if (resources != null) {
				Resource<?> sample = createResource();
				if (sample != null) {
					Class<? extends Resource<?>> clazz = (Class<? extends Resource<?>>) sample.getClass();
					int count = resources.getResourcesCount(clazz);
					for (int i = 0; i < count; i++) {
						cmbResources.addItem(resources.getResource(clazz, i));
					}
					cmbResources.setSelectedIndex(-1);
				}
			}
		} finally {
			ignoreResourcesEvent = false;
		}
	}

	protected abstract JPanel constractChooserPanel();

	protected JPanel constractOptionsPanel() {
		JPanel pnlOptions = new JPanel();
		pnlOptions.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

		JButton btnOK = new JButton(lm.getValue("chooser.button.ok", "OK"));
		btnOK.setActionCommand("option-ok");
		btnOK.addActionListener(this);
		pnlOptions.add(btnOK);

		JButton btnCancel = new JButton(lm.getValue("chooser.button.cancel", "Cancel"));
		btnCancel.setActionCommand("option-cancel");
		btnCancel.addActionListener(this);
		pnlOptions.add(btnCancel);

		return pnlOptions;
	}

	private void close() {
		setVisible(false);
		dispose();
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	private int option = OPTION_CANCEL;

	public int getOption() {
		return option;
	}

	public String getResourceName() {
		return resource != null ? resource.getName() : null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("option-ok".equals(cmd)) {
			option = OPTION_OK;
			close();
		} else if ("option-cancel".equals(cmd)) {
			option = OPTION_CANCEL;
			close();
		}
	}

	public boolean storeResource() {
		String name = txtName.getText().trim();
		if (name.length() > 0) {
			if (resource == null) {
				resource = createResource();
				resource.setName(name);
			}
			if (!name.equals(resource.getName())) {
				if (resources.contains(resource.getClass(), name)) {
					JOptionPane.showMessageDialog(null, lm.getValue("warning.cant-save", "Can't save"), lm.getValue("warning.resource-exists", "Resource already exists"), JOptionPane.WARNING_MESSAGE);
					return false;
				}
				resource = createResource();
				resource.setName(name);
			} else {
				resource.setResource(createResource().getResource());
			}
			resources.addResource(resource);
			return true;
		} else {
			JOptionPane.showMessageDialog(null, lm.getValue("warning.cant-save", "Can't save"), lm.getValue("warning.empty-name", "Empty name"), JOptionPane.WARNING_MESSAGE);
		}
		return false;
	}
}
