/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * ElencoMediciy.java è una classe statica che non richiede altri file.
 */

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.JList;
 
public final class FileHandler
{ 	
	//ritorna una JTable vuota delle dimensioni passate come parametro
	private static JTable TabellaGenerica (int righe, int colonne) 
    {        
		String[] columnNames = {};
        String[][] data = {};
    
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(data, columnNames); 
    
        for (int i = 0; i < colonne; i++)
        	model.addColumn(null);        
    
        for (int i = 0; i < righe; i++)
        	model.addRow(new Object[]{null});
    
        table.setModel(model); 
        return table;
    }
	
    private static void DeleteFile (File file) throws IOException 
    {
        try
        {
 	        if(file.delete())
 	        {
    			//System.out.println(file.getName() + " is deleted!");
    		}
 	        else
 	        {
    			System.out.println(file + ": Delete operation is failed.");
    		}   
    	}
        catch(Exception e)
        {
            e.printStackTrace();
    	}
    }
    
	//esporta la tabella come xls facendo scegliere all'utente la destinazione
    public static void JTableToExcel(JTable table) throws IOException 
    {
    	JFileChooser fc = new JFileChooser();
        int option = fc.showSaveDialog(null);
        if(option == JFileChooser.APPROVE_OPTION)
        {
            String filename = fc.getSelectedFile().getName(); 
            String path = fc.getSelectedFile().getParentFile().getPath();

			int len = filename.length();
			String ext = "";
			String file = "";

			if(len > 4)
			{
				ext = filename.substring(len-4, len);
			}

			if(ext.equals(".xls"))
			{
				file = path + "\\" + filename; 
			}
			else
			{
				file = path + "\\" + filename + ".xls"; 
			}
			toExcel(table, new File(file));
		}
    }     
    
    public static void toExcel(JTable table, File file) throws IOException 
    {
	    TableModel model = table.getModel();
		FileWriter excel = new FileWriter(file);

		for(int i = 0; i < model.getColumnCount(); i++)
		    excel.write(model.getColumnName(i) + "\t");
			
		excel.write("\n");

		for(int i=0; i< model.getRowCount(); i++)
		{
			for(int j=0; j < model.getColumnCount(); j++) 
				excel.write(model.getValueAt(i,j).toString()+"\t");
			
			excel.write("\n");
		}
		excel.close();
	}

    //salva una JTable di stringhe sul file indicato
    public static void saveTable(JTable table, File file) throws IOException 
    {
        FileWriter out = new FileWriter(file);
        
        out.write(table.getRowCount() + "\r\n" + table.getColumnCount() + "\r\n");  
        
        for(int column = 0; column < table.getColumnCount(); column++) 
        	for(int row = 0; row < table.getRowCount(); row++)
        		out.write(table.getValueAt(row, column) + "\r\n");
    	
    	out.flush();
    	out.close();
    }
    
    //carica una JTable di stringhe dal file indicato
    public static JTable LoadTable (File file) throws IOException 
    {
        FileReader in = new FileReader(file);
              
        BufferedReader br = new BufferedReader(in); 

        int righe = (int)(Float.parseFloat(br.readLine()));            		
        int colonne = (int)(Float.parseFloat(br.readLine()));
        
        JTable table = TabellaGenerica(righe, colonne);
                
        for(int column = 0; column < colonne; column++)    
            for(int row = 0; row < righe; row++)
                table.setValueAt(br.readLine(), row, column); 
      
        in.close();
        return table;
    }
    
    //ritorna una lista con tutti i nomi dei file nella cartella indicata
    public static JList<String> ListaCartella (String path)
    {    
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        
	    File folder = new File(path);
	    File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++)
	     	listModel.addElement(listOfFiles[i].getName());	    
	   
        return new JList<String>(listModel);  	 
    }
    
    //cancella tutti i file nella cartella indicata dopo quello indicato (quello indicato rimane)
    public static int PulisciCartella (String path, String ultimo_file_sano) throws IOException 
    {     
	    File folder = new File(path);
	    File[] listOfFiles = folder.listFiles();
        boolean elimina = false;
	    int file_cancellati = 0;
        
	    for (int i = 0; i < listOfFiles.length; i++)
	    {
	     	if (listOfFiles[i].getName().equals(ultimo_file_sano)) elimina = true;	    
	     	else if (elimina) 
	        {
	     	    DeleteFile(listOfFiles[i]); 
	     	    file_cancellati++;
	     	}
	    }    	 
	    return file_cancellati;
    }    
    
    //ritorna true se il file indicato esiste
    public static Boolean Esiste (String path)
    { 
    	File f = new File(path);
            
    	if(f.exists()) return true;
    	else return false;
    }
    
    //salva le variabili scritte dal solutore
    public static void savePlan(double[] lista, JTable dati, File file) throws IOException 
    {
        FileWriter out = new FileWriter(file);
        
        int m = dati.getRowCount();
        int g = dati.getColumnCount() - 1;
        out.write(lista.length + "\r\n" + m + "\r\n" + g + "\r\n");  
        
        for(int i = 0; i < m; i++)
        	out.write(dati.getValueAt(i, 0) + "\r\n");
        
        for(int i = 0; i < lista.length; i++)
        	out.write(lista[i] + "\r\n");
   	
    	out.flush();
    	out.close();
    }
    
    //ritorna la lista delle variabili scritte dal solutore
    public static String[] loadPlan(File file) throws IOException 
    {
        FileReader in = new FileReader(file);
        
        BufferedReader br = new BufferedReader(in); 

        int variabili = (int)(Float.parseFloat(br.readLine())); 
        int medici = (int)(Float.parseFloat(br.readLine()));
        int giorni = (int)(Float.parseFloat(br.readLine()));
        
        String[] lista = new String[variabili+medici+3]; 
        lista [0] = medici + "";
        lista [1] = giorni + "";
        lista [2] = variabili + "";
        for (int i = 0; i < medici+variabili; i++)
        	lista[3+i] = (br.readLine());       

        in.close();
        return lista;
    }
}

