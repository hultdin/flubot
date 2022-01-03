import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.zip.*;

public class Flubot {
    private static final Flubot INSTANCE = new Flubot();

    private Flubot(){
        //
    }

    public static final Flubot getInstance(){
        return INSTANCE;
    }

    public static ArrayList<String> domains(long seed){
        int year = Calendar.getInstance().get(1);
        int month = Calendar.getInstance().get(2);
        return domains(seed, year, month);
    }

    public static ArrayList<String> domains(long seed, int year, int month){
        ArrayList<String> domains = new ArrayList<>();

        // calculate offset for the given year and month (note: month is zero based)
        long offset = (long) ((year ^ month) ^ 0);
        offset = offset * 2;
        offset = offset * (((long) year) ^ offset);
        offset = offset * (((long) month) ^ offset);
        offset = offset * (0L ^ offset);

        Random r = new Random(seed + offset);

        for (int i = 0; i < 5000; i++) {
            StringBuilder domain = new StringBuilder();
            for (int j = 0; j < 15; j++) {
                domain.append((char) (Character.codePointAt("a", 0) + r.nextInt(25)));
            }
            if (i % 3 == 0) {
                domain.append(".ru");
            } else {
                if (i % 2 == 0) {
                    domain.append(".su");
                } else {
                    domain.append(".cn");
                }
            }
            domains.add(domain.toString());
        }
        return(domains);
    }

    public static String decode(short[] bytes, int start, int stop, int key){
        char[] chars = new char[stop - start];
        for (int i = 0; i < stop - start; ++i) {
            chars[i] = (char) (bytes[start + i] ^ key);
        }
        return new String(chars);
    }

    private static void decrypt(InputStream is, String password, OutputStream os) throws IOException {
        char[] characters = password.toCharArray();
        int[] key = {characters[0] | (characters[1] << 16), (characters[3] << 16) | characters[2], (characters[5] << 16) | characters[4], (characters[7] << 16) | characters[6]};
        int[] state = {key[1], key[2], key[3]};

        for(int i = 0, j = key[0]; i < 26; ++i){
            state[i % 3] = (((state[i % 3] >>> 8) | (state[i % 3] << 24)) + j) ^ i;
            j = ((j << 3) | (j >>> 29)) ^ state[i % 3];
        }

        byte[] buffer = new byte[8192];
        for (int i = 0; true; ) {
            int read = is.read(buffer);
            if (0 <= read) {
                int j = 0;
                int k = i;
                while (k < i + read) {
                    buffer[j] = (byte) ((byte) (((byte) (new int[]{(characters[9] << 16) | characters[8], (characters[11] << 16) | characters[10]}[(k % 8) / 4] >> ((k % 4) << 3))) ^ buffer[j]));
                    k++;
                    j++;
                }
                os.write(buffer, 0, read);
                i = k;
            } else {
                return;
            }
        }
    }

    public static void decrypt(String apk, String asset, String password, String output){
        try {
            ZipFile zip = new ZipFile(apk);
            InputStream is = zip.getInputStream(new ZipEntry(asset));
            ZipOutputStream os = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(new File(output))));
            os.putNextEntry(new ZipEntry("classes.dex"));
            decrypt(new InflaterInputStream(is), password, new InflaterOutputStream(os));
            os.closeEntry();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
