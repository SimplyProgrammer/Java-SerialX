package examples.implementations;

import java.io.IOException;

import org.ugp.serialx.json.JsonSerializer;

/**
 * In this example we can see how to perform json reading from remote web url!
 * Note: Internet connection is required for this example to work!
 * 
 * @author PETO
 *
 * @since 1.3.2
 */
public class ReadingJsonFromInternet 
{
	public static void main(String[] args) throws IOException, Exception 
	{
		/* 
		//---------------------- Before SerialX 1.3.5 ---------------------- 
		//Creating JsonSerializer that can parse json!
		JsonSerializer reader = new JsonSerializer();
		
		InputStream urlInput = new URL("https://jsonplaceholder.typicode.com/users").openStream(); //Establishing connection with https://jsonplaceholder.typicode.com/users and getting stream of received data!
		reader.LoadFrom(urlInput); //Parsing url stream content into json!
		*/
		
		JsonSerializer reader = JsonSerializer.from("https://jsonplaceholder.typicode.com/users"); //Getting and deserializing data from remote web address!
		
		String user = "Glenna Reichert"; //User we want to get (Glenna Reichert)!
		String glennasCompany = reader.getScopesWith("name", user).getScope(0).getString("name"); //Obtaining first scope that contains variable with users name and getting name of his company as string from it!
		System.out.println(user + " is working for " + glennasCompany); //Printing results!
	}
}
