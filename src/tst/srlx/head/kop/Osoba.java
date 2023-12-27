package tst.srlx.head.kop;

	/*
	 * Deklaracia triedy Osoba
	 */
	public class Osoba 
	{
		private int vek;
		private float vyska;
		private String meno;
		
		/*
		 * Toto je konstruktor, specialna metoda ktora nam vrati novu instanciu objektu s prislusnymi parametrami ktore mu vlozime.
		 */
		public Osoba(String meno, int vek, float vyska) 
		{
			setMeno(meno);
			setVyska(vyska);
			setVek(vek);
		}
		
		/*
		 * Verejna metoda ktora pracuje s premennymi objektu.
		 * V tomto konkretnom pripade nam "predstavi" danu osobu!
		 */
		public void predstavSaPubliku() {
			System.out.println("Zdravim! Moje meno je " + meno + ", mam " + vek + " rokov a meriam " + vyska + "cm.");
			System.out.println("Rad vas vsetkych spoznavam!");
		}
		
		/*
		 * Verejne pristupove metody, sluziace na pristup k premennym z vonka triedy, a osetrenie nechcenych stavov.
		 */
	
		public int getVek() {
			return vek;
		}
	
		public void setVek(int vek) {
			this.vek = vek < 0 ? 0 : vek;
		}
	
		public float getVyska() {
			return vyska;
		}
	
		public void setVyska(float vyska) {
			this.vyska = vyska < 0 ? 0 : vyska;
		}
	
		public String getMeno() {
			return meno;
		}
	
		public void setMeno(String meno) {
			this.meno = meno;
		}
	}





