/* FAGNO CORINNE */
package Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class Client {

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
    // Taille des entêtes TFTP
    private final static int HEADER_SIZE = 4;
    // Constante pour le mode de transfert
    private final static String BINARY_MODE = "octet";
    // Taille maximale d'un bloc de données
    private final static int DATA_SIZE = 512;

    // CONSTRUCTEUR
    /**
     * Constructeur non paramétré
     */
    public Client() {
        initSocket();
        portServeur = 69;
        datagramPacketEnvoie = new DatagramPacket(new byte[DATA_SIZE + HEADER_SIZE], DATA_SIZE + HEADER_SIZE);
        datagramPacketReception = new DatagramPacket(new byte[DATA_SIZE + HEADER_SIZE], DATA_SIZE + HEADER_SIZE);
    }

    // METHODES
    /**
     * Permet d'initialiser le socket avec un timeout de 3 minutes
     */
    public void initSocket() {
        int SO_TIMEOUT = 9000; // 3 minutes
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(SO_TIMEOUT);
        } catch (SocketException ex) {
            System.err.println("Port déjà occupé : " + ex.getMessage());
        }
    }

    /**
     * Permet de créer un paquet RRQ WRQ .
     *
     * @param codeOp permet de savoir le type du paquet
     * @param nomFichier le nom du fichier
     * @param mode le mode d'envoie
     * @return les données constituant un paquet RRQ ou WRQ
     */
    public byte[] CreatPaquet_RRQ_WRQ(int codeOp, String nomFichier, String mode) {
        int size = 0;
        byte[] data = new byte[nomFichier.getBytes().length + mode.getBytes().length + 4];

        // Code Op taille = 2
        data[0] = 0;
        data[1] = (byte) codeOp;
        size += 2;

        // Nom fichier taille = nomFichier.getBytes().length
        System.arraycopy(nomFichier.getBytes(), 0, data, size, nomFichier.getBytes().length);
        size += nomFichier.getBytes().length;

        // 0 taille = 1
        data[size] = 0;
        size++;

        // Mode taille = mode.getBytes().length
        System.arraycopy(mode.getBytes(), 0, data, size, mode.getBytes().length);
        size += mode.getBytes().length;

        // 0 taille = 1
        data[size] = 0;
        size++;

        return data;
    }

    /**
     * Permet de créer un paquet de données
     *
     * @param i numéro du bloc
     * @param donnees données à envoyer
     * @return les données constituant un paquet DATA
     */
    public byte[] CreatPaquet_DATA(int i, byte[] donnees) {
        int size = 0;
        byte[] data = new byte[donnees.length + HEADER_SIZE];

        // Code Op
        data[0] = 0;
        data[1] = (byte) DATA_OPCODE;
        size += 2;

        // Num bloc
        data[2] = (byte) (i >> 8);
        data[3] = (byte) (i);
        size += 2;
        // Donnees
        System.arraycopy(donnees, 0, data, size, donnees.length);
        size += donnees.length;

        return data;
    }

    /**
     * Permet de créer un paquet d'acknowledgment
     *
     * @param i numéro de bloc
     * @return les données constituant un paquet ACK
     */
    public byte[] CreatPaquet_ACK(byte d, byte u) {
        int size = 0;
        byte[] data = new byte[4];

        // Code Op
        data[0] = 0;
        data[1] = (byte) ACK_OPCODE;
        size = size + 2;

        // Num bloc
        data[2] = d;
        data[3] = u;
        size += 2;

        return data;
    }

    /**
     * Permet de créer un paquet d'erreur
     *
     * @param codeErreur code de l'erreur
     * @param msgErreur message de l'erreur
     * @return les données constituant un paquet ERR
     */
    public byte[] CreatPaquet_ERROR(int codeErreur, String msgErreur) {
        int size = 0;
        byte[] data = new byte[4];

        // Code Op
        data[0] = 0;
        data[1] = (byte) ERROR_OPCODE;
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

    /**
     * Permet d'envoyer une demande de lecture ou d'écriture et d'interpréter la
     * réponse du serveur
     *
     * @return 0 si on recoit un bon acknowledge, le numéro d'erreur si on a un
     * paquet d'erreur
     */
    private int EnvoiWRQ() {
        byte[] buf;
        while (true) {
            try {
                // on envoie
                datagramSocket.send(datagramPacketEnvoie);
                // on receptionne la réponse
                datagramSocket.receive(datagramPacketReception);
                buf = datagramPacketReception.getData();
                if (buf[0] == 0 && buf[1] == ACK_OPCODE && 0 == buf[2] && 0 == buf[3]) {
                    return 0;
                } else if (buf[1] == ERROR_OPCODE) {
                    if (buf[3] == 0) {
                        return -1;
                    }
                    return buf[3];
                }

            } catch (SocketTimeoutException e) {
                System.err.println("time_out dépassé : " + e.getMessage());
                return 10;
            } catch (IOException e) {
                System.err.println(e.getMessage());

            }
        }
    }

    private int EnvoiRRQ() {
        byte[] buf;
        while (true) {
            try {
                // on envoie
                datagramSocket.send(datagramPacketEnvoie);
                // on receptionne la réponse
                datagramSocket.receive(datagramPacketReception);
                buf = datagramPacketReception.getData();
                if (buf[0] == 0 && buf[1] == DATA_OPCODE && 0 == buf[2] && 1 == buf[3]) {
                    return 0;
                } else if (buf[1] == ERROR_OPCODE) {
                    if (buf[3] == 0) {
                        return -1;
                    }
                    return buf[3];
                }

            } catch (SocketTimeoutException e) {
                System.err.println("time_out dépassé : " + e.getMessage());
                return 10;
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Permet d'envoyer des données au serveur et d'interpréter la réponse
     *
     * @return 0 si on recoit un bon acknowledge, le numéro d'erreur si on a un
     * paquet d'erreur
     */
    private int EnvoiData() {
        byte[] buf;
        while (true) {
            try {
                // on envoie
                datagramSocket.send(datagramPacketEnvoie);
                // on receptionne la réponse
                datagramSocket.receive(datagramPacketReception);
                buf = datagramPacketReception.getData();
                if (buf[0] == 0 && buf[1] == ACK_OPCODE && datagramPacketEnvoie.getData()[2] == buf[2] && datagramPacketEnvoie.getData()[3] == buf[3]) {
                    return 0;
                } else if (buf[1] == ERROR_OPCODE) {
                    if (buf[3] == 0) {
                        return -1;
                    }
                    return buf[3];
                }

            } catch (SocketTimeoutException e) {
                System.err.println("time_out dépassé : " + e.getMessage());
                return 10;
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * permet d'envoyer un acknowldge et de vérifier sa bonne réception
     *
     * @return 0 si on recoit un bon packet, le numéro d'erreur si on a un
     * paquet d'erreur
     */
    private int EnvoiAck() {
        byte[] buf;
        while (true) {
            try {
                // on envoie
                datagramSocket.send(datagramPacketEnvoie);
                return 0;
            } catch (SocketTimeoutException e) {
                System.err.println("time_out dépassé : " + e.getMessage());
                return 10;
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * permet d'envoyer un paquet d'erreur
     */
    private void EnvoiErr() {
        try {
            // on envoie
            datagramSocket.send(datagramPacketEnvoie);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * permet d'envoyer un datagrampaket au serveur de n'importe quel type
     *
     * @param data les données à envoyer
     * @param portServeur le port du serveur
     * @param serveur l'adresse du serveur
     * @return 0 si tout s'est bien passé le code d'erreur si l'on en a reçu
     * une(de 1 à 7) -1 en cas de problème ou d'erreur indéfinie
     */
    public int EnvoiDatagram(byte[] data, int portServeur, InetAddress serveur) {
        datagramPacketEnvoie = new DatagramPacket(data, data.length, serveur, portServeur);
        int opcode = data[1];
        switch (opcode) {
            case RRQ_OPCODE:
                return EnvoiRRQ();
            case WRQ_OPCODE:
                return EnvoiWRQ();
            case DATA_OPCODE:
                return EnvoiData();
            case ACK_OPCODE:
                return EnvoiAck();
            case ERROR_OPCODE:
                EnvoiErr();
        }
        return -1;

    }

    /**
     * Permet d'envoyer un fichier au serveur
     *
     * @param fichier le fichier que l'on veut envoyer
     * @param serveur l'adresse du serveur
     * @return 0 si tout s'est bien passé le code d'erreur si l'on en a reçu
     * une(de 1 à 7) 8 = violation d'accès 9 = impossibe de lire le fichier a
     * cause de l'encodage 10 = time out
     */
    public int SendFile(File fichier, InetAddress serveur) {
        int resEnvoie;
        try {
            //vérification des droits et de l'existance du fichier
            if (fichier.canRead()) {
                FileInputStream monFileInputStream = new FileInputStream(fichier);
                byte[] data;
                String temp = new String();

                // demande pour envoyer un fichier et recepetion acknowlege 
                data = CreatPaquet_RRQ_WRQ(WRQ_OPCODE, fichier.getName(), BINARY_MODE);
                //data[fichier.getName().getBytes().length + 3 + temp.getBytes().length] = 0;
                resEnvoie = EnvoiDatagram(data, portServeur, serveur);
                if (resEnvoie != 0) {
                    return resEnvoie;
                }
                portServeur = datagramPacketReception.getPort();

                // Envoie du fichier
                data = new byte[512];
                String buffer = new String();
                //numérotation des datagrammes
                int index = 1;

                //on parcourt le fichier
                for (int i = 0; i < fichier.length(); i++) {
                    //on lit des morceau de 512 octets 
                    if (monFileInputStream.available() >= DATA_SIZE) {
                        monFileInputStream.read(data, 0, DATA_SIZE);
                        //si l'on est à la fin du fichier, on envoie un datagram 
                        //contenant moins de 512 octets de données pour signaler que c'est le dernier
                    } else {
                        int a = monFileInputStream.available();
                        data = new byte[a];
                        monFileInputStream.read(data, 0, a);
                        /*data[0] = 0;
                         data[1] = 3;*/
                        data = CreatPaquet_DATA(index, data);
                        resEnvoie = EnvoiDatagram(data, portServeur, serveur);
                        if (resEnvoie != 0) {
                            return resEnvoie;
                        }
                        break;

                    }
                    data = CreatPaquet_DATA(index, data);
                    resEnvoie = EnvoiDatagram(data, portServeur, serveur);
                    if (resEnvoie != 0) {
                        return resEnvoie;
                    }
                    buffer = new String();
                    data = new byte[512];
                    index++;
                }
            }

        } catch (SecurityException ex) {
            System.err.println(ex.getMessage());
            byte[] data;
            data = CreatPaquet_ERROR(2, BINARY_MODE);
            EnvoiDatagram(data, portServeur, serveur);
            return 8;
        } catch (UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
            byte[] data;
            data = CreatPaquet_ERROR(1, BINARY_MODE);
            EnvoiDatagram(data, portServeur, serveur);
            return 9;
        } catch (IOException exe) {
            System.err.println(exe.getMessage());
            return 8;
        }
        portServeur = 69;
        return 0;
    }

    /**
     * Permet de demander au serveur de nous envoyer un fichier
     *
     * @param nomLocal nom du fichier tel que l'on veut le sauvegarder en local
     * @param nomDistant nom du fichier sur le serveur
     * @param serveur adresse du serveur
     * @param chemin chemin d'accès au fichier
     * @return 0 si tout s'est bien passé le code d'erreur si l'on en a reçu
     * une(de 1 à 7) 8 = violation d'accès 9 = impossibe de lire le fichier a
     * cause de l'encodage 10 = time out 11 = le nom choisit existe déjà
     */
    public int ReceiveFile(String nomLocal, String nomDistant, InetAddress serveur, String chemin) {
        int resEnvoie;
        try {
            byte[] data = new byte[DATA_SIZE];
            String temp = new String();
            File fichier = new File(chemin + "/" + nomLocal);
            if (fichier.exists()) {
                return 11;
            }

            // demande pour envoyer un fichier
            data = CreatPaquet_RRQ_WRQ(RRQ_OPCODE, nomDistant, BINARY_MODE);
            resEnvoie = EnvoiDatagram(data, portServeur, serveur);
            if (resEnvoie != 0) {
                return resEnvoie;
            }

            portServeur = datagramPacketReception.getPort();
            data = CreatPaquet_ACK(datagramPacketReception.getData()[2], datagramPacketReception.getData()[3]);
            resEnvoie = EnvoiDatagram(data, portServeur, serveur);
            if (resEnvoie != 0) {
                return resEnvoie;
            }
            // on résupère le premier datagram de données
            data = new byte[datagramPacketReception.getLength() - HEADER_SIZE];
            System.arraycopy(datagramPacketReception.getData(), HEADER_SIZE, data, 0, datagramPacketReception.getLength() - HEADER_SIZE);
            //Ecriture dans le fichier des premières données
            fichier.createNewFile();
            FileOutputStream monFileOutputStream = new FileOutputStream(fichier, true);
            monFileOutputStream.write(data);
            //tant que le bloc que l'on reçoit contient 516 octets (4 d'entete et 512 de données)
            while (datagramPacketReception.getLength() == DATA_SIZE + HEADER_SIZE) {
                //reception des données
                datagramSocket.receive(datagramPacketReception);
                //acknowledge
                data = CreatPaquet_ACK(datagramPacketReception.getData()[2], datagramPacketReception.getData()[3]);
                resEnvoie = EnvoiDatagram(data, portServeur, serveur);
                if (resEnvoie != 0) {
                    return resEnvoie;
                }
                //
                //récupération de ces données
                data = new byte[datagramPacketReception.getLength() - HEADER_SIZE];
                System.arraycopy(datagramPacketReception.getData(), HEADER_SIZE, data, 0, datagramPacketReception.getLength() - HEADER_SIZE);
                //écriture dans le fichier
                monFileOutputStream.write(data);
            }
        } catch (UnsupportedEncodingException ex) {
            System.err.println(ex.getMessage());
            byte[] data;
            data = CreatPaquet_ERROR(1, BINARY_MODE);
            EnvoiDatagram(data, portServeur, serveur);
            return 9;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            byte[] data;
            data = CreatPaquet_ERROR(2, BINARY_MODE);
            EnvoiDatagram(data, portServeur, serveur);
            return 8;
        }
        portServeur = 69;
        return 0;
    }

}
