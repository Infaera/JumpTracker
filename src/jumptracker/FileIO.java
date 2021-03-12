package jumptracker;

import java.awt.HeadlessException;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/*@author Infaera*/
public class FileIO {

    private static String str;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static DataInputStream din;
    private static DataOutputStream dout;
    private static BufferedInputStream ins;
    private static BufferedOutputStream outs;

    public static void renameFile(File file, String name) throws Exception {

        if (!file.exists() || file.isDirectory()) {
            throw new Exception("File Does Not Exist.");
        }

        File nfile = new File(file.getParent() + "/" + name);

        if (nfile.exists()) {
            throw new Exception("File Already Exists.");
        }

        if (file.renameTo(nfile)) {
            System.out.println("File has been renamed.");
        } else {
            throw new Exception("Error renmaing file");
        }
    }

    public static File convertToFile(String str) {
        return new File(str);
    }

    public static void objectWrite(String path, Object o) {
        try (FileOutputStream fos = new FileOutputStream(path);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(o);
            oos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static Object objectRead(String path) {
        Object o = null;
        try (FileInputStream fis = new FileInputStream(path);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            o = ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return o;
    }

    public static String bufferedRead(String file) throws Exception {
        return bufferedRead(new File(file));
    }

    public static String bufferedRead(File file) throws Exception {
        str = "";
        
        //System.out.println("Readering: "+file.getAbsoluteFile().getAbsolutePath());
        FileReader reader = new FileReader(file.getAbsoluteFile());
        in = new BufferedReader(reader);

        while (in.ready()) {
            str += in.readLine() + (in.ready()?"\n":"");
        }
        
        in.close();
        return str;
    }

    public static void bufferedWrite(String file, String str) throws Exception {
        bufferedWrite(new File(file), str);
    }

    public static void bufferedWrite(File file, String str) throws Exception {
        (out = new BufferedWriter(new FileWriter(file))).write(str);
        out.close();
    }

    public static ArrayList<Integer> bufferedSteamread(File file) throws Exception {

        //byte[] buf = new byte[1024];
        ArrayList<Integer> ints = new ArrayList<>();
        ins = new BufferedInputStream(new FileInputStream(file));

        for (int len; (len = ins.read(/*buf*/)) >= 0;) {
            ints.add(len);
            //outs.write(buf, 0, len);
        }

        ints.toArray();
        ins.close();
        return ints;

    }

    public static void bufferedStremWrite(File file, Integer[] ints) throws Exception {

        //byte[] buf = new byte[1024];
        outs = new BufferedOutputStream(new FileOutputStream(file));

        for (Integer int1 : ints) {
            outs.write(int1);
        }

        outs.close();
    }

    public static byte[] readBytes(File file) throws Exception {

        din = new DataInputStream(new FileInputStream(file));
        byte[] bytes = new byte[din.available()];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = din.readByte();
        }

        din.close();
        return bytes;
    }

    public static byte[] readBytes(File file, int length) throws Exception {
        din = new DataInputStream(new FileInputStream(file));
        if (length > din.available()) {
            throw new Exception("Out of Bounds");
        }

        byte[] bytes = new byte[length];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = din.readByte();
        }

        din.close();
        return bytes;
    }

    public static byte[] readBytes(File file, long offset, int length) throws Exception {
        if (offset > file.length()) {
            throw new Exception("Out of Bounds");
        }

        byte[] bytes = new byte[(int) (length)];
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            for (int i = 0; i < bytes.length; i++) {
                raf.seek(offset + i);
                bytes[i] = raf.readByte();
            }
        }

        return bytes;
    }

    public static int readInt(File file, long offset) throws Exception {
        int num;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            num = raf.readInt();
        }
        return num;
    }

    public static void writeBytes(File file, byte[] bytes) throws Exception {
        dout = new DataOutputStream(new FileOutputStream(file));
        dout.write(bytes);
        dout.flush();
        out.close();
    }

    public static ArrayList<File> getFileList(File dir) throws Exception {

        if (!dir.exists() || dir == null) {
            throw new Exception("File Not Found");
        }

        ArrayList<File> fileList = new ArrayList<>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                fileList.addAll(getFileList(f));
            } else if (f.isFile()) {
                fileList.add(f);
            }
        }
        return fileList;
    }

    public static int getFileCount(File dir) throws Exception {
        int count = 0;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                count += getFileCount(f);
            } else {
                count++;
            }
        }
        return count;
    }

    public static File getFile() {
        return getFile(new File(""));
    }

    public static File getFile(String openFile) {
        return getFile(new File(openFile));
    }

    public static File getFile(File openFile) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Choose a File");
            chooser.setCurrentDirectory(openFile);
            chooser.showOpenDialog(chooser);
            return chooser.getSelectedFile();
        } catch (HeadlessException ex) {
            return null;
        }
    }

    public static File getDir() {
        return getDir(new File(""));
    }

    public static File getDir(String openFile) {
        return getDir(new File(openFile));
    }

    public static File getDir(File openFile) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Choose a Directory");
            chooser.setCurrentDirectory(openFile);
            chooser.showOpenDialog(chooser);
            return chooser.getSelectedFile();
        } catch (HeadlessException ex) {
            return null;
        }
    }

    public static File get(File openFile) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(openFile);
            chooser.setDialogTitle("Choose a File or Directory Name");
            chooser.setSelectedFile(openFile);
            chooser.showSaveDialog(chooser);
            return chooser.getSelectedFile();
        } catch (HeadlessException ex) {
            return null;
        }
    }

    public static File saveDialog(File path) {
        JFrame parentFrame = new JFrame();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save to Location");

        int userSelection = fileChooser.showSaveDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
            return fileToSave;
        } else {
            return null;
        }
    }

    public static File saveDialog(String path) {
        return saveDialog(new File(path));
    }

    public static String removeAlphabet(String text) {
        text = text.toLowerCase();
        String ntext = "";
        for (char c : text.toCharArray()) {
            if (c < 97 || c > 122) {
                ntext += c;
            }
        }
        return ntext.replace("[", "").replace("]", "");
    }
}
