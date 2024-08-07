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

package jmt.gui.jsimgraph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import jmt.framework.gui.components.JMTDialog;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.Defaults;

/**
 * <p>Title: Parameter selection Dialog Factory</p>
 * <p>Description: Creates a modal dialog box into main window that displays given
 * panel. This class is used to enter input parameters of the model.</p>
 * 
 * @author Bertoli Marco
 *         Date: 2-giu-2005
 *         Time: 19.52.26
 */
public class DialogFactory {

	// Private variables
	private JMTDialog dialogFrame;
	private Frame mainWindow;

	/**
	 * Creates a new DialogFactory
	 * @param mainWindow owner frame for created dialogs.
	 */
	public DialogFactory(Frame mainWindow) {
		this.mainWindow = mainWindow;
	}

	private void createDialog(int width, int height) {
		// Creates modal dialog
		dialogFrame = new JMTDialog(mainWindow, true);
		dialogFrame.centerWindow(width, height);
		dialogFrame.setResizable(true);
		dialogFrame.getContentPane().setLayout(new BorderLayout());

		//Adds button bar
		JPanel buttonbar = new JPanel(new FlowLayout());
		dialogFrame.getContentPane().add(buttonbar, BorderLayout.SOUTH);

		// Adds Close button
		JButton closebutton = new JButton();
		closebutton.setText("Done");
		closebutton.setToolTipText("Closes this window");
		closebutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// If this contains wizard panels, calls lostFocus() method
				for (int i = 0; i < dialogFrame.getContentPane().getComponentCount(); i++) {
					Component cmp = dialogFrame.getContentPane().getComponent(i);
					if (cmp instanceof WizardPanel) {
						((WizardPanel) cmp).lostFocus();
					}
				}
				dialogFrame.close();
			}
		});
		buttonbar.add(closebutton);
	}

	/**
	 * Returns modal dialog box showing given panel and with given title
	 * @param panel to be shown on the center area of this dialog
	 * @param title title of this dialog
	 */
	public void getDialog(final JComponent panel, String title) {
		getDialog(panel, title, CommonConstants.MAX_GUI_WIDTH_DIALOG_DEFAULT, CommonConstants.MAX_GUI_HEIGHT_DIALOG_DEFAULT, false, "", "");
	}

	/**
	 * Returns modal dialog box showing given panel and with given title
	 * @param panel to be shown on the center area of this dialog
	 * @param title title of this dialog
	 */
	public void getDialog(final JComponent panel, String title, int width, int height, boolean saveSize, String defaultWidthProp, String defaultHeightProp) {
		createDialog(width, height);
		// Adds input panel
		dialogFrame.getContentPane().add(panel, BorderLayout.CENTER);
		// Sets title
		if (title != null) {
			dialogFrame.setTitle(title);
		}
		// If this is a wizard panel call gotFocus() method
		if (panel instanceof WizardPanel) {
			((WizardPanel) panel).gotFocus();
		}
		// Shows dialog
		dialogFrame.setVisible(true);
		if (saveSize) {
			Dimension d = dialogFrame.getSize();
			Defaults.set(defaultWidthProp, String.valueOf(d.width));
			Defaults.set(defaultHeightProp, String.valueOf(d.height));
			Defaults.save();
		}
	}

}
