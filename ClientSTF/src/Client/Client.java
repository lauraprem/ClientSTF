/* FAGNO CORINNE */
package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
public class Client
{

    // ATTRIBUTS
    private DatagramSocket datagramSoket;
    private DatagramPacket datagramPacketReception;
    private DatagramPacket datagramPacketEnvoie;
    private final int portServeur = 69;

    // CONSTRUCTEUR
    public Client()
    {
        initSocket();
        byte[] data = new byte[512];
        datagramPacketEnvoie = new DatagramPacket(data, data.length);
    }

    // METHODES
    public void initSocket()
    {
        int SO_TIMEOUT = 180000; // 3 minutes
        try
        {
            datagramSoket = new DatagramSocket();
            datagramSoket.setSoTimeout(SO_TIMEOUT);
        } catch (SocketException ex)
        {
            System.err.println("Port déjà occupé : " + ex.getMessage());
        }
    }

    public void CreationDatagramEnvoie(byte[] data, int portServeur, InetAddress adresseServeur)
    {
        datagramPacketEnvoie = new DatagramPacket(data, data.length, adresseServeur, portServeur);
    }

    // -1 = mauvais déroulement; 1 = bon déroulement
    public int sendFile(File fichier, InetAddress serveur)
    {
        try
        {
            if (fichier.canRead())
            {
                byte[] data = new byte[512];
                String temp = new String();
                temp = "02" + fichier.getName() + "0ctet0";
                data = temp.getBytes("octet");
                CreationDatagramEnvoie(data, portServeur, serveur);
                datagramSoket.send(datagramPacketEnvoie);

            }

        } catch (SecurityException ex)
        {
            System.err.println(ex.getMessage());
            return -1;
        } catch (UnsupportedEncodingException e)
        {
            System.err.println(e.getMessage());
            return -1;
        } catch (IOException exe)
        {
            System.err.println(exe.getMessage());
            return -1;
        }
        return 1;
    }

    public static String getContents(File aFile)
    {
        StringBuilder contents = new StringBuilder();

        try
        {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try
            {
                String line = null;
                while ((line = input.readLine()) != null)
                {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally
            {
                input.close();
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return contents.toString();
    }
//    public void run(){
//        try {
//            byte[] dataEnvoi;
//            byte[] dataRecu = new byte[4096];
//            InetAddress ipAdresseServeur = InetAddress.getByName("localhost");
//            int portServeur = 69;
//            
//            /*********************** PRISE DE CONTACT ***********************/
//            // Envoie msg prise de contact au serveur
//            String msg = "";
//            dataEnvoi = msg.getBytes("ascii");
//            CreationDatagramEnvoie(dataEnvoi, portServeur,ipAdresseServeur);
//            datagramSoket.send(datagramPacketEnvoie);
//            
//            // Reception msg prise de contact du serveur
//            datagramSoket.receive(datagramPacketReception);
//            //Recuperation des donnees
//            ipAdresseServeur = datagramPacketReception.getAddress();
//            portServeur = datagramPacketReception.getPort();
//            msg = new String(datagramPacketReception.getData(),"ascii");
//            // Affichage du message du serveur
//            System.out.println("Serveur("+ipAdresseServeur.toString()+" : "+portServeur+") : "+msg);
//            
//            
//            /******************* DISCUSSION AVEC LE SERVEUR *******************/
//            //while(QuitterSocket(dataRecu) != true){
//                
//                //Enregistrement de se que tape le Client au clavier
//                // connexion d'un buffer de lecture sur le flux d'entrée
//                BufferedReader entree = new BufferedReader(new InputStreamReader(System.in));
//                msg = entree.readLine();
//                dataEnvoi = msg.getBytes("ascii");
//                // Envoie le msg au serveur
//                CreationDatagramEnvoie(dataEnvoi, portServeur,ipAdresseServeur);
//                datagramSoket.send(datagramPacketEnvoie);
//                
//                //Reception client
//                datagramPacketReception = new DatagramPacket(dataRecu, dataRecu.length);
//                datagramSoket.receive(datagramPacketReception);
//                //Recuperation des donnees du serveur
//                ipAdresseServeur = datagramPacketReception.getAddress();
//                portServeur = datagramPacketReception.getPort();
//                msg = new String( datagramPacketReception.getData(),"ascii");
//                // Affichage du message du serveur
//                System.out.println("Serveur("+ipAdresseServeur.toString()+" : "+portServeur+") : "+msg);
//           // }
//            datagramSoket.close();
//               
//        }catch(SocketTimeoutException e){
//                System.out.println("time_out dépassé : "+e.getMessage());
//                datagramSoket.close();
//        }catch (UnknownHostException e1) {
//            System.out.println("Erreur adrresse IP : "+e1.getMessage());
//        }catch (IOException e2) {
//            System.out.println("Erreur saisie clavier : "+e2.getMessage());
//        }
//    }
//    public boolean QuitterSocket(byte[] msgData){
//        String msg = "";
//        for(int i = 0; i<7; i++){
//            msg += (char)msgData[i];
//        }
//        return msg.equals("quitter");
//    }
}
