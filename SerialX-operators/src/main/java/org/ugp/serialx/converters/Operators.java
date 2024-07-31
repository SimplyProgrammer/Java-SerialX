package org.ugp.serialx.converters;

import java.util.Arrays;

import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.converters.operators.ArithmeticOperators;
import org.ugp.serialx.converters.operators.ComparisonOperators;
import org.ugp.serialx.converters.operators.ConditionalAssignmentOperators;
import org.ugp.serialx.converters.operators.LogicalOperators;
import org.ugp.serialx.converters.operators.NegationOperator;

/**
 * This class contains all operators that are provided by this module and it allows you to install it to your specific {@link ParserRegistry}!
 * 
 * @see Operators#install(ParserRegistry)
 * 
 * @since 1.3.8
 * 
 * @author PETO
 */
public class Operators {

	/**
	 * Array that contains all binary operators (operators that require 2 operands, one from left, one from right). This includes:<br>
	 * {@link ConditionalAssignmentOperators}<br>
	 * {@link LogicalOperators}<br>
	 * {@link ComparisonOperators}<br>
	 * {@link ArithmeticOperators}<br>
	 * In given order!
	 * 
	 * @since 1.3.8
	 */
	public static final DataParser[] BINARY = { new ConditionalAssignmentOperators(), new LogicalOperators(), new ComparisonOperators(), new ArithmeticOperators() };
	
	/**
	 * Array that contains all unary operators (operators that require only 1 operands, from left or right). This includes:<br>
	 * {@link NegationOperator}<br>
	 * In given order!
	 * 
	 * @since 1.3.8
	 */
	public static final DataParser[] UNARY = { new NegationOperator() };
	
	/**
	 * @param registry | Registry to insert all operators provided by {@link Operators#BINARY} and {@link Operators#UNARY} into. This register is required to contains {@link StringConverter} and {@link BooleanConverter} in order for operators to be inserted in correct order and work in precedence they were meant to.
	 * 
	 * @return The same registry with operators inserted!
	 * 
	 * @since 1.3.8
	 */
	public static ParserRegistry install(ParserRegistry registry) {
		registry.addAllBefore(StringConverter.class, true, BINARY);
		registry.addAllBefore(BooleanConverter.class, true, UNARY);
		return registry;
	}
	
	/**
	 * @param registry | Registry to remove all operators from!
	 * 
	 * @return The same registry with no more operators!
	 * 
	 * @since 1.3.8
	 */
	public static ParserRegistry uninstall(ParserRegistry registry) {
		registry.removeAll(Arrays.asList(BINARY));
		registry.removeAll(Arrays.asList(UNARY));
		return registry;
	}
}
