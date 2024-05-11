package examples.implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ugp.serialx.GenericScope;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.Utils;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.json.JsonSerializer;
import org.ugp.serialx.json.converters.JsonCharacterConverter;
import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.juss.converters.ObjectConverter;

/**
 * This example contains brief example of querying and obtaining real data from deserialized content!
 * 
 * @author PETO
 *
 * @since 1.3.5
 */
public class SimpleQuerying 
{
	public static final String TEST_1 = "Is the best!";
	public static final String TEST_2 = "app";
	public static final long TEST_3 = 58;
	public static final String TEST_JSON = "Hello world I am Javascript object notation!";
	
	@Test
	public void test() throws Exception 
	{
		SimpleQuerying.main(new String[0]);
	}
	
	public static void main(String[] args) throws Exception 
	{
		LogProvider.instance.setReThrowException(true); // This is for testing purposes, so no error are allowed in this case, comment temporary when necessary... 
		
		JussSerializer.JUSS_PARSERS.get(ObjectConverter.class).setAllowStaticMemberInvocation(true); //This is necessary since 1.3.7
		
		//Preparing to load complex juss file "commentedExample.juss"!
		
		JussSerializer content = new JussSerializer(); // Creating JussSerializer for deserialization...
		
		Scope testScope = new Scope(), testScope2 = new Scope(); 
		testScope2.put("1", TEST_1);
		testScope.put("", testScope2);
		content.putAllKv( // Putting initial variables into it before actual deserialization, these variables will then be accessible inside "commentedExample.juss"!
			"TEST", testScope,
			"TEST_2", TEST_2,
			"", TEST_JSON
		);

		content = (JussSerializer) Serializer.from(content, "src/examples/implementations/commentedExample.juss", new String[0]); //Since 1.3.5 we can use "from/into API" to load content of scope by just typing its path into "from" method!

		//Printing loaded data!
		System.out.println("Used content:\n" + content + "\n");

		/*
		 * Scope#getScope method is capable of getting scopes that are direct sub-scopes of scope but also it will automatically search for required scope 
		 * through every sub-scope in content! Thats why we can get sub-scope stored by "serialx" variable even though it is neasted in 3 parent scopes, Scope#getScope method will 
		 * search for it automatically without necessity of chaining multiple of them. In case of there being more than one "serialx" we can specify the specific path like
		 * content.getScope("dependencies", "something", "serialx")! But in case of there being only one, like we have, this is not really necessary.
		 */
		String serialx = content.getScope("serialx").getString(0); //Getting first independent value of scope stored by variable "serialx"!
		System.out.println("SerialX " + serialx.toLowerCase()); //Printing result!
		
		/**
		 * We often times have scopes where there are data with repetitive structure. In this case in scope stored by "ppl" there are always sub-scopes that have name, age and residence!
		 * We can use Scope#getAllStoredBy to get values of all variables from this sub-scopes.
		 * In this example we will take all age of all people, sum it up and than print average age of them!
		 */
		List<Number> ages = content.getScope("ppl").getAllStoredBy("age"); //Getting all age variables of sub-scopes from "ppl" scope! \
		//Suming them and printing avg age!
		double sum = 0;
		for (Number number : ages) 
			sum += number.doubleValue();
		System.out.println("Avarage age of people is: " + (sum / ages.size()));
		
		/**
		 * Sometimes we do not need to get only values of variables inside of a scope, sometimes we need actual scopes with variables meeting certain criterias,
		 * this is case where method Scope#getScopesWith comes in handy! This method will return sub-scope containing all sub-scopes found that contains variable that meats
		 * a certain condition!
		 * With this we can for example get all people that live in a certain country!
		 */
		String residence = "germany"; //Country of residence!
		Scope residents = content.getScope("ppl").getScopesWith("residence", residanceValue -> ((String) residanceValue).equalsIgnoreCase(residence)); //Getting all people sub-scopes that whose have "residence" variable equal to required residence!
		System.out.println("People liveing in " + residence + " are: " + residents.getAllStoredBy("name")); //Printing names of those who live in Germany!
		
		/**
		 * Perhaps the most powerful querying method is GenericScope#filter that allow you to filter away both independent values as well as variables with values that
		 * does not meet your condition!
		 * In this particular example we are writing pretty simple condition that will filter away everything that is not a scope and has no independent values inside!
		 */
		Scope filtered = (Scope) content.getScope("dataStorage").filter(obj -> ((Scope) obj).valuesCount() > 0); //Filtering
		System.out.println(filtered.variables()); //Printing variables of filtered sub-scope!
		
		/**
		 * One also very powerful method is GenericScope#map that will remap independent values of the scope based on rule you write!
		 * In this case we are taking all values of sub-scope stored by "arr" variable and multiplying them by 2 if they are bigger than 3!
		 * Notice that by returning DataParser#VOID we can filter away the certain values!
		 * 
		 * Honorable mention is also GenericScope#transform that will transform entire scope including values of variables not only independent values like map!
		 */
		List<Object> remappedValues = content.getScope("arr").map(obj -> ((Number) obj).doubleValue() > 3 ? ((Number) obj).doubleValue() * 2 : DataParser.VOID); //Remapping independent values of scope stored by "arr" by multiplying them by 2 if they are bigger than 3!
		System.out.println("Mapped number values: " + remappedValues); //Printing remapped independent values of "arr"!
		
		/**
		 * We can use GenericScope#map in combination with Scope#toObject and Scope#into methods to remap scopes into real java objects!
		 * For instance we can remap all all scopes representing residents of Germany into real Java sample Person objects!
		 */
		List<Object> realResidents = residents.map(obj -> {
			try 
			{
				return ((Scope) obj).into(Person.class); //Turning scopes into real Java objects!
			} 
			catch (Exception e) 
			{
				return DataParser.VOID; //We already know that this will filter away the object!
			}
		});
		System.out.println("Real \"Java\" residents of " + residence + " are: " + realResidents); //Printing results

		//Performing tests, note that correctness of these tests depends on src/examples/implementations/commentedExample.juss
		assertEquals(TEST_1, serialx);
		assertEquals(content.getScope("ppl").valuesCount(), ages.size());
		assertEquals(filtered.getScope("serialx"), content.getScope("serialx"));
		
		assertTrue(residents.toObject(List.class).size() > 0); //Should not be 0
		assertTrue(remappedValues.size() > 0);
		
		JsonSerializer test = new JsonSerializer();
		assertEquals(new Scope(residents).clone().clone(JussSerializer.class).into(test).valuesCount(), realResidents.size());
		assertFalse(realResidents.contains(DataParser.VOID) || realResidents.contains(null));
		assertEquals(residents, test);
		
		GenericScope<String, ?> sc = content.getScope("devDependencies", "ppl", "ludvig");
		assertEquals(TEST_2, content.get(-1));
		assertEquals(((Scope) sc).getString("residence"), "null");
		assertEquals(TEST_3, ((Scope) sc).getLong("age"));
		assertEquals(((Scope) content.transformToScope()).getScope("alienFruit").toObjectOf("variants", List.class), new GenericScope<>(1, 2, 3).values());
		
		assertNotEquals(TEST_2, (sc = content.getScope("devDependencies")).get("name"));
		assertNull(sc.get(-1));
		assertNull(sc.get("nope"));

		sc = sc.getGenericScope("something");
		assertNotEquals(sc.get(0), sc.get(1));
		assertFalse(sc.containsVariable("version"));
		
		assertNotNull(content.get("srlxVer1"));
		assertEquals(content.get("srlxVer1"), content.getString("srlxVer1"));
		
		sc = content.getScope("jsonCrossover");
		assertTrue(sc instanceof JsonSerializer);
		assertTrue(Scope.from(sc).castTo(Scope.class) instanceof Scope);
		assertEquals(TEST_JSON, sc.get("he" + Utils.multilpy("l", 2) + "o"));
		assertFalse(sc.getGenericScope("jsonArray").isEmpty());
		assertEquals(sc.getClass(), content.get("jsonArrayCla" + Utils.multilpy('s', 2)));
		
		
		assertEquals(content.<Object, Object>getGenericScope("genericScope").<Object>get(Arrays.asList((Object[]) content.getParsers().parse("(1_0_0e-2 (0b10 ) 3d)"))), new JsonCharacterConverter(false).toString(null, '{') /* DO NOT DO IN PROD ("123") */);
		assertEquals(content.<Object, Object>getGenericScope("genericScope").<Scope>get(Arrays.asList(-+-+04, -+-5.000, 0x6)).<Object>get(0), 456d);
		assertEquals(content.<Object, Object>getGenericScope("genericScope").<Scope>get(Arrays.asList(.7, 8f, (byte) +-+-9)).<Object>get("test"), 789l);

		assertNotNull(sc = content.getScope("arr"));
		for (Object obj : content.getScope("superArr"))
			assertTrue(sc.equals(obj));
	}
	
	/**
	 * Dummy class, part of SerialX {@link SimpleQuerying} example!<br>
	 * Note: In order for {@link Scope#toObject} and {@link Scope#into} and methods to work, object must have valid getters and setters!
	 * 
	 * @author PETO
	 * 
	 * @since 1.3.5
	 */
	public static class Person
	{
		protected String name, residance;
		protected double age;
		
		public Person(String name, String residance, double age) 
		{
			this.name = name;
			this.residance = residance;
			this.age = age;
		}
		
		public String getName() 
		{
			return name;
		}

		public void setName(String name) 
		{
			this.name = name;
		}

		public String getResidance() 
		{
			return residance;
		}

		public void setResidance(String residance) 
		{
			this.residance = residance;
		}

		public double getAge() 
		{
			return age;
		}

		public void setAge(double age)
		{
			this.age = age;
		}

		@Override
		public String toString() 
		{
			return "Person[name=" + name + ", residance=" + residance + ", age=" + age + "]";
		}
	}
}
