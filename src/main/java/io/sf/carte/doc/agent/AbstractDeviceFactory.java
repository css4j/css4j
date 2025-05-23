/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.agent;

import java.util.HashMap;
import java.util.Map;

import io.sf.carte.doc.style.css.StyleDatabase;

/**
 * A simple, map-backed abstract base class for {@link DeviceFactory} implementations.
 */
abstract public class AbstractDeviceFactory implements DeviceFactory {

	private final Map<String, StyleDatabase> mediaDatabaseMap = new HashMap<>();

	protected AbstractDeviceFactory() {
		super();
	}

	@Override
	public StyleDatabase getStyleDatabase(String medium) {
		return mediaDatabaseMap.get(medium);
	}

	public void setStyleDatabase(String medium, StyleDatabase db) {
		this.mediaDatabaseMap.put(medium, db);
	}

}
