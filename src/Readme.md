# Instructions de compilation et d'exécution

## Compilation

```sh
javac -cp . Client/Client.java
javac -cp . MainServer/MainServer.java
javac -cp . Worker/Worker.java

## Exécution
java -cp . Client.Client
java -cp . MainServer.MainServer
### Workers
Nous avons 4 Workers, chacun écoutant sur un port spécifique :
Worker	Port
Worker1	1000
Worker2	1001
Worker3	1002
Worker4	1003
### Lancer un Worker en précisant son port :
java -cp . Worker.Worker 1000

Remplacez 1000 par le port correspondant pour les autres Workers.