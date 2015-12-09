/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * Modellatore.java richiede:
 * - lpsolve55.jar (wrapper di lpsolve55.dll)
 */

import javax.swing.JTable;
import lpsolve.*;

public class Modellatore
{  
    private LpSolve lp;

    private int medici;
    private int giorni;
    private int turni = 6;
    private int tipigiorno = 6; //tipi di giorno (lavoro, malus ecc...)
    private int Ncol;    //numero di colonne (variabili)
    private int Nrow;    //numero di righe (vincoli) 
    
    private int L = 4;   //distanza minima tra turni faticosi
    
    private int[][] ap;  //ap[m][g] vale 1 se il medico m nel giorno g ha un'assenza programmata
    private int[][] ultimiMP; //ultimiMP[m][g] se il medico m nel giorno g (degli L giorni del mese prima) ha un turno MP
    private int[][] ultimiN; //ultimiN[m][g] se il medico m nel giorno g (degli L giorni del mese prima) ha un turno N
    private int[] ultimaR;  //ultimaR[m]se il medico m nell'ultimo giorno del mese prima (che è domenica) ha un turno M1
    private int[] ferie; //ferie[m] indica il massimo numero di giorni di ferie di quel medico
    private int[] bonus; //bonus[m] indica il numero di bonus accumulati dal medico in passato
      
    private int Offset1; 
    private int Offset2;   
    private int Offset3;   
	
	public Modellatore(JTable mese, JTable compatibilità, JTable storico_mese_precedente, int m, int g) throws LpSolveException
	{	
		lp = LpSolve.makeLp(0, 0);
		
		medici = m;
    	giorni = g;   
		
	    Ncol = 0;
	    Nrow = 0;	    
	    
	    Offset1 = medici*giorni*turni;  //l'Offeset1-esima colonna è l'ultima del primo blocco di variabili
	    Offset2 = medici*giorni*tipigiorno;  //l'Offeset2-esima colonna è l'ultima del secondo blocco di variabili
	    Offset3 = medici*giorni/7;  //l'Offeset3-esima colonna è l'ultima del terzo blocco di variabili
	    
        //---------------------------------------------//
	    
	    for (int i = 1; i <= giorni; i++)
	    	 for (int j = 1; j <= medici; j++)
	    	 {
	             AggiungiVariabileBinaria ("g"+i+"m"+j+"turnoM1");    //turno del mattino con reperibilità
	             AggiungiVariabileBinaria ("g"+i+"m"+j+"turnoM2");    //turno del mattino senza reperibilità
	             AggiungiVariabileBinaria ("g"+i+"m"+j+"turnoMP");    //turno di mattina e pomeriggio
	             AggiungiVariabileBinaria ("g"+i+"m"+j+"turnoP");     //turno di pomeriggio
	             AggiungiVariabileBinaria ("g"+i+"m"+j+"turnoN");     //turno di notte
	             AggiungiVariabileBinaria ("g"+i+"m"+j+"turnoE");     //turno extra
	    	 }
	    
	     //---------------------------------------------//
	             
	    for (int i = 1; i <= giorni; i++)
	    	 for (int j = 1; j <= medici; j++)
	    	 {
	             AggiungiVariabileBinaria ("g"+i+"m"+j+"lavoro");     //giorno di lavoro
	    		 AggiungiVariabileBinaria ("g"+i+"m"+j+"ferie");      //giorno di ferie
	    		 AggiungiVariabileBinaria ("g"+i+"m"+j+"rec_sc");     //recupero in giorno scelto
	    		 AggiungiVariabileBinaria ("g"+i+"m"+j+"rec_nsc");    //recupero in giorno non scelto
	    		 AggiungiVariabileBinaria ("g"+i+"m"+j+"riposo");     //giorno di riposo
	    		 AggiungiVariabileBinaria ("g"+i+"m"+j+"malus");      //giorno di riposo extra
	    	 }
	    
        //---------------------------------------------//
	    
	    for (int i = 1; i <= giorni; i = i+7)
	    	 for (int j = 1; j <= medici; j++)
	    	 {
	             AggiungiVariabileBinaria ("lun"+i+"m"+j+"bonus");     //bonus della settimana
	    	 }
	    
        //---------------------------------------------//

	    for (int i = 1; i <= 5; i++)           //al massimo ci sono 5 obiettivi
    		AggiungiVariabileIntera ("z"+ i);  

        //Variabili ausiliarie per esprimere il vincolo su z5
	    
   	    //Distanza tra turni MP all'interno del periodo di pianicazione
	    for (int i = 1; i <= giorni; i++)
	    	 for (int j = 1; j <= medici; j++) 
    	         AggiungiVariabileIntera ("g"+i+"m"+j+"PenaltyMP1"); 
	    	
	    //Distanza tra turni MP a cavallo del periodo di pianicazione
	    for (int i = 1; i <= L; i++)
	    	 for (int j = 1; j <= medici; j++)
	    	     AggiungiVariabileIntera ("g"+i+"m"+j+"PenaltyMP2"); 
	    
	    //Distanza tra turni N all'interno del periodo di pianicazione
	    for (int i = 1; i <= giorni; i++)
	    	 for (int j = 1; j <= medici; j++) 
   	             AggiungiVariabileIntera ("g"+i+"m"+j+"PenaltyN1"); 
    	     
        //Distanza tra turni N a cavallo del periodo di pianicazione
	    for (int i = 1; i <= L; i++)
	    	 for (int j = 1; j <= medici; j++)
	    	     AggiungiVariabileIntera ("g"+i+"m"+j+"PenaltyN2");  
	    
        //---------------------------------------------//

	    ScriviDati (mese, compatibilità, storico_mese_precedente);
	    ScriviVincoli (mese, compatibilità);  //i vincoli fissi sono sempre quelli    	     
	}
	
	//ritorna un modello con solo i vincoli e i dati
	public LpSolve getLp ()
	{
		return lp;
	}
	
	public int getColumns ()
	{
		return Ncol;
	}
	
	public int getRows ()
	{
		return Nrow;
	}
	
	public int getOffset ()
	{
		return Offset1 + Offset2 + Offset3;
	}
	
