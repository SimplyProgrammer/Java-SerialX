package tests.n.benchmarks;

import java.io.StringReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.juss.converters.OperationGroups;
import org.ugp.serialx.juss.converters.VariableConverter;
import org.ugp.serialx.utils.Utils;

import joptsimple.internal.Strings;

/**
 * Testing random algorithms...
 */
public class Testing {
	
	static final String tst = "F;\r\n"
			+ "1004785.25;\r\n"
			+ "1004786,\r\n"
			+ "\"be{nc'h3\";\r\n"
			+ " 111  ;\r\n"
			+ " /*Xddddd"
			+ "\r\ndddddddddd*/\r\n"
			+ "123 ;\r\n"
			+ "// Loool\r\n"
			+ "                  1004794 		;\r\n"
			+ "\"  bench11\";		\r\n"
			+ "T,,,\r\n"
			+ "1004797.25;\r\n"
			+ "{} {} {}\r\n"
			+ " \"bench15\";\r\n"
			+ "  F          ;\r\n"
			+ "{   1004801.25;\r\n"
			+ "1004802;\r\n"
			+ "\"bench19\";\r\n"
			+ "T;\r\n"
			+ "1004805.25;\r\n"
			+ "1004806;\r\n"
			+ "\"bench23\";\r\n"
			+ "F;\r\n"
			+ "1004809.25;\r\n"
			+ "1004810;\r\n"
			+ "\"ben}ch27\";\r\n"
			+ "T; }		";

	public static void main(String[] args) throws Exception {
		System.out.println(System.getProperty("java.version"));
	
		System.out.println(Arrays.asList(Utils.splitValues("123=123 == 55 =2=", "123=123 == 55 =2=".indexOf('='), 0, 1, new char[0], '=')));
		System.out.println(Arrays.asList(Utils.splitValues("==123=123 == \"55\" = 4", 0, 0, 1, new char[0], '=')));
		System.out.println(Arrays.asList(Utils.splitValues("=9", 0, 0, 0, new char[0], '=')));
		System.out.println(Arrays.asList(Utils.splitValues("===98==9", 0, 0, 1, new char[0], '=')));
		System.out.println(Arrays.asList(Utils.splitValues("10   98", 0, 0, 2, new char[0], ' ')));
		
		String str = "srlxVer1 = srlxVer2 = $dependencies.something.dataStorage.serialx.version";
		System.out.println(Arrays.asList(Utils.splitValues(str, VariableConverter.isVarAssignment(str), 0, 1, new char[0], '=')));
		System.out.println(Utils.showPosInString("abc", 1));		
		
		System.out.println(1 +-6 / -2*(2+1)%- 100 + 1);
		
		char mark = (char) new OperationGroups().hashCode();
		System.out.println(OperationGroups.isGroupMark(new StringBuilder().append(mark--).append(21).append(mark), ++mark));
		
//		str = "jjiij {ha -> asd } \"hchaha\" a->b\" aaa bbb ha {}";
//		System.err.println(Utils.showPosInString(str, Utils.indexOfNotInObj(str, 0, str.length(), -1, true, "->")));
		
//		StringReader sr = new StringReader("a");
//		System.out.println(sr.read());
//		System.out.println(sr.read());
//		System.out.println(sr.read());
		
//		JussSerializer test = new JussSerializer();
//		
//		StringBuilder str1 = test.readAndFormat(new StringReader(tst), true);
//		test.splitAndParse(str1);
//		
//		test.readAndParse(new ArrayList<>(), new StringReader(tst), 512);
		
//		new JsonSerializer(new Object[] {1,2,3}, new HashMap<>(), GenericScope.mapKvArray(new HashMap<>(), "hi", 123), null, new ArrayList<>()).SerializeTo(new File("src/tests/n/benchmarks/test.json"));
		
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
