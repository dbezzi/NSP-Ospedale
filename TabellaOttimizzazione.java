/** Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * TabellaOttimizzazione.java non richiede altri file.
 */

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.table.DefaultTableModel;

/**serve solo per LpSolve, non viene mai mostrata a video*/
public class TabellaOttimizzazione 
{	     
    private String[] columnNames = {};
    private String[][] data = {};
    
    private DefaultListModel<String> listModelRighe;
    private DefaultListModel<String> listModelColonne;
    
    private JTable table = new JTable();
    
    @SuppressWarnings("serial")
	DefaultTableModel model = new DefaultTableModel(data, columnNames) 
    {
    	@Override
    	public boolean isCellEditable(int row, int column) 
    	{
            return false;
    	}
    };
    
    public TabellaOttimizzazione (JList<String> medici, JList<String> giorni) 
    {       
    	Costruttore (medici, giorni);     
        
        this.ResetTable();
    }
    
    public TabellaOttimizzazione (JList<String> medici, JList<String> giorni, JTable piano) 
    {       
    	Costruttore (medici, giorni);     
        
        for (int i = 0; i < table.getRowCount(); i++) 
            for (int j = 0; j < table.getColumnCount(); j++)  
                table.setValueAt(piano.getValueAt(i, j), i, j);
    }
    
    private void Costruttore (JList<String> medici, JList<String> giorni)
    {
        listModelRighe = (DefaultListModel<String>) medici.getModel();
        listModelColonne = (DefaultListModel<String>) giorni.getModel();
        
        this.AggiungiColonna("Medico", model);
        for (int i = 0; i < listModelColonne.getSize(); i++)
        	this.AggiungiColonna(listModelColonne.getElementAt(i), model);        
        
        for (int i = 0; i < listModelRighe.getSize(); i++)
        	this.AggiungiRiga(listModelRighe.getElementAt(i), model);
        
        table.setModel(model);
             
        this.ImpostaGrafica();  
    }
       
    public JTable getTable() 
    {
        return this.table;
    }  

    //dove la tabella non è più editabile (ripianificazione) va sovrascritta coi dati storici che già ci sono
    //qui non serve il controllo sulle righe perchè i medici sono per definizione gli stessi
    public void CaricaStorico(JTable input, int edge) 
    {
        for (int i = 0; i < table.getRowCount(); i++) 
            for (int j = 0; j < edge; j++) 
                table.setValueAt(input.getValueAt(i, j), i, j);
    } 
    
    public void ResetTable() 
    {
        for (int i = 0; i < table.getRowCount(); i++) 
        	for (int j = 1; j < table.getColumnCount(); j++)  
                table.setValueAt("", i, j);
    } 
    
	private void ImpostaGrafica ()
	{
		table.setPreferredScrollableViewportSize(new Dimension(800, 500));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        
        for (int i = 1; i < table.getColumnCount(); i++) 
            table.getColumnModel().getColumn(i).setPreferredWidth(48); 
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
	}
    
    private void AggiungiRiga(String medico, DefaultTableModel model)
    {
        model.addRow(new Object[]{medico, null});
    }
    
    private void AggiungiColonna(String header, DefaultTableModel model)
    {
        model.addColumn(header);
    }
}
