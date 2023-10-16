import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommitTest {

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        Utils.writeStringToFile("test1.txt", "this is test 1");
        Utils.writeStringToFile("test2.txt", "this is test 2");

        File testDir = new File("test");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        Utils.writeStringToFile("./test/test3.txt", "this is test 3");
        Utils.writeStringToFile("./test/test4.txt", "this is test 4");

        File sampleDir = new File("sample");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        Utils.writeStringToFile("./sample/sample1.txt", "this is sample 1");
        Utils.writeStringToFile("./sample/sample2.txt", "this is sample 2");

        File exampleDir = new File("example");
        if (!exampleDir.exists()) {
            exampleDir.mkdirs();
        }
        Utils.writeStringToFile("./example/example1.txt", "this is example 1");
        Utils.writeStringToFile("./example/example2.txt", "this is example 2");

        File nestedDir = new File("example/nested");
        if (!nestedDir.exists()) {
            nestedDir.mkdirs();
        }
        Utils.writeStringToFile("./example/nested/nested1.txt", "this is nested 1");
        Utils.writeStringToFile("./example/nested/nested2.txt", "this is nested 2");
    }

    @AfterAll
    static void deleteFiles() {
        File index = new File("index");
        File objects = new File("objects");
        File holdFile = new File("treeHoldingFile");
        index.delete();
        holdFile.delete();
        for (File subfile : objects.listFiles()) {
            subfile.delete();
        }
        objects.delete();
        Utils.deleteFile("HEAD");
        Utils.deleteFile("test1.txt");
        Utils.deleteFile("test2.txt");

        Utils.deleteDirectory("test");

        Utils.deleteFile("./sample/sample1.txt");
        Utils.deleteFile("./sample/sample2.txt");

        Utils.deleteDirectory("sample");

        Utils.deleteFile("./example/nested/nested1.txt");
        Utils.deleteFile("./example/nested/nested2.txt");

        Utils.deleteDirectory("example/nested");

        Utils.deleteFile("./example/example1.txt");
        Utils.deleteFile("./example/example2.txt");

        Utils.deleteDirectory("example");

    }

    @Test
    void testCommitAndWrite() throws Exception {
        Commit test = new Commit("wowza!!!!!!", "big shaq");
        test.commitAndWrite();

        // set up
        FileWriter commit = new FileWriter("objects/commit");
        BufferedWriter writeToCommit = new BufferedWriter(commit);

        writeToCommit.append("da39a3ee5e6b4b0d3255bfef95601890afd80709\n");
        writeToCommit.append("\n");
        writeToCommit.append("\n");
        writeToCommit.append("big shaq\n");
        writeToCommit.append(test.getDate() + "\n");
        writeToCommit.write("wowza!!!!!!");
        writeToCommit.close();

        String sha = Blob.generateSHA("objects/commit");

        File toTest = new File("objects/" + sha);

        assertTrue(toTest.exists());
        Utils.deleteFile("HEAD");
    }

    @Test
    void testCreateTree() throws Exception {
        Commit test = new Commit("random filler", "doens't matter cuh");
        test.createTree();

        File toTest = new File("objects/da39a3ee5e6b4b0d3255bfef95601890afd80709");
        assertTrue(toTest.exists());
    }

    // Include at least 2 added files into the commit
    // Verify the commit has correct Tree, Prev and Next SHA1s
    @Test
    void test1Commit() throws Exception {
        Git git = new Git();
        git.init();

        git.addDirectory("test");
        git.add("test1.txt");
        git.add("test2.txt");
        Commit test = new Commit("test", "drake");
        test.commitAndWrite();

        // tests if tree is correct
        assertEquals(
                "tree : a761014b6abf417a0adea354277e9cee985c04c5 : test\n"
                        + "blob : 5ff8d6a1ea48cbb8c3ea8bf7c0a901a4e7581569 : test1.txt\n" + //
                        "blob : a11b4a39fdb42e418c5dc60730ba24ccfa388ec4 : test2.txt",
                Utils.getFileContents(new File("./objects/" + test.treeSha)));

        // tests if commit is correct

        String read = Utils.readFromCompressedFile(new File("./objects/" + test.getSHA()));

        String[] lines = read.split("\\r?\\n");

        assertEquals(Commit.getTreeSha1(test.getSHA()), lines[0]);
        assertEquals("drake", lines[3]);
        assertEquals("test", lines[5]);
        Utils.deleteFile("HEAD");

    }

    // Include at least 2 added files into each commit
    // Include at least 1 folder in one of the commits
    // Verify the commits have correct Tree, Prev and Next SHA1s
    // Verify the Tree contents are correct

    @Test
    void test2Commit() throws Exception {
        Git git = new Git();
        git.init();
        git.add("test1.txt");
        git.add("test2.txt");
        Commit test = new Commit("test", "drake");
        test.commitAndWrite();
        git.addDirectory("test");
        Commit test2 = new Commit("i love peen", "drakey wakey");
        test2.commitAndWrite();

        String commit1 = Utils.readFromCompressedFile(new File("./objects/" + test.getSHA()));

        String[] commit1ar = commit1.split("\\r?\\n");

        String commit2 = Utils.readFromCompressedFile(new File("./objects/" + test2.getSHA()));

        String[] commit2ar = commit2.split("\\r?\\n");

        // tests commit 1
        assertEquals(Commit.getTreeSha1(test.getSHA()), commit1ar[0]);
        assertEquals(test2.getSHA(), commit1ar[2]);
        assertEquals("drake", commit1ar[3]);
        assertEquals("test", commit1ar[5]);

        // tests commit 2
        assertEquals(Commit.getTreeSha1(test2.getSHA()), commit2ar[0]);
        assertEquals(test.getSHA(), commit2ar[1]);
        assertEquals("drakey wakey", commit2ar[3]);
        assertEquals("i love peen", commit2ar[5]);

        // Test trees

        assertEquals("blob : 5ff8d6a1ea48cbb8c3ea8bf7c0a901a4e7581569 : test1.txt\n" + //
                "blob : a11b4a39fdb42e418c5dc60730ba24ccfa388ec4 : test2.txt",
                Utils.getFileContents(new File("./objects/" + Commit.getTreeSha1(test.getSHA()))));
        assertEquals("tree : a761014b6abf417a0adea354277e9cee985c04c5 : test\n" + //
                "tree : " + Commit.getTreeSha1(test.getSHA()),
                Utils.getFileContents(new File("./objects/" + Commit.getTreeSha1(test2.getSHA()))));
        Utils.deleteFile("HEAD");

    }

    // Each commit must contain at least 2 new files, all of which have unique file
    // data
    // 2 Commits must contain at least one new folder
    // Test tree contents, commit contents for prev and next and trees

    @Test
    void test3Commit() throws Exception {
        Git git = new Git();
        git.init();
        git.add("test1.txt");
        git.add("test2.txt");
        Commit test1 = new Commit("test1", "kanye");
        test1.commitAndWrite();
        git.addDirectory("example");
        Commit test2 = new Commit("test2", "tyler");
        test2.commitAndWrite();
        git.addDirectory("sample");
        Commit test3 = new Commit("test3", "kendrick");

        test3.commitAndWrite();
        git.addDirectory("test");
        Commit test4 = new Commit("free him on god", "r kelly");

        test4.commitAndWrite();

        String commit1 = Utils.readFromCompressedFile(new File("./objects/" + test1.getSHA()));

        String[] commit1ar = commit1.split("\\r?\\n");

        String commit2 = Utils.readFromCompressedFile(new File("./objects/" + test2.getSHA()));

        String[] commit2ar = commit2.split("\\r?\\n");

        String commit3 = Utils.readFromCompressedFile(new File("./objects/" + test3.getSHA()));

        String[] commit3ar = commit3.split("\\r?\\n");

        String commit4 = Utils.readFromCompressedFile(new File("./objects/" + test4.getSHA()));

        String[] commit4ar = commit4.split("\\r?\\n");

        // tests commit 1
        assertEquals(Commit.getTreeSha1(test1.getSHA()), commit1ar[0]);
        assertEquals(test2.getSHA(), commit1ar[2]);
        assertEquals("kanye", commit1ar[3]);
        assertEquals("test1", commit1ar[5]);

        // tests commit 2
        assertEquals(Commit.getTreeSha1(test2.getSHA()), commit2ar[0]);
        assertEquals(test1.getSHA(), commit2ar[1]);
        assertEquals(test3.getSHA(), commit2ar[2]);
        assertEquals("tyler", commit2ar[3]);
        assertEquals("test2", commit2ar[5]);

        // tests commit 3
        assertEquals(Commit.getTreeSha1(test3.getSHA()), commit3ar[0]);
        assertEquals(test2.getSHA(), commit3ar[1]);
        assertEquals(test4.getSHA(), commit3ar[2]);
        assertEquals("kendrick", commit3ar[3]);
        assertEquals("test3", commit3ar[5]);

        // tests commit 4
        assertEquals(Commit.getTreeSha1(test4.getSHA()), commit4ar[0]);
        assertEquals(test3.getSHA(), commit4ar[1]);
        assertEquals("r kelly", commit4ar[3]);
        assertEquals("free him on god", commit4ar[5]);

        assertEquals("blob : 5ff8d6a1ea48cbb8c3ea8bf7c0a901a4e7581569 : test1.txt\n" + //
                "blob : a11b4a39fdb42e418c5dc60730ba24ccfa388ec4 : test2.txt",
                Utils.getFileContents(new File("./objects/" + Commit.getTreeSha1(test1.getSHA()))));

        assertEquals("tree : 8f924f1579f31c9b68c6dc9c8b17ec74fe9e5b17 : example\n" + //
                "tree : " + Commit.getTreeSha1(test1.getSHA()),
                Utils.getFileContents(new File("./objects/" + Commit.getTreeSha1(test2.getSHA()))));

        assertEquals("blob : 911562c85301c3a240bb701439e720c40ea7e3b3 : example1.txt\n" +
                "blob : 310a7caba9f9fb966052b7aec063240aff0ac184 : example2.txt\n" +
                "tree : cb3c5147e9cf8922b97669f795c18c9a8ade9b7f : nested",
                Utils.getFileContents(
                        new File("./objects/" + "8f924f1579f31c9b68c6dc9c8b17ec74fe9e5b17")));

        assertEquals("tree : 6867084de26ff394bb24138afb8d25c20e0aa77c : sample\n" + //
                "tree : " + Commit.getTreeSha1(test2.getSHA()),
                Utils.getFileContents(new File("./objects/" + Commit.getTreeSha1(test3.getSHA()))));

        assertEquals("tree : a761014b6abf417a0adea354277e9cee985c04c5 : test\n" + //
                "tree : " + Commit.getTreeSha1(test3.getSHA()),
                Utils.getFileContents(new File("./objects/" + Commit.getTreeSha1(test4.getSHA()))));

        Utils.deleteFile("HEAD");

    }

}