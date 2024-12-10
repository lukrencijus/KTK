import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.*;

public class ReedMuller {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n");
        System.out.println("Pasirinkite scenarijų:" +
                "\n [1] užrašyti vektorių" +
                "\n [2] užrašo tekstą" +
                "\n [3] nurodyti paveiksliuką");
        int userInput = scanner.nextInt();

        switch (userInput) {
            case 1:
                System.out.print("\nReed-Muller kodui (1, m)\nĮveskite kodo parametrą m: ");
                int m = scanner.nextInt();
                int columns = (int) Math.pow(2, m);
                int rows = m + 1;

                int[][] generatorMatrix = generateReedMullerMatrix(m);
                System.out.println("Rydo-Miulerio generuojanti matrica:");
                printMatrix(generatorMatrix);

                System.out.print("Įveskite informacijos vektorių (" + rows + " ilgio)\n");
                int[] inputVector = new int[rows];

                for (int i = 0; i < rows; i++) {
                    int input;
                    while (true) {
                        System.out.print("Įveskite " + (i + 1) + "-ąjį elementą (0 arba 1): ");
                        if (scanner.hasNextInt()) {
                            input = scanner.nextInt();
                            if (input == 0 || input == 1) {
                                break;
                            } else {
                                System.out.println("Klaida: Vektoriaus elementai turi būti tik 0 arba 1.");
                            }
                        }
                    }
                    inputVector[i] = input;
                }
                System.out.println("\nĮvestas vektorius: " + Arrays.toString(inputVector));

                // Step 4: Input error probability
                scanner.nextLine();
                System.out.print("Įveskite klaidos tikimybę (0 <= p_e <= 1): ");
                double pe = scanner.nextDouble();
                if (pe < 0 || pe > 1) {
                    System.out.println("Klaidos tikimybė turi būti tarp 0 ir 1.");
                    scanner.close();
                    return;
                }

                // Step 5: Encode the vector
                int[] encodedVector = encodeVector(inputVector, generatorMatrix);
                System.out.println("Užkoduotas vektorius:");
                System.out.println(Arrays.toString(encodedVector));

                // Step 6: Transmit through unreliable channel
                int[] receivedVector = transmitVector(encodedVector, pe);
                System.out.println("Iš kanalo išėjęs vektorius:");
                System.out.println(Arrays.toString(receivedVector));

                // Step 8: Detect and report errors between encoded and received vector
                detectErrors(encodedVector, receivedVector);

                // Allow user to edit received vector
                System.out.print("Ar norite redaguoti iš kanalo išėjusį vektorių? (taip/ne): ");
                String editChoice = scanner.next();
                if (editChoice.equalsIgnoreCase("taip")) {
                    System.out.println("Įveskite naują vektorių (" + columns + " ilgio):");
                    for (int i = 0; i < columns; i++) {
                        int inputBit;
                        while (true) {
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
                    System.out.println("Naujas vektorius po redagavimo:");
                    System.out.println(Arrays.toString(receivedVector));
                    detectErrors(encodedVector, receivedVector);
                }

                // Step 7: Decode the vector
                int[] decodedVector = decodeVector(receivedVector, m);
                System.out.println("Dekoduotas vektorius:");
                System.out.println(Arrays.toString(decodedVector));

                scanner.close();

            case 2:
                System.out.println("Įveskite tekstą (galite įvesti kelias eilutes). Pabaigai įrašykite 'exit':");

                // Naudotojo įvestis, kad įrašytų kelias eilutes
                StringBuilder inputText = new StringBuilder();
                String line;
                while (!(line = scanner.nextLine()).equalsIgnoreCase("exit")) {
                    inputText.append(line).append("\n");
                }

                // Nustatome kūno Fq elementų ilgį
                int vectorLength = 5; // Pvz., vektoriai bus ilgio 5
                int q = 2; // Kūnas F10 (skaitmenys nuo 0 iki 9)

                // Konvertuojame tekstą į skaitmenis
                List<Integer> vectorElements = convertTextToFq(inputText.toString(), q);

                // Suskaidome į vektorius
                List<List<Integer>> vectors = splitIntoVectors(vectorElements, vectorLength);

                // Išvedame rezultatus
                System.out.println("Gauti vektoriai:");
                for (List<Integer> vector : vectors) {
                    System.out.println(vector);
                }

                scanner.close();


        } //baigiasi switch

    }

    // Funkcija konvertuoti tekstą į skaitmenis (kūnas F10)
    public static List<Integer> convertTextToFq(String text, int q) {
        List<Integer> elements = new ArrayList<>();

        // Kiekvienas simbolis konvertuojamas į skaitmenį (ASCII vertė % q)
        for (char c : text.toCharArray()) {
            int element = c % q;  // Konvertuojame į skaitmenį F_q
            elements.add(element);
        }
        return elements;
    }

    // Funkcija suskaidyti į vektorius
    public static List<List<Integer>> splitIntoVectors(List<Integer> elements, int vectorLength){
        List<List<Integer>> vectors = new ArrayList<>();

        for (int i = 0; i < elements.size(); i += vectorLength) {
            List<Integer> vector = new ArrayList<>();
            for (int j = i; j < i + vectorLength && j < elements.size(); j++) {
                vector.add(elements.get(j));
            }
            vectors.add(vector);
        }
        return vectors;
    }

    // Klaidų detekcijos funkcija
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

        // Išvedame rezultatus
        if (errorCount == 0) {
            System.out.println("Klaidų nėra.");
        } else {
            System.out.println("Klaidų skaičius: " + errorCount);
            System.out.println("Klaidos pozicijos: " + errorPositions.toString().trim());
        }
    }

