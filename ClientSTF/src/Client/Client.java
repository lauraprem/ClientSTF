/* FAGNO CORINNE */
package Client;

import com.sun.swing.internal.plaf.basic.resources.basic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 *
 * @author Corinne
 */
public class Client
{

    // ATTRIBUTS
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacketReception;
    private DatagramPacket datagramPacketEnvoie;
    private int portServeur;

    // Constantes spécifiques du protocole TFTP
    // Codes des opérations TFTP
    private final static byte RRQ_OPCODE = 1; // Requête lecture
    private final static byte WRQ_OPCODE = 2; // Requête écriture
    private final static byte DATA_OPCODE = 3; // Transmission
    private final static byte ACK_OPCODE = 4; // Acquittement
    private final static byte ERROR_OPCODE = 5; // Erreur
    // Codes des erreurs TFTP
    private final static byte NOTDEF_ERROR = 0;
    private final static byte FILE_NOT_FOUND_ERROR = 1;
    private final static byte ACCESS_VIOLATION_ERROR = 2;
    // Taille des entêtes TFTP
    private final static int HEADER_SIZE = 4;
    // Constante pour le mode de transfert
    private final static String BINARY_MODE = "octet";
    // Taille maximale d'un bloc de données
    private final static int BLOCK_SIZE = 512;

    // CONSTRUCTEUR
    public Client()
    {
        initSocket();

        //?????//
        byte[] data = new byte[BLOCK_SIZE];
        datagramPacketEnvoie = new DatagramPacket(data, data.length);
        portServeur = 69;
        byte[] buffer = new byte[BLOCK_SIZE + 4];
        datagramPacketReception = new DatagramPacket(buffer, buffer.length);
    }

    // METHODES
    public void initSocket()
    {
        int SO_TIMEOUT = 180000; // 3 minutes
        try
        {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(SO_TIMEOUT);
        } catch (SocketException ex)
        {
            System.err.println("Port déjà occupé : " + ex.getMessage());
        }
    }

    public void CreationDatagramEnvoie(byte[] data, int portServeur, InetAddress adresseServeur)
    {
        datagramPacketEnvoie = new DatagramPacket(data, data.length, adresseServeur, portServeur);
    }

    // Paquets
    public byte[] CreatPaquet_RRQ_WRQ(int codeOp, String nomFichier, String mode)
    {
        int size = 0;
        byte[] data = new byte[BLOCK_SIZE];

        // Code Op
        data[0] = 0;
        data[1] = (byte) codeOp;
        size += 2;

        // Nom fichier
        System.arraycopy(nomFichier.getBytes(), 0, data, size, nomFichier.getBytes().length);
        size += nomFichier.getBytes().length;

        // 0
        data[size] = 0;
        size++;

        // Mode
        System.arraycopy(mode.getBytes(), 0, data, size, mode.getBytes().length);
        size += mode.getBytes().length;

        // 0
        data[size] = 0;
        size++;

        return data;
    }

    public byte[] CreatPaquet_DATA(int codeOp, int i, byte[] donnees)
    {
        int size = 0;
        byte[] data = new byte[donnees.length + 4];

        // Code Op
        data[0] = 0;
        data[1] = (byte) codeOp;
        size += 2;

        // Num bloc
       /* data[size] = d;
         size++;
         data[size] = u;
         size++;*/
        data[2] = (byte) (i >> 8);
        data[3] = (byte) (i);
        size += 2;
        // Donnees
        System.arraycopy(donnees, 0, data, size, donnees.length);
        size = size + donnees.length;

        return data;
    }

    public byte[] CreatPaquet_ACK(int codeOp, byte d, byte u)
    {
        int size = 0;
        byte[] data = new byte[4];

        // Code Op
        data[0] = 0;
        data[1] = (byte) codeOp;
        size = size + 2;

        // Num bloc
        data[size] = d;
        size++;
        data[size] = u;
        size++;

        return data;
    }

    public byte[] CreatPaquet_ERROR(int codeOp, int codeErreur, String msgErreur)
    {
        int size = 0;
        byte[] data = new byte[4];

        // Code Op
        data[0] = 0;
        data[1] = (byte) codeOp;
        size = size + 2;

        // Code Erreur
        data[2] = 0;
        data[3] = (byte) codeErreur;
        size = size + 2;

        // Message erreur
        System.arraycopy(msgErreur.getBytes(), 0, data, size, msgErreur.getBytes().length);
        size = size + msgErreur.getBytes().length;

        // 0
        data[msgErreur.getBytes().length] = 0;
        size++;

        return data;
    }

