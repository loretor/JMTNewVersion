/**
 * Copyright (C) 2010, Michail Makaronidis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmt.jmva.analytical.solvers.exceptions;

/**
 * This class represents an exception that can be thrown if an internal error is
 * encountered somewhere in the program.
 *
 * @author Michail Makaronidis, 2010
 */
public class InternalErrorException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an InternalErrorException with an exception message.
	 *
	 * @param string The exception message
	 */
	public InternalErrorException(String string) {
		super(string);
	}

}
