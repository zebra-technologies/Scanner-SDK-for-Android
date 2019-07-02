package com.zebra.scannercontrol.app.barcode;


/**
 * // Input - NULL terminated
 * //  ASCII symbol from 0x20 (space) to 0x127 (DEL)
 * //  FNC1 - 0x01, FNC2 - 0x02, FNC3 - 0x03, FNC4 - 0x04
 * // Output - shall be length of input + 3 (Start, Control, Stop)
 * // Output unit format - LSB X1X1X2X2X3X3X4X4X5X5X6X60000 MSB
 * //                           ^   ^   ^   ^   ^   ^
 * //                          bar  |  bar  |  bar  |
 * //                             space   space   space
 * // XnXn -
 * //      00 - 1
 * //      01 - 2
 * //      10 - 3
 * //      11 - 4
 * // The stop symbol is always 2331112 - 0110100000000100
 */
class Barcode128 {
    private static String TAG;

    private static int[] input;
    private static int[] output;
    private static int width;


    public static int CODE1(int a) {
        int result = ((a % 10) - 1);
        return result;
    }

    public static int CODE2(int a) {
        int result = ((a / 10) % 10 - 1);
        return result;
    }

    public static int CODE3(int a) {
        int result = ((a / 100) % 10 - 1);
        return result;
    }

    public static int CODE4(int a) {
        int result = ((a / 1000) % 10 - 1);
        return result;
    }

    public static int CODE5(int a) {
        int result = ((a / 10000) % 10 - 1);
        return result;
    }

    public static int CODE6(int a) {
        int result = ((a / 100000) % 10 - 1);
        return result;
    }

    public static int CODE7(int a) {
        int result = ((a / 1000000) % 10 - 1);
        return result;
    }

    public static int BARCODE128(int a) {
        return (CODE6(a) | (CODE5(a) * 4) | (CODE4(a) * 16) | (CODE3(a) * 64) | (CODE2(a) * 256)
                | (CODE1(a) * 1024));
    }

    public static int BARCODE128STARTB() {
        return BARCODE128(211214);
    }

    public static int BARCODE128STARTBWIDTH() {
        return (2 + 1 + 1 + 2 + 1 + 4);
    }

    public static int BARCODE128STARTBNO() {
        return 104;
    }

    public static int BARCODE128STOP() {
        return (CODE7(2331112) | (CODE6(2331112) * 4) | (CODE5(2331112) * 16)
                | (CODE4(2331112) * 64) | (CODE3(2331112) * 256) | (CODE2(2331112) * 1024)
                | (CODE1(2331112) * 4096));
    }

    public static int BARCODE128STOPWIDTH() {
        return (2 + 3 + 3 + 1 + 1 + 1 + 2);
    }

    public static int[] s_bc128Bcodes;
    public static int[] s_bc128Bcode_widths;
    public static int[] s_bc128Bnum_codes;
    public static int[] s_bc128Bnum_code_widths;

