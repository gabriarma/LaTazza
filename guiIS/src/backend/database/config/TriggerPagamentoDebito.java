package backend.database.config;

import org.h2.api.Trigger;

import java.sql.*;


public class TriggerPagamentoDebito extends TriggerDebito implements Trigger {

    private static final String TRIGGER_PATH = "\"backend.database.config.TriggerPagamentoDebito\"";
    private static final String TABLE_NAME_DEBITO = "LATAZZASCHEMA.DEBITO";
    private static final String TABLE_NAME = "LATAZZASCHEMA.PAGAMENTO_DEBITO";
    private static final String TRIGGER_NAME = "Update_Table_Debito";
    private static final String CREATE_TRIGGER = "CREATE TRIGGER " + TRIGGER_NAME + " AFTER INSERT ON " + TABLE_NAME + " FOR EACH ROW CALL " + TRIGGER_PATH;

    private static double getDebitoPagato(Connection conn, Object[] newRow) throws SQLException {


        PreparedStatement stat = conn.prepareStatement("select importo " +
                "from " + TABLE_NAME +
                " where nome=? and cognome=? and data= ? ");

        stat.setNString(1, (String) newRow[0]);
        stat.setNString(2, (String) newRow[1]);
        System.out.println("NewRo0: "+ newRow[0]+" NewRow1: "+newRow[1]+" NewRow2: "+newRow[3]);

        stat.setTimestamp(3, (Timestamp) newRow[2]);
        ResultSet rs = stat.executeQuery();
        if(rs.next()) {rs.getDouble(1); System.out.println("debito Pagato: "+rs.getDouble(1));}
            return 0.0;
    }

    @Override
    public void init(Connection connection, String s, String s1, String s2, boolean b, int i) throws SQLException {

    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {

        double debitoAggiornato=getCurrentDebito(conn,newRow)-getDebitoPagato(conn,newRow);

        System.out.println("Debito Aggiornato: "+debitoAggiornato);
        PreparedStatement stat= conn.prepareStatement("update "+TABLE_NAME_DEBITO+" set importo="
                +debitoAggiornato+" where nome=? and cognome=? ");
        stat.setNString(1, (String) newRow[0]);
        stat.setNString(2, (String) newRow[1]);
        stat.executeUpdate();


        /*
        ResultSet rs;
        PreparedStatement prep;
        prep=conn.prepareStatement("select *" +
                "from LATAZZASCHEMA.DEBITO " );
        rs=prep.executeQuery();
        while(rs.next())
            System.out.println("2 : \n"+rs.getString(1) + ", " + rs.getString(2)+", "+rs.getDouble(3));
        */

    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public void remove() throws SQLException {

    }

    public static void initView(Connection conn) {
        Statement stat = null;
        try {
            stat = conn.createStatement();
            stat.execute(CREATE_TRIGGER);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
