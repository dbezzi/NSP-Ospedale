/** Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * TabellaQuote.java non richiede altri file.
 */

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.table.DefaultTableModel;

public class TabellaQuote 
{	     
    private String[] columnNames = {"Medico", "Ferie", "Bonus residui", "Presenze",
    		                        "Quota R", "R svolti", 
    		                        "Quota M", "M svolti", 
    		                        "Quota P", "P svolti",
    		                        "Quota N", "N svolti",
    		                        "Quota MP", "MP svolti",
    		                        "Quota WE", "WE svolti"};
    private String[][] data = {};
    
    private DefaultListModel<String> listModelRighe;
    
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
    
    public TabellaQuote (JList<String> medici) 
    {       
        Costruttore(medici);
        
        for (int i = 0; i < table.getRowCount(); i++) 
            for (int j = 1; j < table.getColumnCount(); j++) 
                table.setValueAt("", i, j); 
    }
    
    public TabellaQuote (JList<String> medici, JTable quote) 
    {       
        Costruttore(medici);
        
        for (int i = 0; i < table.getRowCount(); i++) 
            for (int j = 1; j < table.getColumnCount(); j++) 
                table.setValueAt(quote.getValueAt(i, j), i, j);  
    }
    
    private void Costruttore (JList<String> medici)
    {
        listModelRighe = (DefaultListModel<String>) medici.getModel();
        
        for (int i = 0; i < listModelRighe.getSize(); i++)
        	this.AggiungiRiga(listModelRighe.getElementAt(i), model);
        
        table.setModel(model);     
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
        
        for (int i = 0; i < table.getColumnCount(); i++) 
            table.getColumnModel().getColumn(i).setPreferredWidth(100); 
	}
       
    private void AggiungiRiga(String medico, DefaultTableModel model)
    {
        model.addRow(new Object[]{medico, null});
    }
    