	//aggiunge al modeello una variabile con quel nome	
	private void AggiungiVariabileBinaria (String nome) throws LpSolveException
	{
		 lp.setColName(++Ncol, nome);
         lp.setBinary(Ncol, true);
 
         if (nome.contains("turnoE")) lp.setBounds(Ncol, 0, 0);  //i turni extra vengono assegnati solo su richiesta
	}
	
	private void AggiungiVariabileIntera (String nome) throws LpSolveException
	{
		 lp.setColName(++Ncol, nome);
         lp.setInt(Ncol, true);
	}      
	
	//riempie la tabella di booleani ap[][]	
	//Bisogna avere sempre 2 giorni fissabili come riposo. I giorni dichiarati come "AP" (assenza programmata)
	//vanno ripartiti dal modello tra riposi, recuperi e ferie. Il numero di ferie iniziali è un dato.
	private void EstraiAssenzeProgrammate (JTable dati) 
	{
		 ap = new int[medici+1][giorni+1];  //è più comodo partire da 1
		 for (int g = 1; g <= giorni; g++)
		 	 for (int m = 1; m <= medici; m++)
		 	 {
		 		 ap[m][g] = 0;
		 		 if (dati.getValueAt(m-1, g).equals("AP")) ap[m][g] = 1;        //assenza programmata
		 		 //if (dati.getValueAt(m-1, g).equals("Ferie")) ap[m][g] = 1;
		 		 //if (dati.getValueAt(m-1, g).equals("Rs")) ap[m][g] = 1;
		 		 //if (dati.getValueAt(m-1, g).equals("-")) ap[m][g] = 1;
		 	 }
	}
	
	private void EstraiMonteFerie (JTable tabella_ferie) 
	{
		 ferie = new int[medici+1];  //è più comodo partire da 1
	 	 for (int m = 1; m <= medici; m++)
		     ferie[m] = Integer.parseInt(tabella_ferie.getValueAt(m-1, 1).toString());
	}
	
	private void EstraiMonteBonus (JTable tabella_bonus) 
	{
		 bonus = new int[medici+1];  //è più comodo partire da 1		 
	 	 for (int m = 1; m <= medici; m++)
		     bonus[m] = Integer.parseInt(tabella_bonus.getValueAt(m-1, 2).toString());	  
	}
	
