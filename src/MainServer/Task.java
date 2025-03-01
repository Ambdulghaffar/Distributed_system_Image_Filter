package MainServer;

import java.net.Socket;

public class Task {
    Socket client;// socket to send back the result to client
    String Task; // The task choosed by client
    int[][] image;
    int TaskId;

    public Task(Socket client,int TaskId) {
        this.client = client;
        this.TaskId=TaskId;
    }

    public void execute() {

    }
}

/* La classe Task représente une tâche qui doit être exécutée par le serveur. Elle contient les informations essentielles pour traiter une requête client. */

/* Task est une simple structure de données utilisée pour organiser les requêtes entrantes.
Elle ne contient pas de logique métier (le traitement est géré dans ServerThread).
Elle joue un rôle clé dans la file d’attente (TaskQueue), en permettant de stocker et gérer les tâches en attente d’exécution. */