    public void CalcolaQuote (JTable piano, JTable compatibilità)
    { 	
    	int giorni = piano.getColumnCount();
    	int[] indici = AbbinaRighe(table, compatibilità);
    	int medici = table.getRowCount();
    	for (int m = 0; m < medici; m++)
 	    { 
 	        //calcolo delle ferie utilizzate
 	       	int ferie_residue = 0;	 
 	       	
            if (indici[m] != -1)
            	for (int j = 1; j < table.getColumnCount(); j++) 
            		ferie_residue = Integer.parseInt(compatibilità.getValueAt(indici[m],1).toString());	
 	       	
 	        for (int g = 1; g < giorni; g++)
 	        	if (piano.getValueAt(m,g).toString().equals("Ferie")) ferie_residue--;
 	        
 	        table.setValueAt(ferie_residue, m, 1);
 	        
 	        //calcolo dei bonus assegnati
 	       	int bonus_residui = 0;
 	       	
            if (indici[m] != -1)
            	for (int j = 1; j < table.getColumnCount(); j++) 
            		bonus_residui = Integer.parseInt(compatibilità.getValueAt(indici[m],2).toString());	
            
 	       	int riposi;
 	        for (int sett = 1; sett < giorni; sett = sett + 7)
 	        {
 	        	riposi = 0;
 	            for (int g = sett; g < sett+7; g++)
 	            {
 	        	    if (piano.getValueAt(m,g).toString().equals("-")) riposi++;
 	        	    if (piano.getValueAt(m,g).toString().equals("Rs") || piano.getValueAt(m,g).toString().equals("Rns")) bonus_residui--;
 	            }
 	            if (riposi < 2) bonus_residui++;
 	        }
 	        table.setValueAt(bonus_residui, m, 2); 	       
 	    }  	
    	
 	    //-------- CONTEGGIO PRESENZE E TURNI DA SVOLGERE --------//
        
        int tot_R = 0;    //totale R (tutti i turni M1 ma solo di martedì, giovedì, sabato e domenica)
        int tot_M = 0;    //totale M (tutti i turni M1, M2 e MP)
        int tot_P = 0;    //totale P (tutti i turni P e MP)
        int tot_N = 0;    //totale N (tutti i turni N)
        int tot_MP = 0;   //totale MP (tutti i turni MP)
        int tot_WE = 0;   //totale WE (tutti i turni ma solo di sabato e domenica)    
        
        int R[] = new int[medici];    //R[i] indica il numero di reperibiltà effettivamente svolte dal medico i
        int M[] = new int[medici];    //M[i] indica il numero di mattine effettivamente svolte dal medico i
        int P[] = new int[medici];    //P[i] indica il numero di pomeriggi effettivamente svolte dal medico i
        int N[] = new int[medici];    //N[i] indica il numero di turni N effettivamente svolti dal medico i
        int MP[] = new int[medici];   //MP[i] indica il numero di turni MP effettivamente svolti dal medico i
        int WE[] = new int[medici];   //WE[i] indica il numero di turni svolti dal medico i di sabato e domenica  
        
        int presenze[] = new int[medici];     
        int tot_presenze = 0;
        
        for (int m = 0; m < medici; m++)
 	    { 
            R[m] = 0;   
            M[m] = 0;  
            P[m] = 0;  
            N[m] = 0;  
            MP[m] = 0;  
            WE[m] = 0;  
            
            presenze[m] = 0;
            
	        for (int g = 1; g < giorni; g++)
	        {
 	        	if (piano.getValueAt(m,g).toString().equals("M1"))
 	        	{
 	        		presenze[m]++;
 	        		tot_M++; M[m]++;
 	        		if (g%7 == 2 || g%7 == 4) {tot_R++; R[m]++;}
 	        		if (g%7 == 6 || g%7 == 0) {tot_R++; tot_WE++; R[m]++; WE[m]++;}
 	        	}
 	        	if (piano.getValueAt(m,g).toString().equals("M2")) 
 	        	{
 	        		presenze[m]++;
 	        		tot_M++; M[m]++;
 	        		if (g%7 == 6 || g%7 == 0) {tot_WE++; WE[m]++;}
 	        	}	
 	        	if (piano.getValueAt(m,g).toString().equals("MP")) 
 	        	{
 	        		presenze[m]++;
 	        		tot_M++; M[m]++;
 	        		tot_MP++; MP[m]++;
 	        		tot_P++; P[m]++;
 	        		if (g%7 == 6 || g%7 == 0) {tot_WE++; WE[m]++;}
 	        	}	
 	        	if (piano.getValueAt(m,g).toString().equals("P"))
 	        	{
 	        		presenze[m]++;
 	        		tot_P++; P[m]++;
 	        		if (g%7 == 6 || g%7 == 0) {tot_WE++; WE[m]++;}
 	        	}
 	        	if (piano.getValueAt(m,g).toString().equals("N")) 
 	        	{
 	        		presenze[m]++;
 	        		tot_N++; N[m]++;
 	        		if (g%7 == 6 || g%7 == 0) {tot_WE++; WE[m]++;}
 	        	}
	        }	        
 	        table.setValueAt(presenze[m], m, 3);
 	        tot_presenze += presenze[m];
 	    }
        
        //-------- CALCOLO QUOTE (TEORICHE VS REALI) --------//
        
        int tot_presenze_R = tot_presenze;    //totale delle presenze di tutti i medici compatibili con i turni R
        int tot_presenze_M = tot_presenze;    //totale delle presenze di tutti i medici compatibili con i turni M
        int tot_presenze_P = tot_presenze;    //totale delle presenze di tutti i medici compatibili con i turni P
        int tot_presenze_N = tot_presenze;    //totale delle presenze di tutti i medici compatibili con i turni N
        int tot_presenze_MP = tot_presenze;   //totale delle presenze di tutti i medici compatibili con i turni MP
        int tot_presenze_WE = tot_presenze;   //totale delle presenze di tutti i medici compatibili con i turni WE      
        
        for (int m = 0; m < medici; m++)
 	    {	    
        	if (Boolean.parseBoolean(compatibilità.getValueAt(indici[m], 3).toString()) == false) 
    	    {
        		table.setValueAt(-1, m, 4);
        		tot_presenze_R -= presenze[m];
    	    }
    	    if (Boolean.parseBoolean(compatibilità.getValueAt(indici[m], 3).toString()) == false)
        	if (Boolean.parseBoolean(compatibilità.getValueAt(indici[m], 4).toString()) == false)
        	if (Boolean.parseBoolean(compatibilità.getValueAt(indici[m], 5).toString()) == false)  
    	    {
        		table.setValueAt(-1, m, 6);
        		tot_presenze_M -= presenze[m];
    	    }
	        if (Boolean.parseBoolean(compatibilità.getValueAt(indici[m], 5).toString()) == false)
		    if (Boolean.parseBoolean(compatibilità.getValueAt(indici[m], 6).toString()) == false) 
	        {
        		table.setValueAt(-1, m, 8);
        		tot_presenze_P -= presenze[m];
    	    }
        	if (Boolean.parseBoolean(compatibilità.getValueAt(indici[m], 7).toString()) == false) 
    	    {
        		table.setValueAt(-1, m, 10);
        		tot_presenze_N -= presenze[m];
    	    }
        	if (Boolean.parseBoolean(compatibilità.getValueAt(indici[m], 5).toString()) == false) 
    	    {
        		table.setValueAt(-1, m, 12);
        		tot_presenze_MP -= presenze[m];
    	    }
        	//per il week-end non la facciamo: se un medico non fa mai turni nel WE significa
        	//che non fa nessun turno in generale (quindi si fa prima a toglierlo dal modello)
 	    }
     
        for (int m = 0; m < medici; m++)
 	    {           	    
    	    //calcolo della quota R teorica (vale 0 se il medico m non fa i turni M1)    
        	if (table.getValueAt(m, 4).toString().equals("-1"))  table.setValueAt(0, m, 4);
        	else table.setValueAt(String.format("%.2f", (double) presenze[m] * tot_R / tot_presenze_R), m, 4);     	        
	        table.setValueAt(R[m], m, 5);    //calcolo della quota R reale
	        
    	    //calcolo della quota M teorica (vale 0 se il medico m non fa nè i turni M1, nè M2, nè MP)    
        	if (table.getValueAt(m, 6).toString().equals("-1"))  table.setValueAt(0, m, 6);
        	else table.setValueAt(String.format("%.2f", (double) presenze[m] * tot_M / tot_presenze_M), m, 6);     	        
	        table.setValueAt(M[m], m, 7);    //calcolo della quota M reale
	        
    	    //calcolo della quota P teorica (vale 0 se il medico m non fa nè i turni P nè i turni MP)    
        	if (table.getValueAt(m, 8).toString().equals("-1"))  table.setValueAt(0, m, 8);
        	else table.setValueAt(String.format("%.2f", (double) presenze[m] * tot_P / tot_presenze_P), m, 8);     	        
	        table.setValueAt(P[m], m, 9);    //calcolo della quota P reale
	       
    	    //calcolo della quota N teorica (vale 0 se il medico m non fa i turni N)    
        	if (table.getValueAt(m, 10).toString().equals("-1"))  table.setValueAt(0, m, 10);
        	else table.setValueAt(String.format("%.2f", (double) presenze[m] * tot_N / tot_presenze_N), m, 10);     	        
	        table.setValueAt(N[m], m, 11);    //calcolo della quota N reale
	       
    	    //calcolo della quota MP teorica (vale 0 se il medico m non fa i turni MP)    
        	if (table.getValueAt(m, 12).toString().equals("-1"))  table.setValueAt(0, m, 12);
        	else table.setValueAt(String.format("%.2f", (double) presenze[m] * tot_MP / tot_presenze_MP), m, 12);     	        
	        table.setValueAt(MP[m], m, 13);    //calcolo della quota MP reale
	        
    	    //calcolo della quota WE teorica (vale 0 solo se il medico m non lavora mai)    
            table.setValueAt(String.format("%.2f", (double) presenze[m] * tot_WE / tot_presenze_WE), m, 14);     	        
	        table.setValueAt(WE[m], m, 15);    //calcolo della quota WE reale	       
 	    }
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
