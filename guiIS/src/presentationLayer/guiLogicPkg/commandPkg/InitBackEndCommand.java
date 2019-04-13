package presentationLayer.guiLogicPkg.commandPkg;

import backend.businessLogicLayer.Cassa;
import backend.businessLogicLayer.ControllerCialde;
import backend.businessLogicLayer.ControllerContabilita;
import backend.businessLogicLayer.ControllerPersonale;
import backend.dataAccessLayer.gatewaysPkg.DaoInvoker;
import backend.dataAccessLayer.gatewaysPkg.receiverPkg.*;
import backend.dataAccessLayer.rowdatapkg.AbstractEntryDB;
import backend.dataAccessLayer.rowdatapkg.CialdeEntry;
import backend.dataAccessLayer.rowdatapkg.MagazzinoEntry;
import backend.dataAccessLayer.rowdatapkg.RifornimentoEntry;
import backend.dataAccessLayer.rowdatapkg.clientPkg.Personale;
import backend.dataAccessLayer.rowdatapkg.clientPkg.Visitatore;
import backend.dataAccessLayer.rowdatapkg.movimentoPkg.MovimentoDebito;
import backend.dataAccessLayer.rowdatapkg.movimentoPkg.MovimentoVendita;
import backend.database.ConfigurationDataBase;
import backend.database.DatabaseConnectionHandler;
import javafx.util.Pair;
import presentationLayer.guiLogicPkg.BackEndInvoker;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

public class InitBackEndCommand implements  Command{
    private BackEndInvoker backEndInvoker;

    public InitBackEndCommand(BackEndInvoker backEndInvoker){
        this.backEndInvoker = backEndInvoker;
    }

    @Override
    public boolean execute() throws SQLException, ClassNotFoundException {

        backEndInvoker.setDatabaseConnectionHandler(new DatabaseConnectionHandler());
        backEndInvoker.getDatabaseConnectionHandler().initDataBase();
        backEndInvoker.setDao(new DaoInvoker(backEndInvoker.getDatabaseConnectionHandler().getConnection(),daoCollection));
        ConfigurationDataBase db= new ConfigurationDataBase(backEndInvoker.getDatabaseConnectionHandler());

        if(!db.existsSchema()) {
            db.createSchema();
            db.initTriggers();
            db.inserimentiIniziali();
        }
        backEndInvoker.setControllerCialde(new ControllerCialde());
        backEndInvoker.setControllerContabilita(new ControllerContabilita());
        backEndInvoker.setControllerPersonale(new ControllerPersonale());
        return true;
    }


    /**
     * Mappa dove viene creata l'associazione tra le classi della businessLogic
     * e le classi *DaoReceiver  per il database (contengono il codice per l'interazione con le tabelle del db)
     * La mappa viene usata dalla class SimpleDaoReceiverFactory
     */
    private static Collection<Pair<Class<? extends AbstractEntryDB>,Class<? extends AbstractDaoReceiver>>> daoCollection
            =new LinkedList<Pair<Class<? extends AbstractEntryDB>, Class<? extends AbstractDaoReceiver>>>(){{
        add(new Pair<>(RifornimentoEntry.class,RifornimentoDaoReceiver.class));
        add(new Pair<>(CialdeEntry.class,CialdeDaoReceiver.class));
        add(new Pair<>(MovimentoDebito.class,MovimentoDebitoDaoReceiver.class));
        add(new Pair<>(MovimentoVendita.class,MovimentoVenditaDaoReceiver.class));
        add(new Pair<>(Personale.class,PersonaleDaoReceiver.class));
        add(new Pair<>(Visitatore.class,VisitatoreDaoReceiver.class));
        add(new Pair<>(Cassa.class,CassaDaoReceiver.class));
        add(new Pair<>(MagazzinoEntry.class,MagazzinoDaoReceiver.class));

    }};
}
