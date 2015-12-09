/**
 * Pianificatore turni per l'Ospedale di Crema
 * 
 * Versione 1.0
 * 10 dicembre 2015
 * dario.bezzi@studenti.unimi.it
 */

/*
 * FinestraPrincipale.java contiene il main e richiede:
 * - BottoneMedici.java
 * - BottonePianifica.java
 * - BottoneStorico.java
 */

import java.awt.event.*;
import java.awt.*;

import javax.swing.*;

@SuppressWarnings("serial")
class FinestraPrincipale extends JFrame implements ActionListener 
{
  //private static final long serialVersionUID = 1L;	
  private MyFrame frame;
  private boolean frameAperto;

  private TextArea Hello = new TextArea("Pianificatore turni per il reparto rianimazione dell'Ospedale di Crema \n\n"
  		+ "Versione 1.0, dicembre 2015 \n"
  		+ "Dario Bezzi \n"
  		+ "dario.bezzi@studenti.unimi.it \n");
  
  FinestraPrincipale() 
  {
    setTitle("Menu principale");  

    JPanel buttonPane = new JPanel(new GridLayout(2,2));
    Hello.setEditable(false);
    
    JScrollPane areaScrollPane = new JScrollPane(Hello);
    areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    areaScrollPane.setPreferredSize(new Dimension(400, 150));
    
    JButton b[] = 
    {
    	 new JButton("Istruzioni per l'uso"),
         new JButton("Pianificazione turni"),
         new JButton("Elenco medici"),
         new JButton("Archivio piani"),
    };
    
	for(int i = 0; i < 4; ++i)
        buttonPane.add(b[i]);

    add(buttonPane, BorderLayout.SOUTH);
    add(areaScrollPane, BorderLayout.CENTER);
    
	for(int i = 0; i < 4; ++i)
	    b[i].addActionListener(this); 
    
    addWindowListener(new MyWindowAdapter());

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  }
  
  
  public void actionPerformed(ActionEvent ae) 
  {
    // Se un frame è aperto evito di farne aprire un successivo
    if (frameAperto) return;
    
    frameAperto = true;
    String comando = ((JButton)ae.getSource()).getText();
    frame = new MyFrame(comando,this);

    switch (comando)
	{
		case "Istruzioni per l'uso":	
		{
	        TextArea ContentPane = new TextArea("***MANUALE***");
	        ContentPane.setEditable(false);
            frame.add(ContentPane);
			break;
		}

		case "Pianificazione turni": 
		{
			BottonePianifica ContentPane = new BottonePianifica();  
	        ContentPane.setOpaque(true);
	        frame.setContentPane(ContentPane);
	        
			break;
		}
		case "Elenco medici": 
		{
	        BottoneMedici ContentPane = new BottoneMedici();
	        ContentPane.setOpaque(true);
	        frame.setContentPane(ContentPane);
	        
			break;
		}

		case "Archivio piani": 
		{
	        BottoneStorico ContentPane = new BottoneStorico();
	        ContentPane.setOpaque(true);
	        frame.setContentPane(ContentPane);
			break;
		}
	}
    
    frame.setSize(800,500);
    frame.setVisible(true);
  }
 
  
  // Viene chiamato alla chiusura del frame da parte dell'altra classe
  void frameChiuso() 
  {
      frameAperto = false;
  }
  
  // Classe interna per gestire la chiusura del Frame principale
  private class MyWindowAdapter extends WindowAdapter
  {
      public void windowClosing(WindowEvent we)
      {
          if(frameAperto)
          {
              JOptionPane.showMessageDialog(null,"Chiudere prima tutte le altre finestre","Attenzione",JOptionPane.WARNING_MESSAGE);
              return;
          }
          System.exit(0);
      }
  }
  
  class MyFrame extends JFrame 
  {
	  private FinestraPrincipale frame;
	  
	  MyFrame(String text, FinestraPrincipale frame) 
	  {
	      super(text);
	      this.frame = frame;	
	    
	      addWindowListener(new MyWindowListener1());
	  }
	  	  
	  private class MyWindowListener1 extends WindowAdapter 
	  {
	      public void windowClosing(WindowEvent we) 
	      {        
	          frame.frameChiuso();
	          dispose();
	      }
	  }
	  
  }
  
  
  	public static void makeGUI() 
  	{
  		FinestraPrincipale fin = new FinestraPrincipale();
  		fin.pack();
  		fin.setVisible(true);
  	}
  
  	public static void main(String[] args) 
  	{
  		try 	
  		{
  			SwingUtilities.invokeAndWait
  			(
                new Runnable()
  			    {
  				    public void run() 
  				    {
  					    makeGUI();
  				    }
  			    }
            );
  		}
  	    catch(Exception e) {}
  	}
}

