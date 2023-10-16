import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class TreeTest {
    Tree tree1 = new Tree();

    @BeforeEach
    public void setup() throws IOException {
        File Objects = new File("objects");
        if (!Objects.exists()) {
            Objects.mkdirs();
        }
        File testDir = new File("test");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        Files.write(Paths.get("test/test.txt"), "t1".getBytes());
        Files.write(Paths.get("test/test1.txt"), "t2".getBytes());
        File testDir1 = new File("test/testD");
        if (!testDir1.exists()) {
            testDir1.mkdirs();
        }
        Files.write(Paths.get("test/testD/test2.txt"), "t3".getBytes());

        File testDir2 = new File("test2");
        if (!testDir2.exists()) {
            testDir2.mkdirs();
        }
        Files.write(Paths.get("test2/test.txt"), "t1".getBytes());
        Files.write(Paths.get("test2/test1.txt"), "t2".getBytes());
        File testDir3 = new File("test2/testD");
        if (!testDir3.exists()) {
            testDir3.mkdirs();
        }
        File testDir4 = new File("test2/testD/testD2");
        if (!testDir4.exists()) {
            testDir4.mkdirs();
        }

        Files.write(Paths.get("test2/testD/testD2.txt"), "t23".getBytes());

        File testDir5 = new File("test2/testD2");
        if (!testDir5.exists()) {
            testDir5.mkdirs();
        }
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        Path testDir = Path.of("./test/");
        deleteDirectory(testDir);
        Path testDir2 = Path.of("./test2/");
        deleteDirectory(testDir2);
        Path objects = Path.of("./objects/");
        deleteDirectory(objects);
        Utils.deleteFile("contents.txt");

    }

    public static void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    @Test
    void testAdd() throws Exception {
        tree1.add("blob : 81e0268c84067377a0a1fdfb5cc996c93f6dcf9f : file1.txt");
        assertEquals("Blob is not added to list.", tree1.getListObject(0),
                "blob : 81e0268c84067377a0a1fdfb5cc996c93f6dcf9f : file1.txt");

        tree1.add("tree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b");
        assertEquals("Tree is not added to list.", tree1.getListObject(1),
                "tree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b");

        boolean error1 = false;
        try {
            tree1.add("blob : 01d82591292494afd1602d175e165f94992f6f5f : file1.txt");
        } catch (Exception e) {
            error1 = true;
        }
        assertTrue("Added two blobs of the same file", error1);

        boolean error2 = false;
        try {
            tree1.add("tree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b");
        } catch (Exception e) {
            error2 = true;
        }
        assertTrue("Added two trees", error2);
    }

    @Test
    void testGetFileContents() throws IOException {
        File file = new File("contents.txt");
        Utils.writeStringToFile("contents.txt", "testing");
        assertEquals("File is not read correctly", tree1.getFileContents(file), "testing");

    }

    @Test
    void testGetListObject() throws Exception {
        tree1.add("blob : 01d82591292494afd1602d175e165f94992f6f5f : file2.txt");
        assertEquals("get list object works", "blob : 01d82591292494afd1602d175e165f94992f6f5f : file2.txt",
                tree1.getListObject(0));
    }

    @Test
    void testGetSHA() {
        assertEquals("get SHA is wrong", tree1.getSHA("testString"), "956265657d0b637ef65b9b59f9f858eecf55ed6a");
    }

    @Test
    void testRemove() throws Exception {
        tree1.add("blob : 01d82591292494afd1602d175e165f94992f6f5f : file2.txt");
        tree1.add("tree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b");
        tree1.add("blob : 81e0268c84067377a0a1fdfb5cc996c93f6dcf9f : file1.txt");

        tree1.remove("file2.txt");
        assertEquals("Did not remove file", tree1.getListObject(1),
                "blob : 81e0268c84067377a0a1fdfb5cc996c93f6dcf9f : file1.txt");

        tree1.remove("bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b");
        assertEquals("Did not remove tree", tree1.getListObject(0),
                "blob : 81e0268c84067377a0a1fdfb5cc996c93f6dcf9f : file1.txt");
    }

    @Test
    void testWriteToTree() throws Exception {
        tree1.add("blob : 01d82591292494afd1602d175e165f94992f6f5f : file2.txt");
        tree1.add("tree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b");
        tree1.add("blob : 81e0268c84067377a0a1fdfb5cc996c93f6dcf9f : file1.txt");
        tree1.writeToTree();
        String contents = "blob : 01d82591292494afd1602d175e165f94992f6f5f : file2.txt\ntree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b\nblob : 81e0268c84067377a0a1fdfb5cc996c93f6dcf9f : file1.txt";
        File treeFile = new File("./objects/" + Utils.getSHA(contents));
        assertTrue("treefile not made", treeFile.exists());
        assertEquals("treefile not made correctly", contents, Utils.getFileContents(treeFile));
    }

    @Test
    void testAddDirectory() throws Exception {
        Tree dirTree = new Tree();
        dirTree.addDirectory("test");

        Tree dirTree2 = new Tree();

        dirTree2.addDirectory("test2");

        dirTree.writeToTree();
        dirTree2.writeToTree();

        File treeFile = new File("./objects/" + dirTree.getSha());

        assertEquals("blob : 6e024e7c960af207eb62ee22492e5a2e2f43d11d : test1.txt\n"
                + //
                "blob : 2cc6e72f9bcfb65337b441d909835943ae5e944 : test.txt\n" + //
                "tree : 1c05d283db6a7c2f7a9712f713565ad20b59d2f4 : testD",
                Utils.getFileContents(treeFile));

        File treeFile2 = new File("./objects/" + dirTree2.getSha());

        assertEquals("blob : 6e024e7c960af207eb62ee22492e5a2e2f43d11d : test1.txt\n"
                + //
                "blob : 2cc6e72f9bcfb65337b441d909835943ae5e944 : test.txt\n" + //
                "tree : 1c05d283db6a7c2f7a9712f713565ad20b59d2f4 : testD",
                Utils.getFileContents(treeFile));
        assertEquals("blob : 6e024e7c960af207eb62ee22492e5a2e2f43d11d : test1.txt\n" + //
                "blob : 2cc6e72f9bcfb65337b441d909835943ae5e944 : test.txt\n" + // +
                "tree : 7e4c4e6ffbe32d7e4f445ac1d6ade008d1869acf : testD\n" + //
                "tree : da39a3ee5e6b4b0d3255bfef95601890afd80709 : testD2", Utils.getFileContents(treeFile2));

    }
}