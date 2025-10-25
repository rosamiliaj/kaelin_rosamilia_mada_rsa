import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

// Im Skript haben wir Kommentare hinterlegt, welche die Methoden erklären, was wo passiert
public class RSAApplication {
    private static String SK_FILE = "sk.txt";
    private static String PK_FILE = "pk.txt";
    private static String TEXT_FILE = "text.txt";
    private static String CHIFFRE_FILE = "chiffre.txt";

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("RSA-Programm!");
            System.out.println("*******************************************************");
            System.out.println("Wähle aus:");
            System.out.println("1 = generate (Schlüssel erzeugen)");
            System.out.println("2 = encrypt (Text verschlüsseln)");
            System.out.println("3 = decrypt (Text entschlüsseln)");
            System.out.println("4 = program exit");
            System.out.print("Deine Auswahl: ");
            int auswahl = sc.nextInt();

            switch (auswahl) {
                case 1 -> generate();
                case 2 -> encrypt();
                case 3 -> decrypt();
                case 4 -> System.exit(0);
                default -> System.out.println("Ungültige Auswahl");
            }
        }
    }

    public static void generate() throws IOException {
        BigInteger prime1 = BigInteger.probablePrime(4096, new Random());
        BigInteger prime2 = BigInteger.probablePrime(4096, new Random());
        //System.out.println("Prime1 definiert: " + prime1);
        //System.out.println("Prime2 definiert: " + prime2);
        if (!prime1.equals(prime2)) {
            BigInteger n = prime1.multiply(prime2);
            //System.out.println("n = " + n);
        }
        //phi(n)
        BigInteger phiN = (prime1.subtract(BigInteger.ONE)).multiply(prime2.subtract(BigInteger.ONE)); // (prime1 -1) * (prime2 -1)

        //e muss Teilerfremd zu phi(n) sein
        BigInteger e = BigInteger.valueOf(65537); // 65537 ist ein Standard-Wert

        // (e×d)modphi(n)=1 auf d aufgelöst: d=e−1(modphi(n)) -> e.modInverse(phiN)
        BigInteger d = e.modInverse(phiN);

        //System.out.println("d = " + d);

        //Prüfen, ob das Schlüsselpaar (n,e) und (n,d) gültig sind
        if (!e.multiply(d).mod(phiN).equals(BigInteger.ONE)) {
            throw new IllegalArgumentException();
        }
        System.out.println("(e * d) mod phi(n) = " + e.multiply(d).mod(phiN) + ". Es wurde ein gültiges Schlüsselpaar generiert!");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SK_FILE, false))) {
            writer.write("(" + prime1.multiply(prime2));
            writer.write("," + d + ")");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PK_FILE, false))) {
            writer.write("(" + prime1.multiply(prime2));
            writer.write("," + e + ")");
        }
    }

    public static void encrypt() throws IOException {
        if (Files.exists(Path.of(CHIFFRE_FILE))) {
            Files.delete(Path.of(CHIFFRE_FILE));
        }
        Files.createFile(Path.of(CHIFFRE_FILE));

        //Public-Key einlesen:
        if (!Files.exists(Path.of("pk.txt"))) {
            throw new IllegalArgumentException("Kein Public Key gefunden!");
        }
        String publicKeyLine = Files.readString(Path.of("pk.txt")).trim();
        publicKeyLine = publicKeyLine.replace("(", "").replace(")", "");
        String[] keyParts = publicKeyLine.split(",");

        //(n,e) abspeichern
        BigInteger n = new BigInteger(keyParts[0].trim());
        BigInteger e = new BigInteger(keyParts[1].trim());

        //Prüfen ob text.txt existiert und andernfalls wird ein text.txt erstellt
        if (!Files.exists(Path.of("text.txt"))) {
            System.out.println("text.txt existiert noch nicht. text.txt wird erstellt...");
            Files.createFile(Path.of("text.txt"));
            System.out.println("Fülle text.txt mit deinem Text, welchen du verschlüsseln möchtest!");
            System.exit(0);
        }

        //Text lesen und verschlüsseln
        try (BufferedReader reader = new BufferedReader(new FileReader(TEXT_FILE))) {
            List<String> lines = new ArrayList<>();
            List<BigInteger> chars = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            for (int i = 0; i < lines.size(); i++) {
                for (char c : lines.get(i).toCharArray()) {
                    chars.add(modExpo(BigInteger.valueOf((int) c), e, n));
                }
            }
            for (int i = 0; i < chars.size(); i++) {

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHIFFRE_FILE, true))) {
                    if (i < chars.size() - 1) {
                        writer.write(chars.get(i) + ",");
                    } else {
                        writer.write(chars.get(i).toString());
                    }
                }
            }
        }
        System.out.println("Verschlüsselung wurde erfolgreich ausgeführt. Der verschlüsselte Text kann im chiffre.txt gefunden werden.");
    }

    // Schnelle Exponentiation
    private static BigInteger modExpo(BigInteger base, BigInteger e, BigInteger n) {
        BigInteger result = BigInteger.ONE; //1

        while (e.compareTo(BigInteger.ZERO) > 0) {
            if (e.testBit(0)) result = result.multiply(base).mod(n);
            base = base.multiply(base).mod(n);
            e = e.shiftRight(1);
        }
        return result;
    }

    public static void decrypt() throws IOException {

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
    }
}
