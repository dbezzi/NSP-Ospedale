/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * ElencoMedici.java è una classe statica che non richiede altri file.
 */

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public final class ElencoMedici
{ 
    private static String path_medici = Path.getPathMedici();
	
    private static DefaultListModel<String> listModel = new DefaultListModel<String>(); 
        
    public static JList<String> LoadList(String mese) 
    {
    	try
    	{
            FileReader in = new FileReader(path_medici + mese); 
        
            BufferedReader br = new BufferedReader(in); 
            String line;

            listModel.clear();
            while ((line = br.readLine()) != null)  
    		    listModel.addElement(line); 
    	
            in.close();  
    	}
		catch (IOException ex) 
		{
		    System.out.println(ex.getMessage());
			ex.printStackTrace();
	    }
        
        return new JList<String>(listModel);  
    }      
    
    public static void WriteList(JList<String> nuovo_elenco, String mese)
    {
    	try
    	{
            FileWriter out = new FileWriter(path_medici + mese);
            listModel = (DefaultListModel<String>) nuovo_elenco.getModel();
        
            for (int i = 0; i < listModel.getSize(); i++)
        	    out.write(listModel.getElementAt(i) + "\r\n");

	        out.flush();
            out.close();
    	}
		catch (IOException ex) 
		{
		    System.out.println(ex.getMessage());
			ex.printStackTrace();
	    }
    }
} 