    // Generate Reed-Muller (1, m) generator matrix
    private static int[][] generateReedMullerMatrix(int m) {
        int columns = (int) Math.pow(2, m);
        int rows = m + 1;

        int[][] generatorMatrix = new int[rows][columns];

        // Pirmoji eilutė pilna vienetų
        Arrays.fill(generatorMatrix[0], 1);

        // Kitos eilutės atitinka dvejetainius derinius
        for (int i = 0; i < m; i++) {
            int period = (int) Math.pow(2, m - i - 1);
            for (int j = 0; j < columns; j++) {
                generatorMatrix[i + 1][j] = (j / period) % 2;
            }
        }

        return generatorMatrix;
    }

    // Encode vector using the generator matrix
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

    // Transmit vector through an unreliable channel
    private static int[] transmitVector(int[] vector, double pe) {
        Random random = new Random();
        int[] transmitted = Arrays.copyOf(vector, vector.length);

        for (int i = 0; i < vector.length; i++) {
            if (random.nextDouble() < pe) {
                transmitted[i] = 1 - transmitted[i]; // Flip the bit
            }
        }
        return transmitted;
    }

    // Decode vector using Fast Hadamard Transform
    private static int[] decodeVector(int[] receivedVector, int m) {
        int n = receivedVector.length;


        // Žingsnis 1: Mapavimas 0 -> 1 ir 1 -> -1
        double[] mapped = new double[n];
        for (int i = 0; i < n; i++) {
            mapped[i] = (receivedVector[i] == 0) ? 1.0 : -1.0;
        }

        // Žingsnis 2: Atlikti Fast Hadamard Transform
        double[] transformed = fastHadamardTransform(mapped);

        // Išspausdinti transformuotą vektorių
        System.out.println("Transformuotas vektorius: " + Arrays.toString(transformed));

        // Žingsnis 3: Surasti indeksą su maksimaliu absoliučiu koeficientu
        double maxVal = Math.abs(transformed[0]);
        int maxIndex = 0;
        for (int i = 1; i < transformed.length; i++) {
            double currentAbs = Math.abs(transformed[i]);
            if (currentAbs > maxVal) {
                maxVal = currentAbs;
                maxIndex = i;
            }
        }
        System.out.println("Maksimali reikšmė: " + maxVal + ", indeksas: " + maxIndex);

        // Žingsnis 4: Atkuriame informacijos bitus
        int[] infoBits = new int[m + 1];

        // b0 nustatomas pagal koeficiento ženklą
        infoBits[0] = (transformed[maxIndex] >= 0) ? 0 : 1;

        // b1 iki bm nustatomi iš maksimalaus indekso bitų reprezentacijos (nuo MSB iki LSB)
        for (int i = 1; i <= m; i++) {
            infoBits[i] = (maxIndex >> (m - i)) & 1;
        }

        return infoBits;
    }

    // Fast Hadamard Transform (iteratyvi implementacija)
    private static double[] fastHadamardTransform(double[] a) {
        int n = a.length;

        // Patikriname, ar n yra 2^k
        if ((n & (n - 1)) != 0) {
            throw new IllegalArgumentException("Vektoriaus ilgis turi būti 2^k.");
        }

        // Kopijuojame pradinį vektorių
        double[] A = Arrays.copyOf(a, n);

        // Atlikti FHT
        for (int step = 1; step < n; step <<= 1) {
            for (int i = 0; i < n; i += 2 * step) {
                for (int j = 0; j < step; j++) {
                    double u = A[i + j];
                    double v = A[i + j + step];
                    A[i + j] = u + v;
                    A[i + j + step] = u - v;
                }
            }
        }

        return A;
    }

    // Utility function to print a matrix
    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}
