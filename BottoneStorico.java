/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * BottoneStorico.java richiede:
 * - Path.java
 * - Calendario.java
 * - FileHandler.java
 * - TabellaPiano.java
 * - TabellaQuote.java
 */

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class BottoneStorico extends JPanel 
{
    private String path_archivio = Path.getPathPiani();
    private String path_quote = Path.getPathQuote();
    private String path_soluzioni = Path.getPathSoluzioni();
    private String path_ferie = Path.getPathCompatibilità();
    
    private JPanel pannello_tabelle = new JPanel(); 
    private JScrollPane pannello_tabella_piano = new JScrollPane(); 
    private JScrollPane pannello_tabella_quote = new JScrollPane();
    
    private TabellaPiano tabella_piano;
    private TabellaQuote tabella_quote;
    
    private JList<String> lista_mesi = FileHandler.ListaCartella(path_archivio);
    private int dimensione_lista = lista_mesi.getModel().getSize();
    private String selezione = lista_mesi.getModel().getElementAt(0);
	    
	public BottoneStorico()
	{		
        super(new BorderLayout());

        JComboBox<String> menu = new JComboBox<String>();
        DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<String>();
       
        for (int i = 0; i < dimensione_lista; i++)
        {
        	String tmp = lista_mesi.getModel().getElementAt(i);
        		//listModel.addElement(tmp.substring(tmp.indexOf('_')+1, tmp.indexOf('.')));
        		listModel.addElement(tmp);
        }

        menu.setModel(listModel);
        
        add(menu, BorderLayout.PAGE_START);        
        
        JButton Stampa = new JButton("Stampa piano");
        JButton Esporta = new JButton("Esporta in Excel");
        JButton Modifica = new JButton("Ricalcolo consuntivo");

        pannello_tabelle.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        pannello_tabella_piano.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
                "Piano di " + EstraiMese(selezione) + " " + EstraiAnno(selezione),
                TitledBorder.CENTER,
                TitledBorder.TOP));
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH; 
        c.weightx = 1.0;
        c.weighty = 1.0;
        pannello_tabelle.add(pannello_tabella_piano, c);
        
        pannello_tabella_quote.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
                "Quote di lavoro",
                TitledBorder.CENTER,
                TitledBorder.TOP));
        
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH; 
        c.weightx = 1.0;
        c.weighty = 1.0;
        pannello_tabelle.add(pannello_tabella_quote, c);          
        
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,BoxLayout.LINE_AXIS));
        buttonPane.add(Stampa);
        buttonPane.add(Box.createHorizontalStrut(15));
        buttonPane.add(Esporta);
        buttonPane.add(Box.createHorizontalStrut(15));
        buttonPane.add(Modifica);      
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(pannello_tabelle, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END); 
        
        MostraTabelle(FetchTable(path_archivio + selezione), FetchTable(path_quote + selezione));
        
        
	    Modifica.addActionListener
	    (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {	                   
 	                  int selectedOption = JOptionPane.showConfirmDialog(null, 
                              "Questa tabella contiene i dati dell'archivio storico per questo mese. \n "
                              + "Modificandola verranno cancellati tutti i piani previsti per i mesi successivi. \n"
                              + " Sei sicuro di voler proseguire?", 
                              "ATTENZIONE", 
                              JOptionPane.YES_NO_OPTION); 
                      if (selectedOption == JOptionPane.YES_OPTION)
                      {
	                      try 
                          {
		                      FileHandler.saveTable(tabella_piano.getTable(), new File(path_archivio + selezione));    		                      
		                      tabella_quote.CalcolaQuote(tabella_piano.getTable(), FetchTable(path_ferie + selezione));
		                      FileHandler.saveTable(tabella_quote.getTable(), new File(path_quote + selezione)); 
		                      
                              JOptionPane.showMessageDialog(null,"L'archivio è stato modificato");
		                      
		                      int file_rimossi = FileHandler.PulisciCartella(path_archivio, selezione);
		                      FileHandler.PulisciCartella(path_soluzioni, selezione);
		                      FileHandler.PulisciCartella(path_quote, selezione);		                      
		                      
		                      for (int i = 0; i < file_rimossi; i++)
		                      {
		                    	  dimensione_lista--;
		                    	  listModel.removeElementAt(dimensione_lista);
		                      }		                    	
                          }
                      
                          catch (IOException ex) 
                          {
                              System.out.println(ex.getMessage());
                              ex.printStackTrace();
                          }    	
	
                      }
                }
            }
	    );  
	    
	    Stampa.addActionListener
	    (
	        new ActionListener()
		    {
		        public void actionPerformed(java.awt.event.ActionEvent ignore) 
		        {
	                MessageFormat header = new MessageFormat(EstraiMese(selezione) + " " + EstraiAnno(selezione));
	                try 
	                {
	                    tabella_piano.getTable().print(JTable.PrintMode.NORMAL, header, null);
	                }
	                catch (java.awt.print.PrinterException e) 
	                {
	                    System.err.format("Cannot print %s%n", e.getMessage());
	                }
		        }
		    }
	    );         
	    
	    Esporta.addActionListener
	    (
	        new ActionListener()
		    {
		        public void actionPerformed(java.awt.event.ActionEvent ignore) 
		        {
	                try 
	                {
	                    FileHandler.JTableToExcel(tabella_piano.getTable());
	                }
	                catch (IOException e) 
	                {
	                    System.err.format("Cannot print %s%n", e.getMessage());
	                }
		        }
		    }
	    ); 
	    
	    menu.addActionListener 
	    (
		       new ActionListener()
	           {
	               public void actionPerformed(ActionEvent e)
	               {
	            	   //System.out.println(selezione);
		               @SuppressWarnings("unchecked")
			           JComboBox<String> cb = (JComboBox<String>)e.getSource();
		               selezione = (String)cb.getSelectedItem();		        
		               
		               pannello_tabella_piano.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
		                       "Piano di " + EstraiMese(selezione) + " " + EstraiAnno(selezione),
		                       TitledBorder.CENTER,
		                       TitledBorder.TOP));
		               
		               MostraTabelle(FetchTable(path_archivio + selezione), FetchTable(path_quote + selezione));	
	               }
	           }
		 );    
    
	}	
	
	//recupera la tabella di dati relativa a quel mese (se c'era già)
	private JTable FetchTable(String path)
	{
		JTable temp = new JTable();
		try 
		{
            temp = FileHandler.LoadTable(new File(path));
		}     				

		catch (IOException ex) 
		{
		    System.out.println(ex.getMessage());
			ex.printStackTrace();
	    }
		
        return temp;
	}
	
	//sostituisce la tabella del pannello con quella passata in input
	private void MostraTabelle (JTable nuovo_piano, JTable nuove_quote)
	{	    		     		    	    		        
 		DefaultListModel<String> listModel = new DefaultListModel<String>();
 	        	             		   
 	    for (int i = 0; i < nuovo_piano.getRowCount(); i++)
 	    	listModel.addElement(nuovo_piano.getValueAt(i,0).toString());
 	        
 	    tabella_piano = new TabellaPiano (new JList<String> (listModel), Calendario.ListaGiorni(EstraiMese(selezione), EstraiAnno(selezione)), nuovo_piano);	        
 	    tabella_quote = new TabellaQuote (new JList<String> (listModel), nuove_quote);	
 		
        pannello_tabella_piano.setViewportView(tabella_piano.getTable());		
        pannello_tabella_quote.setViewportView(tabella_quote.getTable());	
	}	
	
	private String EstraiMese (String selezione)
	{
		return (selezione.substring(selezione.indexOf('_')+1, selezione.indexOf(' ')));
	}
	
	private int EstraiAnno (String selezione)
	{
		return (Integer.parseInt(selezione.substring(selezione.indexOf(' ')+1, selezione.indexOf('.'))));	
	}
}
