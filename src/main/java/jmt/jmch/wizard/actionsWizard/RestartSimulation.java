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

package jmt.jmch.wizard.actionsWizard;

import java.awt.event.ActionEvent;

import jmt.jmch.wizard.panels.JMCHWizardPanel;

/**
 * @author Lorenzo Torri
 * Date: 30-mar-2024
 * Time: 15.20
 */
public class RestartSimulation extends AbstractMCHAction {

    private static final long serialVersionUID = 1L;

    /**
     * Defines an <code>Action</code> object with a default
     * description string and default icon.
     */
    public RestartSimulation(JMCHWizardPanel panel) {
        super("Restart Simulation", "Stop", panel);
        putValue(SHORT_DESCRIPTION, "Restart simulation");
        //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        panel.reloadAnimation();
    }

}
   
 
