/**
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package jmt.gui.jsimgraph.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import jmt.common.ConnectionCheck;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.xml.TemplateFileOperation;
import jmt.gui.jsimgraph.template.TemplateConstants;
import jmt.gui.jsimgraph.template.TemplateData;

/**
 * @author S Jiang
 * 
 */
public class TemplateUpdatePanel extends WizardPanel implements TemplateConstants {

	private static final long serialVersionUID = 1L;

	private JProgressBar progress;
	private JButton download;
	private JButton cancel;
	private List<TemplateData> data;
	private DefaultPackageTable defaultPackageTable;
	private JPanel parent;
	private TemplatePanel panel;

	public TemplateUpdatePanel(List<TemplateData> data) {
		this.data = data;
		parent = this;
		this.panel = null;
		initComponents();
	}

	public TemplateUpdatePanel(TemplatePanel panel, List<TemplateData> data) {
		this.data = data;
		parent = this;
		this.panel = panel;
		initComponents();
	}

	public void initComponents() {
		progress = new JProgressBar();
		progress.setStringPainted(true);
		progress.setString("0%");
		download = new JButton("Update");
		cancel = new JButton("Cancel");

		progress.setPreferredSize(new Dimension((int)(200 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling)));
		download.setPreferredSize(new Dimension((int)(110 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling)));
		cancel.setPreferredSize(new Dimension((int)(110 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling)));

		download.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				progress.setValue(0);
				progress.setString("0%");
				download.setEnabled(false);
				cancel.setEnabled(false);
				if (ConnectionCheck.netCheck(CONN_TEST_ADDRESSES)) {
					new Downloader(progress).execute();
				} else {
					download.setEnabled(true);
					cancel.setEnabled(true);
					JOptionPane.showMessageDialog(parent, "Connection error");
				}
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.getWindowAncestor(parent).setVisible(false);
			}
		});

		defaultPackageTable = new DefaultPackageTable();
		JScrollPane fileListPane = new JScrollPane();
		fileListPane.getViewport().add(defaultPackageTable);

		JLabel label = new JLabel("The following template will be updated: ");
		label.setLabelFor(fileListPane);

		JLabel progLabel = new JLabel("PROGRESS:");
		progLabel.setLabelFor(progress);

		this.setLayout(new BorderLayout());

		JPanel upperPane = new JPanel();
		upperPane.setLayout(new FlowLayout(FlowLayout.LEADING));

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new FlowLayout(FlowLayout.LEADING));
		bottomPane.add(progLabel);
		bottomPane.add(progress);
		bottomPane.add(download);
		bottomPane.add(cancel);

		upperPane.add(label);
		this.add(upperPane, BorderLayout.NORTH);
		this.add(fileListPane, BorderLayout.CENTER);
		this.add(bottomPane, BorderLayout.SOUTH);
	}

	public class DefaultPackageTable extends JTable {

		private static final long serialVersionUID = 1L;

		public DefaultPackageTable() {
			setModel(new DefaultPackageTableModel());
			sizeColumns();
			setRowHeight(CELL_HEIGHT);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			getTableHeader().setReorderingAllowed(false);
		}

		private void sizeColumns() {
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setPreferredWidth(((DefaultPackageTableModel) getModel()).columnSizes[i]);
			}
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			String tip = null;
			Point p = e.getPoint();
			int rowIndex = rowAtPoint(p);
			try {
				tip = data.get(rowIndex).getToolTip();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return tip;
		}

	}

	public class DefaultPackageTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		String[] columnNames = { "Template" };
		Class<?>[] columnClass = { String.class };
		public int[] columnSizes = new int[] { 520 };
		boolean[] flags = new boolean[data.size()];

		public DefaultPackageTableModel() {
			Arrays.fill(flags, true);
		}

		@Override
		public Class<?> getColumnClass(int colIndex) {
			return (columnClass[colIndex]);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int colIndex) {
			return columnNames[colIndex];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data.get(rowIndex).getName();
		}

		@Override
		public void setValueAt(Object value, int row, int col) {

		}

	}

	public class Downloader extends SwingWorker<Boolean, Integer> {

		JProgressBar progress;

		public Downloader(JProgressBar progress) {
			this.progress = progress;
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			for (int i = 0; i < data.size(); i++) {
				String file = data.get(i).getUpdateAddress();
				String fileName = data.get(i).getFileName();
				try {
					if (fileName != null) {
						fileName = TEMPLATE_FOLDER + fileName;
						TemplateFileOperation.download(file, fileName, CONN_TIMEOUT, READ_TIMEOUT);
					} else {
						throw new Exception();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(parent, "<html> Error: Unable to download <br>\n" + file);
				}
				publish(i + 1);
			}
			Thread.sleep(300);
			publish(-1);
			return null;
		}

		protected void process(final List<Integer> chunks) {
			for (final Integer task : chunks) {
				if (task > -1) {
					int value = task * 100 / data.size();
					progress.setValue(value);
					progress.setString(value + "%");
				}
				if (task == -1) {
					download.setEnabled(true);
					cancel.setEnabled(true);
					SwingUtilities.getWindowAncestor(parent).setVisible(false);
					if (panel != null) {
						panel.updateTemplates();
					}
				}
			}
		}

	}

	@Override
	public String getName() {
		return "Updating";
	}

}
