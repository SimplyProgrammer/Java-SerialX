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

/**
 * Testing random algorithms...
 */
public class Testing {

	public static void main(String[] args) throws Exception {
		
		String str = "123 == 123";
		double l = 123;
//		String[] path = Utils.splitValues(str, 0, false, new char[0], '=');
//		
//		System.out.println(Arrays.asList(path));
		
		System.out.println(Benchmarks.numberOf("1.612e-19", 10, 0));
		
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
