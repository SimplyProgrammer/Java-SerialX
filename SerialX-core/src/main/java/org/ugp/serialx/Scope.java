package org.ugp.serialx;

import static org.ugp.serialx.converters.DataParser.VOID;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

import org.ugp.serialx.converters.ArrayConverter;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;

	
/**
 * This is some kind of hybrid between {@link List} and {@link Map} which allow you to have both variables and independent values managed by one Object. <br>
 * Note: Variables are managed and accessed classically via {@link Map} methods such as <code>put(String key, Object)</code> and array of independent values is accessed by via {@link List} methods such as <code>add(Object)</code> and <code>get(int)</code><br>
 * Also this is java representation of JUSS Scope group such as:
 * <pre>
 * <code>
 * {
 *     //This is scope in JUSS!
 * }
 * </code>
 * </pre>
 * 
 * @author PETO
 * 
 * @since 1.2.0 and since 1.3.5 splitted to {@link Scope} and {@link GenericScope}
 */
public class Scope extends GenericScope<String, Object> 
{
	private static final long serialVersionUID = 4693418156224566721L;

	/**
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.5
	 */
	@SafeVarargs
	public Scope(Object... values) 
	{
		this(null, values);
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.5
	 */
	@SafeVarargs
	public Scope(Map<String, ?> variablesMap, Object... values) 
	{
		this(variablesMap, values == null ? null : Arrays.asList(values));
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.5
	 */
	public Scope(Map<String, ?> variablesMap, Collection<?> values) 
	{
		this(variablesMap, values, null);
	}
	
	/**
	 * @param sourceScope | Scope with initial content!
	 * 
	 * @since 1.3.5
	 */
	public Scope(GenericScope<String, ?> sourceScope)
	{
		this(sourceScope == null ? null : sourceScope.variables(), sourceScope == null ? null : sourceScope.values());
	}

	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * @param | Parent of this scope.

	 * @since 1.3.5
	 */
	public Scope(Map<String, ?> variablesMap, Collection<?> values, GenericScope<?, ?> parent) 
	{
		super(variablesMap, values, parent);
	}
	
	@Override
	public Scope clone() 
	{
		return (Scope) super.clone();
	}
	
	/**
	 * @param variableName | Variables name.
	 * 
	 * @return Byte value of variable with name or 0 if there is no such a one!
	 * Byte will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public byte getByte(String variableName)
	{
		return getByte(variableName, (byte) 0);
	}
	
	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Byte value of variable with name or defaultValue if there is no such a one!
	 * Byte will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public byte getByte(String variableName, byte defaultValue)
	{
		return (byte) getInt(variableName, defaultValue);
	}
	
	/**
	 * @param variableName | Variables name.
	 * 
	 * @return Byte value of variable with name or 0 if there is no such a one!
	 * Byte will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public short getShort(String variableName)
	{
		return getShort(variableName, (short) 0);
	}
	
	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Byte value of variable with name or defaultValue if there is no such a one!
	 * Byte will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public short getShort(String variableName, short defaultValue)
	{
		return (short) getInt(variableName, defaultValue);
	}
	
	/**
	 * @param variableName | Variables name.
	 * 
	 * @return Int value of variable with name or 0 if there is no such a one!
	 * Int will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public int getInt(String variableName)
	{
		return getInt(variableName, 0);
	}
	
	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Int value of variable with name or defaultValue if there is no such a one!
	 * Int will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public int getInt(String variableName, int defaultValue)
	{
		return (int) getLong(variableName, defaultValue);
	}
	
	/**
	 * @param variableName | Variables name.
	 * 
	 * @return Long value of variable with name or 0 if there is no such a one!
	 * Long will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public int getLong(String variableName)
	{
		return getInt(variableName, 0);
	}
	
	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Long value of variable with name or defaultValue if there is no such a one!
	 * Long will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public long getLong(String variableName, long defaultValue)
	{
		return (long) getDouble(variableName, defaultValue);
	}
	
	/**
	 * @param variableName | Variables name.
	 * 
	 * @return Float value of variable with name or 0 if there is no such a one!
	 * Float will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public float getFloat(String variableName)
	{
		return getFloat(variableName, 0);
	}
	
	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Float value of variable with name or defaultValue if there is no such a one!
	 * Float will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public float getFloat(String variableName, float defaultValue)
	{
		return (float) getDouble(variableName, defaultValue);
	}
	
	/**
	 * @param variableName | Variables name.
	 * 
	 * @return Double value of variable with name or 0 if there is no such a one!
	 * Double will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public double getDouble(String variableName)
	{
		return getDouble(variableName, 0);
	}
	
	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Double value of variable with name or defaultValue if there is no such a one!
	 * Double will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public double getDouble(String variableName, double defaultValue)
	{
		Object obj = get(variableName, defaultValue);
		if (obj instanceof Number)
			return ((Number) obj).doubleValue();
		else if (obj instanceof Character)
			return (double) (char) obj;
		else if (obj instanceof CharSequence)
			return Double.parseDouble(obj.toString());
		return (double) obj;
	}
	
	/**
	 * @param variableName | Variables name.
	 * 
	 * @return String value of variable with name or null if there is no such a one!
	 * String will be also parsed from any object using {@link Object#toString()}!
	 * 
	 * @since 1.2.5
	 */
	public String getString(String variableName)
	{
		return getString(variableName, null);
	}
	
	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return String value of variable with name or defaultValue if there is no such a one!
	 * String will be also parsed from any object using {@link Object#toString()}!
	 * 
	 * @since 1.2.5
	 */
	public String getString(String variableName, String defaultValue)
	{
		return String.valueOf(get(variableName, defaultValue));
	}
	
	/**
	 * @param variableName | Variables name.
	 * 
	 * @return Char value of variable with name or {@code (char) 0} if there is no such a one!
	 * Char will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public char getChar(String variableName)
	{
		return getChar(variableName, (char) 0);
	}
	
	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Char value of variable with name or defaultValue if there is no such a one!
	 * Char will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public char getChar(String variableName, char defaultValue)
	{
		Object obj = get(variableName, defaultValue);
		if (obj instanceof Character)
			return (char) obj;
		else if (obj instanceof CharSequence)
			return ((CharSequence) obj).charAt(0);
		else if (obj instanceof Number)
			return (char) ((Number) obj).intValue();
		return (char) obj;
	}

	/**
	 * @param variableName | Variables name.
	 * 
	 * @return Boolean value of variable with name or false if there is no such a one!
	 * Boolean will be also parsed from {@link Number}, or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public boolean getBool(String variableName) 
	{
		return getBool(variableName, false);
	}

	/**
	 * @param variableName | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Boolean value of variable with name or defaultValue if there is no such a one!
	 * Boolean will be also parsed from {@link Number}, or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public boolean getBool(String variableName, boolean defaultValue) 
	{
		Object obj = get(variableName, defaultValue);
		if (obj instanceof Boolean)
			return (boolean) obj;
		else if (obj instanceof Number)
			return ((Number) obj).doubleValue() > 0;
		else
		{
			String str = obj.toString();
			if (str.equalsIgnoreCase("t"))
				return true;
			else if (str.equalsIgnoreCase("f"))
				return false;
			return Boolean.parseBoolean(obj.toString());
		}
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent byte value with valueIndex.
	 * Byte will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public byte getByte(int valueIndex)
	{
		return (byte) getInt(valueIndex);
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent short value with valueIndex.
	 * Byte will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public short getShort(int valueIndex)
	{
		return (short) getInt(valueIndex);
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent int value with valueIndex.
	 * Int will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public int getInt(int valueIndex)
	{
		return (int) getLong(valueIndex);
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent long value with valueIndex.
	 * Long will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public long getLong(int valueIndex)
	{
		return (long) getDouble(valueIndex);
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent float value with valueIndex.
	 * Float will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public float getFloat(int valueIndex)
	{
		return (float) getDouble(valueIndex);
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent double value with valueIndex.
	 * Double will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public double getDouble(int valueIndex)
	{
		Object obj = get(valueIndex);
		if (obj instanceof Number)
			return ((Number) obj).doubleValue();
		else if (obj instanceof Character)
			return (double) (char) obj;
		else if (obj instanceof CharSequence)
			return Double.parseDouble(obj.toString());
		return (double) obj;
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent string value with valueIndex.
	 * String will be also parsed from any object using {@link Object#toString()}!
	 * 
	 * @since 1.2.5
	 */
	public String getString(int valueIndex)
	{
		return String.valueOf(get(valueIndex));
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent char value with valueIndex.
	 * Char will be also parsed from {@link Number}, {@link Character} or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public char getChar(int valueIndex)
	{
		Object obj = get(valueIndex);
		if (obj instanceof Character)
			return (char) obj;
		else if (obj instanceof CharSequence)
			return ((CharSequence) obj).charAt(0);
		else if (obj instanceof Number)
			return (char) ((Number) obj).intValue();
		return (char) obj;
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent boolean value with valueIndex.
	 * Boolean will be also parsed from {@link Number}, or {@link CharSequence} if possible!
	 * 
	 * @since 1.2.5
	 */
	public boolean getBool(int valueIndex) 
	{
		Object obj = get(valueIndex);
		if (obj instanceof Boolean)
			return (boolean) obj;
		else if (obj instanceof Number)
			return ((Number) obj).doubleValue() > 0;
		else
		{
			String str = obj.toString();
			if (str.equalsIgnoreCase("t"))
				return true;
			else if (str.equalsIgnoreCase("f"))
				return false;
			return Boolean.parseBoolean(obj.toString());
		}
	}
	
	/**
	 
	 
	 * @param scopeValueIndex | Index of sub-scopes value.
	 * 
	 * @return Sub-scope on required index or null if there is no scope on required index!<br>
	 * Note: Keep in mind that you need to insert valid index according to other values. Scopes share same index order with other values!
	 * 
	 * @since 1.2.0
	 */
	public Scope getScope(int scopeValueIndex)
	{
		try
		{
			return (Scope) this.<String, Object>getGenericScope(scopeValueIndex);
		}
		catch (ClassCastException e)
		{
			return null;
		}
	}
	
	/**
	 * @param scopesOrderIndex | Order index of sub-scope.
	 * 
	 * @return Sub-scope with required number. Similar to {@link Scope#getScope(int)} however this will ignore non scope values.
	 * <br><br>
	 * For instance <code>getSubScope(1)</code> in context: <br>
	 * <pre>
	 * <code>
	 * variable = "something";
	 * "something";
	 * {
	 *      "Scope0"
	 * };
	 * 123;
	 * null;
	 * {
	 *      "Scope1" <- This one will be returned!<br>
	 * };
	 * 4;
	 * 5;
	 * 6;
	 * {
	 *      "Scope2"
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @since 1.2.0
	 */
	public Scope getSubScope(int subscopesOrderIndex)
	{
		for (int i = 0, j = 0; i < valuesCount(); i++)
		{
			Scope scope = getScope(i);
			if (scope != null && j++ == subscopesOrderIndex)
				return scope;
		}
		return null;
	}
	
	/**
	 * @param scopesPath | Array with variable names creating path to required scope.
	 * 
	 * @return Sub-scope stored by variable with required name (last element) in inserted path or null if there is no such a one in inserted path. If there is more than one result, the first one found will be returned!
	 * This search will also includes sub-scopes of scope but variables from lower ones are prioritize! <br>
	 * If this function is called with no arguments then self will be returned!
	 * <br>
	 * <pre>
	 * <code>
	 * variable = "something";
	 * scope1 = 
	 * {
	 *      subScope = 
	 *      {
	 *          scopeObjectoFind = {...}; <- this one will be returned if getScope("scope1", "scopeObjectoFind") is issued!
	 *          7;...
	 *      }
	 * };
	 * scopeObjectoFind = {...} <- this one will be returned if getScope("scopeObjectoFind") is issued!
	 * </code>
	 * </pre>
	 * Note: Remember that this search includes variables only, no values!
	 * 
	 * @since 1.2.0
	 */
	public Scope getScope(String... scopesPath)
	{
		try
		{
			return (Scope) getGenericScope(scopesPath);
		}
		catch (ClassCastException e)
		{
			return null;
		}
	}
	
	/**
	 * @param scopesValueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * @param objClass | Object of class to create.
	 * 
	 * @return Object of objClass constructed from independent value with scopesValueIndex! If there is no Scope<Object, ?> stored at scopesValueIndex this function behaves same as {@link GenericScope#get(int)}!
	 * If independent value is {@link GenericScope} like it supposed to, then values of scope are parsed to {@link SerializationProtocol} of objClass.
	 * Note: Scopes are searched via regular independent value index no scope index like {@link Scope#getScope(int)}.
	 * 
	 * @see Serializer#PROTOCOL_REGISTRY
	 * @see GenericScope#toObject(Class)
	 * 
	 * @throws Exception if Exception occurred in {@link SerializationProtocol#unserialize(Class, Object...)}!
	 * 
	 * @since 1.2.5
	 */
	@SuppressWarnings("unchecked")
	public <T> T toObjectOf(int scopesValueIndex, Class<T> objClass) throws Exception
	{
		Object obj = get(scopesValueIndex);
		if (obj instanceof GenericScope)
			return ((GenericScope<?, ?>) obj).toObject(objClass);
		return (T) obj;
	}
	
	/**
	 * @param variableWithScope | Variable that supposed to contains scope.
	 * @param objClass | Object of class to create.
	 * 
	 * @return Value of variable with name or null if there is no such a one! If there is no Scope<Object, ?> stored by variableWithScope this function behaves same as {@link Scope#get(String, Object)}!
	 * If variableWithScope contains {@link GenericScope} like it supposed to, then values of scope are parsed to {@link SerializationProtocol} of objClass.
	 * 
	 * @see Serializer#PROTOCOL_REGISTRY
	 * @see GenericScope#toObject(Class)
	 * 
	 * @throws Exception if Exception occurred in {@link SerializationProtocol#unserialize(Class, Object...)}!
	 * 
	 * @since 1.2.5
	 */
	public <T> T toObjectOf(String variableWithscope, Class<T> objClass) throws Exception
	{
		return toObjectOf(variableWithscope, objClass, null);
	}
	
	/**
	 * @param variableWithScope | Variable that supposed to contains scope.
	 * @param objClass | Object of class to create.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Value of variable with name or defaultValue if there is no such a one! If there is no Scope<Object, ?> stored by variableWithScope this function behaves same as {@link Scope#get(String, Object)}!
	 * If variableWithScope contains {@link GenericScope} like it supposed to, then values of scope are parsed to {@link SerializationProtocol} of objClass.
	 * 
	 * @see Serializer#PROTOCOL_REGISTRY
	 * @see GenericScope#toObject(Class)
	 * 
	 * @throws Exception if Exception occurred in {@link SerializationProtocol#unserialize(Class, Object...)}!
	 * 
	 * @since 1.2.5
	 */
	public <T> T toObjectOf(String variableWithscope, Class<T> objClass, T defaultValue) throws Exception
	{
		return toObjectOf(variableWithscope, objClass, defaultValue, SerializationProtocol.REGISTRY);
	}
	
	/**
	 * @param variableWithScope | Variable that supposed to contains scope.
	 * @param objClass | Object of class to create.
	 * @param defaultValue | Default value to return.
	 * @param protocolsToUse | Registry of protocols to use.
	 * 
	 * @return Value of variable with name or defaultValue if there is no such a one! If there is no Scope<Object, ?> stored by variableWithScope this function behaves same as {@link Scope#get(String, Object)}!
	 * If variableWithScope contains {@link GenericScope} like it supposed to, then values of scope are parsed to {@link SerializationProtocol} of objClass.
	 * 
	 * @see Serializer#PROTOCOL_REGISTRY
	 * @see GenericScope#toObject(Class)
	 * 
	 * @throws Exception if Exception occurred in {@link SerializationProtocol#unserialize(Class, Object...)}!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public <T> T toObjectOf(String variableWithscope, Class<T> objClass, T defaultValue, ProtocolRegistry protocolsToUse) throws Exception
	{
		T obj = get(variableWithscope, defaultValue);
		if (obj instanceof GenericScope)
			return ((GenericScope<String, ?>) obj).toObject(objClass, protocolsToUse);
		return obj;
	}
	
	/**
	 * @param varName | Variable with name to search!
	  Expected generic type of possible results, {@link ClassCastException} will be thrown is some variable contains value that can't be cast to this!
	 * 
	 * @return Values stored by variable with inserted name collected from this scope!
	 * 
	 * @since 1.3.0
	 */
	public <V> List<V> getAllStoredBy(String varName)
	{
		return getAllStoredBy(varName, true);
	}
	
	/**
	 * @param varName | Variable with name to search!
	 * @param includeSubScopes | If true, this search will include sub-scopes as well (variables are iterated first values second)!
	 * Expected generic type of possible results, {@link ClassCastException} will be thrown is some variable contains value that can't be cast to this!
	 * 
	 * @return Values stored by variable with inserted name collected from this scope!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public <V> List<V> getAllStoredBy(String varName, boolean includeSubScopes)
	{
		List<V> result = new ArrayList<>();
		
		for (Entry<String, Object> var : varEntrySet())
			if (var.getKey().equals(varName))
				result.add((V) var.getValue());
			else if (includeSubScopes && var.getValue() instanceof Scope)
				result.addAll(((Scope) var.getValue()).getAllStoredBy(varName, includeSubScopes));
		
		for (Object object : this) 
			if (object instanceof Scope)
				result.addAll(((Scope) object).getAllStoredBy(varName, includeSubScopes));
		
		return result;
	}
	
	/**
	 * @param varName | Variable with name to search!
	 * @param value | Value of variable to search!
	 * 
	 * @return Scope containing all sub-scopes of this scope that contains variable with required name that is set to required value!
	 * 
	 * @since 1.3.2
	 */
	public Scope getScopesWith(String varName, Object value)
	{
		return getScopesWith(varName, value, true);
	}
	
	/**
	 * @param varName | Variable with name to search!
	 * @param value | Value of variable to search!
	 * @param includeSubScopes | If true, this search will include sub-scopes as well (variables are iterated first values second)!
	 * 
	 * @return Scope containing all sub-scopes of this scope that contains variable with required name that is set to required value!
	 * 
	 * @since 1.3.2
	 */
	public Scope getScopesWith(String varName, Object value, boolean includeSubScopes)
	{
		return getScopesWith(varName, obj -> Objects.deepEquals(obj, value));
	}
	
	/**
	 * @param varName | Variable with name to search!
	 * @param condition | Condition that will be tested for value of each variable!
	 * 
	 * @return Scope containing all sub-scopes of this scope that contains variable which meets inserted condition!
	 * 
	 * @since 1.3.2
	 */
	public Scope getScopesWith(String varName, Predicate<Object> condition)
	{
		return getScopesWith(varName, condition, true);
	}
	
	/**
	 * @param varName | Variable with name to search!
	 * @param condition | Condition that will be tested for value of each variable!
	 * @param includeSubScopes | If true, this search will include sub-scopes as well (variables are iterated first values second)!
	 * 
	 * @return Scope containing all sub-scopes of this scope that contains variable which meets inserted condition!
	 * 
	 * @since 1.3.2
	 */
	public Scope getScopesWith(String varName, Predicate<Object> condition, boolean includeSubScopes)
	{
		Scope result = new Scope(null, null, getParent());
		
		for (Entry<String, Object> myVar : varEntrySet())
			if (myVar.getValue() instanceof Scope)
				try
				{
					Scope scope = (Scope) myVar.getValue();
					if (scope.containsVariable(varName) && condition.test(scope.get(varName)))
						result.add(scope);
					else if (includeSubScopes)
						result.addAll(scope.getScopesWith(varName, condition, includeSubScopes));
				}
				catch (ClassCastException e)
				{}
		for (Object object : this) 
			if (object instanceof Scope)
				try
				{
					Scope scope = (Scope) object;
					if (scope.containsVariable(varName) && condition.test(scope.get(varName)))
						result.add(scope);
					else if (includeSubScopes)
						result.addAll(scope.getScopesWith(varName, condition, includeSubScopes));
				}
				catch (ClassCastException e)
				{}
		return result;
	}
	
	/**
	 * @param objClass | Object of class to create using protocols.
	 * @param protocolsToUse | Registry of protocols to use.
	 * 
	 * @return Object of objClass constructed from this scopes independent values using protocol for objClass or null if there was no protocol found in {@link Serializer#PROTOCOL_REGISTRY}! 
	 * If there were no suitable deserialization protocols found, {@link Scope#into(Class, String...)} will be used!
	 * 
	 * @throws Exception | Exception if Exception occurred in {@link SerializationProtocol#unserialize(Class, Object...)}!
	 * 
	 * @see Scope#into(Class, String...)
	 * @see Scope#intoNew(Class, GenericScope, String...)
	 * 
	 * @since 1.3.5
	 */
	public <T> T toObject(Class<T> objClass, ProtocolRegistry protocolsToUse) throws Exception
	{
		T obj = super.toObject(objClass, protocolsToUse);
		if (obj != null)
			return obj;
		
		try
		{
			return into(objClass);
		}
		catch (Exception e)
		{
			LogProvider.instance.logErr("Unable to create new instance of " + objClass.getName() + " because none of provided protocols were suitable and class introspection has failed as well!", e);
			return null;
		}
	}
	
	/**
	 * @param objCls | Class of object to instantiate!
	 * @param fieldNamesToUse | Array of objCls field names to map/populate instantiated object from scopes variables using setters (write method)! PropertyDescriptors of these fields will be obtained using Scope.getPropertyDescriptorsOf(Class, String)! This is used only as a last (default) option!
	 * 
	 * @return New instance of object according to {@link GenericScope#intoNew(Class, GenericScope, String...)} similarly to {@link GenericScope#toObject(Class)} except this one will not use any protocols!
	 * 
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often)!
	 * @throws IntrospectionException when there were no {@link PropertyDescriptor} found for obj class!
	 * 
	 * @see Scope#intoNew(Class, GenericScope, String...)
	 * 
	 * @since 1.3.5  
	 */
	public <T> T into(Class<T> objCls, String... fieldNamesToUse) throws IntrospectionException, Exception
	{
		return (T) intoNew(objCls, this, fieldNamesToUse);
	}
	
	/**
	 * @param obj | Object to map this scopes variables into!
	 * @param fieldNamesToUse | Array of objCls field names to map/populate instantiated object from scopes variables using setters (write method)! PropertyDescriptors of these fields will be obtained using Scope.getPropertyDescriptorsOf(Class, String)! This is used only as a last (default) option!
	 * 
	 * @return New instance of object according to {@link GenericScope#intoNew(Class, GenericScope, String...)} similarly to {@link GenericScope#toObject(Class)} except this one will not use any protocols!
	 * 
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often)!
	 * @throws IntrospectionException when there were no {@link PropertyDescriptor} found for obj class!
	 * 
	 * @see Scope#intoNew(Class, GenericScope, String...)
	 * 
	 * @since 1.3.5  
	 */
	public <T> T into(Object obj, String... fieldNamesToUse) throws IntrospectionException, Exception
	{
		return into(obj, this, fieldNamesToUse);
	}
	
	/**
	 * @param obj | Object to create scope from!
	 * @param fieldNamesToUse | Array of obj field names to map into scopes variables using getters (read method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link GenericScope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
	 * 
	 * @return Scope created from given obj by mapping obj's fields into variables of created scope via given fields (fieldNamesToUse) and conversion rules listed below!!<br><br>
	 * Table of specific Object --> Scope conversions:
	 * 	<style>
			table, th, td 
			{
			  border: 1px solid gray;
			}
		</style>
		<table>
			<tr>
			    <th>Object (obj) type</th>
			    <th>Obtained scope (return)</th> 
			</tr>
			<tr>
			    <td>null</td>
			    <td>new Scope<>()</td>
		  	</tr>
		    <tr>
			    <td>{@link Array} (primitive)</td>
			    <td>Array elements will become independent values of new scope</td>
			</tr>
			<tr>
			    <td>{@link GenericScope}</td>
			    <td>Clone of scope {@link GenericScope#clone()}</td>
			</tr>
			<tr>
			    <td>{@link Collection}</td>
			    <td>Element of collection will become independent values of new scope</td>
		  	</tr>
		  	<tr>
			    <td>{@link Map}</td>
			    <td>Entries of map will become variables of new scope</td>
			</tr>
		  	<tr>
			    <td>Others (default)</td>
			    <td>{@link Scope#from(Object, List)} (return description)</td>
			</tr>
		</table>
	 * 
	 * @throws Exception if calling of some {@link PropertyDescriptor}s read method fails!
	 * @throws IntrospectionException when there were no {@link PropertyDescriptor} found for obj class!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public static Scope from(Object obj, String... fieldNamesToUse) throws Exception, IntrospectionException
	{
		if (obj == null)
			return new Scope();
		
		if (obj.getClass().isArray())
			return new Scope(ArrayConverter.fromAmbiguous(obj));
		
		if (obj instanceof Scope)
		{
			Scope scope = ((Scope) obj).clone();
			if (fieldNamesToUse != null && fieldNamesToUse.length > 0)
				scope.variables().keySet().retainAll(Arrays.asList(fieldNamesToUse));
			return scope;
		}
		
		if (obj instanceof Collection)
			return new Scope(null, (Collection<?>) obj);
		
		if (obj instanceof Map)
		{
			if (fieldNamesToUse != null && fieldNamesToUse.length > 0)
				((Map<String, ?>) obj).keySet().retainAll(Arrays.asList(fieldNamesToUse));
			return new Scope((Map<String, ?>) obj, (Collection<?>) null);
		}
		
		return from(obj, getPropertyDescriptorsOf(obj.getClass(), fieldNamesToUse));
	}
	
	/**
	 * @param obj | Object to create scope from!
	 * @param fieldsToUse | List of {@link PropertyDescriptor}s representing fields of object to map into scopes variables using getters (read method)!
	 * 
	 * @return Scope created from given obj by mapping obj's fields into variables of created scope via given {@link PropertyDescriptor}s (fieldsToUse)!
	 * 
	 * @throws Exception if calling of some {@link PropertyDescriptor}s read method fails (should not happen often)!
	 * 
	 * @since 1.3.5
	 */
	public static Scope from(Object obj, List<PropertyDescriptor> fieldsToUse) throws Exception
	{
		return from(new Scope(), obj, fieldsToUse);
	}
	
	/**
	 * @param newInstance | New instance of specific {@link Scope}!
	 * @param obj | Object to create scope from!
	 * @param fieldsToUse | List of {@link PropertyDescriptor}s representing fields of object to map into scopes variables using getters (read method)!
	 * 
	 * @return Scope (newInstance) structured from given obj by mapping obj's fields into variables of created scope via given {@link PropertyDescriptor}s (fieldsToUse)!
	 * 
	 * @throws Exception if calling of some {@link PropertyDescriptor}s read method fails (should not happen often)!
	 * 
	 * @since 1.3.5
	 */
	public static Scope from(Scope newInstance, Object obj, List<PropertyDescriptor> fieldsToUse) throws Exception
	{
		//if (variablesToUse != null)
		//variablesToUse = AutoProtocol.getPropertyDescriptorsOf(obj.getClass());
		for (PropertyDescriptor var : fieldsToUse) 
			newInstance.put(var.getName(), var.getReadMethod().invoke(obj));
		return newInstance;
	}
	
	/**
	 * @param objCls | Class of object to instantiate!
	 * @param fromScope | Source scope with data to instantiate from!
	 * @param fieldNamesToUse | Array of objCls field names to map/populate instantiated object from scopes variables using setters (write method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link Scope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
	 * 
	 * @return Instantiated object populated/mapped with required variables of fromScope (fieldNamesToUse) and conversion rules listed below! {@link Serializer#Instantiate(Class)} will be used for object instantiation!<br><br>
	 * 	<style>
			table, th, td 
			{
			  border: 1px solid gray;
			}
		</style>
		<table>
			<tr>
			    <th>Class/type (objCls)</th>
			    <th>New object instance (return)</th> 
			</tr>
			<tr>
			    <td>null</td>
			    <td>null</td>
		  	</tr>
		    <tr>
			    <td>{@link Array} (primitive)</td>
			    <td>New primitive array populated with all independent values of fromScope</td>
			</tr>
			<tr>
			    <td>{@link GenericScope}</td>
			    <td>Clone of fromScope</td>
			</tr>
			<tr>
			    <td>{@link Collection}</td>
			    <td>New collection instance with all independent values of fromScope</td>
		  	</tr>
		  	<tr>
			    <td>{@link Map}</td>
			    <td>New map instance with all variables of fromScope</td>
			</tr>
		  	<tr>
			    <td>Others (default)</td>
			    <td>{@link Scope#into(Object, GenericScope, List)} (return description)</td>
			</tr>
		</table>
	 * 
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often)!
	 * @throws IntrospectionException when there were no {@link PropertyDescriptor} found for obj class!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public static <T> T intoNew(Class<T> objCls, GenericScope<String, ?> fromScope, String... fieldNamesToUse) throws IntrospectionException, Exception
	{
		if (objCls == null)
			return null;
		
		if (objCls.isArray())
		{
			if (objCls.getComponentType() == Object.class)
				return (T) fromScope.toValArray();
			
			return (T) into(Array.newInstance(objCls.getComponentType(), fromScope.valuesCount()), fromScope, fieldNamesToUse);
		}
		
		if (GenericScope.class.isAssignableFrom(objCls))
		{
			GenericScope<String, Object> result = ((GenericScope<String, Object>) fromScope).clone((Class<GenericScope<String, Object>>)objCls);
			if (fieldNamesToUse != null && fieldNamesToUse.length > 0)
				result.variables().keySet().retainAll(Arrays.asList(fieldNamesToUse));
			return (T) result;
		}

		if (objCls.isInterface())
		{
			if (Collection.class.isAssignableFrom(objCls))
			{
				return (T) fromScope.toValList();
			}
			
			if (Map.class.isAssignableFrom(objCls))
			{
				Map<?, ?> map = fromScope.toVarMap();
				if (fieldNamesToUse != null && fieldNamesToUse.length > 0)
					map.keySet().retainAll(Arrays.asList(fieldNamesToUse));
				return (T) map;
			}
		}
		
		return into(Serializer.Instantiate(objCls), fromScope, fieldNamesToUse);
	}
	
	/**
	 * @param obj | Object to map scopes variables into!
	 * @param fromScope | Source scope with variables to use for mapping!
	 * @param fieldNamesToUse | Array of obj field names to map/populate from scopes variables using setters (write method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link Scope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
	 * 
	 * @return Same obj after being populated/mapped by variables of given scope via requested fields (fieldNamesToUse) and conversion rules listed below!<br><br>
	 * Table of specific Scope --> Object conversions:
	 * 	<style>
			table, th, td 
			{
			  border: 1px solid gray;
			}
		</style>
		<table>
			<tr>
			    <th>Object (obj) type</th>
			    <th>Action with obj (return)</th> 
			</tr>
			<tr>
			    <td>null</td>
			    <td>null</td>
		  	</tr>
		    <tr>
			    <td>{@link Array} (primitive)</td>
			    <td>Independent values of fromScope will be injected into array</td>
			</tr>
			<tr>
			    <td>{@link GenericScope}</td>
			    <td>Content of fromScope will be added into Scope</td>
			</tr>
			<tr>
			    <td>{@link Collection}</td>
			    <td>Independent values of fromScope will be added to Collection</td>
		  	</tr>
		  	<tr>
			    <td>{@link Map}</td>
			    <td>Variables of fromScope will be added to Map</td>
			</tr>
		  	<tr>
			    <td>Others (default)</td>
			    <td>{@link Scope#into(Object, GenericScope, List)} (return description)</td>
			</tr>
		</table>
	 * 
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often)!
	 * @throws IntrospectionException when there were no {@link PropertyDescriptor} found for obj class!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public static <T> T into(Object obj, GenericScope<String, ?> fromScope, String... fieldNamesToUse) throws IntrospectionException, Exception
	{
		if (obj == null)
			return null;
		if (obj.getClass().isArray())
		{
			for (int i = 0, arrLen = Array.getLength(obj), scSize = fromScope.valuesCount(); i < arrLen && i < scSize; i++)
				Array.set(obj, i, fromScope.get(i));
			return (T) obj;
		}
		
		if (obj instanceof GenericScope)
		{
			((GenericScope<String, Object>) obj).addAll(fromScope);
			if (fieldNamesToUse != null && fieldNamesToUse.length > 0)
				((GenericScope<String, Object>) obj).variables().keySet().retainAll(Arrays.asList(fieldNamesToUse));
			return (T) obj;
		}
		
		if (obj instanceof Collection)
		{	
			((Collection<Object>) obj).addAll(fromScope.values());
			return (T) obj;
		}
		
		if (obj instanceof Map)
		{
			((Map<Object, Object>) obj).putAll(fromScope.variables());
			if (fieldNamesToUse != null && fieldNamesToUse.length > 0)
				((Map<Object, Object>) obj).keySet().retainAll(Arrays.asList(fieldNamesToUse));
			return (T) obj;
		}
		
		return (T) into(obj, fromScope, getPropertyDescriptorsOf(obj.getClass(), fieldNamesToUse));
	}
	
	/**
	 * @param obj | Object to map scopes variables into!
	 * @param fromScope | Source scope with variables to use for mapping!
	 * @param fieldsToUse | List of {@link PropertyDescriptor}s representing fields of object to map/populate from scopes variables using setters (write method)!
	 * 
	 * @return Same obj after being populated/mapped by variables of given scope via requested {@link PropertyDescriptor}s (fieldsToUse)!
	 * 
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often)!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public static <T> T into(T obj, GenericScope<String, ?> fromScope, List<PropertyDescriptor> fieldsToUse) throws Exception
	{
		for (PropertyDescriptor var : fieldsToUse) 
		{
			Object varValue = ((GenericScope<String, Object>) fromScope).get(var.getName(), VOID);
			if (varValue != VOID)
				var.getWriteMethod().invoke(obj, varValue);
		}
		return obj;
	}
	
	/**
	 * @param cls | Class to inspect!
	 * @param fieldNames | Names of fields to get descriptors for, if this array is empty or null, descriptors for all fields with public getters and setters will be obtained!
	 * 
	 * @return List of {@link PropertyDescriptor}s of cls representing access methods of required fields! Only descriptors of fields that have valid and public getter and setter will be returned! 
	 * 
	 * @throws IntrospectionException when there are no suitable fields with valid and public getters and setters.
	 * 
	 * @see PropertyDescriptor
	 * 
	 * @since 1.3.5
	 */
	public static List<PropertyDescriptor> getPropertyDescriptorsOf(Class<?> cls, String... fieldNames) throws IntrospectionException
	{
		return getPropertyDescriptorsOf(cls, null, fieldNames);    
	}
	
	
	/**
	 * @param cls | Class to inspect!
	 * @param cache | Cache to store generated results into!
	 * @param fieldNames | Names of fields to get descriptors for, if this array is empty or null, descriptors for all fields with public getters and setters will be obtained!
	 * 
	 * @return List of {@link PropertyDescriptor}s of cls representing access methods of required fields! Only descriptors of fields that have valid and public getter and setter will be returned! 
	 * 
	 * @throws IntrospectionException when there are no suitable fields with valid and public getters and setters.
	 * 
	 * @see PropertyDescriptor
	 * 
	 * @since 1.3.5
	 */
	public static List<PropertyDescriptor> getPropertyDescriptorsOf(Class<?> cls, Map<Class<?>, List<PropertyDescriptor>> cache, String... fieldNames) throws IntrospectionException
	{
		List<PropertyDescriptor> fieldDescriptors = new ArrayList<>(), cached = cache == null ? null : cache.get(cls);
    	if (cached != null)
    		return cached;
    	
        if (fieldNames == null || fieldNames.length <= 0)
		{
			for (Class<?> c = cls; c != Object.class; c = c.getSuperclass())
				for (Field field : c.getDeclaredFields())
					if (!Modifier.isStatic(field.getModifiers()))
						try
						{
							fieldDescriptors.add(new PropertyDescriptor(field.getName(), cls));
						}
						catch (Exception e)
						{}
			
			if (cache != null && !fieldDescriptors.isEmpty())
			{
				cache.put(cls, fieldDescriptors);
			}
		}
        else
        {
			for (int i = 0; i < fieldNames.length; i++) 
				try
				{
					fieldDescriptors.add(new PropertyDescriptor(fieldNames[i], cls));
				}
				catch (Exception e)
				{}
        }
   	 
		if (fieldDescriptors.isEmpty())
			throw new IntrospectionException("No suitable fields with valid and public getters and setters to use in " + cls.getSimpleName());
		return fieldDescriptors;    
	}
}