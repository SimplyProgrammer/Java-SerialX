package org.ugp.serialx.converters;

import org.ugp.serialx.Registry;

/**
 * This is DataParser with extended functionality! {@link DataConverter} can also parse data like DataParser but is also capable of converting them back to string!
 * This to string convertation is performed by {@link DataConverter#toString(Object)} and result of this convertation supposed to be parsable by {@link DataConverter#parse(String, Object...)} meaning one converter supposed to be parsing and converting via the same string format!
 * 
 * @see DataParser
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public interface DataConverter extends DataParser
{
	/**
	 * @param myHomeRegistry | Registry where this parser is registered provided by {@link DataParser#parseObj(Registry, String, boolean, Class[], Object...)} otherwise it demands on implementation (it should not be null)!
	 * @param obj | Object to convert into string!
	 * @param args | Some additional args. This can be anything and it demands on implementation of DataConverter. Default SerialX API implementation will provide some flags about formating (2 ints)!
	 * 
	 * @return Object converted to string. Easiest way to do this is obj.toString() but you most likely want some more sofisticated formating.
	 * Return {@link DataParser#CONTINUE} to tell that this converter is not suitable for converting this object! You most likely want to do this when obtained obj is not suitable instance!
	 * 
	 * @since 1.3.0
	 */
	CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args);
	
	/**
	 * @param myHomeRegistry | Registry of parsers (might be null)!
	 * @param objToDescribe | Object to generate description for!
	 * @param argsUsedConvert | Array of arguments that were used for converting described object!
	 * 
	 * @return Description for object (should not contains endlines)!
	 * 
	 * @since 1.3.0
	 */
	default CharSequence getDescription(ParserRegistry myHomeRegistry, Object objToDescribe, Object... argsUsedConvert) 
	{
		return "Object of " + objToDescribe.getClass().getName() + ": \""  + objToDescribe + "\" converted by " + this;
	}
	
	/**
	 * @param obj | Object to convert into string!
	 * @param args | Additional arguments that will be obtained in {@link DataParser#toString(String, Object...)}!
	 * 
	 * @return Object converted to string using {@link DataConverter} suitable converter picked from {@link DataParser#REGISTRY}!
	 * {@link DataConverter#toString(Object, Object...)} of all registered converters will be called however only suitable ones should return the result, others should return {@link DataParser#CONTINUE}!
	 * 
	 * @since 1.3.0
	 */
	public static CharSequence objToString(Object obj, Object... args)
	{
		return REGISTRY.toString(obj, args);
	}
	
	/**
	 * @return <code>"Object of " + objToDescribe.getClass().getName() + ": \""  + objToDescribe + "\" converted by " + DataParser.class.getName()</code>
	 * 
	 * @since 1.3.2
	 */
	public static String getDefaultDescriptionFor(Object objToDescribe)
	{
		return "Object of " + objToDescribe.getClass().getName() + ": \""  + objToDescribe + "\" converted by " + DataConverter.class.getName(); 
	}
}
