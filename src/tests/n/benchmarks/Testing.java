package tests.n.benchmarks;

import java.awt.List;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.ugp.serialx.Utils;
import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.juss.converters.ObjectConverter;
import org.ugp.serialx.juss.converters.VariableConverter;

/**
 * Testing random algorithms...
 */
public class Testing {

	public static void main(String[] args) throws Exception {
			
		System.out.println(Arrays.asList(Utils.splitValues("123=123 == 55 =2=", "123=123 == 55 =2=".indexOf('='), 0, 1, new char[0], '=')));
		System.out.println(Arrays.asList(Utils.splitValues("==123=123 == 55 = 4", 0, 0, 1, new char[0], '=')));
		System.out.println(Arrays.asList(Utils.splitValues("=9", 0, 0, 0, new char[0], '=')));
		System.out.println(Arrays.asList(Utils.splitValues("===98==9", 0, 0, 1, new char[0], '=')));
		System.out.println(Arrays.asList(Utils.splitValues("10   98", 0, 0, 2, new char[0], ' ')));
		
		String str = "srlxVer1 = srlxVer2 = $dependencies.something.dataStorage.serialx.version";
		System.out.println(Arrays.asList(Utils.splitValues(str, VariableConverter.isVarAssignment(str), 0, 1, new char[0], '=')));
		
//		JussSerializer.JUSS_PARSERS.get(ObjectConverter.class).setAllowStaticMemberInvocation(true);
//		
//		File file = new File("src/examples/implementations/test.juss");
//		
//		JussSerializer deserializer = new JussSerializer();
//		deserializer.LoadFrom(file);
//		
//		System.out.println(deserializer);
//		System.out.println(deserializer.<Object>get(new String[] { "kkt", "a" }));
//		
//		for (int i = 0; i < 12; i++) {
//			
//			System.out.println(deserializer.getParent(i));
//		}

	}
}
