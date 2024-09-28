/*

 Copyright (c) 2005-2024, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */
/*
 * SPDX-License-Identifier: BSD-3-Clause
 */

package io.sf.carte.doc.style.css;

import java.util.HashMap;

/**
 * Obtain the numeric unit from the unit string.
 * <p>
 * The benchmarks used to choose the implementation are available in the
 * {@code UnitStringToNumberMark} class in the css4j Benchmarks repository.
 * </p>
 */
public class UnitStringToId {

	private static HashMap<String, Short> unitMap = createUnitMap();

	private static HashMap<String, Short> createUnitMap() {
		HashMap<String, Short> unitMap = new HashMap<>(37);
		unitMap.put("%", CSSUnit.CSS_PERCENTAGE);
		unitMap.put("em", CSSUnit.CSS_EM);
		unitMap.put("ex", CSSUnit.CSS_EX);
		unitMap.put("cap", CSSUnit.CSS_CAP);
		unitMap.put("ch", CSSUnit.CSS_CH);
		unitMap.put("lh", CSSUnit.CSS_LH);
		unitMap.put("ic", CSSUnit.CSS_IC);
		unitMap.put("rem", CSSUnit.CSS_REM);
		unitMap.put("rex", CSSUnit.CSS_REX);
		unitMap.put("rch", CSSUnit.CSS_RCH);
		unitMap.put("ric", CSSUnit.CSS_RIC);
		unitMap.put("rlh", CSSUnit.CSS_RLH);
		unitMap.put("vw", CSSUnit.CSS_VW);
		unitMap.put("vh", CSSUnit.CSS_VH);
		unitMap.put("vi", CSSUnit.CSS_VI);
		unitMap.put("vb", CSSUnit.CSS_VB);
		unitMap.put("vmin", CSSUnit.CSS_VMIN);
		unitMap.put("vmax", CSSUnit.CSS_VMAX);
		unitMap.put("cm", CSSUnit.CSS_CM);
		unitMap.put("mm", CSSUnit.CSS_MM);
		unitMap.put("q", CSSUnit.CSS_QUARTER_MM);
		unitMap.put("in", CSSUnit.CSS_IN);
		unitMap.put("pt", CSSUnit.CSS_PT);
		unitMap.put("pc", CSSUnit.CSS_PC);
		unitMap.put("px", CSSUnit.CSS_PX);
		unitMap.put("deg", CSSUnit.CSS_DEG);
		unitMap.put("grad", CSSUnit.CSS_GRAD);
		unitMap.put("rad", CSSUnit.CSS_RAD);
		unitMap.put("turn", CSSUnit.CSS_TURN);
		unitMap.put("s", CSSUnit.CSS_S);
		unitMap.put("ms", CSSUnit.CSS_MS);
		unitMap.put("hz", CSSUnit.CSS_HZ);
		unitMap.put("khz", CSSUnit.CSS_KHZ);
		unitMap.put("dpi", CSSUnit.CSS_DPI);
		unitMap.put("dpcm", CSSUnit.CSS_DPCM);
		unitMap.put("dppx", CSSUnit.CSS_DPPX);
		unitMap.put("fr", CSSUnit.CSS_FR);
		return unitMap;
	}

	/**
	 * Retrieves the CSS unit associated to the given unit string.
	 * 
	 * @param unit the unit string.
	 * @return the associated CSS unit, or <code>CSS_OTHER</code> if the unit is
	 *         not known.
	 */
	public static short unitFromString(String unit) {
		Short numUnit = unitMap.get(unit);
		return numUnit != null ? numUnit.shortValue() : CSSUnit.CSS_OTHER;
	}

}
