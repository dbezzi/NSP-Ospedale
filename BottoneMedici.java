/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * BottoneMedici.java richiede:
 * - Path.java
 * - Calendario.java
 * - FileHandler.java
 * - ElencoMedici.java
 */

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
public class BottoneMedici extends JPanel implements ListSelectionListener 
{	
	private String path_medici = Path.getPathMedici();
    private String path_dati = Path.getPathDati();
    private String path_compatibilità = Path.getPathCompatibilità();
    private String path_archivio = Path.getPathPiani();
    private String path_quote = Path.getPathQuote();
    private String path_soluzioni = Path.getPathSoluzioni();
    
    private static final String hireString = "Aggiungi";
    private static final String fireString = "Elimina";
    private JButton fireButton;
    private JTextField nome_medico;
	private JList<String> list;
    private DefaultListModel<String> listModel;
 
    private JList<String> lista_mesi = FileHandler.ListaCartella(path_medici);
    private int dimensione_lista = 0;
    private String selezione;
    
    public BottoneMedici() 
    {
        super(new BorderLayout());
        
        JComboBox<String> menu = new JComboBox<String>();
        DefaultComboBoxModel<String> listModel_mesi = new DefaultComboBoxModel<String>();
        
        boolean stop = false;
        for (int i = 0; i < lista_mesi.getModel().getSize(); i++)
        {
        	String tmp = lista_mesi.getModel().getElementAt(i); //listModel_mesi.addElement(tmp); dimensione_lista++;
        	if (!stop) stop =  Calendario.MeseCorrente(EstraiMese(tmp), EstraiAnno(tmp));
            else {listModel_mesi.addElement(tmp); dimensione_lista++;}   	
        }
        String tmp = Calendario.prossimoMese(listModel_mesi.getElementAt(dimensione_lista-1));
        listModel_mesi.addElement(lista_mesi.getModel().getSize() + "_" + tmp + ".txt");

        selezione = listModel_mesi.getElementAt(0);
        
        menu.setModel(listModel_mesi);
        menu.setSelectedItem(selezione);
        
        add(menu, BorderLayout.PAGE_START);
        
		list = ElencoMedici.LoadList(selezione);       
        listModel = (DefaultListModel<String>) list.getModel();
 
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);
 
        JButton hireButton = new JButton(hireString);
        HireListener hireListener = new HireListener(hireButton);
        hireButton.setActionCommand(hireString);
        hireButton.addActionListener(hireListener);
        hireButton.setEnabled(false);
 
        fireButton = new JButton(fireString);
        fireButton.setActionCommand(fireString);
        fireButton.addActionListener(new FireListener());
 
        nome_medico = new JTextField(10);
        nome_medico.addActionListener(hireListener);
        nome_medico.getDocument().addDocumentListener(hireListener);
 
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(fireButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(nome_medico);
        buttonPane.add(hireButton);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 
        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
     
        menu.addActionListener 
        (
    	       new ActionListener()
               {
                   public void actionPerformed(ActionEvent e)
                   {
    	               @SuppressWarnings("unchecked")
    		           JComboBox<String> cb = (JComboBox<String>)e.getSource();
    	               selezione = (String)cb.getSelectedItem();		        	              
    	               
    	               if (FileHandler.Esiste(path_medici + selezione))		               
    	                   listModel = (DefaultListModel<String>) ElencoMedici.LoadList(selezione).getModel();
		               
    	               else listModel.clear();
                   }
               }
    	 ); 
    }
    
    class FireListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {
        	if (FileHandler.Esiste(path_dati + selezione))
        	{
        	    int selectedOption = JOptionPane.showConfirmDialog(null, 
                    "Esistono già i dati per questo mese. \n "
                    + "Modificando la lista medici verranno distrutti tutti i dati da " + EstraiMese(selezione) + " "  
                    +  EstraiAnno(selezione) + " in poi. \n"
                    + " Sei sicuro di voler proseguire?", 
                    "ATTENZIONE", 
                    JOptionPane.YES_NO_OPTION); 
                if (selectedOption == JOptionPane.YES_OPTION)
                {        
                	//rimuovi l'elemento dalla lista
                	rimuoviElementoSelezionato();
                    
                    try 
                    {
						FileHandler.PulisciCartella(path_soluzioni, MesePrecedente(selezione));					
                        FileHandler.PulisciCartella(path_archivio, MesePrecedente(selezione));
                        FileHandler.PulisciCartella(path_dati, MesePrecedente(selezione));
                        FileHandler.PulisciCartella(path_compatibilità, MesePrecedente(selezione));
                        FileHandler.PulisciCartella(path_quote, MesePrecedente(selezione));	
                    }
                    catch (IOException e1) 
                    {
						e1.printStackTrace();
					}
                }
        	}
        	else rimuoviElementoSelezionato();
        }
        
