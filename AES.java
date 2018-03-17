import java.util.Scanner;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static java.lang.System.*;

public class AES
{

    public static byte[][] state;
    public static byte[][][] keyRounds;
    public static int keyLengthBits = 256;
    public static int rounds = (keyLengthBits + 192) / 32;

    public static void printUsage()
    {
        System.out.println("Usage: java AES <key.txt> -i <input.txt> -o <output.txt> [-e -d]");
        System.out.println("                <key.txt>:      file name that contains key");
        System.out.println("                <input.txt>:    file name that contains content to be encrypted");
        System.out.println("                <output.txt>:   file name that will contain encrypted content");
        System.out.println("                [-e]:           encryption");
        System.out.println("                [-d]:           decryption");
        System.out.println("Examples:");
        System.out.println("       java AES my_key.txt -i my_input.txt -o my_output.txt -e");
        System.out.println("       java AES my_key.txt -i my_input.txt -o my_output.txt -d");
    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
    {
        // Check for invalid argument format
        if (args.length != 6 || !args[1].equals("-i") || !args[3].equals("-o") || (!args[5].equals("-e") && !args[5].equals("-d")))
        {
            printUsage();
            System.exit(1);
            return;
        }

        // Time before encryption/decryption process starts
        long startTime = System.currentTimeMillis();

        // Read inputs
        File fKey = new File(args[0]);
        File fInput = new File(args[2]);
        File fOutput = new File(args[4]);
        boolean bEncrypt = args[5].toLowerCase().equals("-e");

        // Perform encryption/decryption
        byte[][] key = getKeyTable(fKey);
        keyRounds = KeySchedule.getRoundKeys(key);
        String output = encryptDecrypt(fInput, bEncrypt);
        writeToOutputFile(fOutput, output);

        // Time after encryption/decryption is finished
        long endTime = System.currentTimeMillis();
        long milliDuration = endTime - startTime;

        System.out.println("Duration: " + milliDuration + " milliseconds");
    }

    // Returns a byte array that represents the supplied key
    public static byte[][] getKeyTable(File in) throws FileNotFoundException
    {
        Scanner keyFileScanner = new Scanner(in);
        String keyString = keyFileScanner.nextLine();

        // The key should be only one line of text
        keyFileScanner.close();

        if (keyString.length() != keyLengthBits / 4)
        {
            throw new RuntimeException("Key should be of length " + (keyLengthBits / 4) + " on the first line of " + in.getName());
        }

        int numRows = keyLengthBits / 32;
        byte[][] result = new byte[numRows][4];
        for (int i = 0; i < keyLengthBits / 4; i += 2)
        {
            String strByte = keyString.substring(i, i + 2);
            char[] lineChars = strByte.toCharArray();
            for (char c : lineChars)
            {
                if (!isHexChar(c))
                {
                    throw new RuntimeException(in.getName() + ": Not valid hexidecimal character '" + c + "'");
                }
            }
            // converts substring of length 2 (representing hex) to byte, and places in array
            result[i / numRows][(i / 2) % 4] = Integer.decode("0x" + strByte).byteValue();
        }

        return result;
    }


    public static boolean isHexChar(char c)
    {
        return (('0' <= c) && (c <= '9')) || (('a' <= c) && (c <= 'f'));
    }

    public static String encryptDecrypt(File finput, boolean encrypt) throws FileNotFoundException
    {
        Scanner sc = new Scanner(finput);
        StringBuffer output = new StringBuffer(1024);
        String line;
        while (sc.hasNextLine())
        {
            line = formatInputLine(sc.nextLine());
            while (line.length() > 32)
            {
                aes(encrypt, line.substring(0, 32));
                output.append(stateString());
                line = line.substring(32);
            }
            aes(encrypt, line);
            output.append(stateString() + "\n");
        }
        sc.close();
        return output.toString();
    }

    public static String formatInputLine(String line)
    {
        line = line.toLowerCase();
        char[] lineChars = line.toCharArray();
        for (char c : lineChars)
            if (!isHexChar(c))
                System.err.println("Not valid hex char: " + c);
        // Pads line with 0's if not multiple of block size 32 (16 bytes)
        while (line.length() % 32 != 0)
            line = line + "0";
        return line;
    }

    public static void aes(boolean e, String input)
    {
        if (e)
            encrypt(input);
        else
            decrypt(input);
    }

    public static void encrypt(String input)
    {
        state = getInputArray(input);
        addRoundKey(0);
        for (int r = 1; r < rounds; r++)
        {
            subBytes();
            shiftRows();
            mixColumns();
            addRoundKey(r);
        }
        subBytes();
        shiftRows();
        addRoundKey(rounds);
    }

    public static void decrypt(String input)
    {
        state = getInputArray(input);
        addRoundKey(rounds);
        for (int r = rounds - 1; r > 0; r--)
        {
            invShiftRows();
            invSubBytes();
            addRoundKey(r);
            invMixColumns();
        }
        invShiftRows();
        invSubBytes();
        addRoundKey(0);
    }

    public static byte[][] getInputArray(String s)
    {
        byte[][] result = new byte[4][4];
        for (int i = 0; i < 32; i += 2)
        {
            String strByte = s.substring(i, i + 2);
            result[(i / 2) % 4][(i / 8)] = Integer.decode("0x" + strByte).byteValue();
        }
        return result;
    }

    public static void addRoundKey(int round)
    {
        byte[][] rk = keyRounds[round];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                state[i][j] = (byte) (state[i][j] ^ rk[j][i]);
    }

    public static void subBytes()
    {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                state[j][i] = SubBytes.sub(state[j][i]);
    }

    public static void invSubBytes()
    {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                state[j][i] = SubBytes.invSub(state[j][i]);
    }

    public static void shiftRows()
    {
        for (int i = 1; i < 4; i++)
        {
            byte b0 = state[i][0];
            byte b1 = state[i][1];
            byte b2 = state[i][2];
            byte b3 = state[i][3];
            if (i == 1)
            {
                state[i][0] = b1;
                state[i][1] = b2;
                state[i][2] = b3;
                state[i][3] = b0;
            }
            else if (i == 2)
            {
                state[i][0] = b2;
                state[i][1] = b3;
                state[i][2] = b0;
                state[i][3] = b1;
            }
            else if (i == 3)
            {
                state[i][0] = b3;
                state[i][1] = b0;
                state[i][2] = b1;
                state[i][3] = b2;
            }
        }
    }

    public static void invShiftRows()
    {
        for (int i = 1; i < 4; i++)
        {
            byte b0 = state[i][0];
            byte b1 = state[i][1];
            byte b2 = state[i][2];
            byte b3 = state[i][3];
            if (i == 1)
            {
                state[i][0] = b3;
                state[i][1] = b0;
                state[i][2] = b1;
                state[i][3] = b2;
            }
            else if (i == 2)
            {
                state[i][0] = b2;
                state[i][1] = b3;
                state[i][2] = b0;
                state[i][3] = b1;
            }
            else if (i == 3)
            {
                state[i][0] = b1;
                state[i][1] = b2;
                state[i][2] = b3;
                state[i][3] = b0;
            }
        }
    }

    public static void mixColumns()
    {
        byte[] column;
        for (int i = 0; i < 4; i++)
        {
            column = new byte[] {state[0][i], state[1][i], state[2][i], state[3][i]};
            state[0][i] = MixColumns.mix(0, column);
            state[1][i] = MixColumns.mix(1, column);
            state[2][i] = MixColumns.mix(2, column);
            state[3][i] = MixColumns.mix(3, column);
        }
    }

    public static void invMixColumns()
    {
        byte[] column;
        for (int i = 0; i < 4; i++)
        {
            column = new byte[] {state[0][i], state[1][i], state[2][i], state[3][i]};
            state[0][i] = MixColumns.invMix(0, column);
            state[1][i] = MixColumns.invMix(1, column);
            state[2][i] = MixColumns.invMix(2, column);
            state[3][i] = MixColumns.invMix(3, column);
        }
    }

    public static String stateString()
    {
        StringBuffer result = new StringBuffer(16);
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                result.append(String.format("%02x", state[j][i]));
        return result.toString();
    }

    public static void writeToOutputFile(File f, String s) throws FileNotFoundException
    {
        PrintWriter writer = new PrintWriter(f);
        writer.print(s);
        writer.close();
    }

    public static void printState()
    {
        for (byte[] b : state)
        {
            for (byte h : b)
                System.out.print(Integer.toHexString(h & 0xff) + " ");
            System.out.println();
        }
        System.out.println();
    }

}
