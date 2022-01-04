public class Main {
    public static void main(String[] args) {
        String key = Flubot.getInstance().decode("恣恦恆恛恔恁恦恴恴恺恦恛怊恹恻恴恦恺恴恺恦恻恴恜恘恕恖恄恁恜恕恚恙恦");
        
        System.out.println("key: \"" + key + "\"");

        for(String domain: Flubot.getInstance().domains(1945)){
            System.out.println(domain);
        }
        
        Flubot.getInstance().decryptAsset("~/malware/repository/flubot/DHL14.apk", "assets/gjgIfyy/qdf7ywyt8" + 1 + ".Fej", key, "~/malware/repository/flubot/output.zip");
    }
}
