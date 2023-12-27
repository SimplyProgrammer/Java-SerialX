package tst.srlx.head.kop.protocols;

import org.ugp.serialx.protocols.SerializationProtocol;

import tst.srlx.head.kop.Osoba;

	/*
	 * Protokol urceny na specifikovanie postupu serializacie a deserializacie triedy Osoba.
	 */
	public class OsobaProtocol extends SerializationProtocol<Osoba> {
		
	
		@Override
		public Object[] serialize(Osoba osoba) {
			
			// Toto je pole reprezentujuce elementarne cleny objektu ako samostane objekty.
			// V tomto pripade chceme serializovat vsetky cleny jheo triedy takze jeho dlzka bude 3.
			Object[] polePrvkovObjektu = new Object[3];
			
			/*
			 * Ulozenie clenskych premennych osoby do nasho pola.
			 * Osoba reprezentuje danu instanciu osoby ktora sa ma serializovat.
			 * Pametajme ze ich poradie je meno, vek, vyska.
			 */
			polePrvkovObjektu[0] = osoba.getMeno();
			polePrvkovObjektu[1] = osoba.getVek();
			polePrvkovObjektu[2] = osoba.getVyska();
			
			return polePrvkovObjektu; // Vratenie pola prvkou reprezentujucu nasu osobu ktore bude serializovane.
		}
	
		@Override
		public Osoba unserialize(Class<? extends Osoba> triedaSerializovanehoObjektu, Object... polePrvkovObjektu) throws Exception {

	
			/*
			 * Vytvorenie prazdneho objektu osoby ktorej hodnoty nastavime na ich povodny stav z pred serializacie.
			 * Takto ziskame osobu totoznu z osobou serializovanou.
			 */
			Osoba osoba = new Osoba(null, -1, -1);
			
			/*
			 * Nastavenie clenskych premennych osoby na ich povodne hodnoty ziskane deserializovanim.
			 * Vieme ze ich poradie je rovnake ako pri serializaci a to meno, vek, vyska takze ich v takom poradi mozme z pola vyberat.
			 */
			osoba.setMeno((String) polePrvkovObjektu[0]);
			osoba.setVek((int) polePrvkovObjektu[1]);
			osoba.setVyska((float) polePrvkovObjektu[2]);
			
			return osoba; // Navratenie deserializovanej osoby totoznej s osobou serializovanov.
		}
	
		
		@Override
		public Class<? extends Osoba> applicableFor() {
			/* 
			 * Vratenim referencie na triedu Osoba specifikujeme ze tento protokol sa ma pouzivat pre tuto triedu
			 * po pripade triedy z nej odvodene.
			 */
			return Osoba.class;
		}
	}



