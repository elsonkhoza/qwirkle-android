package za.nmu.wrpv.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import za.nmu.wrpv.messages.Package;
import za.nmu.wrpv.qwirkle.Tile;

public class Player {

    // Game
    public String name;
    public int score,number;
    public List<Tile> hand;

    // Server Communication
    private BlockingQueue<Package> outMessages;
    private String ip;

    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;

    private Receiver receiver;

    private ReadThread readThread;
    private WriteThread writeThread;

    public Player(String name, Receiver receiver) {
        this.receiver = receiver;
        this.name = name;
        this.score = 0;
        this.hand = new ArrayList<>();



        this.hand.add(new Tile(2, 5));
        this.hand.add(new Tile(2, 2));
        this.hand.add(new Tile(2, 3));
        this.hand.add(new Tile(4, 3));
        this.hand.add(new Tile(5, 3));
        this.hand.add(new Tile(3, 5));


        outMessages = new LinkedBlockingQueue<>();

    }

    public void connectToServer(String ip) {
        this.ip = ip;

        readThread = new ReadThread();
        readThread.start();
    }

    public void sendPackage(Package p) {
        try {
            outMessages.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {

            Socket connection = null;
            try {
                connection = new Socket(ip, 5050);

                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();

                writeThread = new WriteThread();
                writeThread.start();

                Package p;
                do {

                    p = (Package) in.readObject();
                    receiver.receive(p);

                } while (!isInterrupted());

                connection.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {

            }

        }
    }

    private class WriteThread extends Thread {

        @Override
        public void run() {

            while (true) {

                Package p = null;
                try {
                    p = outMessages.take();

                    out.writeObject(p);
                    out.flush();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                } finally {

                }

            }

        }

    }



}
