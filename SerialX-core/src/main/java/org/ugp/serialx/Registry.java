package org.ugp.serialx;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is list that can contains only one element per class! Trying to add 2 elements with same class will cause old one to be removed and new one will be added!
 * 
 * @author PETO
 *
 * @param <E> | The type of elements held in this collection
 * 
 * @since 1.3.0
 */
public class Registry<E> extends ArrayList<E> 
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs an {@link Registry} with the specified initial capacity.
	 * 
	 * @param initialSize | Initial capacity.
	 * 
	 * @since 1.3.0
	 */
	public Registry(int initialSize) 
	{
		super(initialSize);
	}
	
	/**
	 * Constructs an {@link Registry} with content of c.
	 * 
	 * @param c | Initial content of registry.
	 * 
	 * @since 1.3.0
	 */
	public Registry(Collection<? extends E> c) 
	{
		super(c);
	}
	
	/**
	 * Constructs an {@link Registry} with objs.
	 * 
	 * @param objs | Initial content of registry.
	 * 
	 * @since 1.3.0
	 */
	@SafeVarargs
	public Registry(E... objs) 
	{
		addAll(objs);
	}
	
	@Override
	public Registry<E> clone() 
	{
		return new Registry<>(this);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) 
	{
		return addAll(size(), c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) 
	{
		for (E s : c)
			add(index++, s);
		return true;
	}
	
	@Override
	public boolean add(E e) 
	{
		add(size(), e);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void add(int index, E element) 
	{
		E contained = get((Class<? extends E>) element.getClass());
		if (contained != null)
			remove(contained);		
		super.add(index, element);
	}
	
	/**
	 * @param exclude | Class of elements to exclude!
	 * 
	 * @return Clone of this registry, excluding elements of inserted classes!
	 * 
	 * @since 1.3.0
	 */
	public Registry<E> clone(Class<?>... exclude) 
	{
		Registry<E> clone = clone();
		
		for (int i = clone.size() - 1; i >= 0; i--)
		{
			E e = clone.get(i);
			for (Class<?> cls : exclude) 
				if (e.getClass() == cls)
					clone.remove(e);
		}
		return clone;
	}
	
	/**
	 * Similar to regular add however this one will not remove existing element if duplicate (should not be public)!
	 * Essentially it will simply do <code>super.add(index, element)</code>!
	 * 
	 * @param index | Index at which the specified element is to be inserted!
	 * @param element | Element to add!
	 * 
	 * @since 1.3.0
	 */
	protected void addDuplicatively(int index, E element)
	{
		super.add(index, element);
	}
	
	/**
	 * @param elms | Array o elements to add!
	 * 
	 * @return {@link Registry#addAll(int, Object...)}
	 * 
	 * @since 1.3.0
	 */
	public void addAll(@SuppressWarnings("unchecked") E... elms)
	{
		addAll(size(), elms);
	}
	
	/**
	 * @param elms | Array o elements to add!
	 * 
	 * @return {@link Registry#addAll(int, Collection)}
	 * 
	 * @since 1.3.0
	 */
	public void addAll(int index, @SuppressWarnings("unchecked") E... elms)
	{
		for (E s : elms)
			add(index++, s);
	}

	/**
	 * @param elm | Class of element to add!
	 * 
	 * @return Instantiated obj of inserted class that was added. Empty constructor required! 
	 * 
	 * @throws Exception
	 * 
	 * @since 1.3.0
	 */
	public E add(Class<E> elm) throws Exception
	{
		E inst = elm.getConstructor().newInstance();
		add(inst);
		return inst;
	}
	
	/**
	 * @param elms | Array of classes to be instantiated and added!
	 * 
	 * @throws Exception
	 * 
	 * @since 1.3.0
	 */
	public void addAll(@SuppressWarnings("unchecked") Class<E>... elms) throws Exception
	{
		for (Class<E> cls : elms) 
			add(cls);
	}
	
	/**
	 * @param cls | Class to find!
	 * @param element | Element to insert before index of required class!
	 * 
	 * @since 1.3.0
	 */
	public void addBefore(Class<? extends E> cls, E element)
	{
		addBefore(cls, false, element);
	}
	
	/**
	 * @param cls | Class to find!
	 * @param includeChildrens | If true, index of child classes of cls will be returned when object with exactly matching class is not registered!
	 * @param element | Element to insert before index of required class!
	 * 
	 * @since 1.3.0
	 */
	public void addBefore(Class<? extends E> cls, boolean includeChildrens, E element)
	{
		int index = indexOf(cls);
		if (index <= -1)
			add(element);
		else
			add(index, element);
	}
	
	/**
	 * @param cls | Class to find!
	 * @param element | Element to insert after index of required class!
	 * 
	 * @since 1.3.0
	 */
	public void addAllBefore(Class<? extends E> cls, @SuppressWarnings("unchecked") E... element)
	{
		addAllBefore(cls, false, element);
	}
	
	/**
	 * @param cls | Class to find!
	 * @param includeChildrens | If true, index of child classes of cls will be returned when object with exactly matching class is not registered!
	 * @param element | Element to insert after index of required class!
	 * 
	 * @since 1.3.0
	 */
	public void addAllBefore(Class<? extends E> cls, boolean includeChildrens, @SuppressWarnings("unchecked") E... element)
	{
		int index = indexOf(cls);
		if (index <= -1)
			addAll(element);
		else
			addAll(index, element);
	}
	
	/**
	 * @param cls | Class to find!
	 * @param element | Element to insert after index of required class!
	 * 
	 * @since 1.3.0
	 */
	public void addAfter(Class<? extends E> cls, E element)
	{
		addAfter(cls, false, element);
	}
	
	/**
	 * @param cls | Class to find!
	 * @param includeChildrens | If true, index of child classes of cls will be returned when object with exactly matching class is not registered!
	 * @param element | Element to insert after index of required class!
	 * 
	 * @since 1.3.0
	 */
	public void addAfter(Class<? extends E> cls, boolean includeChildrens, E element)
	{
		int index = indexOf(cls, includeChildrens);
		if (index <= -1)
			add(element);
		else
			add(index + 1, element);
	}
	
	/**
	 * @param cls | Class to find!
	 * @param element | Element to insert after index of required class!
	 * 
	 * @since 1.3.0
	 */
	public void addAllAfter(Class<? extends E> cls, @SuppressWarnings("unchecked") E... element)
	{
		addAllAfter(cls, false, element);
	}
	
	/**
	 * @param cls | Class to find!
	 * @param includeChildrens | If true, index of child classes of cls will be returned when object with exactly matching class is not registered!
	 * @param element | Element to insert after index of required class!
	 * 
	 * @since 1.3.0
	 */
	public void addAllAfter(Class<? extends E> cls, boolean includeChildrens, @SuppressWarnings("unchecked") E... element)
	{
		int index = indexOf(cls, includeChildrens);
		if (index <= -1)
			addAll(element);
		else
			addAll(index + 1, element);
	}

	/**
	 * @param cls | Class to get instance for!
	 * 
	 * @return Element of class or null if there is no such a one!
	 * 
	 * @since 1.3.0
	 */
	public <C extends E> C get(Class<C> cls)
	{
		return get(cls, false);
	}
	
	/**
	 * @param cls | Class to get instance for!
	 * @param includeChildrens | If true, index of child classes of cls will be returned when object with exactly matching class is not registered!
	 * 
	 * @return Element of class according to includeChildrens flag or null if there is no such a one!
	 * 
	 * @since 1.3.0
	 */
	@SuppressWarnings("unchecked")
	public <C extends E> C get(Class<C> cls, boolean includeChildrens)
	{
		C obj = null;
		for (int i = 0, size = size(); i < size; i++) 
		{
			C elm = (C) get(i);
			Class<?> objCls = elm.getClass();
			if (objCls == cls)
				return elm;
			else if (includeChildrens && cls.isAssignableFrom(objCls))
				obj = elm;
		}
		return obj;
	}
	
	/**
	 * @param cls | Instances class to get index of!
	 * 
	 * @return Index of element with required class or -1 if there is no such a one!
	 * 
	 * @since 1.3.0
	 */
	public int indexOf(Class<? extends E> cls)
	{
		return indexOf(cls, false);
	}
	
	/**
	 * @param cls | Instances class to get index of!
	 * @param includeChildrens | If true, index of child classes of cls will be returned when object with exactly matching class is not registered!
	 * 
	 * @return Index of element with class according to includeChildrens flag or -1 if there is no such a one!
	 * 
	 * @since 1.3.0
	 */
	public int indexOf(Class<? extends E> cls, boolean includeChildrens)
	{
		int index = -1;
		for (int i = 0; i < size(); i++) 
		{
			Class<?> objCls = get(i).getClass();
			if (objCls == cls)
				return i;
			else if (includeChildrens && cls.isAssignableFrom(objCls))
				index = i;
		}
		return index;
	}
	
	/**
	 * @param cls | Class of element to remove!
	 * 
	 * @return Removed element itself!
	 * 
	 * @since 1.3.0
	 */
	public E remove(Class<? extends E> cls) 
	{
		return remove(cls, false);
	}
	
	
	/**
	 * @param cls | Class of element to remove!
	 * @param includeChildrens | If true, index of child classes of cls will be returned when object with exactly matching class is not registered!
	 * 
	 * @return Removed element itself or null if there is no element with cls!
	 * 
	 * @since 1.3.0
	 */
	public E remove(Class<? extends E> cls, boolean includeChildrens) 
	{
		int i = indexOf(cls, includeChildrens);
		if (i > -1)
			return remove(i);
		return null;
	}
	
	
	/**
	 * @param newContent | New content to set, old one will be deleted!
	 * 
	 * @return Registry with previous content!
	 * 
	 * @since 1.3.0
	 */
	public Registry<E> setTo(Registry<E> newContent)
	{
		Registry<E> old = clone();
		clear();
		addAll(newContent);
		return old;
	}
}
