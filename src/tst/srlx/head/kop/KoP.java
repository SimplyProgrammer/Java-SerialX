package tst.srlx.head.kop;

import java.io.File;

import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.protocols.SerializationProtocol;

import tst.srlx.head.kop.converters.OsobaConverter;
import tst.srlx.head.kop.protocols.OsobaProtocol;

public class KoP {

	public static void main(String[] args) throws Exception {
		
		// Registracia nasho OsobaProtocolu
		SerializationProtocol.REGISTRY.add(0, new OsobaProtocol());
		
		// Registracia nasho OsobaParseru.
		// Ak chcete aby bol na serializaciu pouzity parser tak odkomentujte tento kod a zakomentujte ten nad tym.
		//DataParser.REGISTRY.add(0, new OsobaConverter());
			
		/* Serializacia */
		ukazkaSerializacie();
		
		System.out.println("\n---- Po serializaci ----\n");
		
		/* Deserializacia */
		//ukazkaDeserializacie();
	}

	/* Serializacia */
	static void ukazkaSerializacie() throws Exception
	{
		// Deklaracia premennej typu Osoba a nasledne vytvorenie osoby Jozko Mrkvicku.
		Osoba osoba = new Osoba("Jozko Mrkvicka", 16, 165); 
		
		// Vytvorenie noveho Serializera (JussSerializer) pre serializaciu do JUSS
		JussSerializer serializer = new JussSerializer();
		
		// Pridanie objektov ktore chceme serializovat
		serializer.add(osoba);
		
		// Serializovanie do urceneho suboru.
		serializer.into(new File("./test.juss"));
	}
	
	/* Deserializacia */
	static void ukazkaDeserializacie() throws Exception
	{
		// Deserializovanie pomocou pomocou Serializeru (JussSerializer) pre formatu JUSS z urceneho suboru.
		JussSerializer deserializer = JussSerializer.from(new File("./test.juss"));
		
		/*
		 * Overenie korektnosi deserializovanych objektov.
		 */
		
		Osoba deserializovanaOsoba = deserializer.get(0);
		deserializovanaOsoba.predstavSaPubliku();
	}
}
