/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * Calendario.java è una classe statica che non richiede altri file.
 */

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public final class Calendario 
{	  
    private static Calendar calendario = GregorianCalendar.getInstance();
    private static int giorno_corrente = calendario.get(GregorianCalendar.DAY_OF_MONTH);
    private static String mese_corrente = numberToMonth(calendario.get(GregorianCalendar.MONTH)); 
    private static int anno_corrente = calendario.get(GregorianCalendar.YEAR);
    
    //converte un mese in un intero
    public static int monthToNumber(String mese)
    {
        switch(mese)
        {
    	    case "Gennaio": return 0;
    	    case "Febbraio": return 1;
    	    case "Marzo": return 2;
    	    case "Aprile": return 3;
    	    case "Maggio": return 4;
    	    case "Giugno": return 5;
    	    case "Luglio": return 6;
    	    case "Agosto": return 7;
    	    case "Settembre": return 8;
    	    case "Ottobre": return 9;
    	    case "Novembre": return 10;
    	    case "Dicembre": return 11;
    	      
    	    default: return -1;
        }
    }
      
    //converte un intero in un mese
    public static String numberToMonth(int mese)
    {
 	    switch(mese)
    	{
    	    case 0: return "Gennaio";
    	    case 1: return "Febbraio";
    	    case 2: return "Marzo";
    	    case 3: return "Aprile";
    	    case 4:  return "Maggio";
    	    case 5: return "Giugno";
    	    case 6: return "Luglio";
    	    case 7: return "Agosto";
    	    case 8:  return "Settembre";
    	    case 9:  return "Ottobre";
    	    case 10: return "Novembre";
    	    case 11:  return "Dicembre";
    	      
    	    default: return "Errore";
    	}
    }
      
    //ritorna il nome del mese prima di quello indicato
    public static String getMesePrecedente(String mese)
    {
  	  switch(mese)
  	  {
  	      case "Gennaio": return "Dicembre";
  	      case "Febbraio": return "Gennaio";
  	      case "Marzo": return "Febbraio";
  	      case "Aprile": return "Marzo";
  	      case "Maggio": return "Aprile";
  	      case "Giugno": return "Maggio";
  	      case "Luglio": return "Giugno";
  	      case "Agosto": return "Luglio";
  	      case "Settembre": return "Agosto";
  	      case "Ottobre": return "Settembre";
  	      case "Novembre": return "Ottobre";
  	      case "Dicembre": return "Novembre";
  	      
  	      default: return "Errore";
  	  }
    }
    
    //ritorna il nome del prossimo mese
    public static String prossimoMese(String elemento)
    {
        int mese = monthToNumber(elemento.substring(elemento.indexOf('_')+1, elemento.indexOf(' ')));
        int anno = Integer.parseInt(elemento.substring(elemento.indexOf(' ')+1, elemento.indexOf('.')));

        if (mese == 11) 
        {
    	    anno++;
    	    return "Gennaio" + " " + anno;
         }
         else return numberToMonth(mese+1) + " " + anno; 	  
    }
    
    //ritorna il giorno corrente (un intero)
    public static int getDay()
    {
    	return giorno_corrente;
    }
    
    //ritorna il mese corrente (una stringa)
    public static String getMonth()
    {
    	return mese_corrente;
    }  
    
    //ritorna l'anno corrente (un intero)
    public static int getYear()
    {
    	return anno_corrente;
    }
    
    //ritorna vero se siamo nel periodo di pianificazione specificato (che non coincide col mese)
    public static boolean MeseCorrente(String mese, int anno)
    {
    	if ((mese_corrente.equals(mese)) && (anno_corrente == anno)) return true;
    	if ((getMesePrecedente(mese_corrente).equals(mese)) && (anno_corrente == anno) && (giorno_corrente <= FindFirstSunday(monthToNumber(mese_corrente), anno))) return true;
    	if ((mese_corrente.equals("Gennaio")) && (mese.equals("Dicembre")) && (anno_corrente == anno + 1) && (giorno_corrente <= FindFirstSunday(monthToNumber(mese_corrente), anno))) return true;
    	return false;
    }
    
    //ritorna l'indice di colonna del giorno corrente nella tabella
    public static int IndiceGiorno (String mese, int anno) 
    {
    	//System.out.println (mese + " ma siamo a " + mese_corrente);
    	int offset = 1 - FindFirstMonday(monthToNumber(mese), anno);
    	if (!(mese.equals(mese_corrente))) offset = offset + DayInMonth (monthToNumber(mese), anno);
        
    	return giorno_corrente + offset; //la prima colonna non è un giorno
    }      
    
    //ritorna il numero di giorni in quel mese/anno
	private static int DayInMonth (int mese, int anno)
    {
    	GregorianCalendar tmp = new GregorianCalendar(anno, mese, 1); 
        return tmp.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    }
    
	//ritorna una stringa con il nome del giorno della settimana indicato
	private static String NomeEstesoGiorno(int giorno, int mese, int anno)
    {
    	GregorianCalendar tmp = new GregorianCalendar(anno, mese, giorno); 
    	int day_of_week = tmp.get(GregorianCalendar.DAY_OF_WEEK);

    	switch(day_of_week)
  	    {
  	        case 2: return "Lun " + giorno;
  	        case 3: return "Mar " + giorno;
  	        case 4: return "Mer " + giorno;
  	        case 5: return "Gio " + giorno;
  	        case 6: return "Ven " + giorno;
  	        case 7: return "Sab " + giorno;
  	        case 1: return "Dom " + giorno;
  	        
  	        default: return "Errore";
  	    }
    }
    
    //ritorna l'indice del primo lunedì del mese (si parte da 1 e non da 0)
	@SuppressWarnings("static-access")
	private static int FindFirstMonday (int mese, int anno)
    {
    	int i = 1;
    	GregorianCalendar tmp = new GregorianCalendar(anno, mese, i); 
    	int day_of_week = tmp.get(GregorianCalendar.DAY_OF_WEEK);
    	
    	while (day_of_week != 2)
    	{ 
            tmp.set(tmp.DAY_OF_MONTH, ++i);
            day_of_week = tmp.get(GregorianCalendar.DAY_OF_WEEK);   		
    	}
    	return i;
    }
    
	@SuppressWarnings("static-access")
	private static int FindFirstSunday (int mese, int anno)
    {
    	int i = 1;
    	GregorianCalendar tmp = new GregorianCalendar(anno, mese, i); 
    	int day_of_week = tmp.get(GregorianCalendar.DAY_OF_WEEK);
    	
    	while (day_of_week != 1)
    	{ 
            tmp.set(tmp.DAY_OF_MONTH, ++i);
            day_of_week = tmp.get(GregorianCalendar.DAY_OF_WEEK);   		
    	}
    	return i;
    }
    
    //ritorna l'indice dell'ultima domenica del mese (eventualmente sforando nel mese successivo)
    private static int FindLastSunday (int mese, int anno)
    {
    	int i = DayInMonth (mese, anno);
    	GregorianCalendar tmp = new GregorianCalendar(anno, mese, i); 
    	
        if (tmp.get(GregorianCalendar.DAY_OF_WEEK) == 1) return i;
        else if (mese == 11) return FindFirstSunday(0, anno+1);
        else return FindFirstSunday(mese+1, anno);
    }

    
    //ritorna una lista con i nomi dei giorni di quel mese, diviso per settimane intere
    //esempio: "Lunedì 5", "Martedì 6" eccetera, fino a "Sabato 30", "Domenica 1"
    public static JList<String> ListaGiorni (String mese_in_lettere, int anno)
    {
    	//System.out.println("faccio la lista di:  " + mese_in_lettere + " " + anno);
    	
    	int mese = monthToNumber(mese_in_lettere); 
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        
        for (int i = FindFirstMonday (mese, anno); i <= DayInMonth (mese, anno); i++)
        	listModel.addElement(NomeEstesoGiorno(i, mese, anno));
        
        //se l'ultimo giorno di quel mese non è domenica vuol dire che bisogna sforare nel mese successivo
        int domenica = FindLastSunday (mese, anno);
        if (domenica < 10)
        {
        	for (int i = 1; i <= domenica; i++)
        		listModel.addElement(NomeEstesoGiorno(i, mese+1, anno));
        }

        //Crea la lista        
        return new JList<String>(listModel);  	    
    }  	
} 











