import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class RSAApplication {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("RSA-Programm!");
        System.out.println("*******************************************************");
        System.out.println("Wähle aus:");
        System.out.println("1 = generate (Schlüssel erzeugen)");
        System.out.println("2 = encrypt (Text verschlüsseln)");
        System.out.println("3 = decrypt (Text entschlüsseln)");
        System.out.print("Deine Auswahl: ");
        int auswahl = sc.nextInt();

        switch (auswahl) {
            case 1 -> generate();
            case 2 -> encrypt();
            case 3 -> decrypt();
            case 4 -> System.exit(0);
            default -> System.out.println("Du bisch en Tubbel!");
        }
    }


    public void generate(){

    }

    public void encrypt(){

    }
    public void decrypt(){

        //Private Key einlesen:
        if (!Files.exists(Path.of("sk.txt"))) {
            throw new IllegalArgumentException("Kein Private Key gefunden!");
        }
        String publicKeyLine = Files.readString(Path.of("sk.txt")).trim();
        publicKeyLine = publicKeyLine.replace("(", "").replace(")", "");
        String[] keyParts = publicKeyLine.split(",");

        //(n,e) abspeichern
        BigInteger n = new BigInteger(keyParts[0].trim());
        BigInteger d = new BigInteger(keyParts[1].trim());

        //Prüfen ob chiffre.txt existiert und andernfalls wird ein chiffre.txt erstellt
        if (!Files.exists(Path.of(CHIFFRE_FILE))) {
            System.out.println("chiffre.txt existiert noch nicht. chiffre.txt wird erstellt...");
            Files.createFile(Path.of(CHIFFRE_FILE));
            System.out.println("Fülle chiffre.txt mit deinem Text, welchen du entschlüsseln möchtest!");
            System.exit(0);
        }

        //Chiffre.txt einlesen
        try (BufferedReader reader = new BufferedReader(new FileReader(CHIFFRE_FILE))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            //Einzelne "Buchstaben" - kommagetrennt, verarbeiten
            StringBuilder decrypt = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                String[] werte = lines.get(i).split(",");
                for (String w : werte) {
                    if (w.isBlank()) continue;
                    BigInteger c = new BigInteger(w.trim());
                    BigInteger m = modExpo(c, d, n);
                    decrypt.append((char) m.intValue());
                }
            }

            //In text-d.txt schreiben
            if (!Files.exists(Path.of("text-d.txt"))) {
                Files.createFile(Path.of("text-d.txt"));
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("text-d.txt", false))) {
                writer.write(decrypt.toString());
            }
        }
        System.out.println("Entschlüsselung wurde erfolgreich durchgeführt. Der entschlüsselte Text kann in text-d.txt gefunden werden.");
        RSAApplication.main(null);
    }
}
