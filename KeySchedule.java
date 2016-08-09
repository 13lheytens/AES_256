
public class KeySchedule {
	
	public static int rconCount = 1;

	public static byte[] schedule_core(byte[] bs, int i){
		byte[] res;
		res = rotate(bs);
		for (int j = 0; j < res.length; j++)
			res[j] = SubBytes.sub(res[j]);
		res[0] ^= rcon(i);
		return res;
	}
	
	public static byte[] rotate(byte[] b){
		byte[] res = {b[1],b[2],b[3],b[0]};
		return res;
	}
	
	public static int rcon(int i){
		int in = i;
		int c = 1;
		if (in == 0)
			return 0;
		while (in > 1){
			int b = c & 0x80;
			c <<= 1;
			if (b == 0x80)
				c ^= 0x1b;
			in--;	
		}
		return c;
	}
	
	public static byte[][] keyExpansion1(byte[][] key, byte[] last){
		byte[] temp = last;
		temp = schedule_core(temp,rconCount);
		rconCount++;
		temp = keyXor(temp,key[0]);
		byte[] temp2 = keyXor(temp, key[1]);
		byte[] temp3 = keyXor(temp2, key[2]);
		byte[] temp4 = keyXor(temp3, key[3]);
		return new byte[][]{temp,temp2,temp3,temp4};
	}
	
	public static byte[][] keyExpansion2(byte[][] key, byte[] last){
		byte[] t5 = last;
		byte[] t = new byte[4];
		for (int j = 0; j < 4; j++)
			t[j] = SubBytes.sub(t5[j]);
		byte[] temp = keyXor(t, key[0]);
		byte[] temp2 = keyXor(temp, key[1]);
		byte[] temp3 = keyXor(temp2, key[2]);
		byte[] temp4 = keyXor(temp3, key[3]);
		return new byte[][]{temp,temp2,temp3,temp4};
	}
	
	public static byte[] keyXor(byte[] b1, byte[] b2){
		byte[] res = new byte[4];
		for (int i = 0; i < 4; i++)
			res[i] = (byte) (b1[i] ^ b2[i]);
		return res;
	}
	
	public static byte[][][] getRoundKeys(byte[][] key){
		byte[][][] roundKeys = new byte[15][4][4];
		roundKeys[0] = new byte[4][4];
		for (int i = 0; i < 4; i++)
			roundKeys[0][i] = key[i];
		roundKeys[1] = new byte[4][4];
		for (int i = 4; i < 8; i++)
			roundKeys[1][i-4] = key[i];
		for (int i = 2; i < 14; i +=2){
			roundKeys[i] = keyExpansion1(roundKeys[i-2], roundKeys[i-1][3]);
			roundKeys[i+1] = keyExpansion2(roundKeys[i-1], roundKeys[i][3]);
		}
		roundKeys[14] = keyExpansion1(roundKeys[12], roundKeys[13][3]);
		return roundKeys;
	}
	
}
