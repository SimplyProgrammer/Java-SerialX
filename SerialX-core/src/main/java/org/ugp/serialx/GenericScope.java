package org.ugp.serialx;

import static org.ugp.serialx.Utils.Instantiate;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;

	
/**
 * This collection is some sort of hybrid between {@link List} and {@link Map} which allow you to have both variables and independent values managed by one Object. <br>
 * Note: Variables are managed and accessed classically via {@link Map} methods such as <code>put(KeyT key, Object)</code> and array of independent values is accessed by via {@link List} methods such as <code>add(Object)</code> and <code>get(int)</code><br>
 * Also this is java representation of JUSS GenericScope group such as:
 * <pre>
 * <code>
 * {
 *     //This is generic scope in JUSS! Variable keys are generic!
 * }
 * </code>
 * </pre>
 * 
 * @author PETO
 * 
 * @since 1.3.5
 * 
 * @param <KeyT> generic type of variables key.
 * @param <ValT> generic type of variables value and independent value.
 */
public class GenericScope<KeyT, ValT> implements Collection<ValT>, Cloneable, Serializable
{
	private static final long serialVersionUID = 5717775602991055386L;
	
	protected Map<KeyT, ValT> variables;
	protected List<ValT> values;
	protected GenericScope<?, ?> parent;

