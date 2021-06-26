package blockchain;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

class StringUtil {
    /* Applies Sha256 to a string and returns a hash. */
    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class Block implements Serializable{
    private final int id;
    private final long timeStamp ;
    private final String prevHash;
    private final String content;
    private final String currentHash;
    private final String statusOfZero;
    private final int miner;
    private long magicNumber;
    private final int numOfZero;
    private final long timeTaken;
    private int newZero;
    public static final long serialVersionUID = 1L;


    public Block(int id, long timeStamp, String prevHash, String provedHash, String content, int numOfZero, int miner, long finishedTime, long magicNumber) {
        this.id = id;
        this.miner = miner;
        this.timeStamp = timeStamp;
        this.prevHash = prevHash;
        this.content = content;
        this.numOfZero = numOfZero;// it must be initialized before provedHash as provedHash uses this
        this.currentHash = provedHash;
        this.timeTaken = (finishedTime - timeStamp ) / 1000; // ms to s
        this.statusOfZero = calcChange();
        this.magicNumber = magicNumber;

    }

    public String getStatusOfZero() {
        return statusOfZero;
    }

    public int getNewZero() {
        return newZero;
    }

    private String calcChange() {
        if (this.timeTaken < 15) {
            this.newZero = numOfZero + 1;
            return "N was increased to " + newZero;
        } else if (this.timeTaken < 60 ) {
            return "N stays the same";
        } else {
            this.newZero = numOfZero - 1;
            return  "N was decreased by 1";
        }
    }

    public int getId() {
        return id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public int getMiner() {
        return miner;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public String getContent() {
        return content;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public long getTimeTaken() {
        return timeTaken;
    }
}

class BlockChain implements Serializable {
    private static Block[] block;
    private int size;
    private StringUtil getHash;
    private final int numOfZero;
    private static final long serialVersionUID = 1L;// final or not?

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getNumOfZero() {
        return numOfZero;
    }

    public BlockChain(int size, int numOfZero) {
        this.size = size;
        this.numOfZero = numOfZero;
        this.block = checkFile();

    }

    public boolean addBlock(Block temp) {

        Thread t = Thread.currentThread();

        if (temp.getId() < block.length) {
            // System.out.println(blockValid(temp) + "the block validitiy");
            //checking null i.e if it is empty or not
            if (blockValid(temp) && block[temp.getId() - 1] == null) {
                this.block[temp.getId() - 1] = temp;
                // System.out.println("block is added by " + t.getName());
                return true;
            }

        }


       // System.out.println("Block not added");
        return false;

    }


    private Block[] checkFile() {
        try {
            Block[] existingBlock = deserialize("block.txt");

            // System.out.println("existing block size" + existingBlock.length); it is always 5 why
            if (existingBlock != null) {

                this.block = existingBlock;
            }
            //print();
            if (existingBlock != null && isValid()) {
                 // because up to that id is full
                this.size = 2 * block.length;
               // System.out.println(this.size); //id = 10 at second pass
                Block[] temp = new Block[this.size];// dont remember to use this

                int j = 0;
                for (Block i: block) {
                    temp[j] = i;
                    j++;
                }
               // System.out.println("the data in the text file was j = " + j +" and temp length is " + temp.length );
                return temp;
            } else {
               // System.out.println("no any valid file");
                return new Block[size];
            }
        } catch (Exception e) {
           // System.out.println("not valid file");
           // e.printStackTrace();
            return new Block[size];
        }
    }



    public static Block[] deserialize(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("C:\\Users\\Acer\\IdeaProjects\\Blockchain\\Blockchain\\task\\src\\blockchain\\" + fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (Block[]) ois.readObject();

    }

    public static void serialize(Block[] blockchain, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);// when append was true it read the first time written array block object
        BufferedOutputStream bos = new BufferedOutputStream(fos);// as there is always first block of array at the beginning
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        List<Block> values = new ArrayList<Block>();
        for(Block data: blockchain) {
            if(data != null) {
                values.add(data);
            }
        }// removing null as this method writes null object as well

        blockchain = values.toArray(new Block[0]);
        oos.writeObject(blockchain);
        oos.flush();
        oos.close();
       // System.out.println("serialize complete");
    }

    public Block[] getBlock() {
        return block;
    }

    public boolean isValid(){

        String tempHash = "0";
        Block temp = null;
        for (int i = 0; i < block.length  && block[i] != null; i++) {
            temp = block[i];
            if (!temp.getPrevHash().equals(tempHash)) { // dont use !=
                //System.out.println("false");
                return false;
            }

            tempHash = temp.getCurrentHash();
        }
        //System.out.println("true");
        return true;
    }

    // must return null if no any lasy block
    public Block getLastBlock() {// dont call last block when 0 is null
        int i;
       // System.out.println("block length in last block " + this.block.length);

        int nullLocation = -1;
        for ( i = 0; i < block.length ; i++) {
                if (block[i] == null ) {
                    //first null found
                    nullLocation = i;
                    break;
                }
            }

        if (nullLocation == -1) {
            //System.out.println("no null and the last block is " + (block.length - 1));
            return block[block.length - 1];
        } else if (block[0] == null) {
           // System.out.println("First item is null so, return null ");
            return null;

        } else {
            return block[nullLocation - 1];
        }


    }
    public  boolean blockValid(Block temp) {

        if (temp.getId() == 1 && this.block[0] == null) {
            return true;
        }
        // remember while auto getters
        if (getLastBlock() != null && getLastBlock().getCurrentHash().equals(temp.getPrevHash())) {
            return true;
        } else {
            return false;
        }

    }


    public void print() {
        for (int i = 0; i < block.length  && block[i] != null; i++) {
            Block temp = block[i];
            System.out.println("Block:");
            System.out.println("Created by miner # " + temp.getMiner());
            System.out.println("Id:" + " " + temp.getId());
            System.out.println("Timestamp: " + temp.getTimeStamp());
            System.out.println("Magic number: " + temp.getMagicNumber());
            System.out.println("Hash of the previous block:\n" + temp.getPrevHash());
            System.out.println("Hash of the block:\n" + temp.getCurrentHash());
            System.out.println("Block was generating for "+ temp.getTimeTaken()+" seconds");
            System.out.println(temp.getStatusOfZero() + "\n");
            System.out.println("\n");
        }
    }
}


interface Command {

    void execute();

    void Upload();
}

class mineCommand implements blockchain.Command {


    private Magic mFinder;

    public mineCommand(Magic mFinder) {
        this.mFinder = mFinder;
    }
    @Override
    public void execute() {
        mFinder.mine();
    }

    public void Upload() {
        mFinder.Upload();
    }

}

class Miner implements Runnable{//invoker
    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void executeCommand() {
        command.execute();
    }
    @Override
    public void run() {
        int j = 5;
        while(j-- > 0) {

            executeCommand();
            executeUpload();
        }
    }

    private void executeUpload() {
        synchronized (Miner.class) {
            command.Upload();
        }
    }

}

class Magic {
    BlockChain ledger;
    private long magicNumber = 0;
    private Block lastBlock;
    private Block temp;
    private int id;
    private String prevHash;
    private String content;
    private int miner;
    private int numOfZero;
    private long timestamp;

    public Magic(BlockChain ledger) {
        this.ledger = ledger;
    }

    public void mine() {

        Thread t = Thread.currentThread();
        createNewBlock();


        String zero = "0".repeat(Math.max(0, numOfZero));
        String provedHash = null;
        long finishedTime;

        while (true) {

            provedHash = blockchain.StringUtil.applySha256(getString(magicNumber));
            if (provedHash.startsWith(zero)) {
                finishedTime =  new Date().getTime();
                break;
            }
            this.magicNumber++;
        }

        temp = new Block(id, timestamp, prevHash, provedHash, content, numOfZero, miner, finishedTime, magicNumber);

    }

    private void createNewBlock() {
        Thread t = Thread.currentThread();
        if (ledger.getLastBlock() != null) {

           // System.out.println("Block with id 2 is here");
            lastBlock = ledger.getLastBlock();

            id = lastBlock.getId() + 1;
            timestamp = new Date().getTime();
            prevHash = lastBlock.getCurrentHash();
            content = "nth";
            miner = Integer.parseInt(t.getName());
            numOfZero = lastBlock.getNewZero();
        } else {
            id = 1;
            timestamp = new Date().getTime();
            prevHash = "0";
            content = "nth";
            miner = Integer.parseInt(t.getName());
            numOfZero = ledger.getNumOfZero();
        }
    }

    private String getString(long magicNumber){
        return content + id + timestamp + prevHash + magicNumber;
    }

    public void Upload() {
        Thread t = Thread.currentThread();
        ledger.addBlock(temp);
        //System.out.println("In this critical section :The thread is" + t.getName());
    }
}


public class Main {
    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        //System.out.println("Enter how many zeros the hash must start with:");
        int n = 0;//sc.nextInt(); at third  project n initialized at 0

        BlockChain ledger = new BlockChain(6, n);


        Thread[] t = new Thread[1];

        int i = t.length - 1;




            for (int j = 0; j < t.length; j++) {
                Magic magic = new Magic(ledger);
                Command command = new mineCommand(magic);
                Miner miner = new Miner();;
                miner.setCommand(command);
                t[j] = new Thread(miner);

                t[j].setName(String.valueOf(j));

                t[j].start();
            }


            for (int j = 0; j < t.length; j++) {
                try {
                    t[j].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        try {
           // ledger.serialize(ledger.getBlock(), "C:\\Users\\Acer\\IdeaProjects\\Blockchain\\Blockchain\\task\\src\\blockchain\\block.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ledger.print();
    }
}