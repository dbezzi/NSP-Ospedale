/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * ListaSoluzione.java è una classe statica e richiede:
 * - lpsolve 55.jar (wrapper di lpsolve55.dll)
 */

import java.io.File;
import java.io.IOException;

import lpsolve.*;

public class ListaSoluzione 
{
    private double[] soluzione_ammissibile;
    private int medici;
    private int giorni;
    private int dimensione_soluzione;
    private String[] nomi_medici;
    
    public ListaSoluzione (File file)
    {
    	try
    	{  		
    	    String[] lista = FileHandler.loadPlan(file); 
    	    medici = Integer.parseInt(lista[0]);   	    
    	    giorni = Integer.parseInt(lista[1]);
    	    dimensione_soluzione = Integer.parseInt(lista[2]);
    
    	    nomi_medici = new String[medici];
    	    for (int i = 0; i < medici; i++)
    	       nomi_medici[i] = lista[3+i];

    	    soluzione_ammissibile = new double[dimensione_soluzione];
    	    for (int i = 0; i < dimensione_soluzione; i++)
    	    	soluzione_ammissibile[i] = Double.parseDouble(lista[3+medici+i]);  
    	} 
    	
    	catch (IOException ex) 
		{
		    System.out.println(ex.getMessage());
			ex.printStackTrace();
	    }
    }
    
    //inizializza fino al giorno edge (escluso)
    public LpSolve Inizializza (LpSolve modello, int edge) throws LpSolveException
    {
    	int turni = 6;
    	int tipigiorno = 6;
    	int Offset1 = medici*giorni*turni;  //l'Offeset1-esima colonna è l'ultima del primo blocco di variabili
 	    int Offset2 = medici*giorni*tipigiorno;  //l'Offeset2-esima colonna è l'ultima del secondo blocco di variabili
 	    
 	    int count = 0;
    	//nella colonna #0 sta l'obiettivo
 	    
    	for (int g = 1; g < edge; g++)
	    	for (int m = 1; m <= medici; m++)
	    		for (int t = 1; t <= turni; t++)
	    	    {
	    		    modello.setBounds(count+1, soluzione_ammissibile[count], soluzione_ammissibile[count]);
	    	        count++;
	    	    }
	    
	     //---------------------------------------------//
	             
    	count = Offset1;
    	for (int g = 1; g < edge; g++)
	    	for (int m = 1; m <= medici; m++)
	    		for (int t = 1; t <= tipigiorno; t++)
	    	    {
	    		    modello.setBounds(count+1, soluzione_ammissibile[count], soluzione_ammissibile[count]);
	    	        count++;
	    	    }
	    
       //---------------------------------------------//
	    
    	count = Offset1 + Offset2;
	    for (int g = 1; g <= edge-7; g = g+7)
	    	 for (int m = 1; m <= medici; m++)
	    	 {
	    		    modello.setBounds(count+1, soluzione_ammissibile[count], soluzione_ammissibile[count]);
	    	        count++;
	    	 }	    
    	return modello;
    }
    
}
