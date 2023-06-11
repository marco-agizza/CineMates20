package com.SoftEngUniNA.CineMates20Desktop.controllers;

import static com.SoftEngUniNA.CineMates20Desktop.controllers.Connection.newConnection;
import com.SoftEngUniNA.CineMates20Desktop.models.*;
import com.SoftEngUniNA.CineMates20Desktop.views.AuthenticationPage;
import com.SoftEngUniNA.CineMates20Desktop.views.ConfirmDialog;
import com.SoftEngUniNA.CineMates20Desktop.views.MainPage;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class StatsPageController {
    private int reviews_number=0, lists_number=0, number_of_accesses=0, user_searches_number=0, movie_searches_number=0;
    private float rating_sum=0, movies_sum=0;
    private boolean there_are_users=false, there_are_reviews=false;
    
    private final float[] stats=new float[9];
    private final Map<String,Review> reviews=new HashMap<>();
    private final Map<String,User> users=new HashMap<>();
    private final Map<String,Set<MovieList>> lists=new HashMap<>();//<-- per ogni utente metto tutte le liste
    private Firestore db=null;
    private final Object lock=new Object();
    private final MainPage page;
    
    public StatsPageController(MainPage page) {
        this.page=page;
    }
    
    
    /*
    stats
    0: accesses; 1: ext_prov_users; 2: email_users; 3: users_searches; 4:movies_searches;
    5: reviews; 6: avg_rating_review; 7: lists; 8:avg_number_of_movies
    */
    
    public void takeSystemStats(){
        syncOnAuth();
        try{
            db= newConnection();
            db.collection("enabled_collections").document("collection_names").addSnapshotListener(new EventListener<DocumentSnapshot>(){
                @Override
                public void onEvent(DocumentSnapshot val, FirestoreException error) {
                    synchronized(lock){
                        if(there_are_reviews!=(Boolean)val.get("reviews")){
                            there_are_reviews=!there_are_reviews;
                            if(there_are_reviews)
                                syncOnMovies();
                        }
                        if(there_are_users!=(Boolean)val.get("users")){
                            there_are_users=!there_are_users;
                            if(there_are_users)
                                syncOnUsers();
                        }
                    }
                }
            });
        }catch(IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(StatsPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    private void syncOnAuth(){
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference("/users");
        System.out.println("Instaurata prima connessione con realtime db");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                synchronized(lock){
                    int ext_prov_users=0, email_users=0;
                    try{
                        /*Iterate through all users. This will still retrieve users in batches,
                        buffering no more than 1000 users in memory at a time.*/
                        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
                        for (ExportedUserRecord user : page.iterateAll()) {
                            UserInfo[] nonso= user.getProviderData();
                            String provider=nonso[0].getProviderId();
                            System.out.println("Trovato utente con provider: "+provider);
                            if(provider.compareTo("google.com")==0 || provider.compareTo("facebook.com")==0){
                                ext_prov_users++;
                            }else{
                                email_users++;                            
                            }
                        }
                        stats[1]=ext_prov_users;
                        stats[2]=email_users;                   
                    }catch(FirebaseAuthException ex) {
                        Logger.getLogger(MainPage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    page.showStatsPanel(stats);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                throw new UnsupportedOperationException("Not supported yet.");              
            }
        });

    }
    private void syncOnMovies(){
        db.collection("reviews").addSnapshotListener(new EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(QuerySnapshot snapshots, FirestoreException e) {
                synchronized(lock){
                    //ogni snapshot è un film
                    for(DocumentChange doc_ch:snapshots.getDocumentChanges()){
                        switch(doc_ch.getType()){
                            case ADDED:
                                if((Boolean)doc_ch.getDocument().get("hasreviews"))
                                        syncOnReviews(doc_ch.getDocument().getId());
                                break;
                        }
                    }
                }
            }
        });
    }
    private void syncOnReviews(String movie_id){
        db.collection("reviews/"+movie_id+"/review").addSnapshotListener(new EventListener<QuerySnapshot>(){
            public void onEvent(QuerySnapshot value, FirestoreException error) {
                synchronized(lock){
                    for(DocumentChange dc:value.getDocumentChanges()){
                        switch(dc.getType()){
                            case ADDED:
                                //System.out.println("è stato aggiunta una recensione con id"+ dc.getDocument().getId());
                                Review new_review=dc.getDocument().toObject(Review.class);
                                reviews.put(dc.getDocument().getId(), new_review);
                                reviews_number++;
                                rating_sum+=new_review.getRating();
                                break;
                            case REMOVED:
                                //System.out.println("è stata eliminata una recensione con id"+ dc.getDocument().getId());
                                Review del_review=dc.getDocument().toObject(Review.class);
                                reviews.remove(dc.getDocument().getId());
                                reviews_number--;
                                rating_sum-=del_review.getRating();
                                break;
                            case MODIFIED:
                                //System.out.println("è stato modificata una recensione con id"+ dc.getDocument().getId());
                                Review upd_review=dc.getDocument().toObject(Review.class);
                                Review old_review=reviews.get(dc.getDocument().getId());
                                reviews.put(dc.getDocument().getId(), upd_review);
                                rating_sum=rating_sum-old_review.getRating()+upd_review.getRating();
                                break;
                        }
                        stats[5]=reviews_number;
                        stats[6]= (reviews_number==0)? 0 : rating_sum/reviews_number;
                    }
                    page.showStatsPanel(stats);
                }
            }
        });
    }
    private void syncOnUsers(){
        db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(QuerySnapshot value, FirestoreException error) {
                synchronized(lock){
                    for(DocumentChange dc:value.getDocumentChanges()){
                        switch(dc.getType()){
                            case ADDED:
                                //creo un EventListener per ogni utente
                                //System.out.println("Ho trovato l'utente "+ dc.getDocument().getId()+" con "+dc.getDocument().get("accesses")+" accessi");
                                //aggiungo l'utente alla map degli utenti
                                User new_user=dc.getDocument().toObject(User.class);
                                //System.out.println("----Inserisco nella mappa l'utente "+ new_user);
                                users.put(dc.getDocument().getId(), new_user);
                                number_of_accesses+=Integer.parseInt(dc.getDocument().get("accesses").toString());
                                lists_number+=2;
                                Set<MovieList> user_lists=new HashSet<>();
                                MovieList favourites=new MovieList("favourites",null,true);
                                user_lists.add(favourites);
                                lists.put(dc.getDocument().getId(), user_lists);
                                
                                Iterable<CollectionReference> collections= db.collection("users").document(dc.getDocument().getId()).listCollections();
                                //System.out.println("Le raccolte che ha sono:");
                                for(CollectionReference coll:collections){
                                    //System.out.println(coll.getId());
                                    //creo un event listener per le raccolte che mi interessano: lists, searches, favourites
                                    if(coll.getId().compareTo("lists")==0)
                                        syncOnLists(dc.getDocument().getId());
                                    else if(coll.getId().compareTo("searches")==0)
                                        syncOnSearches(dc.getDocument().getId());
                                    else if(coll.getId().compareTo("favourites")==0)
                                        syncOnFavourites(dc.getDocument().getId());
                                }
                                break;
                            case MODIFIED:
                                //l'utente è stato modificato... i campi che possono essere stati modificati sono:accesses, hasfavourites, haslists, hassearches
                                System.out.println("L'utente è stato modificato!");
                                //capisco quale è stata la modifica:
                                User modified_user=users.get(dc.getDocument().getId());
                                User updated_user=dc.getDocument().toObject(User.class);
                                System.out.println("il numero di accessi in precedenza era "+modified_user.getAccesses()+" ma adesso è "+ updated_user.getAccesses());
                                //trovato l'utente modificato all'interno della lista degli utenti
                                if(modified_user.getAccesses()!=updated_user.getAccesses()){
                                    System.out.println("nuovo accesso!");
                                    number_of_accesses++;
                                    stats[0]=number_of_accesses;
                                    page.showStatsPanel(stats);
                                }else if(modified_user.isHasfavourites()!=updated_user.isHasfavourites()){
                                    //è cambiato hasfavourites e l'unico modo in cui può cambiare è da false a true
                                    syncOnFavourites(dc.getDocument().getId()); 
                                }else if(modified_user.isHaslists()!=updated_user.isHaslists()){
                                    //è stato modificato il campo haslists e quindi è passato a true
                                    syncOnLists(dc.getDocument().getId());
                                }else if(modified_user.isHassearches()!=(Boolean)dc.getDocument().get("hassearches")){
                                    syncOnSearches(dc.getDocument().getId());
                                }
                                //aggiorno l'utente all'interno della map
                                users.put(dc.getDocument().getId(), updated_user);
                                break;
                            case REMOVED: //in realtà una volta che un utente entra in firestore non può essere rimosso
                                number_of_accesses-=(Integer)dc.getDocument().get("accesses");
                                lists_number-=2;
                                break;
                        }
                    }
                    stats[0]=number_of_accesses;
                    stats[7]=lists_number;
                    stats[8]=movies_sum/lists_number;
                    page.showStatsPanel(stats);
                }
            }
        });
    }
    private void syncOnLists(String uid){
        db.collection("users/"+uid+"/lists").addSnapshotListener(new EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(QuerySnapshot value, FirestoreException error) {
                synchronized(lock){
                    for(DocumentChange doc_ch:value.getDocumentChanges()){
                        switch(doc_ch.getType()){
                            case ADDED:
                                System.out.println("Lista personale "+doc_ch.getDocument().getId()+" aggiunta con descrizione \""+(String)doc_ch.getDocument().get("description")+"\" e con movies="+(Boolean)doc_ch.getDocument().get("movies"));
                                //qui devo recuperare la lista di MovieList dell' utente uid dalla map lists e 
                                //vi aggiungo la lista aggiunta
                                Set<MovieList> user_lists=lists.get(uid);
                                MovieList new_list=new MovieList(doc_ch.getDocument().getId(), (String)doc_ch.getDocument().get("description"),(Boolean)doc_ch.getDocument().get("movies"));
                                user_lists.add(new_list);
                                lists.put(uid, user_lists);//qui dovrei verificare se ha senso o basta inserire la MovieList nella lista risultante dal get
                                //è stata aggiunta una lista personalizzata o movie to watch
                                if(doc_ch.getDocument().getId().compareTo("Film_da_vedere")!=0)
                                    lists_number++;
                                if((Boolean)doc_ch.getDocument().get("movies")==true){
                                    System.out.println("è stata aggiunta una lista con movies=true");
                                    syncOnMovieList(uid, doc_ch.getDocument().getId());
                                }
                                break;
                            case MODIFIED:
                                MovieList modified_list=new MovieList(doc_ch.getDocument().getId(), (String)doc_ch.getDocument().get("description"), (Boolean)doc_ch.getDocument().get("movies"));
                                //è rilevante solo la modifica di movie e non anche di description
                                //al fine di conoscere quale campo è cambiato conservo in una Map per ogni utente 
                                // le sue liste. Il syncOnMovieList va fatto solo se movies è stato settato a true(l'app mobile
                                // è costruita in modo che questo set a true avvenga una sola volta)
                                Set<MovieList> knowed_lists=lists.get(uid);
                                MovieList list_to_update=null;
                                for(MovieList curr_list:knowed_lists){
                                    if(curr_list.getTitle().compareTo(modified_list.getTitle())==0){
                                        //trovata la lista modificata... confrontandola con doc_ch.getDocument() posso 
                                        //conoscere l'entità della modifica
                                        list_to_update=curr_list;
                                        if(modified_list.getDecription().compareTo(curr_list.getDecription())==0){
                                            //non è stata cambiata la descrizione
                                            System.out.println("La lista "+doc_ch.getDocument().getId()+" è stata modificata e adesso ha campo movies="+(Boolean)doc_ch.getDocument().get("movies"));
                                            if((Boolean)doc_ch.getDocument().get("movies")==true)
                                                syncOnMovieList(uid, doc_ch.getDocument().getId());
                                        }
                                    }
                                }
                                //inserisco la MovieList aggiornata in knowed_lists e knowed_lists nella map per key=uid...
                                if(list_to_update!=null)
                                    knowed_lists.remove(list_to_update);
                                knowed_lists.add(modified_list);
                                lists.put(uid, knowed_lists);
                                break;
                            case REMOVED:
                                //Quando elimino una lista con dei film, vengono eliminati prima i film al suo interno e poi la lista :)
                                System.out.println("rimossa lista: "+doc_ch.getDocument().getId());
                                Set<MovieList> updated_list=lists.get(uid);
                                MovieList list_to_remove=new MovieList(doc_ch.getDocument().getId(), (String)doc_ch.getDocument().get("description"), (Boolean)doc_ch.getDocument().get("movies"));
                                updated_list.remove(list_to_remove);
                                lists.put(uid,updated_list);
                                lists_number--;
                                break;
                        }
                    }
                    stats[7]=lists_number;
                    stats[8]=movies_sum/lists_number;
                    System.out.println("Mostro "+stats[7]+" numero di liste e "+stats[8]+"numero medio di film");
                    page.showStatsPanel(stats);
                }
            }
        });
    }
    private void syncOnMovieList(String uid, String list_name){
        db.collection("users/"+uid+"/lists/"+list_name+"/movies").addSnapshotListener(new EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(QuerySnapshot value, FirestoreException error) {
                System.out.println("evento su movie list");
                synchronized(lock){
                    System.out.println("ECCOLOD");
                    for(DocumentChange d_c:value.getDocumentChanges()){
                        switch(d_c.getType()){
                                case ADDED:
                                    movies_sum++;
                                    System.out.println("è stato aggiunto il film "+ d_c.getDocument().getId()+" alla lista personalizzata "+list_name);
                                    break;
                                case REMOVED:
                                    System.out.println("è stato rimosso il film "+ d_c.getDocument().getId()+" alla lista personalizzata "+list_name);
                                    movies_sum--;
                                    break;
                        }
                    }
                    stats[8]=movies_sum/lists_number;
                    System.out.println("siamo a "+movies_sum+"film nelle liste D");
                    page.showStatsPanel(stats);
                }
            }
        });
    }
    private void syncOnSearches(String uid){
        db.collection("users/"+uid+"/searches").addSnapshotListener(new EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(QuerySnapshot value, FirestoreException error) {
                synchronized(lock){
                    for(DocumentChange document_ch:value.getDocumentChanges()){
                        switch(document_ch.getType()){
                            case ADDED:
                                if((Boolean)document_ch.getDocument().get("friend")==true)
                                    user_searches_number++;
                                else
                                    movie_searches_number++;
                                break;
                        }
                    }
                    stats[3]=user_searches_number;
                    stats[4]=movie_searches_number;
                    page.showStatsPanel(stats);
                }
            }
        });
    }
    private void syncOnFavourites(String uid){
        db.collection("users/"+uid+"/favourites").addSnapshotListener(new EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(QuerySnapshot value, FirestoreException error) {
                synchronized(lock){
                    for(DocumentChange doc_ch:value.getDocumentChanges()){
                        switch(doc_ch.getType()){
                            case ADDED:
                                System.out.println("aggiunto film ai preferiti: "+doc_ch.getDocument().getId());
                                movies_sum++;
                                break;
                            case REMOVED:
                                System.out.println("rimosso film: "+doc_ch.getDocument().getId());
                                movies_sum--;
                                break;
                        }
                    }
                    stats[8]=movies_sum/lists_number;
                    page.showStatsPanel(stats);
                }
            }
        });
    }
    public void logoutPressed(){
        //mostro un jOptionPane in cui chiedo conferma 
        int input=JOptionPane.showConfirmDialog(new ConfirmDialog(), "Sei sicuro di voler effettuare il logout?", "customized dialog", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if(input==0){
            page.dispose();
            new AuthenticationPage().setVisible(true);
        }
    }
}