    static {
        //Initialization
        s_bc128Bcodes = new int[128];
        s_bc128Bcodes[0] = 0;
        s_bc128Bcodes[1] = BARCODE128(411131); // FNC1
        s_bc128Bcodes[2] = BARCODE128(411113); // FNC2
        s_bc128Bcodes[3] = BARCODE128(114311); // FNC3
        s_bc128Bcodes[4] = BARCODE128(114131); // FNC4
        s_bc128Bcodes[5] = 0;
        s_bc128Bcodes[6] = 0;
        s_bc128Bcodes[7] = 0;
        s_bc128Bcodes[8] = 0;
        s_bc128Bcodes[9] = 0;
        s_bc128Bcodes[10] = 0;
        s_bc128Bcodes[11] = 0;
        s_bc128Bcodes[12] = 0;
        s_bc128Bcodes[13] = 0;
        s_bc128Bcodes[14] = 0;
        s_bc128Bcodes[15] = 0;
        s_bc128Bcodes[16] = 0;
        s_bc128Bcodes[17] = 0;
        s_bc128Bcodes[18] = 0;
        s_bc128Bcodes[19] = 0;
        s_bc128Bcodes[20] = 0;
        s_bc128Bcodes[21] = 0;
        s_bc128Bcodes[22] = 0;
        s_bc128Bcodes[23] = 0;
        s_bc128Bcodes[24] = 0;
        s_bc128Bcodes[25] = 0;
        s_bc128Bcodes[26] = 0;
        s_bc128Bcodes[27] = 0;
        s_bc128Bcodes[28] = 0;
        s_bc128Bcodes[29] = 0;
        s_bc128Bcodes[30] = 0;
        s_bc128Bcodes[31] = 0;
        s_bc128Bcodes[32] = BARCODE128(212222); //	space
        s_bc128Bcodes[33] = BARCODE128(222122); //	!
        s_bc128Bcodes[34] = BARCODE128(222221); //	"
        s_bc128Bcodes[35] = BARCODE128(121223); //	#
        s_bc128Bcodes[36] = BARCODE128(121322); //	$
        s_bc128Bcodes[37] = BARCODE128(131222); //	%
        s_bc128Bcodes[38] = BARCODE128(122213); //	&
        s_bc128Bcodes[39] = BARCODE128(122312); //	'
        s_bc128Bcodes[40] = BARCODE128(132212); //	(
        s_bc128Bcodes[41] = BARCODE128(221213); //	)
        s_bc128Bcodes[42] = BARCODE128(221312); //	*
        s_bc128Bcodes[43] = BARCODE128(231212); //	+
        s_bc128Bcodes[44] = BARCODE128(112232); //	,
        s_bc128Bcodes[45] = BARCODE128(122132); //	-
        s_bc128Bcodes[46] = BARCODE128(122231); //	.
        s_bc128Bcodes[47] = BARCODE128(113222); //	/
        s_bc128Bcodes[48] = BARCODE128(123122); //	0
        s_bc128Bcodes[49] = BARCODE128(123221); //	1
        s_bc128Bcodes[50] = BARCODE128(223211); //	2
        s_bc128Bcodes[51] = BARCODE128(221132); //	3
        s_bc128Bcodes[52] = BARCODE128(221231); //	4
        s_bc128Bcodes[53] = BARCODE128(213212); //	5
        s_bc128Bcodes[54] = BARCODE128(223112); //	6
        s_bc128Bcodes[55] = BARCODE128(312131); //	7
        s_bc128Bcodes[56] = BARCODE128(311222); //	8
        s_bc128Bcodes[57] = BARCODE128(321122); //	9
        s_bc128Bcodes[58] = BARCODE128(321221); //	:
        s_bc128Bcodes[59] = BARCODE128(312212); //	;
        s_bc128Bcodes[60] = BARCODE128(322112); //	<
        s_bc128Bcodes[61] = BARCODE128(322211); //	=
        s_bc128Bcodes[62] = BARCODE128(212123); //	>
        s_bc128Bcodes[63] = BARCODE128(212321); //	?
        s_bc128Bcodes[64] = BARCODE128(232121); //	@
        s_bc128Bcodes[65] = BARCODE128(111323); //	A
        s_bc128Bcodes[66] = BARCODE128(131123); //	B
        s_bc128Bcodes[67] = BARCODE128(131321); //	C
        s_bc128Bcodes[68] = BARCODE128(112313); //	D
        s_bc128Bcodes[69] = BARCODE128(132113); //	E
        s_bc128Bcodes[70] = BARCODE128(132311); //	F
        s_bc128Bcodes[71] = BARCODE128(211313); //	G
        s_bc128Bcodes[72] = BARCODE128(231113); //	H
        s_bc128Bcodes[73] = BARCODE128(231311); //	I
        s_bc128Bcodes[74] = BARCODE128(112133); //	J
        s_bc128Bcodes[75] = BARCODE128(112331); //	K
        s_bc128Bcodes[76] = BARCODE128(132131); //	L
        s_bc128Bcodes[77] = BARCODE128(113123); //	M
        s_bc128Bcodes[78] = BARCODE128(113321); //	N
        s_bc128Bcodes[79] = BARCODE128(133121); //	O
        s_bc128Bcodes[80] = BARCODE128(313121); //	P
        s_bc128Bcodes[81] = BARCODE128(211331); //	Q
        s_bc128Bcodes[82] = BARCODE128(231131); //	R
        s_bc128Bcodes[83] = BARCODE128(213113); //	S
        s_bc128Bcodes[84] = BARCODE128(213311); //	T
        s_bc128Bcodes[85] = BARCODE128(213131); //	U
        s_bc128Bcodes[86] = BARCODE128(311123); //	V
        s_bc128Bcodes[87] = BARCODE128(311321); //	W
        s_bc128Bcodes[88] = BARCODE128(331121); //	X
        s_bc128Bcodes[89] = BARCODE128(312113); //	Y
        s_bc128Bcodes[90] = BARCODE128(312311); //	Z
        s_bc128Bcodes[91] = BARCODE128(332111); //	[
        s_bc128Bcodes[92] = BARCODE128(314111); //	\

        s_bc128Bcodes[93] = BARCODE128(221411); //	]
        s_bc128Bcodes[94] = BARCODE128(431111); //	^
        s_bc128Bcodes[95] = BARCODE128(111224); //	_
        s_bc128Bcodes[96] = BARCODE128(111422); //	`
        s_bc128Bcodes[97] = BARCODE128(121124); //	a
        s_bc128Bcodes[98] = BARCODE128(121421); //	b
        s_bc128Bcodes[99] = BARCODE128(141122); //	c
        s_bc128Bcodes[100] = BARCODE128(141221); //	d
        s_bc128Bcodes[101] = BARCODE128(112214); //	e
        s_bc128Bcodes[102] = BARCODE128(112412); //	f
        s_bc128Bcodes[103] = BARCODE128(122114); //	g
        s_bc128Bcodes[104] = BARCODE128(122411); //	h
        s_bc128Bcodes[105] = BARCODE128(142112); //	i
        s_bc128Bcodes[106] = BARCODE128(142211); //	j
        s_bc128Bcodes[107] = BARCODE128(241211); //	k
        s_bc128Bcodes[108] = BARCODE128(221114); //	l
        s_bc128Bcodes[109] = BARCODE128(413111); //	m
        s_bc128Bcodes[110] = BARCODE128(241112); //	n
        s_bc128Bcodes[111] = BARCODE128(134111); //	o
        s_bc128Bcodes[112] = BARCODE128(111242); //	p
        s_bc128Bcodes[113] = BARCODE128(121142); //	q
        s_bc128Bcodes[114] = BARCODE128(121241); //	r
        s_bc128Bcodes[115] = BARCODE128(114212); //	s
        s_bc128Bcodes[116] = BARCODE128(124112); //	t
        s_bc128Bcodes[117] = BARCODE128(124211); //	u
        s_bc128Bcodes[118] = BARCODE128(411212); //	v
        s_bc128Bcodes[119] = BARCODE128(421112); //	w
        s_bc128Bcodes[120] = BARCODE128(421211); //	x
        s_bc128Bcodes[121] = BARCODE128(212141); //	y
        s_bc128Bcodes[122] = BARCODE128(214121); //	z
        s_bc128Bcodes[123] = BARCODE128(412121); //	{
        s_bc128Bcodes[124] = BARCODE128(111143); //	|
        s_bc128Bcodes[125] = BARCODE128(111341); //	}
        s_bc128Bcodes[126] = BARCODE128(131141); //	~
        s_bc128Bcodes[127] = BARCODE128(114113); //	DEL


        s_bc128Bcode_widths = new int[128];
        s_bc128Bcode_widths[0] = 0;
        s_bc128Bcode_widths[1] = (4 + 1 + 1 + 1 + 3 + 1); // FNC1
        s_bc128Bcode_widths[2] = (4 + 1 + 1 + 1 + 1 + 3); // FNC2
        s_bc128Bcode_widths[3] = (1 + 1 + 4 + 3 + 1 + 1); // FNC3
        s_bc128Bcode_widths[4] = (1 + 1 + 4 + 1 + 3 + 1); // FNC4
        s_bc128Bcode_widths[5] = 0;
        s_bc128Bcode_widths[6] = 0;
        s_bc128Bcode_widths[7] = 0;
        s_bc128Bcode_widths[8] = 0;
        s_bc128Bcode_widths[9] = 0;
        s_bc128Bcode_widths[10] = 0;
        s_bc128Bcode_widths[11] = 0;
        s_bc128Bcode_widths[12] = 0;
        s_bc128Bcode_widths[13] = 0;
        s_bc128Bcode_widths[14] = 0;
        s_bc128Bcode_widths[15] = 0;
        s_bc128Bcode_widths[16] = 0;
        s_bc128Bcode_widths[17] = 0;
        s_bc128Bcode_widths[18] = 0;
        s_bc128Bcode_widths[19] = 0;
        s_bc128Bcode_widths[20] = 0;
        s_bc128Bcode_widths[21] = 0;
        s_bc128Bcode_widths[22] = 0;
        s_bc128Bcode_widths[23] = 0;
        s_bc128Bcode_widths[24] = 0;
        s_bc128Bcode_widths[25] = 0;
        s_bc128Bcode_widths[26] = 0;
        s_bc128Bcode_widths[27] = 0;
        s_bc128Bcode_widths[28] = 0;
        s_bc128Bcode_widths[29] = 0;
        s_bc128Bcode_widths[30] = 0;
        s_bc128Bcode_widths[31] = 0;
        s_bc128Bcode_widths[32] = (2 + 1 + 2 + 2 + 2 + 2); //	space
        s_bc128Bcode_widths[33] = (2 + 2 + 2 + 1 + 2 + 2); //	!
        s_bc128Bcode_widths[34] = (2 + 2 + 2 + 2 + 2 + 1); //	"
        s_bc128Bcode_widths[35] = (1 + 2 + 1 + 2 + 2 + 3); //	#
        s_bc128Bcode_widths[36] = (1 + 2 + 1 + 3 + 2 + 2); //	$
        s_bc128Bcode_widths[37] = (1 + 3 + 1 + 2 + 2 + 2); //	%
        s_bc128Bcode_widths[38] = (1 + 2 + 2 + 2 + 1 + 3); //	&
        s_bc128Bcode_widths[39] = (1 + 2 + 2 + 3 + 1 + 2); //	'
        s_bc128Bcode_widths[40] = (1 + 3 + 2 + 2 + 1 + 2); //	(
        s_bc128Bcode_widths[41] = (2 + 2 + 1 + 2 + 1 + 3); //	)
        s_bc128Bcode_widths[42] = (2 + 2 + 1 + 3 + 1 + 2); //	*
        s_bc128Bcode_widths[43] = (2 + 3 + 1 + 2 + 1 + 2); //	+
        s_bc128Bcode_widths[44] = (1 + 1 + 2 + 2 + 3 + 2); //	,
        s_bc128Bcode_widths[45] = (1 + 2 + 2 + 1 + 3 + 2); //	-
        s_bc128Bcode_widths[46] = (1 + 2 + 2 + 2 + 3 + 1); //	.
        s_bc128Bcode_widths[47] = (1 + 1 + 3 + 2 + 2 + 2); //	/
        s_bc128Bcode_widths[48] = (1 + 2 + 3 + 1 + 2 + 2); //	0
        s_bc128Bcode_widths[49] = (1 + 2 + 3 + 2 + 2 + 1); //	1
        s_bc128Bcode_widths[50] = (2 + 2 + 3 + 2 + 1 + 1); //	2
        s_bc128Bcode_widths[51] = (2 + 2 + 1 + 1 + 3 + 2); //	3
        s_bc128Bcode_widths[52] = (2 + 2 + 1 + 2 + 3 + 1); //	4
        s_bc128Bcode_widths[53] = (2 + 1 + 3 + 2 + 1 + 2); //	5
        s_bc128Bcode_widths[54] = (2 + 2 + 3 + 1 + 1 + 2); //	6
        s_bc128Bcode_widths[55] = (3 + 1 + 2 + 1 + 3 + 1); //	7
        s_bc128Bcode_widths[56] = (3 + 1 + 1 + 2 + 2 + 2); //	8
        s_bc128Bcode_widths[57] = (3 + 2 + 1 + 1 + 2 + 2); //	9
        s_bc128Bcode_widths[58] = (3 + 2 + 1 + 2 + 2 + 1); //	:
        s_bc128Bcode_widths[59] = (3 + 1 + 2 + 2 + 1 + 2); //	;
        s_bc128Bcode_widths[60] = (3 + 2 + 2 + 1 + 1 + 2); //	<
        s_bc128Bcode_widths[61] = (3 + 2 + 2 + 2 + 1 + 1); //	=
        s_bc128Bcode_widths[62] = (2 + 1 + 2 + 1 + 2 + 3); //	>
        s_bc128Bcode_widths[63] = (2 + 1 + 2 + 3 + 2 + 1); //	?
        s_bc128Bcode_widths[64] = (2 + 3 + 2 + 1 + 2 + 1); //	@
        s_bc128Bcode_widths[65] = (1 + 1 + 1 + 3 + 2 + 3); //	A
        s_bc128Bcode_widths[66] = (1 + 3 + 1 + 1 + 2 + 3); //	B
        s_bc128Bcode_widths[67] = (1 + 3 + 1 + 3 + 2 + 1); //	C
        s_bc128Bcode_widths[68] = (1 + 1 + 2 + 3 + 1 + 3); //	D
        s_bc128Bcode_widths[69] = (1 + 3 + 2 + 1 + 1 + 3); //	E
        s_bc128Bcode_widths[70] = (1 + 3 + 2 + 3 + 1 + 1); //	F
        s_bc128Bcode_widths[71] = (2 + 1 + 1 + 3 + 1 + 3); //	G
        s_bc128Bcode_widths[72] = (2 + 3 + 1 + 1 + 1 + 3); //	H
        s_bc128Bcode_widths[73] = (2 + 3 + 1 + 3 + 1 + 1); //	I
        s_bc128Bcode_widths[74] = (1 + 1 + 2 + 1 + 3 + 3); //	J
        s_bc128Bcode_widths[75] = (1 + 1 + 2 + 3 + 3 + 1); //	K
        s_bc128Bcode_widths[76] = (1 + 3 + 2 + 1 + 3 + 1); //	L
        s_bc128Bcode_widths[77] = (1 + 1 + 3 + 1 + 2 + 3); //	M
        s_bc128Bcode_widths[78] = (1 + 1 + 3 + 3 + 2 + 1); //	N
        s_bc128Bcode_widths[79] = (1 + 3 + 3 + 1 + 2 + 1); //	O
        s_bc128Bcode_widths[80] = (3 + 1 + 3 + 1 + 2 + 1); //	P
        s_bc128Bcode_widths[81] = (2 + 1 + 1 + 3 + 3 + 1); //	Q
        s_bc128Bcode_widths[82] = (2 + 3 + 1 + 1 + 3 + 1); //	R
        s_bc128Bcode_widths[83] = (2 + 1 + 3 + 1 + 1 + 3); //	S
        s_bc128Bcode_widths[84] = (2 + 1 + 3 + 3 + 1 + 1); //	T
        s_bc128Bcode_widths[85] = (2 + 1 + 3 + 1 + 3 + 1); //	U
        s_bc128Bcode_widths[86] = (3 + 1 + 1 + 1 + 2 + 3); //	V
        s_bc128Bcode_widths[87] = (3 + 1 + 1 + 3 + 2 + 1); //	W
        s_bc128Bcode_widths[88] = (3 + 3 + 1 + 1 + 2 + 1); //	X
        s_bc128Bcode_widths[89] = (3 + 1 + 2 + 1 + 1 + 3); //	Y
        s_bc128Bcode_widths[90] = (3 + 1 + 2 + 3 + 1 + 1); //	Z
        s_bc128Bcode_widths[91] = (3 + 3 + 2 + 1 + 1 + 1); //	[
        s_bc128Bcode_widths[92] = (3 + 1 + 4 + 1 + 1 + 1); //	\

        s_bc128Bcode_widths[93] = (2 + 2 + 1 + 4 + 1 + 1); //	]
        s_bc128Bcode_widths[94] = (4 + 3 + 1 + 1 + 1 + 1); //	^
        s_bc128Bcode_widths[95] = (1 + 1 + 1 + 2 + 2 + 4); //	_
        s_bc128Bcode_widths[96] = (1 + 1 + 1 + 4 + 2 + 2); //	`
        s_bc128Bcode_widths[97] = (1 + 2 + 1 + 1 + 2 + 4); //	a
        s_bc128Bcode_widths[98] = (1 + 2 + 1 + 4 + 2 + 1); //	b
        s_bc128Bcode_widths[99] = (1 + 4 + 1 + 1 + 2 + 2); //	c
        s_bc128Bcode_widths[100] = (1 + 4 + 1 + 2 + 2 + 1); //	d
        s_bc128Bcode_widths[101] = (1 + 1 + 2 + 2 + 1 + 4); //	e
        s_bc128Bcode_widths[102] = (1 + 1 + 2 + 4 + 1 + 2); //	f
        s_bc128Bcode_widths[103] = (1 + 2 + 2 + 1 + 1 + 4); //	g
        s_bc128Bcode_widths[104] = (1 + 2 + 2 + 4 + 1 + 1); //	h
        s_bc128Bcode_widths[105] = (1 + 4 + 2 + 1 + 1 + 2); //	i
        s_bc128Bcode_widths[106] = (1 + 4 + 2 + 2 + 1 + 1); //	j
        s_bc128Bcode_widths[107] = (2 + 4 + 1 + 2 + 1 + 1); //	k
        s_bc128Bcode_widths[108] = (2 + 2 + 1 + 1 + 1 + 4); //	l
        s_bc128Bcode_widths[109] = (4 + 1 + 3 + 1 + 1 + 1); //	m
        s_bc128Bcode_widths[110] = (2 + 4 + 1 + 1 + 1 + 2); //	n
        s_bc128Bcode_widths[111] = (1 + 3 + 4 + 1 + 1 + 1); //	o
        s_bc128Bcode_widths[112] = (1 + 1 + 1 + 2 + 4 + 2); //	p
        s_bc128Bcode_widths[113] = (1 + 2 + 1 + 1 + 4 + 2); //	q
        s_bc128Bcode_widths[114] = (1 + 2 + 1 + 2 + 4 + 1); //	r
        s_bc128Bcode_widths[115] = (1 + 1 + 4 + 2 + 1 + 2); //	s
        s_bc128Bcode_widths[116] = (1 + 2 + 4 + 1 + 1 + 2); //	t
        s_bc128Bcode_widths[117] = (1 + 2 + 4 + 2 + 1 + 1); //	u
        s_bc128Bcode_widths[118] = (4 + 1 + 1 + 2 + 1 + 2); //	v
        s_bc128Bcode_widths[119] = (4 + 2 + 1 + 1 + 1 + 2); //	w
        s_bc128Bcode_widths[120] = (4 + 2 + 1 + 2 + 1 + 1); //	x
        s_bc128Bcode_widths[121] = (2 + 1 + 2 + 1 + 4 + 1); //	y
        s_bc128Bcode_widths[122] = (2 + 1 + 4 + 1 + 2 + 1); //	z
        s_bc128Bcode_widths[123] = (4 + 1 + 2 + 1 + 2 + 1); //	{
        s_bc128Bcode_widths[124] = (1 + 1 + 1 + 1 + 4 + 3); //	|
        s_bc128Bcode_widths[125] = (1 + 1 + 1 + 3 + 4 + 1); //	}
        s_bc128Bcode_widths[126] = (1 + 3 + 1 + 1 + 4 + 1); //	~
        s_bc128Bcode_widths[127] = (1 + 1 + 4 + 1 + 1 + 3); //	DEL

        s_bc128Bnum_codes = new int[107];
        s_bc128Bnum_codes[0] = BARCODE128(212222); //	space
        s_bc128Bnum_codes[1] = BARCODE128(222122); //	!
        s_bc128Bnum_codes[2] = BARCODE128(222221); //	"
        s_bc128Bnum_codes[3] = BARCODE128(121223); //	#
        s_bc128Bnum_codes[4] = BARCODE128(121322); //	$
        s_bc128Bnum_codes[5] = BARCODE128(131222); //	%
        s_bc128Bnum_codes[6] = BARCODE128(122213); //	&
        s_bc128Bnum_codes[7] = BARCODE128(122312); //	'
        s_bc128Bnum_codes[8] = BARCODE128(132212); //	(
        s_bc128Bnum_codes[9] = BARCODE128(221213); //	)
        s_bc128Bnum_codes[10] = BARCODE128(221312); //	*
        s_bc128Bnum_codes[11] = BARCODE128(231212); //	+
        s_bc128Bnum_codes[12] = BARCODE128(112232); //	,
        s_bc128Bnum_codes[13] = BARCODE128(122132); //	-
        s_bc128Bnum_codes[14] = BARCODE128(122231); //	.
        s_bc128Bnum_codes[15] = BARCODE128(113222); //	/
        s_bc128Bnum_codes[16] = BARCODE128(123122); //	0
        s_bc128Bnum_codes[17] = BARCODE128(123221); //	1
        s_bc128Bnum_codes[18] = BARCODE128(223211); //	2
        s_bc128Bnum_codes[19] = BARCODE128(221132); //	3
        s_bc128Bnum_codes[20] = BARCODE128(221231); //	4
        s_bc128Bnum_codes[21] = BARCODE128(213212); //	5
        s_bc128Bnum_codes[22] = BARCODE128(223112); //	6
        s_bc128Bnum_codes[23] = BARCODE128(312131); //	7
        s_bc128Bnum_codes[24] = BARCODE128(311222); //	8
        s_bc128Bnum_codes[25] = BARCODE128(321122); //	9
        s_bc128Bnum_codes[26] = BARCODE128(321221); //	:
        s_bc128Bnum_codes[27] = BARCODE128(312212); //	;
        s_bc128Bnum_codes[28] = BARCODE128(322112); //	<
        s_bc128Bnum_codes[29] = BARCODE128(322211); //	=
        s_bc128Bnum_codes[30] = BARCODE128(212123); //	>
        s_bc128Bnum_codes[31] = BARCODE128(212321); //	?
        s_bc128Bnum_codes[32] = BARCODE128(232121); //	@
        s_bc128Bnum_codes[33] = BARCODE128(111323); //	A
        s_bc128Bnum_codes[34] = BARCODE128(131123); //	B
        s_bc128Bnum_codes[35] = BARCODE128(131321); //	C
        s_bc128Bnum_codes[36] = BARCODE128(112313); //	D
        s_bc128Bnum_codes[37] = BARCODE128(132113); //	E
        s_bc128Bnum_codes[38] = BARCODE128(132311); //	F
        s_bc128Bnum_codes[39] = BARCODE128(211313); //	G
        s_bc128Bnum_codes[40] = BARCODE128(231113); //	H
        s_bc128Bnum_codes[41] = BARCODE128(231311); //	I
        s_bc128Bnum_codes[42] = BARCODE128(112133); //	J
        s_bc128Bnum_codes[43] = BARCODE128(112331); //	K
        s_bc128Bnum_codes[44] = BARCODE128(132131); //	L
        s_bc128Bnum_codes[45] = BARCODE128(113123); //	M
        s_bc128Bnum_codes[46] = BARCODE128(113321); //	N
        s_bc128Bnum_codes[47] = BARCODE128(133121); //	O
        s_bc128Bnum_codes[48] = BARCODE128(313121); //	P
        s_bc128Bnum_codes[49] = BARCODE128(211331); //	Q
        s_bc128Bnum_codes[50] = BARCODE128(231131); //	R
        s_bc128Bnum_codes[51] = BARCODE128(213113); //	S
        s_bc128Bnum_codes[52] = BARCODE128(213311); //	T
        s_bc128Bnum_codes[53] = BARCODE128(213131); //	U
        s_bc128Bnum_codes[54] = BARCODE128(311123); //	V
        s_bc128Bnum_codes[55] = BARCODE128(311321); //	W
        s_bc128Bnum_codes[56] = BARCODE128(331121); //	X
        s_bc128Bnum_codes[57] = BARCODE128(312113); //	Y
        s_bc128Bnum_codes[58] = BARCODE128(312311); //	Z
        s_bc128Bnum_codes[59] = BARCODE128(332111); //	[
        s_bc128Bnum_codes[60] = BARCODE128(314111); //	'\'

        s_bc128Bnum_codes[61] = BARCODE128(221411); //	]
        s_bc128Bnum_codes[62] = BARCODE128(431111); //	^
        s_bc128Bnum_codes[63] = BARCODE128(111224); //	_
        s_bc128Bnum_codes[64] = BARCODE128(111422); //	`
        s_bc128Bnum_codes[65] = BARCODE128(121124); //	a
        s_bc128Bnum_codes[66] = BARCODE128(121421); //	b
        s_bc128Bnum_codes[67] = BARCODE128(141122); //	c
        s_bc128Bnum_codes[68] = BARCODE128(141221); //	d
        s_bc128Bnum_codes[69] = BARCODE128(112214); //	e
        s_bc128Bnum_codes[70] = BARCODE128(112412); //	f
        s_bc128Bnum_codes[71] = BARCODE128(122114); //	g
        s_bc128Bnum_codes[72] = BARCODE128(122411); //	h
        s_bc128Bnum_codes[73] = BARCODE128(142112); //	i
        s_bc128Bnum_codes[74] = BARCODE128(142211); //	j
        s_bc128Bnum_codes[75] = BARCODE128(241211); //	k
        s_bc128Bnum_codes[76] = BARCODE128(221114); //	l
        s_bc128Bnum_codes[77] = BARCODE128(413111); //	m
        s_bc128Bnum_codes[78] = BARCODE128(241112); //	n
        s_bc128Bnum_codes[79] = BARCODE128(134111); //	o
        s_bc128Bnum_codes[80] = BARCODE128(111242); //	p
        s_bc128Bnum_codes[81] = BARCODE128(121142); //	q
        s_bc128Bnum_codes[82] = BARCODE128(121241); //	r
        s_bc128Bnum_codes[83] = BARCODE128(114212); //	s
        s_bc128Bnum_codes[84] = BARCODE128(124112); //	t
        s_bc128Bnum_codes[85] = BARCODE128(124211); //	u
        s_bc128Bnum_codes[86] = BARCODE128(411212); //	v
        s_bc128Bnum_codes[87] = BARCODE128(421112); //	w
        s_bc128Bnum_codes[88] = BARCODE128(421211); //	x
        s_bc128Bnum_codes[89] = BARCODE128(212141); //	y
        s_bc128Bnum_codes[90] = BARCODE128(214121); //	z
        s_bc128Bnum_codes[91] = BARCODE128(412121); //	{
        s_bc128Bnum_codes[92] = BARCODE128(111143); //	|
        s_bc128Bnum_codes[93] = BARCODE128(111341); //	}
        s_bc128Bnum_codes[94] = BARCODE128(131141); //	~
        s_bc128Bnum_codes[95] = BARCODE128(114113); //	DEL
        s_bc128Bnum_codes[96] = BARCODE128(114311); //	FNC 3
        s_bc128Bnum_codes[97] = BARCODE128(411113); //	FNC 2
        s_bc128Bnum_codes[98] = BARCODE128(411311); //	Shift A
        s_bc128Bnum_codes[99] = BARCODE128(113141); //	Code C
        s_bc128Bnum_codes[100] = BARCODE128(114131); //	FNC4
        s_bc128Bnum_codes[101] = BARCODE128(311141); //	Code A
        s_bc128Bnum_codes[102] = BARCODE128(411131); //	FNC 1
        s_bc128Bnum_codes[103] = BARCODE128(211412); //	Start Code A
        s_bc128Bnum_codes[104] = BARCODE128(211214); //	Start Code B
        s_bc128Bnum_codes[105] = BARCODE128(211232); //	Start Code C
        s_bc128Bnum_codes[106] = BARCODE128STOP();      // 2331112	Stop

        s_bc128Bnum_code_widths = new int[107];
        s_bc128Bnum_code_widths[0] = (2 + 1 + 2 + 2 + 2 + 2); //	space
        s_bc128Bnum_code_widths[1] = (2 + 2 + 2 + 1 + 2 + 2); //	!
        s_bc128Bnum_code_widths[2] = (2 + 2 + 2 + 2 + 2 + 1); //	"
        s_bc128Bnum_code_widths[3] = (1 + 2 + 1 + 2 + 2 + 3); //	#
        s_bc128Bnum_code_widths[4] = (1 + 2 + 1 + 3 + 2 + 2); //	$
        s_bc128Bnum_code_widths[5] = (1 + 3 + 1 + 2 + 2 + 2); //	%
        s_bc128Bnum_code_widths[6] = (1 + 2 + 2 + 2 + 1 + 3); //	&
        s_bc128Bnum_code_widths[7] = (1 + 2 + 2 + 3 + 1 + 2); //	'
        s_bc128Bnum_code_widths[8] = (1 + 3 + 2 + 2 + 1 + 2); //	(
        s_bc128Bnum_code_widths[9] = (2 + 2 + 1 + 2 + 1 + 3); //	)
        s_bc128Bnum_code_widths[10] = (2 + 2 + 1 + 3 + 1 + 2); //	*
        s_bc128Bnum_code_widths[11] = (2 + 3 + 1 + 2 + 1 + 2); //	+
        s_bc128Bnum_code_widths[12] = (1 + 1 + 2 + 2 + 3 + 2); //	,
        s_bc128Bnum_code_widths[13] = (1 + 2 + 2 + 1 + 3 + 2); //	-
        s_bc128Bnum_code_widths[14] = (1 + 2 + 2 + 2 + 3 + 1); //	.
        s_bc128Bnum_code_widths[15] = (1 + 1 + 3 + 2 + 2 + 2); //	/
        s_bc128Bnum_code_widths[16] = (1 + 2 + 3 + 1 + 2 + 2); //	0
        s_bc128Bnum_code_widths[17] = (1 + 2 + 3 + 2 + 2 + 1); //	1
        s_bc128Bnum_code_widths[18] = (2 + 2 + 3 + 2 + 1 + 1); //	2
        s_bc128Bnum_code_widths[19] = (2 + 2 + 1 + 1 + 3 + 2); //	3
        s_bc128Bnum_code_widths[20] = (2 + 2 + 1 + 2 + 3 + 1); //	4
        s_bc128Bnum_code_widths[21] = (2 + 1 + 3 + 2 + 1 + 2); //	5
        s_bc128Bnum_code_widths[22] = (2 + 2 + 3 + 1 + 1 + 2); //	6
        s_bc128Bnum_code_widths[23] = (3 + 1 + 2 + 1 + 3 + 1); //	7
        s_bc128Bnum_code_widths[24] = (3 + 1 + 1 + 2 + 2 + 2); //	8
        s_bc128Bnum_code_widths[25] = (3 + 2 + 1 + 1 + 2 + 2); //	9
        s_bc128Bnum_code_widths[26] = (3 + 2 + 1 + 2 + 2 + 1); //	:
        s_bc128Bnum_code_widths[27] = (3 + 1 + 2 + 2 + 1 + 2); //	;
        s_bc128Bnum_code_widths[28] = (3 + 2 + 2 + 1 + 1 + 2); //	<
        s_bc128Bnum_code_widths[29] = (3 + 2 + 2 + 2 + 1 + 1); //	=
        s_bc128Bnum_code_widths[30] = (2 + 1 + 2 + 1 + 2 + 3); //	>
        s_bc128Bnum_code_widths[31] = (2 + 1 + 2 + 3 + 2 + 1); //	?
        s_bc128Bnum_code_widths[32] = (2 + 3 + 2 + 1 + 2 + 1); //	@
        s_bc128Bnum_code_widths[33] = (1 + 1 + 1 + 3 + 2 + 3); //	A
        s_bc128Bnum_code_widths[34] = (1 + 3 + 1 + 1 + 2 + 3); //	B
        s_bc128Bnum_code_widths[35] = (1 + 3 + 1 + 3 + 2 + 1); //	C
        s_bc128Bnum_code_widths[36] = (1 + 1 + 2 + 3 + 1 + 3); //	D
        s_bc128Bnum_code_widths[37] = (1 + 3 + 2 + 1 + 1 + 3); //	E
        s_bc128Bnum_code_widths[38] = (1 + 3 + 2 + 3 + 1 + 1); //	F
        s_bc128Bnum_code_widths[39] = (2 + 1 + 1 + 3 + 1 + 3); //	G
        s_bc128Bnum_code_widths[40] = (2 + 3 + 1 + 1 + 1 + 3); //	H
        s_bc128Bnum_code_widths[41] = (2 + 3 + 1 + 3 + 1 + 1); //	I
        s_bc128Bnum_code_widths[42] = (1 + 1 + 2 + 1 + 3 + 3); //	J
        s_bc128Bnum_code_widths[43] = (1 + 1 + 2 + 3 + 3 + 1); //	K
        s_bc128Bnum_code_widths[44] = (1 + 3 + 2 + 1 + 3 + 1); //	L
        s_bc128Bnum_code_widths[45] = (1 + 1 + 3 + 1 + 2 + 3); //	M
        s_bc128Bnum_code_widths[46] = (1 + 1 + 3 + 3 + 2 + 1); //	N
        s_bc128Bnum_code_widths[47] = (1 + 3 + 3 + 1 + 2 + 1); //	O
        s_bc128Bnum_code_widths[48] = (3 + 1 + 3 + 1 + 2 + 1); //	P
        s_bc128Bnum_code_widths[49] = (2 + 1 + 1 + 3 + 3 + 1); //	Q
        s_bc128Bnum_code_widths[50] = (2 + 3 + 1 + 1 + 3 + 1); //	R
        s_bc128Bnum_code_widths[51] = (2 + 1 + 3 + 1 + 1 + 3); //	S
        s_bc128Bnum_code_widths[52] = (2 + 1 + 3 + 3 + 1 + 1); //	T
        s_bc128Bnum_code_widths[53] = (2 + 1 + 3 + 1 + 3 + 1); //	U
        s_bc128Bnum_code_widths[54] = (3 + 1 + 1 + 1 + 2 + 3); //	V
        s_bc128Bnum_code_widths[55] = (3 + 1 + 1 + 3 + 2 + 1); //	W
        s_bc128Bnum_code_widths[56] = (3 + 3 + 1 + 1 + 2 + 1); //	X
        s_bc128Bnum_code_widths[57] = (3 + 1 + 2 + 1 + 1 + 3); //	Y
        s_bc128Bnum_code_widths[58] = (3 + 1 + 2 + 3 + 1 + 1); //	Z
        s_bc128Bnum_code_widths[59] = (3 + 3 + 2 + 1 + 1 + 1); //	[
        s_bc128Bnum_code_widths[60] = (3 + 1 + 4 + 1 + 1 + 1); //	\

        s_bc128Bnum_code_widths[61] = (2 + 2 + 1 + 4 + 1 + 1); //	]
        s_bc128Bnum_code_widths[62] = (4 + 3 + 1 + 1 + 1 + 1); //	^
        s_bc128Bnum_code_widths[63] = (1 + 1 + 1 + 2 + 2 + 4); //	_
        s_bc128Bnum_code_widths[64] = (1 + 1 + 1 + 4 + 2 + 2); //	`
        s_bc128Bnum_code_widths[65] = (1 + 2 + 1 + 1 + 2 + 4); //	a
        s_bc128Bnum_code_widths[66] = (1 + 2 + 1 + 4 + 2 + 1); //	b
        s_bc128Bnum_code_widths[67] = (1 + 4 + 1 + 1 + 2 + 2); //	c
        s_bc128Bnum_code_widths[68] = (1 + 4 + 1 + 2 + 2 + 1); //	d
        s_bc128Bnum_code_widths[69] = (1 + 1 + 2 + 2 + 1 + 4); //	e
        s_bc128Bnum_code_widths[70] = (1 + 1 + 2 + 4 + 1 + 2); //	f
        s_bc128Bnum_code_widths[71] = (1 + 2 + 2 + 1 + 1 + 4); //	g
        s_bc128Bnum_code_widths[72] = (1 + 2 + 2 + 4 + 1 + 1); //	h
        s_bc128Bnum_code_widths[73] = (1 + 4 + 2 + 1 + 1 + 2); //	i
        s_bc128Bnum_code_widths[74] = (1 + 4 + 2 + 2 + 1 + 1); //	j
        s_bc128Bnum_code_widths[75] = (2 + 4 + 1 + 2 + 1 + 1); //	k
        s_bc128Bnum_code_widths[76] = (2 + 2 + 1 + 1 + 1 + 4); //	l
        s_bc128Bnum_code_widths[77] = (4 + 1 + 3 + 1 + 1 + 1); //	m
        s_bc128Bnum_code_widths[78] = (2 + 4 + 1 + 1 + 1 + 2); //	n
        s_bc128Bnum_code_widths[79] = (1 + 3 + 4 + 1 + 1 + 1); //	o
        s_bc128Bnum_code_widths[80] = (1 + 1 + 1 + 2 + 4 + 2); //	p
        s_bc128Bnum_code_widths[81] = (1 + 2 + 1 + 1 + 4 + 2); //	q
        s_bc128Bnum_code_widths[82] = (1 + 2 + 1 + 2 + 4 + 1); //	r
        s_bc128Bnum_code_widths[83] = (1 + 1 + 4 + 2 + 1 + 2); //	s
        s_bc128Bnum_code_widths[84] = (1 + 2 + 4 + 1 + 1 + 2); //	t
        s_bc128Bnum_code_widths[85] = (1 + 2 + 4 + 2 + 1 + 1); //	u
        s_bc128Bnum_code_widths[86] = (4 + 1 + 1 + 2 + 1 + 2); //	v
        s_bc128Bnum_code_widths[87] = (4 + 2 + 1 + 1 + 1 + 2); //	w
        s_bc128Bnum_code_widths[88] = (4 + 2 + 1 + 2 + 1 + 1); //	x
        s_bc128Bnum_code_widths[89] = (2 + 1 + 2 + 1 + 4 + 1); //	y
        s_bc128Bnum_code_widths[90] = (2 + 1 + 4 + 1 + 2 + 1); //	z
        s_bc128Bnum_code_widths[91] = (4 + 1 + 2 + 1 + 2 + 1); //	{
        s_bc128Bnum_code_widths[92] = (1 + 1 + 1 + 1 + 4 + 3); //	|
        s_bc128Bnum_code_widths[93] = (1 + 1 + 1 + 3 + 4 + 1); //	}
        s_bc128Bnum_code_widths[94] = (1 + 3 + 1 + 1 + 4 + 1); //	~
        s_bc128Bnum_code_widths[95] = (1 + 1 + 4 + 1 + 1 + 3); //	DEL
        s_bc128Bnum_code_widths[96] = (1 + 1 + 4 + 3 + 1 + 1); //	FNC 3
        s_bc128Bnum_code_widths[97] = (4 + 1 + 1 + 1 + 1 + 3); //	FNC 2
        s_bc128Bnum_code_widths[98] = (4 + 1 + 1 + 3 + 1 + 1); //	Shift A
        s_bc128Bnum_code_widths[99] = (1 + 1 + 3 + 1 + 4 + 1); //	Code C
        s_bc128Bnum_code_widths[100] = (1 + 1 + 4 + 1 + 3 + 1); //	FNC4
        s_bc128Bnum_code_widths[101] = (3 + 1 + 1 + 1 + 4 + 1); //	Code A
        s_bc128Bnum_code_widths[102] = (4 + 1 + 1 + 1 + 3 + 1); //	FNC 1
        s_bc128Bnum_code_widths[103] = (2 + 1 + 1 + 4 + 1 + 2); //	Start Code A
        s_bc128Bnum_code_widths[104] = (2 + 1 + 1 + 2 + 1 + 4); //	Start Code B
        s_bc128Bnum_code_widths[105] = (2 + 1 + 1 + 2 + 3 + 2); //	Start Code C
        s_bc128Bnum_code_widths[106] = BARCODE128STOPWIDTH();      // 2331112	Stop
    }

