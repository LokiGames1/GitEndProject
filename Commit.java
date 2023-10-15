import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.util.ArrayList;

public class Commit {
    String parentCommit = "";
    String nextCommit = "";
    String treeSha;
    String summary;
    String author;
    Date date;
    String sha;

    public static void main(String[] args) throws Exception {
        Commit test = new Commit("this is a test commit", "William Abraham");
        test.commitAndWrite();
    }

    public Commit(String parentCommit, String summary, String author) throws Exception {
        this.parentCommit = parentCommit;
        this.summary = summary;
        this.author = author;
        treeSha = createTree();
        this.date = new Date(java.lang.System.currentTimeMillis());
    }

    // optional constructor without parentCommit parameter
    public Commit(String summary, String author) throws Exception {
        try {
            String parent = Utils.getFileContents(new File("HEAD"));
            if (parent != "") {
                this.parentCommit = parent;
            }
        } catch (Exception e) {
        }
        this.summary = summary;
        this.author = author;
        treeSha = createTree();

        this.date = new Date(java.lang.System.currentTimeMillis());
    }

    public void commitAndWrite() throws Exception {
        try {
            FileWriter commit = new FileWriter("objects/commit");
            BufferedWriter writeToCommit = new BufferedWriter(commit);

            writeToCommit.append(treeSha + "\n");
            writeToCommit.append(parentCommit + "\n");
            writeToCommit.append(nextCommit + "\n");
            writeToCommit.append(author + "\n");
            writeToCommit.append(getDate() + "\n");
            writeToCommit.write(summary);
            writeToCommit.close();

            // creates blob of commit contents
            Blob hashed = new Blob("objects/commit");
            sha = hashed.getSHA();

            File toRemove = new File("objects/commit");
            toRemove.delete();

            FileWriter headFile = new FileWriter("HEAD");
            BufferedWriter writer = new BufferedWriter(headFile);
            writer.write(sha);
            writer.close();
            if (parentCommit != "") {
                Commit.addNext(parentCommit, sha);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addNext(String previous, String next) throws Exception {
        try {
            String read = Utils.readFromCompressedFile(new File("./objects/" + previous));

            String[] lines = read.split("\\r?\\n");

            String content = (lines[0] + "\n" + lines[1] + "\n" + next + "\n" + lines[3] + "\n" + lines[4]
                    + "\n" + lines[5]);
            byte[] compressed = Blob.compressToBinary(content);
            Blob.writeToFile(compressed, "./objects/" + previous);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSHA() {
        return sha;
    }

    // local String gets local region time format
    public String getDate() {
        return date.toLocaleString(); 
    }


    public String createTree() throws Exception {
        Git toInit = new Git();
        toInit.init();

        Tree init = new Tree();
        ArrayList<String> lines = getIndexContents();
        String deletePlaceholder = "";
        if (lines.size() != 0) {
            for (String line : lines) {
                if (line.split("\\s+")[0].equals("*deleted*") || line.split("\\s+")[0].equals("*deletedD*")) {
                    if (parentCommit.equals("")) {
                        throw new Exception("need a parent commit to delete smthn");
                    } else {
                        
                        System.out.println("start delete: ");
                        String parentSha = Commit.getTreeSha1(parentCommit);
                        if (isInParent(parentSha, line.split("\\s+")[1], "")) {
                            deletePlaceholder = deleteFromParentFile(parentSha, line.split("\\s+")[1],
                                    deletePlaceholder);
                        }

                    }
                } else if (line.split("\\s+")[0].equals("*edited*") || line.split("\\s+")[0].equals("*editedD*")) {
                    if (parentCommit.equals("")) {
                        throw new Exception("need a parent commit to edit smthn");
                    } else {
                        
                        String parentSha = Commit.getTreeSha1(parentCommit);
                        if (isInParent(parentSha, line.split("\\s+")[1], "")) {
                            deletePlaceholder = deleteFromParentFile(parentSha, line.split("\\s+")[1],
                                    deletePlaceholder);
                        }
                        if (line.split("\\s+")[0].equals("*edited*")) {
                            init.add("blob : " + line.split("\\s+")[2] + " : " + line.split("\\s+")[1]);
                        } else if (line.split("\\s+")[0].equals("*editedD*")) {
                            init.add("tree : " + line.split("\\s+")[2] + " : " + line.split("\\s+")[1]);

                        }

                    }
                } else if (line.split("\\s+")[0].equals("blob") || line.split("\\s+")[0].equals("tree")) {
                    if (!parentCommit.equals("")) {
                        String parentSha = Commit.getTreeSha1(parentCommit);
                        if (!isInParent(parentSha, line.split("\\s+")[4], "")) {
                            init.add(line);
                        }
                    } else {
                        init.add(line);
                    }
                } else {
                    init.add(line);
                }
            }
            if (!deletePlaceholder.equals("")) {
                String[] splits = deletePlaceholder.split("\\r?\\n");
                for (String split : splits) {
                    init.add(split);
                }
            }
        } else {
            init.add("");
        }
        if (!parentCommit.equals("")) {
            init.add("tree : " + Commit.getTreeSha1(parentCommit));
        }
        init.writeToTree();
        return init.getSha();
    }

    private ArrayList<String> getIndexContents() {
        ArrayList<String> contents = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader("index"))) {
            String line;
            while ((line = br.readLine()) != null) {
                contents.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // clearing the contents
        try (PrintWriter pw = new PrintWriter("index")) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    public static String getTreeSha1(String commitSha) throws Exception {
        String contents = Utils.readFromCompressedFile(new File("./objects/" + commitSha));
        String[] lines = contents.split("\\r?\\n");
        return lines[0];
    }

    private String deleteFromParentFile(String sha, String filename, String results) throws IOException {
        String submit = "";
        String[] lines;
        String shaParent = "";
        boolean hasHit = false;
        boolean hasTreeStar = false;
        if (results.equals("")) {
            String str = Utils.getFileContents(new File("./objects/" + sha));
            lines = str.split("\\r?\\n");
        } else {
            lines = results.split("\\r?\\n");
        }
        for (String line : lines) {
            String[] split = line.split("\\s+");
            if (split.length == 1) {
                continue;
            }

            if (split[0].equals("tree*") && split.length == 3 && hasHit == false) {
                if (split[2].equals("null")) {
                    hasTreeStar = true;
                    continue;
                }
                if (isInParent(split[2], filename, "")) {
                    submit += deleteFromParentFile(split[2], filename, "");
                }
                hasTreeStar = true;
                continue;
            }

            // this is where we go down into the previous tree
            if (split[0].equals("tree") && split.length == 3 && hasHit == false && hasTreeStar == false) {
                if (isInParent(split[2], filename, "")) {
                    submit += deleteFromParentFile(split[2], filename, "");
                }
                continue;
            } else if (split[0].equals("tree") && split.length == 3 && hasHit == true && hasTreeStar == false) {
                shaParent = split[2];
                continue;
            } else if (split[0].equals("tree") && split.length == 3 && hasTreeStar == true) {
                continue;
            }
            if (hasHit && split[0].equals("tree*")) {
                shaParent = split[2];
                hasTreeStar = true;
            }
            if (split[0].equals("tree*")) {
                continue;
            }
            if (!split[4].equals(filename)) {
                submit += line;
                submit += '\n';
            } else {
                hasHit = true;
            }
        }
        if (hasHit == true) {

            // tree* signifies to skip the contents of this tree, b/c the tree below it will
            // still have useful information but need to know that we have edited the
            // contents of this tree b/c our new tree will still point to the parent even if
            // its contents is outdated
            // note, this tree does not have to be the direct parent, can be a grandparent,
            // makes checkout a lot easier
            if (shaParent.equals("")) {
                submit += "tree* : " + "null";
            } else {
                submit += "tree* : " + shaParent;
            }
        }
        return submit;
    }

    private Boolean isInParent(String sha, String filename, String results) throws IOException {
        String[] lines;
        if (results.equals("")) {
            String str = Utils.getFileContents(new File("./objects/" + sha));
            lines = str.split("\\r?\\n");
        } else {
            lines = results.split("\\r?\\n");
        }
        for (String line : lines) {
            String[] split = line.split("\\s+");

            // this is where we go down into the previous tree
            if (split[0].equals("tree*") && split.length == 3) {
                if (split[2].equals("null")) {
                    return false;
                }
                return isInParent(split[2], filename, "");
            }
            if (split[0].equals("tree") && split.length == 3) {
                return isInParent(split[2], filename, "");
            }
            if (split[4].equals(filename)) {
                return true;
            }
        }
        return false;
    }

}