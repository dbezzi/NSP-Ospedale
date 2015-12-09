/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * Path.java è una classe statica che non richiede altri file.
 */

public final class Path
{	
    private static String path_medici = "Archivio\\Medici\\";
    private static String path_compatibilità = "Archivio\\Compatibilità\\";
    private static String path_piani = "Archivio\\Piani\\";
    private static String path_soluzioni = "Archivio\\Soluzioni\\";
    private static String path_dati = "Archivio\\Dati\\";
    private static String path_quote = "Archivio\\Quote\\";

	public static String getPathMedici() 
	{
		return path_medici;
	}
	
	public static String getPathQuote() 
	{
		return path_quote;
	}

	public static String getPathCompatibilità()
	{
		return path_compatibilità;
	}

	public static String getPathDati()
	{
		return path_dati;
	}
	
	public static String getPathSoluzioni()
	{
		return path_soluzioni;
	}
	
	public static String getPathPiani()
	{
		return path_piani;
	}
}
	
