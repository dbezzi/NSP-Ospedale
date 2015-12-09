/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * LpSolver.java richiede:
 * - FileHandler.java
 * - Modellatore.java
 * - lpsolve55.jar (wrapper di lpsolve55.dll)
 */

import javax.swing.JTable;
import java.io.File;
import java.io.IOException;

import lpsolve.*;

public class LpSolver
{
    private JTable dati;
   
    private Modellatore Problema;
    private LpSolve lp;
	private double[] obiettivi = new double[5];
    private int n_obiettivi = 0; 
    
    private int medici;
    private int giorni;
    private int turni = 6;
    private int tipigiorno = 6; //tipi di giorno (lavoro, malus ecc...)

    private boolean feasible;
	
    public LpSolver(JTable mese, JTable compatibilità, JTable piano) throws LpSolveException  //costruisce una tabella con la soluzione
	{
		dati = new JTable(mese.getModel());
		
		medici = dati.getRowCount();
    	giorni = dati.getColumnCount()-1;   //la prima colonna contiene i nomi dei medici, la seconda le ferie residue
		
		feasible = true;
		
		Problema = new Modellatore(mese, compatibilità, piano, medici, giorni);
        lp = Problema.getLp();	
	}					
	
	public void SalvaVariabili(String path_soluzioni) throws LpSolveException
	{
		try 
		{
			 FileHandler.savePlan(lp.getPtrVariables(), dati, new File(path_soluzioni));
	    }     				

		catch (IOException ex) 
		{
		    System.out.println(ex.getMessage());
			ex.printStackTrace();
	    }  
	}
	
	public void Inizializza (String path_soluzioni, int giorno) throws LpSolveException
	{
		lp = new ListaSoluzione(new File(path_soluzioni)).Inizializza(lp, giorno);
	}	
	
	public boolean isFeasible()   //ritorna true se la soluzione esiste
	{
		return feasible;
	}
	
    public JTable getSoluzione(JTable tabella_soluzione) throws LpSolveException   //ritorna una tabella con la soluzione
    {	
    	 int pit = 0;
		 double[] var = lp.getPtrVariables();

		 JTable soluzione = new JTable(tabella_soluzione.getModel());	 

		 for (int i = 1; i <= giorni; i++)
		 	 for (int j = 1; j <= medici; j++)
		   		 for (int k = 1; k <= turni; k++)
	         	 {		
		             //if (dati.getValueAt(j-1, i).equals("Normale")) soluzione.setValueAt("", j-1, i);
		             //soluzione.setValueAt(dati.getValueAt(j-1, i), j-1, i);
		             
		             if (var[pit] == 1) 
		             {   
		            	 //System.out.println("Nel giorno " + i + ", il medico " + j + " fa il turno " + k);
		            	 switch (k)
			             {    
				             case 1:	
				             {     
				            	 soluzione.setValueAt("M1", j-1, i);
				                 break;
				             }
				             case 2:	
				             {     
				            	 soluzione.setValueAt("M2", j-1, i);
				                 break;
				             }
				             case 3:	
				             {     
				            	 soluzione.setValueAt("MP", j-1, i);
				                 break;
				             }
				             case 4:	
				             {     
				            	 soluzione.setValueAt("P", j-1, i);
				                 break;
				             }
				             case 5:	
				             {     
				            	 soluzione.setValueAt("N", j-1, i);
				                 break;
				             }
				             case 6:	
				             {     
				            	 soluzione.setValueAt("E", j-1, i);
				                 break;
				             }
				             
							 default:
							 { 
							    //niente
							 }  	 
		                 }
		             }
		             pit++;
	         	 }
	        
		  for (int i = 1; i <= giorni; i++)
		      for (int j = 1; j <= medici; j++)
		   		 for (int k = 1; k <= tipigiorno; k++)
	         	 {		       
		             if (var[pit] == 1) 
		             {   
		            	 //System.out.println("Nel giorno " + i + ", il medico " + j + " fa il turno " + k);
		            	 switch (k)
			             {    
			                 /*case 1:	//lavoro
			                 {     
			                	 if (soluzione.getValueAt(j-1, i-1).equals("N"))
			            	         soluzione.setValueAt("N (continuazione)", j-1, i);
			                     break;
			                 }*/
				             case 2:	//ferie
				             {     
				            	 soluzione.setValueAt("Ferie", j-1, i);
				                 break;
				             }
				             case 3:	//recupero in giorno scelto (di assenza programmata)
				             {     
				            	 soluzione.setValueAt("Rs", j-1, i);
				                 break;
				             }
				             case 4:	//recupero in giorno non scelto
				             {     
				            	 soluzione.setValueAt("Rns", j-1, i);
				                 break;
				             }
				             case 5:	//riposo
				             {     
				            	 soluzione.setValueAt("-", j-1, i);
				                 break;
				             }
				             case 6:	//malus
				             {     
				            	 soluzione.setValueAt("Malus", j-1, i);
				                 break;
				             }
				             
							 default:
							 { 
							    //niente
							 }  	 
		                 }
		             }
		             pit++;		   		 
		         }
		  
		  return soluzione;
    }
    
	private void RispettaObiettiviPrecedenti () throws LpSolveException
	{
		/** Il valore degli z1, z2 ... già calcolati alle iterazioni precedenti 
		 *  deve essere coerente con quello già ottimizzato
		 */ 
		
        for (int i = 0; i < n_obiettivi; i++)       
   		   lp.setBounds(Problema.getOffset() + i + 1, 0, obiettivi[i]);	
	}
	
	private void ObiettivoCorrente () throws LpSolveException
	{
		int Ncol = Problema.getColumns();
		int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        int count = Problema.getOffset() + n_obiettivi + 1;	
	    
        colno = new int[Ncol+1];
        sparserow = new double[Ncol+1];
        
        colno[count] = count; 
        sparserow[count] = 1;  
        count++;
	   
        lp.setObjFnex(count, sparserow, colno);
	}
	
	public void Risolvi (boolean abbiamo_tempo) throws LpSolveException
	{
		int obiettivi_da_calcolare = 4;
		if (abbiamo_tempo) obiettivi_da_calcolare = 5; //se abbiamo tempo minimizziamo anche le penalty
		
	    lp.setVerbose(LpSolve.IMPORTANT);
	    ObiettivoCorrente();
	    int responso;
	    	     
	    for (int i = 0; i < obiettivi_da_calcolare; i++)     
	    {
	    	//lp.writeLp(i + "modello.lp");
	        lp.setScaling(LpSolve.SCALE_GEOMETRIC + LpSolve.SCALE_INTEGERS + LpSolve.SCALE_EQUILIBRATE);
	    	responso = lp.solve();
	    	obiettivi[i] = lp.getObjective();
	    	
	    	if (responso != 2)
	    	{
	    	    n_obiettivi++;
	    	    //System.out.println("obiettivo " + i + ": " + obiettivi[i]);
	    	
			    RispettaObiettiviPrecedenti();
			    ObiettivoCorrente();
	    	}
	    	else feasible = false;
	    } 
	    //lp.printSolution(0); //stampa a video la soluzione
	}  
}
