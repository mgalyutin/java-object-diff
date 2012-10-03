/*
 * Copyright 2012 Daniel Bechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.danielbechler.diff;

import de.danielbechler.diff.annotation.*;
import de.danielbechler.diff.node.*;
import de.danielbechler.diff.path.*;
import de.danielbechler.util.*;

import java.util.*;

/** @author Daniel Bechler */
public class Configuration implements NodeInspector
{
	/**
	 * Defines how default values of primitive types (int, long, short, byte, char, boolean, float, double) will
	 * be treated. A default value is either the one specified by the JDK (numbers are 0, booleans are false) or
	 * the value of the corresponding property when a new instance of its holding class gets created. In order to
	 * determine the proper default value, we'll attempt to instantiate the holding class once via its public
	 * constructor. If this instantiation fails (for example if there is no such constructor), we'll fall back to
	 * the JDK default. This configuration does not apply to the corresponding wrapper types (Integer, Long,
	 * Short, Byte, Character, Boolean, Float, Double).
	 */
	public enum PrimitiveDefaultValueMode
	{
		/**
		 * Default values of primitive types will be treated like any other value. Since there is no distinction,
		 * any change to a primitive value will be marked as {@linkplain Node.State#CHANGED}.
		 */
		ASSIGNED,

		/**
		 * Default values of primitive types will be treated as if the property has not been set. The consequence of
		 * this is that a change from default value to something else will be marked as {@linkplain
		 * Node.State#ADDED} and from something else to the default value as {@linkplain Node.State#REMOVED}.
		 */
		UNASSIGNED
	}

	private final Collection<String> includedCategories = new TreeSet<String>();
	private final Collection<String> excludedCategories = new TreeSet<String>();
	private final Collection<PropertyPath> includedProperties = new HashSet<PropertyPath>(10);
	private final Collection<PropertyPath> excludedProperties = new HashSet<PropertyPath>(10);
	private final Collection<PropertyPath> equalsOnlyProperties = new LinkedHashSet<PropertyPath>(10);
	private final Collection<Class<?>> equalsOnlyTypes = new LinkedHashSet<Class<?>>(10);
	private boolean returnUnchangedNodes = false;
	private boolean returnIgnoredNodes = false;
	private boolean returnCircularNodes = true;
	private boolean returnChildrenOfAddedNodes = false;
	private boolean returnChildrenOfRemovedNodes = false;
	private PrimitiveDefaultValueMode treatPrimitivesAs = PrimitiveDefaultValueMode.UNASSIGNED;

	public Configuration withCategory(final String category)
	{
		this.includedCategories.addAll(Arrays.asList(category));
		return this;
	}

	public Configuration withoutCategory(final String... category)
	{
		this.excludedCategories.addAll(Arrays.asList(category));
		return this;
	}

	public Configuration withPropertyPath(final PropertyPath propertyPath)
	{
		this.includedProperties.add(propertyPath);
		return this;
	}

	public Configuration withoutProperty(final PropertyPath propertyPath)
	{
		this.excludedProperties.add(propertyPath);
		return this;
	}

	public Configuration withEqualsOnlyType(final Class<?> type)
	{
		this.equalsOnlyTypes.add(type);
		return this;
	}

	public Configuration withEqualsOnlyProperty(final PropertyPath propertyPath)
	{
		this.equalsOnlyProperties.add(propertyPath);
		return this;
	}

	public Configuration withIgnoredNodes()
	{
		this.returnIgnoredNodes = true;
		return this;
	}

	public Configuration withoutIgnoredNodes()
	{
		this.returnIgnoredNodes = false;
		return this;
	}

	public Configuration withUntouchedNodes()
	{
		this.returnUnchangedNodes = true;
		return this;
	}

	public Configuration withoutUntouchedNodes()
	{
		this.returnUnchangedNodes = false;
		return this;
	}

	public Configuration withCircularNodes()
	{
		this.returnCircularNodes = true;
		return this;
	}

	public Configuration withoutCircularNodes()
	{
		this.returnCircularNodes = false;
		return this;
	}

	public Configuration withChildrenOfAddedNodes()
	{
		this.returnChildrenOfAddedNodes = true;
		return this;
	}

	public Configuration withoutChildrenOfAddedNodes()
	{
		this.returnChildrenOfAddedNodes = false;
		return this;
	}

	public Configuration withChildrenOfRemovedNodes()
	{
		this.returnChildrenOfRemovedNodes = true;
		return this;
	}

	public Configuration withoutChildrenOfRemovedNodes()
	{
		this.returnChildrenOfRemovedNodes = false;
		return this;
	}

	public Configuration treatPrimitiveDefaultValuesAs(final PrimitiveDefaultValueMode mode)
	{
		this.treatPrimitivesAs = mode;
		return this;
	}

	public PrimitiveDefaultValueMode getPrimitiveDefaultValueMode()
	{
		return treatPrimitivesAs;
	}

	@Override
	public boolean isIgnored(final Node node)
	{
		return node.isIgnored() || !isIncluded(node) || isExcluded(node);
	}

	@Override
	public boolean isIncluded(final Node node)
	{
		if (node.isRootNode())
		{
			return true;
		}
		if (includedCategories.isEmpty() && includedProperties.isEmpty())
		{
			return true;
		}
		else if (de.danielbechler.util.Collections.containsAny(node.getCategories(), includedCategories))
		{
			return true;
		}
		else if (includedProperties.contains(node.getPropertyPath()))
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isExcluded(final Node node)
	{
		if (excludedProperties.contains(node.getPropertyPath()))
		{
			return true;
		}
		if (de.danielbechler.util.Collections.containsAny(node.getCategories(), excludedCategories))
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isEqualsOnly(final Node node)
	{
		final Class<?> propertyType = node.getType();
		if (propertyType != null)
		{
			if (propertyType.getAnnotation(ObjectDiffEqualsOnlyType.class) != null)
			{
				return true;
			}
			if (equalsOnlyTypes.contains(propertyType))
			{
				return true;
			}
			if (Classes.isSimpleType(propertyType))
			{
				return true;
			}
		}
		if (node.isEqualsOnly())
		{
			return true;
		}
		if (equalsOnlyProperties.contains(node.getPropertyPath()))
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isReturnable(final Node node)
	{
		if (node.isIgnored())
		{
			return returnIgnoredNodes;
		}
		else if (node.isCircular())
		{
			return returnCircularNodes;
		}
		else if (node.isUntouched())
		{
			if (node.hasChanges())
			{
				return true;
			}
			else if (node.hasChildren())
			{
				return true;
			}
			return returnUnchangedNodes;
		}
		return true;
	}

	public boolean isIntrospectible(final Node node)
	{
		if (isEqualsOnly(node))
		{
			return false;
		}
		else if (node.isAdded())
		{
			return returnChildrenOfAddedNodes;
		}
		else if (node.isRemoved())
		{
			return returnChildrenOfRemovedNodes;
		}
		return true;
	}
}
