/* FAGNO CORINNE */

package client_stf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 *
 * @author Corinne
 */
public class Client {
    // ATTRIBUTS
    private DatagramSocket datagramSoket;
    private DatagramPacket datagramPacket;
    
    // CONSTRUCTEUR
    public Client(){
        initSocket();
    }
    
    // METHODES
    public void initSocket(){
        int SO_TIMEOUT = 180000; // 3 minutes
        try{
            datagramSoket = new DatagramSocket();
            datagramSoket.setSoTimeout(SO_TIMEOUT);
        }catch (SocketException ex) {
            System.err.println("Port déjà occupé : "+ex.getMessage());
        }
    }
    
    public void CreationDatagram(byte[] data, int portServeur, InetAddress adresseServeur){
        datagramPacket = new DatagramPacket(data, data.length, adresseServeur, portServeur);
    }
    
    public void run(){
        try {
            byte[] dataEnvoi;
            byte[] dataRecu = new byte[4096];
            InetAddress ipAdresseServeur = InetAddress.getByName("localhost");
            int portServeur = 69;
            
            /*********************** CONNEXION ***********************/
            // Envoie msg 'connection' au serveur
            String msg = "";
            dataEnvoi = msg.getBytes("ascii");
            CreationDatagram(dataEnvoi, portServeur,ipAdresseServeur);
            datagramSoket.send(datagramPacket);
            
            // Reception msg 'connection' du serveur
            datagramPacket = new DatagramPacket(dataRecu, dataRecu.length);
            datagramSoket.receive(datagramPacket);
            //Recuperation des donnees
            ipAdresseServeur = datagramPacket.getAddress();
            portServeur = datagramPacket.getPort();
            msg = new String(datagramPacket.getData(),"ascii");
            // Affichage du message du serveur
            System.out.println("Serveur("+ipAdresseServeur.toString()+" : "+portServeur+") : "+msg);
            
            
            /******************* DISCUSSION AVEC LE SERVEUR *******************/
            //while(QuitterSocket(dataRecu) != true){
                
                //Enregistrement de se que tape le Client au clavier
                // connexion d'un buffer de lecture sur le flux d'entrée
                BufferedReader entree = new BufferedReader(new InputStreamReader(System.in));
                msg = entree.readLine();
                dataEnvoi = msg.getBytes("ascii");
                // Envoie le msg au serveur
                CreationDatagram(dataEnvoi, portServeur,ipAdresseServeur);
                datagramSoket.send(datagramPacket);
                
                //Reception client
                datagramPacket = new DatagramPacket(dataRecu, dataRecu.length);
                datagramSoket.receive(datagramPacket);
                //Recuperation des donnees du serveur
                ipAdresseServeur = datagramPacket.getAddress();
                portServeur = datagramPacket.getPort();
                msg = new String( datagramPacket.getData(),"ascii");
                // Affichage du message du serveur
                System.out.println("Serveur("+ipAdresseServeur.toString()+" : "+portServeur+") : "+msg);
           // }
            datagramSoket.close();
               
        }catch(SocketTimeoutException e){
                System.out.println("time_out dépassé : "+e.getMessage());
                datagramSoket.close();
        }catch (UnknownHostException e1) {
            System.out.println("Erreur adrresse IP : "+e1.getMessage());
        }catch (IOException e2) {
            System.out.println("Erreur saisie clavier : "+e2.getMessage());
        }
    }
//    public boolean QuitterSocket(byte[] msgData){
//        String msg = "";
//        for(int i = 0; i<7; i++){
//            msg += (char)msgData[i];
//        }
//        return msg.equals("quitter");
//    }
}
