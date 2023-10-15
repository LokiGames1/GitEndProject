import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Tree {

    // the method should accept a filename, OR a tree string
    // tree : HASH : folderName
    public void add(String fileName) throws IOException {
        File fileToAdd = new File(fileName);

        if (!fileToAdd.exists()) {
            String isTree = fileName.substring(0, 6);

            if (!isTree.equals("tree :")) {
                throw new FileNotFoundException("Invalid file to add");
            }
        }

        if (fileToAdd.exists()) {
            String fileContents = Blob.readFile(fileName);
            String hashOfFile = Blob.doSha(fileContents);

            String newEntryForTree = "blob : " + hashOfFile + " : " + fileName;

            // write the entry to the tree
            // Blob.writeToFile(fileName, fileContents);

        }

    }

    // Tester example
    // add file 1
    // add file 2
    // remove file 1
    // generateblob()
}
