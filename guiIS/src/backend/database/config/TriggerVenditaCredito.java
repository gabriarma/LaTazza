package backend.database.config;

import org.h2.api.Trigger;
import utils.Euro;

import java.sql.*;

public class TriggerVenditaCredito extends ViewDebito implements Trigger {

    private static final String TRIGGER_PATH="\"backend.database.config.MaterializedViewDebito\"";
    private static final String TABLE_NAME_DIPENDENTE="LATAZZASCHEMA.COMPRA_DIPENDENTE";
    private static final String TABLE_NAME_CIALDE="LATAZZASCHEMA.CIALDE";
    private static final String TRIGGER_NAME="Update_Table_Debiti_Pagati";
    private static final String CREATE_TRIGGER_STATEMENT_DEBITO = "CREATE TRIGGER " + TRIGGER_NAME+ " AFTER INSERT ON "+TABLE_NAME_DIPENDENTE+" FOR EACH ROW CALL "+TRIGGER_PATH;



    private static double getPrezzo( Object[] newRow) throws SQLException {

        ResultSet rs;
        PreparedStatement stat= connection.prepareStatement("select prezzo " +
                "from " + TABLE_NAME_CIALDE+" where tipo=?" );

        stat.setNString(1, (String) newRow[2]);
        rs= stat.executeQuery();
        if(rs.next()) return rs.getDouble(1);
        return 0.0;
    }


    private static Euro getNewDebito(Object[] newRow)  throws SQLException{

        PreparedStatement stat= connection.prepareStatement("select numero_cialde " +
                                                             "from " + TABLE_NAME_DIPENDENTE +
                                                                " where contanti=false and nome=? and cognome=? and data=?" );
        stat.setNString(1, (String) newRow[0]);
        stat.setNString(2, (String) newRow[1]);
        stat.setTimestamp(3, (Timestamp) newRow[5]);
        ResultSet rs= stat.executeQuery();
        //getCurrentDebito( newRow)+(rs.getInt(1)*getPrezzo(newRow)));
        if(rs.next()) return new Euro(0,0);
        return new Euro(0,0);//se la tupla cercata nella select viene precedentemente eliminata dal trigger CheckNumCialde
    }



    private static boolean isInDebit( String nome, String cognome) throws SQLException {
        ResultSet rs;
        PreparedStatement stat= connection.prepareStatement("select * " +
                "from "+ TABLE_NAME_DEBITO + " where nome='"+ nome+"' and cognome='"+cognome+"'");
        rs= stat.executeQuery();
        return rs.next();
    }

    private static void insertIfNotExist( String nome, String cognome) throws SQLException {

        PreparedStatement stat;
        if(!isInDebit( nome, cognome)){
            stat= connection.prepareStatement("insert into "+TABLE_NAME_DEBITO+" VALUES('"+nome+"', '"+cognome+"',0, 0 )");
            stat.executeUpdate();
        }

    }

    @Override
    public void init(Connection connection, String s, String s1, String s2, boolean b, int i) throws SQLException {
        this.connection=connection;
    }


    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {

        insertIfNotExist((String) newRow[0],(String) newRow[1]);
        Euro importo=getNewDebito( newRow);
        PreparedStatement stat= conn.prepareStatement("update "+TABLE_NAME_DEBITO+" set euro= "
                                                            +importo.getEuro()+" ,centesimi= "+ importo.getCentesimi()+"  where nome=? and cognome=? ");

        stat.setNString(1, (String) newRow[0]);
        stat.setNString(2, (String) newRow[1]);
        stat.executeUpdate();
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public void remove() throws SQLException {

    }


    public static void initTrigger(Connection conn){
        Statement stat= null;
        try {
            stat = conn.createStatement();
            stat.execute(CREATE_TRIGGER_STATEMENT_DEBITO);
            stat.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
