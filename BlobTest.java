import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BlobTest {
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        Utils.writeStringToFile("test1.txt", "this is test 1");
        Utils.writeStringToFile("test2.txt", "this is test 2");
        Git git = new Git();
        git.init();
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        Utils.deleteFile("test1.txt");
        Utils.deleteFile("test2.txt");
        Utils.deleteFile("test3.txt");
        Utils.deleteFile("index");
        Utils.deleteDirectory("./objects");
    }

    @Test
    void testCompressToBinary() {
        // no real way to test due to zip compressiong varying.
    }

    @Test
    void testGenerateSHA() throws Exception {
        // Blob blob = new Blob("test1.txt"); 
        String hash1 = Utils.compressAndHash("this is test 1");
        File file1 = new File("./objects/" + hash1);
        // byte[] bytes = Utils.compressToBinary("test1.txt");
        assertTrue("Blob is not created", file1.exists());
        assertEquals("Blob has the correct contents", Utils.readFromCompressedFile(file1),
                "this is test 1");

    }

    @Test
    void testGetSHA() throws Exception {
        Blob blob = new Blob("test1.txt");
        String hash1 = Utils.compressAndHash("this is test 1");
        assertEquals("Get sha is wrong", hash1, blob.getSHA());
    }

    @Test
    void testReadFile() throws Exception {
        // Blob blob = new Blob("test1.txt"); this line isn't really needed
        File file = new File("test1.txt");
        assertEquals("Read file is wrong", Utils.getFileContents(file), Blob.readFile("test1.txt"));
    }

    @Test
    void testWriteToFile() throws Exception {
       // Blob blob = new Blob("test1.txt"); neither is this line. I just had them (ln 54) bc it makes sense to
        byte[] bytes = Utils.compressToBinary("test1.txt");
        Blob.writeToFile(bytes, "test2.txt");
        File file2 = new File("test3.txt");
        Utils.writeToFile(bytes, "test3.txt");
        File file = new File("test2.txt");
        assertEquals("Write to file is wrong", Utils.getFileContents(file), Utils.getFileContents(file2));

    }
}