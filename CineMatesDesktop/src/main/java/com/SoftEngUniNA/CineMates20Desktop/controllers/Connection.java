package com.SoftEngUniNA.CineMates20Desktop.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;

import java.io.IOException;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection {
    public static Firestore newConnection() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
        FileInputStream serviceAccount
                = new FileInputStream("credentials.json");
        
        @SuppressWarnings("deprecation")
        FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://cinemates-1.firebaseio.com/")
            .build();
        
        FirebaseApp app;
        
        if(FirebaseApp.getApps().isEmpty()){
            app= FirebaseApp.initializeApp(options);
        }else{
            app= FirebaseApp.getInstance();
        }              
        
        Firestore db= FirestoreClient.getFirestore(app);
        return db;
       
    }


    public static void newRealtimeDBConnection(){
        FileInputStream refreshToken = null;
        try {
            refreshToken = new FileInputStream("credentials.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(refreshToken))
                    .setDatabaseUrl("https://cinemates-1.firebaseio.com/")
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                refreshToken.close();
            } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
/*public class Connection {
    public static Firestore newConnection(){
        FileInputStream serviceAccount= null;
        try{
            serviceAccount=new FileInputStream("credentials.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://cinemates-1.firebaseio.com/")
            .build();
            
            FirebaseApp.initializeApp(options);
     
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } 
       
        return null;
    }
}*/

