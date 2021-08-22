package com.dirror.music.music.kuwo;

public final class b {

    private static char[] char64 = new char[64];
    /* renamed from: c  reason: collision with root package name */
    private static byte[] char128 = new byte[128];

    static {
        char c2 = 'A';
        int i2 = 0;
        while (c2 <= 'Z') {
            char64[i2] = c2;
            c2 = (char) (c2 + 1);
            i2++;
        }
        char c3 = 'a';
        while (c3 <= 'z') {
            char64[i2] = c3;
            c3 = (char) (c3 + 1);
            i2++;
        }
        char c4 = '0';
        while (c4 <= '9') {
            char64[i2] = c4;
            c4 = (char) (c4 + 1);
            i2++;
        }
        char64[i2] = '+';
        char64[i2 + 1] = '/';
        for (int i3 = 0; i3 < char128.length; i3++) {
            char128[i3] = -1;
        }
        for (int i4 = 0; i4 < 64; i4++) {
            char128[char64[i4]] = (byte) i4;
        }
    }

    public static char[] bArrToCArr(byte[] bArr, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        int i10 = ((i2 * 4) + 2) / 3;
        char[] cArr = new char[(((i2 + 2) / 3) * 4)];
        int i11 = 0;
        int i12 = 0;
        while (i11 < i2) {
            int i13 = i11 + 1;
            int i14 = bArr[i11] & 255;
            if (i13 < i2) {
                i3 = i13 + 1;
                i4 = bArr[i13] & 255;
            } else {
                i3 = i13;
                i4 = 0;
            }
            if (i3 < i2) {
                i5 = i3 + 1;
                i6 = bArr[i3] & 255;
            } else {
                i5 = i3;
                i6 = 0;
            }
            int i15 = i14 >>> 2;
            int i16 = ((i14 & 3) << 4) | (i4 >>> 4);
            int i17 = ((i4 & 15) << 2) | (i6 >>> 6);
            int i18 = i6 & 63;
            int i19 = i12 + 1;
            cArr[i12] = char64[i15];
            int i20 = i19 + 1;
            cArr[i19] = char64[i16];
            char c2 = '=';
            cArr[i20] = i20 < i10 ? char64[i17] : '=';
            int i21 = i20 + 1;
            if (i21 < i10) {
                c2 = char64[i18];
            }
            cArr[i21] = c2;
            i12 = i21 + 1;
            i11 = i5;
        }
        return cArr;
    }
}