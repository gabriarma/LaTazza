import dataAccessLayer.database.DatabaseConnectionHandler;
import dataAccessLayer.database.config.*;
import utils.PathHandler;

import java.io.FileReader;
import java.sql.*;
import java.util.Scanner;


public class TriggersTest {

    private Connection conn;
    private DatabaseConnectionHandler database;
    private static final String userDir=System.getProperty("user.dir");
    private final String PATHConfig="\\test\\";
    private final String PATHInsert="\\test\\";
    private Scanner inFile;
    private static ResultSet rs;
    private static PreparedStatement stat;



    public TriggersTest() throws SQLException, ClassNotFoundException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        database= new DatabaseConnectionHandler("databaseTest","jdbc:h2:mem:","");
        database.initDataBase();
        conn = database.getConnection();
    }


    private void updateTable(String path,String sqlFileName) throws SQLException {

        try {

            StringBuilder file= new StringBuilder();
            //System.out.println("Path: "+System.getProperty("user.dir")+sqlFileName);
            inFile= new Scanner(new FileReader(PathHandler.modifyPath(userDir+path+sqlFileName)));
            while(inFile.hasNext()) {
                file.append(inFile.nextLine()).append("\n");
            }
            //System.out.println("----------------------------------\n"+file.toString()+"\n------------------------");
            PreparedStatement stmt=conn.prepareStatement(file.toString());
            stmt.execute();
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
            inFile.close();
            conn.close();
        }

    }




    DatabaseConnectionHandler getDatabase(){return database;}

    public Connection getConn() {
        return conn;
    }

    public void initDataBase() throws SQLException {
        updateTable(PATHConfig, "databaseConfig.sql");
        TriggerCheckNumCialde.initTrigger(conn);
        TriggerCompraVisitatore.initTrigger(conn);
        ViewMagazzino.initView(conn);
        ViewDebito.initView(conn);
        ViewCassa.initView(conn);
        updateTable(PATHInsert, "Insert.sql");
    }

    public void closeConnection() throws SQLException {
        database.closeDataBase();
    }

    private void executeSelect(String table) throws SQLException{
        stat=conn.prepareStatement("SELECT * from " + table);
        rs=stat.executeQuery();
        while(rs.next()){
            System.out.println(rs.getString(1));
        }
    }



}