/** Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * TabellaDati.java non richiede altri file.
 */
 
import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class TabellaDati 
{	     
    private String[] columnNames = {};
    private String[][] data = {};
    
    private int edge;
    
    private DefaultListModel<String> listModelRighe;
    private DefaultListModel<String> listModelColonne;
    
    private JTable table = new JTable();
    
    @SuppressWarnings("serial")
	DefaultTableModel model = new DefaultTableModel(data, columnNames) 
    {
    	@Override
    	public boolean isCellEditable(int row, int column) 
    	{
    		if (column == 0) return false;   //i medici non sono mai editabili   
            if (column < edge) return false;   //non è possibile ripianificare il passato
            else return true;
    	}
    };
    
    public TabellaDati (JList<String> medici, JList<String> giorni, int giorno_corrente) 
    {        	
    	edge = giorno_corrente;
    	Costruttore (medici, giorni);
        
        this.ResetTable();
    }
    
    public TabellaDati (JList<String> medici, JList<String> giorni, int giorno_corrente, JTable dati) 
    {       
    	edge = giorno_corrente;
    	Costruttore (medici, giorni);
        
        for (int i = 0; i < table.getRowCount(); i++) 
            for (int j = 1; j < table.getColumnCount(); j++) 
                table.setValueAt(dati.getValueAt(i, j), i, j);
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
        
        for (int i = 1; i < table.getColumnCount(); i++) 
            setUpMenu(table.getColumnModel().getColumn(i));
        
        this.ImpostaGrafica();  
    }
    
    public JTable getTable() 
    {
        return this.table;
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
    
    public void ResetTable() 
    {
        for (int i = 0; i < table.getRowCount(); i++) 
            for (int j = Math.max(edge, 1); j < table.getColumnCount(); j++) 
                table.setValueAt("", i, j);
    } 
    
    private void AggiungiRiga(String medico, DefaultTableModel model)
    {
        model.addRow(new Object[]{medico, null});
    }
    
    private void AggiungiColonna(String header, DefaultTableModel model)
    {
        model.addColumn(header);
    }
    
    private void setUpMenu(TableColumn colonna)
    {
        JComboBox<String> comboBox = new JComboBox<String>();
        
        comboBox.addItem("AP");         //assenza programmata
        //comboBox.addItem("Ferie");
        comboBox.addItem("E");          //extra
        comboBox.addItem("");           //normale
        comboBox.addItem("M1");
        comboBox.addItem("M2");
        comboBox.addItem("MP");
        comboBox.addItem("P");
        comboBox.addItem("N");

        colonna.setCellEditor(new DefaultCellEditor(comboBox));
 
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("Seleziona un tipo di turno");
        colonna.setCellRenderer(renderer);
    }
}
