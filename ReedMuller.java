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
        System.out.print("Įveskite informacijos vektorių (" + rows + " ilgio): ");
        int[] inputVector = new int[rows];
        for (int i = 0; i < rows; i++) {
            inputVector[i] = scanner.nextInt();
        }
        if (inputVector.length != rows) {
            System.out.println("Vektoriaus ilgis neteisingas. Jis turi būti " + rows);
            return;
        }

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
                receivedVector[i] = scanner.nextInt();
            }
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
        int[][] matrix = new int[rows][columns];

        // First row is all ones
        Arrays.fill(matrix[0], 1);

        // Next rows represent the binary expansions
        for (int i = 0; i < m; i++) {
            int period = (int) Math.pow(2, m - i - 1);
            for (int j = 0; j < columns; j++) {
                matrix[i + 1][j] = (j / period) % 2;
            }
        }
        return matrix;
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
    private static int[] decodeVector(int[] vector, int m) {
        int[] decoded = new int[m + 1];

        int columns = (int) Math.pow(2, m);
        int[] hadamard = Arrays.copyOf(vector, columns);

        // Apply Fast Hadamard Transform
        for (int len = 1; len < columns; len *= 2) {
            for (int i = 0; i < columns; i += 2 * len) {
                for (int j = 0; j < len; j++) {
                    int a = hadamard[i + j];
                    int b = hadamard[i + j + len];
                    hadamard[i + j] = (a + b) % 2;
                    hadamard[i + j + len] = (a - b + 2) % 2; // Ensure non-negative
                }
            }
        }

        // Find maximum absolute value
        int maxIndex = 0;
        for (int i = 0; i < columns; i++) {
            if (Math.abs(hadamard[i]) > Math.abs(hadamard[maxIndex])) {
                maxIndex = i;
            }
        }

        // Decode based on max index
        String binary = Integer.toBinaryString(maxIndex);
        while (binary.length() < m + 1) {
            binary = "0" + binary;
        }

        for (int i = 0; i < binary.length(); i++) {
            decoded[i] = binary.charAt(i) - '0';
        }
        return decoded;
    }

    // Utility function to print a matrix
    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}
