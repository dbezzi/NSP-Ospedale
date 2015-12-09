/** Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * TabellaPiano.java non richiede altri file.
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

public class TabellaPiano
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
            if (column == 0) return false;
            return true;
    	}
    	
    };
    
    //crea una tabella già inizializzata
    public TabellaPiano (JList<String> medici, JList<String> giorni, JTable piano) 
    {       
        Costruttore (medici, giorni);
                
        for (int i = 0; i < table.getRowCount(); i++) 
            for (int j = 1; j < table.getColumnCount(); j++) 
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

        comboBox.addItem("Ferie");     //ferie
        comboBox.addItem("Rs");     //recupero in giorno scelto
        comboBox.addItem("Rns");     //recupero in giorno non scelto
        comboBox.addItem("Malus");     //malus
        comboBox.addItem("-");      //riposo
        comboBox.addItem("E");     //extra
        comboBox.addItem("M1");
        comboBox.addItem("M2");
        comboBox.addItem("MP");
        comboBox.addItem("P");
        comboBox.addItem("N");
        comboBox.addItem("");      //prosieguo notte

        colonna.setCellEditor(new DefaultCellEditor(comboBox));
 
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("ATTENZIONE: Stai per modificare l'archivio reale");
        colonna.setCellRenderer(renderer);
    }
}