	private void EstraiUltimiTurniFaticosi (JTable dati, JTable storico) 
	{
		 ultimiMP = new int[medici+1][L+1];  //è più comodo partire da 1
		 ultimiN = new int[medici+1][L+1];  //è più comodo partire da 1
		 ultimaR = new int[medici+1];  //è più comodo partire da 1
		 
		 int colonne = storico.getColumnCount()-1; 
		 int[] indici = AbbinaRighe(dati, storico);   //i medici assenti nel mese prima non danno problemi

		 for (int m = 1; m <= medici; m++)
		 {   
	         for (int g = 1; g <= L; g++) 
	 	     {
	 		     ultimiMP[m][g] = 0;  
	 		     ultimiN[m][g] = 0;  
	 	     }
	         ultimaR[m] = 0;
	         
			 if (indici[m-1] != -1)
			 {
		         for (int g = 1; g <= L; g++)
		 	     {
		 		     if (storico.getValueAt(indici[m-1], colonne-L+g).equals("MP")) ultimiMP[m][g] = 1; //l'L-esimo giorno è domenica   
		 		     if (storico.getValueAt(indici[m-1], colonne-L+g).equals("N")) ultimiN[m][g] = 1;      
		 	     }
	 		     if (storico.getValueAt(indici[m-1], colonne).equals("M1")) ultimaR[m] = 1;
			 }
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
	
	//ritorna il numero di assenze programmate previste per quel medico in quella settimana
	private int ContaAssenzeProgrammate (int medico, int indice_settimana) 
	{
		 int assenze = 0;
	 	 for (int i = indice_settimana; i < indice_settimana+7; i++)
		 	 assenze = assenze + ap[medico][i+1];
		  
	 	 return assenze;
	}
	
	private void ScriviDati (JTable dati, JTable dati_extra, JTable storico_mese_precedente) throws LpSolveException  //costruisce una tabella con la soluzione
	{
        EstraiAssenzeProgrammate (dati);
        EstraiMonteFerie (dati_extra);
        EstraiMonteBonus (dati_extra);
	    EstraiUltimiTurniFaticosi (dati, storico_mese_precedente); 
	}
	
	private void ScriviVincoli (JTable dati, JTable compatibilità) throws LpSolveException  //costruisce una tabella con la soluzione
	{      	 
		//------ OBIETTIVI ------//
		
	/*	if (n_obiettivi == 0) Obiettivo1_MpFeriali();
		if (n_obiettivi == 1) Obiettivo2_BonusResiduiMinMax();
		if (n_obiettivi == 2) Obiettivo3_BonusResiduiMinSum();
		if (n_obiettivi == 3) Obiettivo4_BonusMalusTotali();
		if (n_obiettivi == 4) Obiettivo5_TurniRavvicinati(); */
		  		
		 //------ VINCOLI ------//
		 
		 lp.setAddRowmode(true);  //il modello va costruito riga per riga
		 
		 /** Vincolo sui turni: 
		  
		 per ogni giorno, o si copre M1,M2,P,N o si copre M1,MP,N */	    	     		 
 		 
 		 DefinisciVincoliSuiTurni ();
		 
		 /** Vincolo sui giorni: 
		  
		 un giorno può essere di lavoro, ferie, recupero, riposo o malus */
			 
		 DefinisciVincoliSuiGiorni ();	
		 DefinisciAssenzeProgrammate ();
		 
		 /** Vincolo sui riposi: 
		  
		 per ogni settimana, ogni medico ha 2 giorni di riposo oppure 1 giorno e 1 bonus */
		 
		 DefinisciGiorniDiRiposo ();
		 DefinisciBonus ();
		 DefinisciRecuperi ();
		 DefinisciFerie ();
		 
		 /** Vincolo sulla relazione tra le variabili x (mg_turno) e y (mg_tipogiorno)
		  
		 un giorno di lavoro è un giorno in cui viene svolto uno dei sei turni, oppure
		 un giorno successivo a un turno di notte: */
		 
		 DefinisciGiorniDiLavoro ();
		 TrovaUltimaN (); 
		 
		 /** Vincolo sulla distanza minima tra turni di reperibilità lunga; 
		  
		 chi fa un turno M1 con reperibilità non può svolgere turni non N nel giorno successivo */
		 
		 DefinisciTurniLunghi ();
		 TrovaUltimaR (); 

 		 //------ DATI DEFINITI DA UTENTE -----//
 		 
 		 /** Compatibilità medico-turno (alcuni medici non fanno mai certi turni) */
 		 
		 DefinisciCompatibilità (compatibilità);
 		 
 		 /** Dati del problema (inseriti a mano dall'utente) */
 		
		 DefinisciDati (dati);	 
		 
		 //------ VINCOLI AUSILIARI ------//
		 
		 /** Necessari per esprimere gli obiettivi */
		 
		 //quando ci sono ancora variabili libere non ha senso vincolarle a obiettivi che ancora non ci sono
		 /*switch(n_obiettivi)
		 {
		     case 5: VincoloScostamentoDalleQuote();			
		     case 4: VincoloTurniRavvicinati();
		     case 3: VincoloBonusMalusTotali();
		     case 2: VincoloBonusResiduiMinSum();
	         case 1: VincoloBonusResiduiMinMax();
		     case 0: VincoloMpFeriali ();
		 }*/	 

         VincoloMpFeriali ();
         VincoloBonusResiduiMinMax();
         VincoloBonusResiduiMinSum();
         VincoloTurniRavvicinati();
         VincoloBonusMalusTotali();
		 
	     //------ FINE COSTRUZIONE DEL MODELLO ------//
		 
		 lp.setAddRowmode(false);	 

	     //System.out.println(Ncol + " colonne (variabili) e " + Nrow + " righe (vincoli)"); 	 
	}
	
	
	//-------------------SUBROUTINE-------------------//	
	
	private void VincoloMpFeriali () throws LpSolveException
	{
		/**  z1 (primo obiettivo) è la somma degli MP nei giorni feriali 
		 *  
		 * 	 z1 = g1m1MP+g2m1MP+g3m1MP+g4m1MP+g5m1MP  + g8m1MP ... + g1m2MP ... 
		 */    
		
		int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
	    int count = 0;
	    
        colno = new int[Ncol+1];
        sparserow = new double[Ncol+1];

        for (int g = 1; g <= giorni; g++) 
        {       
        	if ((g%7 != 0) && ((g+1)%7 != 0))  //saltiamo sabati e domeniche
        	{
                for (int m = 1; m <= medici; m++)
                {
                    count = 3 + ((m-1)*turni) + ((g-1)*turni*medici);
                
   		            colno[count] = count; 
   		            sparserow[count] = 1;  
   		            count++;	     
                }
        	}
        }

        count = Offset1 + Offset2 + Offset3 + 1;	
        colno[count] = count; 
        sparserow[count] = -1;  
        count++;	

        lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 0);    
	}	
	
	private void VincoloBonusResiduiMinMax () throws LpSolveException
	{
		/**  z2 (secondo obiettivo) è il numero di bonus residui del medico con più bonus residui 
		 *  
		 *   per ogni medico:
		 *  
		 * 	 z2 >= (bonuspassati) + lun1m1bonus - (g1m1rec_sc + g1m1rec_nsc +...+ g7m1rec_sc + g7m1rec_nsc)
		 */    
		
	    int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        int count;   //count è il numero di coefficienti non nulli
       
        for (int m = 1; m <= medici; m++)
        {       
            colno = new int[Ncol+1];
            sparserow = new double[Ncol+1];
            for (int sett = 0; sett < giorni; sett = sett+7)
            {
                count = Offset1 + 3 +((m-1)*tipigiorno); 

   	            for (int g = 0; g < sett+7; g++)
   	   	        {
   		            colno[count] = count;         //recupero in giorno scelto
   		            sparserow[count] = -1; 
   		            count++;
   	        	 
   		            colno[count] = count;         //recupero in giorno non scelto
   		            sparserow[count] = -1; 
   		            count = count + medici*tipigiorno -1;
   	            }
   	         
   	            for (int k = 0; k <= sett/7; k++)
   	            {  
   	                count = Offset1 + Offset2 + 1 + (m-1) + k*medici;            
		            colno[count] = count;          //bonus
		            sparserow[count] = 1;  	
		            count++;
   	            }  	        
            }
            
            count = Offset1 + Offset2 + Offset3 + 2;	//z2
	        colno[count] = count; 
	        sparserow[count] = -1;  
	        count++;	

            lp.addConstraintex(count, sparserow, colno, LpSolve.LE, -bonus[m]);
            Nrow++;
        }	
	}
	
	private void VincoloBonusResiduiMinSum () throws LpSolveException
	{
		/**  z3 (terzo obiettivo) è pari alla somma di tutti i bonus residui
		 *  
		 * 	 z3 = (bonuspassati m1) + lun1m1bonus - (g1m1rec_sc...) +...+ (bonuspassati m2) + lun1m2bonus ...
		 */    
		
	    int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        int count;   //count è il numero di coefficienti non nulli
       
        colno = new int[Ncol+1];
        sparserow = new double[Ncol+1];
        
        int bonus_totale = 0;
        for (int m = 1; m <= medici; m++)
        {       
        	bonus_totale = bonus_totale + bonus[m];
            for (int sett = 0; sett < giorni; sett = sett+7)
            {
                count = Offset1 + 3 +((m-1)*tipigiorno) + (sett*tipigiorno*medici);
   	            for (int g = 1; g <= 7; g++)
   	   	        {
   		            colno[count] = count; 
   		            sparserow[count] = -1;  //System.out.println(lp.getColName(count));
   		            count++;
   	        	 
   		            colno[count] = count; 
   		            sparserow[count] = -1;  //System.out.println(lp.getColName(count));
   		            count = count + medici*tipigiorno -1;
   	            }
   	         
   	            count = Offset1 + Offset2 + 1 + (m-1) + (sett/7)*medici;            
		        colno[count] = count;  
		        sparserow[count] = 1;  	
		        count++;          
            }
        }	
        
        count = Offset1 + Offset2 + Offset3 + 3;	
        colno[count] = count; 
        sparserow[count] = -1;  
        count++;	

        lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, -bonus_totale);
        Nrow++;	
	}
	
