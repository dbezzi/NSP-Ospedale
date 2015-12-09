/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * BottonePianifica.java richiede:
 * - Path.java
 * - Calendario.java
 * - FileHandler.java
 * - TabellaDati.java
 * - TabellaCompatibilità.java
 * - TabellaOttimizzazione.java
 */

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings("serial")
public class BottonePianifica extends JPanel
{	
	private String path_medici = Path.getPathMedici();
    private String path_dati = Path.getPathDati();
    private String path_compatibilità = Path.getPathCompatibilità();
    private String path_archivio = Path.getPathPiani();
    private String path_quote = Path.getPathQuote();
    private String path_soluzioni = Path.getPathSoluzioni();
     
    private JPanel pannello_tabelle = new JPanel(); 
    private JScrollPane pannello_tabella_dati = new JScrollPane(); 
    private JScrollPane pannello_tabella_compatibilità = new JScrollPane();
    private JScrollPane pannello_tabella_soluzione = new JScrollPane();
    
    private TabellaDati tabella_dati;
    private TabellaCompatibilità tabella_compatibilità;
    private TabellaOttimizzazione tabella_soluzione;
    
    private JList<String> lista_mesi = FileHandler.ListaCartella(path_dati);
    private int dimensione_lista = 0;
    private String selezione;
    
	private boolean abbiamo_tempo;
    
