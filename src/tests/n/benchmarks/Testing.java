package tests.n.benchmarks;

import java.awt.List;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.ugp.serialx.Utils;
import org.ugp.serialx.juss.JussSerializer;

/**
 * Testing random algorithms...
 */
public class Testing {

	public static void main(String[] args) throws Exception {
		
		File file = new File("src/examples/implementations/test.juss");
		
		JussSerializer deserializer = new JussSerializer();
		deserializer.LoadFrom(file);
		
		System.out.println(deserializer);
		System.out.println(deserializer.<Object>get(new String[] { "kkt", "a" }));
		
		for (int i = 0; i < 12; i++) {
			
			System.out.println(deserializer.getParent(i));
		}

	}
}
