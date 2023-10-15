import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    public static void main(String[] args) throws Exception {
        System.out.print(Utils.compressAndHash("hello"));
    }

    public static void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }

    }

    public static void makeDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public static void writeStringToFile(String filename, String contents) throws IOException {

        File file = new File(filename);

        FileWriter writer = new FileWriter(file);
        writer.write(contents);
        writer.close();

    }

    public static void deleteDirectory(String directoryName) {
        File directory = new File(directoryName);
        if (directory.exists()) {

            File[] allContents = directory.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectory(file.getPath());
                }
            }
            directory.delete();
        }
    }

    public static String getFileContents(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
        StringBuilder contents = new StringBuilder();
        while (reader.ready()) {
            contents.append((char) reader.read());
        }
        reader.close();
        return contents.toString();
    }

    public static String getSHA(String fileContents) {
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

    public static String compressAndHash(String contents) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        byte[] content = compressToBinary(contents);
        byte[] messageDigest = digest.digest(content);
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        return hashtext;
    }

    public static byte[] compressToBinary(String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOS = new GZIPOutputStream(baos)) {
            gzipOS.write(text.getBytes("UTF-8"));
        }
        return baos.toByteArray();
    }

    public static void writeToFile(byte[] text, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(text, 0, text.length);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String readFromCompressedFile(File file) throws Exception {
        FileInputStream filestream = new FileInputStream(file);
        GZIPInputStream inputstream = new GZIPInputStream(filestream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int length;

        while ((length = inputstream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, length);
        }
        outputStream.close();
        inputstream.close();
        filestream.close();
        return outputStream.toString("UTF-8");

    }
}