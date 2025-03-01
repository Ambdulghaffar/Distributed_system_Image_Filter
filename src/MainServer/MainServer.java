package MainServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/* La classe MainServer est le serveur principal d’un système distribué de traitement d’images. Elle gère la connexion des clients, la distribution des tâches aux travailleurs (workers) et l’exécution des tâches via un pool de threads.
 */
/* Cette classe assure le rôle de serveur central dans un système distribué.
Elle écoute les connexions clients et distribue les tâches aux travailleurs.
L'implémentation utilise un pool de threads et une file d’attente pour optimiser le traitement.
Le système est scalable puisqu'il peut gérer plusieurs workers de manière dynamique. */

import RessourcesForRMI.WorkerDataList;

public class MainServer extends Thread {
    private static Executor executor;
    private static TaskQueue taskQueue;
    private static int TaskID = 100;

    static int MainServer_port;
    static String MainServer_host;

    public static void main(String[] args) {
        System.out.println("Démarrage du serveur...");

        // Lire les propriétés du fichier
        Properties prop = new Properties();
        FileInputStream ip;
        String FileConfiguration = "cfgMainServer.properties"; // Nom par défaut du fichier
        if (args.length > 0) {
            FileConfiguration = args[0];
        }

        try {
            ip = new FileInputStream(FileConfiguration);
            prop.load(ip);
            System.out.println("Configuration chargée avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier de configuration: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Initialiser les valeurs de configuration
        try {
            MainServer_port = Integer.parseInt(prop.getProperty("MainServer.port"));
            MainServer_host = prop.getProperty("MainServer.host");
            System.out.println("Port du serveur: " + MainServer_port);
            System.out.println("Hôte du serveur: " + MainServer_host);
        } catch (Exception e) {
            System.err.println("Erreur dans les propriétés du serveur: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Récupérer les workers à partir de la configuration
        int numberOfWorkers = getSelvers(prop);
        System.out.println("Nombre de Workers : " + numberOfWorkers);

        // Créer le serveur socket
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(MainServer_port);
            executor = Executors.newFixedThreadPool(10);
            taskQueue = new TaskQueue();

            // Configuration du socket pour réutiliser l'adresse
            ss.setReuseAddress(true);
            ss.setSoTimeout(0);

            System.out.println("Serveur démarré sur le port " + MainServer_port);

            // Lancer des threads workers pour exécuter les tâches
            for (int i = 0; i < 7; i++) {
                executor.execute(new ServerThread(taskQueue));
            }

            // Écouter les connexions des clients
            while (true) {
                try {
                    // Accepter la connexion du client
                    Socket soc = ss.accept();
                    System.out.println("+(" + TaskID + ") : Nouvelle tâche dans la queue");
                    Task newTask = new Task(soc, TaskID++);
                    taskQueue.add(newTask);
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'acceptation de la connexion : " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (IOException e1) {
            System.err.println("Erreur lors de la création du serveur socket : " + e1.getMessage());
            e1.printStackTrace();
        }
    }

    // Méthode pour récupérer les workers à partir des propriétés
    public static int getSelvers(Properties prop) {
        String hostSlv = prop.getProperty("Worker1.host");
        String portSlv = prop.getProperty("Worker1.port");
        int i = 1;
        while (hostSlv != null) {
            WorkerDataList.addWorker("rmi://" + hostSlv + ":" + portSlv + "/Worker");
            System.out.println("Worker ajouté : rmi://" + hostSlv + ":" + portSlv + "/Worker");
            i++;

            hostSlv = prop.getProperty("Worker" + i + ".host");
            portSlv = prop.getProperty("Worker" + i + ".port");
        }
        return i - 1;
    }
}
