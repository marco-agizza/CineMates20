package com.SoftEngUniNA.CineMates20Desktop.controllers;

import com.SoftEngUniNA.CineMates20Desktop.models.Operator;
import com.SoftEngUniNA.CineMates20Desktop.views.AuthenticationPage;
import com.SoftEngUniNA.CineMates20Desktop.views.MainPage;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class AuthenticationController {
    
    public static void loginButtonPressed(String username, String password, AuthenticationPage page) {
        try {
            Firestore dbconn= Connection.newConnection();
            CollectionReference colRef=dbconn.collection("operators");
            ApiFuture<QuerySnapshot> qlist= colRef.whereEqualTo("username", username).get();
            List<QueryDocumentSnapshot> docs= qlist.get().getDocuments();
            List<Operator> amministratoriList= new ArrayList<>();
            for(DocumentSnapshot document: docs){
                Operator operator= document.toObject(Operator.class);
                amministratoriList.add(operator);
            }
            if(!amministratoriList.isEmpty()){
                Operator operator= amministratoriList.get(0);
                if(operator.login(username,password)){
                    new MainPage(username).setVisible(true);
                    page.dispose();
                }else
                    JOptionPane.showMessageDialog(null, "Password errata", "Errore", JOptionPane.ERROR_MESSAGE);
            }else
                JOptionPane.showMessageDialog(null, "Operatore inesistente", "Errore", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(AuthenticationPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static boolean verifyEmptyFields(String username, String password){
        return username.isEmpty() || password.isEmpty();
    }
}