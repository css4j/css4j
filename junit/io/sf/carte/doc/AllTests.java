/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.sf.carte.doc.agent.AbstractUserAgentTest;
import io.sf.carte.doc.dom.CSSDOMImplementationTest;
import io.sf.carte.doc.dom.CollectionsTest;
import io.sf.carte.doc.dom.DOMDocumentTest;
import io.sf.carte.doc.dom.DOMElementTest;
import io.sf.carte.doc.dom.DOMNodeTest;
import io.sf.carte.doc.dom.DOMWriterTest;
import io.sf.carte.doc.dom.FilteredIteratorTest;
import io.sf.carte.doc.dom.HTMLDocumentTest;
import io.sf.carte.doc.dom.HTMLElementTest;
import io.sf.carte.doc.dom.IEDocumentTest;
import io.sf.carte.doc.dom.IteratorTest;
import io.sf.carte.doc.dom.NodeIteratorTest;
import io.sf.carte.doc.dom.TreeWalkerTest;
import io.sf.carte.doc.dom.XHTMLDocumentTest;
import io.sf.carte.doc.dom.XMLDocumentBuilderTest;
import io.sf.carte.doc.dom.XMLDocumentTest;
import io.sf.carte.doc.style.css.CSSUnitTest;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetTest;
import io.sf.carte.doc.style.css.om.AbstractStyleDatabaseTest;
import io.sf.carte.doc.style.css.om.AnimationShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.BackgroundBuilderTest;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclarationTest;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheetTest1;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheetTest2;
import io.sf.carte.doc.style.css.om.BorderBuilderTest;
import io.sf.carte.doc.style.css.om.BorderImageBuilderTest;
import io.sf.carte.doc.style.css.om.BorderRadiusBuilderTest;
import io.sf.carte.doc.style.css.om.BoxModelHelperTest;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRuleTest;
import io.sf.carte.doc.style.css.om.CascadeTest;
import io.sf.carte.doc.style.css.om.CompatInlineDeclarationTest;
import io.sf.carte.doc.style.css.om.ComputedCSSStyleTest;
import io.sf.carte.doc.style.css.om.ContribSheetTest;
import io.sf.carte.doc.style.css.om.CounterStyleRuleTest;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetTest;
import io.sf.carte.doc.style.css.om.FlexShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.FontBuilderTest;
import io.sf.carte.doc.style.css.om.FontFaceRuleTest;
import io.sf.carte.doc.style.css.om.FontFeatureValuesRuleTest;
import io.sf.carte.doc.style.css.om.FontVariantBuilderTest;
import io.sf.carte.doc.style.css.om.GenericShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.GridAreaShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.GridPlacementShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.GridShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.GridTemplateShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.ImportRuleTest;
import io.sf.carte.doc.style.css.om.KeyframesRuleTest;
import io.sf.carte.doc.style.css.om.ListStyleShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.MarginBuilderTest;
import io.sf.carte.doc.style.css.om.MediaQueryTest;
import io.sf.carte.doc.style.css.om.MediaRuleTest;
import io.sf.carte.doc.style.css.om.NamespaceRuleTest;
import io.sf.carte.doc.style.css.om.OrderedTwoValueShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.PaddingBuilderTest;
import io.sf.carte.doc.style.css.om.PageRuleTest;
import io.sf.carte.doc.style.css.om.RuleListTest;
import io.sf.carte.doc.style.css.om.SelectorMatcherTest;
import io.sf.carte.doc.style.css.om.SequenceShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.ShorthandSetterTest;
import io.sf.carte.doc.style.css.om.SimpleBoxModelTest;
import io.sf.carte.doc.style.css.om.StylableDocumentWrapperTest;
import io.sf.carte.doc.style.css.om.StyleRuleTest;
import io.sf.carte.doc.style.css.om.StyleRuleTest2;
import io.sf.carte.doc.style.css.om.StyleRuleTestNS;
import io.sf.carte.doc.style.css.om.SupportsRuleTest;
import io.sf.carte.doc.style.css.om.TransitionShorthandBuilderTest;
import io.sf.carte.doc.style.css.om.UnknownRuleTest;
import io.sf.carte.doc.style.css.om.ViewportRuleTest;
import io.sf.carte.doc.style.css.om.XMLDocumentWrapperTest;
import io.sf.carte.doc.style.css.parser.AnBExpressionTest;
import io.sf.carte.doc.style.css.parser.CSSParserTest;
import io.sf.carte.doc.style.css.parser.DeclarationParserTest;
import io.sf.carte.doc.style.css.parser.DeclarationRuleParserTest;
import io.sf.carte.doc.style.css.parser.LexicalUnitTest;
import io.sf.carte.doc.style.css.parser.MediaListTest;
import io.sf.carte.doc.style.css.parser.MediaQueryDatabaseTest;
import io.sf.carte.doc.style.css.parser.NSACMediaQueryTest;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactoryTest;
import io.sf.carte.doc.style.css.parser.ParseHelperTest;
import io.sf.carte.doc.style.css.parser.PropertyParserColorTest;
import io.sf.carte.doc.style.css.parser.PropertyParserTest;
import io.sf.carte.doc.style.css.parser.RuleParserTest;
import io.sf.carte.doc.style.css.parser.SelectorParserNSTest;
import io.sf.carte.doc.style.css.parser.SelectorParserTest;
import io.sf.carte.doc.style.css.parser.SheetParserTest;
import io.sf.carte.doc.style.css.parser.SupportsConditionTest;
import io.sf.carte.doc.style.css.property.AttrValueTest;
import io.sf.carte.doc.style.css.property.CalcValueTest;
import io.sf.carte.doc.style.css.property.ColorValueTest;
import io.sf.carte.doc.style.css.property.CounterValueTest;
import io.sf.carte.doc.style.css.property.CountersValueTest;
import io.sf.carte.doc.style.css.property.CustomPropertyValueTest;
import io.sf.carte.doc.style.css.property.ElementReferenceValueTest;
import io.sf.carte.doc.style.css.property.EnvVariableValueTest;
import io.sf.carte.doc.style.css.property.EvaluatorTest;
import io.sf.carte.doc.style.css.property.FunctionValueTest;
import io.sf.carte.doc.style.css.property.GradientValueTest;
import io.sf.carte.doc.style.css.property.IdentifierValueTest;
import io.sf.carte.doc.style.css.property.InheritValueTest;
import io.sf.carte.doc.style.css.property.LexicalValueTest;
import io.sf.carte.doc.style.css.property.NumberValueTest;
import io.sf.carte.doc.style.css.property.PropertyDatabaseTest;
import io.sf.carte.doc.style.css.property.RatioValueTest;
import io.sf.carte.doc.style.css.property.RectValueTest;
import io.sf.carte.doc.style.css.property.ShorthandDatabaseTest;
import io.sf.carte.doc.style.css.property.StringValueDQTest;
import io.sf.carte.doc.style.css.property.StringValueSQTest;
import io.sf.carte.doc.style.css.property.StringValueTest;
import io.sf.carte.doc.style.css.property.StyleValueTest;
import io.sf.carte.doc.style.css.property.URIValueTest;
import io.sf.carte.doc.style.css.property.UnicodeRangeValueTest;
import io.sf.carte.doc.style.css.property.UnknownValueTest;
import io.sf.carte.doc.style.css.property.ValueFactoryTest;
import io.sf.carte.doc.style.css.property.ValueListTest;
import io.sf.carte.doc.xml.dtd.ContentModelTest;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolverTest;
import io.sf.carte.doc.xml.dtd.DocumentTypeDeclarationTest;
import io.sf.carte.doc.xml.dtd.EntityFinderTest;
import io.sf.carte.doc.xml.dtd.StackedEntityResolverTest;
import io.sf.carte.doc.xml.dtd.StackedEntityResolverTest2;
import io.sf.carte.uparser.TokenProducerTest;
import io.sf.carte.util.BufferSimpleWriterTest;
import io.sf.carte.util.SingleElementIteratorTest;
import io.sf.carte.util.agent.AgentUtilTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	DOMTokenListImplTest.class,
	DOMTokenSetImplTest.class,
	AbstractUserAgentTest.class,
	AgentUtilTest.class,
	CSSDOMImplementationTest.class,
	DOMDocumentTest.class,
	DOMElementTest.class,
	DOMNodeTest.class,
	DOMWriterTest.class,
	FilteredIteratorTest.class,
	HTMLDocumentTest.class,
	HTMLElementTest.class,
	IEDocumentTest.class,
	IteratorTest.class,
	NodeIteratorTest.class,
	CollectionsTest.class,
	TreeWalkerTest.class,
	XHTMLDocumentTest.class,
	XMLDocumentBuilderTest.class,
	XMLDocumentTest.class,
	CSSUnitTest.class,
	AbstractStyleDatabaseTest.class,
		AbstractCSSStyleSheetTest.class,
		AnimationShorthandBuilderTest.class,
		BackgroundBuilderTest.class,
		BaseCSSStyleDeclarationTest.class,
		BaseCSSStyleSheetFactoryTest.class,
		BaseCSSStyleSheetTest1.class,
		BaseCSSStyleSheetTest2.class,
		BorderBuilderTest.class,
		BorderImageBuilderTest.class,
		BorderRadiusBuilderTest.class,
		BoxModelHelperTest.class,
		CascadeTest.class,
		CompatInlineDeclarationTest.class,
		ComputedCSSStyleTest.class,
		ContribSheetTest.class,
		CSSStyleDeclarationRuleTest.class,
		CounterStyleRuleTest.class,
		FontFaceRuleTest.class,
		FontFeatureValuesRuleTest.class,
		ImportRuleTest.class,
		KeyframesRuleTest.class,
		MediaRuleTest.class,
		NamespaceRuleTest.class,
		PageRuleTest.class,
		RuleListTest.class,
		StyleRuleTest.class,
		StyleRuleTest2.class,
		StyleRuleTestNS.class,
		DOMCSSStyleSheetFactoryTest.class,
		DOMCSSStyleSheetTest.class,
		SupportsRuleTest.class,
		TransitionShorthandBuilderTest.class,
		UnknownRuleTest.class,
		ViewportRuleTest.class,
		FlexShorthandBuilderTest.class,
		FontBuilderTest.class,
		FontVariantBuilderTest.class,
		GenericShorthandBuilderTest.class,
		GridShorthandBuilderTest.class,
		GridAreaShorthandBuilderTest.class,
		GridPlacementShorthandBuilderTest.class,
		GridTemplateShorthandBuilderTest.class,
		MarginBuilderTest.class,
		MediaQueryTest.class,
		ListStyleShorthandBuilderTest.class,
		OrderedTwoValueShorthandBuilderTest.class,
		PaddingBuilderTest.class,
		SelectorMatcherTest.class,
		SequenceShorthandBuilderTest.class,
		ShorthandSetterTest.class,
		SimpleBoxModelTest.class,
		StylableDocumentWrapperTest.class,
		XMLDocumentWrapperTest.class,
		AnBExpressionTest.class,
		CSSParserTest.class,
		DeclarationParserTest.class,
		DeclarationRuleParserTest.class,
		LexicalUnitTest.class,
		MediaListTest.class,
		MediaQueryDatabaseTest.class,
		NSACMediaQueryTest.class,
		NSACSelectorFactoryTest.class,
		ParseHelperTest.class,
		PropertyParserColorTest.class,
		PropertyParserTest.class,
		RuleParserTest.class,
		SelectorParserTest.class,
		SelectorParserNSTest.class,
		SheetParserTest.class,
		SupportsConditionTest.class,
		StyleValueTest.class,
		AttrValueTest.class,
		CalcValueTest.class,
		ColorValueTest.class,
		CounterValueTest.class,
		CountersValueTest.class,
		CustomPropertyValueTest.class,
		ElementReferenceValueTest.class,
		EnvVariableValueTest.class,
		EvaluatorTest.class,
		FunctionValueTest.class,
		GradientValueTest.class,
		IdentifierValueTest.class,
		InheritValueTest.class,
		LexicalValueTest.class,
		NumberValueTest.class,
		RectValueTest.class,
		PropertyDatabaseTest.class,
		RatioValueTest.class,
		ShorthandDatabaseTest.class,
		StringValueTest.class,
		StringValueDQTest.class,
		StringValueSQTest.class,
		UnicodeRangeValueTest.class,
		UnknownValueTest.class,
		URIValueTest.class,
		ValueListTest.class,
		ValueFactoryTest.class,
		ContentModelTest.class,
		DefaultEntityResolverTest.class,
		DocumentTypeDeclarationTest.class,
		EntityFinderTest.class,
		StackedEntityResolverTest.class,
		StackedEntityResolverTest2.class,
		TokenProducerTest.class,
		BufferSimpleWriterTest.class,
		SingleElementIteratorTest.class
		})
public class AllTests {
}
