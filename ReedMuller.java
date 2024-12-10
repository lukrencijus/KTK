import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class ReedMuller {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Input m
        System.out.print("Įveskite kodo parametrą m: ");
        int m = scanner.nextInt();
        int columns = (int) Math.pow(2, m);
        int rows = m + 1;

        // Step 2: Generate Reed-Muller matrix
        int[][] generatorMatrix = generateReedMullerMatrix(m);
        System.out.println("Rydo-Miulerio generuojanti matrica:");
        printMatrix(generatorMatrix);

        // Step 3: Input vector
        System.out.print("Įveskite informacijos vektorių (" + rows + " ilgio)\n");
        int[] inputVector = new int[rows];

        for (int i = 0; i < rows; i++) {
            int input;
            while (true) { // Kartojama, kol įvestis teisinga
                System.out.print("Įveskite " + (i + 1) + "-ąjį elementą (0 arba 1): ");
                if (scanner.hasNextInt()) {  // Tikrina, ar įvestis yra sveikasis skaičius
                    input = scanner.nextInt();
                    if (input == 0 || input == 1) {
                        break; // Teisinga reikšmė, baigiame ciklą
                    } else {
                        System.out.println("Klaida: Vektoriaus elementai turi būti tik 0 arba 1.");
                    }
                } else {
                    System.out.println("Klaida: Įvestis turi būti sveikasis skaičius (0 arba 1).");
                    scanner.next(); // Pašalinama neteisinga įvestis
                }
            }
            inputVector[i] = input;
        }
        System.out.println("Įvestas vektorius: " + Arrays.toString(inputVector));


        // Step 4: Input error probability
        System.out.print("Įveskite klaidos tikimybę (0 <= p_e <= 1): ");
        double pe = scanner.nextDouble();
        if (pe < 0 || pe > 1) {
            System.out.println("Klaidos tikimybė turi būti tarp 0 ir 1.");
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

        // Allow user to edit received vector
        System.out.print("Ar norite redaguoti iš kanalo išėjusį vektorių? (taip/ne): ");
        String editChoice = scanner.next();
        if (editChoice.equalsIgnoreCase("taip")) {
            System.out.println("Įveskite naują vektorių (" + columns + " ilgio):");
            for (int i = 0; i < columns; i++) {
                int input;
                while (true) { // Kartojama, kol įvedama teisinga reikšmė
                    System.out.print("Įveskite " + (i + 1) + "-ąjį elementą (0 arba 1): ");
                    input = scanner.nextInt();
                    if (input == 0 || input == 1) {
                        break; // Teisinga reikšmė, baigiame ciklą
                    } else {
                        System.out.println("Klaida: Įvestas skaičius turi būti 0 arba 1.");
                    }
                }
                receivedVector[i] = input;
            }
            System.out.println("Naujas vektorius po redagavimo:");
            System.out.println(Arrays.toString(receivedVector));
        }

        // Step 7: Decode the vector
        int[] decodedVector = decodeVector(receivedVector, m);
        System.out.println("Dekoduotas vektorius:");
        System.out.println(Arrays.toString(decodedVector));

        scanner.close();
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

    private static int[] decodeVector(int[] receivedVector, int m) {
        int columns = (int) Math.pow(2, m);
        int rows = m + 1;

        // Patikrinimas, ar vektoriaus ilgis teisingas
        if (receivedVector.length != columns) {
            throw new IllegalArgumentException("Vektoriaus ilgis turi būti lygus 2^m.");
        }

        System.out.println("Pradinis vektorius dekodavimui: " + Arrays.toString(receivedVector));

        // Hadamardo transformacija
        int[] hadamard = hadamardTransform(receivedVector);

        System.out.println("Hadamardo transformacijos rezultatas: " + Arrays.toString(hadamard));

        // Rasti maksimalų indeksą pagal absoliučią reikšmę
        int maxIndex = 0;
        int maxValue = Math.abs(hadamard[0]);
        for (int i = 1; i < columns; i++) {
            if (Math.abs(hadamard[i]) > maxValue) {
                maxValue = Math.abs(hadamard[i]);
                maxIndex = i;
            }
        }

        System.out.println("Maksimali reikšmė: " + maxValue + ", indeksas: " + maxIndex);

        // Indeksą paversti į dvejetainį formatą
        String binary = Integer.toBinaryString(maxIndex);
        while (binary.length() < rows) {
            binary = "0" + binary; // Papildome nuliais, jei trūksta bitų
        }

        System.out.println("Maksimalus indeksas dvejetainiu formatu: " + binary);

        // Dekodavimas pagal Hadamardo transformacijos koeficientus
        int[] decoded = new int[rows];
        for (int i = 0; i < rows; i++) {
            decoded[i] = hadamard[maxIndex] > 0 ? 1 : 0;
        }

        System.out.println("Dekoduotas vektorius: " + Arrays.toString(decoded));
        return decoded;
    }

    private static int[] hadamardTransform(int[] vector) {
        int n = vector.length;
        int[] transformed = Arrays.copyOf(vector, n);

        for (int size = 1; size < n; size *= 2) {
            for (int i = 0; i < n; i += 2 * size) {
                for (int j = i; j < i + size; j++) {
                    int temp = transformed[j];
                    transformed[j] += transformed[j + size];
                    transformed[j + size] = temp - transformed[j + size];
                }
            }
        }

        return transformed;
    }



    // Utility function to print a matrix
    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}
