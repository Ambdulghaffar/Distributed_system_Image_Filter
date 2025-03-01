package MainServer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue {
    private BlockingQueue<Task> queue;

    public TaskQueue() {
        queue = new LinkedBlockingQueue<>();
    }

    public void add(Task task) {
        try {
            queue.put(task);
        } catch (InterruptedException e) {
        }
    }

    public Task take() throws InterruptedException {
        return queue.take();
    }
}

/* La classe TaskQueue est une file d'attente concurrente qui stocke les tâches (Task) en attente d'exécution. Elle permet de gérer efficacement les requêtes entrantes grâce à une file thread-safe. */
