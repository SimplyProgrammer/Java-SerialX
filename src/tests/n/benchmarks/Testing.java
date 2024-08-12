package tests.n.benchmarks;

import java.util.Arrays;

import org.ugp.serialx.Utils;
import org.ugp.serialx.juss.converters.OperationGroups;
import org.ugp.serialx.juss.converters.VariableConverter;

/**
 * Testing random algorithms...
 */
public class Testing {

	public static void main(String[] args) throws Exception {
			
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
		
		str = "jjiij {ha -> asd } \"hchaha\"\" a->b\" aaa bbb ha";
		System.err.println(Utils.showPosInString(str, Utils.indexOfNotInObj(str, 0, str.length(), -1, true, "->")));

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
