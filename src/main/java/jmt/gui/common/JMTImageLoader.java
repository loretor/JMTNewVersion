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

package jmt.gui.common;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.ImageIcon;

import jmt.framework.gui.image.ImageLoader;
import net.sf.saxon.exslt.Common;

/**
 * <p><b>Name:</b> JMTImageLoader</p> 
 * <p><b>Description:</b> 
 * Loads and caches images to create icons. 
 * This class is designed to be accessed in a static manner.
 * </p>
 * <p><b>Date:</b> 23/gen/07
 * <b>Time:</b> 16:15:32</p>
 * @author Bertoli Marco
 * @version 1.2
 */
public class JMTImageLoader {

	protected static ImageLoader imageLoader = new ImageLoaderImpl();


	/**
	 * Loads the image from this directory. Please put all images in the class
	 * package.
	 *
	 * @param imageName string containing the image name
	 * @return the icon of the image
	 */
	public static ImageIcon loadImage(String imageName) {
		return imageLoader.loadIcon(imageName);
		//ImageIcon ico =  imageLoader.loadIcon(imageName);
		//int standardIconHeight = (int)(ico.getIconHeight()* CommonConstants.heightScaling);
		//int standardIconWidth = (int)(ico.getIconWidth()* CommonConstants.widthScaling);
		//return resizeIcon(ico, new Dimension(standardIconWidth, standardIconHeight));
	}

	/**
	 * Loads the image from this directory, and applies a specified size. Please
	 * put all images in the class package.
	 *
	 * @param imageName string containing the image name
	 * @param size specific size for the image to be returned
	 * @return the icon of the image
	 */
	public static ImageIcon loadImage(String imageName, Dimension size) {
		return imageLoader.loadIcon(imageName, size);
		//ImageIcon ico =  imageLoader.loadIcon(imageName, size);
		//int standardIconHeight = (int)(ico.getIconHeight()* CommonConstants.heightScaling);
		//int standardIconWidth = (int)(ico.getIconWidth()* CommonConstants.widthScaling);
		//return resizeIcon(ico, new Dimension(standardIconWidth, standardIconHeight));
	}

	/**
	 * Loads the image from this directory. Please put all images in the class
	 * package.
	 *
	 * @param imageName string containing the image name
	 * @return the image
	 */
	public static Image loadImageAwt(String imageName) {
		ImageIcon icon = imageLoader.loadIcon(imageName);
		if (icon != null) {
			return icon.getImage();
		} else {
			return null;
		}
	}

	/**
	 * @return encapsulated ImageLoader object
	 */
	public static ImageLoader getImageLoader() {
		return imageLoader;
	}

	/**
	 * Loads the image from this directory, and applies a known modifier. Please
	 * put all images in the class package.
	 *
	 * @param imageName string containing the image name
	 * @param modifier known modifier method
	 * @return the icon of the image
	 * @see ImageLoader#loadIcon(String, String)
	 */
	public static ImageIcon loadImage(String imageName, String modifier) {
		return loadImage(imageName, modifier,0.0);
	}

	/**
	 * Loads the image from this directory, and applies a known modifier. Please
	 * put all images in the class package.
	 *
	 * @param imageName string containing the image name
	 * @param modifier known modifier method
	 * @param angle the rotation angle in degrees
	 * @return the icon of the image
	 * @see ImageLoader#loadIcon(String, String)
	 */
	public static ImageIcon loadImage(String imageName, String modifier, Double angle) {
		return imageLoader.loadIcon(imageName, modifier, angle);
		//ImageIcon ico =  imageLoader.loadIcon(imageName, modifier, angle);
		//int standardIconHeight = (int)(ico.getIconHeight()* CommonConstants.heightScaling);
		//int standardIconWidth = (int)(ico.getIconWidth()* CommonConstants.widthScaling);
		//return resizeIcon(ico, new Dimension(standardIconWidth, standardIconHeight));
	}

	/**
	 * Loads the image from this directory, and applies a known modifier and
	 * specified size. Please put all images in the class package.
	 *
	 * @param imageName string containing the image name
	 * @param modifier known modifier method
	 * @param size specific size for the image to be returned
	 * @return the icon of the image
	 * @see ImageLoader#loadIcon(String, String, Dimension)
	 */
	public static ImageIcon loadImage(String imageName, String modifier, Dimension size) {
		return imageLoader.loadIcon(imageName, modifier, size);
		//ImageIcon ico =  imageLoader.loadIcon(imageName, modifier, size);
		//int standardIconHeight = (int)(ico.getIconHeight()* CommonConstants.heightScaling);
		//int standardIconWidth = (int)(ico.getIconWidth()* CommonConstants.widthScaling);
		//return resizeIcon(ico, new Dimension(standardIconWidth, standardIconHeight));
	}

	/**
	 * Resizes the given ImageIcon to the specified width and height.
	 *
	 * @param icon the ImageIcon to resize
	 * @param size the new size for the ImageIcon
	 * @return a new ImageIcon with the specified size
	 */
	private static ImageIcon resizeIcon(ImageIcon icon, Dimension size) {
		Image img = icon.getImage();
		Image resizedImage = img.getScaledInstance((int) (size.width * CommonConstants.widthScaling), (int) (size.height* CommonConstants.heightScaling), Image.SCALE_SMOOTH);
		return new ImageIcon(resizedImage);
	}

}
