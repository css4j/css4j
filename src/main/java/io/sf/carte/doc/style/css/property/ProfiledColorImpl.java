/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Arrays;
import java.util.Objects;

import io.sf.carte.doc.color.Illuminant;
import io.sf.jclf.math.linear3.Matrices;

/**
 * A color specified through the {@code color()} function, where the profile is
 * available at object creation time.
 */
class ProfiledColorImpl extends BaseProfiledColor {

	private static final long serialVersionUID = 1L;

	private ColorProfile profile;

	/**
	 * Construct a new profiled color.
	 * 
	 * @param profileName a string with the name of the color space.
	 * @param profile     the color profile.
	 * @param components  the components.
	 */
	ProfiledColorImpl(String profileName, ColorProfile profile, double[] components) {
		super(profileName, new PrimitiveValue[components.length]);
		this.profile = profile;
		setColorComponents(components);
	}

	ProfiledColorImpl(ProfiledColorImpl copyMe) {
		super(copyMe);
		profile = copyMe.profile;
	}

	@Override
	void set(BaseColor color) {
		super.set(color);
		this.profile = ((ProfiledColorImpl) color).profile;
	}

	@Override
	public double[] toXYZ(Illuminant white) {
		double[] comps = toNumberArray();

		for (int i = 0; i < comps.length; i++) {
			comps[i] = profile.linearComponent(comps[i]);
		}

		double[] xyz = new double[3];
		profile.linearRgbToXYZ(comps[0], comps[1], comps[2], xyz);

		if (white != profile.getIlluminant()) {
			if (white == Illuminant.D50) {
				xyz = ColorUtil.d65xyzToD50(xyz);
			} else {
				xyz = ColorUtil.d50xyzToD65(xyz);
			}
		}

		return xyz;
	}

	/**
	 * Convert this color to the XYZ space using the given reference white.
	 * 
	 * @param white the white point tristimulus value, normalized so the {@code Y}
	 *              component is always {@code 1}.
	 * @return the color expressed in XYZ coordinates with the given white point.
	 */
	@Override
	public double[] toXYZ(double[] white) {
		double[] comps = toNumberArray();

		for (int i = 0; i < comps.length; i++) {
			comps[i] = profile.linearComponent(comps[i]);
		}

		double[] xyz = new double[3];

		profile.linearRgbToXYZ(comps, xyz);

		double[] wp = profile.getWhitePoint();
		if (!Arrays.equals(wp, white)) {
			double[][] cam = new double[3][3];
			ChromaticAdaption.chromaticAdaptionMatrix(wp, white, cam);
			double[] result = new double[3];
			Matrices.multiplyByVector3(cam, xyz, result);
			xyz = result;
		}

		return xyz;
	}

	@Override
	double[] toSRGB(boolean clamp) {
		double[] xyz = toXYZ(Illuminant.D65);

		double[] rgb = new double[3];
		ColorUtil.d65xyzToSRGB(xyz, rgb);

		// range check
		if (!ColorUtil.rangeRoundCheck(rgb) && clamp) {
			double[] xyzD50 = ColorUtil.d65xyzToD50(xyz);
			double[] lab = new double[3];
			ColorUtil.xyzD50ToLab(xyzD50, lab);
			ColorProfile profile = new SRGBColorProfile();
			ColorUtil.clampRGB(lab[0], lab[1], lab[2], profile, rgb);
		}

		return rgb;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(profile);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProfiledColorImpl other = (ProfiledColorImpl) obj;
		return Objects.equals(profile, other.profile);
	}

	@Override
	public ProfiledColorImpl clone() {
		return new ProfiledColorImpl(this);
	}

}
