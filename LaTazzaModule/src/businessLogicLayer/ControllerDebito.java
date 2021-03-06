package businessLogicLayer;
import businessLogicLayer.commandPkg.Command;
import dataAccessLayer.gatewaysPkg.IDao;
import dataAccessLayer.rowdatapkg.clientPkg.Personale;
import dataAccessLayer.rowdatapkg.movimentoPkg.Movimento;
import dataAccessLayer.rowdatapkg.movimentoPkg.MovimentoDebito;
import presentationLayer.LaTazzaApplication;
import utils.Euro;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import static businessLogicLayer.ObserverSubscriptionType.DEBITOLIST;
import static businessLogicLayer.commandPkg.Command.LaTazzaErrno.*;


public  final class ControllerDebito extends Observable {

    private IDao dao;
    private ControllerPersonale controllerPersonale;

    public   ControllerDebito(){
        dao=LaTazzaApplication.backEndInvoker.getDao();
        controllerPersonale=LaTazzaApplication.backEndInvoker.getControllerPersonale();
        this.setChanged();
    }


    private Command.LaTazzaErrno aggiungiMovimentoDebito(Personale p, Euro importo) throws NullPointerException{
        Date date=new Date();
        Movimento mp=new MovimentoDebito(new Timestamp(date.getTime()),p,importo);
        return dao.save(mp)?NOERROR:IMPORTOMAGGIOREDIDEBITO;
    }



    public Command.LaTazzaErrno registrarePagamentoDebito(Euro importo , String nome, String cognome )throws NullPointerException{
        Personale cliente= controllerPersonale.getPersonale(nome, cognome);
        Command.LaTazzaErrno exitStat=NOERROR;
        //Questa parte di codice è una "sezione critica" tra la consistenza
        // tra db e objs in ram
        //Utilizzo quindi una transazione
        dao.startTransaction();
            if((exitStat=aggiungiMovimentoDebito(cliente, importo))==NOERROR) {
                if (!cliente.pagamentoDebito(importo)) {//se non andato a buon fine devo fare rollback del db
                    dao.setTransactionStatus(false);//verrà eseguito quindi il rollback...
                    exitStat = IMPORTOMAGGIOREDIDEBITO;
                }
            }
        dao.endTransaction();
         //fine transazione
        this.setChanged();this.notifyObservers(DEBITOLIST);
        return exitStat;
    }


    public  boolean registrareAumentoDebito(Euro importo ,Personale personale)throws Euro.OverflowEuroException,NullPointerException{
        Personale cliente = controllerPersonale.getPersonale(personale);
        if (cliente == null) return false;
        cliente.aumentaDebito(importo);
        this.setChanged();this.notifyObservers(DEBITOLIST);
        return true;
    }


    public  List<Personale> esaminareDebitiPersonale(){
        List<Personale> list= controllerPersonale.getCopyList();
        list.removeIf(p -> Euro.compare(p.getImportoDebito(), new Euro(0, 0)) == 0);
        return list;
    }

}
