
public class MixColumns {
	 
	 public static byte mix(int i, byte[] col){
		 switch(i) {
		 case 0: return (byte) (gmul2(col[0]) ^ gmul3(col[1]) ^ col[2] ^ col[3]);
		 case 1: return (byte) (col[0] ^ gmul2(col[1]) ^ gmul3(col[2]) ^ col[3]);
		 case 2: return (byte) (col[0] ^ col[1] ^ gmul2(col[2]) ^ gmul3(col[3]));
		 case 3: return (byte) (gmul3(col[0]) ^ col[1] ^ col[2] ^ gmul2(col[3]));
		 }
		return 0;
	 }
	 
	 
	 public static byte invMix(int i, byte[] col) {
		 switch(i) {
		 case 0: return (byte) (gmul14(col[0]) ^ gmul11(col[1]) ^ gmul13(col[2]) ^ gmul9(col[3]));
		 case 1: return (byte) (gmul9(col[0]) ^ gmul14(col[1]) ^ gmul11(col[2]) ^ gmul13(col[3]));
		 case 2: return (byte) (gmul13(col[0]) ^ gmul9(col[1]) ^ gmul14(col[2]) ^ gmul11(col[3]));
		 case 3: return (byte) (gmul11(col[0]) ^ gmul13(col[1]) ^ gmul9(col[2]) ^ gmul14(col[3]));
		 }
		 return 0;
	 }
	    
	 public static byte gmul2(byte a) {
		 byte hi = (byte) (a & 0x80);
		 a <<= 1;
		 if ((hi == (byte) 0x80))
			 a ^= 0x1b;
		 return a;
	 }

	 public static byte gmul3(byte a) {
		 byte b = a;
		 a = gmul2(a);
		 b ^= a;
		 return b;
	 }

	 public static byte gmul4(byte a) {
		 byte b = gmul2(a);
		 return gmul2(b);
	 }

	 public static byte gmul8(byte a) {
		 byte b = gmul2(a);
		 byte c = gmul2(b);
		 return gmul2(c);
	 }

	 public static byte gmul9(byte a) {
		 byte b;
		 b = (byte) (gmul8(a) ^ a);
		 return b;
	 }

	 public static byte gmul11(byte a) {
		 byte b;
		 b = (byte) (gmul8(a) ^ gmul2(a) ^ a);
		 return b;
	 }

	 public static byte gmul13(byte a) {
		 byte b;
		 b = (byte) (gmul8(a) ^ gmul4(a) ^ a);
		 return b;
	 }

	 public static byte gmul14(byte a) {
		 byte b;
		 b = (byte) (gmul8(a) ^ gmul4(a) ^ gmul2(a));
		 return b;
	 }
}
