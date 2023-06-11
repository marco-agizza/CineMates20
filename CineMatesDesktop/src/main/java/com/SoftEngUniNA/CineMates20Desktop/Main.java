package com.SoftEngUniNA.CineMates20Desktop;
import com.SoftEngUniNA.CineMates20Desktop.controllers.Connection;
import static com.SoftEngUniNA.CineMates20Desktop.controllers.Connection.newConnection;
import com.SoftEngUniNA.CineMates20Desktop.views.AuthenticationPage;
import com.google.cloud.firestore.Firestore;

public class Main {
    public static void main(String[] args) {
        // all jframe' s with look'n feel windows 
        /*try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AuthenticationPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }*/
        new AuthenticationPage().setVisible(true);
    }
    
}
