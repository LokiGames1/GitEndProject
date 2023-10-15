import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;

public class Tree {
    String sha;
    File treeHoldingFile;
    File treeFile;
    ArrayList<String> list;

    public static void main(String[] args) throws Exception {
        Tree tree = new Tree();

        tree.addDirectory("./penis/");
        tree.writeToTree();
    }

    public Tree() {
        treeHoldingFile = new File("treeHoldingFile");
        list = new ArrayList<String>();
    }

    public String getSha() {
        return sha;
    }

    public void add(String treeEntry) throws Exception {
        String substring = "";
        if (treeEntry.length() > 4) {
            substring = treeEntry.substring(0, 4);
        }
        if (substring.equals("tree")) {
            String sha = treeEntry.substring(7, treeEntry.length());
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).contains(sha)) {
                    throw new Exception("already containts that tree");
                }
            }
        } else if (substring.equals("blob")) {
            String shaFileName = treeEntry.substring(treeEntry.indexOf(":") + 2, treeEntry.length());
            String fileName = shaFileName.substring(shaFileName.indexOf(":") + 2, shaFileName.length());
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).contains(fileName)) {
                    throw new Exception("already containts that file");
                }
            }
        }
        list.add(treeEntry);
    }

    public void remove(String treeEntry) {
        boolean removed = false;
        for (int i = 0; i < list.size() && removed == false; i++) {
            if (list.get(i).contains(treeEntry)) {
                list.remove(i);
                removed = true;
            }
        }
    }

    public void writeToTree() throws IOException {
        FileWriter writer = new FileWriter(treeHoldingFile);
        for (int i = 0; i < list.size() - 1; i++) {
            writer.write(list.get(i) + "\n");
        }
        writer.write(list.get(list.size() - 1));
        writer.close();
        String contents = getFileContents(treeHoldingFile);
        String shaString = getSHA(contents);
        if (treeFile != null && treeFile.exists()) {
            treeFile.delete();
        }
        treeFile = new File("./objects/" + shaString);
        FileWriter treeWriter = new FileWriter(treeFile);
        treeWriter.write(contents);
        treeWriter.close();
    }

    public String getFileContents(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
        StringBuilder contents = new StringBuilder();
        while (reader.ready()) {
            contents.append((char) reader.read());
        }
        reader.close();
        return contents.toString();
    }

    public String getSHA(String fileContents) {
        String sha1 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(fileContents.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.sha = sha1;
        return sha1;
    }

    // Used for sha1
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public String getListObject(int position) {
        return list.get(position);
    }

    public String addDirectory(String directoryPath) throws Exception {
        return addDirectory(directoryPath, false);

    }

    private String addDirectory(String directoryPath, boolean isRecursion) throws Exception {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
            throw new Exception("shut yo bitch ass up");
        }
        File files[] = dir.listFiles();
        if (files.length == 0) {
            add("");
        }
        for (File file : files) {
            if (file.isDirectory()) {
                Tree tempTree = new Tree();
                tempTree.addDirectory(file.getPath(), true);
                add("tree : " + tempTree.getSha() + " : " + file.getName());
            } else {
                Blob b = new Blob(file.getPath());
                add("blob : " + b.getSHA() + " : " + file.getName());
            }
        }

        writeToTree();

        return getSha();
    }
}