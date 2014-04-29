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
    private int portServeur;

    // CONSTRUCTEUR
    public Client()
    {
        initSocket();
        byte[] data = new byte[512];
        datagramPacketEnvoie = new DatagramPacket(data, data.length);
        portServeur = 69;
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

    public void CreationDatagramReception(InetAddress adresseServeur)
    {
        byte[] buffer = new byte[512];
        datagramPacketReception = new DatagramPacket(buffer, buffer.length, adresseServeur, portServeur);
    }

    public void EnvoiData(DatagramPacket d, InetAddress serveur)
    {
        while (true)
        {
            try
            {
                // on envoie
                datagramSoket.send(d);
                CreationDatagramReception(serveur);
                // on receptionne la réponse
                datagramSoket.receive(datagramPacketReception);
                byte[] buf = new byte[512];
                buf = datagramPacketReception.getData();
                // si on avait envoyé
                if (d.getData()[1] == 2)
                {
                    if (buf[0] == 0 && buf[1] == 4 && 0 == buf[2] && 0 == buf[3])
                    {
                        break;
                    }
                } else if ((d.getData()[1] == 3))
                {
                    if (buf[0] == 0 && buf[1] == 4 && d.getData()[2] == buf[2] && d.getData()[3] == buf[3])
                    {
                        break;
                    }
                }
            } catch (SocketTimeoutException e)
            {
                System.err.println("time_out dépassé : " + e.getMessage());
            } catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
        }
    }

    // -1 = mauvais déroulement; 1 = bon déroulement
    public int sendFile(File fichier, InetAddress serveur)
    {
        try
        {
            //vérification des droits et de l'existance du fichier
            if (fichier.canRead())
            {
                FileReader monFileReader = new FileReader(fichier);
                byte[] data = new byte[512];
                String temp = new String();

                // demande pour envoyer un fichier et acknowlege 
                temp = "02" + fichier.getName() + "0ctet0";
                data = temp.getBytes("octet");
                CreationDatagramEnvoie(data, portServeur, serveur);
                EnvoiData(datagramPacketEnvoie, serveur);
                portServeur = datagramPacketReception.getPort();

                // Envoie du fichier
                data = new byte[512];
                temp = new String();
                String buffer = new String();
                //numérotation des datagrammes
                int n = 1;
                
                for (int i = 0; i < fichier.length(); i++)
                {
                    for (int j = 0; j < 512 && monFileReader.ready(); j++)
                    {
                        buffer += monFileReader.read();
                    }
                    monFileReader.mark(i);
                    if (n < 10)
                    {
                        temp = "0" + n + buffer;
                    } else
                    {
                        temp = n + buffer;
                    }
                    data = temp.getBytes("octet");
                    CreationDatagramEnvoie(data, portServeur, serveur);
                    EnvoiData(datagramPacketEnvoie, serveur);
                    buffer = new String();
                    n++;

                }
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
