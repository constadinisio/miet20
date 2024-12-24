package conexion;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class conexionMysql {

    Connection enlazar = null;

    /*
    
    String bd = "et20plataforma";
    String url = "jdbc:mysql://172.16.26.190:3306/";
    String user = "et20tics";
    String password = "laboratoriotics";
    String driver = "com.mysql.cj.jdbc.Driver";
    
    */
    
    String bd = "et20plataforma";
    String url = "jdbc:mysql://localhost/";
    String user = "root";
    String password = "";
    String driver = "com.mysql.cj.jdbc.Driver";
    Connection cx;
    PreparedStatement ps;

    public Connection conectar() {
        try {
            Class.forName(driver);
            cx = DriverManager.getConnection(url + bd, user, password);
            System.out.println("Se conecto a DB... " + bd);
        } catch (SQLException ex) {
            Logger.getLogger(conexionMysql.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No se conecto a DB... " + bd);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(conexionMysql.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cx;
    }

}
