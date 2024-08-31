# css4j version 4.4 Release Notes

### August 31, 2024

<br/>

## Highlights

Color improvements:

- Added the `toXYZ(Illuminant)`, `toXYZ(double[])`, `deltaEOK(CSSColor)` and `isInGamut(String)` methods to `CSSColor`.
- The `srgb-linear` color space is now supported in the `color()` and `color-mix()` functions.
- `RGBColorDeclarationFormattingContext` was refactored so alternative color serializations can be easily implemented.
- A few other changes that either directly or indirectly improve the handling of colors.

<br/>

## Detail of changes

- NSAC: allow mathematical functions inside `color-mix()`.
- CSSOM: add a public `setMaximumFractionDigits()` method to `NumberValue`.
- CSSOM: add methods `toXYZ(Illuminant)`, `toXYZ(double[])` and `deltaEOK(CSSColor)` to `CSSColor`
- CSSOM: add the `isInGamut(String)` method to `CSSColor`, improve component type checks.
- CSSOM: support the `srgb-linear` color space in the `color()` and `color-mix()` functions.
- CSSOM: throw exceptions for invalid `calc()` and math functions in color component percentages and rgb components.
- CSSOM: extract superclass from `RGBColorDeclarationFormattingContext`.
- CSSOM: check the unit at the final result instead of inside an operation in `PercentageEvaluator`.
- CSSOM: unofficial and undocumented support for linear versions of predefined RGB color spaces.
- Javadocs: improve the LexicalType javadoc.
- Upgrade to Junit 5.11.0.
- Upgrade Gradle wrapper to 8.10.
- Upgrade to wrapper-validation action v4.
- Code style: add a checkstyle check.
