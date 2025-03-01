package RessourcesForRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

import MainServer.ImageDivider.SubMatrix;
/**
 * Filters
 */
public interface Filters extends Remote {

    public SubMatrix applyFilter(SubMatrix inputMatrix, int[][] ker) throws RemoteException;

}

/* L'interface Filters définit un service distant RMI (Remote Method Invocation) permettant d'appliquer un filtre de convolution sur une sous-matrice d'image (SubMatrix). */
