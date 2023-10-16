import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GitTest {
    Git git = new Git();

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        Utils.writeStringToFile("test1.txt", "this is test 1");
        Utils.writeStringToFile("test2.txt", "this is test 2");
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        Utils.deleteFile("test1.txt");
        Utils.deleteFile("test2.txt");
        Utils.deleteFile("testFile");
        Utils.deleteFile("index");
        Utils.deleteDirectory("./objects");
    }

    @Test
    void testInit() throws IOException {
        git.init();
        File objects = new File("./objects");
        assertTrue("Objects does not exist", objects.exists());
        File index = new File("index");
        assertTrue("Index does not exist", index.exists());
    }

    @Test
    void testAdd() throws Exception {
        git.init();
        git.add("test1.txt");
        String hash1 = Utils.compressAndHash("this is test 1");
        byte[] bytes = Utils.compressToBinary("test1.txt");
        File file1 = new File("./objects/" + hash1);
        Utils.writeToFile(bytes, "testFile");
        assertTrue("Blob is not created", file1.exists());

        File index = new File("index");
        assertEquals("Index does not have file", Utils.getFileContents(index), "blob : " + hash1 + " : " + "test1.txt");
    }

    @Test
    void testRemove() throws Exception {
        git.init();
        git.add("test1.txt");
        git.remove("test1.txt");
        String index = Utils.getFileContents(new File("index"));
        assertEquals("Blob is not removed", "", index);
    }

}