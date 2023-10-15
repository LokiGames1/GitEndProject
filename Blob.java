import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Blob {
    private Path toTextFile;
    private String fileName;
    private String fileContents;
    private String shaName;

    public Blob(String nameOfFile) throws IOException {
        toTextFile = Paths.get(nameOfFile);
        fileName = nameOfFile;
        fileContents = readFile(nameOfFile);
        shaName = doSha(fileContents);
    }

    public static String doSha(String input) {
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(String fileName) throws IOException {
        String output = "";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while (reader.ready()) {
            int letter = reader.read();
            output += (char) letter;
        }
        reader.close();
        return output;
    }

    public String getShaName() {
        return shaName;
    }

    public byte[] makeBite(String fileName) throws IOException {
        String inputString = readFile(fileName);
        byte[] byteArray = inputString.getBytes();

        return byteArray;
    }

    public void makeBlob() throws IOException {
        byte[] insideFile = makeBite(fileName);
        String folderPath = "Objects";
        Path toObjectsFolder = Paths.get(folderPath, doSha(fileName));
        Files.write(toObjectsFolder, insideFile);
    }

    public Path getToTextFile() {
        return toTextFile;
    }

    public Object getFileContents() {
        return fileContents;
    }
}