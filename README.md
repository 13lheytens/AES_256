HOW TO USE:

AES.java takes the following arguments in order: 
<ul>
<li>Key file name (hex value of key on first line of file)</li>
<li>Input file name (hex values)</li>
<li>Output file name (hex values)</li>
<li>An encryption/decryption flag (-e or -d)</li>
</ul>

EXAMPLE:
<pre>
java AES key.txt -i input.txt -o encrypted.txt -e
java AES key.txt -i input.txt -o decrypted.txt -d
</pre>

FILES:

1. *AES.java* - This is the main file that reads in arguments, encrypts or decrypts in blocks of 128 bits, and writes the encrypted/decrypted values to the output file.
2. *MixColumns.java* - The resulting bytes in the state are equal to a combination of row-specific multiplication (in Rijndael Galois field) and the XOR operation with each element in the column. 
3. *SubBytes.java* - Substitutes each byte for a new byte using substitution table (S-Box). S-Box comes from combination of modular multiplicative inverse, affine transformation, and XOR with 0x63.
4. *KeySchedule.java* - Creates the rounds from supplied key using rotation, substitution, exponentiation of 2 (in finite field), and XOR with previous round keys.
5. *sample_key.txt* - Example key
6. *sample_input.txt* - Example input
7. *sample_output_encrypted.txt* - Encoded input.txt
8. *sample_output_decrypted.txt* - Decoded input.txt

AES:

The Advanced Encryption Standard or AES is a symmetric block cipher used by the U.S. government to protect classified information.

It is implemented in software and hardware throughout the world to encrypt sensitive data. The key size options are 128, 192, and 256 bits.

Each "round" of encryption and decryption involves four steps:

1. Substituting byte for byte with Substitution table
2. Shifting nth row of the current state by n bytes
3. Mixing the Columns of the current state
4. XORing the state with the round key for the current round
