package presentationLayer.guiLogicPkg.contentsPanelsPkg;

import businessLogicLayer.ControllerContabilita;
import businessLogicLayer.ControllerDebito;
import dataAccessLayer.rowdatapkg.CialdeEntry;
import dataAccessLayer.rowdatapkg.clientPkg.Personale;
import presentationLayer.guiConfig.contentsPanelsPropertiesPkg.StatoPaneProperties;
import presentationLayer.LaTazzaApplication;
import utils.Euro;
import utils.MyJLabel;
import javax.swing.*;
import java.util.*;
import static presentationLayer.guiConfig.contentsPanelsPropertiesPkg.StatoPaneProperties.*;
import static businessLogicLayer.ObserverSubscriptionType.CONTABILITALIST;
import static businessLogicLayer.ObserverSubscriptionType.DEBITOLIST;


public class StatoPane extends AbstractPanel {

    private MyJLabel labelDebitiPersonale;
    private MyJLabel labelCassa;
    private MyJLabel labelTitolo;
    private MyJLabel labelMagazzino;
    private MyJLabel labelSaldo;
    private JPanel panelMagazzino;
    private JPanel panelCassa1;
    private JPanel panelDebiti1;
    private JTextArea debitiPersonaleTextArea;
    private JScrollPane scrollPane, scrollPaneMagazzino;
    private  JTextArea statoMagazzino;

    public StatoPane() {

        //inizializza tutti i campi necessari
		super(1L,DEFAULT_LINKDESCRIPTION,DEFAULT_PANELNAME);
		StatoPaneProperties.initStatoPanel(this);
        add(labelTitolo);
        add(panelMagazzino=StatoPaneProperties.createAndInitPanelMagazzino());
        panelMagazzino.add(labelMagazzino=StatoPaneProperties.createAndInitLabelMagazzino());
		add(panelCassa1=StatoPaneProperties.createAndInitPanelCassa1());
        panelCassa1.add(labelCassa=StatoPaneProperties.createAndInitLabelCassa());
        add(labelSaldo=StatoPaneProperties.createAndInitLabelSaldo());
        add(panelDebiti1=StatoPaneProperties.createAndInitPanelDebiti1());
        panelDebiti1.add(labelDebitiPersonale=StatoPaneProperties.createAndInitLabelDebitiPersonale());
        debitiPersonaleTextArea=StatoPaneProperties.createAndInitDebitiPersonaleTextArea();
        add(scrollPane=createAndInitScrollPane(debitiPersonaleTextArea));

        statoMagazzino=StatoPaneProperties.createAndInitStatoMagazzinoTextArea();
        add(scrollPaneMagazzino=createAndInitScrollPaneStatoMagazzino(statoMagazzino));

        LaTazzaApplication.backEndInvoker.addObserver(DEBITOLIST,this);
        LaTazzaApplication.backEndInvoker.addObserver(CONTABILITALIST,this);
	}


    public void setCialdeList(Map<CialdeEntry,Integer> listaCialde){

        statoMagazzino.setText(null);
        for (CialdeEntry s : listaCialde.keySet())
        {
            statoMagazzino.append(s.getTipo()+" "+ listaCialde.get(s)+"\n");
        }
    }

    public void setDebitiPersonaleTextArea(List<Personale> listaPersonale){
        debitiPersonaleTextArea.setText(null);
        for(Personale i:listaPersonale){
            int centesimi=i.getImportoDebito().getCentesimi();
            String cs=centesimi<10?"0"+centesimi: String.valueOf(centesimi);
            debitiPersonaleTextArea.append(
                    i.getNome()+
                            " "+
                            i.getCognome()+
                            " "+
                            i.getImportoDebito().getEuro()+
                                    "."+
                            cs+"\n"

            );

        }

    }


    public void setLabelTitolo(MyJLabel labelTitolo) {
        this.labelTitolo = labelTitolo;
    }


    public void setCassa(Euro euroCassa) {
        int citti=euroCassa.getCentesimi();
        if(citti<10){
            labelSaldo.setText(euroCassa.getEuro() + ".0" + euroCassa.getCentesimi());

        }else {
            labelSaldo.setText(euroCassa.getEuro() + "." + euroCassa.getCentesimi());
        }
    }


    @Override
    public void update(Observable o, Object arg) {
        if(arg ==DEBITOLIST){
            setDebitiPersonaleTextArea(((ControllerDebito)o).esaminareDebitiPersonale());
        }else if(arg==CONTABILITALIST){
            setCialdeList(((ControllerContabilita)o).statoMagazzino());
            setCassa(((ControllerContabilita)o).statoCassa());
        }
    }
}