	/**
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.2.0
	 */
	@SafeVarargs
	public GenericScope(ValT... values) 
	{
		this(null, values);
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.2.0
	 */
	@SafeVarargs
	public GenericScope(Map<? extends KeyT, ? extends ValT> variablesMap, ValT... values) 
	{
		this(variablesMap, values == null ? null : Arrays.asList(values), null);
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.2.0
	 */
	public GenericScope(Map<? extends KeyT, ? extends ValT> variablesMap, Collection<? extends ValT> values) 
	{
		this(variablesMap, values, null);
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * @param parent | Parent of this scope.

	 * @since 1.2.0
	 */
	public GenericScope(Map<? extends KeyT, ? extends ValT> variablesMap, Collection<? extends ValT> values, GenericScope<?, ?> parent) 
	{
		if (variablesMap != null)
			this.variables = new LinkedHashMap<>(variablesMap);
		if (values != null)
			this.values = new ArrayList<>(values);
		this.parent = parent;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof GenericScope)
			return values().equals(((GenericScope<?, ?>) obj).values()) && variables().equals(((GenericScope<?, ?>) obj).variables());
		if (obj instanceof Collection)
			return variablesCount() <= 0 && values().equals(obj);
		if (obj instanceof Map)
			return valuesCount() <= 0 && variables().equals(obj);
		if (obj != null && obj.getClass().isArray())
			return variablesCount() <= 0 && Objects.deepEquals(toArray(), Utils.fromAmbiguousArray(obj));
		return super.equals(obj);
	}
	
	@Override
	public String toString()
	{
		String name = getClass().getSimpleName();
		if (variablesCount() > 0 ^ valuesCount() > 0)
			return name + (variablesCount() > 0 ? variables() : values());
		else
			return name + toUnifiedList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public GenericScope<KeyT, ValT> clone()
	{
		try 
		{
			return clone(getClass());
		}
		catch (Exception e) 
		{
			return new GenericScope<>(variables(), values(), getParent());
		}
	}

	/**
	 * @param typeOfClone | Class representing type of scope that will be created.
	 * @return Copy of this scope converted to instance of typeOfClone.
	 * 
	 * @throws Exception | When program was unable to create instance of typeOfClone.
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public <S extends GenericScope<KeyT, ValT>> S clone(Class<S> typeOfClone) throws Exception
	{
		S clone = Instantiate(typeOfClone);
		clone.values = (List<ValT>) toValList();
		clone.variables = (Map<KeyT, ValT>) toVarMap();
		clone.parent = getParent();
		return (S) clone;
	}
	
	/**
	 * @param newType | Type of new scope.
	 * @return Original scope retyped/casted into instance of newType. Similar to {@link GenericScope#clone(Class)} but this will share same instances of values list and variables map with original!
	 * 
	 * @throws Exception | When program was unable to create instance of newType.
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public <K, V, S extends GenericScope<? super K, ? super V>> S castTo(Class<S> newType) throws Exception
	{
		if (getClass() == newType)
			return (S) this;
		
		GenericScope<Object, Object> clone = (GenericScope<Object, Object>) Instantiate(newType);
		clone.values = (List<Object>) values();
		clone.variables = (Map<Object, Object>) variables();
		clone.parent = getParent();
		return (S) clone;
	} 
	
	/**
	 * @return Iterator of independent values.
	 * 
	 * @since 1.2.0
	 */
	@Override
	public Iterator<ValT> iterator() 
	{
		return values().iterator();
	}
	
	/**
	 * Insert new variable.
	 *
	 * @param variableKey | Name of variable.
	 * @param variableValue | Variables value.
	 * 
	 * @return Old value of variable with given name or null is there was no this variable before!
	 * 
	 * @since 1.2.0
	 */
	public ValT put(KeyT variableKey, ValT variableValue)
	{
		if (variableValue instanceof GenericScope && ((GenericScope<?, ?>) variableValue).getParent() == null)
			((GenericScope<?, ?>) variableValue).parent = this;
		return variables().put(variableKey, variableValue);
	}
	
	/**
	 * @param kVkVkV | kV array with keys and values to insert into this map. Elements with even indexes are values and the ones with odd indexes are keys...
	 * 
	 * @return Array of values previously at keys from kv array.
	 * 
	 * @since 1.3.8
	 */
	@SuppressWarnings("unchecked")
	public ValT[] putAllKv(Object... kVkVkV)
	{
		ValT[] oldValues = (ValT[]) new Object[kVkVkV.length/2];
		for (int i = 1; i < kVkVkV.length; i+=2) {
			oldValues[i/2] = put((KeyT) kVkVkV[i-1], (ValT) kVkVkV[i]);
		}
		return oldValues;
	}
	
	/**
	 * @param index | Index of variable!
	 * 
	 * @return Value of variable at required index or null if index was not found!
	 * 
	 * @since 1.2.5
	 */
	public <V extends ValT> V getVarAt(int index)
	{
		return getVarAt(index, null);
	}
	
	/**
	 * @param index | Index of variable!
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Value of variable at required index or defaultValue if index was not found!
	 * 
	 * @since 1.2.5
	 */
	@SuppressWarnings("unchecked")
	public <V extends ValT> V getVarAt(int index, V defaultValue)
	{
		int i = 0;
		for (Map.Entry<KeyT, ValT> ent : varEntrySet())
			if (i++ == index)
				return (V) ent.getValue();
		return defaultValue;
	}
	
	/**
	 * @param variableKey | Variables name.
	 * 
	 * @return Value of variable with name or null if there is no such a one!
	 * 
	 * @since 1.2.0
	 */
	public <V extends ValT> V get(KeyT variableKey) 
	{
		return get(variableKey, null);
	}
	
	/**
	 * @param variableKey | Variables name.
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Value of variable with name or defaultValue if there is no such a one (or given key contains null)!
	 * 
	 * @since 1.2.5
	 */
	@SuppressWarnings("unchecked")
	public <V extends ValT> V get(KeyT variableKey, V defaultValue)
	{
		V obj;
		if ((obj = (V) variables().get(variableKey)) == null)
			return defaultValue;
		return obj;
	}
	
	/**
	 * @param pathToValue | Array with variables creating path to required value, nested in multiple sub-scopes.
	 * 
	 * @return Value of variable at given path. If no path is given (length is 0) then this {@link GenericScope} will be returned.<br>
	 * If 1 path argument is given then this behaves similarly to {@link GenericScope#get(Object)}.<br>
	 * if 2+ path arguments are given then it will search the sub-scopes and return first match value. For example:<br>
	 * In either way, if there is no suitable result then null will be returned!<br>
	 * Consider this scope tree:<br>
	 * <pre>
	 * <code>
	 * {
	 *   125: {
	 *   	"hello": {
	 *   		"value" true
	 *   	}
	 *   }
	 * }
	 * </code>
	 * </pre>
	 * Then to get value of "value" you can do <code>scope.get(125, "hello", "value")</code>!<br>
	 * If there is no other variable called "value" in the scope tree then you can also simplify it to <code>scope.get("value")</code>, but make sure that there is no equally-named variable!<br>
	 * Note: Make sure that you are not calling {@link GenericScope#get(Object, Object)} by accident when you are using inline vargas array (unspecified count of arguments)!
	 * 
	 * @since 1.3.8
	 */
	@SuppressWarnings("unchecked")
	public <V extends ValT> V get(KeyT... pathToValue)
	{
		try
		{
			if (pathToValue.length <= 0)
				return (V) this;
			Object obj = get((KeyT) pathToValue[0]);
			if (obj instanceof GenericScope)
				return ((GenericScope<KeyT, V>) obj).get(pathToValue = Arrays.copyOfRange(pathToValue, 1, pathToValue.length));
			for (Map.Entry<KeyT, ValT> var : varEntrySet())
				if (var.getValue() instanceof GenericScope)
					try 
					{
						GenericScope<KeyT, V> sc = (GenericScope<KeyT, V>) var.getValue();
						if ((sc = sc.getGenericScope(pathToValue[0])) != null)
							return sc.get(pathToValue = Arrays.copyOfRange(pathToValue, 1, pathToValue.length));
					}
					catch (Exception e) {}
			
			return (V) obj;
		}
		catch (ClassCastException e)
		{}
		return null;
	}
	
	/**
	 * @param variableKey | Variables name.
	 * @param cls | Default value to return.
	 * @param defaultValue | Class that you want the obtained object to be converted into! Exact conversion algorithm can differ based on its implementations.
	 * 
	 * @return Value of variable with name given converted to object of cls or defaultValue if there is no such a one (or given key contains null)!
	 * 
	 * @throws Exception | If converting to object of cls failed from some reason! This can differ from implementation to implementation! By default it uses {@link GenericScope#toObject(cls)}
	 * 
	 * @since 1.3.8
	 */
	public <V extends ValT> V get(KeyT variableKey, Class<? extends V> cls, V defaultValue) throws Exception
	{
		V obj = get(variableKey, defaultValue);
		if (obj != null && obj.getClass() == cls)
			return obj;
		if (obj instanceof GenericScope)
			return ((GenericScope<?, ?>) obj).toObject(cls);
		return obj;
	}
	
	/**
	 * @param variableKey | Variables name to search for.
	 * 
	 * @return True if variable with given name was found in this scope.
	 * 
	 * @since 1.2.0
	 */
	public boolean containsVariable(KeyT variableKey) 
	{
		return variables().containsKey(variableKey);
	}
	
	/**
	 * @param value | Object the independent value.
	 * 
	 * @return True if independent value was found in this scope.
	 * 
	 * @since 1.3.8
	 */
	@Override
	public boolean contains(Object value) 
	{
		return values().contains(value);
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Independent value with valueIndex of this {@link GenericScope}!
	 * 
	 * @since 1.2.0
	 */
	@SuppressWarnings("unchecked")
	public <V extends ValT> V get(int valueIndex)
	{
		return (V) values().get(valueIndex < 0 ? valuesCount() + valueIndex : valueIndex);
	}
	
	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * @param cls | Class that you want the obtained object to be converted into! Exact conversion algorithm can differ based on its implementations.
	 * 
	 * @return Independent value with valueIndex of this converted to object of cls!
	 * 
	 * @throws Exception | If converting to object of cls failed from some reason! This can differ from implementation to implementation!
     *
     * @since 1.3.8
	 */
	public <V extends ValT> V get(int valueIndex, Class<V> cls) throws Exception
	{
		V obj = get(valueIndex);
		if (obj != null && obj.getClass() == cls)
			return obj;
		if (obj instanceof GenericScope)
			return ((GenericScope<?, ?>) obj).toObject(cls);
		return obj;
	}
	
	/**
	 * @param value | Independent value to add into array of values.
	 * 
	 * @return {@link ArrayList#add(Object)}
	 * 
	 * @since 1.2.0
	 */
	@Override
	public boolean add(ValT value)
	{
		boolean result = values().add(value);
		if (result && value instanceof GenericScope && ((GenericScope<?, ?>) value).getParent() == null)
			((GenericScope<?, ?>) value).parent = this;
		return result;
	}
	
	/**
	 * @param values | List of independent value or values to add into array of values.
	 * 
	 * @return {@link ArrayList#add(Object)}
	 * 
	 * @since 1.2.0
	 */
	@Override
	public boolean addAll(Collection<? extends ValT> values)
	{
		if (values.isEmpty())
			return false;
		return values().addAll((Collection<? extends ValT>) values);
	}
	
	/**
	 * @param values | Array of independent value or values to add into array of values.
	 * 
	 * @return {@link GenericScope#addAll(Object...)}
	 * 
	 * @since 1.3.2
	 */
	public boolean addAll(@SuppressWarnings("unchecked") ValT... values)
	{
		return addAll(Arrays.asList(values));
	}
	
	/**
	 * @param values | Independent values to check.
	 * 
	 * @return True all provided values are contained in this scope as independent values!
	 * 
	 * @since 1.3.8
	 */
	@Override
	public boolean containsAll(Collection<?> values)
	{
		return values().containsAll(values);
	}

	/**
	 * @param values | Independent values to check.
	 * 
	 * @return {@link Collection#removeAll(Collection)} for the independent values,,,
	 * 
	 * @since 1.3.8
	 */
	@Override
	public boolean removeAll(Collection<?> values)
	{
		return values().removeAll(values);
	}

	/**
	 * @param values | Independent values to check.
	 * 
	 * @return {@link Collection#retainAll(Collection)} for the independent values,,,
	 * 
	 * @since 1.3.8
	 */
	@Override
	public boolean retainAll(Collection<?> values)
	{
		return values().retainAll(values);
	}
	
	/**
	 * @param scopeValueIndex | Index of sub-scopes value.
	 * 
	 * @return Sub-scope on required index or null if there is no scope on required index!<br>
	 * Note: Keep in mind that you need to insert valid index according to other values. Scopes share same index order with other values!
	 * Note: Also remember that this function will work only when this scope generically allows to store other scopes inside (when ValT is base class of {@link GenericScope})
	 * 
	 * @since 1.2.0
	 */
	@SuppressWarnings("unchecked")
	public <K, V> GenericScope<K, V> getGenericScope(int scopeValueIndex)
	{
		GenericScope<K, V> obj = (GenericScope<K, V>) get(scopeValueIndex);
		if (obj instanceof GenericScope)
			return (GenericScope<K, V>) obj;
		return null;
	}
	
	/**
	 * @param pathToScope | Array with variables creating path to required scope.
	 * 
	 * @return Sub-scope stored by variable with required name (last element of pathToScope) or null if there is no such a one in inserted path. If there is more than one result, the first one found will be returned!
	 * This search will also includes sub-scopes stored by variables of this scope while variables from lower ones are prioritize!<br>
	 * If this function is called with no arguments then self will be returned!
	 * Note: Remember that this search includes variables only, no values! <br>
	 * Note: Also remember that this function will work only when this scope generically allows to store other scopes inside (when ValT is base class of {@link GenericScope})
	 * 
	 * @since 1.2.0
	 */
	@SuppressWarnings("unchecked")
	public <K, V> GenericScope<K, V> getGenericScope(K... pathToScope)
	{
		Object obj = get((KeyT[]) pathToScope);
		if (obj instanceof GenericScope)
			return (GenericScope<K, V>) obj;

		if (containsVariable((KeyT) pathToScope[0]))
			LogProvider.instance.logErr("Variable with name \"" + pathToScope[0] + "\" does exists! However its value is not instance of scope, use \"get\" function instead if possible!", null);
		return null;
	}
	
	/**
	 * @param type | Class of object to create (may or may not support other implementations of {@link Type}).
	 * 
	 * @return Object of type constructed from this scopes independent values using protocol for given class or null if there was no protocol found in {@link SerializationProtocol#REGISTRY}! 
	 * 
	 * @throws Exception | Exception if Exception occurred in {@link SerializationProtocol#unserialize(Class, Object...)}!
	 * 
	 * @since 1.2.5
	 */
	public <T> T toObject(Type type) throws Exception
	{
		return toObject(type, SerializationProtocol.REGISTRY);
	}
	
	/**
	 * @param type | Class of object to create using protocols (may or may not support other implementations of {@link Type}).
	 * @param protocolsToUse | Registry of protocols to use.
	 * 
	 * @return Object of class constructed from this scopes independent values using protocol for given class or null if there was no protocol found in protocolsToUse! 
	 * 
	 * @throws Exception | Exception if Exception occurred in {@link SerializationProtocol#unserialize(Class, Object...)}!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public <T> T toObject(Type type, ProtocolRegistry protocolsToUse) throws Exception
	{
		if (protocolsToUse == null)
			return null;

		Class<T> objClass;
		SerializationProtocol<T> pro;
		if ((pro = protocolsToUse.GetProtocolFor(objClass = (Class<T>) type, SerializationProtocol.MODE_DESERIALIZE)) != null)
		{
			T obj;
			if ((obj = pro.unserialize(objClass, variablesCount() > 0 ? new Object[] {this} : toArray())) != null)
				return obj;
		}
		return null;
	}
	
	/**
	 * @param predicate | Predicate object with filter condition in test method!
	 * 
	 * @return Scope after filtration using inserted predicate! If some object can't be casted to {@link Predicate#test(Object)} argument, it will be treated as invalid and will be filtered away! Sub-scopes are not included!
	 * 
	 * @since 1.2.5
	 */
	public GenericScope<KeyT, ValT> filter(Predicate<ValT> predicate)
	{
		return filter(predicate, false);
	}
	
	/**
	 * @param predicate | Predicate object with filter condition in test method!
	 * @param includeSubScopes | If true filtration will be also applied on sub-scopes, if false sub-scopes will be treated as other things (parsed in to {@link Predicate#test(Object)}).
	 * If sub-scope is empty after filtration it will not be included in result!
	 * Note: Remember that this will work only when this scope generically allows to store other scopes inside (when ValT is base class of {@link GenericScope})!
	 * 
	 * @return Scope after filtration using inserted predicate! If some object can't be casted to {@link Predicate#test(Object)} argument, it will be treated as invalid and will be filtered away!
	 * 
	 * @since 1.2.5
	 */
	@SuppressWarnings("unchecked")
	public GenericScope<KeyT, ValT> filter(Predicate<ValT> predicate, boolean includeSubScopes)
	{
		return (GenericScope<KeyT, ValT>) transform(new Function<ValT, Object>()
		{
			@Override
			public Object apply(ValT t) 
			{
				return predicate.test(t) ? t : DataParser.VOID;
			}
		}, includeSubScopes);
	}
	
	/**
	 * @param trans | Function to transform objects of this scope!
	 * 
	 * @return Scope after transformation using inserted function! If some object can't be casted to {@link Function#apply(Object)} argument, it will be treated as invalid and will be filtered away! Sub-scopes are not included!
	 * 
	 * @since 1.2.5
	 */
	public <V> GenericScope<KeyT, V> transform(Function<ValT, V> trans)
	{
		return transform(trans, false);
	}
	
	/**
	 * @param trans | Function to transform objects of this scope!
	 * @param includeSubScopes | If true transformation will be also applied on sub-scopes, if false sub-scopes will be treated as other things (parsed in to {@link Function#apply(Object)}).
	 * If sub-scope is empty after transformation it will not be included in result!
	 * Note: Remember that this will work only when this scope generically allows to store other scopes inside (when ValT is base class of {@link GenericScope})!
	 * 
	 * @return Scope after transformation using inserted function! If some object can't be casted to {@link Function#apply(Object)} argument, it will be treated as invalid and will be filtered away!
	 * 
	 * @since 1.2.5
	 */
	@SuppressWarnings("unchecked")
	public <V> GenericScope<KeyT, V> transform(Function<ValT, V> trans, boolean includeSubScopes)
	{
		if (trans == null || isEmpty())
			return (GenericScope<KeyT, V>) this;

		LinkedHashMap<KeyT, V> mappedVars = new LinkedHashMap<>();
		for (Entry<KeyT, ValT> ent : this.varEntrySet())
			try
			{
				Object obj = ent.getValue();
				if (includeSubScopes && obj instanceof GenericScope)
				{ 
					GenericScope<?, V> sc = ((GenericScope<?, ValT>) obj).transform(trans, includeSubScopes);
					if (!sc.isEmpty())
						mappedVars.put(ent.getKey(), (V) sc);
				}
				else if ((obj = trans.apply((ValT) obj)) != DataParser.VOID)
					mappedVars.put(ent.getKey(), trans.apply((ValT) obj));
			}
			catch (ClassCastException e) 
			{}
		
		List<V> mappedVals = map(trans, includeSubScopes);
		try
		{ 
			GenericScope<KeyT, V> clone = Instantiate(getClass());
			clone.values = mappedVals;
			clone.variables = mappedVars;
			clone.parent = getParent();
			return clone;
		} 
		catch (Exception e) 
		{
			return new GenericScope<>(mappedVars, mappedVals, getParent());
		}
	}
	
	/**
	 * @param trans | Function to transform independent objects of this scope!
	 * 
	 * @return Original scope after transformation using inserted function! If some object can't be casted to {@link Function#apply(Object)} argument, it will be treated as invalid and will be filtered away!
	 * 
	 * @since 1.3.5
	 */
	public <V> List<V> map(Function<ValT, V> trans)
	{
		return map(trans, false);
	}
	
	/**
	 * @param trans | Function to transform independent objects of this scope!
	 * @param includeSubScopes | If true transformation will be also applied on sub-scopes, if false sub-scopes will be treated as other things (parsed in to {@link Function#apply(Object)}).
	 * If sub-scope is empty after transformation it will not be included in result!
	 * Note: Remember that this will work only when this scope generically allows to store other scopes inside (when ValT is base class of {@link GenericScope})!
	 * 
	 * @return Original scope after transformation using inserted function! If some object can't be casted to {@link Function#apply(Object)} argument, it will be treated as invalid and will be filtered away!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public <V> List<V> map(Function<ValT, V> trans, boolean includeSubScopes)
	{
//		List<V> mapped = new ArrayList<>(size());
//		for (Object obj : this)
//			try
//			{
//				if (obj instanceof GenericScope && includeSubScopes)
//				{ 
//					GenericScope<?, V> sc = (GenericScope<?, V>) ((GenericScope<?, ValT>) obj).map(trans, includeSubScopes);
//					if (!sc.isEmpty())
//						mapped.add((V) sc);
//				}
//				else if ((obj = trans.apply((ValT) obj)) != DataParser.VOID)
//					mapped.add((V) obj);
//			}
//			catch (ClassCastException e) 
//			{}

		List<V> mapped = (List<V>) toValList();
		for (int i = valuesCount()-1; i >= 0; i--)
		{
			try
			{
				Object obj = mapped.get(i);
				if (includeSubScopes && obj instanceof GenericScope)
				{ 
					GenericScope<?, V> sc = (GenericScope<?, V>) ((GenericScope<?, ValT>) obj).map(trans, includeSubScopes);
					if (!sc.isEmpty())
					{
						mapped.set(i, (V) sc);
						continue;
					}
				}
				else if ((obj = trans.apply((ValT) obj)) != DataParser.VOID)
				{
					mapped.set(i, (V) obj);
					continue;
				}
			}
			catch (ClassCastException e)
			{}

			mapped.remove(i);
		}
		
		return mapped;
	}
	
	/**
	 * @param valueIndex | Index of independent value to remove!
	 * 
	 * @return Removed independent value!
	 * 
	 * @since 1.3.2
	 */
	public ValT remove(int valueIndex)
	{
		return values().remove(valueIndex < 0 ? valuesCount() + valueIndex : valueIndex);
	}
	
	/**
	 * @param independentValue | Independent value to remove!
	 * 
	 * @return Value of variable that was removed!
	 * 
	 * @since 1.3.8
	 */
	@Override
	public boolean remove(Object independentValue)
	{
		return values().remove(independentValue);
	}
	
	/**
	 * @param variableKey | Name of variable to remove!
	 * 
	 * @return Value of variable that was removed!
	 * 
	 * @since 1.3.8
	 */
	public ValT removeVariable(KeyT variableKey)
	{
		return variables().remove(variableKey);
	}
	
	/**
	 * Removes all independent values and variables of this scope!
	 * <br>
	 * Basically it just calls <code>variables().clear()</code> and <code>values().clear()</code>!
	 * 
	 * @since 1.3.5
	 */
	@Override
	public void clear()
	{
		variables().clear();
		values().clear();
	}
	
	/**
	 * @return This scope after inheriting variables of its parent (return this)!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public GenericScope<KeyT, ValT> inheritParent()
	{
		GenericScope<?, ?> parent = getParent();
		if (parent != null)
			variables().putAll((Map<? extends KeyT, ? extends ValT>) parent.variables());
		return this;
	}
	
	/**
	 * @param scope | GenericScope whose content will be added!
	 * 
	 * @return This scope...
	 * 
	 * @since 1.3.0
	 */
	public GenericScope<KeyT, ValT> addAll(GenericScope<? extends KeyT, ? extends ValT> scope)
	{
		values().addAll(scope.values());
		variables().putAll(scope.variables());
		return this;
	}
	
	/**
	 * @return Entry set of variables!
	 * 
	 * @since 1.2.0
	 */
	public Set<Entry<KeyT, ValT>> varEntrySet()
	{
		return variables().entrySet();
	}
	
	/**
	 * @return Count of keyless values of this scope! (<code>values().size()</code>)
	 * 
	 * @since 1.2.0
	 */
	public int valuesCount()
	{
		return values().size();
	}
	
	/**
	 * @return Count of variables! (<code>variables().size()</code>)
	 * 
	 * @since 1.2.0
	 */
	public int variablesCount()
	{
		return variables().size();
	}
	
	/**
	 * @return Total number of variables and independent values of this scope! (<code>vvaluesCount() + variablesCount()</code>)
	 * 
	 * @since 1.3.8 (before 1.3.8 known as totalSize)
	 */
	@Override
	public int size()
	{
		return valuesCount() + variablesCount();
	}
	
	/**
	 * @return True if this scope is completely empty, meaning there are no variables or values.
	 * 
	 * @since 1.2.0
	 */
	@Override
	public boolean isEmpty()
	{
		return size() <= 0;
	}
	
	/**
	 * @return The parent scope of this scope or null if this scope has no parent such as default one in file.<br>
	 * Note: This may or may not be always null based on the way this scope was instantiated...
	 * 
	 * @since 1.2.0
	 */
	public <T extends GenericScope<?, ?>> T getParent()
	{
		return getParent(1);
	}
	
	/**
	 * @param depth | Positive number representing how many times will this method call itself... <br> For example 0 or less = this, 1 = parent, 2 = parentOfParent (parent.getParent()) and so on...
	 * If you want to get the root parent (the one that has no parent), simply put a big number as depth. 99 should be enough!
	 *
	 * @return The parent scope based on depth or null if this scope has no parent which means its already root (default one in file).
	 * If depth was bigger than 1, null will not be returned, last not null parent will be returned instead!<br>
	 * Note: This may or may not be always null based on the way this scope was instantiated...
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public <T extends GenericScope<?, ?>> T getParent(int depth)
	{
		if (depth < 1)
			return (T) this;
		
		GenericScope<?, ?> parentsParent;
		if (depth == 1 || parent == null || (parentsParent = parent.getParent(--depth)) == null)
			return (T) parent;
		return (T) parentsParent;
	}
	
	/**
	 * @return New {@link LinkedHashMap} with variables of this a in defined order! Key is a KeyT of variable and value is its value!<br>
	 * Modifying this map will not affect this GenericScope object!
	 * 
	 * @since 1.2.0
	 */
	public LinkedHashMap<? extends KeyT, ? extends ValT> toVarMap()
	{
		return new LinkedHashMap<>(variables());
	}
	
	/**
	 * @return New {@link ArrayList} with independent values of this {@link GenericScope}. These values have nothing to do with values of variables, they are independent!
	 * Modifying this list will not affect this GenericScope object!<br>
	 * Note: Returning other {@link List} implementations than {@link ArrayList} (or other collection that stores elements in "array-like fashion") may result in serious performance implications! Queues or LinkedLists are the prime examples to avoid!
	 * 
	 * @since 1.2.0
	 */
	public List<? extends ValT> toValList()
	{
		return new ArrayList<>(values());
	}
	
	/**
	 * @return Primitive array with independent values of this {@link GenericScope}. These values have nothing to do with values of variables, they are independent!
	 * Modifying this list will not affect this {@link GenericScope} object!
	 * 
	 * @since 1.3.8 (before 1.3.8 known as toValArray)
	 */
	@Override
	public Object[] toArray() 
	{
		return values().toArray();
	}

	/**
	 * @param vals | Array to store independent values into!
	 * 
	 * @return Primitive array with independent values of this {@link GenericScope}. These values have nothing to do with values of variables, they are independent!
	 * Modifying this list will not affect this {@link GenericScope} object!
	 * 
	 * @since 1.3.8 (before 1.3.8 known as toValArray)
	 */
	@Override
	public <T> T[] toArray(T[] vals) 
	{
		return values().toArray(vals);
	}
	
	/**
	 * @return List with both variables and values! Variables will be added as {@link Entry}!
	 * Variables will be always first!
	 * Modifying this map will not affect this GenericScope object!
	 * 
	 * @since 1.3.0
	 */
	@SuppressWarnings("unchecked")
	public List<Object> toUnifiedList()
	{
		List<Object> list = (List<Object>) toValList();
		list.addAll(0, varEntrySet());
		return list;
	}
	
	/**
	 * @return Independent values of this scope. These are not the values of keys these are values that have no key. You can access them via {@link GenericScope#get(int)}!
	 * Note: Editing this List will affect this scope!
	 * 
	 * @since 1.2.0
	 */
	public List<ValT> values() 
	{
		if (values == null)
			values = new ArrayList<>();
		return values;
	}
	
	/**
	 * @return Variables of this scope. Objecthis variables has nothing to do with values. Key is a KeyT name of variable and value is value of variable.
	 * Note: Editing this Map will affect this scope!
	 * 
	 * @since 1.2.0
	 */
	public Map<KeyT, ValT> variables() 
	{
		if (variables == null)
			variables = new LinkedHashMap<>();
		return variables;
	}
	
	/**
	 * @param variablesMap | Variables map to use!
	 * 
	 * @return New scope that is bidirectional with given data structures (list and map), which means that changing these data structures will affect scope and changing scope will affect data structures. This is behavior that regular constructor created scopes do not possess!
	 * 
	 * @since 1.3.5
	 */
	public static <K, V> GenericScope<K, V> newBidirectional(Map<K, V> variablesMap)
	{
		return newBidirectional(variablesMap, null);
	}
	
	/**
	 * @param variablesMap | Variables map to use!
	 * @param values | Values list to use!
	 * 
	 * @return New scope that is bidirectional with given data structures (list and map), which means that changing these data structures will affect scope and changing scope will affect data structures. This is behavior that regular constructor created scopes do not possess!
	 * 
	 * @since 1.3.5
	 */
	public static <K, V> GenericScope<K, V> newBidirectional(Map<K, V> variablesMap, List<V> values)
	{
		return newBidirectional(variablesMap, values, null);
	}
	
	/**
	 * @param variablesMap | Variables map to use!
	 * @param values | Values list to use!
	 * @param parent | Parent of scope!
	 * 
	 * @return New scope that is bidirectional with given data structures (list and map), which means that changing these data structures will affect scope and changing scope will affect data structures. This is behavior that regular constructor created scopes do not possess!
	 * 
	 * @since 1.3.5
	 */
	public static <K, V> GenericScope<K, V> newBidirectional(Map<K, V> variablesMap, List<V> values, GenericScope<?, ?> parent)
	{
		return intoBidirectional(new GenericScope<>(null, null, null), variablesMap, values, parent);
	}
	
	/**
	 * @param scopeToMakeBidirectional | GenericScope to make bidirectional!
	 * @param variablesMap | Variables map to use!
	 * @param values | Values list to use!
	 * 
	 * @return Inserted scope that is bidirectional with given data structures (list and map), which means that changing these data structures will affect scope and changing scope will affect data structures. This is behavior that regular constructor created scopes do not possess!
	 * 
	 * @since 1.3.5
	 */
	public static <K, V, S extends GenericScope<K, V>> S intoBidirectional(S scopeToMakeBidirectional, Map<K, V> variablesMap, List<V> values)
	{
		return intoBidirectional(scopeToMakeBidirectional, variablesMap, values, null);
	}
	
	/**
	 * @param scopeToMakeBidirectional | GenericScope to make bidirectional!
	 * @param variablesMap | Variables map to use!
	 * @param values | Values list to use!
	 * @param parent | Parent of scope!
	 * 
	 * @return Inserted scope that is bidirectional with given data structures (list and map), which means that changing these data structures will affect scope and changing scope will affect data structures. This is behavior that regular constructor created scopes do not possess!
	 * 
	 * @since 1.3.5
	 */
	public static <K, V, S extends GenericScope<K, V>> S intoBidirectional(S scopeToMakeBidirectional, Map<K, V> variablesMap, List<V> values, GenericScope<?, ?> parent)
	{
		scopeToMakeBidirectional.variables = variablesMap;
		scopeToMakeBidirectional.values = values;
		scopeToMakeBidirectional.parent = parent;
		return scopeToMakeBidirectional;
	}
	
	/**
	 * @param map | Map to populate from kV array.
	 * @param kVkVkV | kV array with keys and values to insert into this map. Elements with even indexes are values and the ones with odd indexes are keys...
	 * 
	 * @return Same map populated with kV array.
	 * 
	 * @since 1.3.8
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, M extends Map<K, V>> M mapKvArray(M map, Object... kVkVkV)
	{
		for (int i = 1; i < kVkVkV.length; i+=2) 
			map.put((K) kVkVkV[i-1], (V) kVkVkV[i]);
		return map;
	}
}
