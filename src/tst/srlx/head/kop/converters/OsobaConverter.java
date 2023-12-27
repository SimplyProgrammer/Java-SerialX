package tst.srlx.head.kop.converters;

import org.ugp.serialx.Utils;
import org.ugp.serialx.converters.DataConverter;

import tst.srlx.head.kop.Osoba;


	/*
	 * DataConverter urceny na konvertovanie objektov triedy Osoba na string a spat.
	 * Format: @"menoOsoby" jejVek jejVyska
	 */
	public class OsobaConverter implements DataConverter {
	
		@Override
		public Object parse(ParserRegistry mojRegister, String objektAkoString, Object... args) {
			
			/*
			 * Podmienka ktora zaruci ze dany string z objektom naozaj reprezentuje nasu objekt osoby.
			 * To sa docielime tak ze zistime ci nie je prazdny a ci sa zacina '@'.
			 */
			objektAkoString = objektAkoString.trim();
			if (!objektAkoString.isEmpty() && objektAkoString.charAt(0) == '@') 
			{
				objektAkoString = objektAkoString.substring(1); // Odstranenie zavinacu z nasho stringu.
				
				/*
				 * Tymto rozdelmi string na jednotlive tokeny (slova) s ktorymi budeme nasledne pracovat.
				 */
				String[] tokeny = Utils.splitValues(objektAkoString, ' ');
				
				/*
				 * Vytvarame prazdny objekt osoby ktorej hodnoty nastavime podla nasich tokenov.
				 */
				Osoba osoba = new Osoba(null, -1, -1);
				
				/*
				 * Nastavenie clenskych premennych osoby na ich povodne hodnoty ziskane.
				 * Vieme ze tokeny obsahuju hodnoy mena, veku a vysky v takomto poradi.
				 * Vsimnime si ze jednotlive tokeny je najprv tiez nutne parsnut rekurzivnym volanim parserov.
				 */
				osoba.setMeno(REGISTRY.parse(tokeny[0], true, null, args).toString());
				osoba.setVek(((Number) REGISTRY.parse(tokeny[1], args)).intValue());
				osoba.setVyska(((Number) REGISTRY.parse(tokeny[2], args)).floatValue());

				return osoba; // Vraciame novoytvorenu parsnutu osobu.
			}
			
			return CONTINUE; // Nesmieme zabudnut vratit CONTINUE ked uz vieme ze sa nejedna o objekt triedy Osoba.
		}
	
		@Override
		public CharSequence toString(ParserRegistry mojRegister, Object objekt, Object... args) {
			
			/*
			 * Podmienka ktorou zistime ci sa jedna o objekt triedy Osoba
			 */
			if (objekt instanceof Osoba)
			{
				Osoba objektAkoString = (Osoba) objekt;
				
				/*
				 * Konvertovanie objektu osoba na string serialovej podoby.
				 * Opat za postupneho recurzivneho konvertovanie vsetkych jeho clensky premennych na string.
				 * 
				 */
				StringBuilder serialovaForma = new StringBuilder("@");
				serialovaForma.append(mojRegister.toString(objektAkoString.getMeno(), args)).append(' ');
				serialovaForma.append(mojRegister.toString(objektAkoString.getVek(), args)).append(' ');
				return serialovaForma.append(mojRegister.toString(objektAkoString.getVyska(), args));
			}
			
			return CONTINUE; // Ani tu nesmieme zabudnut vratit CONTINUE ked uz vieme ze sa nejedna o objekt triedy Osoba. 
		}
	
	}

	
