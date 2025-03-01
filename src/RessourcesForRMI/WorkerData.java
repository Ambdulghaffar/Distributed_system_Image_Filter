package RessourcesForRMI;


public class WorkerData{
    public String linkRMI;
    public int dispo;

    public WorkerData(String linkRMI,int dispo){
        this.linkRMI=linkRMI;
        this.dispo=dispo;
    }

    public WorkerData(String linkRMI){
        this.linkRMI=linkRMI;
        this.dispo=1;
    }
}

/* La classe WorkerData représente un travailleur distant (worker) utilisé pour le traitement distribué d’images via RMI (Remote Method Invocation). */

/* WorkerData joue un rôle central dans la distribution des tâches de filtrage d'images via RMI.
Il permet une gestion dynamique des workers disponibles et facilite le traitement parallèle. */