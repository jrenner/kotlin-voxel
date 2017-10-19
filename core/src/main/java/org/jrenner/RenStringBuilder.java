package org.jrenner;

import com.badlogic.gdx.utils.StringBuilder;

public class RenStringBuilder extends StringBuilder {

	/***
	 * Kotlin has trouble with property 'length' and function 'length()', so we wrap it with this function and class
	 */
	public int sbLength() {
		return this.length();
	}
}