        private void rimuoviElementoSelezionato()
        {
        	int index = list.getSelectedIndex();
            listModel.remove(index);

            int size = listModel.getSize();

            if (size == 0)  
            {
                fireButton.setEnabled(false);
            }
            else             
            {
                if (index == listModel.getSize()) index--;
        
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
            ElencoMedici.WriteList(list, selezione);	
        }
    }

    class HireListener implements ActionListener, DocumentListener 
    {
        private boolean alreadyEnabled = false;
        private JButton button;
 
        public HireListener(JButton button) 
        {
            this.button = button;
        }
 
        //Required by ActionListener.
        public void actionPerformed(ActionEvent e) 
        {
            String nome = nome_medico.getText();
            
            if (FileHandler.Esiste(path_dati + selezione))
        	{
        	    int selectedOption = JOptionPane.showConfirmDialog(null, 
                    "Esistono già i dati per questo mese. \n "
                    + "Modificando la lista medici verranno distrutti tutti i dati da " + EstraiMese(selezione) + " "  
                    +  EstraiAnno(selezione) + " in poi. \n"
                    + " Sei sicuro di voler proseguire?", 
                    "ATTENZIONE", 
                    JOptionPane.YES_NO_OPTION); 
                if (selectedOption == JOptionPane.YES_OPTION)
                {        
                	//aggiungi quel medico alla lista
                	aggiungiElementoSelezionato(nome);
                    
                    try 
                    {
						FileHandler.PulisciCartella(path_soluzioni, MesePrecedente(selezione));					
                        FileHandler.PulisciCartella(path_archivio, MesePrecedente(selezione));
                        FileHandler.PulisciCartella(path_dati, MesePrecedente(selezione));
                        FileHandler.PulisciCartella(path_compatibilità, MesePrecedente(selezione));
                        FileHandler.PulisciCartella(path_quote, MesePrecedente(selezione));	
                    }
                    catch (IOException e1) 
                    {
						e1.printStackTrace();
					}
                }
        	}
            else aggiungiElementoSelezionato (nome);
        }
        
        private void aggiungiElementoSelezionato (String nome)
        {
            if (nome.equals("") || alreadyInList(nome)) 
            {
                nome_medico.requestFocusInWindow();
                nome_medico.selectAll();
                return;
            }
 
            int index = list.getSelectedIndex() + 1; 
            listModel.add(index, nome_medico.getText());
 
            //Sbianca il campo testuale
            nome_medico.requestFocusInWindow();
            nome_medico.setText("");
 
            //Seleziona il nuovo nome e lo rende visibile
            list.setSelectedIndex(index);
            list.ensureIndexIsVisible(index);
            
            ElencoMedici.WriteList(list, selezione);
        }
        
        //Questo metodo testa l'uguaglianza delle stringhe.
        protected boolean alreadyInList(String name) 
        {
            return listModel.contains(name);
        }
 
        //Required by DocumentListener.
        public void insertUpdate(DocumentEvent e) 
        {
            enableButton();
        }
 
        //Richiesto da DocumentListener.
        public void removeUpdate(DocumentEvent e) 
        {
            handleEmptyTextField(e);
        }
 
        //Richiesto da DocumentListener.
        public void changedUpdate(DocumentEvent e) 
        {
            if (!handleEmptyTextField(e)) enableButton();           
        }
 
        private void enableButton() 
        {
            if (!alreadyEnabled) button.setEnabled(true);           
        }
 
        private boolean handleEmptyTextField(DocumentEvent e) 
        {
            if (e.getDocument().getLength() <= 0) 
            {
                button.setEnabled(false);
                alreadyEnabled = false;
                return true;
            }
            return false;
        }
    }
 
    //Richiesto da ListSelectionListener.
    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting() == false) 
        {
            if (list.getSelectedIndex() == -1) 
            {
            //Nessuna selezione, non si può cancellare nulla
                fireButton.setEnabled(false);               
            }
            else 
            {
            //Selezione, abilita il bottone di cancellazione
                fireButton.setEnabled(true);
            }
        }
    }
    
	private String EstraiMese (String selezione)
	{
		return (selezione.substring(selezione.indexOf('_')+1, selezione.indexOf(' ')));
	}
	
	private int EstraiAnno (String selezione)
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


