import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class ReedMuller {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Paprašome vartotojo pasirinkti ką jis nori daryti
        System.out.println("\n");
        System.out.println("Pasirinkite scenarijų:" +
                "\n [1] užrašyti vektorių" +
                "\n [2] užrašyti tekstą" +
                "\n [3] nurodyti paveiksliuką");
        int userInput = scanner.nextInt();

        switch (userInput) {
            // Pirmasis scenarijus, kai vartotojas užrašo vektorių
            case 1:
                // Vartotojas įveda kodo parametrą m Rydo-Miulerio kodui
                System.out.print("\nRydo-Miulerio kodui (1, m)\nĮveskite kodo parametrą m: ");
                int m = scanner.nextInt();
                int columns = (int) Math.pow(2, m);         // Stulpeliai
                int rows = m + 1;                           // Eilutės

                // Yra sudaroma generuojanti matrica pagal vartotojo parametrą m
                int[][] generatorMatrix = generateReedMullerMatrix(m);
                System.out.println("Rydo-Miulerio generuojanti matrica:");
                printMatrix(generatorMatrix);

                // Paprašoma įvesti informacijos vektorių m+1 ilgio nes tokia yra matematinė formulė
                System.out.print("Įveskite informacijos vektorių (" + rows + " ilgio)\n");
                int[] inputVector = new int[rows];

                // Vektoriaus skaitymas yra daromas kas vieną integerį, kad vartotojui būtų aiškiau
                // Bet vartotojas gali ir iš karto vesti pilną savo vektorių atskirdamas tarpais, programa automatiškai įrašys į tinkamas indekso vietas
                for (int i = 0; i < rows; i++) {
                    int input;
                    while (true) {
                        System.out.print("Įveskite " + (i + 1) + "-ąjį elementą (0 arba 1): ");
                        if (scanner.hasNextInt()) {
                            input = scanner.nextInt();
                            if (input == 0 || input == 1) {
                                break;
                            } else {
                                // Programa praneša jeigu bloga įvestis
                                System.out.println("Klaida: Vektoriaus elementai turi būti tik 0 arba 1.");
                            }
                        }
                    }
                    inputVector[i] = input;
                }
                System.out.println("\nĮvestas vektorius: " + Arrays.toString(inputVector));

                // Jeigu vartotojas įveda per daug integerių, programa tiesiog juos ignoruoja
                scanner.nextLine();
                System.out.print("Įveskite klaidos tikimybę (0 <= p_e <= 1): "); // Įvedame klaidos tikimybę
                double pe = scanner.nextDouble();
                if (pe < 0 || pe > 1) {
                    System.out.println("Klaidos tikimybė turi būti tarp 0 ir 1.");
                    scanner.close();
                    return;
                }

                // Užkoduojame vektorių
                int[] encodedVector = encodeVector(inputVector, generatorMatrix);
                System.out.println("Užkoduotas vektorius:");
                System.out.println(Arrays.toString(encodedVector));
                int[] testVector = Arrays.copyOf(encodedVector, encodedVector.length);

                // Siunčiame vektorių per nepatikimą kanalą
                transmitVector(encodedVector, pe);
                int[] receivedVector = Arrays.copyOf(encodedVector, encodedVector.length);
                System.out.println("Iš kanalo išėjęs vektorius:");
                System.out.println(Arrays.toString(receivedVector));

                // Surandame klaidų kiekį ir klaidų vietas lygindami užkoduotą vektorių ir iš kanalo išėjusį vektorių
                detectErrors(testVector, receivedVector);

                // Leidžiame vartotojui pasirinkti, ar jis nori redaguoti iš kanalo išėjusį vektorių
                System.out.print("Ar norite redaguoti iš kanalo išėjusį vektorių? (taip/ne): ");
                String editChoice = scanner.next();
                if (editChoice.equalsIgnoreCase("taip")) {
                    System.out.println("Įveskite naują vektorių (" + columns + " ilgio):");
                    for (int i = 0; i < columns; i++) {
                        int inputBit;
                        while (true) {
                            // Vektoriaus įvedimas yra daromas kas vieną integerį, kad vartotojui būtų aiškiau
                            // Bet vartotojas gali ir iš karto vesti pilną savo vektorių atskirdamas tarpais, programa automatiškai įrašys į tinkamas indekso vietas
                            System.out.print("Įveskite " + (i + 1) + "-ąjį elementą (0 arba 1): ");
                            if (scanner.hasNextInt()) {
                                inputBit = scanner.nextInt();
                                if (inputBit == 0 || inputBit == 1) {
                                    break;
                                } else {
                                    System.out.println("Klaida: Įvestas skaičius turi būti 0 arba 1.");
                                }
                            } else {
                                System.out.println("Klaida: Įvestis turi būti sveikasis skaičius (0 arba 1).");
                                scanner.next();
                            }
                        }
                        receivedVector[i] = inputBit;
                    }
                    // Išvedame naują vartotojo paredaguotą vektorių
                    System.out.println("Naujas vektorius po redagavimo:");
                    System.out.println(Arrays.toString(receivedVector));
                    // Dar kartą patikriname klaidų kiekį ir klaidų vietas
                    detectErrors(testVector, receivedVector);
                }

                // Dekoduojame vektorių
                int[] decodedVector = decodeVector(receivedVector, m);
                System.out.println("Dekoduotas vektorius:");
                System.out.println(Arrays.toString(decodedVector));

                scanner.close();
                return;



            // Antras scenarijus, kai vartotojas užrašo tekstą
            case 2:
                // Vartotojas įveda kodo parametrą m Rydo-Miulerio kodui
                System.out.print("\nRydo-Miulerio kodui (1, m)\nĮveskite kodo parametrą m: ");
                m = scanner.nextInt();

                generatorMatrix = generateReedMullerMatrix(m);
                System.out.println("Rydo-Miulerio generuojanti matrica:");
                printMatrix(generatorMatrix);

                System.out.println("Įveskite tekstą (galite įvesti kelias eilutes). Kad pabaigti rašyti naujoje eilutėje parašykite 'exit'");

                // Nuskaitome vartotojo įvestį
                StringBuilder inputText = new StringBuilder();
                String line;
                while (!(line = scanner.nextLine()).equalsIgnoreCase("exit")) {
                    inputText.append(line).append("\n");
                }

                inputText.deleteCharAt(0);
                inputText.setLength(inputText.length() - 1);

                // Paverčiame naudotojo įvestą tekstą į binary ASCII
                String binaryText = textToBinary(inputText.toString());

                // Suskaidome jau binary tekstą į vektorius ilgio 2^m
                int[][] vectors = splitIntoVectors(binaryText, m);
                System.out.println("Suskaidyti vektoriai:");
                printMatrix(vectors);

                // Iteruojame per suskaidytus vektorius ir užkoduojame juos po vieną
                int[][] encodedVectors = new int[vectors.length][];
                System.out.println("Užkoduotas vektorius: ");
                for (int i = 0; i < vectors.length; i++) {
                    encodedVectors[i] = encodeVector(vectors[i], generatorMatrix);  // Užkoduojame vektorių
                    System.out.println(Arrays.toString(encodedVectors[i]));         // Spausdiname užkoduotą vektorių
                }

                // Įvedame klaidos tikimybę
                System.out.print("Įveskite klaidos tikimybę (0 <= p_e <= 1): ");
                pe = scanner.nextDouble();
                if (pe < 0 || pe > 1) {
                    System.out.println("Klaidos tikimybė turi būti tarp 0 ir 1.");
                    scanner.close();
                    return;
                }

                // Iteruojame per neužkoduotus vektorius ir perduodame juos į kanalą po vieną
                System.out.println("Iš kanalo išėjęs neužkoduotas vektorius:");
                for (int i = 0; i < vectors.length; i++) {
                    transmitVector(vectors[i], pe);        // Perduodame per kanalą
                    System.out.println(Arrays.toString(vectors[i]));    // Spausdiname gautą vektorių
                }

                int[][] receivedVectors2 = Arrays.copyOf(vectors, vectors.length);

                // Iteruojame per užkoduotus vektorius ir perduodame juos į kanalą po vieną
                System.out.println("Iš kanalo išėjęs užkoduotas vektorius:");
                for (int i = 0; i < vectors.length; i++) {
                    transmitVector(encodedVectors[i], pe); // Perduodame per kanalą
                    System.out.println(Arrays.toString(encodedVectors[i]));    // Spausdiname gautą vektorių
                }

                int[][] receivedVectors = Arrays.copyOf(encodedVectors, encodedVectors.length);

                // Iteruojame per iš kanalo išėjusius vektorius ir dekoduojame juos po vieną
                int[][] decodedVectors = new int[vectors.length][];
                System.out.println("Dekoduotas vektorius:");
                for (int i = 0; i < vectors.length; i++) {
                    decodedVectors[i] = decodeVector(receivedVectors[i], m);    // Dekoduojame vektorių
                    System.out.println(Arrays.toString(decodedVectors[i]));     // Spausdiname dekotuotą vektorių
                }

                // Išspausdiname į ekraną palyginimui pradinį tekstą, siustą neužkoduotą tekstą ir atkurtą tekstą
                System.out.println("\nPradinis tekstas:\n" + inputText);
                String decodedText2 = decodedVectorsToString(receivedVectors2); // Turime paversti dekoduotus vektorius į vieną stringą
                System.out.println("\nAtkurtas tekstas (siųstas neužkoduotas pro kanalą):\n" + decodedText2);
                String decodedText = decodedVectorsToString(decodedVectors);    // Turime paversti dekoduotus vektorius į vieną stringą
                System.out.println("\nAtkurtas tekstas: (siųstas užkoduotas pro kanalą)\n" + decodedText);

                scanner.close();
                return;



            // Trečias scenarijus, kai vartotojas nurodo paveiksliuką
            case 3:
                // Vartotojas įveda kodo parametrą m Rydo-Miulerio kodui
                System.out.print("\nRydo-Miulerio kodui (1, m)\nĮveskite kodo parametrą m: ");
                m = scanner.nextInt();

                generatorMatrix = generateReedMullerMatrix(m);
                System.out.println("Rydo-Miulerio generuojanti matrica:");
                printMatrix(generatorMatrix);

                System.out.println("\nPrašome pasirinkti BMP paveiksliuką");
                JFrame frame = new JFrame("Originalus paveikslėlis");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);

                // Paprašome vartotojo pasirinkti failą
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("BMP Paveikslėliai", "bmp"));

                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    // Gauname pasirinktą failą
                    File selectedFile = fileChooser.getSelectedFile();

                    try {
                        // Nuskaitome BMP paveikslėlį į BufferedImage objektą
                        BufferedImage image = ImageIO.read(selectedFile);

                        // Konvertuojame paveikslėlį į binary formatą
                        String binaryImage = convertToBinaryWithRGB(image);

                        // Parodome originalų paveikslėlį
                        ImageIcon imageIcon = new ImageIcon(image);
                        JLabel label = new JLabel(imageIcon);
                        frame.getContentPane().add(label, BorderLayout.CENTER);
                        frame.setVisible(true);

                        // Suskaidome vektorius į vektorius ilgio 2^m
                        vectors = splitIntoVectors(binaryImage, m);
                        printMatrix(vectors);
                        System.out.println("Suskaidyti vektoriai");

                        // Pranešame vartotojui, kad tuoj bus užkoduotas vektorius,
                        // kad viskas nevyktų per greitai ir vartotojas spėtų pamatyti suskaidytą vektorių
                        System.out.println("\nSuskaidytas vektorius bus užkoduotas po 3");
                        Thread.sleep(1000);
                        System.out.println("\nSuskaidytas vektorius bus užkoduotas po 2");
                        Thread.sleep(1000);
                        System.out.println("\nSuskaidytas vektorius bus užkoduotas po 1");
                        Thread.sleep(1000);

                        // Iteruojame per suskaidytus vektorius ir užkoduojame juos po vieną
                        for (int i = 0; i < vectors.length; i++) {
                            vectors[i] = encodeVector(vectors[i], generatorMatrix);  // Užkoduojame vektorių
                            System.out.println(Arrays.toString(vectors[i]));         // Spausdiname užkoduotą vektorių
                        }
                        System.out.println("Užkoduotas vektorius");
                        encodedVectors = Arrays.copyOf(vectors, vectors.length);

                        // Paprašome vartotojo įvesti klaidos tikimybę
                        System.out.print("\nĮveskite klaidos tikimybę (0 <= p_e <= 1): ");
                        pe = scanner.nextDouble();
                        if (pe < 0 || pe > 1) {
                            System.out.println("Klaidos tikimybė turi būti tarp 0 ir 1.");
                            scanner.close();
                            return;
                        }

                        // Iteruojame per užkoduotus vektorius ir perduodame juos po vieną per kanalą
                        for (int i = 0; i < vectors.length; i++) {
                            transmitVector(encodedVectors[i], pe);     // Perduodame per kanalą
                            System.out.println(Arrays.toString(encodedVectors[i]));        // Spausdiname gautą vektorių
                        }
                        System.out.println("Iš kanalo išėjęs vektorius");
                        receivedVectors = Arrays.copyOf(encodedVectors, encodedVectors.length);

                        // Iteruojame per iš kanalo išėjusius vektorius ir dekoduojame juos po vieną
                        for (int i = 0; i < vectors.length; i++) {
                            receivedVectors[i] = decodeVector(receivedVectors[i], m);        // Dekoduojame vektorių
                            System.out.println(Arrays.toString(receivedVectors[i]));         // Spausdiname dekotuotą vektorių
                        }
                        System.out.println("Dekoduotas vektorius");
                        decodedVectors = Arrays.copyOf(receivedVectors, receivedVectors.length);

                        // Sukuriame SB iš dekoduotų vektorių
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < decodedVectors.length; i++) {
                            for (int j = 0; j < decodedVectors[i].length; j++) {
                                // Konvertuojame kiekvieną skaičių į SB
                                stringBuilder.append(Integer.toBinaryString(decodedVectors[i][j]));
                            }
                        }

                        // Gauname paveikslėlio ilgį ir aukštį
                        int width = image.getWidth();
                        int height = image.getHeight();

                        // Sukuriame dekoduotą paveikslėlį
                        BufferedImage decodedImage = createImageFromBinaryRGB(stringBuilder.toString(), width, height);
                        displayImage(decodedImage);
                        System.out.println("\nRodomas dekoduotas paveikslėlis");
                        frame.setAlwaysOnTop(true);
                        frame.toFront();
                        frame.repaint();
                        frame.requestFocus();
                        frame.requestFocusInWindow();
                        frame.setAlwaysOnTop(false);

                    } catch (IOException e) {
                        // Jei nepavyksta nuskaityti paveikslėlio, parodome klaidos pranešimą
                        JOptionPane.showMessageDialog(frame, "Nepavyko įkelti paveikslėlio: " + e.getMessage(), "Klaida", JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("Vartotojas atsisakė pasirinkti failą.");
                    return;
                }
        return;
        } // Baigiasi switch
    }

    // Funkcija, kuri konvertuoja binary string atgal į RGB formą
    public static BufferedImage createImageFromBinaryRGB(String binaryImage, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Išimame 8-bitų binary kodą kiekvienam RGB kanalui
                int r = Integer.parseInt(binaryImage.substring(index, index + 8), 2);
                index += 8;
                int g = Integer.parseInt(binaryImage.substring(index, index + 8), 2);
                index += 8;
                int b = Integer.parseInt(binaryImage.substring(index, index + 8), 2);
                index += 8;

                // Sukuriame spalvą ir nustatome RGB reikšmes
                Color color = new Color(r, g, b);
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    // Funkcija, kuri atvaizduoja dekoduotą paveikslėlį
    public static void displayImage(BufferedImage image) {
        // Sukuriame JFrame ir pridedame JLabel su paveikslėliu
        JFrame frame = new JFrame("Atkurtas paveikslėlis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon icon = new ImageIcon(image);
        JLabel label = new JLabel(icon);
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setLocation(1000, 0);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.repaint();
        frame.requestFocusInWindow();
        frame.setAlwaysOnTop(false);
    }

    // Funkcija, kuri konvertuoja RGB reikšmes į 8-bitų binary formatą
    public static String convertToBinaryWithRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        StringBuilder binaryImageSB = new StringBuilder();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                // Konvertuojame kiekvieną komponentą į 8-bitų binary formatą
                binaryImageSB.append(String.format("%8s", Integer.toBinaryString(r)).replace(' ', '0')); // Raudonas kanalas
                binaryImageSB.append(String.format("%8s", Integer.toBinaryString(g)).replace(' ', '0')); // Žalias kanalas
                binaryImageSB.append(String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0')); // Mėlynas kanalas
            }
        }

        return binaryImageSB.toString();
    }

    // Funkcija, kuri dekoduotą binary tekstą paverčia atgal į normalų stringą
    private static String decodedVectorsToString(int[][] decodedVectors) {
        StringBuilder binaryStringBuilder = new StringBuilder();

        // Iteruojame per kiekvieną dekoduotą vektorių
        for (int i = 0; i < decodedVectors.length; i++) {
            for (int j = 0; j < decodedVectors[i].length; j++) {
                binaryStringBuilder.append(decodedVectors[i][j]);  // Sukaupiame visus bitus
            }
        }

        // Konvertuojame bitų eilutę į simbolius (8 bitų grupėmis)
        StringBuilder result = new StringBuilder();
        String binaryString = binaryStringBuilder.toString();

        for (int i = 0; i < binaryString.length(); i += 8) {
            // Patikriname, ar liko pakankamai bitų (8 bitai)
            if (i + 8 <= binaryString.length()) {
                String byteString = binaryString.substring(i, i + 8);  // Paimame 8 bitų grupę
                int charCode = Integer.parseInt(byteString, 2);        // Konvertuojame į skaičių
                result.append((char) charCode);                        // Pridedame simbolį į rezultatą
            }
        }
        return result.toString();
    }

    // Funkcija, kuri paverčia simbolį į 8-bitų binary formą (ASCII kodas)
    public static String charToBinary(char c) {
        return String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');   // Padaromas 8 bitų ilgio binary stringas
    }

    // Funkcija, kuri paverčia tekstą į binary stringą
    public static String textToBinary(String text) {
        StringBuilder binaryText = new StringBuilder();
        for (char c : text.toCharArray()) {
            binaryText.append(charToBinary(c));
        }
        return binaryText.toString();
    }

    // Funkcija, kuri paverčia binary stringą į binary masyvą
    public static int[] binaryStringToIntArray(String binaryText) {
        int[] intArray = new int[binaryText.length()];
        for (int i = 0; i < binaryText.length(); i++) {
            intArray[i] = binaryText.charAt(i) - '0'; // Paverčia '0' arba '1' į 0 arba 1
        }
        return intArray;
    }

    // Funkcija, kuri suskaido binary tekstą į vektorius ilgio 2^m ir jeigu reikia užpildo nuliais
    public static int[][] splitIntoVectors(String binaryText, int m) {
        int vectorLength = m+1;
        int numVectors = (int) Math.ceil((double) binaryText.length() / vectorLength);  // Apskaičiuojame, kiek vektorių reikės

        int[][] vectors = new int[numVectors][vectorLength];

        // Suskaidome binary tekstą į vektorius
        for (int i = 0; i < numVectors; i++) {
            int start = i * vectorLength;
            int end = Math.min((i + 1) * vectorLength, binaryText.length());
            String vector = binaryText.substring(start, end);

            // Paverčiame vektorių į int masyvą ir jei reikia užpildome nuliais
            int[] intVector = binaryStringToIntArray(vector);

            // Jeigu vektorius trumpesnis tai užpildome trūkstamus bitus nuliais
            if (intVector.length < vectorLength) {
                int[] filledVector = new int[vectorLength];
                System.arraycopy(intVector, 0, filledVector, 0, intVector.length);
                vectors[i] = filledVector;
            } else {
                vectors[i] = intVector;
            }
        }
        return vectors;
    }

    // Funkcija, kuri lygina užkoduotą vektorių su iš kanalo gautu vektoriu
    private static void detectErrors(int[] encodedVector, int[] receivedVector) {
        int errorCount = 0;
        StringBuilder errorPositions = new StringBuilder();

        // Lyginame užkoduotą vektorių su iš kanalo gautu vektoriu
        for (int i = 0; i < encodedVector.length; i++) {
            if (encodedVector[i] != receivedVector[i]) {
                errorCount++;
                errorPositions.append(i).append(" ");
            }
        }

        // Parodome klaidų kiekį ir klaidų pozicijas
        if (errorCount == 0) {
            System.out.println("Klaidų nėra.");
        } else {
            System.out.println("Klaidų skaičius: " + errorCount);
            System.out.println("Klaidos pozicijos: " + errorPositions.toString().trim());
        }
    }

    // Funkcija, kuri sugeneruoja Rydo-Miulerio(1, m) generuojančią matricą
    private static int[][] generateReedMullerMatrix(int m) {
        int columns = (int) Math.pow(2, m);
        int rows = m + 1;

        int[][] generatorMatrix = new int[rows][columns];

        // Pirmoji eilutė pilna vienetų
        Arrays.fill(generatorMatrix[0], 1);

        // Kitos eilutės atitinka binary derinius
        for (int i = 0; i < m; i++) {
            int period = (int) Math.pow(2, m - i - 1);
            for (int j = 0; j < columns; j++) {
                generatorMatrix[i + 1][j] = (j / period) % 2;
            }
        }
        return generatorMatrix;
    }

    // Funkcija, kuri užkoduotą vektorių naudodama Rydo-Miulerio(1, m) generuojančią matricą
    private static int[] encodeVector(int[] vector, int[][] generatorMatrix) {
        int columns = generatorMatrix[0].length;
        int[] encoded = new int[columns];

        for (int i = 0; i < columns; i++) {
            int sum = 0;
            for (int j = 0; j < vector.length; j++) {
                sum += vector[j] * generatorMatrix[j][i];
            }
            encoded[i] = sum % 2;
        }
        return encoded;
    }

    private static Random random = new Random();

    // Funkcija vektoriui perduoti per nepatikimą kanalą
    private static void transmitVector(int[] vector, double pe) {
        for (int i = 0; i < vector.length; i++) {
            if (random.nextDouble() < pe) {
                vector[i] ^= 1; // Apverčiame bitą
            }
        }
    }

    // Funkcija, kuri dekoduoja vektorių naudodama greitąją Hadamardo transformacijos funkciją
    private static int[] decodeVector(int[] receivedVector, int m) {
        int n = receivedVector.length;

        double[] transformed = new double[n];
        for (int i = 0; i < n; i++) {
            // Mapiname 0 -> 1 ir 1 -> -1
            transformed[i] = (receivedVector[i] == 0) ? 1.0 : -1.0;
        }

        fastHadamardTransform(transformed);

        // Surandame max reikšmę ir jos indeksą
        double maxVal = Math.abs(transformed[0]);
        int maxIndex = 0;
        for (int i = 1; i < transformed.length; i++) {
            double currentAbs = Math.abs(transformed[i]);
            if (currentAbs > maxVal) {
                maxVal = currentAbs;
                maxIndex = i;
            }
        }

        // Atkuriame informacijos bitus
        int[] infoBits = new int[m + 1];

        // Nustatome infoBits[0] pagal max reikšmės ženklą
        infoBits[0] = (transformed[maxIndex] >= 0) ? 0 : 1;

        // infoBits[1] iki infoBits[m] nustatome iš maksimalaus indekso bitų reprezentacijos (nuo MSB iki LSB)
        for (int i = 1; i <= m; i++) {
            infoBits[i] = (maxIndex >> (m - i)) & 1;
        }
        return infoBits;
    }

    // Greitoji Hadamardo transformacijos funkcija
    private static void fastHadamardTransform(double[] data) {
        int n = data.length;
        for (int size = 2; size <= n; size *= 2) {
            for (int i = 0; i < n; i += size) {
                for (int j = 0; j < size / 2; j++) {
                    double a = data[i + j];
                    double b = data[i + j + size / 2];
                    data[i + j] = a + b;
                    data[i + j + size / 2] = a - b;
                }
            }
        }
    }

    // Funkcija, kuri išprintina matricą
    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}
