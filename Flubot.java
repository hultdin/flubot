package main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.InflaterInputStream;
import java.util.zip.InflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

    public static String decode(String string) {
        try {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < string.length(); ++i) {
                buffer.append((char) (string.charAt(i) ^ (char) 24627));
            }
            return buffer.toString();
        } catch (Exception e) {
            return "";
        }
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

    public static String findAsset(String apk){
        String asset = null;
        try {
            ZipFile zip = new ZipFile(apk);
            if(zip != null) {
                for (Enumeration<? extends ZipEntry> enumeration = zip.entries(); asset == null && enumeration.hasMoreElements(); ) {
                    ZipEntry entry = enumeration.nextElement();
                    InputStream is = zip.getInputStream(entry);
                    byte[] magic = new byte[2];
                    is.read(magic);

                    // convert magic to zlib cmf and flg bytes
                    byte cmf = magic[0];
                    byte flg = magic[1];

                    if ((cmf == (byte) 0x78) && ((flg == (byte) 0x01) || (flg == (byte) 0x9C) || (flg == (byte) 0xDA))) {
                        asset = entry.getName();
                    }
                    is.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return asset;
    }

    public static void decryptAsset(String apk, String asset, String password, String output){
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
