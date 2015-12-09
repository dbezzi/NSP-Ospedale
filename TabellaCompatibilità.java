/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * TabellaCompatibilità.java non richiede altri file.
 */

import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;


public class TabellaCompatibilità 
{	
	private String[] columnNames = {"Medico", "Ferie", "Bonus residui", "Turno M1", "Turno M2", "Turno MP", "Turno P", "Turno N"};
    private Object[][] data = {};
    
    private DefaultListModel<String> listModel;
    
    private JTable table = new JTable();
    
    @SuppressWarnings("serial")
	DefaultTableModel model = new DefaultTableModel(data, columnNames) 
    {
    	@Override
    	public boolean isCellEditable(int row, int column) 
    	{
            if (column == 0) return false;
            if (column == 2) return false;
            return true;
    	}
    	         
		@Override
        public Class<?> getColumnClass(int column)
        {
            if (column <= 2)  return String.class;
            else return Boolean.class;                
        }
    };
    
    public TabellaCompatibilità (JList<String> medici) 
    {
    	Costruttore (medici); 
    }
    
    public TabellaCompatibilità (JList<String> medici, JTable compatibilità) 
    {
    	Costruttore (medici);
    
    	int[] indici = AbbinaRighe(table, compatibilità);
        for (int i = 0; i < table.getRowCount(); i++) 
        {
            if (indici[i] != -1)
            {
            	table.setValueAt(compatibilità.getValueAt(indici[i], 1).toString(), i, 1); 
            	table.setValueAt(compatibilità.getValueAt(indici[i], 2).toString(), i, 2); 
            	for (int j = 3; j < table.getColumnCount(); j++) 
                    table.setValueAt(Boolean.parseBoolean(compatibilità.getValueAt(indici[i], j).toString()), i, j);
            }
        }
    }
    
    private void Costruttore (JList<String> medici) 
    {       
        listModel = (DefaultListModel<String>) medici.getModel();

        for (int i = 0; i < listModel.getSize(); i++)
        	this.AggiungiRiga(listModel.getElementAt(i).toString(), model);
        
        table.setModel(model);

        setUpMenuFerie(table.getColumnModel().getColumn(1));
        
        this.ImpostaGrafica();
    }
    
	private void ImpostaGrafica ()
	{
		table.setPreferredScrollableViewportSize(new Dimension(800, 500));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        
        for (int i = 0; i < table.getColumnCount(); i++) 
            table.getColumnModel().getColumn(i).setPreferredWidth(100); 
	}
    
    public JTable getTable() 
    {
        return this.table;
    }  

    public void ResetTable(JTable quote_mese_precedente) 
    {
    	InizializzaFerie(quote_mese_precedente);
    	InizializzaBonus(quote_mese_precedente);
        for (int i = 0; i < table.getRowCount(); i++) 
        {
            for (int j = 3; j < table.getColumnCount(); j++) 
                table.setValueAt(true, i, j);            
        }
    } 

    private void AggiungiRiga(String medico, DefaultTableModel model)
    {
        model.addRow(new Object[]{medico, null});
    }
    
    public int getColumnCount() 
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        return data.length;
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col)
    {
        return data[row][col];
    }
    
    public void InizializzaFerie (JTable ferie)
    {  	   	
    	int[] indici = AbbinaRighe(table, ferie);
        for (int m = 0; m < table.getRowCount(); m++) 
        {
        	if (indici[m] != -1)            
            	table.setValueAt(ferie.getValueAt(indici[m], 1), m, 1); 

            else table.setValueAt(0, m, 1);         
        }
    }
    
    public void InizializzaBonus (JTable bonus)
    {
    	int[] indici = AbbinaRighe(table, bonus);
        for (int m = 0; m < table.getRowCount(); m++) 
        {
            if (indici[m] != -1)
           		table.setValueAt(bonus.getValueAt(indici[m], 2), m, 2);  

            else table.setValueAt(0, m, 2);         
        }			
    }
    
    private void setUpMenuFerie(TableColumn colonna)
    {
        JComboBox<String> comboBox = new JComboBox<String>();
        
        for (int i = 0; i <= 50; i++)    
            comboBox.addItem(i + "");         //assenza programmata

        colonna.setCellEditor(new DefaultCellEditor(comboBox));
 
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("Quanti giorni di ferie fa?");
        colonna.setCellRenderer(renderer);      
    }
    
    private int[] AbbinaRighe (JTable tabella1, JTable tabella2)
    {
    	int medici_1 = tabella1.getRowCount();
    	int medici_2 = tabella2.getRowCount();
    	int[] indici = new int[medici_1];
    	for (int i = 0; i < medici_1; i++)
    	{
    		indici[i] = -1; //non c'è più il medico
    		for (int j = 0; j < medici_2; j++)
    		    if (tabella1.getValueAt(i, 0).equals(tabella2.getValueAt(j,0))) indici[i] = j;
    	}
    	return indici;
    }
}