    public static int generateBarcode128B(int[] input, int[] output, int width) {
        int len = 1;
        int barcode;
        int ctrl_code = BARCODE128STARTBNO();
        int w;

        int outputPointer = 0;
        int inputPointer = 0;

        output[outputPointer] = BARCODE128STARTB();
        outputPointer++;
        w = BARCODE128STOPWIDTH();

        while (inputPointer < input.length) {
            if (input[inputPointer] > 127) {
                return 0;
            }
            barcode = s_bc128Bcodes[input[inputPointer]];
            w += s_bc128Bcode_widths[input[inputPointer]];
            if (barcode == 0) {
                return 0;
            }
            switch (input[inputPointer]) {
                case 1: // FUNC1
                    ctrl_code += len * 102;
                    break;
                case 2: // FUNC2
                    ctrl_code += len * 97;
                    break;
                case 3: // FUNC3
                    ctrl_code += len * 96;
                    break;
                case 4: // FUNC4
                    ctrl_code += len * 100;
                    break;
                default:
                    ctrl_code += len * (input[inputPointer] - 32);
                    break;
            }
            output[outputPointer] = barcode;
            outputPointer++;
            inputPointer++;
            len++;
        }
        ctrl_code %= 103;

        output[outputPointer] = s_bc128Bnum_codes[ctrl_code];
        w += s_bc128Bnum_code_widths[ctrl_code];
        outputPointer++;
        len++;
        output[outputPointer] = BARCODE128STOP();
        w += BARCODE128STOPWIDTH();
        len++;

        width = w;

        Barcode128.width = width;
        Barcode128.input = input;
        Barcode128.output = output;

        /*if (width > 0) {

        }*/
        return len;
    }

    public static int[] getInput() {
        return input;
    }

    public static int[] getOutput() {
        return output;
    }

    public static int getWidth() {
        return width;
    }
}
