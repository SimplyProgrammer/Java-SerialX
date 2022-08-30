package examples.implementations;

import java.util.List;

import org.ugp.serialx.JussSerializer;
import org.ugp.serialx.Scope;
import org.ugp.serialx.converters.DataParser;

/**
 * This example contains brief example of querying and obtaining real data from deserialized content!
 * 
 * @author PETO
 *
 * @since 1.3.5
 */
public class SimpleQuerying 
{
	public static void main(String[] args) throws Exception 
	{
		//Loading complex juss file "commentedExample.juss"!
		JussSerializer content = JussSerializer.from("src/examples/implementations/commentedExample.juss"); //Since 1.3.5 we can use "from/into API" to load content of scope by just typing its path into "from" method!

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
