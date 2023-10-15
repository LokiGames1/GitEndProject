import java.io.*;
import java.security.NoSuchAlgorithmException;


public class Git {
    public static void main(String[] args) throws Exception {
        Git git = new Git();
        git.init();

        git.editedDir("penis");

    }

    public void init() throws IOException {
        File dir = new File("./objects");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File index = new File("index");
        if (!index.exists()) {
            index.createNewFile();
        }
    }

    public void add(String fileName) throws NoSuchAlgorithmException, IOException {
        Blob newBlob = new Blob(fileName);
        String sha = newBlob.getSHA();
        if (existsAlready(fileName, sha)) {
            return;
        }
        // remove(fileName);

        FileWriter fileWriter = new FileWriter("index", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        File file = new File("index");
        if (file.length() != 0) {
            bufferedWriter.write('\n');
        }
        bufferedWriter.write("blob : " + sha + " : " + fileName);
        bufferedWriter.close();
        fileWriter.close();

    }

    public void addDirectory(String directory) throws Exception {

        Tree tree = new Tree();
        tree.addDirectory(directory);
        tree.writeToTree();
        String sha = tree.getSha();
        if (existsAlready(directory, sha)) {
            return;
        }

        FileWriter fileWriter = new FileWriter("index", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        File file = new File("index");
        if (file.length() != 0) {
            bufferedWriter.write('\n');
        }
        bufferedWriter.write("tree : " + sha + " : " + directory);
        bufferedWriter.close();
        fileWriter.close();
    }

    public void remove(String fileName) throws IOException {
        File ogFile = new File("index");
        File temp = new File("temp.txt");

        BufferedReader br = new BufferedReader(new FileReader(ogFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));

        String line;
        Boolean isFirst = true;
        while (br.ready()) {
            line = br.readLine();

            // splits at the space, your welcome
            String[] name = line.split("\\s+");

            if (!name[4].equals(fileName)) {
                if (!isFirst) {
                    bw.write('\n');

                }
                isFirst = false;
                bw.write(line);
                // if (br.ready()) {
                // bw.write('\n');
                // }
            } else {
                System.out.println("ran");
                // I think we aint supposed to delete blob
                // File deleteFile = new File("./objects/" + name[2]);
                // deleteFile.delete();
            }

        }
        br.close();
        bw.close();
        ogFile.delete();
        temp.renameTo(ogFile);

    }

    public void delete(String fileName) throws IOException {
        if (existsAlready(fileName, "")) {
            return;
        }
        FileWriter fileWriter = new FileWriter("index", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        File file = new File("index");
        if (file.length() != 0) {
            bufferedWriter.write('\n');
        }
        bufferedWriter.write("*deleted* " + fileName);
        bufferedWriter.close();
        fileWriter.close();
    }

    public void edited(String fileName) throws IOException, NoSuchAlgorithmException {
        if (existsAlready(fileName, "")) {
            return;
        }
        Blob newBlob = new Blob(fileName);
        String sha = newBlob.getSHA();
        FileWriter fileWriter = new FileWriter("index", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        File file = new File("index");
        if (file.length() != 0) {
            bufferedWriter.write('\n');
        }
        bufferedWriter.write("*edited* " + fileName + " " + sha);
        bufferedWriter.close();
        fileWriter.close();
    }

    public void deleteDir(String fileName) throws IOException {
        if (existsAlready(fileName, "")) {
            return;
        }
        FileWriter fileWriter = new FileWriter("index", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        File file = new File("index");
        if (file.length() != 0) {
            bufferedWriter.write('\n');
        }
        bufferedWriter.write("*deletedD* " + fileName);
        bufferedWriter.close();
        fileWriter.close();
    }

    public void editedDir(String fileName) throws Exception {
        if (existsAlready(fileName, "")) {
            return;
        }
        Tree tree = new Tree();
        tree.addDirectory(fileName);
        tree.writeToTree();
        String sha = tree.getSha();
        FileWriter fileWriter = new FileWriter("index", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        File file = new File("index");
        if (file.length() != 0) {
            bufferedWriter.write('\n');
        }
        bufferedWriter.write("*editedD* " + fileName + " " + sha);
        bufferedWriter.close();
        fileWriter.close();
    }

    public boolean existsAlready(String fileName, String hash) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("index"));

        String line;
        while (br.ready()) {
            line = br.readLine();
            String[] name = line.split("\\s+");
            if (name[0].equals("*deleted*") || name[0].equals("*edited*") || name[0].equals("*deletedD*")
                    || name[0].equals("*editedD*")) {
                if (name[1].equals(fileName)) {
                    br.close();
                    return true;
                }
                continue;
            }
            if (name[4].equals(fileName) && name[2].equals(hash)) {
                br.close();
                return true;
            }

        }
        br.close();
        return false;

    }

    public void checkout(String commitSha) throws Exception {
        String mainTree = Commit.getTreeSha1(commitSha);
        String mainContents = Utils.getFileContents(new File("./objects/" + mainTree));
        String[] lines = mainContents.split("\\r?\\n");
        boolean hasTreeStar = false;
        for (String line : lines) {
            String[] split = line.split("\\s+");
            if (split[0].equals("blob")) {
                String contents = Utils.readFromCompressedFile(new File("./objects/" + split[2]));
                Utils.writeStringToFile(split[4], contents);

            } else if (split[0].equals("tree") && split.length == 5) {
                checkoutRecursive(split[2], "./" + split[4]);
            } else if (split[0].equals("tree*") && !split[2].equals("null")) {
                checkoutRecursive(split[2], "");
                hasTreeStar = true;
            } else if (split[0].equals("tree") && hasTreeStar == false && split.length == 3) {
                checkoutRecursive(split[2], "");
            }

        }

    }

    private void checkoutRecursive(String treeSha, String folder) throws Exception {
        if (!folder.equals("")) {
            Utils.makeDir(folder);
        }
        String mainContents = Utils.getFileContents(new File("./objects/" + treeSha));
        String[] lines = mainContents.split("\\r?\\n");
        boolean hasTreeStar = false;
        for (String line : lines) {
            String[] split = line.split("\\s+");
            if (split[0].equals("blob")) {
                String contents = Utils.readFromCompressedFile(new File("./objects/" + split[2]));
                if (!folder.equals("")) {
                    Utils.writeStringToFile(folder + "/" + split[4], contents);
                } else {
                    Utils.writeStringToFile(split[4], contents);

                }

            } else if (split[0].equals("tree") && split.length == 5) {
                checkoutRecursive(split[2], folder + "/" + split[4]);
            } else if (split[0].equals("tree*") && !split[2].equals("null")) {
                checkoutRecursive(split[2], folder);
                hasTreeStar = true;
            } else if (split[0].equals("tree") && hasTreeStar == false && split.length == 3) {
                checkoutRecursive(split[2], folder);
            }

        }

    }
}