    public void EnvoiData(DatagramPacket d, InetAddress serveur)
    {
        while (true)
        {
            try
            {
                // on envoie
                datagramSocket.send(d);
                // on receptionne la réponse
                datagramSocket.receive(datagramPacketReception);
                byte[] buf;
                buf = datagramPacketReception.getData();
                // si on avait envoyé
                if (d.getData()[1] == WRQ_OPCODE)
                {
                    if (buf[0] == 0 && buf[1] == 4 && 0 == buf[2] && 0 == buf[3])
                    {
                        break;
                    }
                } else if ((d.getData()[1] == DATA_OPCODE))
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

    private void EvoieAck(DatagramPacket d, InetAddress serveur)
    {
        try
        {
            byte[] data = new byte[BLOCK_SIZE];
            String temp = new String();
            data = CreatPaquet_ACK(4, d.getData()[2], d.getData()[3]);
            datagramSocket.send(new DatagramPacket(data, data.length, serveur, portServeur));
        } catch (UnsupportedEncodingException ex)
        {
            System.err.println(ex.getMessage());
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    // 0 = bon déroulement
    // 1 = impossible de lire le fichier parce qu'il n'existe pas ou qu'on n'a pas les droits
    // 2 = impossibe de lire le fichier a cause de l'encodage
    // 3 = problème d'entrée/sortie
    public int SendFile(File fichier, InetAddress serveur)
    {
        try
        {
            //vérification des droits et de l'existance du fichier
            if (fichier.canRead())
            {
                FileInputStream monFileInputStream = new FileInputStream(fichier);
                byte[] data;
                String temp = new String();

                // demande pour envoyer un fichier et acknowlege 
                data = CreatPaquet_RRQ_WRQ(WRQ_OPCODE, fichier.getName(), BINARY_MODE);
                data[fichier.getName().getBytes().length + 3 + temp.getBytes().length] = 0;
                CreationDatagramEnvoie(data, portServeur, serveur);
                EnvoiData(datagramPacketEnvoie, serveur);
                portServeur = datagramPacketReception.getPort();

                // Envoie du fichier
                data = new byte[512];
                String buffer = new String();
                //numérotation des datagrammes
                int index = 1;

                for (int i = 0; i < fichier.length(); i++)
                {
                    if (monFileInputStream.available() >= BLOCK_SIZE)
                    {
                        monFileInputStream.read(data, 0, BLOCK_SIZE);
                    } else
                    {
                        int a = monFileInputStream.available();
                        data = new byte[a];
                        monFileInputStream.read(data, 0, a);
                        /*data[0] = 0;
                         data[1] = 3;*/
                        data = CreatPaquet_DATA(DATA_OPCODE, index, data);
                        CreationDatagramEnvoie(data, portServeur, serveur);
                        EnvoiData(datagramPacketEnvoie, serveur);
                        break;

                    }
                    data = CreatPaquet_DATA(DATA_OPCODE, index, data);
                    CreationDatagramEnvoie(data, portServeur, serveur);
                    EnvoiData(datagramPacketEnvoie, serveur);
                    buffer = new String();
                    data = new byte[512];
                    index++;
                }
            }

        } catch (SecurityException ex)
        {
            System.err.println(ex.getMessage());
            return 1;
        } catch (UnsupportedEncodingException e)
        {
            System.err.println(e.getMessage());
            return 2;
        } catch (IOException exe)
        {
            System.err.println(exe.getMessage());
            return 3;
        }
        portServeur = 69;
        return 0;
    }

    public int ReceiveFile(String nomLocal, String nomDistant, InetAddress serveur, String chemin)
    {
        try
        {
            byte[] data = new byte[BLOCK_SIZE];
            String temp = new String();
            File fichier = new File(chemin + "/" + nomLocal);
            if (!fichier.exists())
            {
                fichier.createNewFile();
            }
            FileOutputStream monFileOutputStream = new FileOutputStream(fichier, true);
            // demande pour envoyer un fichier et acknowlege
            data = CreatPaquet_RRQ_WRQ(RRQ_OPCODE, nomDistant, BINARY_MODE);
            CreationDatagramEnvoie(data, portServeur, serveur);
            datagramSocket.send(datagramPacketEnvoie);

            datagramSocket.receive(datagramPacketReception);
            portServeur = datagramPacketReception.getPort();
            data = new byte[datagramPacketReception.getLength() - 4];
            EvoieAck(datagramPacketReception, serveur);
            System.arraycopy(datagramPacketReception.getData(), 4, data, 0, datagramPacketReception.getLength() - 4);

            monFileOutputStream.write(data);
            while (datagramPacketReception.getLength() == BLOCK_SIZE + 4)
            {
                datagramSocket.receive(datagramPacketReception);
                data = new byte[datagramPacketReception.getLength() - 4];
                EvoieAck(datagramPacketReception, serveur);
                System.arraycopy(datagramPacketReception.getData(), 4, data, 0, datagramPacketReception.getLength() - 4);
                monFileOutputStream.write(data);
            }
        } catch (UnsupportedEncodingException ex)
        {
            System.err.println(ex.getMessage());
            return 1;
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
            return 2;
        }
        portServeur = 69;
        return 0;
    }

}