	private void VincoloBonusMalusTotali () throws LpSolveException
	{
		/**  z4 (quarto obiettivo) è pari al totale di tutti i bonus e malus attribuiti dal solutore questo mese
		 *  
		 * 	 z4 = (bonuspassati m1) + lun1m1bonus - (g1m1rec_sc...) +...+ (bonuspassati m2) + lun1m2bonus ...
		 */    
		
	    int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        int count;   //count è il numero di coefficienti non nulli
       
        colno = new int[Ncol+1];
        sparserow = new double[Ncol+1];
        
        for (int m = 1; m <= medici; m++)
        {       
            for (int sett = 0; sett < giorni; sett = sett+7)
            {
                count = Offset1 + 3 +((m-1)*tipigiorno) + (sett*tipigiorno*medici);
   	            for (int g = 1; g <= 7; g++)
   	   	        {
   		            colno[count] = count;    //recupero in giorno scelto
   		            sparserow[count] = 1; 
   		            count++;
   		            
   		            colno[count] = count;    //recupero in giorno non scelto
		            sparserow[count] = 1; 
		            count = count + 2;
   	        	 
   		            colno[count] = count;    //malus (conta doppio) 
   		            sparserow[count] = 2; 
   		            count = count + medici*tipigiorno -3;
   	            }
   	         
   	            count = Offset1 + Offset2 + 1 + (m-1) + (sett/7)*medici;            
		        colno[count] = count;  
		        sparserow[count] = 1;  	
		        count++;          
            }
        }	
        
        count = Offset1 + Offset2 + Offset3 + 4;	
        colno[count] = count; 
        sparserow[count] = -1;  
        count++;	

        lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 0);
        Nrow++;	
	}
	
	private void VincoloTurniRavvicinati () throws LpSolveException
	{
		/**  z5 (quinto obiettivo) è la somma delle penalità per turni MP e N ravvicinati.
		 *   Due turni sono ravvicinati se tra la fine del primo e l'inizio del successivo
         *   (dello stesso tipo e assegnato allo stesso medico) non trascorrano almeno L giorni completi.
		 *  
		 *   per ogni medico:
		 *  
		 * 	 z5 = penaltyMP + penaltyMP al confine + penaltyN + penaltyN al confine
		 */   
		
		int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        int count;   //count è il numero di coefficienti non nulli
       
        colno = new int[Ncol+1];
        sparserow = new double[Ncol+1];
        
        //calcolo penalty MP durante il periodo di pianificazione
        DistanzaMP1 ();
        
        //calcolo penalty MP a cavallo del periodo di pianificazione
        DistanzaMP2 ();
        
        //calcolo penalty N durante il periodo di pianificazione
        DistanzaN1 (); 
        
        //calcolo penalty N a cavallo del periodo di pianificazione
        DistanzaN2 ();
        
        count = Offset1 + Offset2 + Offset3 + 5;	//z5
        colno[count] = count; 
        sparserow[count] = -1; 
        
    	count = Offset1 + Offset2 + Offset3 + 6;
        for (int g = 0; g < 2*(giorni + L); g++)   //li prendo tutti in fila
        {       
            for (int m = 1; m <= medici; m++) 
            {
		        colno[count] = count; 
		        sparserow[count] = 1;  //System.out.println(lp.getColName(count));
		        count++;
            }
        }

        lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 0);
        Nrow++;	 
	}
	
	//----------------- SUBROUTINE PER ESPRIMERE z5 -----------------//
	
	private void DistanzaMP1 () throws LpSolveException
	{
		/**
		 * s.t. Distanza_MP1 {m in Medici, g in Giorni, h in Giorni: h>=g-L && h<=g-1}: 
         *   penaltyMP1[m,g,h] >= ((L+1)-(g-h)) * (x[m,g,"MP"] + x[m,h,"MP"] - 1);
         *   
         *   passano 3 giorni invece di 4: penalty 1  (es. MP lunedì e venerdì)
         *   passano 2 giorni invece di 4: penalty 2  (es. MP lunedì e giovedì)
         *   passano 1 giorni invece di 4: penalty 3  (es. MP lunedì e mercoledì)
         *   passano 0 giorni invece di 4: penalty 4  (es. MP lunedì e martedì)
		 * 
		 */
        
        int penalty;
        
        for (int m = 1; m <= medici; m++) 
        {       
        	 for (int g = 1; g <= giorni; g++)  //non ha senso verificare anche il primo giorno (serve un vincolo apposta)
            {
        		 for (int h = Math.max(g - L, 1); h <= g - 1; h++) 
        		 {
        			 penalty = ((L + 1) - (g - h));
        			 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        		     double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        		     int count;   //count è il numero di coefficienti non nulli
        		       
        		     colno = new int[Ncol+1];
        		     sparserow = new double[Ncol+1];	
        			 
        			 //giorno g, medico m, turno MP
        	         count = 3 + ((g-1)*turni*medici) + ((m-1)*turni);
		             colno[count] = count; 
		             sparserow[count] = penalty;  //System.out.println("giorno " + g + ", medico " +m +": "+ lp.getColName(count));
		             count++;
		             
		           //giorno g-h, medico m, turno MP
		             count = 3 + ((h-1)*turni*medici) + ((m-1)*turni);
		             colno[count] = count; 
		             sparserow[count] = penalty;  //System.out.println(lp.getColName(count));
		             count++;
		             
		           //giorno g, medico m, penalty MP1
		             count = Offset1 + Offset2 + Offset3 + 6 + (g-1)*medici + (m-1);
		             colno[count] = count; 
		             sparserow[count] = -1;  //System.out.println(lp.getColName(count));
		             count++;
		                 			 
		             lp.addConstraintex(count, sparserow, colno, LpSolve.LE, penalty);
		             Nrow++;
        		 }
            }
        }
	}
	
	private void DistanzaMP2 () throws LpSolveException
	{
        int penalty;
        
        for (int m = 1; m <= medici; m++) 
        {       
        	for (int g = 1; g <= L; g++)  
            {
        		 for (int k = g; k <= L; k++) 
        		 {
        			 penalty = k - g + 1;
        			 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        		     double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        		     int count;   //count è il numero di coefficienti non nulli
        		       
        		     colno = new int[Ncol+1];
        		     sparserow = new double[Ncol+1];	
        			 
        			 //giorno g, medico m, turno MP
        	         count = 3 + ((g-1)*turni*medici) + ((m-1)*turni);
		             colno[count] = count; 
		             sparserow[count] = penalty;  //System.out.println("giorno " + g + ", medico " +m +": "+ lp.getColName(count));
		             count++;
		             
		           //giorno g, medico m, penalty MP2
		             count = Offset1 + Offset2 + Offset3 + 6 + giorni*medici + (g-1)*medici + (m-1);
		             colno[count] = count; 
		             sparserow[count] = -1;  //System.out.println(lp.getColName(count));
		             count++;
		                 			 
		             lp.addConstraintex(count, sparserow, colno, LpSolve.LE, penalty * (1 - ultimiMP[m][k]));
		             Nrow++;
        		 }
            }
        }
	}
	
	private void DistanzaN1 () throws LpSolveException
	{
		/**
		 * s.t. Distanza_N1 {m in Medici, g in Giorni, h in Giorni: h>=g-L && h<=g-1}: 
         *   penaltyN1[m,g,h] >= ((L+1)-(g-h)) * (x[m,g,"N"] + x[m,h,"N"] - 1);
         *   
         *   passano 3 giorni invece di 4: penalty 1  (es. N lunedì e venerdì)
         *   passano 2 giorni invece di 4: penalty 2  (es. N lunedì e giovedì)
         *   passano 1 giorni invece di 4: penalty 3  (es. N lunedì e mercoledì)
         *   passano 0 giorni invece di 4: penalty 4  (es. N lunedì e martedì)
		 * 
		 */
        
        int penalty;
        
        for (int m = 1; m <= medici; m++) 
        {       
        	 for (int g = 1; g <= giorni; g++)  //non ha senso verificare anche il primo giorno (serve un vincolo apposta)
            {
        		 for (int h = Math.max(g - L, 1); h <= g - 1; h++) 
        		 {
        			 penalty = ((L + 1) - (g - h));
        			 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        		     double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        		     int count;   //count è il numero di coefficienti non nulli
        		       
        		     colno = new int[Ncol+1];
        		     sparserow = new double[Ncol+1];	
        			 
        			 //giorno g, medico m, turno N
        	         count = 5 + ((g-1)*turni*medici) + ((m-1)*turni);
		             colno[count] = count; 
		             sparserow[count] = penalty;  //System.out.println("giorno " + g + ", medico " +m +": "+ lp.getColName(count));
		             count++;
		             
		           //giorno g-h, medico m, turno N
		             count = 5 + ((h-1)*turni*medici) + ((m-1)*turni);
		             colno[count] = count; 
		             sparserow[count] = penalty;  //System.out.println(lp.getColName(count));
		             count++;
		             
		           //giorno g, medico m, penalty N1
		             count = Offset1 + Offset2 + Offset3 + 6 + (giorni + L)*medici + (g-1)*medici + (m-1);
		             colno[count] = count; 
		             sparserow[count] = -1;  //System.out.println(lp.getColName(count));
		             count++;
		                 			 
		             lp.addConstraintex(count, sparserow, colno, LpSolve.LE, penalty);
		             Nrow++;
        		 }
            }
        }
	}
	
	private void DistanzaN2 () throws LpSolveException
	{
        int penalty;
        
        for (int m = 1; m <= medici; m++) 
        {       
        	for (int g = 1; g <= L; g++)  
            {
        		 for (int k = g; k <= L; k++) 
        		 {
        			 penalty = k - g + 1;
        			 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        		     double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        		     int count;   //count è il numero di coefficienti non nulli
        		       
        		     colno = new int[Ncol+1];
        		     sparserow = new double[Ncol+1];	
        			 
        			 //giorno g, medico m, turno N
        	         count = 5 + ((g-1)*turni*medici) + ((m-1)*turni);
		             colno[count] = count; 
		             sparserow[count] = penalty;  //System.out.println("giorno " + g + ", medico " +m +": "+ lp.getColName(count));
		             count++;
		             
		           //giorno g, medico m, penalty N2
		             count = Offset1 + Offset2 + Offset3 + 6 + ((2*giorni) + L)*medici + (g-1)*medici + (m-1);
		             colno[count] = count; 
		             sparserow[count] = -1;  //System.out.println(lp.getColName(count));
		             count++;
		                 			 
		             lp.addConstraintex(count, sparserow, colno, LpSolve.LE, penalty * (1 - ultimiN[m][k]));
		             Nrow++;
        		 }
            }
        }
	}
	
	//-----------------SUBROUTINE PER I VINCOLI REALI-----------------//
	
	private void DefinisciVincoliSuiGiorni () throws LpSolveException
	{
		/**  per ogni giorno, per ogni medico:  
		 *  
		 * 	 g1m1lavoro+g1m1ferie+...+g1m1malus = 1
		 */     
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli
        
         for (int g = 0; g < giorni; g++)
         {       
             for (int m = 1; m <= medici; m++)
             {
                 count = Offset1 + 1 +((m-1)*tipigiorno) + (g*tipigiorno*medici);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];
    	         for (int t = 1; t <= tipigiorno; t++)
    	   	     {
    		         colno[count] = count; 
    		         sparserow[count] = 1;  //System.out.println(lp.getColName(count));
    		         count++;
    	         }
                 lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 1);
                 Nrow++;
             }
         }		
	}
	
	private void DefinisciAssenzeProgrammate () throws LpSolveException
	{
		/**  per ogni giorno, per ogni medico:  
		 *  
		 * 	 se un giorno è di assenza programmata, non può essere di lavoro, di recupero non richiesto o malus:
		 *   g1m1lavoro + g1m1rec_nsc + g1m1malus <= 1 - ap[m][g]
		 * 
		 *   se un giorno non è di assenza programamta, non può essere di ferie o di recupero richiesto:
		 *   g1m1ferie + g1m1rec_sc <= ap[m][g]
		 */     
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli
        
         for (int g = 1; g <= giorni; g++)
         {       
             for (int m = 1; m <= medici; m++)
             {
                 count = Offset1 + 1 +((m-1)*tipigiorno) + ((g-1)*tipigiorno*medici);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];

                 //prendo il primo, il quarto e il sesto
    		     colno[count] = count; 
    		     sparserow[count] = 1;  
    		     count = count+3;
    		     
    		     colno[count] = count; 
    		     sparserow[count] = 1;  
    		     count = count+2;
    	         
    		     colno[count] = count; 
    		     sparserow[count] = 1;  
    		     count = count+1;
    		     
                 lp.addConstraintex(count, sparserow, colno, LpSolve.LE, 1 - ap[m][g]);
                 Nrow++;
             }
         }	
         
         for (int g = 1; g <= giorni; g++)
         {       
             for (int m = 1; m <= medici; m++)
             {
                 count = Offset1 + 2 +((m-1)*tipigiorno) + ((g-1)*tipigiorno*medici);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];

                 //prendo il secondo e il terzo
    		     colno[count] = count; 
    		     sparserow[count] = 1;  
    		     count = count+1;
    	         
    		     colno[count] = count; 
    		     sparserow[count] = 1;  
    		     count = count+5;
    		     
                 lp.addConstraintex(count, sparserow, colno, LpSolve.LE, ap[m][g]);
                 Nrow++;
             }
         }	
	}
	
	private void DefinisciGiorniDiRiposo () throws LpSolveException
	{
		/**  per ogni settimana, per ogni medico:  
		 *  
		 * 	 g1m1riposo+g2m1riposo+...+g7m1riposo + lun1m1bonus = 2
		 */     
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli
        
         for (int m = 1; m <= medici; m++)
         {       
             for (int sett = 0; sett < giorni; sett = sett+7)
             {
                 count = Offset1 + 5 +((m-1)*tipigiorno) + (sett*tipigiorno*medici);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];
    	         for (int g = 1; g <= 7; g++)
    	   	     {
    		         colno[count] = count; 
    		         sparserow[count] = 1;  //System.out.println(lp.getColName(count));
    		         count = count + medici*tipigiorno;
    	         }
    	         
    	         count = Offset1 + Offset2 + 1 + (m-1) + (sett/7)*medici;            
		         colno[count] = count;  
		         sparserow[count] = 1;  	
		         count++;                 
    	         
                 lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 2);
                 Nrow++;
             }
         }		
	}	
	
	private void DefinisciBonus () throws LpSolveException
	{
		/**  per ogni medico:  
		 *  
		 * 	 (bonuspassati) + lun1m1bonus - (g1m1rec_sc + g1m1rec_nsc +...+ g7m1rec_sc + g7m1rec_nsc) >= 0
		 *   (bonuspassati) + lun1m1bonus + lun2m1bonus - (g1m1rec_sc +...+ g8m1rec_sc +...) >= 0
		 * 
		 */     
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli
        
         for (int m = 1; m <= medici; m++)
         {       
             for (int sett = 0; sett < giorni; sett = sett+7)
             {
                 count = Offset1 + 3 +((m-1)*tipigiorno); //+ (sett*tipigiorno*medici);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];
    	         for (int g = 0; g < sett+7; g++)
    	   	     {
    		         colno[count] = count; 
    		         sparserow[count] = -1; 
    		         count++;
    	        	 
    		         colno[count] = count; 
    		         sparserow[count] = -1; 
    		         count = count + medici*tipigiorno -1;
    	         }
    	         
    	         for (int k = 0; k <= sett/7; k++)
    	         {
    	             count = Offset1 + Offset2 + 1 + (m-1) + k*medici;            
		             colno[count] = count;  
		             sparserow[count] = 1;  	
		             count++;
    	         }

                 lp.addConstraintex(count, sparserow, colno, LpSolve.GE, -bonus[m]);
                 Nrow++;
             }
         }		
	}	
	
	private void DefinisciRecuperi () throws LpSolveException
	{
		/**  per ogni settimana, per ogni medico:  
		 *  
		 * 	 g1m1ferie+g1m1rec_sc +g2m1ferie+g2m1rec_sc +...+ g7m1ferie+g7m1rec_sc = A
		 * 
		 *   dove A è il numero di assenze programmate per quella settimana meno 2
		 *   (in teoria non dovrebbero mai essere più di due)
		 *   
		 *   I giorni di assenza programmata contano prima come riposi, poi come recuperi, poi come ferie
		 */     
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli
        
         for (int m = 1; m <= medici; m++)
         {       
             for (int sett = 0; sett < giorni; sett = sett+7)
             {
                 count = Offset1 + 2 +((m-1)*tipigiorno) + (sett*tipigiorno*medici);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];
    	         for (int g = 1; g <= 7; g++)
    	   	     {
    		         colno[count] = count; 
    		         sparserow[count] = 1;    //ferie
    		         count++;
    	        	 
    		         colno[count] = count; 
    		         sparserow[count] = 1;    //recupero in giorno scelto
    		         count = count + medici*tipigiorno -1;
    	         }           
    	         int a = ContaAssenzeProgrammate(m, sett);
    	         //System.out.println ("medico" +m +" in settimana " + sett + " fa " + a + " assenze");
    	         
                 lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, (Math.max(a-2, 0)));
                 Nrow++;
             }
         }
	}	
	
	private void DefinisciFerie() throws LpSolveException
	{
		/**  per ogni medico, le ferie non devono sforare il monte ferie indicato:  
		 *  
		 * 	 g1m1ferie+g2m1ferie +g3m1ferie +...+ g30m1ferie <= ferie[m]
		 */
		
		int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        int count;   //count è il numero di coefficienti non nulli
       
        for (int m = 1; m <= medici; m++)
        {
            count = Offset1 + 2 +((m-1)*tipigiorno);
            colno = new int[Ncol+1];
            sparserow = new double[Ncol+1];

            for (int g = 1; g <= giorni; g++)
            {
                //prendo il secondo
   		        colno[count] = count; 
   		        sparserow[count] = 1;  
   		        count = count + medici*tipigiorno;
            }
            lp.addConstraintex(count, sparserow, colno, LpSolve.LE, ferie[m]);
            Nrow++;
        }
          
	}
	
	private void DefinisciTurniLunghi () throws LpSolveException
	{ 
		/**  per ogni settimana, per ogni medico:  
		 *  
		 * 	 g2m1turnoM1+g3m1turnoM1+g3m1turnoM2+g3m1turnoMP+g3t1turnoP <= 1
		 *   g4m1turnoM1+g5m1turnoM1+g5m1turnoM2+g5m1turnoMP+g5t1turnoP <= 1
		 *   g7m1turnoM1+g8m1turnoM1+g8m1turnoM2+g8m1turnoMP+g8t1turnoP <= 1
		 *   
		 *   (solo per i martedì, giovedì e domeniche)
		 */     
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli
        
         for (int m = 1; m <= medici; m++)
         { 
             for (int sett = 0; sett < giorni; sett = sett+7)
             {       
        	     int g = 1+sett; //martedì
            
                 count = 1 + (g*turni*medici) + ((m-1)*turni);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];
                 
		         colno[count] = count; 
		         sparserow[count] = 1;  //System.out.println(lp.getColName(count));
                 g++; 
                 count = 1 + (g*turni*medici) + ((m-1)*turni);
                 
    	         for (int t = 1; t <= turni-2; t++)  //le notti e gli extra non contano
    	   	     {
    		         colno[count] = count; 
    		         sparserow[count] = 1;  //System.out.println(lp.getColName(count));
    		         count++;
    	         }
                 lp.addConstraintex(count, sparserow, colno, LpSolve.LE, 1);
                 Nrow++;
             
                 g=g+1;  //giovedì

                 count = 1 + (g*turni*medici) + ((m-1)*turni);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];
                 
		         colno[count] = count; 
		         sparserow[count] = 1;  //System.out.println(lp.getColName(count));
                 g++; 
                 count = 1 + (g*turni*medici) + ((m-1)*turni);
                 
    	         for (int t = 1; t <= turni-2; t++)  //le notti e gli extra non contano
    	   	     {
    		         colno[count] = count; 
    		         sparserow[count] = 1;  //System.out.println(lp.getColName(count));
    		         count++;
    	         }
                 lp.addConstraintex(count, sparserow, colno, LpSolve.LE, 1);
                 Nrow++;
             
                 if (g != giorni - 3) //l'ultima domenica non conta (conta nel mese successivo)
                 {
                     g=g+2; //domenica

                     count = 1 + (g*turni*medici) + ((m-1)*turni);
                     colno = new int[Ncol+1];
                     sparserow = new double[Ncol+1];
                 
		             colno[count] = count; 
		             sparserow[count] = 1;  //System.out.println(lp.getColName(count));
                     g++; 
                     count = 1 + (g*turni*medici) + ((m-1)*turni);
                 
    	             for (int t = 1; t <= turni-2; t++)  //le notti e gli extra non contano
    	   	         {
    		             colno[count] = count; 
    		             sparserow[count] = 1;  //System.out.println(lp.getColName(count));
    		             count++;
    	             }
                     lp.addConstraintex(count, sparserow, colno, LpSolve.LE, 1);
                     Nrow++;
                 }
             }
         }		
	
	}
	
	private void DefinisciGiorniDiLavoro () throws LpSolveException
	{
		/**  per ogni giorno, per ogni medico:  
		 *  
		 * 	 g1m1turnoM1 + g1m1turnoM2 + g1m1turnoMP + g1m1turnoP + g1m1turnoN +g1m1turnoE + g0m1turnoN = g1m1lavoro
		 */      
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli

         for (int g = 1; g < giorni; g++) //per il primo giorno serve un vincolo apposta (TrovaUltimaN)
         {       
             for (int m = 1; m <= medici; m++)
             {
                 count = 1+((m-1)*turni) + (g*turni*medici);
                 colno = new int[Ncol+1];
                 sparserow = new double[Ncol+1];
    	         for (int t = 1; t <= turni; t++)
    	   	     {
    		         colno[count] = count; 
    		         sparserow[count] = 1;  
    		         count++;
    	         }
     		     
    	         count = count - 2 - (turni*medici); //la notte è anche quella del giorno prima
		         colno[count] = count; 
		         sparserow[count] = 1;  	  
		         
	             count = Offset1 + 1 +((m-1)*tipigiorno) + (g*tipigiorno*medici);
	                
		         colno[count] = count;  //giorno di lavoro
		         sparserow[count] = -1;  	
		         count++;                 
	                 
                 lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 0);
                 Nrow++;
             }
         }
	}	
	
	//aggiunge il vincolo relativo a chi ha fatto l'ultima notte
	private void TrovaUltimaN () throws LpSolveException
	{
		/**  per ogni medico:  
		 *  
		 * 	 g1m1turnoM1 + g1m1turnoM2 + g1m1turnoMP + g1m1turnoP + g1m1turnoN +g1m1turnoE + g0m1turnoN = g1m1lavoro
		 *   (il dato su g0 viene recuperato dallo storico)
		 */      
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli
       
         for (int m = 1; m <= medici; m++)
         {
             int HaFattoUltimaNotte = ultimiN[m][L]; 
             
             count = 1+((m-1)*turni);
             colno = new int[Ncol+1];
             sparserow = new double[Ncol+1];
    	     for (int t = 1; t <= turni; t++)      //conta anche la notte del giorno prima?
    	     {
    	         colno[count] = count; 
    		     sparserow[count] = 1;  
    		     count++;
    	     }
	                
    	     count = Offset1 + 1 +((m-1)*tipigiorno);
		     colno[count] = count;  
		     sparserow[count] = -1;  	
		     count++;                 
		     
             lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, -HaFattoUltimaNotte);
             Nrow++;
         }
         
	}	
		
	//aggiunge il vincolo relativo a chi ha fatto l'ultimo turno M1
	private void TrovaUltimaR () throws LpSolveException
	{
		/**  per ogni medico:  
		 *  
		 * 	 g0m1turnoM1+g1m1turnoM1+g1m1turnoM2+g1m1turnoMP+g1t1turnoP <= 1
		 *   (il dato su g0 viene recuperato dallo storico)
		 */     
		
		 int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
         double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
         int count;   //count è il numero di coefficienti non nulli
           
         for (int m = 1; m <= medici; m++)
         {              
             int HaFattoUltimaReperibilità = ultimaR[m]; 
             
        	 count = 1 + ((m-1)*turni);
             colno = new int[Ncol+1];
             sparserow = new double[Ncol+1];
                 
    	     for (int t = 1; t <= turni-2; t++)  //le notti e gli extra non contano
    	   	 {
    		     colno[count] = count; 
    		     sparserow[count] = 1;  
    		     count++;
    	     }
    	        	     
             lp.addConstraintex(count, sparserow, colno, LpSolve.LE, 1 - HaFattoUltimaReperibilità);
             Nrow++;                  
         }         	
	}
	
	private void DefinisciVincoliSuiTurni () throws LpSolveException
	{
		/**    
		 * 	 t1 = turno M1
		 *   t2 = turno M2
		 *   t3 = turno MP
		 *   t4 = turno P
		 *   t5 = turno N
		 *   
		 *   per ogni giorno:
		 *   		     
		 *   m1g1M1+m2g1M1+...+m7g1M1 = 1 
		 *   m1g1N+m2g1N+...+m7g1N = 1 
		 *   m1g1M2+m1g1M2+...+m7g1tM2 + m1g1MP+m2g1MP+...+m7g1MP = 1 
		 *   m1g1P+m1g1P+...+m7g1P + m1g1MP+m2g1MP+...+m7g1MP = 1 	     
	     */
		
		int[] colno;   //colno[i] indica l'i-esimo coefficiente non nullo del vincolo 
        double[] sparserow;  //sparserow[i] è il valore di tale coefficiente
        int count;   //count è il numero di coefficienti non nulli

        // va sempre coperto M1
        for (int g = 0; g < giorni; g++)
        {
            count = 1 + (g*turni*medici);
            colno = new int[Ncol+1];
            sparserow = new double[Ncol+1];
	         for (int m = 1; m <= medici; m++)
	   	     {
		         colno[count] = count; 
		         sparserow[count] = 1;  
		         count = count+turni; 
	         }
            lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 1);
            Nrow++;
        }
                 
        //va sempre coperto N
        for (int g = 0; g < giorni; g++)
        {
            count = 5 + (g*turni*medici);
            colno = new int[Ncol+1];
            sparserow = new double[Ncol+1];
	         for (int m = 1; m <= medici; m++)
	     	 {
		         colno[count] = count;
		         sparserow[count] = 1;  
		         count = count+turni;
	         }
            lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 1);
            Nrow++;
        }
        
        //va sempre coperto uno tra MP e M2
        for (int g = 0; g < giorni; g++)
        {
            count = 2 + (g*turni*medici);
            colno = new int[Ncol+1];
            sparserow = new double[Ncol+1];
	         for (int m = 1; m <= medici; m++)
	   	     {
		         colno[count] = count; //System.out.println("count: " + count + "   " + lp.getColName(count));
		         sparserow[count] = 1;  
		         count = count+turni;
	         }
            count = 3 + (g*turni*medici);
	         for (int m = 1; m <= medici; m++)
	   	     {
		         colno[count] = count; //System.out.println("count: " + count + "   " + lp.getColName(count));
		         sparserow[count] = 1;  
		         count = count+turni;
	         }
            lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 1);
            Nrow++;
        }
        
        //va sempre coperto uno tra MP e P
        for (int g = 0; g < giorni; g++)
        {
            count = 3 + (g*turni*medici);
            colno = new int[Ncol+1];
            sparserow = new double[Ncol+1];
	         for (int m = 1; m <= medici; m++)
	      	 {
		         colno[count] = count; 
		         sparserow[count] = 1;  
		         count = count+turni;
	         }
            count = 4 + (g*turni*medici);
	         for (int m = 1; m <= medici; m++)
	      	 {
		         colno[count] = count; 
		         sparserow[count] = 1;  
		         count = count+turni;
	         }
            lp.addConstraintex(count, sparserow, colno, LpSolve.EQ, 1);
            Nrow++;
        }       	
	}
	
	private void DefinisciCompatibilità (JTable compatibilità) throws LpSolveException
	{  
        for (int riga = 0; riga < compatibilità.getRowCount(); riga++) 
 	       for (int colonna = 2; colonna < compatibilità.getColumnCount(); colonna++) 
 	       {
 		       String prossimo = compatibilità.getValueAt(riga, colonna).toString();

 			   if (prossimo.equals("false")) 
 			   {
 		           for (int g = 0; g < giorni; g++)
 		           {	
 		              lp.setBounds(colonna - 2 + (turni*(riga)) + (g*turni*medici), 0, 0);
 		           }
 			       //System.out.println(compatibilità.getValueAt(riga, 0).toString() + " non fa i turni " + colonna-2);
 		       }
 		   }
	}
	
	private void DefinisciDati (JTable dati) throws LpSolveException
	{        
		for (int riga = 0; riga < dati.getRowCount(); riga++) 
	 	    for (int colonna = 1; colonna < dati.getColumnCount(); colonna++) 
	 	    {
	 		    String prossimo = dati.getValueAt(riga, colonna).toString();	
			    switch (prossimo)
			    {    
				    case "AP":	
				    {     
					    for (int t = 1; t <= turni; t++)					    
					        lp.setBounds(t + (turni*(riga)) + ((colonna-1)*turni*medici), 0, 0);				
				        break;
				    }

				    case "M1":	
				    {     
				        lp.setBounds(1 + (turni*(riga)) + ((colonna-1)*turni*medici), 1, 1);				
				        break;
				    }
				    case "M2":	
				    {     
				        lp.setBounds(2 + (turni*(riga)) + ((colonna-1)*turni*medici), 1, 1);						
				        break;
				    }
				    case "MP":	
				    {     
				        lp.setBounds(3 + (turni*(riga)) + ((colonna-1)*turni*medici), 1, 1);						
				        break;
				    }
				    case "P":	
				    {     
				        lp.setBounds(4 + (turni*(riga)) + ((colonna-1)*turni*medici), 1, 1);						
				        break;
				    }
				    case "N":	
				    {     
				        lp.setBounds(5 + (turni*(riga)) + ((colonna-1)*turni*medici), 1, 1);				
				        break;
				    }
				    case "E":	
				    {     
				        lp.setBounds(6 + (turni*(riga)) + ((colonna-1)*turni*medici), 1, 1);				
				        break;
			    	}				   
				    
				    default:
				    { 
			            //niente
				    } 
			    }
		}
	}
}