	public BottonePianifica()
	{		
        super(new BorderLayout());

        JComboBox<String> menu = new JComboBox<String>();
        DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<String>();

        boolean stop = false;
        for (int i = 0; i < lista_mesi.getModel().getSize(); i++)
        {
        	String tmp = lista_mesi.getModel().getElementAt(i); //listModel.addElement(tmp); dimensione_lista++;
        	if (!stop) stop =  Calendario.MeseCorrente(EstraiMese(tmp), EstraiAnno(tmp));
            if (stop) {listModel.addElement(tmp); dimensione_lista++;}   	
        }
        String tmp = Calendario.prossimoMese(listModel.getElementAt(dimensione_lista-1));
        int p = lista_mesi.getModel().getSize() + 1;
        String prossimo = p + "_" + tmp + ".txt";

        if (FileHandler.Esiste(path_medici + prossimo))
        {       
            listModel.addElement(prossimo);
        }
        else dimensione_lista--;
        
        selezione = listModel.getElementAt(dimensione_lista);
        
        menu.setModel(listModel);
        menu.setSelectedItem(selezione);
        
        add(menu, BorderLayout.PAGE_START);
        
        JButton Salva = new JButton("Salva i dati");
        JButton Reset = new JButton("Resetta i dati");
        JButton Risolvi = new JButton("Risolvi");  
        JButton SalvaSol = new JButton("Archivia piano");
        SalvaSol.setEnabled(false);

        pannello_tabelle.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        MostraTabelleInputVuote();
        MostraTabellaOutput();
        
        pannello_tabella_dati.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
                "Pianificazione turni di " + EstraiMese(selezione) + " " + EstraiAnno(selezione),
                TitledBorder.CENTER,
                TitledBorder.TOP));
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH; 
        c.weightx = 1.0;
        c.weighty = 1.0;
        pannello_tabelle.add(pannello_tabella_dati, c);
        
        pannello_tabella_compatibilità.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
                "Giorni di ferie e compatibilità medico-turno",
                TitledBorder.CENTER,
                TitledBorder.TOP));
        
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH; 
        c.weightx = 1.0;
        c.weighty = 1.0;
        pannello_tabelle.add(pannello_tabella_compatibilità, c);    
        
        pannello_tabella_soluzione.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
                "Soluzione consigliata",
                TitledBorder.CENTER,
                TitledBorder.TOP));
        
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH; 
        c.weightx = 1.0;
        c.weighty = 1.0;
        pannello_tabelle.add(pannello_tabella_soluzione, c); 
   
        
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,BoxLayout.LINE_AXIS));
        buttonPane.add(Salva);
        buttonPane.add(Box.createHorizontalStrut(15));
        buttonPane.add(Reset);
        buttonPane.add(Box.createHorizontalStrut(15));
        buttonPane.add(Risolvi);
        buttonPane.add(Box.createHorizontalStrut(15));
        buttonPane.add(SalvaSol);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(pannello_tabelle, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);  

        
	    Salva.addActionListener
	    (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                	salvaDati();
                }
            }
	    );  
	    
	    Reset.addActionListener
	    (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
               	    tabella_dati.ResetTable();
               	    tabella_compatibilità.ResetTable(FetchTable(path_quote + MesePrecedente(selezione)));
                    pannello_tabella_dati.setViewportView(tabella_dati.getTable());
                    pannello_tabella_compatibilità.setViewportView(tabella_compatibilità.getTable());
                }
            }
	    ); 	    

	    Risolvi.addActionListener
	    (
	        new ActionListener()
	        {
	            public void actionPerformed(ActionEvent ev)
	            {
	               	if (FileHandler.Esiste(path_archivio + MesePrecedente(selezione)))    
	               	{		          
	               		int selectedOption = JOptionPane.showConfirmDialog(null, 
	                            "Hai tempo da perdere?", 
	                            "Sono pronto", 
	                            JOptionPane.YES_NO_OPTION); 
	                    if (selectedOption == JOptionPane.YES_OPTION) abbiamo_tempo = true;
	                    else abbiamo_tempo = false;	                        
	               		
	                    SwingSolver solver = new SwingSolver();
	               		JDialog WIP = StoCalcolando(solver);

	                    solver.addPropertyChangeListener
	                    (
	                        new PropertyChangeListener() 
	                        {
	                            @Override
	                            public void propertyChange(PropertyChangeEvent pcEvt) 
	                            { 
	                                // siccome SwingWorker.StateValue è un enum, si può usare ==
	                                if (SwingWorker.StateValue.DONE == pcEvt.getNewValue()) 
	                                {
	                                	WIP.setVisible(false);	
	                                	try 
	                                	{
	                                		if (solver.get()) SalvaSol.setEnabled(true);
	                                		else JOptionPane.showMessageDialog(null,"Rifai il modello","NON RISOLVIBILE",JOptionPane.WARNING_MESSAGE);							    	    
										} 
	                                	catch (InterruptedException | ExecutionException e)
	                                	{
											e.printStackTrace();
										}
	                                }
	                            }
	                        }
	                    );
	                        
	                    solver.execute();
	                    WIP.setVisible(true);	   
	                    Risolvi.setEnabled(false);
	                }
	             	else
	               	{
	              	    JOptionPane.showMessageDialog(null,"Mancano i dati storici di " + MesePrecedente(selezione) + ".","IMPOSSIBILE CONTINUARE",JOptionPane.WARNING_MESSAGE);
	               	}	            		       	                	         	                  
	            }
	        }
	    ); 
	    
	    SalvaSol.addActionListener
	    (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                	salvaDati(); //altrimenti non sono coerenti con la soluzione ottenuta
               	    try 
                    {
            		    FileHandler.saveTable(tabella_soluzione.getTable(), new File(path_archivio + selezione));
	                    TabellaQuote quote = new TabellaQuote(ElencoMedici.LoadList(selezione));
	                    quote.CalcolaQuote(tabella_soluzione.getTable(), tabella_compatibilità.getTable());
	                    FileHandler.saveTable(quote.getTable(), new File(path_quote + selezione)); 
	                    
	                    JOptionPane.showMessageDialog(null,"File salvato nello storico");	                   
                    } 
             	    catch (IOException ex) 
                    {
                        System.out.println(ex.getMessage());
                        ex.printStackTrace();
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
		               @SuppressWarnings("unchecked")
			           JComboBox<String> cb = (JComboBox<String>)e.getSource();
		               selezione = (String)cb.getSelectedItem();
		               
		               pannello_tabella_dati.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
		                       "Pianificazione turni di " + EstraiMese(selezione) + " " + EstraiAnno(selezione),
		                       TitledBorder.CENTER,
		                       TitledBorder.TOP));
		               
	            	   MostraTabellaOutput();
		               
		               //se esiste esiste per entrambi, visto che li si salva sempre assieme
		               if (FileHandler.Esiste(path_dati + selezione))
		               {
		            	   MostraTabelleInput(FetchTable(path_dati + selezione), FetchTable(path_compatibilità + selezione));
		            	   MostraTabellaOutput();
		               }
		               else 
		               {
		           	       MostraTabelleInputVuote();
		               }

	            	   Risolvi.setEnabled(false);
	            	   SalvaSol.setEnabled(false);
	                }
	            }
		);    
	}	    
	
	private void salvaDati ()
	{
		if (FileHandler.Esiste(path_archivio + selezione))
    	{
    	    int selectedOption = JOptionPane.showConfirmDialog(null, 
                "Questo mese è già stato pianificato e salvato in archivio. \n "
                + "Modificando i dati verranno distrutti tutti i piani da " + EstraiMese(selezione) + " "  
                +  EstraiAnno(selezione) + " in poi. \n"
                + " Sei sicuro di voler proseguire?", 
                "ATTENZIONE", 
                JOptionPane.YES_NO_OPTION); 
            if (selectedOption == JOptionPane.YES_OPTION)
            {
                try 
                {	                      	                      
                    FileHandler.PulisciCartella(path_archivio, MesePrecedente(selezione));
                    FileHandler.PulisciCartella(path_soluzioni, MesePrecedente(selezione));
                    FileHandler.PulisciCartella(path_quote, MesePrecedente(selezione));		
                    
                    FileHandler.saveTable(tabella_dati.getTable(), new File(path_dati + selezione));  
         		    FileHandler.saveTable(tabella_compatibilità.getTable(), new File(path_compatibilità + selezione)); 
              
                    JOptionPane.showMessageDialog(null,"Salvataggio completato. L'archivio è stato modificato.");		                        
                }                    
                catch (IOException ex) 
                {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }    	
            }
    	}
 	
    	else
    	{
    	    try 
            { 
 		       FileHandler.saveTable(tabella_dati.getTable(), new File(path_dati + selezione));  
 		       FileHandler.saveTable(tabella_compatibilità.getTable(), new File(path_compatibilità + selezione)); 
      
               JOptionPane.showMessageDialog(null,"Salvataggio completato");
            } 
 	        catch (IOException ex) 
            {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }               	
        }
	}
	
	class SwingSolver extends SwingWorker<Boolean, Void> 
	{
	    @Override
		protected Boolean doInBackground() throws Exception 
	    {	
	        LpSolver Solutore = new LpSolver(tabella_dati.getTable(), tabella_compatibilità.getTable(), FetchTable(path_archivio + MesePrecedente(selezione)));
	        
	        //se stiamo ripianificando il presente
	        if (Calendario.MeseCorrente(EstraiMese(selezione), EstraiAnno(selezione)))
	    	{
	        	int giorno = Calendario.IndiceGiorno(EstraiMese(selezione), EstraiAnno(selezione));
	        	Solutore.Inizializza(path_soluzioni + selezione, giorno);
	    	}
	        Solutore.Risolvi(abbiamo_tempo);
	        
	        if (Solutore.isFeasible())  
    	    {
    		    Solutore.SalvaVariabili(path_soluzioni + selezione);
    		    tabella_soluzione = new TabellaOttimizzazione (ElencoMedici.LoadList(selezione), Calendario.ListaGiorni(EstraiMese(selezione), EstraiAnno(selezione)), Solutore.getSoluzione(tabella_soluzione.getTable()));
    		    pannello_tabella_soluzione.setViewportView(tabella_soluzione.getTable());
    	    }
		    return Solutore.isFeasible();
		}
    }
	
	
	private JDialog StoCalcolando (SwingSolver solver)
	{
		final JDialog Dialog = new JDialog();
        JPanel pannello = new JPanel(); 
        pannello.setLayout(new BorderLayout());
        JLabel Hello = new JLabel("Sto ottimizzando...");
        if (!abbiamo_tempo) Hello = new JLabel("Sto calcolando...");
        Hello.setHorizontalAlignment(JLabel.CENTER);
        pannello.add(Hello, BorderLayout.CENTER);
                 
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        pannello.add(progressBar, BorderLayout.PAGE_END);
        
        /*JButton stopButton = new JButton("Annulla");
        pannello.add(stopButton, BorderLayout.PAGE_END);
        pannello.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        stopButton.addActionListener
	    (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    solver.cancel(true);
                }

        });*/

        Dialog.add(pannello);
        Dialog.setSize(new Dimension(200, 150));
        Dialog.setResizable(false);
        Dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        Dialog.setLocationRelativeTo(null);
        return Dialog;
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
	private void MostraTabelleInput (JTable nuovi_dati, JTable nuove_compatibilità)
	{	    		      	    	    		        
 		DefaultListModel<String> listModel = new DefaultListModel<String>();
		   
 	    for (int i = 0; i < nuovi_dati.getRowCount(); i++)
 	    	listModel.addElement(nuovi_dati.getValueAt(i,0).toString());
 	        
 	    String mese = EstraiMese(selezione);
 	    int anno = EstraiAnno(selezione);
 	    int giorno = -1;

 	    if (Calendario.MeseCorrente(mese, anno))
 	    {
 	    	giorno = Calendario.IndiceGiorno(mese, anno);	
 	    }
 	    
 	    tabella_dati = new TabellaDati (new JList<String>(listModel), Calendario.ListaGiorni(mese, anno), giorno, nuovi_dati);	
 	    tabella_compatibilità = new TabellaCompatibilità (new JList<String> (listModel), nuove_compatibilità);		        
 	    
        pannello_tabella_dati.setViewportView(tabella_dati.getTable());
        pannello_tabella_compatibilità.setViewportView(tabella_compatibilità.getTable());	
	}
	
	private void MostraTabellaOutput ()
	{	    		     		    	    		        	        	             		 
 	    String mese = EstraiMese(selezione);
 	    int anno = EstraiAnno(selezione);
 	    int giorno = -1;

 	    if (Calendario.MeseCorrente(mese, anno))
 	    {
 	    	giorno = Calendario.IndiceGiorno(mese, anno);	
 	    }
 			        
	    tabella_soluzione = new TabellaOttimizzazione (ElencoMedici.LoadList(selezione), Calendario.ListaGiorni(EstraiMese(selezione), EstraiAnno(selezione)));
	     
 	    if (Calendario.MeseCorrente(mese, anno))
 	    {
 	    	 tabella_soluzione.CaricaStorico(FetchTable(path_archivio + selezione), giorno);	
 	    }
 		
 	    pannello_tabella_soluzione.setViewportView(tabella_soluzione.getTable());			
	}
	
	//mette a zero tutto l'input
	private void MostraTabelleInputVuote()	
	{      
	    tabella_dati = new TabellaDati (ElencoMedici.LoadList(selezione), Calendario.ListaGiorni(EstraiMese(selezione), EstraiAnno(selezione)), -1);
	    tabella_compatibilità = new TabellaCompatibilità (ElencoMedici.LoadList(selezione));	

		if (FileHandler.Esiste(path_quote + MesePrecedente(selezione)))    
       	{
	        tabella_compatibilità.ResetTable(FetchTable(path_quote + MesePrecedente(selezione)));
       	}
     	else
       	{
      	    JOptionPane.showMessageDialog(null,"Il bilancio consuntivo di " + MesePrecedente(selezione) + " non è disponibile.","ATTENZIONE",JOptionPane.WARNING_MESSAGE);
       	}	
        pannello_tabella_dati.setViewportView(tabella_dati.getTable());
        pannello_tabella_compatibilità.setViewportView(tabella_compatibilità.getTable());	
	}	

	private String EstraiMese(String selezione)
	{
		return (selezione.substring(selezione.indexOf('_')+1, selezione.indexOf(' ')));
	}
	
	private int EstraiAnno(String selezione)
	{
		return (Integer.parseInt(selezione.substring(selezione.indexOf(' ')+1, selezione.indexOf('.'))));	
	}
	
	private String MesePrecedente(String selezione)
	{
		String risposta;
		
		int indice = Integer.parseInt(selezione.substring(0, selezione.indexOf('_')));
		String tmp = Calendario.getMesePrecedente(EstraiMese(selezione));
    indice--;
		if (tmp.equals("Dicembre")) 
		{
			int anno = EstraiAnno(selezione);
			anno--;
			risposta = indice + "_" + tmp + " " + anno + ".txt";
		}
		else risposta = indice + "_" + tmp + " " + EstraiAnno(selezione) + ".txt";
		return risposta;
	}
}
