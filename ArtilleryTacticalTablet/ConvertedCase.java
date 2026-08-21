package net.nazarick.artillerytablet.client.screen;

/**
 * The case, as rectangles taken off a reference picture.
 *
 * <p>Generated — do not hand-edit the run of {@code fill} calls unless you mean
 * to stop regenerating it. Produced from Screen.png
 * on a 360 by 203 logical grid, 28 colours,
 * 5190 rectangles.
 *
 * <p>Every coordinate is a share of the destination box rather than a pixel
 * count, so this draws at the same proportions in any window.
 */
public final class ConvertedCase {
    private static final int LOGICAL_W = 360;
    private static final int LOGICAL_H = 203;

    private ConvertedCase() {
    }

    public static void draw(Paint p, int left, int top, int width, int height) {
        // Shared edges, built once. Two rectangles that meet in the grid read the
        // same array element here, so they cannot part company on screen.
        final int[] cx = new int[LOGICAL_W + 1];
        for (int i = 0; i <= LOGICAL_W; i++) {
            cx[i] = left + (int) Math.floor(i * (double) width / LOGICAL_W);
        }
        final int[] cy = new int[LOGICAL_H + 1];
        for (int i = 0; i <= LOGICAL_H; i++) {
            cy[i] = top + (int) Math.floor(i * (double) height / LOGICAL_H);
        }

        p.batch(() -> {
            part0(p, cx, cy);
            part1(p, cx, cy);
            part2(p, cx, cy);
            part3(p, cx, cy);
            part4(p, cx, cy);
            part5(p, cx, cy);
            part6(p, cx, cy);
            part7(p, cx, cy);
            part8(p, cx, cy);
            part9(p, cx, cy);
            part10(p, cx, cy);
        });
    }

    private static void part0(Paint p, int[] cx, int[] cy) {

        p.fill(cx[51], cy[28], cx[91], cy[166], 0xFF202423);
        p.fill(cx[269], cy[28], cx[309], cy[166], 0xFF202423);
        p.fill(cx[92], cy[28], cx[117], cy[166], 0xFF202423);
        p.fill(cx[118], cy[28], cx[142], cy[166], 0xFF202423);
        p.fill(cx[143], cy[28], cx[167], cy[166], 0xFF202423);
        p.fill(cx[168], cy[28], cx[192], cy[166], 0xFF202423);
        p.fill(cx[193], cy[28], cx[217], cy[166], 0xFF202423);
        p.fill(cx[219], cy[28], cx[243], cy[166], 0xFF202423);
        p.fill(cx[244], cy[28], cx[268], cy[166], 0xFF202423);
        p.fill(cx[47], cy[28], cx[50], cy[166], 0xFF202423);
        p.fill(cx[313], cy[28], cx[316], cy[166], 0xFF202423);
        p.fill(cx[310], cy[29], cx[313], cy[166], 0xFF202423);

        p.fill(cx[343], cy[47], cx[346], cy[153], 0xFF2F3336);
        p.fill(cx[14], cy[48], cx[17], cy[153], 0xFF2F3336);

        p.fill(cx[217], cy[29], cx[219], cy[166], 0xFF202423);
        p.fill(cx[43], cy[30], cx[45], cy[166], 0xFF202423);

        p.fill(cx[354], cy[90], cx[358], cy[146], 0xFF565B54);

        p.fill(cx[81], cy[24], cx[279], cy[25], 0xFF202423);
        p.fill(cx[81], cy[173], cx[279], cy[174], 0xFF202423);
        p.fill(cx[50], cy[6], cx[51], cy[194], 0xFF202423);

        p.fill(cx[30], cy[12], cx[47], cy[23], 0xFF3C3F3B);

        p.fill(cx[309], cy[7], cx[310], cy[193], 0xFF202423);

        p.fill(cx[4], cy[42], cx[6], cy[135], 0xFF565B54);

        p.fill(cx[96], cy[193], cx[272], cy[194], 0xFF42473F);
        p.fill(cx[201], cy[195], cx[223], cy[203], 0xFF42473F);

        p.fill(cx[51], cy[169], cx[91], cy[173], 0xFF2F3336);

        p.fill(cx[124], cy[8], cx[280], cy[9], 0xFF3C3F3B);

        p.fill(cx[9], cy[54], cx[12], cy[106], 0xFF565B54);

        p.fill(cx[132], cy[2], cx[183], cy[5], 0xFF61625E);

        p.fill(cx[35], cy[84], cx[37], cy[159], 0xFF2F3336);

        p.fill(cx[132], cy[0], cx[206], cy[2], 0xFF61625E);

        p.fill(cx[279], cy[169], cx[308], cy[174], 0xFF2F3336);

        p.fill(cx[151], cy[5], cx[220], cy[7], 0xFF676763);

        p.fill(cx[45], cy[28], cx[46], cy[166], 0xFF202423);
        p.fill(cx[46], cy[29], cx[47], cy[166], 0xFF202423);
        p.fill(cx[91], cy[29], cx[92], cy[166], 0xFF202423);
        p.fill(cx[117], cy[29], cx[118], cy[166], 0xFF202423);
        p.fill(cx[142], cy[29], cx[143], cy[166], 0xFF202423);
        p.fill(cx[167], cy[29], cx[168], cy[166], 0xFF202423);
        p.fill(cx[192], cy[29], cx[193], cy[166], 0xFF202423);
        p.fill(cx[243], cy[29], cx[244], cy[166], 0xFF202423);
        p.fill(cx[268], cy[29], cx[269], cy[166], 0xFF202423);
        p.fill(cx[316], cy[30], cx[317], cy[166], 0xFF202423);

        p.fill(cx[352], cy[46], cx[359], cy[65], 0xFF61625E);

        p.fill(cx[94], cy[194], cx[225], cy[195], 0xFF42473F);

        p.fill(cx[222], cy[25], cx[282], cy[27], 0xFF2F3336);
        p.fill(cx[169], cy[168], cx[192], cy[173], 0xFF2F3336);

        p.fill(cx[2], cy[43], cx[3], cy[157], 0xFF565B54);

        p.fill(cx[241], cy[195], cx[255], cy[203], 0xFF42473F);

        p.fill(cx[1], cy[46], cx[2], cy[157], 0xFF565B54);
        p.fill(cx[0], cy[47], cx[1], cy[155], 0xFF565B54);

        p.fill(cx[324], cy[51], cx[325], cy[159], 0xFF2F3336);
        p.fill(cx[323], cy[52], cx[324], cy[159], 0xFF2F3336);

        p.fill(cx[133], cy[195], cx[148], cy[202], 0xFF42473F);

        p.fill(cx[3], cy[41], cx[4], cy[138], 0xFF565B54);

        p.fill(cx[45], cy[1], cx[77], cy[4], 0xFF51504E);

        p.fill(cx[341], cy[20], cx[347], cy[36], 0xFF3C3F3B);

        p.fill(cx[92], cy[169], cx[116], cy[173], 0xFF2F3336);

        p.fill(cx[248], cy[166], cx[294], cy[168], 0xFF42473F);

        p.fill(cx[13], cy[68], cx[14], cy[159], 0xFF2F3336);

        p.fill(cx[191], cy[9], cx[280], cy[10], 0xFF3C3F3B);

        p.fill(cx[15], cy[154], cx[32], cy[159], 0xFF2F3336);
        p.fill(cx[303], cy[176], cx[308], cy[193], 0xFF2F3336);

        p.fill(cx[21], cy[20], cx[26], cy[36], 0xFF3C3F3B);

        p.fill(cx[354], cy[72], cx[359], cy[88], 0xFF61625E);

        p.fill(cx[324], cy[190], cx[330], cy[203], 0xFF2F3336);

        p.fill(cx[40], cy[72], cx[41], cy[149], 0xFF3C3F3B);

        p.fill(cx[227], cy[195], cx[238], cy[202], 0xFF42473F);

        p.fill(cx[327], cy[154], cx[342], cy[159], 0xFF2F3336);
        p.fill(cx[194], cy[168], cx[209], cy[173], 0xFF2F3336);
        p.fill(cx[100], cy[177], cx[105], cy[192], 0xFF2F3336);

        p.fill(cx[336], cy[160], cx[340], cy[178], 0xFF3C3F3B);

        p.fill(cx[31], cy[161], cx[39], cy[170], 0xFF2F3336);
        p.fill(cx[165], cy[174], cx[201], cy[176], 0xFF2F3336);

        p.fill(cx[216], cy[0], cx[286], cy[1], 0xFF61625E);

        p.fill(cx[16], cy[22], cx[21], cy[36], 0xFF3C3F3B);

        p.fill(cx[51], cy[180], cx[56], cy[194], 0xFF2F3336);

        p.fill(cx[217], cy[1], cx[286], cy[2], 0xFF61625E);

        p.fill(cx[244], cy[169], cx[261], cy[173], 0xFF2F3336);
        p.fill(cx[281], cy[176], cx[285], cy[193], 0xFF2F3336);

        p.fill(cx[88], cy[1], cx[110], cy[4], 0xFF565B54);

        p.fill(cx[184], cy[2], cx[206], cy[5], 0xFF61625E);

        p.fill(cx[40], cy[38], cx[42], cy[71], 0xFF3C3F3B);
        p.fill(cx[125], cy[9], cx[189], cy[10], 0xFF3C3F3B);

        p.fill(cx[319], cy[53], cx[323], cy[69], 0xFF2F3336);
        p.fill(cx[319], cy[143], cx[323], cy[159], 0xFF2F3336);

        p.fill(cx[151], cy[195], cx[159], cy[203], 0xFF42473F);
        p.fill(cx[180], cy[195], cx[188], cy[203], 0xFF42473F);

        p.fill(cx[248], cy[5], cx[279], cy[7], 0xFF676763);

        p.fill(cx[30], cy[8], cx[50], cy[11], 0xFF3C3F3B);
        p.fill(cx[310], cy[8], cx[313], cy[28], 0xFF3C3F3B);
        p.fill(cx[75], cy[11], cx[79], cy[26], 0xFF3C3F3B);

        p.fill(cx[349], cy[110], cx[351], cy[140], 0xFF565B54);

        p.fill(cx[84], cy[174], cx[113], cy[176], 0xFF2F3336);

        p.fill(cx[333], cy[22], cx[336], cy[41], 0xFF3C3F3B);

        p.fill(cx[327], cy[43], cx[346], cy[46], 0xFF2F3336);

        p.fill(cx[356], cy[32], cx[360], cy[46], 0xFF565B54);

        p.fill(cx[327], cy[149], cx[341], cy[153], 0xFF2F3336);

        p.fill(cx[15], cy[164], cx[19], cy[178], 0xFF3C3F3B);

        p.fill(cx[75], cy[179], cx[79], cy[193], 0xFF2F3336);

        p.fill(cx[20], cy[0], cx[74], cy[1], 0xFF51504E);

        p.fill(cx[13], cy[23], cx[16], cy[41], 0xFF3C3F3B);

        p.fill(cx[81], cy[175], cx[84], cy[193], 0xFF2F3336);

        p.fill(cx[325], cy[96], cx[326], cy[149], 0xFF202423);

        p.fill(cx[320], cy[165], cx[333], cy[169], 0xFF2F3336);

        p.fill(cx[287], cy[177], cx[300], cy[181], 0xFFD43835);

        p.fill(cx[325], cy[44], cx[326], cy[95], 0xFF202423);

        p.fill(cx[353], cy[89], cx[354], cy[140], 0xFF565B54);

        p.fill(cx[37], cy[125], cx[40], cy[142], 0xFF2F3336);
        p.fill(cx[320], cy[125], cx[323], cy[142], 0xFF2F3336);

        p.fill(cx[194], cy[166], cx[219], cy[168], 0xFF42473F);
        p.fill(cx[188], cy[196], cx[195], cy[203], 0xFF42473F);

        p.fill(cx[20], cy[1], cx[44], cy[3], 0xFF51504E);

        p.fill(cx[283], cy[25], cx[299], cy[28], 0xFF2F3336);
        p.fill(cx[320], cy[89], cx[323], cy[105], 0xFF2F3336);
        p.fill(cx[37], cy[143], cx[40], cy[159], 0xFF2F3336);
        p.fill(cx[18], cy[149], cx[34], cy[152], 0xFF2F3336);
        p.fill(cx[106], cy[177], cx[109], cy[193], 0xFF2F3336);
        p.fill(cx[176], cy[177], cx[179], cy[193], 0xFF2F3336);
        p.fill(cx[207], cy[177], cx[210], cy[193], 0xFF2F3336);
        p.fill(cx[260], cy[189], cx[272], cy[193], 0xFF2F3336);

        p.fill(cx[226], cy[194], cx[273], cy[195], 0xFF42473F);

        p.fill(cx[317], cy[90], cx[318], cy[136], 0xFF2F3336);

        p.fill(cx[303], cy[11], cx[306], cy[26], 0xFF3C3F3B);

        p.fill(cx[321], cy[160], cx[336], cy[163], 0xFF2F3336);
        p.fill(cx[131], cy[177], cx[134], cy[192], 0xFF2F3336);
        p.fill(cx[156], cy[178], cx[159], cy[193], 0xFF2F3336);

        p.fill(cx[336], cy[178], cx[351], cy[181], 0xFF202423);

        p.fill(cx[348], cy[107], cx[349], cy[151], 0xFF565B54);

        p.fill(cx[118], cy[169], cx[129], cy[173], 0xFF2F3336);
        p.fill(cx[290], cy[189], cx[301], cy[193], 0xFF2F3336);

        p.fill(cx[0], cy[0], cx[7], cy[6], 0xFF676763);

        p.fill(cx[286], cy[0], cx[307], cy[2], 0xFF565B54);
        p.fill(cx[287], cy[2], cx[308], cy[4], 0xFF565B54);
        p.fill(cx[12], cy[63], cx[13], cy[105], 0xFF565B54);

        p.fill(cx[353], cy[65], cx[359], cy[72], 0xFF61625E);

        p.fill(cx[43], cy[169], cx[50], cy[175], 0xFF2F3336);

        p.fill(cx[328], cy[171], cx[334], cy[178], 0xFF3C3F3B);

        p.fill(cx[254], cy[174], cx[275], cy[176], 0xFF2F3336);
        p.fill(cx[45], cy[182], cx[47], cy[203], 0xFF2F3336);

        p.fill(cx[111], cy[0], cx[131], cy[2], 0xFF61625E);

        p.fill(cx[221], cy[5], cx[241], cy[7], 0xFF676763);

        p.fill(cx[6], cy[61], cx[7], cy[101], 0xFF565B54);
        p.fill(cx[358], cy[91], cx[360], cy[111], 0xFF565B54);
        p.fill(cx[347], cy[108], cx[348], cy[148], 0xFF565B54);

        p.fill(cx[294], cy[195], cx[304], cy[199], 0xFF3C3F3B);

        p.fill(cx[97], cy[196], cx[105], cy[201], 0xFF42473F);

        p.fill(cx[248], cy[2], cx[287], cy[3], 0xFF61625E);

        p.fill(cx[106], cy[11], cx[109], cy[24], 0xFF3C3F3B);

        p.fill(cx[107], cy[25], cx[120], cy[28], 0xFF2F3336);
        p.fill(cx[146], cy[25], cx[159], cy[28], 0xFF2F3336);
        p.fill(cx[251], cy[180], cx[254], cy[193], 0xFF2F3336);

        p.fill(cx[93], cy[166], cx[131], cy[167], 0xFF42473F);

        p.fill(cx[20], cy[47], cx[29], cy[51], 0xFF51504E);

        p.fill(cx[347], cy[65], cx[348], cy[101], 0xFF61625E);

        p.fill(cx[39], cy[88], cx[40], cy[124], 0xFF2F3336);

        p.fill(cx[20], cy[101], cx[29], cy[105], 0xFF51504E);

        p.fill(cx[8], cy[160], cx[12], cy[169], 0xFF61625E);

        p.fill(cx[150], cy[166], cx[168], cy[168], 0xFF42473F);
        p.fill(cx[220], cy[166], cx[238], cy[168], 0xFF42473F);

        p.fill(cx[227], cy[175], cx[229], cy[193], 0xFF2F3336);

        p.fill(cx[57], cy[193], cx[75], cy[195], 0xFF3C3F3B);
        p.fill(cx[15], cy[194], cx[19], cy[203], 0xFF3C3F3B);

        p.fill(cx[241], cy[2], cx[248], cy[7], 0xFF676763);

        p.fill(cx[348], cy[66], cx[349], cy[101], 0xFF61625E);

        p.fill(cx[157], cy[168], cx[164], cy[173], 0xFF2F3336);
        p.fill(cx[310], cy[169], cx[317], cy[174], 0xFF2F3336);

        p.fill(cx[70], cy[195], cx[75], cy[202], 0xFF42473F);
        p.fill(cx[166], cy[196], cx[171], cy[203], 0xFF42473F);
        p.fill(cx[175], cy[196], cx[180], cy[203], 0xFF42473F);

        p.fill(cx[26], cy[19], cx[28], cy[36], 0xFF3C3F3B);

        p.fill(cx[162], cy[25], cx[179], cy[27], 0xFF2F3336);
        p.fill(cx[192], cy[25], cx[209], cy[27], 0xFF2F3336);
        p.fill(cx[317], cy[70], cx[319], cy[87], 0xFF2F3336);
        p.fill(cx[319], cy[71], cx[321], cy[88], 0xFF2F3336);
        p.fill(cx[37], cy[89], cx[39], cy[106], 0xFF2F3336);
        p.fill(cx[37], cy[107], cx[39], cy[124], 0xFF2F3336);
        p.fill(cx[318], cy[107], cx[320], cy[124], 0xFF2F3336);

        p.fill(cx[317], cy[159], cx[351], cy[160], 0xFF202423);

        p.fill(cx[20], cy[83], cx[31], cy[86], 0xFF51504E);

        p.fill(cx[358], cy[112], cx[359], cy[145], 0xFF565B54);
        p.fill(cx[359], cy[114], cx[360], cy[147], 0xFF565B54);

        p.fill(cx[249], cy[3], cx[281], cy[4], 0xFF61625E);

        p.fill(cx[93], cy[5], cx[109], cy[7], 0xFF565B54);

        p.fill(cx[319], cy[40], cx[323], cy[48], 0xFF2F3336);

        p.fill(cx[359], cy[47], cx[360], cy[79], 0xFF61625E);

        p.fill(cx[327], cy[62], cx[343], cy[64], 0xFF2F3336);

        p.fill(cx[41], cy[71], cx[42], cy[103], 0xFF3C3F3B);

        p.fill(cx[352], cy[92], cx[353], cy[124], 0xFF565B54);

        p.fill(cx[9], cy[118], cx[10], cy[150], 0xFF51504E);

        p.fill(cx[286], cy[175], cx[302], cy[177], 0xFF202423);

        p.fill(cx[278], cy[176], cx[280], cy[192], 0xFF2F3336);
        p.fill(cx[276], cy[177], cx[278], cy[193], 0xFF2F3336);

        p.fill(cx[81], cy[193], cx[89], cy[197], 0xFF3C3F3B);

        p.fill(cx[171], cy[195], cx[175], cy[203], 0xFF42473F);
        p.fill(cx[195], cy[195], cx[199], cy[203], 0xFF42473F);

        p.fill(cx[296], cy[199], cx[304], cy[203], 0xFF3C3F3B);

        p.fill(cx[248], cy[4], cx[278], cy[5], 0xFF61625E);

        p.fill(cx[11], cy[9], cx[14], cy[19], 0xFF3C3F3B);
        p.fill(cx[47], cy[13], cx[50], cy[23], 0xFF3C3F3B);

        p.fill(cx[6], cy[46], cx[8], cy[61], 0xFF565B54);

        p.fill(cx[11], cy[121], cx[12], cy[151], 0xFF51504E);

        p.fill(cx[4], cy[140], cx[6], cy[155], 0xFF565B54);

        p.fill(cx[52], cy[166], cx[82], cy[167], 0xFF42473F);

        p.fill(cx[51], cy[173], cx[81], cy[174], 0xFF2F3336);

        p.fill(cx[58], cy[175], cx[73], cy[177], 0xFF202423);
        p.fill(cx[9], cy[178], cx[24], cy[180], 0xFF202423);

        p.fill(cx[232], cy[178], cx[234], cy[193], 0xFF2F3336);
        p.fill(cx[257], cy[178], cx[259], cy[193], 0xFF2F3336);
        p.fill(cx[345], cy[182], cx[348], cy[192], 0xFF2F3336);
        p.fill(cx[313], cy[192], cx[323], cy[195], 0xFF2F3336);

        p.fill(cx[289], cy[193], cx[304], cy[195], 0xFF3C3F3B);
        p.fill(cx[344], cy[193], cx[347], cy[203], 0xFF3C3F3B);

        p.fill(cx[349], cy[72], cx[350], cy[101], 0xFF61625E);

        p.fill(cx[308], cy[166], cx[309], cy[195], 0xFF202423);

        p.fill(cx[51], cy[174], cx[80], cy[175], 0xFF2F3336);

        p.fill(cx[287], cy[9], cx[301], cy[11], 0xFF202423);

        p.fill(cx[344], cy[12], cx[348], cy[19], 0xFF3C3F3B);

        p.fill(cx[20], cy[119], cx[27], cy[123], 0xFF51504E);
        p.fill(cx[21], cy[137], cx[28], cy[141], 0xFF51504E);
        p.fill(cx[358], cy[147], cx[360], cy[161], 0xFF51504E);

        p.fill(cx[15], cy[160], cx[22], cy[164], 0xFF3C3F3B);

        p.fill(cx[171], cy[166], cx[185], cy[168], 0xFF42473F);

        p.fill(cx[143], cy[169], cx[150], cy[173], 0xFF2F3336);
        p.fill(cx[280], cy[174], cx[308], cy[175], 0xFF2F3336);

        p.fill(cx[37], cy[176], cx[39], cy[190], 0xFF3C3F3B);
        p.fill(cx[35], cy[177], cx[37], cy[191], 0xFF3C3F3B);

        p.fill(cx[126], cy[179], cx[128], cy[193], 0xFF2F3336);

        p.fill(cx[296], cy[181], cx[300], cy[188], 0xFFD43835);

        p.fill(cx[17], cy[187], cx[21], cy[194], 0xFF2F3336);
        p.fill(cx[339], cy[187], cx[343], cy[194], 0xFF2F3336);
        p.fill(cx[235], cy[189], cx[242], cy[193], 0xFF2F3336);

        p.fill(cx[159], cy[196], cx[163], cy[203], 0xFF42473F);
        p.fill(cx[223], cy[196], cx[227], cy[203], 0xFF42473F);

        p.fill(cx[304], cy[196], cx[308], cy[203], 0xFF3C3F3B);

        p.fill(cx[122], cy[25], cx[131], cy[28], 0xFF2F3336);

        p.fill(cx[330], cy[66], cx[339], cy[69], 0xFF51504E);
        p.fill(cx[10], cy[123], cx[11], cy[150], 0xFF51504E);

        p.fill(cx[333], cy[179], cx[336], cy[188], 0xFF202423);

        p.fill(cx[341], cy[194], cx[344], cy[203], 0xFF3C3F3B);

        p.fill(cx[331], cy[1], cx[344], cy[3], 0xFF565B54);

        p.fill(cx[221], cy[3], cx[234], cy[5], 0xFF61625E);

        p.fill(cx[29], cy[9], cx[30], cy[35], 0xFF3C3F3B);

        p.fill(cx[18], cy[60], cx[31], cy[62], 0xFF202423);
        p.fill(cx[329], cy[60], cx[342], cy[62], 0xFF202423);
        p.fill(cx[18], cy[78], cx[31], cy[80], 0xFF202423);

        p.fill(cx[182], cy[180], cx[184], cy[193], 0xFF2F3336);

        p.fill(cx[341], cy[36], cx[346], cy[41], 0xFF3C3F3B);

        p.fill(cx[36], cy[55], cx[37], cy[80], 0xFF2F3336);

        p.fill(cx[107], cy[167], cx[132], cy[168], 0xFF42473F);

        p.fill(cx[356], cy[9], cx[360], cy[15], 0xFF8A8C83);

        p.fill(cx[101], cy[12], cx[103], cy[24], 0xFF3C3F3B);
        p.fill(cx[132], cy[12], cx[134], cy[24], 0xFF3C3F3B);

        p.fill(cx[187], cy[12], cx[199], cy[14], 0xFF42473F);

        p.fill(cx[257], cy[12], cx[259], cy[24], 0xFF3C3F3B);
        p.fill(cx[277], cy[12], cx[280], cy[20], 0xFF3C3F3B);
        p.fill(cx[320], cy[18], cx[323], cy[26], 0xFF3C3F3B);

        p.fill(cx[2], cy[22], cx[6], cy[28], 0xFF676763);
        p.fill(cx[356], cy[23], cx[360], cy[29], 0xFF676763);

        p.fill(cx[88], cy[25], cx[100], cy[27], 0xFF2F3336);
        p.fill(cx[244], cy[27], cx[268], cy[28], 0xFF2F3336);

        p.fill(cx[327], cy[46], cx[329], cy[58], 0xFF202423);

        p.fill(cx[337], cy[50], cx[340], cy[58], 0xFF51504E);

        p.fill(cx[17], cy[62], cx[29], cy[64], 0xFF2F3336);

        p.fill(cx[326], cy[65], cx[328], cy[77], 0xFF202423);
        p.fill(cx[31], cy[137], cx[33], cy[149], 0xFF202423);
        p.fill(cx[326], cy[137], cx[328], cy[149], 0xFF202423);

        p.fill(cx[136], cy[169], cx[142], cy[173], 0xFF2F3336);
        p.fill(cx[51], cy[176], cx[57], cy[180], 0xFF2F3336);

        p.fill(cx[154], cy[176], cx[156], cy[188], 0xFF202423);

        p.fill(cx[310], cy[176], cx[316], cy[180], 0xFF2F3336);

        p.fill(cx[39], cy[177], cx[41], cy[189], 0xFF3C3F3B);

        p.fill(cx[348], cy[181], cx[351], cy[189], 0xFF2F3336);

        p.fill(cx[287], cy[182], cx[291], cy[188], 0xFFD43835);

        p.fill(cx[336], cy[183], cx[344], cy[186], 0xFF202423);

        p.fill(cx[89], cy[189], cx[95], cy[193], 0xFF2F3336);
        p.fill(cx[141], cy[189], cx[147], cy[193], 0xFF2F3336);
        p.fill(cx[160], cy[189], cx[166], cy[193], 0xFF2F3336);
        p.fill(cx[34], cy[191], cx[36], cy[203], 0xFF2F3336);
        p.fill(cx[39], cy[191], cx[41], cy[203], 0xFF2F3336);
        p.fill(cx[31], cy[195], cx[34], cy[203], 0xFF2F3336);

        p.fill(cx[163], cy[195], cx[166], cy[203], 0xFF42473F);
        p.fill(cx[262], cy[195], cx[265], cy[203], 0xFF42473F);

        p.fill(cx[338], cy[195], cx[341], cy[203], 0xFF3C3F3B);
        p.fill(cx[58], cy[199], cx[64], cy[203], 0xFF3C3F3B);

        p.fill(cx[57], cy[4], cx[80], cy[5], 0xFF676763);

        p.fill(cx[89], cy[4], cx[112], cy[5], 0xFF565B54);

        p.fill(cx[350], cy[44], cx[351], cy[67], 0xFF676763);

        p.fill(cx[12], cy[112], cx[13], cy[135], 0xFF51504E);

        p.fill(cx[318], cy[136], cx[319], cy[159], 0xFF2F3336);

        p.fill(cx[22], cy[3], cx[44], cy[4], 0xFF51504E);

        p.fill(cx[203], cy[11], cx[205], cy[22], 0xFF2F3336);

        p.fill(cx[347], cy[43], cx[348], cy[65], 0xFF676763);
        p.fill(cx[348], cy[44], cx[349], cy[66], 0xFF676763);
        p.fill(cx[349], cy[45], cx[350], cy[67], 0xFF676763);

        p.fill(cx[32], cy[47], cx[34], cy[58], 0xFF202423);

        p.fill(cx[34], cy[67], cx[35], cy[89], 0xFF42473F);

        p.fill(cx[31], cy[83], cx[33], cy[94], 0xFF202423);
        p.fill(cx[326], cy[83], cx[328], cy[94], 0xFF202423);
        p.fill(cx[326], cy[101], cx[328], cy[112], 0xFF202423);
        p.fill(cx[326], cy[119], cx[328], cy[130], 0xFF202423);

        p.fill(cx[153], cy[174], cx[164], cy[176], 0xFF2F3336);

        p.fill(cx[26], cy[180], cx[28], cy[191], 0xFF202423);

        p.fill(cx[90], cy[0], cx[111], cy[1], 0xFF565B54);

        p.fill(cx[318], cy[0], cx[321], cy[7], 0xFF676763);

        p.fill(cx[113], cy[18], cx[120], cy[21], 0xFF42473F);

        p.fill(cx[222], cy[27], cx[243], cy[28], 0xFF2F3336);

        p.fill(cx[333], cy[47], cx[340], cy[50], 0xFF51504E);

        p.fill(cx[6], cy[108], cx[7], cy[129], 0xFF565B54);

        p.fill(cx[0], cy[159], cx[3], cy[166], 0xFF51504E);

        p.fill(cx[48], cy[182], cx[49], cy[203], 0xFF2F3336);
        p.fill(cx[310], cy[182], cx[311], cy[203], 0xFF2F3336);

        p.fill(cx[112], cy[196], cx[115], cy[203], 0xFF42473F);
        p.fill(cx[120], cy[196], cx[123], cy[203], 0xFF42473F);
        p.fill(cx[255], cy[196], cx[258], cy[203], 0xFF42473F);

        p.fill(cx[321], cy[0], cx[331], cy[2], 0xFF61625E);

        p.fill(cx[88], cy[8], cx[108], cy[9], 0xFF3C3F3B);

        p.fill(cx[17], cy[11], cx[21], cy[16], 0xFF202423);

        p.fill(cx[236], cy[14], cx[240], cy[19], 0xFF42473F);

        p.fill(cx[51], cy[21], cx[55], cy[26], 0xFF3C3F3B);
        p.fill(cx[340], cy[21], cx[341], cy[41], 0xFF3C3F3B);
        p.fill(cx[329], cy[24], cx[331], cy[34], 0xFF3C3F3B);

        p.fill(cx[51], cy[26], cx[61], cy[28], 0xFF2F3336);
        p.fill(cx[131], cy[26], cx[141], cy[28], 0xFF2F3336);
        p.fill(cx[299], cy[26], cx[309], cy[28], 0xFF2F3336);

        p.fill(cx[35], cy[29], cx[37], cy[39], 0xFF3C3F3B);

        p.fill(cx[352], cy[30], cx[354], cy[40], 0xFF42473F);

        p.fill(cx[0], cy[32], cx[4], cy[37], 0xFF565B54);

        p.fill(cx[327], cy[41], cx[347], cy[42], 0xFF202423);
        p.fill(cx[31], cy[65], cx[33], cy[75], 0xFF202423);
        p.fill(cx[331], cy[78], cx[341], cy[80], 0xFF202423);

        p.fill(cx[22], cy[107], cx[27], cy[111], 0xFF51504E);

        p.fill(cx[150], cy[168], cx[154], cy[173], 0xFF2F3336);
        p.fill(cx[209], cy[169], cx[214], cy[173], 0xFF2F3336);
        p.fill(cx[229], cy[169], cx[234], cy[173], 0xFF2F3336);
        p.fill(cx[14], cy[183], cx[16], cy[193], 0xFF2F3336);
        p.fill(cx[135], cy[189], cx[140], cy[193], 0xFF2F3336);
        p.fill(cx[193], cy[189], cx[198], cy[193], 0xFF2F3336);
        p.fill(cx[221], cy[189], cx[226], cy[193], 0xFF2F3336);
        p.fill(cx[245], cy[189], cx[250], cy[193], 0xFF2F3336);

        p.fill(cx[10], cy[191], cx[12], cy[201], 0xFF42473F);

        p.fill(cx[13], cy[193], cx[15], cy[203], 0xFF3C3F3B);

        p.fill(cx[132], cy[5], cx[151], cy[6], 0xFF61625E);

        p.fill(cx[311], cy[7], cx[330], cy[8], 0xFF51504E);

        p.fill(cx[28], cy[18], cx[29], cy[37], 0xFF3C3F3B);

        p.fill(cx[13], cy[41], cx[32], cy[42], 0xFF202423);

        p.fill(cx[320], cy[105], cx[321], cy[124], 0xFF2F3336);

        p.fill(cx[6], cy[150], cx[7], cy[169], 0xFF42473F);

        p.fill(cx[47], cy[184], cx[48], cy[203], 0xFF2F3336);

        p.fill(cx[307], cy[0], cx[316], cy[2], 0xFF61625E);

        p.fill(cx[349], cy[0], cx[358], cy[2], 0xFF676763);
        p.fill(cx[280], cy[4], cx[298], cy[5], 0xFF676763);

        p.fill(cx[30], cy[11], cx[48], cy[12], 0xFF3C3F3B);
        p.fill(cx[157], cy[13], cx[159], cy[22], 0xFF3C3F3B);

        p.fill(cx[261], cy[14], cx[264], cy[20], 0xFF42473F);
        p.fill(cx[187], cy[15], cx[190], cy[21], 0xFF42473F);
        p.fill(cx[196], cy[15], cx[199], cy[21], 0xFF42473F);

        p.fill(cx[331], cy[22], cx[332], cy[40], 0xFF3C3F3B);

        p.fill(cx[6], cy[31], cx[8], cy[40], 0xFF42473F);

        p.fill(cx[323], cy[42], cx[325], cy[51], 0xFF2F3336);
        p.fill(cx[27], cy[43], cx[33], cy[46], 0xFF2F3336);

        p.fill(cx[26], cy[51], cx[29], cy[57], 0xFF51504E);
        p.fill(cx[346], cy[63], cx[347], cy[81], 0xFF51504E);
        p.fill(cx[21], cy[66], cx[23], cy[75], 0xFF51504E);
        p.fill(cx[26], cy[87], cx[29], cy[93], 0xFF51504E);

        p.fill(cx[319], cy[88], cx[320], cy[106], 0xFF2F3336);

        p.fill(cx[338], cy[102], cx[340], cy[111], 0xFF51504E);

        p.fill(cx[321], cy[106], cx[322], cy[124], 0xFF2F3336);
        p.fill(cx[319], cy[124], cx[320], cy[142], 0xFF2F3336);

        p.fill(cx[19], cy[138], cx[21], cy[147], 0xFF51504E);

        p.fill(cx[3], cy[139], cx[4], cy[157], 0xFF565B54);

        p.fill(cx[6], cy[141], cx[8], cy[150], 0xFF42473F);

        p.fill(cx[11], cy[159], cx[29], cy[160], 0xFF202423);

        p.fill(cx[33], cy[170], cx[39], cy[173], 0xFF2F3336);
        p.fill(cx[128], cy[174], cx[137], cy[176], 0xFF2F3336);
        p.fill(cx[203], cy[174], cx[212], cy[176], 0xFF2F3336);
        p.fill(cx[152], cy[175], cx[153], cy[193], 0xFF2F3336);
        p.fill(cx[49], cy[180], cx[50], cy[198], 0xFF2F3336);

        p.fill(cx[120], cy[181], cx[123], cy[187], 0xFF42473F);

        p.fill(cx[321], cy[181], cx[330], cy[183], 0xFF3C3F3B);

        p.fill(cx[311], cy[183], cx[312], cy[201], 0xFF2F3336);

        p.fill(cx[324], cy[184], cx[330], cy[187], 0xFF3C3F3B);

        p.fill(cx[312], cy[185], cx[313], cy[203], 0xFF2F3336);

        p.fill(cx[348], cy[191], cx[350], cy[200], 0xFF42473F);

        p.fill(cx[332], cy[194], cx[334], cy[203], 0xFF2F3336);

        p.fill(cx[51], cy[196], cx[54], cy[202], 0xFF3C3F3B);

        p.fill(cx[148], cy[196], cx[151], cy[202], 0xFF42473F);
        p.fill(cx[238], cy[196], cx[241], cy[202], 0xFF42473F);
        p.fill(cx[115], cy[197], cx[118], cy[203], 0xFF42473F);
        p.fill(cx[258], cy[197], cx[261], cy[203], 0xFF42473F);
        p.fill(cx[351], cy[197], cx[354], cy[203], 0xFF42473F);

        p.fill(cx[281], cy[5], cx[298], cy[6], 0xFF61625E);

        p.fill(cx[79], cy[6], cx[80], cy[23], 0xFF202423);
        p.fill(cx[280], cy[6], cx[281], cy[23], 0xFF202423);

        p.fill(cx[337], cy[24], cx[338], cy[41], 0xFF3C3F3B);

        p.fill(cx[7], cy[63], cx[8], cy[80], 0xFF565B54);

        p.fill(cx[322], cy[71], cx[323], cy[88], 0xFF2F3336);

        p.fill(cx[42], cy[89], cx[43], cy[106], 0xFF3C3F3B);

        p.fill(cx[322], cy[107], cx[323], cy[124], 0xFF2F3336);

        p.fill(cx[41], cy[124], cx[42], cy[141], 0xFF3C3F3B);

        p.fill(cx[42], cy[140], cx[43], cy[157], 0xFF2F3336);

        p.fill(cx[15], cy[153], cx[32], cy[154], 0xFF202423);

        p.fill(cx[14], cy[161], cx[15], cy[178], 0xFF3C3F3B);
        p.fill(cx[22], cy[161], cx[23], cy[178], 0xFF3C3F3B);

        p.fill(cx[128], cy[176], cx[129], cy[193], 0xFF2F3336);
        p.fill(cx[153], cy[176], cx[154], cy[193], 0xFF2F3336);
        p.fill(cx[229], cy[176], cx[230], cy[193], 0xFF2F3336);

        p.fill(cx[280], cy[176], cx[281], cy[193], 0xFF202423);
        p.fill(cx[79], cy[177], cx[80], cy[194], 0xFF202423);

        p.fill(cx[80], cy[177], cx[81], cy[194], 0xFF2F3336);

        p.fill(cx[345], cy[0], cx[349], cy[4], 0xFF61625E);
        p.fill(cx[206], cy[1], cx[210], cy[5], 0xFF61625E);

        p.fill(cx[41], cy[5], cx[49], cy[7], 0xFF51504E);

        p.fill(cx[14], cy[11], cx[16], cy[19], 0xFF3C3F3B);

        p.fill(cx[287], cy[12], cx[289], cy[20], 0xFF51504E);

        p.fill(cx[96], cy[13], cx[98], cy[21], 0xFF42473F);

        p.fill(cx[352], cy[20], cx[354], cy[28], 0xFF676763);

        p.fill(cx[193], cy[27], cx[209], cy[28], 0xFF2F3336);

        p.fill(cx[0], cy[37], cx[4], cy[41], 0xFF51504E);

        p.fill(cx[347], cy[37], cx[351], cy[41], 0xFF6D6A64);

        p.fill(cx[13], cy[42], cx[21], cy[44], 0xFF2F3336);

        p.fill(cx[317], cy[44], cx[319], cy[52], 0xFF3C3F3B);

        p.fill(cx[37], cy[54], cx[38], cy[70], 0xFF2F3336);
        p.fill(cx[38], cy[72], cx[40], cy[80], 0xFF2F3336);
        p.fill(cx[321], cy[72], cx[322], cy[88], 0xFF2F3336);
        p.fill(cx[32], cy[75], cx[34], cy[83], 0xFF2F3336);
        p.fill(cx[327], cy[80], cx[343], cy[81], 0xFF2F3336);
        p.fill(cx[318], cy[89], cx[319], cy[105], 0xFF2F3336);

        p.fill(cx[34], cy[114], cx[35], cy[130], 0xFF42473F);

        p.fill(cx[21], cy[143], cx[25], cy[147], 0xFF51504E);

        p.fill(cx[317], cy[143], cx[318], cy[159], 0xFF2F3336);

        p.fill(cx[23], cy[162], cx[24], cy[178], 0xFF3C3F3B);

        p.fill(cx[220], cy[169], cx[224], cy[173], 0xFF2F3336);
        p.fill(cx[264], cy[169], cx[268], cy[173], 0xFF2F3336);
        p.fill(cx[269], cy[169], cx[273], cy[173], 0xFF2F3336);

        p.fill(cx[343], cy[170], cx[345], cy[178], 0xFF3C3F3B);

        p.fill(cx[320], cy[171], cx[321], cy[187], 0xFF2F3336);
        p.fill(cx[129], cy[177], cx[130], cy[193], 0xFF2F3336);
        p.fill(cx[226], cy[177], cx[227], cy[193], 0xFF2F3336);

        p.fill(cx[264], cy[178], cx[268], cy[182], 0xFF51504E);

        p.fill(cx[24], cy[179], cx[26], cy[187], 0xFF202423);

        p.fill(cx[197], cy[179], cx[199], cy[187], 0xFF42473F);

        p.fill(cx[16], cy[183], cx[24], cy[185], 0xFF202423);

        p.fill(cx[337], cy[186], cx[339], cy[194], 0xFF2F3336);
        p.fill(cx[62], cy[189], cx[66], cy[193], 0xFF2F3336);
        p.fill(cx[110], cy[189], cx[114], cy[193], 0xFF2F3336);
        p.fill(cx[172], cy[189], cx[176], cy[193], 0xFF2F3336);
        p.fill(cx[185], cy[189], cx[189], cy[193], 0xFF2F3336);
        p.fill(cx[210], cy[189], cx[214], cy[193], 0xFF2F3336);
        p.fill(cx[37], cy[190], cx[39], cy[198], 0xFF2F3336);

        p.fill(cx[0], cy[195], cx[4], cy[199], 0xFF51504E);

        p.fill(cx[58], cy[196], cx[66], cy[198], 0xFF3C3F3B);

        p.fill(cx[120], cy[4], cx[125], cy[7], 0xFF61625E);

        p.fill(cx[22], cy[5], cx[27], cy[8], 0xFF3C3F3B);

        p.fill(cx[56], cy[6], cx[61], cy[9], 0xFF42473F);

        p.fill(cx[58], cy[9], cx[73], cy[10], 0xFF202423);

        p.fill(cx[318], cy[9], cx[321], cy[14], 0xFF42473F);

        p.fill(cx[340], cy[11], cx[343], cy[16], 0xFF202423);

        p.fill(cx[292], cy[12], cx[297], cy[15], 0xFF51504E);

        p.fill(cx[210], cy[23], cx[225], cy[24], 0xFF2F3336);
        p.fill(cx[61], cy[25], cx[66], cy[28], 0xFF2F3336);

        p.fill(cx[9], cy[37], cx[12], cy[42], 0xFF676763);

        p.fill(cx[332], cy[42], cx[347], cy[43], 0xFF2F3336);
        p.fill(cx[38], cy[55], cx[39], cy[70], 0xFF2F3336);
        p.fill(cx[35], cy[66], cx[36], cy[81], 0xFF2F3336);
        p.fill(cx[326], cy[77], cx[341], cy[78], 0xFF2F3336);
        p.fill(cx[17], cy[80], cx[32], cy[81], 0xFF2F3336);
        p.fill(cx[17], cy[98], cx[32], cy[99], 0xFF2F3336);
        p.fill(cx[327], cy[116], cx[342], cy[117], 0xFF2F3336);
        p.fill(cx[19], cy[131], cx[34], cy[132], 0xFF2F3336);
        p.fill(cx[18], cy[152], cx[33], cy[153], 0xFF2F3336);

        p.fill(cx[327], cy[153], cx[342], cy[154], 0xFF202423);

        p.fill(cx[82], cy[166], cx[87], cy[169], 0xFF51504E);

        p.fill(cx[214], cy[168], cx[217], cy[173], 0xFF2F3336);
        p.fill(cx[273], cy[168], cx[276], cy[173], 0xFF2F3336);
        p.fill(cx[34], cy[173], cx[39], cy[176], 0xFF2F3336);

        p.fill(cx[34], cy[176], cx[35], cy[191], 0xFF3C3F3B);

        p.fill(cx[74], cy[176], cx[79], cy[179], 0xFF2F3336);

        p.fill(cx[135], cy[176], cx[150], cy[177], 0xFF202423);
        p.fill(cx[160], cy[176], cx[175], cy[177], 0xFF202423);
        p.fill(cx[185], cy[176], cx[200], cy[177], 0xFF202423);
        p.fill(cx[235], cy[176], cx[250], cy[177], 0xFF202423);
        p.fill(cx[260], cy[176], cx[275], cy[177], 0xFF202423);

        p.fill(cx[322], cy[176], cx[327], cy[179], 0xFF2F3336);
        p.fill(cx[151], cy[178], cx[152], cy[193], 0xFF2F3336);
        p.fill(cx[203], cy[178], cx[204], cy[193], 0xFF2F3336);

        p.fill(cx[69], cy[181], cx[72], cy[186], 0xFF51504E);

        p.fill(cx[187], cy[182], cx[190], cy[187], 0xFF42473F);
        p.fill(cx[218], cy[184], cx[223], cy[187], 0xFF42473F);

        p.fill(cx[42], cy[188], cx[43], cy[203], 0xFF2F3336);
        p.fill(cx[44], cy[188], cx[45], cy[203], 0xFF2F3336);
        p.fill(cx[335], cy[188], cx[336], cy[203], 0xFF2F3336);

        p.fill(cx[280], cy[193], cx[285], cy[196], 0xFF42473F);

        p.fill(cx[273], cy[194], cx[276], cy[199], 0xFF3C3F3B);
        p.fill(cx[316], cy[195], cx[319], cy[200], 0xFF3C3F3B);

        p.fill(cx[4], cy[198], cx[7], cy[203], 0xFF42473F);

        p.fill(cx[83], cy[200], cx[88], cy[203], 0xFF3C3F3B);

        p.fill(cx[331], cy[0], cx[345], cy[1], 0xFF565B54);

        p.fill(cx[358], cy[0], cx[360], cy[7], 0xFF6D6A64);

        p.fill(cx[13], cy[1], cx[20], cy[3], 0xFF565B54);
    }

    private static void part1(Paint p, int[] cx, int[] cy) {

        p.fill(cx[79], cy[2], cx[86], cy[4], 0xFF51504E);

        p.fill(cx[117], cy[2], cx[131], cy[3], 0xFF61625E);
        p.fill(cx[221], cy[2], cx[235], cy[3], 0xFF61625E);

        p.fill(cx[72], cy[5], cx[79], cy[7], 0xFF51504E);

        p.fill(cx[63], cy[7], cx[70], cy[9], 0xFF42473F);

        p.fill(cx[203], cy[7], cx[217], cy[8], 0xFF565B54);

        p.fill(cx[85], cy[10], cx[99], cy[11], 0xFF202423);
        p.fill(cx[110], cy[10], cx[124], cy[11], 0xFF202423);
        p.fill(cx[136], cy[10], cx[150], cy[11], 0xFF202423);
        p.fill(cx[161], cy[10], cx[175], cy[11], 0xFF202423);
        p.fill(cx[186], cy[10], cx[200], cy[11], 0xFF202423);
        p.fill(cx[211], cy[10], cx[225], cy[11], 0xFF202423);
        p.fill(cx[236], cy[10], cx[250], cy[11], 0xFF202423);
        p.fill(cx[261], cy[10], cx[275], cy[11], 0xFF202423);

        p.fill(cx[55], cy[12], cx[56], cy[26], 0xFF3C3F3B);

        p.fill(cx[236], cy[12], cx[243], cy[14], 0xFF42473F);
        p.fill(cx[161], cy[13], cx[163], cy[20], 0xFF42473F);

        p.fill(cx[161], cy[22], cx[175], cy[23], 0xFF202423);
        p.fill(cx[261], cy[22], cx[275], cy[23], 0xFF202423);

        p.fill(cx[110], cy[23], cx[124], cy[24], 0xFF2F3336);
        p.fill(cx[161], cy[23], cx[175], cy[24], 0xFF2F3336);
        p.fill(cx[261], cy[23], cx[275], cy[24], 0xFF2F3336);

        p.fill(cx[326], cy[23], cx[328], cy[30], 0xFF3C3F3B);
        p.fill(cx[30], cy[26], cx[37], cy[28], 0xFF3C3F3B);
        p.fill(cx[338], cy[27], cx[339], cy[41], 0xFF3C3F3B);
        p.fill(cx[324], cy[30], cx[326], cy[37], 0xFF3C3F3B);
        p.fill(cx[36], cy[43], cx[38], cy[50], 0xFF3C3F3B);

        p.fill(cx[14], cy[44], cx[21], cy[46], 0xFF2F3336);

        p.fill(cx[38], cy[44], cx[40], cy[51], 0xFF3C3F3B);

        p.fill(cx[18], cy[66], cx[20], cy[73], 0xFF51504E);

        p.fill(cx[328], cy[98], cx[342], cy[99], 0xFF2F3336);
        p.fill(cx[32], cy[112], cx[34], cy[119], 0xFF2F3336);
        p.fill(cx[18], cy[116], cx[32], cy[117], 0xFF2F3336);

        p.fill(cx[12], cy[138], cx[13], cy[152], 0xFF51504E);
        p.fill(cx[357], cy[146], cx[358], cy[160], 0xFF51504E);

        p.fill(cx[27], cy[162], cx[29], cy[169], 0xFF2F3336);

        p.fill(cx[21], cy[164], cx[22], cy[178], 0xFF3C3F3B);

        p.fill(cx[118], cy[174], cx[125], cy[176], 0xFF2F3336);
        p.fill(cx[229], cy[174], cx[236], cy[176], 0xFF2F3336);

        p.fill(cx[85], cy[176], cx[99], cy[177], 0xFF202423);
        p.fill(cx[110], cy[176], cx[124], cy[177], 0xFF202423);
        p.fill(cx[211], cy[176], cx[225], cy[177], 0xFF202423);

        p.fill(cx[236], cy[177], cx[250], cy[178], 0xFF51504E);

        p.fill(cx[202], cy[179], cx[203], cy[193], 0xFF2F3336);

        p.fill(cx[10], cy[180], cx[24], cy[181], 0xFF202423);

        p.fill(cx[86], cy[180], cx[88], cy[187], 0xFF42473F);

        p.fill(cx[332], cy[180], cx[333], cy[194], 0xFF202423);

        p.fill(cx[10], cy[181], cx[12], cy[188], 0xFF2F3336);

        p.fill(cx[293], cy[181], cx[295], cy[188], 0xFFD43835);

        p.fill(cx[12], cy[182], cx[14], cy[189], 0xFF2F3336);

        p.fill(cx[211], cy[188], cx[225], cy[189], 0xFF202423);
        p.fill(cx[236], cy[188], cx[250], cy[189], 0xFF202423);
        p.fill(cx[287], cy[188], cx[301], cy[189], 0xFF202423);

        p.fill(cx[43], cy[189], cx[44], cy[203], 0xFF2F3336);

        p.fill(cx[20], cy[196], cx[22], cy[203], 0xFF3C3F3B);

        p.fill(cx[124], cy[196], cx[126], cy[203], 0xFF42473F);
        p.fill(cx[131], cy[196], cx[133], cy[203], 0xFF42473F);
        p.fill(cx[199], cy[196], cx[201], cy[203], 0xFF42473F);

        p.fill(cx[81], cy[197], cx[88], cy[199], 0xFF3C3F3B);

        p.fill(cx[98], cy[201], cx[105], cy[203], 0xFF42473F);

        p.fill(cx[77], cy[0], cx[90], cy[1], 0xFF51504E);

        p.fill(cx[118], cy[3], cx[131], cy[4], 0xFF61625E);

        p.fill(cx[27], cy[5], cx[28], cy[18], 0xFF2F3336);
        p.fill(cx[206], cy[11], cx[207], cy[24], 0xFF2F3336);
        p.fill(cx[254], cy[11], cx[255], cy[24], 0xFF2F3336);

        p.fill(cx[12], cy[22], cx[13], cy[35], 0xFF3C3F3B);

        p.fill(cx[111], cy[22], cx[124], cy[23], 0xFF202423);
        p.fill(cx[136], cy[22], cx[149], cy[23], 0xFF202423);
        p.fill(cx[186], cy[22], cx[199], cy[23], 0xFF202423);
        p.fill(cx[237], cy[22], cx[250], cy[23], 0xFF202423);

        p.fill(cx[30], cy[23], cx[43], cy[24], 0xFF3C3F3B);

        p.fill(cx[85], cy[23], cx[98], cy[24], 0xFF2F3336);
        p.fill(cx[136], cy[23], cx[149], cy[24], 0xFF2F3336);
        p.fill(cx[186], cy[23], cx[199], cy[24], 0xFF2F3336);

        p.fill(cx[59], cy[24], cx[72], cy[25], 0xFF3C3F3B);

        p.fill(cx[269], cy[27], cx[282], cy[28], 0xFF2F3336);

        p.fill(cx[339], cy[28], cx[340], cy[41], 0xFF3C3F3B);

        p.fill(cx[328], cy[58], cx[341], cy[59], 0xFF202423);

        p.fill(cx[328], cy[59], cx[341], cy[60], 0xFF2F3336);

        p.fill(cx[328], cy[64], cx[329], cy[77], 0xFF202423);
        p.fill(cx[18], cy[76], cx[31], cy[77], 0xFF202423);

        p.fill(cx[353], cy[76], cx[354], cy[89], 0xFF61625E);

        p.fill(cx[18], cy[77], cx[31], cy[78], 0xFF2F3336);
        p.fill(cx[17], cy[99], cx[30], cy[100], 0xFF2F3336);

        p.fill(cx[31], cy[100], cx[32], cy[113], 0xFF202423);

        p.fill(cx[41], cy[105], cx[42], cy[118], 0xFF3C3F3B);

        p.fill(cx[18], cy[115], cx[31], cy[116], 0xFF202423);
        p.fill(cx[329], cy[115], cx[342], cy[116], 0xFF202423);
        p.fill(cx[31], cy[118], cx[32], cy[131], 0xFF202423);
        p.fill(cx[328], cy[118], cx[329], cy[131], 0xFF202423);
        p.fill(cx[18], cy[132], cx[31], cy[133], 0xFF202423);
        p.fill(cx[328], cy[136], cx[329], cy[149], 0xFF202423);

        p.fill(cx[353], cy[141], cx[354], cy[154], 0xFF42473F);

        p.fill(cx[18], cy[148], cx[31], cy[149], 0xFF202423);
        p.fill(cx[30], cy[159], cx[43], cy[160], 0xFF202423);

        p.fill(cx[320], cy[163], cx[333], cy[164], 0xFF2F3336);
        p.fill(cx[321], cy[164], cx[334], cy[165], 0xFF2F3336);

        p.fill(cx[19], cy[165], cx[20], cy[178], 0xFF3C3F3B);

        p.fill(cx[346], cy[165], cx[347], cy[178], 0xFF2F3336);

        p.fill(cx[302], cy[176], cx[303], cy[189], 0xFF202423);
        p.fill(cx[59], cy[188], cx[72], cy[189], 0xFF202423);
        p.fill(cx[110], cy[188], cx[123], cy[189], 0xFF202423);

        p.fill(cx[334], cy[190], cx[335], cy[203], 0xFF2F3336);

        p.fill(cx[98], cy[195], cx[111], cy[196], 0xFF3C3F3B);

        p.fill(cx[13], cy[3], cx[19], cy[5], 0xFF565B54);

        p.fill(cx[349], cy[3], cx[355], cy[5], 0xFF676763);

        p.fill(cx[9], cy[4], cx[12], cy[8], 0xFF61625E);
        p.fill(cx[127], cy[4], cx[131], cy[7], 0xFF61625E);
        p.fill(cx[114], cy[5], cx[120], cy[7], 0xFF61625E);

        p.fill(cx[290], cy[7], cx[302], cy[8], 0xFF42473F);

        p.fill(cx[73], cy[8], cx[79], cy[10], 0xFF3C3F3B);
        p.fill(cx[117], cy[8], cx[123], cy[10], 0xFF3C3F3B);

        p.fill(cx[105], cy[11], cx[106], cy[23], 0xFF2F3336);
        p.fill(cx[128], cy[11], cx[129], cy[23], 0xFF2F3336);
        p.fill(cx[181], cy[11], cx[182], cy[23], 0xFF2F3336);
        p.fill(cx[229], cy[11], cx[230], cy[23], 0xFF2F3336);
        p.fill(cx[231], cy[11], cx[232], cy[23], 0xFF2F3336);
        p.fill(cx[256], cy[11], cx[257], cy[23], 0xFF2F3336);

        p.fill(cx[82], cy[12], cx[84], cy[18], 0xFF3C3F3B);

        p.fill(cx[86], cy[12], cx[98], cy[13], 0xFF42473F);
        p.fill(cx[136], cy[12], cx[140], cy[15], 0xFF42473F);
        p.fill(cx[162], cy[12], cx[174], cy[13], 0xFF42473F);

        p.fill(cx[253], cy[12], cx[254], cy[24], 0xFF2F3336);
        p.fill(cx[276], cy[12], cx[277], cy[24], 0xFF2F3336);

        p.fill(cx[281], cy[13], cx[283], cy[19], 0xFF3C3F3B);
        p.fill(cx[348], cy[13], cx[350], cy[19], 0xFF3C3F3B);
        p.fill(cx[306], cy[14], cx[307], cy[26], 0xFF3C3F3B);
        p.fill(cx[313], cy[14], cx[316], cy[18], 0xFF3C3F3B);

        p.fill(cx[289], cy[16], cx[291], cy[22], 0xFF51504E);

        p.fill(cx[171], cy[17], cx[174], cy[21], 0xFF42473F);
        p.fill(cx[91], cy[18], cx[95], cy[21], 0xFF42473F);
        p.fill(cx[191], cy[18], cx[195], cy[21], 0xFF42473F);
        p.fill(cx[214], cy[18], cx[218], cy[21], 0xFF42473F);
        p.fill(cx[328], cy[18], cx[332], cy[21], 0xFF42473F);

        p.fill(cx[318], cy[19], cx[320], cy[25], 0xFF3C3F3B);

        p.fill(cx[111], cy[21], cx[123], cy[22], 0xFF51504E);
        p.fill(cx[237], cy[21], cx[249], cy[22], 0xFF51504E);

        p.fill(cx[86], cy[22], cx[98], cy[23], 0xFF202423);
        p.fill(cx[212], cy[22], cx[224], cy[23], 0xFF202423);

        p.fill(cx[236], cy[23], cx[248], cy[24], 0xFF2F3336);

        p.fill(cx[288], cy[24], cx[300], cy[25], 0xFF3C3F3B);
        p.fill(cx[332], cy[24], cx[333], cy[36], 0xFF3C3F3B);

        p.fill(cx[71], cy[25], cx[75], cy[28], 0xFF2F3336);
        p.fill(cx[214], cy[25], cx[218], cy[28], 0xFF2F3336);

        p.fill(cx[336], cy[29], cx[337], cy[41], 0xFF3C3F3B);
        p.fill(cx[32], cy[31], cx[34], cy[37], 0xFF3C3F3B);

        p.fill(cx[318], cy[36], cx[324], cy[38], 0xFF2F3336);
        p.fill(cx[24], cy[42], cx[27], cy[46], 0xFF2F3336);

        p.fill(cx[31], cy[46], cx[32], cy[58], 0xFF202423);

        p.fill(cx[346], cy[49], cx[347], cy[61], 0xFF51504E);
        p.fill(cx[20], cy[51], cx[22], cy[57], 0xFF51504E);

        p.fill(cx[19], cy[59], cx[31], cy[60], 0xFF2F3336);

        p.fill(cx[17], cy[64], cx[18], cy[76], 0xFF202423);

        p.fill(cx[26], cy[66], cx[29], cy[70], 0xFF51504E);
        p.fill(cx[27], cy[70], cx[29], cy[76], 0xFF51504E);

        p.fill(cx[329], cy[76], cx[341], cy[77], 0xFF202423);
        p.fill(cx[17], cy[82], cx[18], cy[94], 0xFF202423);
        p.fill(cx[328], cy[82], cx[329], cy[94], 0xFF202423);

        p.fill(cx[20], cy[87], cx[22], cy[93], 0xFF51504E);

        p.fill(cx[329], cy[94], cx[341], cy[95], 0xFF202423);

        p.fill(cx[19], cy[95], cx[31], cy[96], 0xFF2F3336);
        p.fill(cx[329], cy[95], cx[341], cy[96], 0xFF2F3336);

        p.fill(cx[17], cy[100], cx[18], cy[112], 0xFF202423);
        p.fill(cx[328], cy[100], cx[329], cy[112], 0xFF202423);

        p.fill(cx[335], cy[101], cx[338], cy[105], 0xFF51504E);
        p.fill(cx[20], cy[105], cx[22], cy[111], 0xFF51504E);

        p.fill(cx[20], cy[113], cx[32], cy[114], 0xFF2F3336);

        p.fill(cx[17], cy[118], cx[18], cy[130], 0xFF202423);
        p.fill(cx[32], cy[119], cx[33], cy[131], 0xFF202423);

        p.fill(cx[27], cy[120], cx[30], cy[124], 0xFF51504E);
        p.fill(cx[330], cy[120], cx[334], cy[123], 0xFF51504E);
        p.fill(cx[20], cy[123], cx[22], cy[129], 0xFF51504E);
        p.fill(cx[335], cy[126], cx[339], cy[129], 0xFF51504E);

        p.fill(cx[19], cy[130], cx[31], cy[131], 0xFF202423);
        p.fill(cx[329], cy[130], cx[341], cy[131], 0xFF202423);
        p.fill(cx[17], cy[136], cx[18], cy[148], 0xFF202423);
        p.fill(cx[342], cy[136], cx[343], cy[148], 0xFF202423);

        p.fill(cx[335], cy[138], cx[339], cy[141], 0xFF51504E);

        p.fill(cx[329], cy[148], cx[341], cy[149], 0xFF202423);

        p.fill(cx[32], cy[153], cx[34], cy[159], 0xFF2F3336);
        p.fill(cx[344], cy[153], cx[346], cy[159], 0xFF2F3336);
        p.fill(cx[29], cy[159], cx[30], cy[171], 0xFF2F3336);

        p.fill(cx[343], cy[160], cx[345], cy[166], 0xFF3C3F3B);

        p.fill(cx[39], cy[165], cx[40], cy[177], 0xFF2F3336);

        p.fill(cx[20], cy[166], cx[21], cy[178], 0xFF3C3F3B);

        p.fill(cx[296], cy[166], cx[308], cy[167], 0xFF42473F);

        p.fill(cx[41], cy[168], cx[43], cy[174], 0xFF2F3336);
        p.fill(cx[317], cy[168], cx[319], cy[174], 0xFF2F3336);

        p.fill(cx[25], cy[169], cx[28], cy[173], 0xFF3C3F3B);

        p.fill(cx[320], cy[169], cx[332], cy[170], 0xFF2F3336);

        p.fill(cx[0], cy[170], cx[4], cy[173], 0xFF61625E);
        p.fill(cx[8], cy[172], cx[10], cy[178], 0xFF61625E);

        p.fill(cx[0], cy[173], cx[4], cy[176], 0xFF676763);
        p.fill(cx[356], cy[173], cx[360], cy[176], 0xFF676763);

        p.fill(cx[31], cy[174], cx[33], cy[180], 0xFF3C3F3B);

        p.fill(cx[125], cy[175], cx[128], cy[179], 0xFF2F3336);

        p.fill(cx[285], cy[176], cx[286], cy[188], 0xFF202423);
        p.fill(cx[72], cy[177], cx[73], cy[189], 0xFF202423);

        p.fill(cx[73], cy[177], cx[74], cy[189], 0xFF2F3336);

        p.fill(cx[99], cy[177], cx[100], cy[189], 0xFF202423);
        p.fill(cx[124], cy[177], cx[125], cy[189], 0xFF202423);
        p.fill(cx[175], cy[177], cx[176], cy[189], 0xFF202423);
        p.fill(cx[185], cy[177], cx[186], cy[189], 0xFF202423);
        p.fill(cx[200], cy[177], cx[201], cy[189], 0xFF202423);
        p.fill(cx[210], cy[177], cx[211], cy[189], 0xFF202423);
        p.fill(cx[225], cy[177], cx[226], cy[189], 0xFF202423);
        p.fill(cx[235], cy[177], cx[236], cy[189], 0xFF202423);
        p.fill(cx[260], cy[177], cx[261], cy[189], 0xFF202423);

        p.fill(cx[60], cy[178], cx[72], cy[179], 0xFF42473F);
        p.fill(cx[119], cy[178], cx[123], cy[181], 0xFF42473F);

        p.fill(cx[163], cy[178], cx[165], cy[184], 0xFF51504E);

        p.fill(cx[356], cy[179], cx[360], cy[182], 0xFF8A8C83);
        p.fill(cx[0], cy[180], cx[3], cy[184], 0xFF8A8C83);

        p.fill(cx[148], cy[180], cx[150], cy[186], 0xFF51504E);

        p.fill(cx[12], cy[181], cx[24], cy[182], 0xFF202423);
        p.fill(cx[336], cy[181], cx[348], cy[182], 0xFF202423);

        p.fill(cx[352], cy[183], cx[356], cy[186], 0xFF8B9494);

        p.fill(cx[143], cy[184], cx[147], cy[187], 0xFF42473F);
        p.fill(cx[213], cy[184], cx[217], cy[187], 0xFF42473F);

        p.fill(cx[22], cy[185], cx[23], cy[197], 0xFF2F3336);

        p.fill(cx[321], cy[185], cx[324], cy[189], 0xFF3C3F3B);

        p.fill(cx[23], cy[186], cx[24], cy[198], 0xFF2F3336);
        p.fill(cx[24], cy[187], cx[25], cy[199], 0xFF2F3336);
        p.fill(cx[25], cy[188], cx[26], cy[200], 0xFF2F3336);

        p.fill(cx[135], cy[188], cx[147], cy[189], 0xFF202423);
        p.fill(cx[161], cy[188], cx[173], cy[189], 0xFF202423);
        p.fill(cx[186], cy[188], cx[198], cy[189], 0xFF202423);

        p.fill(cx[85], cy[189], cx[88], cy[193], 0xFF2F3336);
        p.fill(cx[115], cy[189], cx[118], cy[193], 0xFF2F3336);
        p.fill(cx[120], cy[189], cx[123], cy[193], 0xFF2F3336);
        p.fill(cx[215], cy[189], cx[218], cy[193], 0xFF2F3336);
        p.fill(cx[189], cy[190], cx[193], cy[193], 0xFF2F3336);
        p.fill(cx[30], cy[192], cx[34], cy[195], 0xFF2F3336);

        p.fill(cx[75], cy[193], cx[79], cy[196], 0xFF42473F);

        p.fill(cx[304], cy[193], cx[308], cy[196], 0xFF2F3336);

        p.fill(cx[277], cy[194], cx[279], cy[200], 0xFF3C3F3B);

        p.fill(cx[289], cy[195], cx[291], cy[201], 0xFF42473F);

        p.fill(cx[313], cy[195], cx[316], cy[199], 0xFF2F3336);

        p.fill(cx[66], cy[196], cx[70], cy[199], 0xFF42473F);
        p.fill(cx[93], cy[197], cx[95], cy[203], 0xFF42473F);
        p.fill(cx[105], cy[197], cx[107], cy[203], 0xFF42473F);
        p.fill(cx[108], cy[197], cx[110], cy[203], 0xFF42473F);
        p.fill(cx[129], cy[197], cx[131], cy[203], 0xFF42473F);

        p.fill(cx[36], cy[199], cx[39], cy[203], 0xFF2F3336);

        p.fill(cx[67], cy[199], cx[70], cy[203], 0xFF42473F);
        p.fill(cx[89], cy[199], cx[92], cy[203], 0xFF42473F);
        p.fill(cx[137], cy[202], cx[149], cy[203], 0xFF42473F);
        p.fill(cx[56], cy[5], cx[67], cy[6], 0xFF42473F);

        p.fill(cx[26], cy[8], cx[27], cy[19], 0xFF2F3336);

        p.fill(cx[298], cy[8], cx[309], cy[9], 0xFF3C3F3B);
        p.fill(cx[200], cy[10], cx[211], cy[11], 0xFF3C3F3B);
        p.fill(cx[225], cy[10], cx[236], cy[11], 0xFF3C3F3B);
        p.fill(cx[250], cy[10], cx[261], cy[11], 0xFF3C3F3B);

        p.fill(cx[84], cy[11], cx[85], cy[22], 0xFF202423);
        p.fill(cx[99], cy[11], cx[100], cy[22], 0xFF202423);
        p.fill(cx[109], cy[11], cx[110], cy[22], 0xFF202423);

        p.fill(cx[136], cy[11], cx[147], cy[12], 0xFF51504E);

        p.fill(cx[150], cy[11], cx[151], cy[22], 0xFF202423);
        p.fill(cx[154], cy[11], cx[155], cy[22], 0xFF202423);

        p.fill(cx[156], cy[11], cx[157], cy[22], 0xFF2F3336);

        p.fill(cx[160], cy[11], cx[161], cy[22], 0xFF202423);
        p.fill(cx[175], cy[11], cx[176], cy[22], 0xFF202423);
        p.fill(cx[185], cy[11], cx[186], cy[22], 0xFF202423);
        p.fill(cx[200], cy[11], cx[201], cy[22], 0xFF202423);
        p.fill(cx[235], cy[11], cx[236], cy[22], 0xFF202423);
        p.fill(cx[250], cy[11], cx[251], cy[22], 0xFF202423);
        p.fill(cx[260], cy[11], cx[261], cy[22], 0xFF202423);
        p.fill(cx[275], cy[11], cx[276], cy[22], 0xFF202423);
        p.fill(cx[285], cy[11], cx[286], cy[22], 0xFF202423);

        p.fill(cx[125], cy[12], cx[126], cy[23], 0xFF2F3336);
        p.fill(cx[151], cy[12], cx[152], cy[23], 0xFF2F3336);
        p.fill(cx[201], cy[12], cx[202], cy[23], 0xFF2F3336);
        p.fill(cx[234], cy[12], cx[235], cy[23], 0xFF2F3336);

        p.fill(cx[286], cy[12], cx[287], cy[23], 0xFF202423);

        p.fill(cx[159], cy[13], cx[160], cy[24], 0xFF2F3336);

        p.fill(cx[308], cy[15], cx[309], cy[26], 0xFF3C3F3B);

        p.fill(cx[13], cy[19], cx[24], cy[20], 0xFF202423);

        p.fill(cx[30], cy[24], cx[41], cy[25], 0xFF3C3F3B);

        p.fill(cx[168], cy[27], cx[179], cy[28], 0xFF2F3336);

        p.fill(cx[37], cy[28], cx[38], cy[39], 0xFF3C3F3B);

        p.fill(cx[18], cy[46], cx[19], cy[57], 0xFF51504E);

        p.fill(cx[17], cy[47], cx[18], cy[58], 0xFF202423);
        p.fill(cx[326], cy[47], cx[327], cy[58], 0xFF202423);

        p.fill(cx[329], cy[47], cx[330], cy[58], 0xFF51504E);

        p.fill(cx[342], cy[47], cx[343], cy[58], 0xFF202423);

        p.fill(cx[35], cy[54], cx[36], cy[65], 0xFF2F3336);

        p.fill(cx[19], cy[58], cx[30], cy[59], 0xFF202423);

        p.fill(cx[20], cy[65], cx[21], cy[76], 0xFF51504E);

        p.fill(cx[330], cy[65], cx[341], cy[66], 0xFF3C3F3B);

        p.fill(cx[342], cy[65], cx[343], cy[76], 0xFF202423);
        p.fill(cx[342], cy[83], cx[343], cy[94], 0xFF202423);

        p.fill(cx[7], cy[91], cx[8], cy[102], 0xFF676763);

        p.fill(cx[19], cy[94], cx[30], cy[95], 0xFF202423);

        p.fill(cx[30], cy[100], cx[31], cy[111], 0xFF51504E);
        p.fill(cx[18], cy[101], cx[19], cy[112], 0xFF51504E);

        p.fill(cx[32], cy[101], cx[33], cy[112], 0xFF202423);
        p.fill(cx[342], cy[101], cx[343], cy[112], 0xFF202423);
        p.fill(cx[329], cy[112], cx[340], cy[113], 0xFF202423);

        p.fill(cx[329], cy[113], cx[340], cy[114], 0xFF2F3336);
        p.fill(cx[20], cy[114], cx[31], cy[115], 0xFF2F3336);

        p.fill(cx[330], cy[117], cx[341], cy[118], 0xFF202423);

        p.fill(cx[19], cy[118], cx[30], cy[119], 0xFF676763);

        p.fill(cx[18], cy[119], cx[19], cy[130], 0xFF51504E);

        p.fill(cx[342], cy[119], cx[343], cy[130], 0xFF202423);

        p.fill(cx[346], cy[126], cx[347], cy[137], 0xFF42473F);

        p.fill(cx[19], cy[129], cx[30], cy[130], 0xFF565B54);

        p.fill(cx[330], cy[135], cx[341], cy[136], 0xFF202423);

        p.fill(cx[329], cy[136], cx[330], cy[147], 0xFF51504E);

        p.fill(cx[33], cy[138], cx[34], cy[149], 0xFF202423);

        p.fill(cx[8], cy[141], cx[9], cy[152], 0xFF676763);

        p.fill(cx[356], cy[148], cx[357], cy[159], 0xFF51504E);

        p.fill(cx[7], cy[158], cx[8], cy[169], 0xFF42473F);

        p.fill(cx[30], cy[160], cx[41], cy[161], 0xFF2F3336);

        p.fill(cx[340], cy[161], cx[341], cy[172], 0xFF3C3F3B);
        p.fill(cx[341], cy[162], cx[342], cy[173], 0xFF3C3F3B);
        p.fill(cx[13], cy[167], cx[14], cy[178], 0xFF3C3F3B);
        p.fill(cx[29], cy[176], cx[30], cy[187], 0xFF3C3F3B);

        p.fill(cx[58], cy[177], cx[59], cy[188], 0xFF202423);
        p.fill(cx[84], cy[177], cx[85], cy[188], 0xFF202423);
        p.fill(cx[109], cy[177], cx[110], cy[188], 0xFF202423);
        p.fill(cx[130], cy[177], cx[131], cy[188], 0xFF202423);
        p.fill(cx[134], cy[177], cx[135], cy[188], 0xFF202423);

        p.fill(cx[174], cy[177], cx[175], cy[188], 0xFF51504E);

        p.fill(cx[231], cy[177], cx[232], cy[188], 0xFF202423);
        p.fill(cx[250], cy[177], cx[251], cy[188], 0xFF202423);
        p.fill(cx[256], cy[177], cx[257], cy[188], 0xFF202423);

        p.fill(cx[261], cy[177], cx[272], cy[178], 0xFF51504E);

        p.fill(cx[286], cy[177], cx[287], cy[188], 0xFF202423);

        p.fill(cx[336], cy[187], cx[337], cy[198], 0xFF2F3336);

        p.fill(cx[261], cy[188], cx[272], cy[189], 0xFF202423);

        p.fill(cx[41], cy[192], cx[42], cy[203], 0xFF2F3336);

        p.fill(cx[59], cy[195], cx[70], cy[196], 0xFF3C3F3B);

        p.fill(cx[7], cy[0], cx[12], cy[2], 0xFF61625E);

        p.fill(cx[78], cy[1], cx[88], cy[2], 0xFF51504E);
        p.fill(cx[335], cy[3], cx[340], cy[5], 0xFF51504E);

        p.fill(cx[340], cy[3], cx[345], cy[5], 0xFF565B54);

        p.fill(cx[311], cy[5], cx[316], cy[7], 0xFF676763);
        p.fill(cx[323], cy[5], cx[328], cy[7], 0xFF676763);

        p.fill(cx[20], cy[6], cx[22], cy[11], 0xFF3C3F3B);
        p.fill(cx[335], cy[6], cx[337], cy[11], 0xFF3C3F3B);

        p.fill(cx[332], cy[7], cx[333], cy[17], 0xFF2F3336);

        p.fill(cx[110], cy[8], cx[115], cy[10], 0xFF3C3F3B);

        p.fill(cx[320], cy[8], cx[330], cy[9], 0xFF42473F);

        p.fill(cx[333], cy[8], cx[334], cy[18], 0xFF2F3336);

        p.fill(cx[51], cy[9], cx[53], cy[14], 0xFF42473F);

        p.fill(cx[87], cy[9], cx[97], cy[10], 0xFF3C3F3B);
        p.fill(cx[98], cy[9], cx[108], cy[10], 0xFF3C3F3B);

        p.fill(cx[334], cy[9], cx[335], cy[19], 0xFF2F3336);

        p.fill(cx[10], cy[10], cx[11], cy[20], 0xFF3C3F3B);
        p.fill(cx[80], cy[10], cx[82], cy[15], 0xFF3C3F3B);
        p.fill(cx[99], cy[10], cx[109], cy[11], 0xFF3C3F3B);
        p.fill(cx[124], cy[10], cx[134], cy[11], 0xFF3C3F3B);

        p.fill(cx[60], cy[11], cx[70], cy[12], 0xFF51504E);
        p.fill(cx[261], cy[11], cx[271], cy[12], 0xFF51504E);

        p.fill(cx[313], cy[11], cx[318], cy[13], 0xFF3C3F3B);

        p.fill(cx[100], cy[12], cx[101], cy[22], 0xFF2F3336);
        p.fill(cx[131], cy[12], cx[132], cy[22], 0xFF2F3336);
        p.fill(cx[153], cy[12], cx[154], cy[22], 0xFF2F3336);
        p.fill(cx[178], cy[12], cx[179], cy[22], 0xFF2F3336);

        p.fill(cx[210], cy[12], cx[211], cy[22], 0xFF202423);

        p.fill(cx[215], cy[12], cx[225], cy[13], 0xFF42473F);

        p.fill(cx[225], cy[12], cx[226], cy[22], 0xFF202423);

        p.fill(cx[244], cy[12], cx[249], cy[14], 0xFF42473F);

        p.fill(cx[251], cy[12], cx[252], cy[22], 0xFF2F3336);
        p.fill(cx[259], cy[12], cx[260], cy[22], 0xFF2F3336);

        p.fill(cx[262], cy[12], cx[272], cy[13], 0xFF42473F);

        p.fill(cx[274], cy[12], cx[275], cy[22], 0xFF51504E);

        p.fill(cx[302], cy[12], cx[303], cy[22], 0xFF2F3336);

        p.fill(cx[53], cy[13], cx[55], cy[18], 0xFF3C3F3B);

        p.fill(cx[207], cy[13], cx[209], cy[18], 0xFF2F3336);

        p.fill(cx[89], cy[14], cx[94], cy[16], 0xFF51504E);

        p.fill(cx[252], cy[14], cx[253], cy[24], 0xFF3C3F3B);
        p.fill(cx[307], cy[16], cx[308], cy[26], 0xFF3C3F3B);
        p.fill(cx[313], cy[18], cx[315], cy[23], 0xFF3C3F3B);

        p.fill(cx[0], cy[23], cx[2], cy[28], 0xFF676763);

        p.fill(cx[30], cy[25], cx[40], cy[26], 0xFF3C3F3B);
        p.fill(cx[131], cy[25], cx[141], cy[26], 0xFF3C3F3B);

        p.fill(cx[66], cy[26], cx[71], cy[28], 0xFF2F3336);
        p.fill(cx[179], cy[26], cx[184], cy[28], 0xFF2F3336);

        p.fill(cx[326], cy[31], cx[327], cy[41], 0xFF3C3F3B);
        p.fill(cx[19], cy[36], cx[21], cy[41], 0xFF3C3F3B);

        p.fill(cx[26], cy[36], cx[28], cy[41], 0xFF2F3336);

        p.fill(cx[9], cy[45], cx[11], cy[50], 0xFF61625E);

        p.fill(cx[331], cy[49], cx[333], cy[54], 0xFF51504E);
        p.fill(cx[334], cy[52], cx[336], cy[57], 0xFF51504E);

        p.fill(cx[317], cy[57], cx[318], cy[67], 0xFF2F3336);
        p.fill(cx[318], cy[58], cx[319], cy[68], 0xFF2F3336);
        p.fill(cx[39], cy[60], cx[40], cy[70], 0xFF2F3336);

        p.fill(cx[30], cy[65], cx[31], cy[75], 0xFF51504E);

        p.fill(cx[349], cy[67], cx[351], cy[72], 0xFF61625E);

        p.fill(cx[37], cy[71], cx[38], cy[81], 0xFF2F3336);

        p.fill(cx[20], cy[81], cx[30], cy[82], 0xFF202423);

        p.fill(cx[359], cy[81], cx[360], cy[91], 0xFF61625E);

        p.fill(cx[18], cy[83], cx[19], cy[93], 0xFF51504E);

        p.fill(cx[330], cy[83], cx[331], cy[93], 0xFF42473F);

        p.fill(cx[33], cy[84], cx[34], cy[94], 0xFF202423);

        p.fill(cx[354], cy[88], cx[359], cy[90], 0xFF565B54);

        p.fill(cx[32], cy[96], cx[34], cy[101], 0xFF2F3336);

        p.fill(cx[341], cy[101], cx[342], cy[111], 0xFF51504E);

        p.fill(cx[33], cy[102], cx[34], cy[112], 0xFF202423);

        p.fill(cx[27], cy[106], cx[29], cy[111], 0xFF51504E);

        p.fill(cx[9], cy[108], cx[10], cy[118], 0xFF565B54);

        p.fill(cx[42], cy[109], cx[43], cy[119], 0xFF3C3F3B);

        p.fill(cx[20], cy[112], cx[30], cy[113], 0xFF202423);

        p.fill(cx[30], cy[119], cx[31], cy[129], 0xFF51504E);

        p.fill(cx[33], cy[121], cx[34], cy[131], 0xFF202423);

        p.fill(cx[27], cy[124], cx[29], cy[129], 0xFF51504E);

        p.fill(cx[318], cy[125], cx[319], cy[135], 0xFF2F3336);

        p.fill(cx[4], cy[135], cx[6], cy[140], 0xFF51504E);
        p.fill(cx[18], cy[137], cx[19], cy[147], 0xFF51504E);
        p.fill(cx[30], cy[137], cx[31], cy[147], 0xFF51504E);

        p.fill(cx[349], cy[140], cx[350], cy[150], 0xFF565B54);

        p.fill(cx[341], cy[148], cx[343], cy[153], 0xFF2F3336);

        p.fill(cx[9], cy[154], cx[11], cy[159], 0xFF61625E);

        p.fill(cx[12], cy[160], cx[13], cy[170], 0xFF2F3336);

        p.fill(cx[138], cy[166], cx[143], cy[168], 0xFF51504E);

        p.fill(cx[40], cy[167], cx[41], cy[177], 0xFF2F3336);

        p.fill(cx[65], cy[167], cx[75], cy[168], 0xFF42473F);

        p.fill(cx[24], cy[168], cx[25], cy[178], 0xFF3C3F3B);

        p.fill(cx[131], cy[168], cx[133], cy[173], 0xFF2F3336);
        p.fill(cx[165], cy[168], cx[167], cy[173], 0xFF2F3336);
        p.fill(cx[224], cy[168], cx[226], cy[173], 0xFF2F3336);
        p.fill(cx[227], cy[168], cx[229], cy[173], 0xFF2F3336);
        p.fill(cx[236], cy[168], cx[238], cy[173], 0xFF2F3336);
        p.fill(cx[243], cy[168], cx[253], cy[169], 0xFF2F3336);
        p.fill(cx[277], cy[168], cx[279], cy[173], 0xFF2F3336);

        p.fill(cx[335], cy[168], cx[336], cy[178], 0xFF3C3F3B);

        p.fill(cx[323], cy[170], cx[333], cy[171], 0xFF2F3336);

        p.fill(cx[25], cy[173], cx[27], cy[178], 0xFF3C3F3B);

        p.fill(cx[215], cy[174], cx[220], cy[176], 0xFF2F3336);
        p.fill(cx[45], cy[176], cx[50], cy[178], 0xFF2F3336);
        p.fill(cx[250], cy[176], cx[260], cy[177], 0xFF2F3336);

        p.fill(cx[111], cy[177], cx[121], cy[178], 0xFF51504E);
        p.fill(cx[59], cy[178], cx[60], cy[188], 0xFF51504E);
        p.fill(cx[85], cy[178], cx[86], cy[188], 0xFF51504E);

        p.fill(cx[88], cy[178], cx[98], cy[179], 0xFF42473F);

        p.fill(cx[98], cy[178], cx[99], cy[188], 0xFF51504E);
        p.fill(cx[135], cy[178], cx[136], cy[188], 0xFF51504E);

        p.fill(cx[159], cy[178], cx[160], cy[188], 0xFF202423);

        p.fill(cx[173], cy[178], cx[174], cy[188], 0xFF51504E);
        p.fill(cx[199], cy[178], cx[200], cy[188], 0xFF51504E);
        p.fill(cx[224], cy[178], cx[225], cy[188], 0xFF51504E);

        p.fill(cx[236], cy[178], cx[238], cy[183], 0xFF42473F);

        p.fill(cx[275], cy[178], cx[276], cy[188], 0xFF202423);

        p.fill(cx[300], cy[178], cx[301], cy[188], 0xFFD43835);

        p.fill(cx[301], cy[178], cx[302], cy[188], 0xFF202423);
        p.fill(cx[180], cy[179], cx[181], cy[189], 0xFF202423);

        p.fill(cx[245], cy[179], cx[250], cy[181], 0xFF42473F);

        p.fill(cx[268], cy[179], cx[273], cy[181], 0xFF51504E);

        p.fill(cx[310], cy[180], cx[315], cy[182], 0xFF2F3336);

        p.fill(cx[14], cy[182], cx[24], cy[183], 0xFF202423);

        p.fill(cx[33], cy[182], cx[34], cy[192], 0xFF3C3F3B);

        p.fill(cx[112], cy[182], cx[114], cy[187], 0xFF42473F);
        p.fill(cx[246], cy[182], cx[248], cy[187], 0xFF42473F);

        p.fill(cx[330], cy[183], cx[332], cy[188], 0xFF2F3336);
        p.fill(cx[344], cy[183], cx[345], cy[193], 0xFF2F3336);
        p.fill(cx[21], cy[186], cx[22], cy[196], 0xFF2F3336);

        p.fill(cx[319], cy[187], cx[321], cy[192], 0xFF3C3F3B);
        p.fill(cx[325], cy[187], cx[330], cy[189], 0xFF3C3F3B);

        p.fill(cx[154], cy[188], cx[156], cy[193], 0xFF2F3336);
        p.fill(cx[230], cy[188], cx[232], cy[193], 0xFF2F3336);
        p.fill(cx[272], cy[188], cx[274], cy[193], 0xFF2F3336);

        p.fill(cx[0], cy[192], cx[5], cy[194], 0xFF565B54);

        p.fill(cx[286], cy[193], cx[287], cy[203], 0xFF42473F);

        p.fill(cx[331], cy[193], cx[332], cy[203], 0xFF2F3336);

        p.fill(cx[350], cy[193], cx[351], cy[203], 0xFF42473F);

        p.fill(cx[28], cy[198], cx[30], cy[203], 0xFF2F3336);

        p.fill(cx[354], cy[198], cx[356], cy[203], 0xFF42473F);

        p.fill(cx[41], cy[4], cx[50], cy[5], 0xFF565B54);

        p.fill(cx[109], cy[5], cx[112], cy[8], 0xFF61625E);
        p.fill(cx[290], cy[6], cx[299], cy[7], 0xFF61625E);
        p.fill(cx[89], cy[7], cx[98], cy[8], 0xFF61625E);

        p.fill(cx[330], cy[7], cx[331], cy[16], 0xFF2F3336);

        p.fill(cx[313], cy[8], cx[316], cy[11], 0xFF42473F);

        p.fill(cx[25], cy[10], cx[26], cy[19], 0xFF2F3336);

        p.fill(cx[9], cy[11], cx[10], cy[20], 0xFF3C3F3B);

        p.fill(cx[161], cy[11], cx[170], cy[12], 0xFF51504E);
        p.fill(cx[291], cy[11], cx[300], cy[12], 0xFF51504E);

        p.fill(cx[335], cy[11], cx[336], cy[20], 0xFF2F3336);

        p.fill(cx[1], cy[12], cx[4], cy[15], 0xFF8A8C83);

        p.fill(cx[5], cy[12], cx[8], cy[15], 0xFFA5A49E);

        p.fill(cx[57], cy[12], cx[58], cy[21], 0xFF2F3336);

        p.fill(cx[60], cy[12], cx[61], cy[21], 0xFF42473F);

        p.fill(cx[74], cy[12], cx[75], cy[21], 0xFF2F3336);

        p.fill(cx[85], cy[12], cx[86], cy[21], 0xFF51504E);
        p.fill(cx[98], cy[12], cx[99], cy[21], 0xFF51504E);

        p.fill(cx[176], cy[12], cx[177], cy[21], 0xFF2F3336);

        p.fill(cx[199], cy[12], cx[200], cy[21], 0xFF51504E);

        p.fill(cx[209], cy[12], cx[210], cy[21], 0xFF2F3336);

        p.fill(cx[301], cy[12], cx[302], cy[21], 0xFF202423);

        p.fill(cx[268], cy[13], cx[271], cy[16], 0xFF51504E);
        p.fill(cx[289], cy[13], cx[292], cy[16], 0xFF51504E);

        p.fill(cx[184], cy[14], cx[185], cy[23], 0xFF2F3336);
        p.fill(cx[226], cy[15], cx[227], cy[24], 0xFF2F3336);
        p.fill(cx[228], cy[15], cx[229], cy[24], 0xFF2F3336);

        p.fill(cx[233], cy[15], cx[234], cy[24], 0xFF3C3F3B);

        p.fill(cx[357], cy[15], cx[360], cy[18], 0xFF8A8C83);

        p.fill(cx[283], cy[16], cx[284], cy[25], 0xFF3C3F3B);

        p.fill(cx[51], cy[18], cx[54], cy[21], 0xFF42473F);
        p.fill(cx[138], cy[18], cx[141], cy[21], 0xFF42473F);
    }

    private static void part2(Paint p, int[] cx, int[] cy) {

        p.fill(cx[240], cy[18], cx[243], cy[21], 0xFF42473F);

        p.fill(cx[324], cy[20], cx[325], cy[29], 0xFF3C3F3B);
        p.fill(cx[337], cy[20], cx[340], cy[23], 0xFF3C3F3B);

        p.fill(cx[190], cy[21], cx[199], cy[22], 0xFF51504E);

        p.fill(cx[288], cy[23], cx[297], cy[24], 0xFF202423);

        p.fill(cx[8], cy[24], cx[11], cy[27], 0xFF676763);

        p.fill(cx[11], cy[24], cx[12], cy[33], 0xFF6D6A64);

        p.fill(cx[101], cy[25], cx[104], cy[28], 0xFF2F3336);

        p.fill(cx[322], cy[27], cx[323], cy[36], 0xFF3C3F3B);
        p.fill(cx[327], cy[32], cx[328], cy[41], 0xFF3C3F3B);
        p.fill(cx[21], cy[38], cx[24], cy[41], 0xFF3C3F3B);

        p.fill(cx[21], cy[43], cx[24], cy[46], 0xFF2F3336);
        p.fill(cx[320], cy[48], cx[323], cy[51], 0xFF2F3336);

        p.fill(cx[42], cy[50], cx[43], cy[59], 0xFF42473F);

        p.fill(cx[31], cy[58], cx[34], cy[61], 0xFF2F3336);

        p.fill(cx[33], cy[66], cx[34], cy[75], 0xFF202423);

        p.fill(cx[339], cy[67], cx[340], cy[76], 0xFF51504E);
        p.fill(cx[19], cy[84], cx[20], cy[93], 0xFF51504E);
        p.fill(cx[332], cy[84], cx[335], cy[87], 0xFF51504E);
        p.fill(cx[331], cy[85], cx[332], cy[94], 0xFF51504E);
        p.fill(cx[20], cy[86], cx[29], cy[87], 0xFF51504E);

        p.fill(cx[350], cy[90], cx[351], cy[99], 0xFF61625E);

        p.fill(cx[326], cy[94], cx[329], cy[97], 0xFF2F3336);

        p.fill(cx[346], cy[101], cx[347], cy[110], 0xFF51504E);

        p.fill(cx[347], cy[101], cx[350], cy[104], 0xFF565B54);

        p.fill(cx[329], cy[102], cx[330], cy[111], 0xFF51504E);

        p.fill(cx[11], cy[112], cx[12], cy[121], 0xFF565B54);

        p.fill(cx[17], cy[112], cx[20], cy[115], 0xFF2F3336);

        p.fill(cx[21], cy[117], cx[30], cy[118], 0xFF202423);

        p.fill(cx[337], cy[120], cx[340], cy[123], 0xFF51504E);

        p.fill(cx[352], cy[131], cx[353], cy[140], 0xFF565B54);

        p.fill(cx[28], cy[138], cx[29], cy[147], 0xFF51504E);

        p.fill(cx[40], cy[150], cx[41], cy[159], 0xFF3C3F3B);

        p.fill(cx[347], cy[152], cx[350], cy[155], 0xFF676763);

        p.fill(cx[356], cy[162], cx[359], cy[165], 0xFF51504E);

        p.fill(cx[348], cy[163], cx[349], cy[172], 0xFF676763);

        p.fill(cx[342], cy[165], cx[343], cy[174], 0xFF3C3F3B);

        p.fill(cx[343], cy[166], cx[346], cy[169], 0xFF2F3336);

        p.fill(cx[8], cy[169], cx[11], cy[172], 0xFF61625E);

        p.fill(cx[334], cy[169], cx[335], cy[178], 0xFF3C3F3B);

        p.fill(cx[321], cy[172], cx[322], cy[181], 0xFF2F3336);

        p.fill(cx[323], cy[173], cx[326], cy[176], 0xFF3C3F3B);

        p.fill(cx[42], cy[175], cx[45], cy[178], 0xFF2F3336);

        p.fill(cx[105], cy[177], cx[106], cy[186], 0xFF202423);

        p.fill(cx[141], cy[178], cx[150], cy[179], 0xFF42473F);

        p.fill(cx[160], cy[178], cx[161], cy[187], 0xFF51504E);

        p.fill(cx[184], cy[178], cx[185], cy[187], 0xFF202423);
        p.fill(cx[259], cy[178], cx[260], cy[187], 0xFF202423);

        p.fill(cx[4], cy[179], cx[7], cy[182], 0xFFA5A49E);

        p.fill(cx[95], cy[179], cx[98], cy[182], 0xFF42473F);

        p.fill(cx[274], cy[179], cx[275], cy[188], 0xFF51504E);

        p.fill(cx[9], cy[180], cx[10], cy[189], 0xFF2F3336);

        p.fill(cx[336], cy[182], cx[345], cy[183], 0xFF202423);

        p.fill(cx[190], cy[184], cx[193], cy[187], 0xFF42473F);

        p.fill(cx[204], cy[184], cx[205], cy[193], 0xFF2F3336);
        p.fill(cx[206], cy[184], cx[207], cy[193], 0xFF2F3336);

        p.fill(cx[85], cy[188], cx[94], cy[189], 0xFF202423);

        p.fill(cx[166], cy[190], cx[169], cy[193], 0xFF2F3336);
        p.fill(cx[218], cy[190], cx[221], cy[193], 0xFF2F3336);
        p.fill(cx[242], cy[190], cx[245], cy[193], 0xFF2F3336);

        p.fill(cx[9], cy[194], cx[10], cy[203], 0xFF42473F);

        p.fill(cx[54], cy[194], cx[55], cy[203], 0xFF3C3F3B);

        p.fill(cx[285], cy[194], cx[286], cy[203], 0xFF42473F);
        p.fill(cx[287], cy[194], cx[288], cy[203], 0xFF42473F);

        p.fill(cx[124], cy[195], cx[133], cy[196], 0xFF3C3F3B);

        p.fill(cx[12], cy[0], cx[20], cy[1], 0xFF565B54);

        p.fill(cx[214], cy[0], cx[216], cy[4], 0xFF676763);
        p.fill(cx[7], cy[2], cx[8], cy[10], 0xFF676763);

        p.fill(cx[8], cy[2], cx[12], cy[4], 0xFF61625E);
        p.fill(cx[111], cy[2], cx[115], cy[4], 0xFF61625E);
        p.fill(cx[308], cy[2], cx[316], cy[3], 0xFF61625E);
        p.fill(cx[323], cy[2], cx[331], cy[3], 0xFF61625E);

        p.fill(cx[349], cy[2], cx[357], cy[3], 0xFF676763);

        p.fill(cx[86], cy[3], cx[88], cy[7], 0xFF51504E);

        p.fill(cx[237], cy[3], cx[241], cy[5], 0xFF61625E);
        p.fill(cx[321], cy[3], cx[329], cy[4], 0xFF61625E);

        p.fill(cx[355], cy[3], cx[357], cy[7], 0xFF6D6A64);

        p.fill(cx[22], cy[4], cx[30], cy[5], 0xFF51504E);
        p.fill(cx[82], cy[5], cx[86], cy[7], 0xFF51504E);
        p.fill(cx[89], cy[5], cx[93], cy[7], 0xFF51504E);

        p.fill(cx[28], cy[7], cx[29], cy[15], 0xFF565B54);
        p.fill(cx[130], cy[7], cx[138], cy[8], 0xFF565B54);

        p.fill(cx[337], cy[7], cx[338], cy[15], 0xFF3C3F3B);

        p.fill(cx[339], cy[7], cx[347], cy[8], 0xFF42473F);

        p.fill(cx[356], cy[7], cx[360], cy[9], 0xFF797D7C);

        p.fill(cx[22], cy[8], cx[26], cy[10], 0xFF3C3F3B);

        p.fill(cx[290], cy[8], cx[298], cy[9], 0xFF42473F);

        p.fill(cx[1], cy[10], cx[5], cy[12], 0xFF797D7C);

        p.fill(cx[24], cy[11], cx[25], cy[19], 0xFF2F3336);

        p.fill(cx[116], cy[11], cx[124], cy[12], 0xFF51504E);

        p.fill(cx[192], cy[11], cx[200], cy[12], 0xFF565B54);
        p.fill(cx[214], cy[11], cx[222], cy[12], 0xFF565B54);

        p.fill(cx[230], cy[11], cx[231], cy[19], 0xFF3C3F3B);
        p.fill(cx[255], cy[11], cx[256], cy[19], 0xFF3C3F3B);
        p.fill(cx[325], cy[11], cx[327], cy[15], 0xFF3C3F3B);

        p.fill(cx[58], cy[12], cx[59], cy[20], 0xFF202423);

        p.fill(cx[111], cy[12], cx[115], cy[14], 0xFF42473F);
        p.fill(cx[273], cy[12], cx[274], cy[20], 0xFF42473F);

        p.fill(cx[350], cy[12], cx[351], cy[20], 0xFF3C3F3B);

        p.fill(cx[119], cy[13], cx[123], cy[15], 0xFF42473F);
        p.fill(cx[212], cy[13], cx[213], cy[21], 0xFF42473F);
        p.fill(cx[220], cy[13], cx[224], cy[15], 0xFF42473F);

        p.fill(cx[300], cy[13], cx[301], cy[21], 0xFF51504E);
        p.fill(cx[113], cy[14], cx[117], cy[16], 0xFF51504E);

        p.fill(cx[81], cy[15], cx[82], cy[23], 0xFF3C3F3B);

        p.fill(cx[126], cy[15], cx[127], cy[23], 0xFF2F3336);
        p.fill(cx[337], cy[15], cx[339], cy[19], 0xFF2F3336);

        p.fill(cx[135], cy[17], cx[137], cy[21], 0xFF42473F);

        p.fill(cx[284], cy[17], cx[285], cy[25], 0xFF3C3F3B);

        p.fill(cx[62], cy[18], cx[64], cy[22], 0xFF51504E);

        p.fill(cx[315], cy[19], cx[317], cy[23], 0xFF3C3F3B);

        p.fill(cx[337], cy[19], cx[345], cy[20], 0xFF202423);

        p.fill(cx[0], cy[20], cx[4], cy[22], 0xFF6D6A64);

        p.fill(cx[182], cy[20], cx[184], cy[24], 0xFF3C3F3B);

        p.fill(cx[354], cy[21], cx[355], cy[29], 0xFF676763);

        p.fill(cx[350], cy[22], cx[352], cy[26], 0xFF6D6A64);

        p.fill(cx[48], cy[23], cx[50], cy[27], 0xFF3C3F3B);
        p.fill(cx[43], cy[24], cx[45], cy[28], 0xFF3C3F3B);
        p.fill(cx[46], cy[24], cx[48], cy[28], 0xFF3C3F3B);

        p.fill(cx[141], cy[25], cx[145], cy[27], 0xFF2F3336);

        p.fill(cx[317], cy[25], cx[319], cy[29], 0xFF3C3F3B);

        p.fill(cx[75], cy[26], cx[79], cy[28], 0xFF2F3336);
        p.fill(cx[84], cy[26], cx[88], cy[28], 0xFF2F3336);
        p.fill(cx[188], cy[26], cx[192], cy[28], 0xFF2F3336);
        p.fill(cx[218], cy[26], cx[222], cy[28], 0xFF2F3336);

        p.fill(cx[38], cy[27], cx[40], cy[31], 0xFF3C3F3B);

        p.fill(cx[92], cy[27], cx[100], cy[28], 0xFF2F3336);

        p.fill(cx[0], cy[28], cx[2], cy[32], 0xFF61625E);

        p.fill(cx[321], cy[28], cx[322], cy[36], 0xFF3C3F3B);

        p.fill(cx[350], cy[28], cx[351], cy[36], 0xFF6D6A64);

        p.fill(cx[38], cy[32], cx[39], cy[40], 0xFF2F3336);

        p.fill(cx[354], cy[32], cx[355], cy[40], 0xFF676763);

        p.fill(cx[355], cy[32], cx[356], cy[40], 0xFF42473F);

        p.fill(cx[328], cy[33], cx[329], cy[41], 0xFF3C3F3B);
        p.fill(cx[17], cy[37], cx[19], cy[41], 0xFF3C3F3B);

        p.fill(cx[36], cy[39], cx[38], cy[43], 0xFF2F3336);

        p.fill(cx[351], cy[45], cx[352], cy[53], 0xFF61625E);

        p.fill(cx[13], cy[49], cx[14], cy[57], 0xFF2F3336);
        p.fill(cx[32], cy[61], cx[34], cy[65], 0xFF2F3336);

        p.fill(cx[19], cy[64], cx[27], cy[65], 0xFF676763);

        p.fill(cx[351], cy[65], cx[353], cy[69], 0xFF51504E);

        p.fill(cx[29], cy[66], cx[30], cy[74], 0xFF565B54);

        p.fill(cx[25], cy[71], cx[27], cy[75], 0xFF51504E);
        p.fill(cx[338], cy[84], cx[340], cy[88], 0xFF51504E);
        p.fill(cx[329], cy[85], cx[330], cy[93], 0xFF51504E);
        p.fill(cx[22], cy[89], cx[24], cy[93], 0xFF51504E);

        p.fill(cx[326], cy[97], cx[328], cy[101], 0xFF2F3336);

        p.fill(cx[328], cy[97], cx[336], cy[98], 0xFF202423);

        p.fill(cx[22], cy[100], cx[30], cy[101], 0xFF676763);

        p.fill(cx[330], cy[103], cx[332], cy[107], 0xFF51504E);

        p.fill(cx[330], cy[107], cx[332], cy[111], 0xFF42473F);

        p.fill(cx[11], cy[108], cx[13], cy[112], 0xFF565B54);
        p.fill(cx[330], cy[111], cx[338], cy[112], 0xFF565B54);
        p.fill(cx[10], cy[115], cx[11], cy[123], 0xFF565B54);

        p.fill(cx[335], cy[119], cx[337], cy[123], 0xFF51504E);
        p.fill(cx[6], cy[132], cx[7], cy[140], 0xFF51504E);

        p.fill(cx[334], cy[132], cx[342], cy[133], 0xFF202423);

        p.fill(cx[19], cy[133], cx[23], cy[135], 0xFF2F3336);
        p.fill(cx[32], cy[133], cx[34], cy[137], 0xFF2F3336);
        p.fill(cx[329], cy[133], cx[333], cy[135], 0xFF2F3336);
        p.fill(cx[338], cy[133], cx[342], cy[135], 0xFF2F3336);

        p.fill(cx[19], cy[135], cx[27], cy[136], 0xFF202423);

        p.fill(cx[34], cy[135], cx[35], cy[143], 0xFF42473F);

        p.fill(cx[26], cy[142], cx[28], cy[146], 0xFF51504E);

        p.fill(cx[346], cy[144], cx[347], cy[152], 0xFF3C3F3B);

        p.fill(cx[21], cy[147], cx[29], cy[148], 0xFF565B54);

        p.fill(cx[355], cy[149], cx[356], cy[157], 0xFF51504E);

        p.fill(cx[352], cy[150], cx[353], cy[158], 0xFF3C3F3B);

        p.fill(cx[354], cy[150], cx[355], cy[158], 0xFF51504E);

        p.fill(cx[34], cy[151], cx[35], cy[159], 0xFF2F3336);

        p.fill(cx[355], cy[158], cx[356], cy[166], 0xFF3C3F3B);

        p.fill(cx[353], cy[160], cx[354], cy[168], 0xFF42473F);

        p.fill(cx[345], cy[161], cx[347], cy[165], 0xFF3C3F3B);

        p.fill(cx[30], cy[162], cx[31], cy[170], 0xFF2F3336);

        p.fill(cx[334], cy[164], cx[336], cy[168], 0xFF3C3F3B);

        p.fill(cx[349], cy[165], cx[351], cy[169], 0xFF676763);

        p.fill(cx[134], cy[166], cx[138], cy[168], 0xFF42473F);
        p.fill(cx[188], cy[166], cx[192], cy[168], 0xFF42473F);

        p.fill(cx[310], cy[166], cx[314], cy[168], 0xFF51504E);
        p.fill(cx[75], cy[167], cx[79], cy[169], 0xFF51504E);

        p.fill(cx[253], cy[168], cx[261], cy[169], 0xFF42473F);

        p.fill(cx[133], cy[169], cx[135], cy[173], 0xFF2F3336);
        p.fill(cx[238], cy[169], cx[240], cy[173], 0xFF2F3336);
        p.fill(cx[322], cy[171], cx[326], cy[173], 0xFF2F3336);

        p.fill(cx[349], cy[171], cx[351], cy[175], 0xFF61625E);
        p.fill(cx[356], cy[171], cx[360], cy[173], 0xFF61625E);

        p.fill(cx[140], cy[174], cx[144], cy[176], 0xFF2F3336);
        p.fill(cx[150], cy[174], cx[152], cy[178], 0xFF2F3336);
        p.fill(cx[201], cy[175], cx[203], cy[179], 0xFF2F3336);
        p.fill(cx[156], cy[176], cx[160], cy[178], 0xFF2F3336);
        p.fill(cx[203], cy[176], cx[211], cy[177], 0xFF2F3336);

        p.fill(cx[165], cy[178], cx[173], cy[179], 0xFF42473F);

        p.fill(cx[251], cy[178], cx[255], cy[180], 0xFF2F3336);

        p.fill(cx[60], cy[179], cx[64], cy[181], 0xFF42473F);

        p.fill(cx[64], cy[179], cx[68], cy[181], 0xFF51504E);

        p.fill(cx[74], cy[179], cx[75], cy[187], 0xFF202423);

        p.fill(cx[352], cy[179], cx[356], cy[181], 0xFFA5A49E);

        p.fill(cx[110], cy[180], cx[111], cy[188], 0xFF51504E);
        p.fill(cx[186], cy[180], cx[187], cy[188], 0xFF51504E);
        p.fill(cx[261], cy[180], cx[262], cy[188], 0xFF51504E);

        p.fill(cx[323], cy[180], cx[331], cy[181], 0xFF3C3F3B);
        p.fill(cx[32], cy[184], cx[33], cy[192], 0xFF3C3F3B);
        p.fill(cx[41], cy[184], cx[43], cy[188], 0xFF3C3F3B);

        p.fill(cx[62], cy[184], cx[66], cy[186], 0xFF51504E);
        p.fill(cx[167], cy[185], cx[171], cy[187], 0xFF51504E);

        p.fill(cx[255], cy[185], cx[256], cy[193], 0xFF2F3336);
        p.fill(cx[343], cy[186], cx[344], cy[194], 0xFF2F3336);

        p.fill(cx[187], cy[187], cx[195], cy[188], 0xFF51504E);
        p.fill(cx[236], cy[187], cx[244], cy[188], 0xFF51504E);

        p.fill(cx[354], cy[187], cx[358], cy[189], 0xFF676763);

        p.fill(cx[71], cy[189], cx[73], cy[193], 0xFF2F3336);
        p.fill(cx[96], cy[189], cx[98], cy[193], 0xFF2F3336);
        p.fill(cx[148], cy[189], cx[150], cy[193], 0xFF2F3336);
        p.fill(cx[169], cy[189], cx[171], cy[193], 0xFF2F3336);
        p.fill(cx[199], cy[189], cx[201], cy[193], 0xFF2F3336);
        p.fill(cx[287], cy[189], cx[289], cy[193], 0xFF2F3336);
        p.fill(cx[26], cy[191], cx[27], cy[199], 0xFF2F3336);

        p.fill(cx[90], cy[193], cx[94], cy[195], 0xFF42473F);

        p.fill(cx[4], cy[194], cx[6], cy[198], 0xFF51504E);

        p.fill(cx[19], cy[195], cx[20], cy[203], 0xFF3C3F3B);
        p.fill(cx[55], cy[195], cx[56], cy[203], 0xFF3C3F3B);
        p.fill(cx[57], cy[195], cx[58], cy[203], 0xFF3C3F3B);

        p.fill(cx[111], cy[195], cx[112], cy[203], 0xFF42473F);
        p.fill(cx[119], cy[195], cx[120], cy[203], 0xFF42473F);
        p.fill(cx[123], cy[195], cx[124], cy[203], 0xFF42473F);

        p.fill(cx[330], cy[195], cx[331], cy[203], 0xFF2F3336);

        p.fill(cx[265], cy[196], cx[267], cy[200], 0xFF42473F);

        p.fill(cx[319], cy[196], cx[321], cy[200], 0xFF3C3F3B);

        p.fill(cx[88], cy[197], cx[92], cy[199], 0xFF42473F);
        p.fill(cx[281], cy[197], cx[285], cy[199], 0xFF42473F);
        p.fill(cx[283], cy[199], cx[285], cy[203], 0xFF42473F);

        p.fill(cx[313], cy[199], cx[315], cy[203], 0xFF2F3336);

        p.fill(cx[356], cy[199], cx[358], cy[203], 0xFF42473F);
        p.fill(cx[227], cy[202], cx[235], cy[203], 0xFF42473F);

        p.fill(cx[271], cy[202], cx[279], cy[203], 0xFF3C3F3B);

        p.fill(cx[335], cy[5], cx[342], cy[6], 0xFF42473F);

        p.fill(cx[132], cy[6], cx[139], cy[7], 0xFF61625E);

        p.fill(cx[338], cy[6], cx[345], cy[7], 0xFF42473F);

        p.fill(cx[117], cy[7], cx[124], cy[8], 0xFF61625E);

        p.fill(cx[80], cy[8], cx[87], cy[9], 0xFF3C3F3B);
        p.fill(cx[301], cy[9], cx[308], cy[10], 0xFF3C3F3B);

        p.fill(cx[323], cy[9], cx[330], cy[10], 0xFF42473F);

        p.fill(cx[67], cy[10], cx[74], cy[11], 0xFF202423);

        p.fill(cx[73], cy[11], cx[74], cy[18], 0xFF2F3336);

        p.fill(cx[117], cy[12], cx[124], cy[13], 0xFF42473F);

        p.fill(cx[174], cy[12], cx[175], cy[19], 0xFF51504E);

        p.fill(cx[86], cy[13], cx[93], cy[14], 0xFF42473F);

        p.fill(cx[180], cy[13], cx[181], cy[20], 0xFF3C3F3B);

        p.fill(cx[202], cy[13], cx[203], cy[20], 0xFF2F3336);

        p.fill(cx[261], cy[13], cx[268], cy[14], 0xFF42473F);
        p.fill(cx[248], cy[14], cx[249], cy[21], 0xFF42473F);

        p.fill(cx[56], cy[19], cx[57], cy[26], 0xFF3C3F3B);

        p.fill(cx[5], cy[20], cx[12], cy[21], 0xFF6D6A64);

        p.fill(cx[6], cy[21], cx[7], cy[28], 0xFF676763);

        p.fill(cx[323], cy[21], cx[324], cy[28], 0xFF3C3F3B);

        p.fill(cx[293], cy[22], cx[300], cy[23], 0xFF202423);

        p.fill(cx[325], cy[22], cx[326], cy[29], 0xFF3C3F3B);

        p.fill(cx[355], cy[22], cx[356], cy[29], 0xFF676763);

        p.fill(cx[328], cy[25], cx[329], cy[32], 0xFF3C3F3B);

        p.fill(cx[8], cy[27], cx[9], cy[34], 0xFF676763);

        p.fill(cx[323], cy[29], cx[324], cy[36], 0xFF3C3F3B);
        p.fill(cx[34], cy[30], cx[35], cy[37], 0xFF3C3F3B);

        p.fill(cx[4], cy[33], cx[5], cy[40], 0xFF42473F);

        p.fill(cx[27], cy[42], cx[34], cy[43], 0xFF202423);

        p.fill(cx[8], cy[46], cx[9], cy[53], 0xFF3C3F3B);

        p.fill(cx[30], cy[47], cx[31], cy[54], 0xFF51504E);
        p.fill(cx[24], cy[51], cx[25], cy[58], 0xFF51504E);

        p.fill(cx[334], cy[64], cx[341], cy[65], 0xFF61625E);
        p.fill(cx[350], cy[80], cx[351], cy[87], 0xFF61625E);

        p.fill(cx[42], cy[81], cx[43], cy[88], 0xFF3C3F3B);

        p.fill(cx[30], cy[86], cx[31], cy[93], 0xFF51504E);

        p.fill(cx[332], cy[93], cx[339], cy[94], 0xFF2F3336);
        p.fill(cx[329], cy[96], cx[336], cy[97], 0xFF2F3336);

        p.fill(cx[351], cy[96], cx[352], cy[103], 0xFF61625E);

        p.fill(cx[34], cy[106], cx[35], cy[113], 0xFF42473F);

        p.fill(cx[10], cy[107], cx[11], cy[114], 0xFF565B54);

        p.fill(cx[342], cy[112], cx[343], cy[119], 0xFF2F3336);

        p.fill(cx[346], cy[118], cx[347], cy[125], 0xFF42473F);

        p.fill(cx[329], cy[123], cx[330], cy[130], 0xFF51504E);

        p.fill(cx[334], cy[131], cx[341], cy[132], 0xFF2F3336);

        p.fill(cx[21], cy[136], cx[28], cy[137], 0xFF676763);

        p.fill(cx[325], cy[152], cx[326], cy[159], 0xFF2F3336);
        p.fill(cx[22], cy[160], cx[29], cy[161], 0xFF2F3336);

        p.fill(cx[359], cy[161], cx[360], cy[168], 0xFF51504E);

        p.fill(cx[99], cy[167], cx[106], cy[168], 0xFF42473F);
        p.fill(cx[301], cy[167], cx[308], cy[168], 0xFF42473F);

        p.fill(cx[301], cy[168], cx[308], cy[169], 0xFF3C3F3B);

        p.fill(cx[12], cy[171], cx[13], cy[178], 0xFF2F3336);
        p.fill(cx[51], cy[175], cx[58], cy[176], 0xFF2F3336);

        p.fill(cx[161], cy[177], cx[168], cy[178], 0xFF51504E);

        p.fill(cx[206], cy[177], cx[207], cy[184], 0xFF202423);

        p.fill(cx[8], cy[178], cx[9], cy[185], 0xFF2F3336);

        p.fill(cx[150], cy[178], cx[151], cy[185], 0xFF202423);

        p.fill(cx[214], cy[178], cx[221], cy[179], 0xFF42473F);

        p.fill(cx[234], cy[179], cx[235], cy[186], 0xFF2F3336);

        p.fill(cx[196], cy[180], cx[197], cy[187], 0xFF42473F);
        p.fill(cx[238], cy[180], cx[239], cy[187], 0xFF42473F);

        p.fill(cx[249], cy[181], cx[250], cy[188], 0xFF51504E);

        p.fill(cx[323], cy[183], cx[330], cy[184], 0xFF3C3F3B);
        p.fill(cx[31], cy[185], cx[32], cy[192], 0xFF3C3F3B);

        p.fill(cx[181], cy[186], cx[182], cy[193], 0xFF2F3336);

        p.fill(cx[86], cy[187], cx[93], cy[188], 0xFF51504E);
        p.fill(cx[262], cy[187], cx[269], cy[188], 0xFF51504E);

        p.fill(cx[29], cy[188], cx[30], cy[195], 0xFF2F3336);

        p.fill(cx[347], cy[192], cx[348], cy[199], 0xFF42473F);

        p.fill(cx[89], cy[195], cx[96], cy[196], 0xFF3C3F3B);
        p.fill(cx[112], cy[195], cx[119], cy[196], 0xFF3C3F3B);

        p.fill(cx[8], cy[196], cx[9], cy[203], 0xFF42473F);

        p.fill(cx[30], cy[196], cx[31], cy[203], 0xFF2F3336);

        p.fill(cx[56], cy[196], cx[57], cy[203], 0xFF3C3F3B);

        p.fill(cx[92], cy[196], cx[93], cy[203], 0xFF42473F);
        p.fill(cx[95], cy[196], cx[96], cy[203], 0xFF42473F);
        p.fill(cx[118], cy[196], cx[119], cy[203], 0xFF42473F);
        p.fill(cx[128], cy[196], cx[129], cy[203], 0xFF42473F);
        p.fill(cx[261], cy[196], cx[262], cy[203], 0xFF42473F);

        p.fill(cx[58], cy[198], cx[65], cy[199], 0xFF3C3F3B);

        p.fill(cx[131], cy[1], cx[132], cy[7], 0xFF61625E);

        p.fill(cx[317], cy[1], cx[318], cy[7], 0xFF676763);

        p.fill(cx[218], cy[2], cx[220], cy[5], 0xFF61625E);

        p.fill(cx[281], cy[3], cx[287], cy[4], 0xFF565B54);

        p.fill(cx[311], cy[3], cx[317], cy[4], 0xFF61625E);

        p.fill(cx[329], cy[3], cx[335], cy[4], 0xFF565B54);

        p.fill(cx[8], cy[4], cx[9], cy[10], 0xFF676763);

        p.fill(cx[31], cy[4], cx[34], cy[6], 0xFF51504E);
        p.fill(cx[80], cy[4], cx[86], cy[5], 0xFF51504E);

        p.fill(cx[321], cy[4], cx[323], cy[7], 0xFF676763);

        p.fill(cx[67], cy[5], cx[70], cy[7], 0xFF51504E);

        p.fill(cx[125], cy[5], cx[127], cy[8], 0xFF61625E);

        p.fill(cx[332], cy[5], cx[335], cy[7], 0xFF3C3F3B);

        p.fill(cx[1], cy[6], cx[7], cy[7], 0xFF676763);
        p.fill(cx[145], cy[6], cx[151], cy[7], 0xFF676763);

        p.fill(cx[1], cy[7], cx[3], cy[10], 0xFF6D6A64);

        p.fill(cx[16], cy[7], cx[18], cy[10], 0xFF42473F);

        p.fill(cx[101], cy[7], cx[107], cy[8], 0xFF61625E);

        p.fill(cx[155], cy[7], cx[161], cy[8], 0xFF565B54);

        p.fill(cx[274], cy[7], cx[280], cy[8], 0xFF51504E);

        p.fill(cx[282], cy[7], cx[283], cy[13], 0xFF42473F);
        p.fill(cx[342], cy[8], cx[348], cy[9], 0xFF42473F);
        p.fill(cx[343], cy[9], cx[349], cy[10], 0xFF42473F);

        p.fill(cx[324], cy[10], cx[325], cy[16], 0xFF3C3F3B);

        p.fill(cx[93], cy[11], cx[99], cy[12], 0xFF51504E);

        p.fill(cx[157], cy[11], cx[160], cy[13], 0xFF3C3F3B);

        p.fill(cx[213], cy[11], cx[214], cy[17], 0xFF51504E);
        p.fill(cx[66], cy[12], cx[69], cy[14], 0xFF51504E);

        p.fill(cx[71], cy[12], cx[73], cy[15], 0xFF42473F);

        p.fill(cx[103], cy[12], cx[104], cy[18], 0xFF2F3336);

        p.fill(cx[126], cy[12], cx[128], cy[15], 0xFF3C3F3B);

        p.fill(cx[140], cy[12], cx[143], cy[14], 0xFF51504E);

        p.fill(cx[143], cy[12], cx[149], cy[13], 0xFF42473F);

        p.fill(cx[182], cy[12], cx[185], cy[14], 0xFF2F3336);

        p.fill(cx[205], cy[12], cx[206], cy[18], 0xFF42473F);

        p.fill(cx[226], cy[12], cx[229], cy[14], 0xFF2F3336);

        p.fill(cx[232], cy[12], cx[234], cy[15], 0xFF3C3F3B);

        p.fill(cx[338], cy[12], cx[340], cy[15], 0xFF202423);

        p.fill(cx[23], cy[13], cx[24], cy[19], 0xFF2F3336);

        p.fill(cx[166], cy[13], cx[168], cy[16], 0xFF51504E);

        p.fill(cx[168], cy[13], cx[174], cy[14], 0xFF42473F);
        p.fill(cx[272], cy[13], cx[273], cy[19], 0xFF42473F);

        p.fill(cx[351], cy[13], cx[352], cy[19], 0xFF3C3F3B);

        p.fill(cx[352], cy[13], cx[354], cy[16], 0xFFD1D2C3);

        p.fill(cx[62], cy[14], cx[65], cy[16], 0xFF51504E);

        p.fill(cx[94], cy[14], cx[96], cy[17], 0xFF42473F);

        p.fill(cx[124], cy[14], cx[125], cy[20], 0xFF2F3336);

        p.fill(cx[186], cy[14], cx[192], cy[15], 0xFF42473F);

        p.fill(cx[336], cy[14], cx[337], cy[20], 0xFF2F3336);

        p.fill(cx[111], cy[15], cx[112], cy[21], 0xFF42473F);

        p.fill(cx[127], cy[15], cx[128], cy[21], 0xFF3C3F3B);

        p.fill(cx[138], cy[15], cx[140], cy[18], 0xFF51504E);

        p.fill(cx[223], cy[15], cx[224], cy[21], 0xFF42473F);

        p.fill(cx[293], cy[15], cx[295], cy[18], 0xFFFBFBFB);

        p.fill(cx[327], cy[15], cx[328], cy[21], 0xFF42473F);

        p.fill(cx[4], cy[16], cx[7], cy[18], 0xFF8A8C83);

        p.fill(cx[104], cy[16], cx[105], cy[22], 0xFF3C3F3B);

        p.fill(cx[186], cy[16], cx[187], cy[22], 0xFF42473F);
        p.fill(cx[317], cy[16], cx[319], cy[19], 0xFF42473F);

        p.fill(cx[340], cy[16], cx[343], cy[18], 0xFF3C3F3B);

        p.fill(cx[0], cy[17], cx[2], cy[20], 0xFF797D7C);

        p.fill(cx[182], cy[17], cx[184], cy[20], 0xFF2F3336);

        p.fill(cx[146], cy[18], cx[148], cy[21], 0xFF42473F);
        p.fill(cx[169], cy[18], cx[171], cy[21], 0xFF42473F);

        p.fill(cx[177], cy[18], cx[178], cy[24], 0xFF3C3F3B);

        p.fill(cx[221], cy[18], cx[223], cy[21], 0xFF42473F);

        p.fill(cx[232], cy[18], cx[233], cy[24], 0xFF3C3F3B);

        p.fill(cx[218], cy[19], cx[220], cy[22], 0xFF51504E);

        p.fill(cx[237], cy[19], cx[240], cy[21], 0xFF42473F);

        p.fill(cx[281], cy[19], cx[282], cy[25], 0xFF3C3F3B);
        p.fill(cx[282], cy[20], cx[283], cy[26], 0xFF3C3F3B);

        p.fill(cx[349], cy[20], cx[352], cy[22], 0xFF6D6A64);
        p.fill(cx[356], cy[20], cx[358], cy[23], 0xFF6D6A64);
        p.fill(cx[10], cy[21], cx[12], cy[24], 0xFF6D6A64);

        p.fill(cx[166], cy[21], cx[172], cy[22], 0xFF51504E);
        p.fill(cx[212], cy[21], cx[218], cy[22], 0xFF51504E);

        p.fill(cx[336], cy[21], cx[337], cy[27], 0xFF3C3F3B);

        p.fill(cx[59], cy[22], cx[65], cy[23], 0xFF2F3336);

        p.fill(cx[69], cy[22], cx[72], cy[24], 0xFF202423);

        p.fill(cx[328], cy[22], cx[331], cy[24], 0xFF3D3E3D);

        p.fill(cx[300], cy[23], cx[302], cy[26], 0xFF3C3F3B);
        p.fill(cx[41], cy[25], cx[43], cy[28], 0xFF3C3F3B);

        p.fill(cx[79], cy[25], cx[81], cy[28], 0xFF2F3336);
        p.fill(cx[82], cy[25], cx[84], cy[28], 0xFF2F3336);
        p.fill(cx[186], cy[25], cx[188], cy[28], 0xFF2F3336);
        p.fill(cx[104], cy[26], cx[107], cy[28], 0xFF2F3336);
        p.fill(cx[159], cy[26], cx[162], cy[28], 0xFF2F3336);

        p.fill(cx[9], cy[28], cx[10], cy[34], 0xFF676763);

        p.fill(cx[30], cy[28], cx[36], cy[29], 0xFF3C3F3B);

        p.fill(cx[2], cy[29], cx[5], cy[31], 0xFF61625E);
        p.fill(cx[354], cy[29], cx[360], cy[30], 0xFF61625E);

        p.fill(cx[349], cy[30], cx[350], cy[36], 0xFF6D6A64);

        p.fill(cx[31], cy[32], cx[32], cy[38], 0xFF3C3F3B);

        p.fill(cx[39], cy[34], cx[42], cy[36], 0xFF42473F);

        p.fill(cx[29], cy[35], cx[30], cy[41], 0xFF2F3336);
        p.fill(cx[330], cy[35], cx[331], cy[41], 0xFF2F3336);

        p.fill(cx[346], cy[36], cx[352], cy[37], 0xFF202423);

        p.fill(cx[33], cy[39], cx[36], cy[41], 0xFF3C3F3B);

        p.fill(cx[0], cy[41], cx[3], cy[43], 0xFF51504E);

        p.fill(cx[326], cy[42], cx[332], cy[43], 0xFF202423);

        p.fill(cx[0], cy[43], cx[2], cy[46], 0xFF51504E);

        p.fill(cx[19], cy[46], cx[25], cy[47], 0xFF676763);

        p.fill(cx[329], cy[46], cx[335], cy[47], 0xFF61625E);

        p.fill(cx[341], cy[51], cx[342], cy[57], 0xFF51504E);

        p.fill(cx[9], cy[52], cx[12], cy[54], 0xFF61625E);

        p.fill(cx[12], cy[56], cx[13], cy[62], 0xFF565B54);

        p.fill(cx[326], cy[58], cx[328], cy[61], 0xFF2F3336);

        p.fill(cx[23], cy[67], cx[26], cy[69], 0xFF51504E);
        p.fill(cx[329], cy[69], cx[335], cy[70], 0xFF51504E);
        p.fill(cx[338], cy[69], cx[339], cy[75], 0xFF51504E);

        p.fill(cx[341], cy[69], cx[342], cy[75], 0xFF676763);

        p.fill(cx[329], cy[70], cx[330], cy[76], 0xFF51504E);
        p.fill(cx[331], cy[70], cx[333], cy[73], 0xFF51504E);

        p.fill(cx[7], cy[81], cx[8], cy[87], 0xFF565B54);
        p.fill(cx[351], cy[81], cx[352], cy[87], 0xFF565B54);

        p.fill(cx[24], cy[87], cx[25], cy[93], 0xFF51504E);
        p.fill(cx[332], cy[87], cx[333], cy[93], 0xFF51504E);
        p.fill(cx[339], cy[88], cx[340], cy[94], 0xFF51504E);
        p.fill(cx[346], cy[93], cx[347], cy[99], 0xFF51504E);

        p.fill(cx[31], cy[94], cx[34], cy[96], 0xFF2F3336);
        p.fill(cx[341], cy[94], cx[343], cy[97], 0xFF2F3336);
        p.fill(cx[8], cy[96], cx[9], cy[102], 0xFF2F3336);

        p.fill(cx[29], cy[96], cx[32], cy[98], 0xFF202423);

        p.fill(cx[350], cy[99], cx[351], cy[105], 0xFF565B54);

        p.fill(cx[7], cy[102], cx[8], cy[108], 0xFF61625E);
        p.fill(cx[349], cy[104], cx[350], cy[110], 0xFF61625E);

        p.fill(cx[335], cy[107], cx[338], cy[109], 0xFF51504E);

        p.fill(cx[326], cy[112], cx[329], cy[114], 0xFF2F3336);
        p.fill(cx[8], cy[115], cx[9], cy[121], 0xFF2F3336);

        p.fill(cx[41], cy[121], cx[43], cy[124], 0xFF3C3F3B);

        p.fill(cx[24], cy[123], cx[25], cy[129], 0xFF51504E);
        p.fill(cx[26], cy[123], cx[27], cy[129], 0xFF51504E);

        p.fill(cx[330], cy[123], cx[331], cy[129], 0xFF42473F);

        p.fill(cx[7], cy[124], cx[8], cy[130], 0xFF565B54);

        p.fill(cx[326], cy[130], cx[328], cy[133], 0xFF2F3336);

        p.fill(cx[331], cy[131], cx[334], cy[133], 0xFF202423);

        p.fill(cx[7], cy[134], cx[8], cy[140], 0xFF51504E);

        p.fill(cx[326], cy[134], cx[329], cy[136], 0xFF2F3336);

        p.fill(cx[334], cy[137], cx[335], cy[143], 0xFF51504E);

        p.fill(cx[335], cy[137], cx[341], cy[138], 0xFF2F3336);

        p.fill(cx[332], cy[138], cx[334], cy[141], 0xFF51504E);
        p.fill(cx[339], cy[139], cx[340], cy[145], 0xFF51504E);

        p.fill(cx[330], cy[141], cx[331], cy[147], 0xFF42473F);

        p.fill(cx[351], cy[143], cx[352], cy[149], 0xFF565B54);
        p.fill(cx[354], cy[146], cx[357], cy[148], 0xFF565B54);

        p.fill(cx[11], cy[153], cx[12], cy[159], 0xFF61625E);

        p.fill(cx[14], cy[153], cx[15], cy[159], 0xFF2F3336);
        p.fill(cx[326], cy[153], cx[327], cy[159], 0xFF2F3336);
        p.fill(cx[342], cy[153], cx[343], cy[159], 0xFF2F3336);

        p.fill(cx[5], cy[155], cx[6], cy[161], 0xFF61625E);

        p.fill(cx[319], cy[160], cx[321], cy[163], 0xFF3C3F3B);

        p.fill(cx[348], cy[160], cx[351], cy[162], 0xFF61625E);

        p.fill(cx[23], cy[161], cx[29], cy[162], 0xFF2F3336);

        p.fill(cx[349], cy[162], cx[351], cy[165], 0xFF61625E);

        p.fill(cx[26], cy[163], cx[27], cy[169], 0xFF2F3336);

        p.fill(cx[357], cy[165], cx[359], cy[168], 0xFF51504E);

        p.fill(cx[143], cy[166], cx[149], cy[167], 0xFF42473F);

        p.fill(cx[0], cy[167], cx[2], cy[170], 0xFF565B54);
        p.fill(cx[3], cy[167], cx[5], cy[170], 0xFF565B54);

        p.fill(cx[54], cy[167], cx[57], cy[169], 0xFF51504E);
        p.fill(cx[96], cy[167], cx[99], cy[169], 0xFF51504E);

        p.fill(cx[298], cy[167], cx[301], cy[169], 0xFF3C3F3B);

        p.fill(cx[69], cy[168], cx[75], cy[169], 0xFF51504E);

        p.fill(cx[283], cy[168], cx[289], cy[169], 0xFF42473F);

        p.fill(cx[358], cy[168], cx[360], cy[171], 0xFF565B54);

        p.fill(cx[11], cy[169], cx[12], cy[175], 0xFF676763);

        p.fill(cx[355], cy[169], cx[358], cy[171], 0xFF565B54);

        p.fill(cx[30], cy[170], cx[33], cy[172], 0xFF3C3F3B);

        p.fill(cx[347], cy[170], cx[348], cy[176], 0xFF676763);

        p.fill(cx[28], cy[171], cx[29], cy[177], 0xFF2F3336);

        p.fill(cx[4], cy[174], cx[6], cy[177], 0xFF676763);

        p.fill(cx[225], cy[174], cx[227], cy[177], 0xFF2F3336);

        p.fill(cx[248], cy[174], cx[254], cy[175], 0xFF202423);

        p.fill(cx[310], cy[174], cx[316], cy[175], 0xFF2F3336);

        p.fill(cx[354], cy[174], cx[356], cy[177], 0xFF676763);

        p.fill(cx[33], cy[175], cx[34], cy[181], 0xFF3C3F3B);

        p.fill(cx[73], cy[175], cx[79], cy[176], 0xFF2F3336);
        p.fill(cx[248], cy[175], cx[254], cy[176], 0xFF2F3336);
        p.fill(cx[275], cy[175], cx[278], cy[177], 0xFF2F3336);
        p.fill(cx[280], cy[175], cx[286], cy[176], 0xFF2F3336);
        p.fill(cx[302], cy[175], cx[308], cy[176], 0xFF2F3336);

        p.fill(cx[310], cy[175], cx[316], cy[176], 0xFF202423);

        p.fill(cx[350], cy[175], cx[352], cy[178], 0xFF61625E);

        p.fill(cx[179], cy[176], cx[181], cy[179], 0xFF202423);
    }

    private static void part3(Paint p, int[] cx, int[] cy) {

        p.fill(cx[0], cy[177], cx[3], cy[179], 0xFF797D7C);

        p.fill(cx[59], cy[177], cx[65], cy[178], 0xFF51504E);
        p.fill(cx[186], cy[177], cx[188], cy[180], 0xFF51504E);
        p.fill(cx[190], cy[177], cx[196], cy[178], 0xFF51504E);
        p.fill(cx[211], cy[177], cx[217], cy[178], 0xFF51504E);
        p.fill(cx[116], cy[178], cx[119], cy[180], 0xFF51504E);

        p.fill(cx[181], cy[178], cx[184], cy[180], 0xFF2F3336);

        p.fill(cx[189], cy[178], cx[195], cy[179], 0xFF42473F);

        p.fill(cx[204], cy[178], cx[205], cy[184], 0xFF202423);

        p.fill(cx[261], cy[178], cx[264], cy[180], 0xFF42473F);

        p.fill(cx[43], cy[179], cx[44], cy[185], 0xFF2F3336);

        p.fill(cx[141], cy[179], cx[144], cy[181], 0xFF51504E);

        p.fill(cx[144], cy[179], cx[150], cy[180], 0xFF42473F);
        p.fill(cx[168], cy[179], cx[171], cy[181], 0xFF42473F);
        p.fill(cx[220], cy[179], cx[223], cy[181], 0xFF42473F);

        p.fill(cx[326], cy[179], cx[332], cy[180], 0xFF3C3F3B);

        p.fill(cx[145], cy[180], cx[148], cy[182], 0xFF42473F);

        p.fill(cx[181], cy[180], cx[182], cy[186], 0xFF202423);

        p.fill(cx[187], cy[180], cx[193], cy[181], 0xFF42473F);

        p.fill(cx[351], cy[180], cx[352], cy[186], 0xFF2F3336);

        p.fill(cx[60], cy[181], cx[61], cy[187], 0xFF42473F);
        p.fill(cx[111], cy[181], cx[112], cy[187], 0xFF42473F);
        p.fill(cx[162], cy[181], cx[163], cy[187], 0xFF42473F);
        p.fill(cx[195], cy[181], cx[196], cy[187], 0xFF42473F);
        p.fill(cx[212], cy[181], cx[213], cy[187], 0xFF42473F);
        p.fill(cx[262], cy[181], cx[263], cy[187], 0xFF42473F);

        p.fill(cx[30], cy[182], cx[32], cy[185], 0xFF3C3F3B);

        p.fill(cx[147], cy[182], cx[148], cy[188], 0xFF42473F);

        p.fill(cx[179], cy[182], cx[180], cy[188], 0xFF202423);

        p.fill(cx[221], cy[182], cx[224], cy[184], 0xFF51504E);

        p.fill(cx[230], cy[182], cx[231], cy[188], 0xFF3C3F3B);

        p.fill(cx[357], cy[182], cx[360], cy[184], 0xFF8A8C83);

        p.fill(cx[317], cy[183], cx[320], cy[185], 0xFF2F3336);

        p.fill(cx[4], cy[184], cx[7], cy[186], 0xFF8B9494);

        p.fill(cx[116], cy[184], cx[118], cy[187], 0xFF42473F);
        p.fill(cx[140], cy[184], cx[142], cy[187], 0xFF42473F);
        p.fill(cx[163], cy[184], cx[166], cy[186], 0xFF42473F);

        p.fill(cx[16], cy[185], cx[22], cy[186], 0xFF202423);

        p.fill(cx[315], cy[185], cx[318], cy[187], 0xFF3C3F3B);

        p.fill(cx[16], cy[186], cx[17], cy[192], 0xFF2F3336);

        p.fill(cx[164], cy[186], cx[167], cy[188], 0xFF51504E);

        p.fill(cx[318], cy[186], cx[319], cy[192], 0xFF3C3F3B);

        p.fill(cx[3], cy[187], cx[6], cy[189], 0xFF676763);

        p.fill(cx[74], cy[187], cx[75], cy[193], 0xFF2F3336);

        p.fill(cx[136], cy[187], cx[142], cy[188], 0xFF51504E);

        p.fill(cx[184], cy[187], cx[185], cy[193], 0xFF2F3336);
        p.fill(cx[201], cy[187], cx[202], cy[193], 0xFF2F3336);
        p.fill(cx[205], cy[187], cx[206], cy[193], 0xFF2F3336);
        p.fill(cx[234], cy[187], cx[235], cy[193], 0xFF2F3336);
        p.fill(cx[259], cy[187], cx[260], cy[193], 0xFF2F3336);

        p.fill(cx[0], cy[188], cx[3], cy[190], 0xFF676763);

        p.fill(cx[56], cy[188], cx[57], cy[194], 0xFF2F3336);
        p.fill(cx[330], cy[188], cx[331], cy[194], 0xFF2F3336);
        p.fill(cx[39], cy[189], cx[42], cy[191], 0xFF2F3336);

        p.fill(cx[313], cy[189], cx[315], cy[192], 0xFF3C3F3B);
        p.fill(cx[321], cy[189], cx[323], cy[192], 0xFF3C3F3B);

        p.fill(cx[60], cy[190], cx[62], cy[193], 0xFF2F3336);
        p.fill(cx[66], cy[190], cx[68], cy[193], 0xFF2F3336);
        p.fill(cx[69], cy[190], cx[71], cy[193], 0xFF2F3336);
        p.fill(cx[118], cy[190], cx[120], cy[193], 0xFF2F3336);

        p.fill(cx[351], cy[190], cx[352], cy[196], 0xFF51504E);

        p.fill(cx[354], cy[190], cx[360], cy[191], 0xFF61625E);

        p.fill(cx[352], cy[191], cx[353], cy[197], 0xFF51504E);

        p.fill(cx[36], cy[192], cx[37], cy[198], 0xFF2F3336);

        p.fill(cx[354], cy[192], cx[360], cy[193], 0xFF565B54);

        p.fill(cx[12], cy[193], cx[13], cy[199], 0xFF42473F);
        p.fill(cx[288], cy[193], cx[289], cy[199], 0xFF42473F);
        p.fill(cx[96], cy[195], cx[97], cy[201], 0xFF42473F);

        p.fill(cx[188], cy[195], cx[194], cy[196], 0xFF3C3F3B);
        p.fill(cx[270], cy[195], cx[273], cy[197], 0xFF3C3F3B);
        p.fill(cx[276], cy[195], cx[277], cy[201], 0xFF3C3F3B);

        p.fill(cx[358], cy[195], cx[360], cy[198], 0xFF51504E);

        p.fill(cx[75], cy[196], cx[76], cy[202], 0xFF42473F);

        p.fill(cx[279], cy[196], cx[281], cy[199], 0xFF3C3F3B);

        p.fill(cx[7], cy[197], cx[8], cy[203], 0xFF42473F);

        p.fill(cx[27], cy[197], cx[28], cy[203], 0xFF2F3336);

        p.fill(cx[126], cy[197], cx[127], cy[203], 0xFF42473F);

        p.fill(cx[292], cy[197], cx[293], cy[203], 0xFF3D3E3D);

        p.fill(cx[308], cy[197], cx[309], cy[203], 0xFF3C3F3B);

        p.fill(cx[322], cy[197], cx[324], cy[200], 0xFF2F3336);

        p.fill(cx[49], cy[198], cx[51], cy[201], 0xFF3C3F3B);

        p.fill(cx[270], cy[198], cx[272], cy[201], 0xFF42473F);
        p.fill(cx[0], cy[200], cx[3], cy[202], 0xFF42473F);

        p.fill(cx[80], cy[200], cx[82], cy[203], 0xFF3C3F3B);

        p.fill(cx[12], cy[1], cx[13], cy[6], 0xFF61625E);

        p.fill(cx[220], cy[2], cx[221], cy[7], 0xFF676763);

        p.fill(cx[236], cy[2], cx[241], cy[3], 0xFF61625E);

        p.fill(cx[357], cy[2], cx[358], cy[7], 0xFF6D6A64);

        p.fill(cx[302], cy[4], cx[307], cy[5], 0xFF676763);

        p.fill(cx[311], cy[4], cx[316], cy[5], 0xFF61625E);
        p.fill(cx[323], cy[4], cx[328], cy[5], 0xFF61625E);

        p.fill(cx[329], cy[4], cx[334], cy[5], 0xFF565B54);

        p.fill(cx[349], cy[5], cx[354], cy[6], 0xFF676763);

        p.fill(cx[140], cy[6], cx[145], cy[7], 0xFF61625E);

        p.fill(cx[173], cy[7], cx[178], cy[8], 0xFF42473F);

        p.fill(cx[193], cy[7], cx[198], cy[8], 0xFF565B54);

        p.fill(cx[256], cy[7], cx[261], cy[8], 0xFF51504E);
        p.fill(cx[268], cy[7], cx[273], cy[8], 0xFF51504E);

        p.fill(cx[74], cy[10], cx[79], cy[11], 0xFF3C3F3B);
        p.fill(cx[150], cy[10], cx[155], cy[11], 0xFF3C3F3B);
        p.fill(cx[156], cy[10], cx[161], cy[11], 0xFF3C3F3B);
        p.fill(cx[175], cy[10], cx[180], cy[11], 0xFF3C3F3B);
        p.fill(cx[181], cy[10], cx[186], cy[11], 0xFF3C3F3B);
        p.fill(cx[275], cy[10], cx[280], cy[11], 0xFF3C3F3B);

        p.fill(cx[325], cy[10], cx[330], cy[11], 0xFF42473F);
        p.fill(cx[345], cy[10], cx[350], cy[11], 0xFF42473F);

        p.fill(cx[100], cy[11], cx[105], cy[12], 0xFF3C3F3B);

        p.fill(cx[149], cy[11], cx[150], cy[16], 0xFF42473F);

        p.fill(cx[176], cy[11], cx[181], cy[12], 0xFF3C3F3B);

        p.fill(cx[144], cy[13], cx[149], cy[14], 0xFF42473F);

        p.fill(cx[215], cy[13], cx[220], cy[14], 0xFF51504E);
        p.fill(cx[224], cy[13], cx[225], cy[18], 0xFF51504E);

        p.fill(cx[22], cy[14], cx[23], cy[19], 0xFF2F3336);
        p.fill(cx[134], cy[14], cx[135], cy[19], 0xFF2F3336);

        p.fill(cx[163], cy[14], cx[164], cy[19], 0xFF42473F);
        p.fill(cx[169], cy[14], cx[174], cy[15], 0xFF42473F);
        p.fill(cx[194], cy[14], cx[199], cy[15], 0xFF42473F);

        p.fill(cx[8], cy[15], cx[9], cy[20], 0xFF3C3F3B);

        p.fill(cx[59], cy[15], cx[60], cy[20], 0xFF42473F);
        p.fill(cx[72], cy[15], cx[73], cy[20], 0xFF42473F);

        p.fill(cx[16], cy[16], cx[21], cy[17], 0xFF2F3336);

        p.fill(cx[112], cy[16], cx[113], cy[21], 0xFF42473F);
        p.fill(cx[122], cy[16], cx[123], cy[21], 0xFF42473F);
        p.fill(cx[137], cy[16], cx[138], cy[21], 0xFF42473F);
        p.fill(cx[247], cy[16], cx[248], cy[21], 0xFF42473F);

        p.fill(cx[352], cy[16], cx[357], cy[17], 0xFF8A8C83);

        p.fill(cx[95], cy[17], cx[96], cy[22], 0xFF42473F);
        p.fill(cx[211], cy[17], cx[212], cy[22], 0xFF42473F);

        p.fill(cx[82], cy[18], cx[83], cy[23], 0xFF3C3F3B);
        p.fill(cx[207], cy[18], cx[208], cy[23], 0xFF3C3F3B);
        p.fill(cx[152], cy[19], cx[153], cy[24], 0xFF3C3F3B);

        p.fill(cx[137], cy[21], cx[142], cy[22], 0xFF51504E);
        p.fill(cx[268], cy[21], cx[273], cy[22], 0xFF51504E);

        p.fill(cx[288], cy[22], cx[293], cy[23], 0xFF2F3336);

        p.fill(cx[45], cy[23], cx[46], cy[28], 0xFF3C3F3B);
        p.fill(cx[314], cy[23], cx[315], cy[28], 0xFF3C3F3B);
        p.fill(cx[316], cy[23], cx[317], cy[28], 0xFF3C3F3B);

        p.fill(cx[347], cy[23], cx[348], cy[28], 0xFF42473F);

        p.fill(cx[348], cy[24], cx[349], cy[29], 0xFF797D7C);

        p.fill(cx[66], cy[25], cx[71], cy[26], 0xFF3C3F3B);
        p.fill(cx[179], cy[25], cx[184], cy[26], 0xFF3C3F3B);

        p.fill(cx[349], cy[25], cx[350], cy[30], 0xFF797D7C);

        p.fill(cx[162], cy[27], cx[167], cy[28], 0xFF2F3336);

        p.fill(cx[351], cy[30], cx[352], cy[35], 0xFF6D6A64);

        p.fill(cx[355], cy[30], cx[360], cy[31], 0xFF61625E);

        p.fill(cx[5], cy[35], cx[6], cy[40], 0xFF676763);
        p.fill(cx[12], cy[36], cx[13], cy[41], 0xFF676763);

        p.fill(cx[16], cy[36], cx[17], cy[41], 0xFF3C3F3B);

        p.fill(cx[39], cy[36], cx[40], cy[41], 0xFF2F3336);
        p.fill(cx[320], cy[38], cx[325], cy[39], 0xFF2F3336);

        p.fill(cx[42], cy[45], cx[43], cy[50], 0xFF3C3F3B);

        p.fill(cx[13], cy[46], cx[18], cy[47], 0xFF2F3336);

        p.fill(cx[340], cy[49], cx[341], cy[54], 0xFF51504E);
        p.fill(cx[22], cy[52], cx[23], cy[57], 0xFF51504E);

        p.fill(cx[330], cy[52], cx[331], cy[57], 0xFF42473F);

        p.fill(cx[318], cy[53], cx[319], cy[58], 0xFF3C3F3B);

        p.fill(cx[336], cy[53], cx[337], cy[58], 0xFF51504E);

        p.fill(cx[25], cy[57], cx[30], cy[58], 0xFF2F3336);
        p.fill(cx[13], cy[59], cx[14], cy[64], 0xFF2F3336);

        p.fill(cx[21], cy[65], cx[26], cy[66], 0xFF42473F);

        p.fill(cx[340], cy[68], cx[341], cy[73], 0xFF51504E);
        p.fill(cx[24], cy[70], cx[25], cy[75], 0xFF51504E);

        p.fill(cx[330], cy[70], cx[331], cy[75], 0xFF42473F);

        p.fill(cx[337], cy[70], cx[338], cy[75], 0xFF51504E);

        p.fill(cx[22], cy[75], cx[27], cy[76], 0xFF61625E);

        p.fill(cx[351], cy[75], cx[352], cy[80], 0xFF565B54);

        p.fill(cx[352], cy[76], cx[353], cy[81], 0xFF51504E);

        p.fill(cx[38], cy[81], cx[39], cy[86], 0xFF2F3336);

        p.fill(cx[329], cy[81], cx[334], cy[82], 0xFF202423);

        p.fill(cx[334], cy[81], cx[339], cy[82], 0xFF2F3336);
        p.fill(cx[37], cy[83], cx[38], cy[88], 0xFF2F3336);

        p.fill(cx[341], cy[83], cx[342], cy[88], 0xFF676763);

        p.fill(cx[352], cy[83], cx[353], cy[88], 0xFF61625E);

        p.fill(cx[29], cy[88], cx[30], cy[93], 0xFF51504E);
        p.fill(cx[334], cy[88], cx[335], cy[93], 0xFF51504E);

        p.fill(cx[8], cy[89], cx[9], cy[94], 0xFF2F3336);
        p.fill(cx[24], cy[96], cx[29], cy[97], 0xFF2F3336);

        p.fill(cx[24], cy[97], cx[29], cy[98], 0xFF202423);

        p.fill(cx[351], cy[104], cx[352], cy[109], 0xFF61625E);

        p.fill(cx[19], cy[106], cx[20], cy[111], 0xFF51504E);

        p.fill(cx[7], cy[109], cx[8], cy[114], 0xFF565B54);

        p.fill(cx[19], cy[111], cx[24], cy[112], 0xFF61625E);

        p.fill(cx[340], cy[120], cx[341], cy[125], 0xFF42473F);
        p.fill(cx[19], cy[123], cx[20], cy[128], 0xFF42473F);

        p.fill(cx[23], cy[124], cx[24], cy[129], 0xFF51504E);
        p.fill(cx[332], cy[124], cx[333], cy[129], 0xFF51504E);
        p.fill(cx[334], cy[124], cx[335], cy[129], 0xFF51504E);
        p.fill(cx[339], cy[124], cx[340], cy[129], 0xFF51504E);
        p.fill(cx[340], cy[125], cx[341], cy[130], 0xFF51504E);

        p.fill(cx[330], cy[129], cx[335], cy[130], 0xFF565B54);

        p.fill(cx[42], cy[132], cx[43], cy[137], 0xFF2F3336);

        p.fill(cx[333], cy[133], cx[338], cy[134], 0xFF202423);

        p.fill(cx[333], cy[134], cx[338], cy[135], 0xFF2F3336);

        p.fill(cx[336], cy[136], cx[341], cy[137], 0xFF51504E);

        p.fill(cx[317], cy[137], cx[318], cy[142], 0xFF2F3336);

        p.fill(cx[346], cy[138], cx[347], cy[143], 0xFF3C3F3B);

        p.fill(cx[340], cy[139], cx[341], cy[144], 0xFF42473F);

        p.fill(cx[338], cy[141], cx[339], cy[146], 0xFF51504E);

        p.fill(cx[331], cy[142], cx[332], cy[147], 0xFF42473F);

        p.fill(cx[333], cy[145], cx[338], cy[146], 0xFF51504E);

        p.fill(cx[17], cy[148], cx[18], cy[153], 0xFF2F3336);

        p.fill(cx[7], cy[151], cx[8], cy[156], 0xFF42473F);

        p.fill(cx[343], cy[154], cx[344], cy[159], 0xFF2F3336);
        p.fill(cx[346], cy[154], cx[347], cy[159], 0xFF2F3336);

        p.fill(cx[0], cy[157], cx[5], cy[158], 0xFF51504E);

        p.fill(cx[351], cy[159], cx[352], cy[164], 0xFF61625E);

        p.fill(cx[45], cy[166], cx[50], cy[167], 0xFF51504E);
        p.fill(cx[60], cy[167], cx[65], cy[168], 0xFF51504E);

        p.fill(cx[143], cy[167], cx[148], cy[168], 0xFF42473F);

        p.fill(cx[319], cy[167], cx[320], cy[172], 0xFF2F3336);
        p.fill(cx[129], cy[168], cx[130], cy[173], 0xFF2F3336);
        p.fill(cx[135], cy[168], cx[136], cy[173], 0xFF2F3336);

        p.fill(cx[143], cy[168], cx[148], cy[169], 0xFF51504E);

        p.fill(cx[155], cy[168], cx[156], cy[173], 0xFF2F3336);

        p.fill(cx[209], cy[168], cx[214], cy[169], 0xFF42473F);
        p.fill(cx[229], cy[168], cx[234], cy[169], 0xFF42473F);

        p.fill(cx[234], cy[168], cx[235], cy[173], 0xFF2F3336);
        p.fill(cx[240], cy[168], cx[241], cy[173], 0xFF2F3336);
        p.fill(cx[261], cy[168], cx[262], cy[173], 0xFF2F3336);
        p.fill(cx[263], cy[168], cx[264], cy[173], 0xFF2F3336);

        p.fill(cx[354], cy[168], cx[355], cy[173], 0xFF565B54);

        p.fill(cx[326], cy[171], cx[327], cy[176], 0xFF3C3F3B);
        p.fill(cx[345], cy[171], cx[346], cy[176], 0xFF3C3F3B);
        p.fill(cx[327], cy[172], cx[328], cy[177], 0xFF3C3F3B);

        p.fill(cx[10], cy[173], cx[11], cy[178], 0xFF61625E);

        p.fill(cx[319], cy[173], cx[320], cy[178], 0xFF2F3336);
        p.fill(cx[41], cy[174], cx[42], cy[179], 0xFF2F3336);

        p.fill(cx[220], cy[174], cx[225], cy[175], 0xFF202423);
        p.fill(cx[241], cy[174], cx[246], cy[175], 0xFF202423);
        p.fill(cx[275], cy[174], cx[280], cy[175], 0xFF202423);

        p.fill(cx[27], cy[175], cx[28], cy[180], 0xFF3C3F3B);
        p.fill(cx[30], cy[175], cx[31], cy[180], 0xFF3C3F3B);

        p.fill(cx[45], cy[175], cx[50], cy[176], 0xFF202423);

        p.fill(cx[220], cy[175], cx[225], cy[176], 0xFF2F3336);
        p.fill(cx[241], cy[175], cx[246], cy[176], 0xFF2F3336);
        p.fill(cx[99], cy[176], cx[104], cy[177], 0xFF2F3336);
        p.fill(cx[105], cy[176], cx[110], cy[177], 0xFF2F3336);

        p.fill(cx[92], cy[177], cx[97], cy[178], 0xFF61625E);

        p.fill(cx[144], cy[177], cx[149], cy[178], 0xFF565B54);

        p.fill(cx[251], cy[177], cx[256], cy[178], 0xFF2F3336);

        p.fill(cx[244], cy[178], cx[249], cy[179], 0xFF42473F);
        p.fill(cx[268], cy[178], cx[273], cy[179], 0xFF42473F);

        p.fill(cx[328], cy[178], cx[333], cy[179], 0xFF3C3F3B);

        p.fill(cx[211], cy[179], cx[212], cy[184], 0xFF51504E);
        p.fill(cx[215], cy[179], cx[220], cy[180], 0xFF51504E);
        p.fill(cx[136], cy[180], cx[137], cy[185], 0xFF51504E);
        p.fill(cx[138], cy[180], cx[139], cy[185], 0xFF51504E);
        p.fill(cx[91], cy[181], cx[92], cy[186], 0xFF51504E);
        p.fill(cx[168], cy[181], cx[173], cy[182], 0xFF51504E);

        p.fill(cx[187], cy[181], cx[192], cy[182], 0xFF42473F);

        p.fill(cx[213], cy[181], cx[218], cy[182], 0xFF51504E);

        p.fill(cx[287], cy[181], cx[292], cy[182], 0xFFD43835);

        p.fill(cx[161], cy[182], cx[162], cy[187], 0xFF42473F);

        p.fill(cx[205], cy[182], cx[206], cy[187], 0xFF3C3F3B);

        p.fill(cx[239], cy[182], cx[240], cy[187], 0xFF42473F);
        p.fill(cx[241], cy[182], cx[242], cy[187], 0xFF42473F);

        p.fill(cx[254], cy[182], cx[255], cy[187], 0xFF2F3336);

        p.fill(cx[263], cy[182], cx[264], cy[187], 0xFF42473F);
        p.fill(cx[115], cy[183], cx[116], cy[188], 0xFF42473F);

        p.fill(cx[291], cy[183], cx[292], cy[188], 0xFFD43835);

        p.fill(cx[313], cy[184], cx[314], cy[189], 0xFF2F3336);

        p.fill(cx[0], cy[186], cx[5], cy[187], 0xFF6D6A64);
        p.fill(cx[355], cy[186], cx[360], cy[187], 0xFF6D6A64);

        p.fill(cx[211], cy[187], cx[216], cy[188], 0xFF51504E);

        p.fill(cx[317], cy[187], cx[318], cy[192], 0xFF3C3F3B);

        p.fill(cx[84], cy[188], cx[85], cy[193], 0xFF2F3336);
        p.fill(cx[98], cy[188], cx[99], cy[193], 0xFF2F3336);
        p.fill(cx[109], cy[188], cx[110], cy[193], 0xFF2F3336);
        p.fill(cx[123], cy[188], cx[124], cy[193], 0xFF2F3336);
        p.fill(cx[125], cy[188], cx[126], cy[193], 0xFF2F3336);
        p.fill(cx[130], cy[188], cx[131], cy[193], 0xFF2F3336);
        p.fill(cx[134], cy[188], cx[135], cy[193], 0xFF2F3336);
        p.fill(cx[147], cy[188], cx[148], cy[193], 0xFF2F3336);
        p.fill(cx[150], cy[188], cx[151], cy[193], 0xFF2F3336);
        p.fill(cx[159], cy[188], cx[160], cy[193], 0xFF2F3336);
        p.fill(cx[179], cy[188], cx[180], cy[193], 0xFF2F3336);
        p.fill(cx[198], cy[188], cx[199], cy[193], 0xFF2F3336);
        p.fill(cx[250], cy[188], cx[251], cy[193], 0xFF2F3336);
        p.fill(cx[254], cy[188], cx[255], cy[193], 0xFF2F3336);
        p.fill(cx[256], cy[188], cx[257], cy[193], 0xFF2F3336);
        p.fill(cx[275], cy[188], cx[276], cy[193], 0xFF2F3336);

        p.fill(cx[9], cy[189], cx[10], cy[194], 0xFF51504E);

        p.fill(cx[0], cy[191], cx[5], cy[192], 0xFF61625E);

        p.fill(cx[8], cy[191], cx[9], cy[196], 0xFF51504E);
        p.fill(cx[7], cy[192], cx[8], cy[197], 0xFF51504E);
        p.fill(cx[353], cy[192], cx[354], cy[197], 0xFF51504E);

        p.fill(cx[309], cy[193], cx[310], cy[198], 0xFF2F3336);

        p.fill(cx[355], cy[193], cx[360], cy[194], 0xFF565B54);

        p.fill(cx[357], cy[194], cx[358], cy[199], 0xFF51504E);

        p.fill(cx[166], cy[195], cx[171], cy[196], 0xFF3C3F3B);
        p.fill(cx[175], cy[195], cx[180], cy[196], 0xFF3C3F3B);
        p.fill(cx[257], cy[195], cx[262], cy[196], 0xFF3C3F3B);

        p.fill(cx[110], cy[196], cx[111], cy[201], 0xFF42473F);

        p.fill(cx[321], cy[197], cx[322], cy[202], 0xFF3C3F3B);

        p.fill(cx[107], cy[198], cx[108], cy[203], 0xFF42473F);
        p.fill(cx[127], cy[198], cx[128], cy[203], 0xFF42473F);

        p.fill(cx[309], cy[198], cx[310], cy[203], 0xFF3C3F3B);

        p.fill(cx[358], cy[198], cx[359], cy[203], 0xFF42473F);

        p.fill(cx[84], cy[199], cx[89], cy[200], 0xFF3C3F3B);

        p.fill(cx[266], cy[202], cx[271], cy[203], 0xFF42473F);

        p.fill(cx[319], cy[202], cx[324], cy[203], 0xFF2F3336);

        p.fill(cx[212], cy[1], cx[213], cy[5], 0xFF61625E);

        p.fill(cx[115], cy[2], cx[117], cy[4], 0xFF565B54);

        p.fill(cx[19], cy[3], cx[21], cy[5], 0xFF51504E);
        p.fill(cx[40], cy[4], cx[41], cy[8], 0xFF51504E);
        p.fill(cx[50], cy[4], cx[54], cy[5], 0xFF51504E);
        p.fill(cx[88], cy[4], cx[89], cy[8], 0xFF51504E);

        p.fill(cx[112], cy[4], cx[116], cy[5], 0xFF61625E);

        p.fill(cx[17], cy[5], cx[19], cy[7], 0xFF42473F);

        p.fill(cx[29], cy[5], cx[30], cy[9], 0xFF2F3336);

        p.fill(cx[54], cy[5], cx[56], cy[7], 0xFF3C3F3B);

        p.fill(cx[347], cy[5], cx[349], cy[7], 0xFF61625E);

        p.fill(cx[15], cy[6], cx[16], cy[10], 0xFF42473F);

        p.fill(cx[63], cy[6], cx[67], cy[7], 0xFF51504E);

        p.fill(cx[112], cy[6], cx[114], cy[8], 0xFF61625E);
        p.fill(cx[282], cy[6], cx[286], cy[7], 0xFF61625E);

        p.fill(cx[304], cy[6], cx[308], cy[7], 0xFF3C3F3B);

        p.fill(cx[349], cy[6], cx[353], cy[7], 0xFF676763);
        p.fill(cx[3], cy[7], cx[7], cy[8], 0xFF676763);

        p.fill(cx[13], cy[7], cx[15], cy[9], 0xFF42473F);

        p.fill(cx[19], cy[7], cx[20], cy[11], 0xFF3C3F3B);

        p.fill(cx[74], cy[7], cx[78], cy[8], 0xFF51504E);

        p.fill(cx[83], cy[7], cx[87], cy[8], 0xFF61625E);

        p.fill(cx[150], cy[7], cx[154], cy[8], 0xFF565B54);

        p.fill(cx[168], cy[7], cx[172], cy[8], 0xFF51504E);

        p.fill(cx[182], cy[7], cx[186], cy[8], 0xFF42473F);
        p.fill(cx[287], cy[7], cx[289], cy[9], 0xFF42473F);

        p.fill(cx[3], cy[8], cx[5], cy[10], 0xFF6D6A64);

        p.fill(cx[115], cy[8], cx[117], cy[10], 0xFF42473F);

        p.fill(cx[283], cy[8], cx[287], cy[9], 0xFF3C3F3B);
        p.fill(cx[338], cy[8], cx[339], cy[12], 0xFF3C3F3B);
        p.fill(cx[340], cy[8], cx[342], cy[10], 0xFF3C3F3B);

        p.fill(cx[354], cy[8], cx[356], cy[10], 0xFF797D7C);
        p.fill(cx[0], cy[9], cx[1], cy[13], 0xFF797D7C);

        p.fill(cx[5], cy[9], cx[7], cy[11], 0xFF6D6A64);

        p.fill(cx[54], cy[9], cx[58], cy[10], 0xFF3C3F3B);
        p.fill(cx[80], cy[9], cx[84], cy[10], 0xFF3C3F3B);

        p.fill(cx[58], cy[10], cx[62], cy[11], 0xFF202423);

        p.fill(cx[303], cy[10], cx[307], cy[11], 0xFF3C3F3B);

        p.fill(cx[323], cy[10], cx[324], cy[14], 0xFF3D3E3D);

        p.fill(cx[351], cy[10], cx[353], cy[12], 0xFFA5A49E);

        p.fill(cx[354], cy[10], cx[356], cy[12], 0xFF8A8C83);

        p.fill(cx[86], cy[11], cx[90], cy[12], 0xFF51504E);
        p.fill(cx[111], cy[11], cx[115], cy[12], 0xFF51504E);

        p.fill(cx[131], cy[11], cx[135], cy[12], 0xFF3C3F3B);

        p.fill(cx[237], cy[11], cx[241], cy[12], 0xFF565B54);

        p.fill(cx[276], cy[11], cx[280], cy[12], 0xFF3C3F3B);
        p.fill(cx[343], cy[11], cx[347], cy[12], 0xFF3C3F3B);

        p.fill(cx[347], cy[11], cx[351], cy[12], 0xFF42473F);
        p.fill(cx[56], cy[12], cx[57], cy[16], 0xFF42473F);
        p.fill(cx[62], cy[12], cx[66], cy[13], 0xFF42473F);
        p.fill(cx[283], cy[12], cx[285], cy[14], 0xFF42473F);
        p.fill(cx[297], cy[12], cx[301], cy[13], 0xFF42473F);

        p.fill(cx[354], cy[12], cx[355], cy[16], 0xFFD1D2C3);

        p.fill(cx[0], cy[13], cx[1], cy[17], 0xFF8A8C83);

        p.fill(cx[115], cy[13], cx[119], cy[14], 0xFF51504E);

        p.fill(cx[135], cy[13], cx[136], cy[17], 0xFF565B54);

        p.fill(cx[164], cy[13], cx[166], cy[15], 0xFF42473F);

        p.fill(cx[211], cy[13], cx[212], cy[17], 0xFF51504E);

        p.fill(cx[249], cy[13], cx[250], cy[17], 0xFF42473F);

        p.fill(cx[313], cy[13], cx[317], cy[14], 0xFF3C3F3B);
        p.fill(cx[52], cy[14], cx[53], cy[18], 0xFF3C3F3B);

        p.fill(cx[67], cy[14], cx[69], cy[16], 0xFF42473F);

        p.fill(cx[129], cy[14], cx[130], cy[18], 0xFF202423);

        p.fill(cx[141], cy[14], cx[143], cy[16], 0xFF51504E);

        p.fill(cx[145], cy[14], cx[149], cy[15], 0xFF42473F);

        p.fill(cx[177], cy[14], cx[178], cy[18], 0xFF2F3336);

        p.fill(cx[215], cy[14], cx[219], cy[15], 0xFF51504E);
        p.fill(cx[240], cy[14], cx[242], cy[16], 0xFF51504E);

        p.fill(cx[242], cy[14], cx[246], cy[15], 0xFF42473F);

        p.fill(cx[283], cy[14], cx[285], cy[16], 0xFF3C3F3B);

        p.fill(cx[297], cy[14], cx[299], cy[16], 0xFF51504E);

        p.fill(cx[323], cy[14], cx[324], cy[18], 0xFF3C3F3B);

        p.fill(cx[21], cy[15], cx[22], cy[19], 0xFF2F3336);

        p.fill(cx[61], cy[15], cx[62], cy[19], 0xFF42473F);
        p.fill(cx[86], cy[15], cx[88], cy[17], 0xFF42473F);
        p.fill(cx[170], cy[15], cx[174], cy[16], 0xFF42473F);

        p.fill(cx[343], cy[15], cx[344], cy[19], 0xFF3C3F3B);

        p.fill(cx[2], cy[16], cx[3], cy[20], 0xFF797D7C);

        p.fill(cx[146], cy[16], cx[150], cy[17], 0xFF51504E);

        p.fill(cx[192], cy[16], cx[196], cy[17], 0xFFFBFBFB);

        p.fill(cx[227], cy[16], cx[228], cy[20], 0xFF2F3336);

        p.fill(cx[266], cy[16], cx[270], cy[17], 0xFFFBFBFB);

        p.fill(cx[298], cy[16], cx[299], cy[20], 0xFF51504E);

        p.fill(cx[326], cy[16], cx[327], cy[20], 0xFF42473F);

        p.fill(cx[17], cy[17], cx[21], cy[18], 0xFF2F3336);

        p.fill(cx[67], cy[17], cx[69], cy[19], 0xFF51504E);

        p.fill(cx[86], cy[17], cx[87], cy[21], 0xFF42473F);
        p.fill(cx[123], cy[17], cx[124], cy[21], 0xFF42473F);
        p.fill(cx[143], cy[17], cx[144], cy[21], 0xFF42473F);
        p.fill(cx[145], cy[17], cx[146], cy[21], 0xFF42473F);
        p.fill(cx[168], cy[17], cx[169], cy[21], 0xFF42473F);
        p.fill(cx[195], cy[17], cx[196], cy[21], 0xFF42473F);
        p.fill(cx[213], cy[17], cx[214], cy[21], 0xFF42473F);
        p.fill(cx[246], cy[17], cx[247], cy[21], 0xFF42473F);
        p.fill(cx[269], cy[17], cx[270], cy[21], 0xFF42473F);
        p.fill(cx[271], cy[17], cx[272], cy[21], 0xFF42473F);

        p.fill(cx[319], cy[17], cx[323], cy[18], 0xFF3C3F3B);

        p.fill(cx[5], cy[18], cx[7], cy[20], 0xFF797D7C);

        p.fill(cx[69], cy[18], cx[70], cy[22], 0xFF51504E);

        p.fill(cx[129], cy[18], cx[130], cy[22], 0xFF42473F);
        p.fill(cx[144], cy[18], cx[145], cy[22], 0xFF42473F);
        p.fill(cx[179], cy[18], cx[180], cy[22], 0xFF42473F);

        p.fill(cx[265], cy[18], cx[269], cy[19], 0xFF51504E);

        p.fill(cx[352], cy[18], cx[354], cy[20], 0xFF6D6A64);

        p.fill(cx[354], cy[18], cx[358], cy[19], 0xFF797D7C);

        p.fill(cx[70], cy[19], cx[72], cy[21], 0xFF51504E);

        p.fill(cx[80], cy[19], cx[81], cy[23], 0xFF2F3336);
        p.fill(cx[103], cy[19], cx[104], cy[23], 0xFF2F3336);

        p.fill(cx[163], cy[19], cx[165], cy[21], 0xFF51504E);

        p.fill(cx[208], cy[19], cx[209], cy[23], 0xFF3C3F3B);

        p.fill(cx[243], cy[19], cx[245], cy[21], 0xFF42473F);
        p.fill(cx[332], cy[19], cx[334], cy[21], 0xFF42473F);

        p.fill(cx[354], cy[19], cx[356], cy[21], 0xFF6D6A64);

        p.fill(cx[356], cy[19], cx[360], cy[20], 0xFF797D7C);

        p.fill(cx[12], cy[20], cx[16], cy[21], 0xFF3C3F3B);
        p.fill(cx[202], cy[20], cx[203], cy[24], 0xFF3C3F3B);
        p.fill(cx[277], cy[20], cx[279], cy[22], 0xFF3C3F3B);
        p.fill(cx[317], cy[20], cx[318], cy[24], 0xFF3C3F3B);

        p.fill(cx[220], cy[21], cx[224], cy[22], 0xFF51504E);

        p.fill(cx[358], cy[21], cx[360], cy[23], 0xFF6D6A64);
        p.fill(cx[8], cy[22], cx[10], cy[24], 0xFF6D6A64);

        p.fill(cx[57], cy[22], cx[58], cy[26], 0xFF3C3F3B);

        p.fill(cx[83], cy[22], cx[85], cy[24], 0xFF2F3336);
        p.fill(cx[149], cy[22], cx[151], cy[24], 0xFF2F3336);

        p.fill(cx[302], cy[22], cx[303], cy[26], 0xFF3C3F3B);
        p.fill(cx[72], cy[23], cx[74], cy[25], 0xFF3C3F3B);
        p.fill(cx[286], cy[23], cx[288], cy[25], 0xFF3C3F3B);
        p.fill(cx[313], cy[24], cx[314], cy[28], 0xFF3C3F3B);
        p.fill(cx[315], cy[24], cx[316], cy[28], 0xFF3C3F3B);
        p.fill(cx[84], cy[25], cx[88], cy[26], 0xFF3C3F3B);
        p.fill(cx[188], cy[25], cx[192], cy[26], 0xFF3C3F3B);
        p.fill(cx[218], cy[25], cx[222], cy[26], 0xFF3C3F3B);
        p.fill(cx[40], cy[26], cx[41], cy[30], 0xFF3C3F3B);

        p.fill(cx[120], cy[26], cx[122], cy[28], 0xFF2F3336);
        p.fill(cx[209], cy[26], cx[211], cy[28], 0xFF2F3336);
        p.fill(cx[212], cy[26], cx[214], cy[28], 0xFF2F3336);

        p.fill(cx[319], cy[26], cx[320], cy[30], 0xFF3C3F3B);
        p.fill(cx[320], cy[27], cx[321], cy[31], 0xFF3C3F3B);

        p.fill(cx[10], cy[28], cx[11], cy[32], 0xFF6D6A64);

        p.fill(cx[30], cy[29], cx[34], cy[30], 0xFF3C3F3B);

        p.fill(cx[41], cy[29], cx[42], cy[33], 0xFF2F3336);
        p.fill(cx[318], cy[29], cx[319], cy[33], 0xFF2F3336);
        p.fill(cx[42], cy[30], cx[43], cy[34], 0xFF2F3336);
        p.fill(cx[317], cy[30], cx[318], cy[34], 0xFF2F3336);

        p.fill(cx[348], cy[31], cx[349], cy[35], 0xFF797D7C);

        p.fill(cx[356], cy[31], cx[360], cy[32], 0xFF61625E);

        p.fill(cx[8], cy[34], cx[10], cy[36], 0xFF6D6A64);

        p.fill(cx[329], cy[34], cx[330], cy[38], 0xFF2F3336);

        p.fill(cx[22], cy[36], cx[26], cy[37], 0xFF3C3F3B);
        p.fill(cx[21], cy[37], cx[25], cy[38], 0xFF3C3F3B);

        p.fill(cx[25], cy[37], cx[26], cy[41], 0xFF2F3336);
        p.fill(cx[28], cy[37], cx[29], cy[41], 0xFF2F3336);

        p.fill(cx[32], cy[37], cx[33], cy[41], 0xFF3C3F3B);

        p.fill(cx[33], cy[37], cx[35], cy[39], 0xFF2F3336);
        p.fill(cx[320], cy[39], cx[324], cy[40], 0xFF2F3336);

        p.fill(cx[42], cy[40], cx[43], cy[44], 0xFF3C3F3B);

        p.fill(cx[324], cy[40], cx[326], cy[42], 0xFF2F3336);

        p.fill(cx[8], cy[41], cx[9], cy[45], 0xFF3C3F3B);

        p.fill(cx[35], cy[41], cx[36], cy[45], 0xFF2F3336);
        p.fill(cx[317], cy[42], cx[319], cy[44], 0xFF2F3336);

        p.fill(cx[355], cy[42], cx[356], cy[46], 0xFF565B54);

        p.fill(cx[33], cy[43], cx[35], cy[45], 0xFF202423);

        p.fill(cx[11], cy[44], cx[13], cy[46], 0xFF61625E);
        p.fill(cx[11], cy[46], cx[12], cy[50], 0xFF61625E);

        p.fill(cx[26], cy[46], cx[30], cy[47], 0xFF676763);

        p.fill(cx[34], cy[46], cx[35], cy[50], 0xFF42473F);

        p.fill(cx[338], cy[46], cx[342], cy[47], 0xFF565B54);

        p.fill(cx[342], cy[46], cx[346], cy[47], 0xFF2F3336);
        p.fill(cx[13], cy[47], cx[17], cy[48], 0xFF2F3336);

        p.fill(cx[35], cy[47], cx[36], cy[51], 0xFF3C3F3B);

        p.fill(cx[29], cy[49], cx[30], cy[53], 0xFF565B54);

        p.fill(cx[36], cy[50], cx[38], cy[52], 0xFF2F3336);

        p.fill(cx[9], cy[51], cx[13], cy[52], 0xFF61625E);

        p.fill(cx[35], cy[52], cx[37], cy[54], 0xFF3C3F3B);

        p.fill(cx[23], cy[53], cx[24], cy[57], 0xFF51504E);

        p.fill(cx[34], cy[53], cx[35], cy[57], 0xFF42473F);

        p.fill(cx[333], cy[53], cx[334], cy[57], 0xFF51504E);

        p.fill(cx[17], cy[58], cx[19], cy[60], 0xFF2F3336);
        p.fill(cx[326], cy[61], cx[327], cy[65], 0xFF2F3336);
        p.fill(cx[30], cy[62], cx[32], cy[64], 0xFF2F3336);
        p.fill(cx[8], cy[63], cx[9], cy[67], 0xFF2F3336);

        p.fill(cx[13], cy[64], cx[14], cy[68], 0xFF3C3F3B);

        p.fill(cx[23], cy[71], cx[24], cy[75], 0xFF51504E);
        p.fill(cx[333], cy[71], cx[334], cy[75], 0xFF51504E);
        p.fill(cx[334], cy[72], cx[335], cy[76], 0xFF51504E);

        p.fill(cx[352], cy[72], cx[354], cy[74], 0xFF565B54);

        p.fill(cx[335], cy[73], cx[337], cy[75], 0xFF51504E);

        p.fill(cx[341], cy[75], cx[342], cy[79], 0xFF2F3336);

        p.fill(cx[42], cy[76], cx[43], cy[80], 0xFF42473F);

        p.fill(cx[326], cy[79], cx[327], cy[83], 0xFF2F3336);

        p.fill(cx[36], cy[80], cx[37], cy[84], 0xFF3C3F3B);

        p.fill(cx[331], cy[83], cx[335], cy[84], 0xFF2F3336);

        p.fill(cx[335], cy[83], cx[336], cy[87], 0xFF51504E);
        p.fill(cx[337], cy[83], cx[338], cy[87], 0xFF51504E);

        p.fill(cx[340], cy[83], cx[341], cy[87], 0xFF42473F);

        p.fill(cx[317], cy[87], cx[319], cy[89], 0xFF3C3F3B);

        p.fill(cx[333], cy[89], cx[334], cy[93], 0xFF51504E);
        p.fill(cx[335], cy[89], cx[336], cy[93], 0xFF51504E);
        p.fill(cx[338], cy[89], cx[339], cy[93], 0xFF51504E);
    }

    private static void part4(Paint p, int[] cx, int[] cy) {

        p.fill(cx[336], cy[91], cx[338], cy[93], 0xFF51504E);

        p.fill(cx[26], cy[93], cx[30], cy[94], 0xFF2F3336);
        p.fill(cx[17], cy[94], cx[19], cy[96], 0xFF2F3336);

        p.fill(cx[22], cy[96], cx[24], cy[98], 0xFF202423);
        p.fill(cx[336], cy[96], cx[338], cy[98], 0xFF202423);
        p.fill(cx[338], cy[97], cx[342], cy[98], 0xFF202423);

        p.fill(cx[342], cy[97], cx[343], cy[101], 0xFF2F3336);
        p.fill(cx[328], cy[99], cx[332], cy[100], 0xFF2F3336);

        p.fill(cx[332], cy[99], cx[336], cy[100], 0xFF202423);

        p.fill(cx[336], cy[99], cx[340], cy[100], 0xFF2F3336);

        p.fill(cx[335], cy[100], cx[339], cy[101], 0xFF565B54);

        p.fill(cx[34], cy[101], cx[35], cy[105], 0xFF42473F);

        p.fill(cx[333], cy[102], cx[335], cy[104], 0xFF51504E);

        p.fill(cx[8], cy[103], cx[9], cy[107], 0xFF3C3F3B);

        p.fill(cx[6], cy[104], cx[7], cy[108], 0xFF51504E);

        p.fill(cx[333], cy[104], cx[335], cy[106], 0xFF42473F);

        p.fill(cx[347], cy[104], cx[348], cy[108], 0xFF61625E);

        p.fill(cx[332], cy[108], cx[334], cy[110], 0xFF42473F);

        p.fill(cx[336], cy[109], cx[338], cy[111], 0xFF51504E);

        p.fill(cx[341], cy[111], cx[342], cy[115], 0xFF2F3336);
        p.fill(cx[336], cy[114], cx[340], cy[115], 0xFF2F3336);

        p.fill(cx[346], cy[114], cx[347], cy[118], 0xFF565B54);

        p.fill(cx[326], cy[115], cx[327], cy[119], 0xFF2F3336);

        p.fill(cx[7], cy[117], cx[8], cy[121], 0xFF61625E);
        p.fill(cx[330], cy[118], cx[334], cy[119], 0xFF61625E);

        p.fill(cx[334], cy[118], cx[335], cy[122], 0xFF51504E);

        p.fill(cx[337], cy[119], cx[341], cy[120], 0xFF3C3F3B);

        p.fill(cx[42], cy[124], cx[43], cy[128], 0xFF2F3336);

        p.fill(cx[22], cy[125], cx[23], cy[129], 0xFF51504E);

        p.fill(cx[331], cy[125], cx[332], cy[129], 0xFF42473F);

        p.fill(cx[42], cy[128], cx[43], cy[132], 0xFF3C3F3B);

        p.fill(cx[336], cy[129], cx[340], cy[130], 0xFF565B54);
        p.fill(cx[351], cy[129], cx[353], cy[131], 0xFF565B54);

        p.fill(cx[17], cy[130], cx[19], cy[132], 0xFF2F3336);
        p.fill(cx[341], cy[130], cx[343], cy[132], 0xFF2F3336);
        p.fill(cx[342], cy[132], cx[343], cy[136], 0xFF2F3336);
        p.fill(cx[26], cy[133], cx[30], cy[134], 0xFF2F3336);
        p.fill(cx[17], cy[134], cx[19], cy[136], 0xFF2F3336);
        p.fill(cx[23], cy[134], cx[27], cy[135], 0xFF2F3336);
        p.fill(cx[28], cy[134], cx[32], cy[135], 0xFF2F3336);

        p.fill(cx[28], cy[136], cx[30], cy[138], 0xFF51504E);

        p.fill(cx[8], cy[137], cx[9], cy[141], 0xFF2F3336);

        p.fill(cx[332], cy[143], cx[334], cy[145], 0xFF51504E);
        p.fill(cx[29], cy[144], cx[30], cy[148], 0xFF51504E);

        p.fill(cx[34], cy[144], cx[35], cy[148], 0xFF42473F);
        p.fill(cx[339], cy[145], cx[341], cy[147], 0xFF42473F);

        p.fill(cx[334], cy[146], cx[338], cy[147], 0xFF51504E);

        p.fill(cx[41], cy[150], cx[42], cy[154], 0xFF2F3336);

        p.fill(cx[350], cy[151], cx[351], cy[155], 0xFF676763);

        p.fill(cx[8], cy[152], cx[9], cy[156], 0xFF61625E);

        p.fill(cx[353], cy[154], cx[354], cy[158], 0xFF3C3F3B);

        p.fill(cx[12], cy[155], cx[13], cy[159], 0xFF676763);

        p.fill(cx[347], cy[155], cx[351], cy[156], 0xFF61625E);
        p.fill(cx[349], cy[157], cx[351], cy[159], 0xFF61625E);

        p.fill(cx[0], cy[158], cx[4], cy[159], 0xFF51504E);

        p.fill(cx[352], cy[158], cx[354], cy[160], 0xFF42473F);

        p.fill(cx[354], cy[158], cx[355], cy[162], 0xFF565B54);

        p.fill(cx[352], cy[160], cx[353], cy[164], 0xFF3C3F3B);
        p.fill(cx[39], cy[161], cx[41], cy[163], 0xFF3C3F3B);

        p.fill(cx[354], cy[162], cx[355], cy[166], 0xFF42473F);

        p.fill(cx[24], cy[163], cx[26], cy[165], 0xFF3C3F3B);

        p.fill(cx[351], cy[164], cx[352], cy[168], 0xFF676763);

        p.fill(cx[352], cy[164], cx[353], cy[168], 0xFF42473F);

        p.fill(cx[25], cy[165], cx[26], cy[169], 0xFF2F3336);

        p.fill(cx[2], cy[166], cx[3], cy[170], 0xFF565B54);
        p.fill(cx[41], cy[166], cx[45], cy[167], 0xFF565B54);

        p.fill(cx[241], cy[166], cx[243], cy[168], 0xFF42473F);

        p.fill(cx[314], cy[166], cx[316], cy[168], 0xFF565B54);

        p.fill(cx[88], cy[167], cx[92], cy[168], 0xFF51504E);

        p.fill(cx[92], cy[167], cx[96], cy[168], 0xFF42473F);
        p.fill(cx[294], cy[167], cx[296], cy[169], 0xFF42473F);

        p.fill(cx[333], cy[167], cx[334], cy[171], 0xFF2F3336);

        p.fill(cx[46], cy[168], cx[50], cy[169], 0xFF51504E);
        p.fill(cx[64], cy[168], cx[68], cy[169], 0xFF51504E);
        p.fill(cx[87], cy[168], cx[91], cy[169], 0xFF51504E);

        p.fill(cx[111], cy[168], cx[115], cy[169], 0xFF42473F);

        p.fill(cx[136], cy[168], cx[140], cy[169], 0xFF51504E);

        p.fill(cx[218], cy[168], cx[220], cy[170], 0xFF2F3336);

        p.fill(cx[220], cy[168], cx[224], cy[169], 0xFF42473F);
        p.fill(cx[290], cy[168], cx[294], cy[169], 0xFF42473F);

        p.fill(cx[311], cy[168], cx[315], cy[169], 0xFF51504E);

        p.fill(cx[130], cy[169], cx[131], cy[173], 0xFF2F3336);
        p.fill(cx[154], cy[169], cx[155], cy[173], 0xFF2F3336);
        p.fill(cx[156], cy[169], cx[157], cy[173], 0xFF2F3336);
        p.fill(cx[164], cy[169], cx[165], cy[173], 0xFF2F3336);
        p.fill(cx[168], cy[169], cx[169], cy[173], 0xFF2F3336);
        p.fill(cx[226], cy[169], cx[227], cy[173], 0xFF2F3336);
        p.fill(cx[235], cy[169], cx[236], cy[173], 0xFF2F3336);
        p.fill(cx[241], cy[169], cx[242], cy[173], 0xFF2F3336);
        p.fill(cx[262], cy[169], cx[263], cy[173], 0xFF2F3336);
        p.fill(cx[276], cy[169], cx[277], cy[173], 0xFF2F3336);
        p.fill(cx[352], cy[169], cx[354], cy[171], 0xFF2F3336);

        p.fill(cx[5], cy[170], cx[6], cy[174], 0xFF61625E);

        p.fill(cx[116], cy[171], cx[118], cy[173], 0xFF2F3336);
        p.fill(cx[217], cy[171], cx[219], cy[173], 0xFF2F3336);

        p.fill(cx[352], cy[171], cx[354], cy[173], 0xFF565B54);

        p.fill(cx[31], cy[172], cx[33], cy[174], 0xFF2F3336);

        p.fill(cx[6], cy[173], cx[8], cy[175], 0xFF61625E);

        p.fill(cx[29], cy[173], cx[31], cy[175], 0xFF2F3336);

        p.fill(cx[80], cy[174], cx[84], cy[175], 0xFF202423);

        p.fill(cx[115], cy[174], cx[117], cy[176], 0xFF2F3336);

        p.fill(cx[144], cy[174], cx[148], cy[175], 0xFF202423);

        p.fill(cx[237], cy[174], cx[239], cy[176], 0xFF2F3336);
        p.fill(cx[246], cy[174], cx[248], cy[176], 0xFF2F3336);

        p.fill(cx[341], cy[174], cx[342], cy[178], 0xFF3C3F3B);

        p.fill(cx[6], cy[175], cx[8], cy[177], 0xFF676763);

        p.fill(cx[79], cy[175], cx[81], cy[177], 0xFF202423);

        p.fill(cx[144], cy[175], cx[148], cy[176], 0xFF2F3336);
        p.fill(cx[316], cy[175], cx[318], cy[177], 0xFF2F3336);

        p.fill(cx[352], cy[175], cx[354], cy[177], 0xFF676763);

        p.fill(cx[131], cy[176], cx[135], cy[177], 0xFF2F3336);
        p.fill(cx[175], cy[176], cx[179], cy[177], 0xFF2F3336);
        p.fill(cx[181], cy[176], cx[185], cy[177], 0xFF2F3336);
        p.fill(cx[231], cy[176], cx[235], cy[177], 0xFF2F3336);

        p.fill(cx[348], cy[176], cx[350], cy[178], 0xFF61625E);

        p.fill(cx[65], cy[177], cx[69], cy[178], 0xFF565B54);

        p.fill(cx[85], cy[177], cx[89], cy[178], 0xFF51504E);
        p.fill(cx[221], cy[177], cx[225], cy[178], 0xFF51504E);

        p.fill(cx[230], cy[177], cx[231], cy[181], 0xFF3C3F3B);

        p.fill(cx[316], cy[177], cx[317], cy[181], 0xFF2F3336);

        p.fill(cx[358], cy[177], cx[360], cy[179], 0xFF797D7C);

        p.fill(cx[46], cy[178], cx[50], cy[179], 0xFF2F3336);

        p.fill(cx[112], cy[178], cx[116], cy[179], 0xFF42473F);
        p.fill(cx[136], cy[178], cx[140], cy[179], 0xFF42473F);
        p.fill(cx[223], cy[178], cx[224], cy[182], 0xFF42473F);

        p.fill(cx[238], cy[178], cx[240], cy[180], 0xFF51504E);

        p.fill(cx[318], cy[178], cx[319], cy[182], 0xFF2F3336);

        p.fill(cx[0], cy[179], cx[4], cy[180], 0xFF8A8C83);

        p.fill(cx[41], cy[179], cx[43], cy[181], 0xFF3C3F3B);
        p.fill(cx[44], cy[179], cx[48], cy[180], 0xFF3C3F3B);

        p.fill(cx[68], cy[179], cx[72], cy[180], 0xFF42473F);
        p.fill(cx[88], cy[179], cx[92], cy[180], 0xFF42473F);
        p.fill(cx[110], cy[179], cx[114], cy[180], 0xFF42473F);

        p.fill(cx[114], cy[179], cx[116], cy[181], 0xFF51504E);
        p.fill(cx[139], cy[179], cx[140], cy[183], 0xFF51504E);

        p.fill(cx[190], cy[179], cx[194], cy[180], 0xFF42473F);

        p.fill(cx[30], cy[180], cx[32], cy[182], 0xFF2F3336);
        p.fill(cx[44], cy[180], cx[45], cy[184], 0xFF2F3336);

        p.fill(cx[45], cy[180], cx[49], cy[181], 0xFF3C3F3B);

        p.fill(cx[68], cy[180], cx[72], cy[181], 0xFF51504E);

        p.fill(cx[93], cy[180], cx[95], cy[182], 0xFF42473F);

        p.fill(cx[215], cy[180], cx[219], cy[181], 0xFF51504E);

        p.fill(cx[45], cy[181], cx[49], cy[182], 0xFF2F3336);

        p.fill(cx[219], cy[181], cx[223], cy[182], 0xFF51504E);

        p.fill(cx[245], cy[181], cx[249], cy[182], 0xFF42473F);

        p.fill(cx[268], cy[181], cx[272], cy[182], 0xFF51504E);

        p.fill(cx[56], cy[182], cx[57], cy[186], 0xFF3C3F3B);

        p.fill(cx[57], cy[182], cx[58], cy[186], 0xFF202423);

        p.fill(cx[125], cy[182], cx[126], cy[186], 0xFF2F3336);

        p.fill(cx[145], cy[182], cx[147], cy[184], 0xFF51504E);
        p.fill(cx[213], cy[182], cx[215], cy[184], 0xFF51504E);
        p.fill(cx[248], cy[182], cx[249], cy[186], 0xFF51504E);

        p.fill(cx[271], cy[182], cx[273], cy[184], 0xFF42473F);

        p.fill(cx[313], cy[182], cx[315], cy[184], 0xFF2F3336);

        p.fill(cx[3], cy[183], cx[7], cy[184], 0xFF8B9494);

        p.fill(cx[88], cy[183], cx[89], cy[187], 0xFF42473F);

        p.fill(cx[96], cy[183], cx[98], cy[185], 0xFF51504E);

        p.fill(cx[118], cy[183], cx[119], cy[187], 0xFF42473F);

        p.fill(cx[123], cy[183], cx[124], cy[187], 0xFF51504E);

        p.fill(cx[139], cy[183], cx[140], cy[187], 0xFF42473F);
        p.fill(cx[193], cy[183], cx[194], cy[187], 0xFF42473F);

        p.fill(cx[201], cy[183], cx[202], cy[187], 0xFF202423);

        p.fill(cx[217], cy[183], cx[218], cy[187], 0xFF42473F);
        p.fill(cx[237], cy[183], cx[238], cy[187], 0xFF42473F);
        p.fill(cx[264], cy[183], cx[265], cy[187], 0xFF42473F);
        p.fill(cx[266], cy[183], cx[267], cy[187], 0xFF42473F);

        p.fill(cx[321], cy[183], cx[323], cy[185], 0xFF2F3336);

        p.fill(cx[92], cy[184], cx[94], cy[186], 0xFF51504E);

        p.fill(cx[94], cy[184], cx[96], cy[186], 0xFF42473F);
        p.fill(cx[114], cy[184], cx[115], cy[188], 0xFF42473F);
        p.fill(cx[119], cy[184], cx[120], cy[188], 0xFF42473F);

        p.fill(cx[243], cy[184], cx[245], cy[186], 0xFF51504E);

        p.fill(cx[356], cy[184], cx[358], cy[186], 0xFF797D7C);

        p.fill(cx[30], cy[185], cx[31], cy[189], 0xFF2F3336);

        p.fill(cx[66], cy[185], cx[68], cy[187], 0xFF51504E);

        p.fill(cx[268], cy[185], cx[270], cy[187], 0xFF42473F);

        p.fill(cx[314], cy[185], cx[315], cy[189], 0xFF2F3336);

        p.fill(cx[17], cy[186], cx[21], cy[187], 0xFF202423);

        p.fill(cx[43], cy[186], cx[45], cy[188], 0xFF2F3336);
        p.fill(cx[56], cy[186], cx[58], cy[188], 0xFF2F3336);

        p.fill(cx[91], cy[186], cx[95], cy[187], 0xFF42473F);
        p.fill(cx[148], cy[186], cx[150], cy[188], 0xFF42473F);

        p.fill(cx[339], cy[186], cx[343], cy[187], 0xFF202423);

        p.fill(cx[62], cy[187], cx[66], cy[188], 0xFF565B54);
        p.fill(cx[67], cy[187], cx[71], cy[188], 0xFF565B54);

        p.fill(cx[93], cy[187], cx[97], cy[188], 0xFF61625E);

        p.fill(cx[269], cy[187], cx[273], cy[188], 0xFF565B54);

        p.fill(cx[316], cy[188], cx[317], cy[192], 0xFF3C3F3B);
        p.fill(cx[331], cy[188], cx[332], cy[192], 0xFF3C3F3B);

        p.fill(cx[333], cy[188], cx[335], cy[190], 0xFF202423);

        p.fill(cx[358], cy[188], cx[360], cy[190], 0xFF676763);

        p.fill(cx[7], cy[189], cx[9], cy[191], 0xFF565B54);

        p.fill(cx[57], cy[189], cx[58], cy[193], 0xFF2F3336);
        p.fill(cx[59], cy[189], cx[60], cy[193], 0xFF2F3336);
        p.fill(cx[68], cy[189], cx[69], cy[193], 0xFF2F3336);
        p.fill(cx[99], cy[189], cx[100], cy[193], 0xFF2F3336);
        p.fill(cx[105], cy[189], cx[106], cy[193], 0xFF2F3336);
        p.fill(cx[124], cy[189], cx[125], cy[193], 0xFF2F3336);
        p.fill(cx[180], cy[189], cx[181], cy[193], 0xFF2F3336);

        p.fill(cx[189], cy[189], cx[193], cy[190], 0xFF202423);

        p.fill(cx[274], cy[189], cx[275], cy[193], 0xFF2F3336);
        p.fill(cx[285], cy[189], cx[286], cy[193], 0xFF2F3336);
        p.fill(cx[302], cy[189], cx[303], cy[193], 0xFF2F3336);

        p.fill(cx[326], cy[189], cx[330], cy[190], 0xFF3C3F3B);

        p.fill(cx[350], cy[189], cx[351], cy[193], 0xFF51504E);

        p.fill(cx[2], cy[190], cx[6], cy[191], 0xFF61625E);

        p.fill(cx[333], cy[190], cx[334], cy[194], 0xFF202423);

        p.fill(cx[323], cy[191], cx[324], cy[195], 0xFF2F3336);

        p.fill(cx[356], cy[191], cx[360], cy[192], 0xFF61625E);

        p.fill(cx[6], cy[193], cx[7], cy[197], 0xFF51504E);

        p.fill(cx[27], cy[193], cx[29], cy[195], 0xFF2F3336);

        p.fill(cx[275], cy[193], cx[279], cy[194], 0xFF3C3F3B);

        p.fill(cx[354], cy[193], cx[355], cy[197], 0xFF51504E);

        p.fill(cx[0], cy[194], cx[4], cy[195], 0xFF565B54);

        p.fill(cx[50], cy[194], cx[51], cy[198], 0xFF2F3336);
        p.fill(cx[52], cy[194], cx[54], cy[196], 0xFF2F3336);

        p.fill(cx[355], cy[194], cx[356], cy[198], 0xFF51504E);

        p.fill(cx[80], cy[195], cx[81], cy[199], 0xFF3C3F3B);
        p.fill(cx[223], cy[195], cx[227], cy[196], 0xFF3C3F3B);
        p.fill(cx[265], cy[195], cx[269], cy[196], 0xFF3C3F3B);

        p.fill(cx[269], cy[195], cx[270], cy[199], 0xFF42473F);

        p.fill(cx[320], cy[195], cx[324], cy[196], 0xFF3C3F3B);

        p.fill(cx[356], cy[195], cx[357], cy[199], 0xFF51504E);

        p.fill(cx[76], cy[196], cx[80], cy[197], 0xFF3C3F3B);
        p.fill(cx[105], cy[196], cx[109], cy[197], 0xFF3C3F3B);

        p.fill(cx[268], cy[196], cx[269], cy[200], 0xFF42473F);

        p.fill(cx[22], cy[197], cx[23], cy[201], 0xFF3C3F3B);
        p.fill(cx[337], cy[197], cx[338], cy[201], 0xFF3C3F3B);
        p.fill(cx[79], cy[198], cx[80], cy[202], 0xFF3C3F3B);

        p.fill(cx[272], cy[198], cx[273], cy[202], 0xFF3D3E3D);

        p.fill(cx[3], cy[199], cx[4], cy[203], 0xFF42473F);

        p.fill(cx[12], cy[199], cx[13], cy[203], 0xFF3C3F3B);
        p.fill(cx[82], cy[199], cx[83], cy[203], 0xFF3C3F3B);

        p.fill(cx[336], cy[199], cx[337], cy[203], 0xFF2F3336);

        p.fill(cx[347], cy[199], cx[348], cy[203], 0xFF3C3F3B);

        p.fill(cx[359], cy[199], cx[360], cy[203], 0xFF42473F);

        p.fill(cx[22], cy[201], cx[24], cy[203], 0xFF2F3336);

        p.fill(cx[64], cy[201], cx[66], cy[203], 0xFF3C3F3B);

        p.fill(cx[76], cy[201], cx[78], cy[203], 0xFF3D3E3D);

        p.fill(cx[279], cy[201], cx[281], cy[203], 0xFF3C3F3B);

        p.fill(cx[316], cy[201], cx[318], cy[203], 0xFF2F3336);

        p.fill(cx[70], cy[202], cx[74], cy[203], 0xFF42473F);

        p.fill(cx[74], cy[0], cx[77], cy[1], 0xFF565B54);

        p.fill(cx[206], cy[0], cx[209], cy[1], 0xFF676763);

        p.fill(cx[209], cy[0], cx[212], cy[1], 0xFF61625E);

        p.fill(cx[316], cy[0], cx[317], cy[3], 0xFF676763);

        p.fill(cx[110], cy[1], cx[111], cy[4], 0xFF61625E);

        p.fill(cx[216], cy[1], cx[217], cy[4], 0xFF676763);

        p.fill(cx[211], cy[2], cx[212], cy[5], 0xFF61625E);
        p.fill(cx[213], cy[2], cx[214], cy[5], 0xFF61625E);

        p.fill(cx[308], cy[3], cx[311], cy[4], 0xFF565B54);
        p.fill(cx[37], cy[4], cx[40], cy[5], 0xFF565B54);

        p.fill(cx[214], cy[4], cx[217], cy[5], 0xFF61625E);
        p.fill(cx[234], cy[4], cx[237], cy[5], 0xFF61625E);

        p.fill(cx[301], cy[4], cx[302], cy[7], 0xFF565B54);
        p.fill(cx[307], cy[4], cx[310], cy[5], 0xFF565B54);

        p.fill(cx[310], cy[4], cx[311], cy[7], 0xFF676763);
        p.fill(cx[316], cy[4], cx[317], cy[7], 0xFF676763);
        p.fill(cx[328], cy[4], cx[329], cy[7], 0xFF676763);

        p.fill(cx[346], cy[4], cx[349], cy[5], 0xFF61625E);

        p.fill(cx[13], cy[5], cx[16], cy[6], 0xFF565B54);

        p.fill(cx[81], cy[5], cx[82], cy[8], 0xFF42473F);

        p.fill(cx[298], cy[5], cx[301], cy[6], 0xFF565B54);

        p.fill(cx[302], cy[5], cx[305], cy[6], 0xFF51504E);

        p.fill(cx[305], cy[5], cx[308], cy[6], 0xFF42473F);

        p.fill(cx[331], cy[5], cx[332], cy[8], 0xFF676763);

        p.fill(cx[354], cy[5], cx[355], cy[8], 0xFF6D6A64);
        p.fill(cx[0], cy[6], cx[1], cy[9], 0xFF6D6A64);

        p.fill(cx[30], cy[6], cx[33], cy[7], 0xFF42473F);
        p.fill(cx[62], cy[6], cx[63], cy[9], 0xFF42473F);
        p.fill(cx[70], cy[6], cx[71], cy[9], 0xFF42473F);
        p.fill(cx[289], cy[6], cx[290], cy[9], 0xFF42473F);

        p.fill(cx[353], cy[6], cx[354], cy[9], 0xFF6D6A64);

        p.fill(cx[37], cy[7], cx[40], cy[8], 0xFF51504E);

        p.fill(cx[46], cy[7], cx[49], cy[8], 0xFF565B54);

        p.fill(cx[71], cy[7], cx[74], cy[8], 0xFF42473F);

        p.fill(cx[145], cy[7], cx[148], cy[8], 0xFF565B54);

        p.fill(cx[161], cy[7], cx[164], cy[8], 0xFF51504E);

        p.fill(cx[199], cy[7], cx[202], cy[8], 0xFF565B54);
        p.fill(cx[219], cy[7], cx[222], cy[8], 0xFF565B54);

        p.fill(cx[229], cy[7], cx[232], cy[8], 0xFF42473F);

        p.fill(cx[233], cy[7], cx[236], cy[8], 0xFF565B54);

        p.fill(cx[237], cy[7], cx[240], cy[8], 0xFF42473F);
        p.fill(cx[248], cy[7], cx[251], cy[8], 0xFF42473F);

        p.fill(cx[251], cy[7], cx[254], cy[8], 0xFF51504E);

        p.fill(cx[265], cy[7], cx[268], cy[8], 0xFF565B54);

        p.fill(cx[350], cy[7], cx[353], cy[8], 0xFF676763);
        p.fill(cx[9], cy[8], cx[10], cy[11], 0xFF676763);

        p.fill(cx[317], cy[8], cx[318], cy[11], 0xFF42473F);

        p.fill(cx[349], cy[8], cx[352], cy[9], 0xFF676763);

        p.fill(cx[84], cy[9], cx[87], cy[10], 0xFF42473F);

        p.fill(cx[284], cy[9], cx[287], cy[10], 0xFF3C3F3B);
        p.fill(cx[339], cy[9], cx[340], cy[12], 0xFF3C3F3B);
        p.fill(cx[22], cy[10], cx[25], cy[11], 0xFF3C3F3B);

        p.fill(cx[82], cy[10], cx[85], cy[11], 0xFF42473F);
        p.fill(cx[281], cy[10], cx[282], cy[13], 0xFF42473F);
        p.fill(cx[307], cy[10], cx[308], cy[13], 0xFF42473F);

        p.fill(cx[6], cy[11], cx[9], cy[12], 0xFFA5A49E);

        p.fill(cx[21], cy[11], cx[24], cy[12], 0xFF3C3F3B);

        p.fill(cx[90], cy[11], cx[93], cy[12], 0xFF565B54);

        p.fill(cx[125], cy[11], cx[128], cy[12], 0xFF3C3F3B);

        p.fill(cx[129], cy[11], cx[130], cy[14], 0xFF42473F);

        p.fill(cx[151], cy[11], cx[154], cy[12], 0xFF3C3F3B);
        p.fill(cx[182], cy[11], cx[185], cy[12], 0xFF3C3F3B);
        p.fill(cx[207], cy[11], cx[210], cy[12], 0xFF3C3F3B);

        p.fill(cx[222], cy[11], cx[225], cy[12], 0xFF51504E);

        p.fill(cx[226], cy[11], cx[229], cy[12], 0xFF3C3F3B);
        p.fill(cx[232], cy[11], cx[235], cy[12], 0xFF3C3F3B);

        p.fill(cx[247], cy[11], cx[250], cy[12], 0xFF51504E);

        p.fill(cx[251], cy[11], cx[254], cy[12], 0xFF3C3F3B);
        p.fill(cx[257], cy[11], cx[260], cy[12], 0xFF3C3F3B);

        p.fill(cx[288], cy[11], cx[291], cy[12], 0xFF565B54);

        p.fill(cx[321], cy[11], cx[322], cy[14], 0xFF42473F);
        p.fill(cx[327], cy[11], cx[330], cy[12], 0xFF42473F);

        p.fill(cx[336], cy[11], cx[337], cy[14], 0xFF3C3F3B);

        p.fill(cx[16], cy[12], cx[17], cy[15], 0xFF202423);
        p.fill(cx[21], cy[12], cx[22], cy[15], 0xFF202423);

        p.fill(cx[47], cy[12], cx[50], cy[13], 0xFF42473F);

        p.fill(cx[59], cy[12], cx[60], cy[15], 0xFF51504E);

        p.fill(cx[69], cy[12], cx[70], cy[15], 0xFF42473F);

        p.fill(cx[104], cy[12], cx[105], cy[15], 0xFF3C3F3B);

        p.fill(cx[289], cy[12], cx[292], cy[13], 0xFF42473F);

        p.fill(cx[343], cy[12], cx[344], cy[15], 0xFF202423);

        p.fill(cx[4], cy[13], cx[5], cy[16], 0xFFA5A49E);

        p.fill(cx[70], cy[13], cx[71], cy[16], 0xFF42473F);

        p.fill(cx[143], cy[13], cx[144], cy[16], 0xFF51504E);

        p.fill(cx[152], cy[13], cx[153], cy[16], 0xFF2F3336);

        p.fill(cx[355], cy[13], cx[356], cy[16], 0xFFD1D2C3);

        p.fill(cx[86], cy[14], cx[89], cy[15], 0xFF42473F);

        p.fill(cx[110], cy[14], cx[111], cy[17], 0xFF51504E);
        p.fill(cx[246], cy[14], cx[247], cy[17], 0xFF51504E);

        p.fill(cx[299], cy[14], cx[300], cy[17], 0xFF42473F);

        p.fill(cx[320], cy[14], cx[323], cy[15], 0xFF3D3E3D);

        p.fill(cx[328], cy[14], cx[329], cy[17], 0xFF42473F);

        p.fill(cx[80], cy[15], cx[81], cy[18], 0xFF2F3336);

        p.fill(cx[146], cy[15], cx[149], cy[16], 0xFF42473F);

        p.fill(cx[214], cy[15], cx[217], cy[16], 0xFF51504E);
        p.fill(cx[222], cy[15], cx[223], cy[18], 0xFF51504E);

        p.fill(cx[232], cy[15], cx[233], cy[18], 0xFF2F3336);

        p.fill(cx[265], cy[15], cx[266], cy[18], 0xFFFBFBFB);

        p.fill(cx[170], cy[16], cx[173], cy[17], 0xFF42473F);

        p.fill(cx[241], cy[16], cx[244], cy[17], 0xFF2F3336);
        p.fill(cx[339], cy[16], cx[340], cy[19], 0xFF2F3336);

        p.fill(cx[62], cy[17], cx[65], cy[18], 0xFF42473F);
        p.fill(cx[69], cy[17], cx[72], cy[18], 0xFF42473F);

        p.fill(cx[87], cy[17], cx[90], cy[18], 0xFF51504E);

        p.fill(cx[110], cy[17], cx[111], cy[20], 0xFF42473F);
        p.fill(cx[148], cy[17], cx[149], cy[20], 0xFF42473F);

        p.fill(cx[149], cy[17], cx[150], cy[20], 0xFF51504E);
        p.fill(cx[249], cy[17], cx[250], cy[20], 0xFF51504E);
        p.fill(cx[299], cy[17], cx[300], cy[20], 0xFF51504E);

        p.fill(cx[328], cy[17], cx[331], cy[18], 0xFF3D3E3D);

        p.fill(cx[354], cy[17], cx[357], cy[18], 0xFF8A8C83);

        p.fill(cx[18], cy[18], cx[21], cy[19], 0xFF2F3336);
        p.fill(cx[83], cy[18], cx[84], cy[21], 0xFF2F3336);

        p.fill(cx[164], cy[18], cx[167], cy[19], 0xFF51504E);

        p.fill(cx[167], cy[18], cx[168], cy[21], 0xFF42473F);

        p.fill(cx[218], cy[18], cx[221], cy[19], 0xFF51504E);

        p.fill(cx[245], cy[18], cx[246], cy[21], 0xFF42473F);
        p.fill(cx[264], cy[18], cx[265], cy[21], 0xFF42473F);
        p.fill(cx[270], cy[18], cx[271], cy[21], 0xFF42473F);

        p.fill(cx[293], cy[18], cx[296], cy[19], 0xFF51504E);
        p.fill(cx[297], cy[18], cx[298], cy[21], 0xFF51504E);
        p.fill(cx[68], cy[19], cx[69], cy[22], 0xFF51504E);

        p.fill(cx[73], cy[19], cx[74], cy[22], 0xFF2F3336);

        p.fill(cx[89], cy[19], cx[90], cy[22], 0xFF51504E);

        p.fill(cx[174], cy[19], cx[175], cy[22], 0xFF42473F);
        p.fill(cx[230], cy[19], cx[231], cy[22], 0xFF42473F);

        p.fill(cx[266], cy[19], cx[269], cy[20], 0xFF51504E);
        p.fill(cx[291], cy[19], cx[292], cy[22], 0xFF51504E);

        p.fill(cx[325], cy[19], cx[326], cy[22], 0xFF3D3E3D);

        p.fill(cx[16], cy[20], cx[19], cy[21], 0xFF42473F);

        p.fill(cx[227], cy[20], cx[228], cy[23], 0xFF3C3F3B);

        p.fill(cx[279], cy[20], cx[280], cy[23], 0xFF2F3336);

        p.fill(cx[334], cy[20], cx[337], cy[21], 0xFF42473F);

        p.fill(cx[14], cy[21], cx[17], cy[22], 0xFF3D3E3D);

        p.fill(cx[17], cy[21], cx[20], cy[22], 0xFF42473F);

        p.fill(cx[134], cy[21], cx[135], cy[24], 0xFF2F3336);

        p.fill(cx[176], cy[21], cx[177], cy[24], 0xFF3C3F3B);

        p.fill(cx[187], cy[21], cx[190], cy[22], 0xFF2F3336);

        p.fill(cx[293], cy[21], cx[296], cy[22], 0xFF51504E);

        p.fill(cx[348], cy[21], cx[349], cy[24], 0xFF6D6A64);

        p.fill(cx[74], cy[22], cx[75], cy[25], 0xFF3C3F3B);

        p.fill(cx[209], cy[22], cx[212], cy[23], 0xFF2F3336);

        p.fill(cx[285], cy[22], cx[286], cy[25], 0xFF3C3F3B);

        p.fill(cx[7], cy[23], cx[8], cy[26], 0xFF6D6A64);

        p.fill(cx[58], cy[23], cx[59], cy[26], 0xFF3C3F3B);

        p.fill(cx[60], cy[23], cx[63], cy[24], 0xFF565B54);

        p.fill(cx[63], cy[23], cx[66], cy[24], 0xFF202423);

        p.fill(cx[124], cy[23], cx[127], cy[24], 0xFF3C3F3B);
        p.fill(cx[199], cy[23], cx[202], cy[24], 0xFF3C3F3B);
        p.fill(cx[229], cy[23], cx[232], cy[24], 0xFF3C3F3B);
        p.fill(cx[248], cy[23], cx[251], cy[24], 0xFF3C3F3B);
        p.fill(cx[104], cy[25], cx[107], cy[26], 0xFF3C3F3B);
        p.fill(cx[159], cy[25], cx[162], cy[26], 0xFF3C3F3B);

        p.fill(cx[184], cy[25], cx[185], cy[28], 0xFF2F3336);
        p.fill(cx[211], cy[25], cx[212], cy[28], 0xFF2F3336);
        p.fill(cx[88], cy[27], cx[91], cy[28], 0xFF2F3336);

        p.fill(cx[2], cy[28], cx[5], cy[29], 0xFF676763);

        p.fill(cx[42], cy[28], cx[45], cy[29], 0xFF2F3336);
        p.fill(cx[310], cy[28], cx[313], cy[29], 0xFF2F3336);

        p.fill(cx[42], cy[29], cx[45], cy[30], 0xFF202423);

        p.fill(cx[5], cy[30], cx[6], cy[33], 0xFF676763);

        p.fill(cx[30], cy[30], cx[33], cy[31], 0xFF3C3F3B);

        p.fill(cx[40], cy[30], cx[41], cy[33], 0xFF2F3336);
        p.fill(cx[319], cy[30], cx[320], cy[33], 0xFF2F3336);
        p.fill(cx[320], cy[31], cx[321], cy[34], 0xFF2F3336);

        p.fill(cx[10], cy[32], cx[11], cy[35], 0xFF676763);

        p.fill(cx[39], cy[33], cx[42], cy[34], 0xFF3C3F3B);

        p.fill(cx[318], cy[34], cx[321], cy[35], 0xFF42473F);
        p.fill(cx[317], cy[35], cx[320], cy[36], 0xFF42473F);

        p.fill(cx[8], cy[37], cx[9], cy[40], 0xFF3C3F3B);

        p.fill(cx[30], cy[37], cx[31], cy[40], 0xFF51504E);

        p.fill(cx[40], cy[37], cx[43], cy[38], 0xFF3C3F3B);

        p.fill(cx[317], cy[37], cx[318], cy[40], 0xFF2F3336);
        p.fill(cx[318], cy[38], cx[319], cy[41], 0xFF2F3336);

        p.fill(cx[6], cy[40], cx[7], cy[43], 0xFF42473F);

        p.fill(cx[351], cy[40], cx[352], cy[43], 0xFF565B54);

        p.fill(cx[38], cy[41], cx[39], cy[44], 0xFF2F3336);

        p.fill(cx[348], cy[41], cx[351], cy[42], 0xFF6D6A64);

        p.fill(cx[21], cy[42], cx[24], cy[43], 0xFF202423);

        p.fill(cx[354], cy[43], cx[355], cy[46], 0xFF565B54);

        p.fill(cx[326], cy[44], cx[327], cy[47], 0xFF2F3336);

        p.fill(cx[351], cy[44], cx[354], cy[45], 0xFF565B54);

        p.fill(cx[330], cy[47], cx[333], cy[48], 0xFF42473F);

        p.fill(cx[19], cy[48], cx[20], cy[51], 0xFF51504E);

        p.fill(cx[9], cy[50], cx[12], cy[51], 0xFF565B54);

        p.fill(cx[34], cy[50], cx[35], cy[53], 0xFF202423);

        p.fill(cx[333], cy[50], cx[336], cy[51], 0xFF51504E);
        p.fill(cx[334], cy[51], cx[337], cy[52], 0xFF51504E);

        p.fill(cx[37], cy[52], cx[40], cy[53], 0xFFFBFBFB);

        p.fill(cx[25], cy[54], cx[26], cy[57], 0xFF51504E);

        p.fill(cx[317], cy[54], cx[318], cy[57], 0xFF3C3F3B);

        p.fill(cx[20], cy[57], cx[23], cy[58], 0xFF2F3336);

        p.fill(cx[333], cy[57], cx[336], cy[58], 0xFF565B54);

        p.fill(cx[341], cy[57], cx[342], cy[60], 0xFF2F3336);
        p.fill(cx[342], cy[58], cx[343], cy[61], 0xFF2F3336);

        p.fill(cx[351], cy[62], cx[352], cy[65], 0xFF61625E);

        p.fill(cx[42], cy[64], cx[43], cy[67], 0xFF3C3F3B);
        p.fill(cx[26], cy[65], cx[29], cy[66], 0xFF3C3F3B);

        p.fill(cx[23], cy[66], cx[26], cy[67], 0xFF565B54);
        p.fill(cx[329], cy[66], cx[330], cy[69], 0xFF565B54);

        p.fill(cx[341], cy[66], cx[342], cy[69], 0xFF51504E);

        p.fill(cx[317], cy[67], cx[318], cy[70], 0xFF3C3F3B);

        p.fill(cx[320], cy[69], cx[323], cy[70], 0xFF2F3336);

        p.fill(cx[351], cy[69], cx[352], cy[72], 0xFF61625E);

        p.fill(cx[352], cy[69], cx[353], cy[72], 0xFF51504E);

        p.fill(cx[42], cy[70], cx[43], cy[73], 0xFF42473F);
        p.fill(cx[8], cy[72], cx[9], cy[75], 0xFF42473F);

        p.fill(cx[351], cy[72], cx[352], cy[75], 0xFF51504E);
        p.fill(cx[18], cy[73], cx[19], cy[76], 0xFF51504E);

        p.fill(cx[42], cy[73], cx[43], cy[76], 0xFF3C3F3B);

        p.fill(cx[331], cy[75], cx[334], cy[76], 0xFF565B54);

        p.fill(cx[17], cy[76], cx[18], cy[79], 0xFF2F3336);
        p.fill(cx[31], cy[76], cx[32], cy[79], 0xFF2F3336);
        p.fill(cx[342], cy[76], cx[343], cy[79], 0xFF2F3336);
        p.fill(cx[8], cy[77], cx[9], cy[80], 0xFF2F3336);

        p.fill(cx[350], cy[77], cx[351], cy[80], 0xFF676763);

        p.fill(cx[17], cy[81], cx[20], cy[82], 0xFF2F3336);
        p.fill(cx[340], cy[81], cx[343], cy[82], 0xFF2F3336);

        p.fill(cx[346], cy[81], cx[347], cy[84], 0xFF565B54);

        p.fill(cx[19], cy[82], cx[22], cy[83], 0xFF61625E);
        p.fill(cx[26], cy[82], cx[29], cy[83], 0xFF61625E);

        p.fill(cx[39], cy[82], cx[40], cy[85], 0xFF2F3336);

        p.fill(cx[336], cy[84], cx[337], cy[87], 0xFF51504E);

        p.fill(cx[39], cy[85], cx[40], cy[88], 0xFF3C3F3B);

        p.fill(cx[350], cy[87], cx[351], cy[90], 0xFF676763);

        p.fill(cx[320], cy[88], cx[323], cy[89], 0xFFECEEEF);

        p.fill(cx[337], cy[88], cx[338], cy[91], 0xFF42473F);

        p.fill(cx[352], cy[89], cx[353], cy[92], 0xFF61625E);

        p.fill(cx[25], cy[90], cx[26], cy[93], 0xFF51504E);

        p.fill(cx[351], cy[93], cx[352], cy[96], 0xFF565B54);

        p.fill(cx[34], cy[94], cx[35], cy[97], 0xFF42473F);

        p.fill(cx[19], cy[96], cx[22], cy[97], 0xFF2F3336);
        p.fill(cx[338], cy[96], cx[341], cy[97], 0xFF2F3336);

        p.fill(cx[19], cy[97], cx[22], cy[98], 0xFF202423);

        p.fill(cx[34], cy[97], cx[35], cy[100], 0xFF3C3F3B);

        p.fill(cx[339], cy[100], cx[342], cy[101], 0xFF51504E);

        p.fill(cx[331], cy[101], cx[334], cy[102], 0xFF42473F);

        p.fill(cx[19], cy[102], cx[20], cy[105], 0xFF51504E);

        p.fill(cx[29], cy[104], cx[30], cy[107], 0xFF565B54);

        p.fill(cx[332], cy[104], cx[333], cy[107], 0xFF51504E);
        p.fill(cx[12], cy[105], cx[13], cy[108], 0xFF51504E);
        p.fill(cx[26], cy[105], cx[29], cy[106], 0xFF51504E);

        p.fill(cx[23], cy[106], cx[26], cy[107], 0xFFFBFBFB);

        p.fill(cx[334], cy[106], cx[335], cy[109], 0xFF51504E);

        p.fill(cx[350], cy[106], cx[351], cy[109], 0xFF565B54);

        p.fill(cx[340], cy[107], cx[341], cy[110], 0xFF42473F);

        p.fill(cx[8], cy[108], cx[9], cy[111], 0xFF3C3F3B);

        p.fill(cx[8], cy[111], cx[9], cy[114], 0xFF676763);

        p.fill(cx[340], cy[112], cx[341], cy[115], 0xFF2F3336);
        p.fill(cx[331], cy[114], cx[334], cy[115], 0xFF2F3336);
        p.fill(cx[17], cy[115], cx[18], cy[118], 0xFF2F3336);
        p.fill(cx[18], cy[117], cx[21], cy[118], 0xFF2F3336);
        p.fill(cx[327], cy[117], cx[330], cy[118], 0xFF2F3336);
        p.fill(cx[41], cy[118], cx[42], cy[121], 0xFF2F3336);

        p.fill(cx[329], cy[119], cx[330], cy[122], 0xFF51504E);

        p.fill(cx[331], cy[119], cx[334], cy[120], 0xFF3C3F3B);

        p.fill(cx[37], cy[124], cx[40], cy[125], 0xFFFBFBFB);

        p.fill(cx[320], cy[124], cx[323], cy[125], 0xFFECEEEF);
    }

    private static void part5(Paint p, int[] cx, int[] cy) {

        p.fill(cx[25], cy[126], cx[26], cy[129], 0xFF51504E);
        p.fill(cx[29], cy[126], cx[30], cy[129], 0xFF51504E);
        p.fill(cx[351], cy[126], cx[352], cy[129], 0xFF51504E);
        p.fill(cx[341], cy[127], cx[342], cy[130], 0xFF51504E);

        p.fill(cx[34], cy[131], cx[35], cy[134], 0xFF42473F);

        p.fill(cx[328], cy[131], cx[331], cy[132], 0xFF2F3336);
        p.fill(cx[31], cy[132], cx[34], cy[133], 0xFF2F3336);

        p.fill(cx[328], cy[132], cx[331], cy[133], 0xFF202423);
        p.fill(cx[23], cy[133], cx[26], cy[134], 0xFF202423);
        p.fill(cx[326], cy[133], cx[329], cy[134], 0xFF202423);

        p.fill(cx[18], cy[136], cx[21], cy[137], 0xFF565B54);

        p.fill(cx[332], cy[136], cx[335], cy[137], 0xFF51504E);

        p.fill(cx[351], cy[136], cx[352], cy[139], 0xFF565B54);

        p.fill(cx[42], cy[137], cx[43], cy[140], 0xFF3C3F3B);

        p.fill(cx[29], cy[138], cx[30], cy[141], 0xFF565B54);

        p.fill(cx[331], cy[139], cx[332], cy[142], 0xFF51504E);
        p.fill(cx[341], cy[142], cx[342], cy[145], 0xFF51504E);

        p.fill(cx[352], cy[143], cx[353], cy[146], 0xFF42473F);

        p.fill(cx[25], cy[144], cx[26], cy[147], 0xFF51504E);

        p.fill(cx[335], cy[144], cx[338], cy[145], 0xFF42473F);

        p.fill(cx[350], cy[144], cx[351], cy[147], 0xFF61625E);

        p.fill(cx[41], cy[146], cx[42], cy[149], 0xFF3C3F3B);
        p.fill(cx[352], cy[146], cx[353], cy[149], 0xFF3C3F3B);

        p.fill(cx[335], cy[147], cx[338], cy[148], 0xFF565B54);

        p.fill(cx[347], cy[156], cx[348], cy[159], 0xFF676763);

        p.fill(cx[348], cy[156], cx[351], cy[157], 0xFF61625E);

        p.fill(cx[356], cy[159], cx[357], cy[162], 0xFF565B54);

        p.fill(cx[41], cy[160], cx[42], cy[163], 0xFF3C3F3B);

        p.fill(cx[340], cy[160], cx[343], cy[161], 0xFF2F3336);

        p.fill(cx[13], cy[162], cx[14], cy[165], 0xFF3C3F3B);

        p.fill(cx[317], cy[164], cx[320], cy[165], 0xFF565B54);

        p.fill(cx[317], cy[165], cx[318], cy[168], 0xFF61625E);

        p.fill(cx[87], cy[166], cx[90], cy[167], 0xFF42473F);

        p.fill(cx[168], cy[166], cx[171], cy[167], 0xFF51504E);
        p.fill(cx[185], cy[166], cx[188], cy[167], 0xFF51504E);
        p.fill(cx[238], cy[166], cx[241], cy[167], 0xFF51504E);
        p.fill(cx[243], cy[166], cx[246], cy[167], 0xFF51504E);
        p.fill(cx[356], cy[166], cx[357], cy[169], 0xFF51504E);

        p.fill(cx[42], cy[167], cx[45], cy[168], 0xFF565B54);

        p.fill(cx[45], cy[167], cx[48], cy[168], 0xFF51504E);

        p.fill(cx[57], cy[167], cx[60], cy[168], 0xFF42473F);
        p.fill(cx[168], cy[167], cx[171], cy[168], 0xFF42473F);
        p.fill(cx[185], cy[167], cx[188], cy[168], 0xFF42473F);
        p.fill(cx[238], cy[167], cx[241], cy[168], 0xFF42473F);

        p.fill(cx[57], cy[168], cx[60], cy[169], 0xFF3C3F3B);

        p.fill(cx[79], cy[168], cx[82], cy[169], 0xFF51504E);
        p.fill(cx[93], cy[168], cx[96], cy[169], 0xFF51504E);
        p.fill(cx[103], cy[168], cx[106], cy[169], 0xFF51504E);

        p.fill(cx[106], cy[168], cx[109], cy[169], 0xFF42473F);

        p.fill(cx[167], cy[168], cx[168], cy[171], 0xFFECEEEF);

        p.fill(cx[192], cy[168], cx[193], cy[171], 0xFFD1D2C3);

        p.fill(cx[265], cy[168], cx[268], cy[169], 0xFF3C3F3B);

        p.fill(cx[268], cy[168], cx[269], cy[171], 0xFFD1D2C3);

        p.fill(cx[270], cy[168], cx[273], cy[169], 0xFF42473F);

        p.fill(cx[280], cy[168], cx[283], cy[169], 0xFF3C3F3B);
        p.fill(cx[343], cy[169], cx[346], cy[170], 0xFF3C3F3B);

        p.fill(cx[193], cy[170], cx[194], cy[173], 0xFF2F3336);
        p.fill(cx[219], cy[170], cx[220], cy[173], 0xFF2F3336);
        p.fill(cx[243], cy[170], cx[244], cy[173], 0xFF2F3336);

        p.fill(cx[351], cy[170], cx[352], cy[173], 0xFF61625E);
        p.fill(cx[4], cy[171], cx[5], cy[174], 0xFF61625E);
        p.fill(cx[348], cy[172], cx[349], cy[175], 0xFF61625E);

        p.fill(cx[340], cy[173], cx[341], cy[176], 0xFF3C3F3B);

        p.fill(cx[352], cy[173], cx[355], cy[174], 0xFF61625E);

        p.fill(cx[125], cy[174], cx[128], cy[175], 0xFF202423);
        p.fill(cx[137], cy[174], cx[140], cy[175], 0xFF202423);
        p.fill(cx[212], cy[174], cx[215], cy[175], 0xFF202423);

        p.fill(cx[318], cy[174], cx[319], cy[177], 0xFF2F3336);

        p.fill(cx[351], cy[174], cx[354], cy[175], 0xFF61625E);
        p.fill(cx[11], cy[175], cx[12], cy[178], 0xFF61625E);

        p.fill(cx[137], cy[175], cx[140], cy[176], 0xFF2F3336);
        p.fill(cx[212], cy[175], cx[215], cy[176], 0xFF2F3336);

        p.fill(cx[342], cy[175], cx[343], cy[178], 0xFF3C3F3B);

        p.fill(cx[0], cy[176], cx[3], cy[177], 0xFF6D6A64);

        p.fill(cx[57], cy[176], cx[58], cy[179], 0xFF202423);

        p.fill(cx[357], cy[176], cx[360], cy[177], 0xFF6D6A64);

        p.fill(cx[28], cy[177], cx[29], cy[180], 0xFF3C3F3B);

        p.fill(cx[123], cy[177], cx[124], cy[180], 0xFF42473F);

        p.fill(cx[138], cy[177], cx[141], cy[178], 0xFF51504E);

        p.fill(cx[168], cy[177], cx[171], cy[178], 0xFF61625E);

        p.fill(cx[171], cy[177], cx[174], cy[178], 0xFF565B54);

        p.fill(cx[182], cy[177], cx[185], cy[178], 0xFF2F3336);

        p.fill(cx[197], cy[177], cx[200], cy[178], 0xFF51504E);

        p.fill(cx[203], cy[177], cx[206], cy[178], 0xFF2F3336);
        p.fill(cx[232], cy[177], cx[235], cy[178], 0xFF2F3336);
        p.fill(cx[257], cy[177], cx[260], cy[178], 0xFF2F3336);

        p.fill(cx[355], cy[177], cx[358], cy[178], 0xFF6D6A64);

        p.fill(cx[5], cy[178], cx[8], cy[179], 0xFF8A8C83);

        p.fill(cx[43], cy[178], cx[46], cy[179], 0xFF3C3F3B);

        p.fill(cx[140], cy[178], cx[141], cy[181], 0xFF51504E);
        p.fill(cx[195], cy[178], cx[196], cy[181], 0xFF51504E);

        p.fill(cx[196], cy[178], cx[199], cy[179], 0xFF42473F);
        p.fill(cx[240], cy[178], cx[243], cy[179], 0xFF42473F);

        p.fill(cx[243], cy[178], cx[244], cy[181], 0xFF51504E);
        p.fill(cx[273], cy[178], cx[274], cy[181], 0xFF51504E);

        p.fill(cx[333], cy[178], cx[336], cy[179], 0xFF2F3336);

        p.fill(cx[352], cy[178], cx[355], cy[179], 0xFF8B9494);

        p.fill(cx[355], cy[178], cx[358], cy[179], 0xFF797D7C);

        p.fill(cx[57], cy[179], cx[58], cy[182], 0xFF2F3336);

        p.fill(cx[92], cy[179], cx[95], cy[180], 0xFF51504E);
        p.fill(cx[161], cy[179], cx[162], cy[182], 0xFF51504E);

        p.fill(cx[179], cy[179], cx[180], cy[182], 0xFF2F3336);

        p.fill(cx[201], cy[179], cx[202], cy[182], 0xFF202423);

        p.fill(cx[212], cy[179], cx[215], cy[180], 0xFF42473F);

        p.fill(cx[240], cy[179], cx[243], cy[180], 0xFF51504E);

        p.fill(cx[319], cy[179], cx[320], cy[182], 0xFF2F3336);
        p.fill(cx[323], cy[179], cx[326], cy[180], 0xFF2F3336);

        p.fill(cx[3], cy[180], cx[4], cy[183], 0xFFA5A49E);

        p.fill(cx[32], cy[180], cx[33], cy[183], 0xFF3C3F3B);

        p.fill(cx[89], cy[180], cx[92], cy[181], 0xFF42473F);

        p.fill(cx[255], cy[180], cx[256], cy[183], 0xFF2F3336);

        p.fill(cx[315], cy[180], cx[316], cy[183], 0xFF3C3F3B);
        p.fill(cx[317], cy[180], cx[318], cy[183], 0xFF3C3F3B);

        p.fill(cx[42], cy[181], cx[43], cy[184], 0xFF2F3336);

        p.fill(cx[63], cy[181], cx[64], cy[184], 0xFFFBFBFB);

        p.fill(cx[64], cy[181], cx[67], cy[182], 0xFF42473F);

        p.fill(cx[137], cy[181], cx[138], cy[184], 0xFF51504E);

        p.fill(cx[167], cy[181], cx[168], cy[184], 0xFFFBFBFB);
        p.fill(cx[192], cy[181], cx[193], cy[184], 0xFFFBFBFB);

        p.fill(cx[353], cy[181], cx[356], cy[182], 0xFFA5A49E);

        p.fill(cx[65], cy[182], cx[68], cy[183], 0xFF42473F);

        p.fill(cx[170], cy[182], cx[173], cy[183], 0xFF51504E);

        p.fill(cx[268], cy[182], cx[271], cy[183], 0xFFFBFBFB);

        p.fill(cx[273], cy[182], cx[274], cy[185], 0xFF51504E);

        p.fill(cx[316], cy[182], cx[317], cy[185], 0xFF2F3336);

        p.fill(cx[354], cy[182], cx[357], cy[183], 0xFFA5A49E);

        p.fill(cx[166], cy[183], cx[167], cy[186], 0xFF42473F);

        p.fill(cx[172], cy[183], cx[173], cy[186], 0xFF51504E);

        p.fill(cx[1], cy[184], cx[4], cy[185], 0xFF8A8C83);

        p.fill(cx[68], cy[184], cx[69], cy[187], 0xFF51504E);

        p.fill(cx[89], cy[184], cx[90], cy[187], 0xFF42473F);
        p.fill(cx[137], cy[184], cx[138], cy[187], 0xFF42473F);
        p.fill(cx[167], cy[184], cx[170], cy[185], 0xFF42473F);

        p.fill(cx[171], cy[184], cx[172], cy[187], 0xFF51504E);

        p.fill(cx[194], cy[184], cx[195], cy[187], 0xFF42473F);
        p.fill(cx[236], cy[184], cx[237], cy[187], 0xFF42473F);
        p.fill(cx[245], cy[184], cx[246], cy[187], 0xFF42473F);
        p.fill(cx[265], cy[184], cx[266], cy[187], 0xFF42473F);

        p.fill(cx[267], cy[184], cx[270], cy[185], 0xFF51504E);

        p.fill(cx[270], cy[184], cx[271], cy[187], 0xFF42473F);

        p.fill(cx[1], cy[185], cx[4], cy[186], 0xFF797D7C);

        p.fill(cx[7], cy[185], cx[8], cy[188], 0xFF8B9494);

        p.fill(cx[292], cy[185], cx[293], cy[188], 0xFFD43835);
        p.fill(cx[295], cy[185], cx[296], cy[188], 0xFFD43835);

        p.fill(cx[95], cy[186], cx[98], cy[187], 0xFF51504E);

        p.fill(cx[242], cy[186], cx[245], cy[187], 0xFF42473F);

        p.fill(cx[352], cy[186], cx[355], cy[187], 0xFF8B9494);

        p.fill(cx[0], cy[187], cx[3], cy[188], 0xFF6D6A64);

        p.fill(cx[120], cy[187], cx[123], cy[188], 0xFF565B54);

        p.fill(cx[144], cy[187], cx[147], cy[188], 0xFF42473F);

        p.fill(cx[161], cy[187], cx[164], cy[188], 0xFF51504E);
        p.fill(cx[217], cy[187], cx[220], cy[188], 0xFF51504E);

        p.fill(cx[220], cy[187], cx[223], cy[188], 0xFF565B54);

        p.fill(cx[351], cy[187], cx[354], cy[188], 0xFF8A8C83);

        p.fill(cx[95], cy[188], cx[98], cy[189], 0xFF202423);

        p.fill(cx[28], cy[189], cx[29], cy[192], 0xFF2F3336);

        p.fill(cx[30], cy[189], cx[31], cy[192], 0xFF3C3F3B);

        p.fill(cx[166], cy[189], cx[169], cy[190], 0xFF202423);
        p.fill(cx[218], cy[189], cx[221], cy[190], 0xFF202423);
        p.fill(cx[242], cy[189], cx[245], cy[190], 0xFF202423);

        p.fill(cx[323], cy[189], cx[326], cy[190], 0xFF2F3336);

        p.fill(cx[353], cy[189], cx[356], cy[190], 0xFF61625E);

        p.fill(cx[6], cy[190], cx[7], cy[193], 0xFF565B54);

        p.fill(cx[58], cy[190], cx[59], cy[193], 0xFF2F3336);
        p.fill(cx[73], cy[190], cx[74], cy[193], 0xFF2F3336);
        p.fill(cx[88], cy[190], cx[89], cy[193], 0xFF2F3336);
        p.fill(cx[95], cy[190], cx[96], cy[193], 0xFF2F3336);
        p.fill(cx[114], cy[190], cx[115], cy[193], 0xFF2F3336);
        p.fill(cx[140], cy[190], cx[141], cy[193], 0xFF2F3336);
        p.fill(cx[171], cy[190], cx[172], cy[193], 0xFF2F3336);
        p.fill(cx[214], cy[190], cx[215], cy[193], 0xFF2F3336);
        p.fill(cx[286], cy[190], cx[287], cy[193], 0xFF2F3336);
        p.fill(cx[289], cy[190], cx[290], cy[193], 0xFF2F3336);
        p.fill(cx[301], cy[190], cx[302], cy[193], 0xFF2F3336);

        p.fill(cx[5], cy[191], cx[6], cy[194], 0xFF565B54);
        p.fill(cx[353], cy[191], cx[356], cy[192], 0xFF565B54);

        p.fill(cx[102], cy[192], cx[105], cy[193], 0xFF2F3336);

        p.fill(cx[279], cy[192], cx[280], cy[195], 0xFF202423);

        p.fill(cx[28], cy[195], cx[31], cy[196], 0xFF3C3F3B);

        p.fill(cx[120], cy[195], cx[123], cy[196], 0xFF3D3E3D);

        p.fill(cx[148], cy[195], cx[151], cy[196], 0xFF3C3F3B);
        p.fill(cx[160], cy[195], cx[163], cy[196], 0xFF3C3F3B);
        p.fill(cx[238], cy[195], cx[241], cy[196], 0xFF3C3F3B);
        p.fill(cx[27], cy[196], cx[30], cy[197], 0xFF3C3F3B);
        p.fill(cx[115], cy[196], cx[118], cy[197], 0xFF3C3F3B);
        p.fill(cx[258], cy[196], cx[261], cy[197], 0xFF3C3F3B);
        p.fill(cx[282], cy[196], cx[285], cy[197], 0xFF3C3F3B);

        p.fill(cx[321], cy[196], cx[324], cy[197], 0xFF2F3336);

        p.fill(cx[77], cy[197], cx[80], cy[198], 0xFF3C3F3B);

        p.fill(cx[267], cy[197], cx[268], cy[200], 0xFF42473F);

        p.fill(cx[23], cy[198], cx[24], cy[201], 0xFF3C3F3B);
        p.fill(cx[36], cy[198], cx[39], cy[199], 0xFF3C3F3B);

        p.fill(cx[76], cy[198], cx[77], cy[201], 0xFF42473F);

        p.fill(cx[0], cy[199], cx[3], cy[200], 0xFF51504E);

        p.fill(cx[26], cy[199], cx[27], cy[202], 0xFF3C3F3B);
        p.fill(cx[78], cy[199], cx[79], cy[202], 0xFF3C3F3B);

        p.fill(cx[24], cy[200], cx[25], cy[203], 0xFF2F3336);

        p.fill(cx[282], cy[200], cx[283], cy[203], 0xFF42473F);
        p.fill(cx[288], cy[200], cx[289], cy[203], 0xFF42473F);

        p.fill(cx[295], cy[200], cx[296], cy[203], 0xFF3C3F3B);

        p.fill(cx[318], cy[200], cx[319], cy[203], 0xFF2F3336);

        p.fill(cx[348], cy[200], cx[349], cy[203], 0xFF3C3F3B);

        p.fill(cx[275], cy[201], cx[278], cy[202], 0xFF3D3E3D);
        p.fill(cx[289], cy[201], cx[292], cy[202], 0xFF3D3E3D);

        p.fill(cx[133], cy[202], cx[136], cy[203], 0xFF42473F);
        p.fill(cx[289], cy[202], cx[292], cy[203], 0xFF42473F);

        p.fill(cx[210], cy[1], cx[212], cy[2], 0xFF676763);

        p.fill(cx[344], cy[1], cx[345], cy[3], 0xFF61625E);

        p.fill(cx[44], cy[2], cx[45], cy[4], 0xFF51504E);
        p.fill(cx[77], cy[2], cx[78], cy[4], 0xFF51504E);

        p.fill(cx[86], cy[2], cx[88], cy[3], 0xFF565B54);

        p.fill(cx[235], cy[2], cx[236], cy[4], 0xFF676763);
        p.fill(cx[321], cy[2], cx[323], cy[3], 0xFF676763);

        p.fill(cx[21], cy[3], cx[22], cy[5], 0xFF565B54);
        p.fill(cx[117], cy[3], cx[118], cy[5], 0xFF565B54);

        p.fill(cx[183], cy[3], cx[184], cy[5], 0xFF61625E);
        p.fill(cx[210], cy[3], cx[211], cy[5], 0xFF61625E);
        p.fill(cx[217], cy[3], cx[218], cy[5], 0xFF61625E);

        p.fill(cx[35], cy[4], cx[37], cy[5], 0xFF51504E);

        p.fill(cx[54], cy[4], cx[56], cy[5], 0xFF61625E);

        p.fill(cx[118], cy[4], cx[120], cy[5], 0xFF565B54);
        p.fill(cx[125], cy[4], cx[127], cy[5], 0xFF565B54);
        p.fill(cx[278], cy[4], cx[280], cy[5], 0xFF565B54);

        p.fill(cx[299], cy[4], cx[301], cy[5], 0xFF6D6A64);

        p.fill(cx[345], cy[4], cx[346], cy[6], 0xFF565B54);

        p.fill(cx[20], cy[5], cx[22], cy[6], 0xFF42473F);

        p.fill(cx[34], cy[5], cx[36], cy[6], 0xFF51504E);

        p.fill(cx[36], cy[5], cx[37], cy[7], 0xFF42473F);
        p.fill(cx[38], cy[5], cx[39], cy[7], 0xFF42473F);

        p.fill(cx[49], cy[5], cx[51], cy[6], 0xFF565B54);

        p.fill(cx[52], cy[5], cx[54], cy[6], 0xFF2F3336);

        p.fill(cx[70], cy[5], cx[72], cy[6], 0xFF42473F);

        p.fill(cx[112], cy[5], cx[114], cy[6], 0xFF565B54);

        p.fill(cx[279], cy[5], cx[281], cy[6], 0xFF202423);

        p.fill(cx[308], cy[5], cx[310], cy[6], 0xFF676763);
        p.fill(cx[329], cy[5], cx[330], cy[7], 0xFF676763);

        p.fill(cx[346], cy[5], cx[347], cy[7], 0xFF565B54);

        p.fill(cx[13], cy[6], cx[15], cy[7], 0xFF676763);

        p.fill(cx[33], cy[6], cx[35], cy[7], 0xFF51504E);
        p.fill(cx[49], cy[6], cx[50], cy[8], 0xFF51504E);

        p.fill(cx[51], cy[6], cx[53], cy[7], 0xFF2F3336);

        p.fill(cx[286], cy[6], cx[288], cy[7], 0xFF51504E);

        p.fill(cx[299], cy[6], cx[301], cy[7], 0xFF565B54);
        p.fill(cx[302], cy[6], cx[303], cy[8], 0xFF565B54);

        p.fill(cx[303], cy[6], cx[304], cy[8], 0xFF51504E);

        p.fill(cx[308], cy[6], cx[310], cy[7], 0xFF202423);

        p.fill(cx[18], cy[7], cx[19], cy[9], 0xFF3D3E3D);

        p.fill(cx[34], cy[7], cx[36], cy[8], 0xFF61625E);

        p.fill(cx[41], cy[7], cx[43], cy[8], 0xFF51504E);
        p.fill(cx[44], cy[7], cx[46], cy[8], 0xFF51504E);

        p.fill(cx[51], cy[7], cx[52], cy[9], 0xFF202423);

        p.fill(cx[52], cy[7], cx[54], cy[8], 0xFF42473F);
        p.fill(cx[55], cy[7], cx[56], cy[9], 0xFF42473F);
        p.fill(cx[61], cy[7], cx[62], cy[9], 0xFF42473F);

        p.fill(cx[114], cy[7], cx[116], cy[8], 0xFF676763);

        p.fill(cx[128], cy[7], cx[130], cy[8], 0xFF61625E);

        p.fill(cx[142], cy[7], cx[144], cy[8], 0xFF565B54);

        p.fill(cx[148], cy[7], cx[150], cy[8], 0xFF61625E);

        p.fill(cx[165], cy[7], cx[167], cy[8], 0xFF51504E);

        p.fill(cx[178], cy[7], cx[180], cy[8], 0xFF565B54);

        p.fill(cx[180], cy[7], cx[182], cy[8], 0xFF51504E);
        p.fill(cx[186], cy[7], cx[188], cy[8], 0xFF51504E);

        p.fill(cx[188], cy[7], cx[190], cy[8], 0xFF42473F);

        p.fill(cx[223], cy[7], cx[225], cy[8], 0xFF565B54);

        p.fill(cx[227], cy[7], cx[229], cy[8], 0xFF51504E);
        p.fill(cx[240], cy[7], cx[242], cy[8], 0xFF51504E);

        p.fill(cx[242], cy[7], cx[244], cy[8], 0xFF42473F);

        p.fill(cx[254], cy[7], cx[256], cy[8], 0xFF565B54);

        p.fill(cx[261], cy[7], cx[263], cy[8], 0xFF42473F);
        p.fill(cx[284], cy[7], cx[286], cy[8], 0xFF42473F);

        p.fill(cx[304], cy[7], cx[306], cy[8], 0xFF51504E);

        p.fill(cx[333], cy[7], cx[335], cy[8], 0xFF3C3F3B);

        p.fill(cx[348], cy[7], cx[350], cy[8], 0xFF61625E);

        p.fill(cx[5], cy[8], cx[7], cy[9], 0xFF676763);

        p.fill(cx[10], cy[8], cx[11], cy[10], 0xFF61625E);

        p.fill(cx[52], cy[8], cx[54], cy[9], 0xFF3C3F3B);

        p.fill(cx[71], cy[8], cx[73], cy[9], 0xFF42473F);
        p.fill(cx[108], cy[8], cx[110], cy[9], 0xFF42473F);

        p.fill(cx[316], cy[8], cx[317], cy[10], 0xFF3D3E3D);

        p.fill(cx[352], cy[8], cx[353], cy[10], 0xFF6D6A64);

        p.fill(cx[14], cy[9], cx[15], cy[11], 0xFF42473F);
        p.fill(cx[18], cy[9], cx[19], cy[11], 0xFF42473F);

        p.fill(cx[109], cy[9], cx[110], cy[11], 0xFF3C3F3B);

        p.fill(cx[308], cy[9], cx[309], cy[11], 0xFF42473F);

        p.fill(cx[322], cy[9], cx[323], cy[11], 0xFF3C3F3B);

        p.fill(cx[342], cy[9], cx[343], cy[11], 0xFF3D3E3D);

        p.fill(cx[350], cy[9], cx[352], cy[10], 0xFF676763);

        p.fill(cx[353], cy[9], cx[354], cy[11], 0xFF797D7C);

        p.fill(cx[15], cy[10], cx[17], cy[11], 0xFF3D3E3D);

        p.fill(cx[53], cy[10], cx[55], cy[11], 0xFF42473F);

        p.fill(cx[62], cy[10], cx[64], cy[11], 0xFF2F3336);

        p.fill(cx[64], cy[10], cx[66], cy[11], 0xFF202423);

        p.fill(cx[134], cy[10], cx[136], cy[11], 0xFF42473F);

        p.fill(cx[301], cy[10], cx[303], cy[11], 0xFF202423);

        p.fill(cx[331], cy[10], cx[332], cy[12], 0xFF51504E);

        p.fill(cx[343], cy[10], cx[345], cy[11], 0xFF3D3E3D);

        p.fill(cx[53], cy[11], cx[54], cy[13], 0xFF42473F);

        p.fill(cx[54], cy[11], cx[56], cy[12], 0xFF3D3E3D);

        p.fill(cx[70], cy[11], cx[72], cy[12], 0xFF42473F);
        p.fill(cx[82], cy[11], cx[84], cy[12], 0xFF42473F);
        p.fill(cx[115], cy[11], cx[116], cy[13], 0xFF42473F);

        p.fill(cx[130], cy[11], cx[131], cy[13], 0xFF2F3336);

        p.fill(cx[147], cy[11], cx[149], cy[12], 0xFF565B54);
        p.fill(cx[170], cy[11], cx[172], cy[12], 0xFF565B54);

        p.fill(cx[172], cy[11], cx[174], cy[12], 0xFF51504E);
        p.fill(cx[187], cy[11], cx[189], cy[12], 0xFF51504E);
        p.fill(cx[190], cy[11], cx[192], cy[12], 0xFF51504E);

        p.fill(cx[201], cy[11], cx[203], cy[12], 0xFF3C3F3B);

        p.fill(cx[242], cy[11], cx[244], cy[12], 0xFF565B54);

        p.fill(cx[244], cy[11], cx[246], cy[12], 0xFF51504E);

        p.fill(cx[271], cy[11], cx[273], cy[12], 0xFF565B54);

        p.fill(cx[306], cy[11], cx[307], cy[13], 0xFF42473F);

        p.fill(cx[353], cy[11], cx[354], cy[13], 0xFFA5A49E);

        p.fill(cx[22], cy[12], cx[24], cy[13], 0xFF3C3F3B);

        p.fill(cx[61], cy[12], cx[62], cy[14], 0xFF51504E);
        p.fill(cx[124], cy[12], cx[125], cy[14], 0xFF51504E);

        p.fill(cx[134], cy[12], cx[136], cy[13], 0xFF202423);

        p.fill(cx[177], cy[12], cx[178], cy[14], 0xFF3C3F3B);
        p.fill(cx[179], cy[12], cx[181], cy[13], 0xFF3C3F3B);

        p.fill(cx[186], cy[12], cx[187], cy[14], 0xFF51504E);

        p.fill(cx[207], cy[12], cx[209], cy[13], 0xFF3C3F3B);

        p.fill(cx[214], cy[12], cx[215], cy[14], 0xFF51504E);

        p.fill(cx[322], cy[12], cx[323], cy[14], 0xFF42473F);

        p.fill(cx[327], cy[12], cx[328], cy[14], 0xFF3C3F3B);

        p.fill(cx[331], cy[12], cx[332], cy[14], 0xFF61625E);

        p.fill(cx[348], cy[12], cx[350], cy[13], 0xFF42473F);
        p.fill(cx[8], cy[13], cx[9], cy[15], 0xFF42473F);

        p.fill(cx[64], cy[13], cx[66], cy[14], 0xFF51504E);
        p.fill(cx[93], cy[13], cx[95], cy[14], 0xFF51504E);
        p.fill(cx[123], cy[13], cx[124], cy[15], 0xFF51504E);

        p.fill(cx[130], cy[13], cx[131], cy[15], 0xFF202423);

        p.fill(cx[298], cy[13], cx[300], cy[14], 0xFF42473F);

        p.fill(cx[306], cy[13], cx[308], cy[14], 0xFF3C3F3B);

        p.fill(cx[308], cy[13], cx[309], cy[15], 0xFF42473F);
        p.fill(cx[317], cy[13], cx[318], cy[15], 0xFF42473F);

        p.fill(cx[328], cy[13], cx[330], cy[14], 0xFF3C3F3B);

        p.fill(cx[111], cy[14], cx[113], cy[15], 0xFF42473F);
        p.fill(cx[117], cy[14], cx[119], cy[15], 0xFF42473F);

        p.fill(cx[144], cy[14], cx[145], cy[16], 0xFF51504E);

        p.fill(cx[155], cy[14], cx[156], cy[16], 0xFF42473F);

        p.fill(cx[168], cy[14], cx[169], cy[16], 0xFF51504E);

        p.fill(cx[182], cy[14], cx[184], cy[15], 0xFF3C3F3B);

        p.fill(cx[192], cy[14], cx[194], cy[15], 0xFF51504E);

        p.fill(cx[226], cy[14], cx[228], cy[15], 0xFF2F3336);

        p.fill(cx[267], cy[14], cx[268], cy[16], 0xFF51504E);

        p.fill(cx[318], cy[14], cx[320], cy[15], 0xFF42473F);

        p.fill(cx[1], cy[15], cx[3], cy[16], 0xFF8A8C83);

        p.fill(cx[3], cy[15], cx[4], cy[17], 0xFFA5A49E);
        p.fill(cx[5], cy[15], cx[7], cy[16], 0xFFA5A49E);

        p.fill(cx[7], cy[15], cx[8], cy[17], 0xFF676763);

        p.fill(cx[51], cy[15], cx[52], cy[17], 0xFF3C3F3B);

        p.fill(cx[65], cy[15], cx[67], cy[16], 0xFFFBFBFB);

        p.fill(cx[69], cy[15], cx[70], cy[17], 0xFF51504E);
        p.fill(cx[71], cy[15], cx[72], cy[17], 0xFF51504E);

        p.fill(cx[122], cy[15], cx[124], cy[16], 0xFF42473F);

        p.fill(cx[130], cy[15], cx[131], cy[17], 0xFF2F3336);

        p.fill(cx[136], cy[15], cx[138], cy[16], 0xFF42473F);

        p.fill(cx[145], cy[15], cx[146], cy[17], 0xFF51504E);
        p.fill(cx[164], cy[15], cx[166], cy[16], 0xFF51504E);

        p.fill(cx[183], cy[15], cx[184], cy[17], 0xFF3C3F3B);

        p.fill(cx[193], cy[15], cx[195], cy[16], 0xFF42473F);
        p.fill(cx[220], cy[15], cx[222], cy[16], 0xFF42473F);
        p.fill(cx[243], cy[15], cx[245], cy[16], 0xFF42473F);
        p.fill(cx[264], cy[15], cx[265], cy[17], 0xFF42473F);

        p.fill(cx[316], cy[15], cx[317], cy[17], 0xFF3C3F3B);

        p.fill(cx[317], cy[15], cx[319], cy[16], 0xFF3D3E3D);

        p.fill(cx[319], cy[15], cx[320], cy[17], 0xFF42473F);

        p.fill(cx[320], cy[15], cx[321], cy[17], 0xFF3D3E3D);
        p.fill(cx[322], cy[15], cx[323], cy[17], 0xFF3D3E3D);
        p.fill(cx[325], cy[15], cx[327], cy[16], 0xFF3D3E3D);

        p.fill(cx[329], cy[15], cx[330], cy[17], 0xFF42473F);

        p.fill(cx[56], cy[16], cx[57], cy[18], 0xFF3C3F3B);

        p.fill(cx[62], cy[16], cx[64], cy[17], 0xFF51504E);

        p.fill(cx[65], cy[16], cx[67], cy[17], 0xFF565B54);

        p.fill(cx[88], cy[16], cx[90], cy[17], 0xFF42473F);

        p.fill(cx[91], cy[16], cx[93], cy[17], 0xFF51504E);

        p.fill(cx[93], cy[16], cx[94], cy[18], 0xFFFBFBFB);

        p.fill(cx[113], cy[16], cx[114], cy[18], 0xFF51504E);

        p.fill(cx[116], cy[16], cx[117], cy[18], 0xFFD1D2C3);

        p.fill(cx[119], cy[16], cx[120], cy[18], 0xFFFBFBFB);
        p.fill(cx[141], cy[16], cx[142], cy[18], 0xFFFBFBFB);

        p.fill(cx[142], cy[16], cx[143], cy[18], 0xFFECEEEF);

        p.fill(cx[164], cy[16], cx[165], cy[18], 0xFF42473F);

        p.fill(cx[179], cy[16], cx[180], cy[18], 0xFF2F3336);

        p.fill(cx[190], cy[16], cx[192], cy[17], 0xFF3C3F3B);

        p.fill(cx[214], cy[16], cx[215], cy[18], 0xFF51504E);

        p.fill(cx[216], cy[16], cx[218], cy[17], 0xFF42473F);

        p.fill(cx[218], cy[16], cx[219], cy[18], 0xFF2F3336);

        p.fill(cx[220], cy[16], cx[221], cy[18], 0xFFFBFBFB);

        p.fill(cx[221], cy[16], cx[222], cy[18], 0xFF51504E);

        p.fill(cx[240], cy[16], cx[241], cy[18], 0xFF3C3F3B);

        p.fill(cx[245], cy[16], cx[246], cy[18], 0xFFFBFBFB);

        p.fill(cx[270], cy[16], cx[272], cy[17], 0xFF42473F);

        p.fill(cx[295], cy[16], cx[296], cy[18], 0xFFFBFBFB);

        p.fill(cx[297], cy[16], cx[298], cy[18], 0xFF42473F);

        p.fill(cx[324], cy[16], cx[326], cy[17], 0xFF3D3E3D);

        p.fill(cx[16], cy[17], cx[17], cy[19], 0xFF3C3F3B);

        p.fill(cx[65], cy[17], cx[67], cy[18], 0xFFFBFBFB);
        p.fill(cx[91], cy[17], cx[93], cy[18], 0xFFFBFBFB);

        p.fill(cx[146], cy[17], cx[148], cy[18], 0xFF51504E);

        p.fill(cx[152], cy[17], cx[153], cy[19], 0xFF2F3336);

        p.fill(cx[190], cy[17], cx[192], cy[18], 0xFFFBFBFB);
        p.fill(cx[242], cy[17], cx[244], cy[18], 0xFFFBFBFB);

        p.fill(cx[291], cy[17], cx[293], cy[18], 0xFF42473F);
        p.fill(cx[296], cy[17], cx[297], cy[19], 0xFF42473F);

        p.fill(cx[331], cy[17], cx[333], cy[18], 0xFF202423);

        p.fill(cx[352], cy[17], cx[354], cy[18], 0xFF797D7C);
        p.fill(cx[3], cy[18], cx[4], cy[20], 0xFF797D7C);

        p.fill(cx[7], cy[18], cx[8], cy[20], 0xFF61625E);

        p.fill(cx[70], cy[18], cx[72], cy[19], 0xFF42473F);

        p.fill(cx[87], cy[18], cx[89], cy[19], 0xFF4A4B47);

        p.fill(cx[90], cy[18], cx[91], cy[20], 0xFF51504E);
        p.fill(cx[120], cy[18], cx[122], cy[19], 0xFF51504E);

        p.fill(cx[130], cy[18], cx[131], cy[20], 0xFF2F3336);

        p.fill(cx[141], cy[18], cx[143], cy[19], 0xFF51504E);

        p.fill(cx[205], cy[18], cx[206], cy[20], 0xFF3C3F3B);

        p.fill(cx[243], cy[18], cx[245], cy[19], 0xFF51504E);

        p.fill(cx[315], cy[18], cx[317], cy[19], 0xFF3D3E3D);

        p.fill(cx[332], cy[18], cx[334], cy[19], 0xFF202423);

        p.fill(cx[341], cy[18], cx[343], cy[19], 0xFF3C3F3B);

        p.fill(cx[358], cy[18], cx[360], cy[19], 0xFF8A8C83);

        p.fill(cx[4], cy[19], cx[5], cy[21], 0xFF797D7C);

        p.fill(cx[24], cy[19], cx[26], cy[20], 0xFF676763);

        p.fill(cx[54], cy[19], cx[55], cy[21], 0xFF42473F);

        p.fill(cx[64], cy[19], cx[66], cy[20], 0xFF4A4B47);

        p.fill(cx[66], cy[19], cx[67], cy[21], 0xFF42473F);

        p.fill(cx[67], cy[19], cx[68], cy[21], 0xFF4A4B47);

        p.fill(cx[87], cy[19], cx[89], cy[20], 0xFF42473F);
        p.fill(cx[121], cy[19], cx[122], cy[21], 0xFF42473F);

        p.fill(cx[134], cy[19], cx[135], cy[21], 0xFF202423);

        p.fill(cx[141], cy[19], cx[143], cy[20], 0xFF42473F);
        p.fill(cx[155], cy[19], cx[156], cy[21], 0xFF42473F);

        p.fill(cx[165], cy[19], cx[167], cy[20], 0xFF4A4B47);

        p.fill(cx[190], cy[19], cx[191], cy[21], 0xFF42473F);
        p.fill(cx[220], cy[19], cx[221], cy[21], 0xFF42473F);

        p.fill(cx[224], cy[19], cx[225], cy[21], 0xFF51504E);
        p.fill(cx[236], cy[19], cx[237], cy[21], 0xFF51504E);

        p.fill(cx[255], cy[19], cx[256], cy[21], 0xFF202423);

        p.fill(cx[265], cy[19], cx[266], cy[21], 0xFF42473F);

        p.fill(cx[272], cy[19], cx[273], cy[21], 0xFF4A4B47);

        p.fill(cx[294], cy[19], cx[296], cy[20], 0xFF42473F);

        p.fill(cx[296], cy[19], cx[297], cy[21], 0xFF51504E);

        p.fill(cx[323], cy[19], cx[325], cy[20], 0xFF3C3F3B);

        p.fill(cx[345], cy[19], cx[347], cy[20], 0xFF61625E);

        p.fill(cx[347], cy[19], cx[349], cy[20], 0xFF2F3336);

        p.fill(cx[20], cy[20], cx[21], cy[22], 0xFF42473F);

        p.fill(cx[58], cy[20], cx[59], cy[22], 0xFF2F3336);

        p.fill(cx[64], cy[20], cx[65], cy[22], 0xFF51504E);
        p.fill(cx[88], cy[20], cx[89], cy[22], 0xFF51504E);

        p.fill(cx[130], cy[20], cx[131], cy[22], 0xFF202423);

        p.fill(cx[142], cy[20], cx[143], cy[22], 0xFF51504E);
        p.fill(cx[148], cy[20], cx[149], cy[22], 0xFF51504E);
        p.fill(cx[161], cy[20], cx[162], cy[22], 0xFF51504E);
        p.fill(cx[165], cy[20], cx[166], cy[22], 0xFF51504E);

        p.fill(cx[180], cy[20], cx[181], cy[22], 0xFF42473F);
        p.fill(cx[249], cy[20], cx[250], cy[22], 0xFF42473F);

        p.fill(cx[261], cy[20], cx[262], cy[22], 0xFF51504E);

        p.fill(cx[262], cy[20], cx[264], cy[21], 0xFF42473F);

        p.fill(cx[267], cy[20], cx[268], cy[22], 0xFF51504E);
        p.fill(cx[273], cy[20], cx[274], cy[22], 0xFF51504E);
        p.fill(cx[288], cy[20], cx[289], cy[22], 0xFF51504E);
        p.fill(cx[292], cy[20], cx[293], cy[22], 0xFF51504E);

        p.fill(cx[298], cy[20], cx[300], cy[21], 0xFF42473F);

        p.fill(cx[326], cy[20], cx[327], cy[22], 0xFF3D3E3D);

        p.fill(cx[347], cy[20], cx[349], cy[21], 0xFF797D7C);
        p.fill(cx[358], cy[20], cx[360], cy[21], 0xFF797D7C);

        p.fill(cx[8], cy[21], cx[10], cy[22], 0xFF676763);

        p.fill(cx[12], cy[21], cx[14], cy[22], 0xFF42473F);

        p.fill(cx[60], cy[21], cx[62], cy[22], 0xFF51504E);

        p.fill(cx[65], cy[21], cx[66], cy[23], 0xFF2F3336);

        p.fill(cx[66], cy[21], cx[68], cy[22], 0xFF51504E);

        p.fill(cx[85], cy[21], cx[86], cy[23], 0xFF202423);

        p.fill(cx[86], cy[21], cx[88], cy[22], 0xFF51504E);
        p.fill(cx[90], cy[21], cx[92], cy[22], 0xFF51504E);

        p.fill(cx[92], cy[21], cx[94], cy[22], 0xFF565B54);

        p.fill(cx[98], cy[21], cx[99], cy[23], 0xFF202423);

        p.fill(cx[146], cy[21], cx[148], cy[22], 0xFF51504E);

        p.fill(cx[199], cy[21], cx[200], cy[23], 0xFF202423);
        p.fill(cx[224], cy[21], cx[225], cy[23], 0xFF202423);
        p.fill(cx[236], cy[21], cx[237], cy[23], 0xFF202423);

        p.fill(cx[262], cy[21], cx[264], cy[22], 0xFF51504E);
        p.fill(cx[265], cy[21], cx[267], cy[22], 0xFF51504E);

        p.fill(cx[287], cy[21], cx[288], cy[23], 0xFF202423);

        p.fill(cx[296], cy[21], cx[298], cy[22], 0xFF2F3336);
        p.fill(cx[301], cy[21], cx[302], cy[23], 0xFF2F3336);

        p.fill(cx[327], cy[21], cx[329], cy[22], 0xFF3D3E3D);

        p.fill(cx[333], cy[21], cx[335], cy[22], 0xFF42473F);

        p.fill(cx[347], cy[21], cx[348], cy[23], 0xFF676763);

        p.fill(cx[0], cy[22], cx[2], cy[23], 0xFF6D6A64);

        p.fill(cx[14], cy[22], cx[16], cy[23], 0xFF3D3E3D);

        p.fill(cx[66], cy[22], cx[67], cy[24], 0xFF202423);

        p.fill(cx[67], cy[22], cx[69], cy[23], 0xFF2F3336);

        p.fill(cx[100], cy[22], cx[101], cy[24], 0xFF3C3F3B);

        p.fill(cx[109], cy[22], cx[111], cy[23], 0xFF2F3336);

        p.fill(cx[127], cy[22], cx[128], cy[24], 0xFF3C3F3B);

        p.fill(cx[130], cy[22], cx[131], cy[24], 0xFF2F3336);
        p.fill(cx[135], cy[22], cx[136], cy[24], 0xFF2F3336);

        p.fill(cx[153], cy[22], cx[155], cy[23], 0xFF3C3F3B);

        p.fill(cx[155], cy[22], cx[156], cy[24], 0xFF2F3336);

        p.fill(cx[156], cy[22], cx[158], cy[23], 0xFF3C3F3B);
    }

    private static void part6(Paint p, int[] cx, int[] cy) {

        p.fill(cx[158], cy[22], cx[159], cy[24], 0xFF2F3336);
        p.fill(cx[160], cy[22], cx[161], cy[24], 0xFF2F3336);

        p.fill(cx[178], cy[22], cx[179], cy[24], 0xFF3C3F3B);

        p.fill(cx[179], cy[22], cx[180], cy[24], 0xFF2F3336);
        p.fill(cx[185], cy[22], cx[186], cy[24], 0xFF2F3336);

        p.fill(cx[203], cy[22], cx[204], cy[24], 0xFF3C3F3B);

        p.fill(cx[204], cy[22], cx[205], cy[24], 0xFF2F3336);
        p.fill(cx[225], cy[22], cx[226], cy[24], 0xFF2F3336);

        p.fill(cx[251], cy[22], cx[252], cy[24], 0xFF3C3F3B);

        p.fill(cx[260], cy[22], cx[261], cy[24], 0xFF2F3336);
        p.fill(cx[275], cy[22], cx[276], cy[24], 0xFF2F3336);
        p.fill(cx[278], cy[22], cx[279], cy[24], 0xFF2F3336);

        p.fill(cx[326], cy[22], cx[328], cy[23], 0xFF42473F);

        p.fill(cx[332], cy[22], cx[333], cy[24], 0xFF3D3E3D);

        p.fill(cx[67], cy[23], cx[69], cy[24], 0xFF202423);

        p.fill(cx[79], cy[23], cx[80], cy[25], 0xFF3C3F3B);

        p.fill(cx[81], cy[23], cx[83], cy[24], 0xFF2F3336);

        p.fill(cx[98], cy[23], cx[100], cy[24], 0xFF3C3F3B);
        p.fill(cx[128], cy[23], cx[130], cy[24], 0xFF3C3F3B);

        p.fill(cx[156], cy[23], cx[158], cy[24], 0xFF2F3336);
        p.fill(cx[207], cy[23], cx[209], cy[24], 0xFF2F3336);

        p.fill(cx[234], cy[23], cx[236], cy[24], 0xFF3C3F3B);
        p.fill(cx[255], cy[23], cx[257], cy[24], 0xFF3C3F3B);
        p.fill(cx[280], cy[23], cx[281], cy[25], 0xFF3C3F3B);

        p.fill(cx[297], cy[23], cx[299], cy[24], 0xFF51504E);

        p.fill(cx[337], cy[23], cx[339], cy[24], 0xFF3C3F3B);

        p.fill(cx[349], cy[23], cx[350], cy[25], 0xFF6D6A64);

        p.fill(cx[59], cy[25], cx[61], cy[26], 0xFF3C3F3B);
        p.fill(cx[120], cy[25], cx[122], cy[26], 0xFF3C3F3B);
        p.fill(cx[209], cy[25], cx[211], cy[26], 0xFF3C3F3B);
        p.fill(cx[212], cy[25], cx[214], cy[26], 0xFF3C3F3B);

        p.fill(cx[7], cy[26], cx[8], cy[28], 0xFF676763);

        p.fill(cx[81], cy[26], cx[82], cy[28], 0xFF2F3336);
        p.fill(cx[100], cy[26], cx[101], cy[28], 0xFF2F3336);
        p.fill(cx[145], cy[26], cx[146], cy[28], 0xFF2F3336);
        p.fill(cx[185], cy[26], cx[186], cy[28], 0xFF2F3336);
        p.fill(cx[282], cy[26], cx[283], cy[28], 0xFF2F3336);

        p.fill(cx[321], cy[26], cx[323], cy[27], 0xFF3C3F3B);
        p.fill(cx[338], cy[26], cx[340], cy[27], 0xFF3C3F3B);

        p.fill(cx[350], cy[26], cx[352], cy[27], 0xFF797D7C);

        p.fill(cx[9], cy[27], cx[11], cy[28], 0xFF6D6A64);

        p.fill(cx[143], cy[27], cx[145], cy[28], 0xFF2F3336);

        p.fill(cx[336], cy[27], cx[337], cy[29], 0xFF3D3E3D);

        p.fill(cx[5], cy[28], cx[6], cy[30], 0xFF61625E);

        p.fill(cx[6], cy[28], cx[8], cy[29], 0xFF2F3336);

        p.fill(cx[217], cy[28], cx[219], cy[29], 0xFFA5A49E);

        p.fill(cx[351], cy[28], cx[352], cy[30], 0xFF797D7C);

        p.fill(cx[352], cy[28], cx[354], cy[29], 0xFF2F3336);

        p.fill(cx[316], cy[29], cx[318], cy[30], 0xFF202423);

        p.fill(cx[347], cy[29], cx[348], cy[31], 0xFF3C3F3B);

        p.fill(cx[348], cy[29], cx[349], cy[31], 0xFF6D6A64);

        p.fill(cx[352], cy[29], cx[354], cy[30], 0xFF676763);

        p.fill(cx[6], cy[30], cx[8], cy[31], 0xFF51504E);

        p.fill(cx[2], cy[31], cx[4], cy[32], 0xFF61625E);

        p.fill(cx[4], cy[31], cx[5], cy[33], 0xFF565B54);

        p.fill(cx[39], cy[31], cx[40], cy[33], 0xFF2F3336);

        p.fill(cx[5], cy[33], cx[6], cy[35], 0xFF51504E);

        p.fill(cx[11], cy[33], cx[12], cy[35], 0xFF676763);

        p.fill(cx[318], cy[33], cx[320], cy[34], 0xFF3C3F3B);

        p.fill(cx[347], cy[33], cx[348], cy[35], 0xFF61625E);

        p.fill(cx[10], cy[35], cx[12], cy[36], 0xFF6D6A64);

        p.fill(cx[30], cy[35], cx[31], cy[37], 0xFF42473F);

        p.fill(cx[42], cy[35], cx[43], cy[37], 0xFF202423);
        p.fill(cx[10], cy[36], cx[12], cy[37], 0xFF202423);

        p.fill(cx[17], cy[36], cx[19], cy[37], 0xFF42473F);

        p.fill(cx[40], cy[36], cx[42], cy[37], 0xFF202423);

        p.fill(cx[324], cy[37], cx[326], cy[38], 0xFF2F3336);

        p.fill(cx[332], cy[37], cx[333], cy[39], 0xFF3C3F3B);

        p.fill(cx[351], cy[37], cx[352], cy[39], 0xFF565B54);

        p.fill(cx[42], cy[38], cx[43], cy[40], 0xFF2F3336);

        p.fill(cx[319], cy[38], cx[320], cy[40], 0xFF3C3F3B);
        p.fill(cx[325], cy[38], cx[326], cy[40], 0xFF3C3F3B);

        p.fill(cx[346], cy[38], cx[347], cy[40], 0xFF2F3336);

        p.fill(cx[24], cy[39], cx[25], cy[41], 0xFF3C3F3B);
        p.fill(cx[31], cy[39], cx[32], cy[41], 0xFF3C3F3B);

        p.fill(cx[329], cy[39], cx[330], cy[41], 0xFF2F3336);
        p.fill(cx[4], cy[40], cx[5], cy[42], 0xFF2F3336);

        p.fill(cx[317], cy[40], cx[318], cy[42], 0xFF3C3F3B);
        p.fill(cx[323], cy[40], cx[324], cy[42], 0xFF3C3F3B);

        p.fill(cx[353], cy[40], cx[354], cy[42], 0xFF42473F);

        p.fill(cx[12], cy[41], cx[13], cy[43], 0xFF2F3336);
        p.fill(cx[32], cy[41], cx[34], cy[42], 0xFF2F3336);
        p.fill(cx[347], cy[41], cx[348], cy[43], 0xFF2F3336);

        p.fill(cx[9], cy[42], cx[11], cy[43], 0xFF676763);

        p.fill(cx[11], cy[42], cx[12], cy[44], 0xFF2F3336);
        p.fill(cx[39], cy[42], cx[40], cy[44], 0xFF2F3336);
        p.fill(cx[348], cy[42], cx[349], cy[44], 0xFF2F3336);

        p.fill(cx[349], cy[42], cx[351], cy[43], 0xFF797D7C);

        p.fill(cx[352], cy[42], cx[354], cy[43], 0xFF3C3F3B);

        p.fill(cx[6], cy[43], cx[8], cy[44], 0xFF2F3336);
        p.fill(cx[10], cy[43], cx[11], cy[45], 0xFF2F3336);

        p.fill(cx[325], cy[43], cx[327], cy[44], 0xFF202423);

        p.fill(cx[346], cy[43], cx[347], cy[45], 0xFF565B54);

        p.fill(cx[349], cy[43], cx[350], cy[45], 0xFF2F3336);
        p.fill(cx[352], cy[43], cx[354], cy[44], 0xFF2F3336);

        p.fill(cx[13], cy[44], cx[14], cy[46], 0xFF3C3F3B);

        p.fill(cx[33], cy[45], cx[34], cy[47], 0xFF2F3336);

        p.fill(cx[352], cy[45], cx[354], cy[46], 0xFF565B54);
        p.fill(cx[12], cy[46], cx[13], cy[48], 0xFF565B54);
        p.fill(cx[335], cy[46], cx[337], cy[47], 0xFF565B54);

        p.fill(cx[340], cy[47], cx[341], cy[49], 0xFF42473F);

        p.fill(cx[341], cy[47], cx[342], cy[49], 0xFF51504E);

        p.fill(cx[346], cy[47], cx[347], cy[49], 0xFF42473F);

        p.fill(cx[319], cy[48], cx[320], cy[50], 0xFF3C3F3B);

        p.fill(cx[330], cy[48], cx[331], cy[50], 0xFF42473F);

        p.fill(cx[331], cy[48], cx[333], cy[49], 0xFF4A4B47);

        p.fill(cx[12], cy[49], cx[13], cy[51], 0xFF565B54);

        p.fill(cx[341], cy[49], cx[342], cy[51], 0xFF676763);

        p.fill(cx[319], cy[50], cx[320], cy[52], 0xFF2F3336);

        p.fill(cx[330], cy[50], cx[331], cy[52], 0xFF51504E);

        p.fill(cx[19], cy[51], cx[20], cy[53], 0xFF42473F);

        p.fill(cx[25], cy[51], cx[26], cy[53], 0xFFFBFBFB);

        p.fill(cx[320], cy[51], cx[322], cy[52], 0xFF2F3336);

        p.fill(cx[322], cy[51], cx[324], cy[52], 0xFF3C3F3B);

        p.fill(cx[12], cy[52], cx[13], cy[54], 0xFF565B54);

        p.fill(cx[320], cy[52], cx[322], cy[53], 0xFFFBFBFB);

        p.fill(cx[29], cy[53], cx[30], cy[55], 0xFF51504E);

        p.fill(cx[38], cy[53], cx[40], cy[54], 0xFF2F3336);

        p.fill(cx[12], cy[54], cx[13], cy[56], 0xFF61625E);

        p.fill(cx[19], cy[54], cx[20], cy[56], 0xFF42473F);

        p.fill(cx[39], cy[54], cx[40], cy[56], 0xFF2F3336);

        p.fill(cx[331], cy[54], cx[332], cy[56], 0xFF42473F);
        p.fill(cx[340], cy[54], cx[341], cy[56], 0xFF42473F);

        p.fill(cx[351], cy[54], cx[352], cy[56], 0xFF61625E);

        p.fill(cx[29], cy[55], cx[30], cy[57], 0xFF565B54);

        p.fill(cx[30], cy[55], cx[31], cy[57], 0xFF51504E);
        p.fill(cx[332], cy[55], cx[333], cy[57], 0xFF51504E);

        p.fill(cx[8], cy[56], cx[9], cy[58], 0xFF2F3336);

        p.fill(cx[340], cy[56], cx[341], cy[58], 0xFF51504E);

        p.fill(cx[13], cy[57], cx[14], cy[59], 0xFF3C3F3B);

        p.fill(cx[30], cy[57], cx[31], cy[59], 0xFF202423);

        p.fill(cx[331], cy[57], cx[333], cy[58], 0xFF2F3336);

        p.fill(cx[8], cy[58], cx[9], cy[60], 0xFF3C3F3B);
        p.fill(cx[39], cy[58], cx[40], cy[60], 0xFF3C3F3B);

        p.fill(cx[351], cy[58], cx[352], cy[60], 0xFF61625E);

        p.fill(cx[351], cy[60], cx[352], cy[62], 0xFF565B54);

        p.fill(cx[7], cy[61], cx[8], cy[63], 0xFF61625E);

        p.fill(cx[8], cy[61], cx[9], cy[63], 0xFF3C3F3B);
        p.fill(cx[42], cy[61], cx[43], cy[63], 0xFF3C3F3B);

        p.fill(cx[327], cy[61], cx[329], cy[62], 0xFF202423);

        p.fill(cx[346], cy[61], cx[347], cy[63], 0xFF61625E);

        p.fill(cx[34], cy[62], cx[35], cy[64], 0xFF42473F);

        p.fill(cx[18], cy[64], cx[19], cy[66], 0xFF565B54);

        p.fill(cx[28], cy[64], cx[30], cy[65], 0xFF676763);

        p.fill(cx[30], cy[64], cx[32], cy[65], 0xFF202423);

        p.fill(cx[329], cy[64], cx[331], cy[65], 0xFF565B54);

        p.fill(cx[332], cy[64], cx[334], cy[65], 0xFF51504E);

        p.fill(cx[341], cy[64], cx[343], cy[65], 0xFF202423);

        p.fill(cx[34], cy[65], cx[35], cy[67], 0xFF565B54);

        p.fill(cx[339], cy[66], cx[341], cy[67], 0xFF42473F);
        p.fill(cx[42], cy[67], cx[43], cy[69], 0xFF42473F);

        p.fill(cx[8], cy[68], cx[9], cy[70], 0xFF2F3336);

        p.fill(cx[318], cy[68], cx[319], cy[70], 0xFF3C3F3B);

        p.fill(cx[24], cy[69], cx[26], cy[70], 0xFF51504E);

        p.fill(cx[319], cy[69], cx[320], cy[71], 0xFF3C3F3B);

        p.fill(cx[335], cy[69], cx[336], cy[71], 0xFFFBFBFB);

        p.fill(cx[8], cy[70], cx[9], cy[72], 0xFF3C3F3B);

        p.fill(cx[334], cy[70], cx[335], cy[72], 0xFF42473F);

        p.fill(cx[38], cy[71], cx[40], cy[72], 0xFF3C3F3B);

        p.fill(cx[335], cy[72], cx[337], cy[73], 0xFF4A4B47);

        p.fill(cx[331], cy[73], cx[332], cy[75], 0xFF42473F);

        p.fill(cx[332], cy[73], cx[333], cy[75], 0xFF51504E);
        p.fill(cx[19], cy[74], cx[20], cy[76], 0xFF51504E);
        p.fill(cx[29], cy[74], cx[30], cy[76], 0xFF51504E);
        p.fill(cx[340], cy[74], cx[341], cy[76], 0xFF51504E);

        p.fill(cx[352], cy[74], cx[353], cy[76], 0xFF565B54);

        p.fill(cx[8], cy[75], cx[9], cy[77], 0xFF3C3F3B);

        p.fill(cx[30], cy[75], cx[32], cy[76], 0xFF202423);

        p.fill(cx[336], cy[75], cx[338], cy[76], 0xFF61625E);
        p.fill(cx[350], cy[75], cx[351], cy[77], 0xFF61625E);

        p.fill(cx[326], cy[78], cx[328], cy[79], 0xFF2F3336);

        p.fill(cx[328], cy[78], cx[329], cy[80], 0xFF202423);

        p.fill(cx[329], cy[78], cx[331], cy[79], 0xFF2F3336);

        p.fill(cx[329], cy[79], cx[331], cy[80], 0xFF202423);
        p.fill(cx[341], cy[79], cx[343], cy[80], 0xFF202423);

        p.fill(cx[359], cy[79], cx[360], cy[81], 0xFF565B54);

        p.fill(cx[7], cy[80], cx[9], cy[81], 0xFF3C3F3B);

        p.fill(cx[30], cy[81], cx[32], cy[82], 0xFF2F3336);

        p.fill(cx[37], cy[81], cx[38], cy[83], 0xFF3C3F3B);

        p.fill(cx[327], cy[81], cx[329], cy[82], 0xFF2F3336);

        p.fill(cx[22], cy[82], cx[24], cy[83], 0xFF565B54);

        p.fill(cx[30], cy[82], cx[32], cy[83], 0xFF202423);

        p.fill(cx[329], cy[82], cx[330], cy[84], 0xFF51504E);

        p.fill(cx[331], cy[82], cx[333], cy[83], 0xFF42473F);

        p.fill(cx[333], cy[82], cx[335], cy[83], 0xFF51504E);
        p.fill(cx[336], cy[82], cx[338], cy[83], 0xFF51504E);

        p.fill(cx[338], cy[82], cx[340], cy[83], 0xFF61625E);

        p.fill(cx[8], cy[83], cx[9], cy[85], 0xFF3C3F3B);

        p.fill(cx[338], cy[83], cx[340], cy[84], 0xFF2F3336);

        p.fill(cx[346], cy[84], cx[347], cy[86], 0xFF51504E);

        p.fill(cx[29], cy[86], cx[30], cy[88], 0xFF565B54);

        p.fill(cx[38], cy[86], cx[39], cy[88], 0xFF3C3F3B);

        p.fill(cx[23], cy[87], cx[24], cy[89], 0xFF42473F);

        p.fill(cx[333], cy[87], cx[334], cy[89], 0xFFD1D2C3);

        p.fill(cx[340], cy[87], cx[341], cy[89], 0xFF51504E);
        p.fill(cx[346], cy[87], cx[347], cy[89], 0xFF51504E);

        p.fill(cx[7], cy[88], cx[8], cy[90], 0xFF676763);

        p.fill(cx[37], cy[88], cx[39], cy[89], 0xFFECEEEF);

        p.fill(cx[351], cy[88], cx[353], cy[89], 0xFF565B54);

        p.fill(cx[336], cy[89], cx[337], cy[91], 0xFF42473F);
        p.fill(cx[346], cy[89], cx[347], cy[91], 0xFF42473F);

        p.fill(cx[351], cy[89], cx[352], cy[91], 0xFF565B54);

        p.fill(cx[340], cy[90], cx[342], cy[91], 0xFF51504E);
        p.fill(cx[341], cy[91], cx[342], cy[93], 0xFF51504E);

        p.fill(cx[346], cy[91], cx[347], cy[93], 0xFF565B54);

        p.fill(cx[351], cy[91], cx[352], cy[93], 0xFF51504E);

        p.fill(cx[34], cy[92], cx[35], cy[94], 0xFF202423);

        p.fill(cx[340], cy[92], cx[341], cy[94], 0xFF51504E);

        p.fill(cx[19], cy[93], cx[21], cy[94], 0xFF2F3336);

        p.fill(cx[21], cy[93], cx[23], cy[94], 0xFF565B54);
        p.fill(cx[24], cy[93], cx[26], cy[94], 0xFF565B54);

        p.fill(cx[30], cy[93], cx[31], cy[95], 0xFF202423);

        p.fill(cx[8], cy[94], cx[9], cy[96], 0xFF565B54);

        p.fill(cx[18], cy[96], cx[19], cy[98], 0xFF202423);

        p.fill(cx[346], cy[99], cx[347], cy[101], 0xFF565B54);

        p.fill(cx[18], cy[100], cx[20], cy[101], 0xFF676763);

        p.fill(cx[20], cy[100], cx[22], cy[101], 0xFF565B54);
        p.fill(cx[331], cy[100], cx[333], cy[101], 0xFF565B54);

        p.fill(cx[333], cy[100], cx[335], cy[101], 0xFF51504E);
        p.fill(cx[6], cy[101], cx[7], cy[103], 0xFF51504E);

        p.fill(cx[338], cy[101], cx[340], cy[102], 0xFF2F3336);

        p.fill(cx[340], cy[101], cx[341], cy[103], 0xFF42473F);

        p.fill(cx[29], cy[102], cx[30], cy[104], 0xFF51504E);

        p.fill(cx[41], cy[103], cx[42], cy[105], 0xFF2F3336);

        p.fill(cx[340], cy[103], cx[341], cy[105], 0xFF51504E);

        p.fill(cx[22], cy[105], cx[23], cy[107], 0xFFFBFBFB);

        p.fill(cx[318], cy[105], cx[319], cy[107], 0xFF3C3F3B);

        p.fill(cx[335], cy[105], cx[337], cy[106], 0xFFFBFBFB);

        p.fill(cx[337], cy[105], cx[338], cy[107], 0xFFA5A49E);

        p.fill(cx[348], cy[105], cx[349], cy[107], 0xFF61625E);

        p.fill(cx[9], cy[106], cx[11], cy[107], 0xFF565B54);

        p.fill(cx[11], cy[106], cx[12], cy[108], 0xFF51504E);

        p.fill(cx[37], cy[106], cx[39], cy[107], 0xFFFBFBFB);

        p.fill(cx[29], cy[108], cx[30], cy[110], 0xFF565B54);

        p.fill(cx[334], cy[109], cx[335], cy[111], 0xFF42473F);

        p.fill(cx[350], cy[109], cx[352], cy[110], 0xFF676763);

        p.fill(cx[29], cy[110], cx[30], cy[112], 0xFF51504E);
        p.fill(cx[340], cy[110], cx[341], cy[112], 0xFF51504E);

        p.fill(cx[346], cy[110], cx[347], cy[112], 0xFF565B54);

        p.fill(cx[30], cy[111], cx[31], cy[113], 0xFF202423);

        p.fill(cx[359], cy[111], cx[360], cy[113], 0xFF565B54);

        p.fill(cx[346], cy[112], cx[347], cy[114], 0xFF51504E);

        p.fill(cx[7], cy[114], cx[8], cy[116], 0xFF676763);

        p.fill(cx[31], cy[114], cx[32], cy[116], 0xFF202423);

        p.fill(cx[326], cy[114], cx[328], cy[115], 0xFF2F3336);

        p.fill(cx[328], cy[114], cx[329], cy[116], 0xFF202423);

        p.fill(cx[334], cy[114], cx[336], cy[115], 0xFF3C3F3B);

        p.fill(cx[351], cy[114], cx[352], cy[116], 0xFF61625E);

        p.fill(cx[351], cy[116], cx[352], cy[118], 0xFF42473F);

        p.fill(cx[30], cy[117], cx[32], cy[118], 0xFF2F3336);

        p.fill(cx[337], cy[118], cx[339], cy[119], 0xFF61625E);

        p.fill(cx[351], cy[118], cx[352], cy[120], 0xFF565B54);

        p.fill(cx[19], cy[119], cx[20], cy[121], 0xFF42473F);

        p.fill(cx[33], cy[119], cx[34], cy[121], 0xFF2F3336);
        p.fill(cx[42], cy[119], cx[43], cy[121], 0xFF2F3336);

        p.fill(cx[341], cy[119], cx[342], cy[121], 0xFF565B54);

        p.fill(cx[351], cy[120], cx[352], cy[122], 0xFF42473F);

        p.fill(cx[19], cy[121], cx[20], cy[123], 0xFF51504E);

        p.fill(cx[351], cy[122], cx[352], cy[124], 0xFF61625E);

        p.fill(cx[22], cy[123], cx[23], cy[125], 0xFFFBFBFB);

        p.fill(cx[331], cy[123], cx[332], cy[125], 0xFF51504E);
        p.fill(cx[338], cy[123], cx[340], cy[124], 0xFF51504E);
        p.fill(cx[341], cy[123], cx[342], cy[125], 0xFF51504E);

        p.fill(cx[29], cy[124], cx[30], cy[126], 0xFF565B54);

        p.fill(cx[338], cy[124], cx[339], cy[126], 0xFF42473F);

        p.fill(cx[352], cy[124], cx[353], cy[126], 0xFF61625E);

        p.fill(cx[333], cy[125], cx[334], cy[127], 0xFF51504E);

        p.fill(cx[341], cy[125], cx[342], cy[127], 0xFF61625E);

        p.fill(cx[8], cy[126], cx[9], cy[128], 0xFF565B54);
        p.fill(cx[352], cy[126], cx[353], cy[128], 0xFF565B54);

        p.fill(cx[8], cy[128], cx[9], cy[130], 0xFF3C3F3B);

        p.fill(cx[6], cy[129], cx[7], cy[131], 0xFF51504E);

        p.fill(cx[7], cy[130], cx[9], cy[131], 0xFF61625E);

        p.fill(cx[6], cy[131], cx[8], cy[132], 0xFF565B54);

        p.fill(cx[8], cy[131], cx[9], cy[133], 0xFF202423);

        p.fill(cx[351], cy[131], cx[352], cy[133], 0xFF42473F);

        p.fill(cx[8], cy[133], cx[9], cy[135], 0xFF676763);

        p.fill(cx[17], cy[133], cx[19], cy[134], 0xFF202423);
        p.fill(cx[30], cy[133], cx[32], cy[134], 0xFF202423);
        p.fill(cx[27], cy[134], cx[28], cy[136], 0xFF202423);
        p.fill(cx[28], cy[135], cx[30], cy[136], 0xFF202423);

        p.fill(cx[30], cy[135], cx[32], cy[136], 0xFF2F3336);
        p.fill(cx[341], cy[135], cx[342], cy[137], 0xFF2F3336);

        p.fill(cx[30], cy[136], cx[32], cy[137], 0xFF202423);

        p.fill(cx[326], cy[136], cx[328], cy[137], 0xFF2F3336);

        p.fill(cx[330], cy[136], cx[332], cy[137], 0xFF42473F);

        p.fill(cx[19], cy[137], cx[21], cy[138], 0xFF2F3336);
        p.fill(cx[330], cy[137], cx[332], cy[138], 0xFF2F3336);

        p.fill(cx[330], cy[138], cx[331], cy[140], 0xFF42473F);
        p.fill(cx[339], cy[138], cx[341], cy[139], 0xFF42473F);

        p.fill(cx[341], cy[139], cx[342], cy[141], 0xFF51504E);

        p.fill(cx[351], cy[139], cx[352], cy[141], 0xFF676763);

        p.fill(cx[350], cy[140], cx[351], cy[142], 0xFF61625E);

        p.fill(cx[352], cy[140], cx[354], cy[141], 0xFF42473F);

        p.fill(cx[22], cy[141], cx[23], cy[143], 0xFFFBFBFB);

        p.fill(cx[26], cy[141], cx[28], cy[142], 0xFF565B54);

        p.fill(cx[333], cy[141], cx[334], cy[143], 0xFFFBFBFB);

        p.fill(cx[335], cy[141], cx[336], cy[143], 0xFFA5A49E);

        p.fill(cx[336], cy[141], cx[337], cy[143], 0xFF51504E);
        p.fill(cx[351], cy[141], cx[352], cy[143], 0xFF51504E);

        p.fill(cx[352], cy[141], cx[353], cy[143], 0xFF3C3F3B);

        p.fill(cx[29], cy[142], cx[30], cy[144], 0xFF565B54);

        p.fill(cx[37], cy[142], cx[39], cy[143], 0xFFFBFBFB);

        p.fill(cx[321], cy[142], cx[323], cy[143], 0xFFECEEEF);

        p.fill(cx[350], cy[142], cx[351], cy[144], 0xFF565B54);

        p.fill(cx[334], cy[143], cx[335], cy[145], 0xFF42473F);
        p.fill(cx[332], cy[145], cx[333], cy[147], 0xFF42473F);

        p.fill(cx[341], cy[146], cx[342], cy[148], 0xFF51504E);

        p.fill(cx[18], cy[147], cx[20], cy[148], 0xFF565B54);
        p.fill(cx[330], cy[147], cx[332], cy[148], 0xFF565B54);

        p.fill(cx[350], cy[148], cx[351], cy[150], 0xFF61625E);

        p.fill(cx[354], cy[148], cx[356], cy[149], 0xFF565B54);

        p.fill(cx[40], cy[149], cx[42], cy[150], 0xFF2F3336);

        p.fill(cx[347], cy[149], cx[348], cy[151], 0xFF565B54);

        p.fill(cx[325], cy[150], cx[326], cy[152], 0xFF202423);

        p.fill(cx[326], cy[150], cx[327], cy[152], 0xFF2F3336);

        p.fill(cx[349], cy[150], cx[351], cy[151], 0xFF676763);

        p.fill(cx[9], cy[151], cx[10], cy[153], 0xFF61625E);

        p.fill(cx[351], cy[152], cx[352], cy[154], 0xFF565B54);

        p.fill(cx[9], cy[153], cx[11], cy[154], 0xFF676763);

        p.fill(cx[41], cy[154], cx[42], cy[156], 0xFF3C3F3B);

        p.fill(cx[4], cy[155], cx[5], cy[157], 0xFF565B54);
        p.fill(cx[351], cy[155], cx[352], cy[157], 0xFF565B54);

        p.fill(cx[8], cy[157], cx[9], cy[159], 0xFF676763);

        p.fill(cx[41], cy[157], cx[43], cy[158], 0xFF3C3F3B);

        p.fill(cx[348], cy[157], cx[349], cy[159], 0xFF676763);

        p.fill(cx[4], cy[159], cx[5], cy[161], 0xFF3D3E3D);

        p.fill(cx[8], cy[159], cx[10], cy[160], 0xFF202423);

        p.fill(cx[345], cy[160], cx[347], cy[161], 0xFF2F3336);

        p.fill(cx[347], cy[160], cx[348], cy[162], 0xFF565B54);

        p.fill(cx[357], cy[160], cx[358], cy[162], 0xFF42473F);

        p.fill(cx[318], cy[161], cx[319], cy[163], 0xFF3C3F3B);

        p.fill(cx[341], cy[161], cx[343], cy[162], 0xFF2F3336);

        p.fill(cx[3], cy[162], cx[4], cy[164], 0xFF42473F);

        p.fill(cx[4], cy[162], cx[5], cy[164], 0xFF3D3E3D);

        p.fill(cx[25], cy[162], cx[27], cy[163], 0xFF3C3F3B);

        p.fill(cx[347], cy[162], cx[349], cy[163], 0xFF676763);

        p.fill(cx[39], cy[163], cx[41], cy[164], 0xFF42473F);

        p.fill(cx[42], cy[163], cx[43], cy[165], 0xFF565B54);

        p.fill(cx[317], cy[163], cx[319], cy[164], 0xFF51504E);

        p.fill(cx[334], cy[163], cx[336], cy[164], 0xFF2F3336);
        p.fill(cx[19], cy[164], cx[21], cy[165], 0xFF2F3336);

        p.fill(cx[40], cy[164], cx[42], cy[165], 0xFF565B54);

        p.fill(cx[5], cy[165], cx[6], cy[167], 0xFF676763);

        p.fill(cx[13], cy[165], cx[14], cy[167], 0xFF2F3336);

        p.fill(cx[40], cy[165], cx[41], cy[167], 0xFF565B54);

        p.fill(cx[41], cy[165], cx[43], cy[166], 0xFF61625E);

        p.fill(cx[318], cy[165], cx[320], cy[166], 0xFF565B54);

        p.fill(cx[333], cy[165], cx[334], cy[167], 0xFF3C3F3B);

        p.fill(cx[347], cy[165], cx[348], cy[167], 0xFF676763);

        p.fill(cx[0], cy[166], cx[2], cy[167], 0xFF51504E);

        p.fill(cx[24], cy[166], cx[25], cy[168], 0xFF2F3336);

        p.fill(cx[91], cy[166], cx[93], cy[167], 0xFF51504E);

        p.fill(cx[132], cy[166], cx[133], cy[168], 0xFF42473F);

        p.fill(cx[192], cy[166], cx[194], cy[167], 0xFF51504E);

        p.fill(cx[294], cy[166], cx[296], cy[167], 0xFF3D3E3D);

        p.fill(cx[354], cy[166], cx[356], cy[167], 0xFF2F3336);

        p.fill(cx[48], cy[167], cx[50], cy[168], 0xFF42473F);

        p.fill(cx[51], cy[167], cx[53], cy[168], 0xFF51504E);

        p.fill(cx[80], cy[167], cx[82], cy[168], 0xFF42473F);
        p.fill(cx[192], cy[167], cx[194], cy[168], 0xFF42473F);

        p.fill(cx[244], cy[167], cx[246], cy[168], 0xFF51504E);

        p.fill(cx[246], cy[167], cx[248], cy[168], 0xFF42473F);

        p.fill(cx[355], cy[167], cx[356], cy[169], 0xFF51504E);

        p.fill(cx[44], cy[168], cx[46], cy[169], 0xFF565B54);
        p.fill(cx[51], cy[168], cx[53], cy[169], 0xFF565B54);

        p.fill(cx[91], cy[168], cx[92], cy[170], 0xFFECEEEF);

        p.fill(cx[99], cy[168], cx[101], cy[169], 0xFF51504E);

        p.fill(cx[109], cy[168], cx[111], cy[169], 0xFF3C3F3B);

        p.fill(cx[116], cy[168], cx[117], cy[170], 0xFFD1D2C3);

        p.fill(cx[119], cy[168], cx[121], cy[169], 0xFF51504E);

        p.fill(cx[121], cy[168], cx[123], cy[169], 0xFF42473F);

        p.fill(cx[124], cy[168], cx[126], cy[169], 0xFF565B54);

        p.fill(cx[140], cy[168], cx[142], cy[169], 0xFF3C3F3B);

        p.fill(cx[217], cy[168], cx[218], cy[170], 0xFFECEEEF);

        p.fill(cx[238], cy[168], cx[240], cy[169], 0xFF42473F);

        p.fill(cx[296], cy[168], cx[298], cy[169], 0xFF3C3F3B);
        p.fill(cx[352], cy[168], cx[354], cy[169], 0xFF3C3F3B);

        p.fill(cx[6], cy[169], cx[8], cy[170], 0xFF2F3336);

        p.fill(cx[349], cy[169], cx[350], cy[171], 0xFF676763);

        p.fill(cx[116], cy[170], cx[118], cy[171], 0xFF202423);
        p.fill(cx[217], cy[170], cx[219], cy[171], 0xFF202423);

        p.fill(cx[320], cy[170], cx[322], cy[171], 0xFF2F3336);

        p.fill(cx[6], cy[171], cx[7], cy[173], 0xFF565B54);

        p.fill(cx[29], cy[171], cx[30], cy[173], 0xFF3C3F3B);

        p.fill(cx[91], cy[171], cx[92], cy[173], 0xFF2F3336);
        p.fill(cx[142], cy[171], cx[143], cy[173], 0xFF2F3336);
        p.fill(cx[167], cy[171], cx[168], cy[173], 0xFF2F3336);
        p.fill(cx[192], cy[171], cx[193], cy[173], 0xFF2F3336);
        p.fill(cx[242], cy[171], cx[243], cy[173], 0xFF2F3336);
        p.fill(cx[268], cy[171], cx[269], cy[173], 0xFF2F3336);

        p.fill(cx[355], cy[172], cx[356], cy[174], 0xFF61625E);

        p.fill(cx[27], cy[173], cx[28], cy[175], 0xFF2F3336);

        p.fill(cx[113], cy[174], cx[115], cy[175], 0xFF202423);

        p.fill(cx[148], cy[174], cx[149], cy[176], 0xFF2F3336);

        p.fill(cx[201], cy[174], cx[203], cy[175], 0xFF202423);
        p.fill(cx[227], cy[174], cx[229], cy[175], 0xFF202423);

        p.fill(cx[240], cy[174], cx[241], cy[176], 0xFF2F3336);

        p.fill(cx[316], cy[174], cx[318], cy[175], 0xFF202423);

        p.fill(cx[322], cy[174], cx[323], cy[176], 0xFF3C3F3B);

        p.fill(cx[113], cy[175], cx[115], cy[176], 0xFF2F3336);

        p.fill(cx[278], cy[175], cx[280], cy[176], 0xFF202423);

        p.fill(cx[348], cy[175], cx[350], cy[176], 0xFF676763);

        p.fill(cx[35], cy[176], cx[37], cy[177], 0xFF2F3336);

        p.fill(cx[129], cy[176], cx[131], cy[177], 0xFF202423);

        p.fill(cx[3], cy[177], cx[5], cy[178], 0xFF6D6A64);

        p.fill(cx[69], cy[177], cx[71], cy[178], 0xFF676763);

        p.fill(cx[89], cy[177], cx[91], cy[178], 0xFF61625E);

        p.fill(cx[97], cy[177], cx[99], cy[178], 0xFF51504E);

        p.fill(cx[121], cy[177], cx[123], cy[178], 0xFF565B54);
        p.fill(cx[141], cy[177], cx[143], cy[178], 0xFF565B54);
        p.fill(cx[188], cy[177], cx[190], cy[178], 0xFF565B54);

        p.fill(cx[218], cy[177], cx[220], cy[178], 0xFF51504E);

        p.fill(cx[272], cy[177], cx[274], cy[178], 0xFF565B54);

        p.fill(cx[274], cy[177], cx[276], cy[178], 0xFF202423);
        p.fill(cx[300], cy[177], cx[302], cy[178], 0xFF202423);

        p.fill(cx[317], cy[177], cx[319], cy[178], 0xFF3C3F3B);

        p.fill(cx[327], cy[177], cx[328], cy[179], 0xFF2F3336);

        p.fill(cx[352], cy[177], cx[354], cy[178], 0xFF8A8C83);

        p.fill(cx[24], cy[178], cx[26], cy[179], 0xFF2F3336);

        p.fill(cx[87], cy[178], cx[88], cy[180], 0xFF51504E);

        p.fill(cx[161], cy[178], cx[163], cy[179], 0xFF42473F);

        p.fill(cx[188], cy[178], cx[189], cy[180], 0xFF51504E);

        p.fill(cx[211], cy[178], cx[213], cy[179], 0xFF42473F);

        p.fill(cx[221], cy[178], cx[223], cy[179], 0xFF51504E);

        p.fill(cx[255], cy[178], cx[256], cy[180], 0xFF3C3F3B);

        p.fill(cx[48], cy[179], cx[50], cy[180], 0xFF2F3336);

        p.fill(cx[137], cy[179], cx[138], cy[181], 0xFF42473F);

        p.fill(cx[165], cy[179], cx[167], cy[180], 0xFF51504E);

        p.fill(cx[172], cy[179], cx[173], cy[181], 0xFF42473F);

        p.fill(cx[194], cy[179], cx[195], cy[181], 0xFF51504E);

        p.fill(cx[111], cy[180], cx[113], cy[181], 0xFF42473F);

        p.fill(cx[113], cy[180], cx[114], cy[182], 0xFF51504E);

        p.fill(cx[117], cy[180], cx[119], cy[181], 0xFF42473F);

        p.fill(cx[123], cy[180], cx[124], cy[182], 0xFF51504E);

        p.fill(cx[166], cy[180], cx[168], cy[181], 0xFF42473F);

        p.fill(cx[193], cy[180], cx[194], cy[182], 0xFF51504E);

        p.fill(cx[205], cy[180], cx[206], cy[182], 0xFF2F3336);

        p.fill(cx[212], cy[180], cx[214], cy[181], 0xFF42473F);

        p.fill(cx[239], cy[180], cx[241], cy[181], 0xFF51504E);

        p.fill(cx[241], cy[180], cx[243], cy[181], 0xFF42473F);

        p.fill(cx[254], cy[180], cx[255], cy[182], 0xFF202423);

        p.fill(cx[262], cy[180], cx[264], cy[181], 0xFF42473F);

        p.fill(cx[7], cy[181], cx[8], cy[183], 0xFF51504E);

        p.fill(cx[28], cy[181], cx[29], cy[183], 0xFF202423);

        p.fill(cx[61], cy[181], cx[62], cy[183], 0xFF51504E);

        p.fill(cx[67], cy[181], cx[69], cy[182], 0xFFFBFBFB);

        p.fill(cx[88], cy[181], cx[89], cy[183], 0xFF51504E);

        p.fill(cx[89], cy[181], cx[91], cy[182], 0xFF42473F);

        p.fill(cx[115], cy[181], cx[116], cy[183], 0xFF2F3336);

        p.fill(cx[118], cy[181], cx[120], cy[182], 0xFF51504E);
        p.fill(cx[141], cy[181], cx[143], cy[182], 0xFF51504E);

        p.fill(cx[240], cy[181], cx[242], cy[182], 0xFF42473F);

        p.fill(cx[331], cy[181], cx[332], cy[183], 0xFF202423);

        p.fill(cx[4], cy[182], cx[6], cy[183], 0xFFA5A49E);

        p.fill(cx[41], cy[182], cx[42], cy[184], 0xFF2F3336);

        p.fill(cx[47], cy[182], cx[48], cy[184], 0xFF3C3F3B);

        p.fill(cx[64], cy[182], cx[65], cy[184], 0xFFFBFBFB);
        p.fill(cx[68], cy[182], cx[69], cy[184], 0xFFFBFBFB);

        p.fill(cx[93], cy[182], cx[94], cy[184], 0xFF42473F);

        p.fill(cx[94], cy[182], cx[95], cy[184], 0xFFFBFBFB);

        p.fill(cx[95], cy[182], cx[96], cy[184], 0xFF51504E);

        p.fill(cx[96], cy[182], cx[98], cy[183], 0xFF42473F);

        p.fill(cx[114], cy[182], cx[115], cy[184], 0xFF8B9494);

        p.fill(cx[119], cy[182], cx[120], cy[184], 0xFFFBFBFB);
        p.fill(cx[140], cy[182], cx[141], cy[184], 0xFFFBFBFB);
        p.fill(cx[165], cy[182], cx[166], cy[184], 0xFFFBFBFB);
        p.fill(cx[190], cy[182], cx[191], cy[184], 0xFFFBFBFB);

        p.fill(cx[193], cy[182], cx[195], cy[183], 0xFF42473F);

        p.fill(cx[218], cy[182], cx[219], cy[184], 0xFF2F3336);

        p.fill(cx[219], cy[182], cx[221], cy[183], 0xFFFBFBFB);

        p.fill(cx[242], cy[182], cx[243], cy[184], 0xFF51504E);

        p.fill(cx[245], cy[182], cx[246], cy[184], 0xFFFBFBFB);

        p.fill(cx[265], cy[182], cx[266], cy[184], 0xFFA5A49E);

        p.fill(cx[266], cy[182], cx[268], cy[183], 0xFF51504E);

        p.fill(cx[292], cy[182], cx[293], cy[184], 0xFFD43835);
        p.fill(cx[295], cy[182], cx[296], cy[184], 0xFFD43835);

        p.fill(cx[311], cy[182], cx[313], cy[183], 0xFF3C3F3B);
        p.fill(cx[318], cy[182], cx[320], cy[183], 0xFF3C3F3B);

        p.fill(cx[7], cy[183], cx[8], cy[185], 0xFF565B54);

        p.fill(cx[61], cy[183], cx[62], cy[185], 0xFF42473F);

        p.fill(cx[66], cy[183], cx[68], cy[184], 0xFF51504E);
        p.fill(cx[90], cy[183], cx[91], cy[185], 0xFF51504E);

        p.fill(cx[255], cy[183], cx[256], cy[185], 0xFF3C3F3B);

        p.fill(cx[269], cy[183], cx[271], cy[184], 0xFFFBFBFB);

        p.fill(cx[312], cy[183], cx[313], cy[185], 0xFF3C3F3B);

        p.fill(cx[315], cy[183], cx[316], cy[185], 0xFF2F3336);

        p.fill(cx[0], cy[184], cx[1], cy[186], 0xFF797D7C);

        p.fill(cx[28], cy[184], cx[29], cy[186], 0xFF3C3F3B);
        p.fill(cx[44], cy[184], cx[45], cy[186], 0xFF3C3F3B);

        p.fill(cx[66], cy[184], cx[68], cy[185], 0xFFFBFBFB);

        p.fill(cx[211], cy[184], cx[212], cy[186], 0xFF42473F);
        p.fill(cx[272], cy[184], cx[273], cy[186], 0xFF42473F);

        p.fill(cx[358], cy[184], cx[360], cy[185], 0xFF8A8C83);

        p.fill(cx[90], cy[185], cx[91], cy[187], 0xFF42473F);
        p.fill(cx[136], cy[185], cx[137], cy[187], 0xFF42473F);
        p.fill(cx[138], cy[185], cx[139], cy[187], 0xFF42473F);
        p.fill(cx[142], cy[185], cx[143], cy[187], 0xFF42473F);
        p.fill(cx[223], cy[185], cx[224], cy[187], 0xFF42473F);
        p.fill(cx[240], cy[185], cx[241], cy[187], 0xFF42473F);
    }

    private static void part7(Paint p, int[] cx, int[] cy) {

        p.fill(cx[271], cy[185], cx[272], cy[187], 0xFF42473F);

        p.fill(cx[318], cy[185], cx[320], cy[186], 0xFF2F3336);

        p.fill(cx[358], cy[185], cx[360], cy[186], 0xFF797D7C);

        p.fill(cx[5], cy[186], cx[7], cy[187], 0xFF8B9494);

        p.fill(cx[63], cy[186], cx[65], cy[187], 0xFF42473F);

        p.fill(cx[71], cy[186], cx[72], cy[188], 0xFF51504E);

        p.fill(cx[125], cy[186], cx[126], cy[188], 0xFF202423);
        p.fill(cx[150], cy[186], cx[151], cy[188], 0xFF202423);

        p.fill(cx[272], cy[186], cx[274], cy[187], 0xFF51504E);

        p.fill(cx[28], cy[187], cx[29], cy[189], 0xFF42473F);

        p.fill(cx[105], cy[187], cx[106], cy[189], 0xFF202423);

        p.fill(cx[112], cy[187], cx[114], cy[188], 0xFF565B54);
        p.fill(cx[142], cy[187], cx[144], cy[188], 0xFF565B54);

        p.fill(cx[160], cy[187], cx[161], cy[189], 0xFF202423);

        p.fill(cx[167], cy[187], cx[169], cy[188], 0xFF61625E);

        p.fill(cx[169], cy[187], cx[171], cy[188], 0xFF51504E);

        p.fill(cx[244], cy[187], cx[246], cy[188], 0xFF565B54);

        p.fill(cx[315], cy[187], cx[317], cy[188], 0xFF2F3336);

        p.fill(cx[358], cy[187], cx[360], cy[188], 0xFF6D6A64);

        p.fill(cx[6], cy[188], cx[8], cy[189], 0xFF61625E);

        p.fill(cx[148], cy[188], cx[150], cy[189], 0xFF202423);

        p.fill(cx[285], cy[188], cx[287], cy[189], 0xFF2F3336);
        p.fill(cx[315], cy[188], cx[316], cy[190], 0xFF2F3336);

        p.fill(cx[352], cy[188], cx[354], cy[189], 0xFF61625E);

        p.fill(cx[3], cy[189], cx[5], cy[190], 0xFF676763);

        p.fill(cx[5], cy[189], cx[7], cy[190], 0xFF61625E);

        p.fill(cx[11], cy[189], cx[13], cy[190], 0xFF3C3F3B);

        p.fill(cx[60], cy[189], cx[62], cy[190], 0xFF202423);
        p.fill(cx[66], cy[189], cx[68], cy[190], 0xFF202423);
        p.fill(cx[69], cy[189], cx[71], cy[190], 0xFF202423);
        p.fill(cx[118], cy[189], cx[120], cy[190], 0xFF202423);

        p.fill(cx[348], cy[189], cx[349], cy[191], 0xFF2F3336);

        p.fill(cx[351], cy[189], cx[353], cy[190], 0xFF565B54);

        p.fill(cx[356], cy[189], cx[358], cy[190], 0xFF676763);
        p.fill(cx[0], cy[190], cx[2], cy[191], 0xFF676763);

        p.fill(cx[12], cy[190], cx[14], cy[191], 0xFF3C3F3B);
        p.fill(cx[315], cy[190], cx[316], cy[192], 0xFF3C3F3B);

        p.fill(cx[352], cy[190], cx[354], cy[191], 0xFF565B54);

        p.fill(cx[12], cy[191], cx[14], cy[192], 0xFF2F3336);

        p.fill(cx[27], cy[191], cx[28], cy[193], 0xFF202423);

        p.fill(cx[132], cy[192], cx[134], cy[193], 0xFF2F3336);
        p.fill(cx[15], cy[193], cx[17], cy[194], 0xFF2F3336);

        p.fill(cx[273], cy[193], cx[275], cy[194], 0xFF42473F);

        p.fill(cx[19], cy[194], cx[21], cy[195], 0xFF2F3336);

        p.fill(cx[337], cy[194], cx[339], cy[195], 0xFF3C3F3B);

        p.fill(cx[339], cy[194], cx[341], cy[195], 0xFF2F3336);

        p.fill(cx[358], cy[194], cx[360], cy[195], 0xFF565B54);

        p.fill(cx[199], cy[195], cx[201], cy[196], 0xFF3C3F3B);

        p.fill(cx[255], cy[195], cx[257], cy[196], 0xFF3D3E3D);

        p.fill(cx[308], cy[195], cx[309], cy[197], 0xFF2F3336);
        p.fill(cx[337], cy[195], cx[338], cy[197], 0xFF2F3336);

        p.fill(cx[90], cy[196], cx[92], cy[197], 0xFF3C3F3B);
        p.fill(cx[93], cy[196], cx[95], cy[197], 0xFF3C3F3B);
        p.fill(cx[129], cy[196], cx[131], cy[197], 0xFF3C3F3B);

        p.fill(cx[291], cy[196], cx[292], cy[198], 0xFF42473F);

        p.fill(cx[292], cy[196], cx[294], cy[197], 0xFF3C3F3B);
        p.fill(cx[28], cy[197], cx[30], cy[198], 0xFF3C3F3B);

        p.fill(cx[270], cy[197], cx[272], cy[198], 0xFF3D3E3D);

        p.fill(cx[293], cy[197], cx[294], cy[199], 0xFF3C3F3B);

        p.fill(cx[77], cy[198], cx[79], cy[199], 0xFF3D3E3D);
        p.fill(cx[64], cy[199], cx[65], cy[201], 0xFF3D3E3D);

        p.fill(cx[77], cy[199], cx[78], cy[201], 0xFF42473F);

        p.fill(cx[80], cy[199], cx[82], cy[200], 0xFF3D3E3D);
        p.fill(cx[279], cy[199], cx[281], cy[200], 0xFF3D3E3D);

        p.fill(cx[291], cy[199], cx[292], cy[201], 0xFF42473F);

        p.fill(cx[293], cy[199], cx[294], cy[201], 0xFF3D3E3D);

        p.fill(cx[25], cy[200], cx[26], cy[202], 0xFF3C3F3B);

        p.fill(cx[66], cy[200], cx[67], cy[202], 0xFF42473F);

        p.fill(cx[266], cy[200], cx[268], cy[201], 0xFF3C3F3B);

        p.fill(cx[268], cy[200], cx[269], cy[202], 0xFF3D3E3D);

        p.fill(cx[269], cy[200], cx[270], cy[202], 0xFF3C3F3B);

        p.fill(cx[274], cy[200], cx[275], cy[202], 0xFF3D3E3D);
        p.fill(cx[278], cy[200], cx[279], cy[202], 0xFF3D3E3D);
        p.fill(cx[280], cy[200], cx[282], cy[201], 0xFF3D3E3D);
        p.fill(cx[294], cy[200], cx[295], cy[202], 0xFF3D3E3D);

        p.fill(cx[316], cy[200], cx[318], cy[201], 0xFF3C3F3B);
        p.fill(cx[319], cy[200], cx[320], cy[202], 0xFF3C3F3B);
        p.fill(cx[322], cy[200], cx[323], cy[202], 0xFF3C3F3B);

        p.fill(cx[10], cy[201], cx[11], cy[203], 0xFF42473F);

        p.fill(cx[50], cy[201], cx[51], cy[203], 0xFF3C3F3B);

        p.fill(cx[265], cy[201], cx[266], cy[203], 0xFF42473F);

        p.fill(cx[266], cy[201], cx[268], cy[202], 0xFF3D3E3D);

        p.fill(cx[1], cy[202], cx[3], cy[203], 0xFF42473F);

        p.fill(cx[25], cy[202], cx[27], cy[203], 0xFF2F3336);

        p.fill(cx[52], cy[202], cx[54], cy[203], 0xFF3C3F3B);

        p.fill(cx[74], cy[202], cx[76], cy[203], 0xFF3D3E3D);
        p.fill(cx[78], cy[202], cx[80], cy[203], 0xFF3D3E3D);

        p.fill(cx[96], cy[202], cx[98], cy[203], 0xFF42473F);

        p.fill(cx[235], cy[202], cx[237], cy[203], 0xFF3D3E3D);

        p.fill(cx[239], cy[202], cx[241], cy[203], 0xFF42473F);

        p.fill(cx[131], cy[0], cx[132], cy[1], 0xFF565B54);

        p.fill(cx[212], cy[0], cx[213], cy[1], 0xFF676763);

        p.fill(cx[213], cy[0], cx[214], cy[1], 0xFF61625E);
        p.fill(cx[317], cy[0], cx[318], cy[1], 0xFF61625E);

        p.fill(cx[44], cy[1], cx[45], cy[2], 0xFF565B54);
        p.fill(cx[77], cy[1], cx[78], cy[2], 0xFF565B54);

        p.fill(cx[213], cy[1], cx[214], cy[2], 0xFF676763);

        p.fill(cx[78], cy[2], cx[79], cy[3], 0xFF565B54);

        p.fill(cx[183], cy[2], cx[184], cy[3], 0xFF676763);
        p.fill(cx[210], cy[2], cx[211], cy[3], 0xFF676763);
        p.fill(cx[217], cy[2], cx[218], cy[3], 0xFF676763);

        p.fill(cx[78], cy[3], cx[79], cy[4], 0xFF51504E);

        p.fill(cx[234], cy[3], cx[235], cy[4], 0xFF676763);
        p.fill(cx[236], cy[3], cx[237], cy[4], 0xFF676763);
        p.fill(cx[248], cy[3], cx[249], cy[4], 0xFF676763);

        p.fill(cx[30], cy[4], cx[31], cy[5], 0xFF565B54);
        p.fill(cx[34], cy[4], cx[35], cy[5], 0xFF565B54);

        p.fill(cx[56], cy[4], cx[57], cy[5], 0xFF51504E);

        p.fill(cx[116], cy[4], cx[117], cy[5], 0xFF565B54);

        p.fill(cx[298], cy[4], cx[299], cy[5], 0xFF61625E);

        p.fill(cx[334], cy[4], cx[335], cy[5], 0xFF51504E);

        p.fill(cx[16], cy[5], cx[17], cy[6], 0xFF676763);

        p.fill(cx[19], cy[5], cx[20], cy[6], 0xFF3C3F3B);

        p.fill(cx[28], cy[5], cx[29], cy[6], 0xFF6D6A64);

        p.fill(cx[30], cy[5], cx[31], cy[6], 0xFF51504E);
        p.fill(cx[37], cy[5], cx[38], cy[6], 0xFF51504E);
        p.fill(cx[39], cy[5], cx[40], cy[6], 0xFF51504E);

        p.fill(cx[51], cy[5], cx[52], cy[6], 0xFF676763);

        p.fill(cx[79], cy[5], cx[80], cy[6], 0xFF6D6A64);

        p.fill(cx[80], cy[5], cx[81], cy[6], 0xFF202423);

        p.fill(cx[330], cy[5], cx[331], cy[6], 0xFF51504E);

        p.fill(cx[342], cy[5], cx[343], cy[6], 0xFF61625E);

        p.fill(cx[343], cy[5], cx[344], cy[6], 0xFF676763);

        p.fill(cx[344], cy[5], cx[345], cy[6], 0xFF565B54);
        p.fill(cx[12], cy[6], cx[13], cy[7], 0xFF565B54);

        p.fill(cx[16], cy[6], cx[17], cy[7], 0xFF3C3F3B);

        p.fill(cx[19], cy[6], cx[20], cy[7], 0xFF3D3E3D);

        p.fill(cx[28], cy[6], cx[29], cy[7], 0xFF61625E);

        p.fill(cx[35], cy[6], cx[36], cy[7], 0xFF42473F);
        p.fill(cx[37], cy[6], cx[38], cy[7], 0xFF42473F);
        p.fill(cx[39], cy[6], cx[40], cy[7], 0xFF42473F);

        p.fill(cx[53], cy[6], cx[54], cy[7], 0xFF3C3F3B);

        p.fill(cx[61], cy[6], cx[62], cy[7], 0xFF51504E);
        p.fill(cx[71], cy[6], cx[72], cy[7], 0xFF51504E);

        p.fill(cx[80], cy[6], cx[81], cy[7], 0xFF42473F);

        p.fill(cx[139], cy[6], cx[140], cy[7], 0xFF676763);
        p.fill(cx[279], cy[6], cx[280], cy[7], 0xFF676763);
        p.fill(cx[281], cy[6], cx[282], cy[7], 0xFF676763);

        p.fill(cx[288], cy[6], cx[289], cy[7], 0xFF565B54);

        p.fill(cx[330], cy[6], cx[331], cy[7], 0xFF3C3F3B);

        p.fill(cx[337], cy[6], cx[338], cy[7], 0xFF3D3E3D);

        p.fill(cx[345], cy[6], cx[346], cy[7], 0xFF676763);
        p.fill(cx[12], cy[7], cx[13], cy[8], 0xFF676763);

        p.fill(cx[30], cy[7], cx[31], cy[8], 0xFF51504E);

        p.fill(cx[31], cy[7], cx[32], cy[8], 0xFF565B54);

        p.fill(cx[32], cy[7], cx[33], cy[8], 0xFF61625E);

        p.fill(cx[33], cy[7], cx[34], cy[8], 0xFF565B54);
        p.fill(cx[36], cy[7], cx[37], cy[8], 0xFF565B54);
        p.fill(cx[43], cy[7], cx[44], cy[8], 0xFF565B54);

        p.fill(cx[54], cy[7], cx[55], cy[8], 0xFF51504E);

        p.fill(cx[78], cy[7], cx[79], cy[8], 0xFF42473F);

        p.fill(cx[80], cy[7], cx[81], cy[8], 0xFF565B54);

        p.fill(cx[82], cy[7], cx[83], cy[8], 0xFF42473F);

        p.fill(cx[87], cy[7], cx[88], cy[8], 0xFF51504E);
        p.fill(cx[98], cy[7], cx[99], cy[8], 0xFF51504E);

        p.fill(cx[99], cy[7], cx[100], cy[8], 0xFF61625E);

        p.fill(cx[100], cy[7], cx[101], cy[8], 0xFF42473F);

        p.fill(cx[107], cy[7], cx[108], cy[8], 0xFF51504E);

        p.fill(cx[108], cy[7], cx[109], cy[8], 0xFF61625E);

        p.fill(cx[116], cy[7], cx[117], cy[8], 0xFF51504E);

        p.fill(cx[124], cy[7], cx[125], cy[8], 0xFF565B54);
        p.fill(cx[127], cy[7], cx[128], cy[8], 0xFF565B54);

        p.fill(cx[138], cy[7], cx[139], cy[8], 0xFF51504E);

        p.fill(cx[139], cy[7], cx[140], cy[8], 0xFF61625E);

        p.fill(cx[140], cy[7], cx[141], cy[8], 0xFF51504E);

        p.fill(cx[141], cy[7], cx[142], cy[8], 0xFF61625E);

        p.fill(cx[144], cy[7], cx[145], cy[8], 0xFF51504E);

        p.fill(cx[154], cy[7], cx[155], cy[8], 0xFF42473F);

        p.fill(cx[164], cy[7], cx[165], cy[8], 0xFF565B54);

        p.fill(cx[167], cy[7], cx[168], cy[8], 0xFF42473F);

        p.fill(cx[172], cy[7], cx[173], cy[8], 0xFF565B54);

        p.fill(cx[190], cy[7], cx[191], cy[8], 0xFF51504E);

        p.fill(cx[191], cy[7], cx[192], cy[8], 0xFF61625E);

        p.fill(cx[192], cy[7], cx[193], cy[8], 0xFF51504E);
        p.fill(cx[198], cy[7], cx[199], cy[8], 0xFF51504E);
        p.fill(cx[202], cy[7], cx[203], cy[8], 0xFF51504E);

        p.fill(cx[217], cy[7], cx[218], cy[8], 0xFF42473F);

        p.fill(cx[218], cy[7], cx[219], cy[8], 0xFF51504E);
        p.fill(cx[222], cy[7], cx[223], cy[8], 0xFF51504E);

        p.fill(cx[225], cy[7], cx[226], cy[8], 0xFF42473F);

        p.fill(cx[226], cy[7], cx[227], cy[8], 0xFF565B54);

        p.fill(cx[232], cy[7], cx[233], cy[8], 0xFF51504E);
        p.fill(cx[236], cy[7], cx[237], cy[8], 0xFF51504E);
        p.fill(cx[244], cy[7], cx[245], cy[8], 0xFF51504E);

        p.fill(cx[245], cy[7], cx[246], cy[8], 0xFF42473F);

        p.fill(cx[246], cy[7], cx[247], cy[8], 0xFF565B54);

        p.fill(cx[247], cy[7], cx[248], cy[8], 0xFF51504E);
        p.fill(cx[263], cy[7], cx[264], cy[8], 0xFF51504E);

        p.fill(cx[264], cy[7], cx[265], cy[8], 0xFF42473F);
        p.fill(cx[273], cy[7], cx[274], cy[8], 0xFF42473F);

        p.fill(cx[281], cy[7], cx[282], cy[8], 0xFF51504E);

        p.fill(cx[283], cy[7], cx[284], cy[8], 0xFF565B54);
        p.fill(cx[286], cy[7], cx[287], cy[8], 0xFF565B54);
        p.fill(cx[306], cy[7], cx[307], cy[8], 0xFF565B54);

        p.fill(cx[307], cy[7], cx[308], cy[8], 0xFF51504E);

        p.fill(cx[308], cy[7], cx[309], cy[8], 0xFF42473F);
        p.fill(cx[310], cy[7], cx[311], cy[8], 0xFF42473F);

        p.fill(cx[338], cy[7], cx[339], cy[8], 0xFF3D3E3D);

        p.fill(cx[347], cy[7], cx[348], cy[8], 0xFF676763);

        p.fill(cx[355], cy[7], cx[356], cy[8], 0xFF6D6A64);

        p.fill(cx[11], cy[8], cx[12], cy[9], 0xFF676763);

        p.fill(cx[12], cy[8], cx[13], cy[9], 0xFF42473F);

        p.fill(cx[54], cy[8], cx[55], cy[9], 0xFF3D3E3D);

        p.fill(cx[87], cy[8], cx[88], cy[9], 0xFF42473F);
        p.fill(cx[123], cy[8], cx[124], cy[9], 0xFF42473F);
        p.fill(cx[281], cy[8], cx[282], cy[9], 0xFF42473F);

        p.fill(cx[318], cy[8], cx[319], cy[9], 0xFF3D3E3D);

        p.fill(cx[319], cy[8], cx[320], cy[9], 0xFF3C3F3B);

        p.fill(cx[331], cy[8], cx[332], cy[9], 0xFF51504E);

        p.fill(cx[334], cy[8], cx[335], cy[9], 0xFF3C3F3B);

        p.fill(cx[339], cy[8], cx[340], cy[9], 0xFF3D3E3D);

        p.fill(cx[348], cy[8], cx[349], cy[9], 0xFF797D7C);

        p.fill(cx[53], cy[9], cx[54], cy[10], 0xFF3D3E3D);

        p.fill(cx[97], cy[9], cx[98], cy[10], 0xFF42473F);
        p.fill(cx[108], cy[9], cx[109], cy[10], 0xFF42473F);

        p.fill(cx[123], cy[9], cx[124], cy[10], 0xFF3C3F3B);

        p.fill(cx[124], cy[9], cx[125], cy[10], 0xFF3D3E3D);

        p.fill(cx[189], cy[9], cx[190], cy[10], 0xFF42473F);

        p.fill(cx[190], cy[9], cx[191], cy[10], 0xFF3D3E3D);

        p.fill(cx[281], cy[9], cx[282], cy[10], 0xFF3C3F3B);

        p.fill(cx[283], cy[9], cx[284], cy[10], 0xFF42473F);

        p.fill(cx[321], cy[9], cx[322], cy[10], 0xFF3D3E3D);

        p.fill(cx[331], cy[9], cx[332], cy[10], 0xFF61625E);

        p.fill(cx[349], cy[9], cx[350], cy[10], 0xFF8A8C83);

        p.fill(cx[7], cy[10], cx[8], cy[11], 0xFF8B9494);

        p.fill(cx[8], cy[10], cx[9], cy[11], 0xFF8A8C83);

        p.fill(cx[17], cy[10], cx[18], cy[11], 0xFF42473F);

        p.fill(cx[55], cy[10], cx[56], cy[11], 0xFF3C3F3B);

        p.fill(cx[56], cy[10], cx[57], cy[11], 0xFF3D3E3D);

        p.fill(cx[57], cy[10], cx[58], cy[11], 0xFF2F3336);
        p.fill(cx[66], cy[10], cx[67], cy[11], 0xFF2F3336);

        p.fill(cx[155], cy[10], cx[156], cy[11], 0xFF42473F);

        p.fill(cx[180], cy[10], cx[181], cy[11], 0xFF2F3336);

        p.fill(cx[283], cy[10], cx[284], cy[11], 0xFF3C3F3B);

        p.fill(cx[284], cy[10], cx[285], cy[11], 0xFF42473F);

        p.fill(cx[285], cy[10], cx[286], cy[11], 0xFF3C3F3B);

        p.fill(cx[286], cy[10], cx[287], cy[11], 0xFF202423);

        p.fill(cx[316], cy[10], cx[317], cy[11], 0xFF42473F);

        p.fill(cx[321], cy[10], cx[322], cy[11], 0xFF3C3F3B);
        p.fill(cx[340], cy[10], cx[341], cy[11], 0xFF3C3F3B);

        p.fill(cx[341], cy[10], cx[342], cy[11], 0xFF42473F);

        p.fill(cx[350], cy[10], cx[351], cy[11], 0xFF8A8C83);

        p.fill(cx[5], cy[11], cx[6], cy[12], 0xFF797D7C);

        p.fill(cx[16], cy[11], cx[17], cy[12], 0xFF42473F);

        p.fill(cx[48], cy[11], cx[49], cy[12], 0xFF3D3E3D);

        p.fill(cx[49], cy[11], cx[50], cy[12], 0xFF3C3F3B);
        p.fill(cx[56], cy[11], cx[57], cy[12], 0xFF3C3F3B);

        p.fill(cx[57], cy[11], cx[58], cy[12], 0xFF202423);

        p.fill(cx[58], cy[11], cx[59], cy[12], 0xFF2F3336);

        p.fill(cx[59], cy[11], cx[60], cy[12], 0xFF202423);
        p.fill(cx[72], cy[11], cx[73], cy[12], 0xFF202423);
        p.fill(cx[74], cy[11], cx[75], cy[12], 0xFF202423);

        p.fill(cx[85], cy[11], cx[86], cy[12], 0xFF676763);

        p.fill(cx[110], cy[11], cx[111], cy[12], 0xFF3C3F3B);

        p.fill(cx[124], cy[11], cx[125], cy[12], 0xFF202423);

        p.fill(cx[135], cy[11], cx[136], cy[12], 0xFF2F3336);

        p.fill(cx[155], cy[11], cx[156], cy[12], 0xFF3C3F3B);

        p.fill(cx[174], cy[11], cx[175], cy[12], 0xFF61625E);

        p.fill(cx[186], cy[11], cx[187], cy[12], 0xFF565B54);
        p.fill(cx[189], cy[11], cx[190], cy[12], 0xFF565B54);

        p.fill(cx[205], cy[11], cx[206], cy[12], 0xFF3C3F3B);

        p.fill(cx[210], cy[11], cx[211], cy[12], 0xFF2F3336);

        p.fill(cx[211], cy[11], cx[212], cy[12], 0xFF51504E);

        p.fill(cx[212], cy[11], cx[213], cy[12], 0xFF42473F);

        p.fill(cx[225], cy[11], cx[226], cy[12], 0xFF2F3336);

        p.fill(cx[236], cy[11], cx[237], cy[12], 0xFF51504E);
        p.fill(cx[241], cy[11], cx[242], cy[12], 0xFF51504E);

        p.fill(cx[246], cy[11], cx[247], cy[12], 0xFF565B54);

        p.fill(cx[273], cy[11], cx[274], cy[12], 0xFF51504E);

        p.fill(cx[274], cy[11], cx[275], cy[12], 0xFF42473F);

        p.fill(cx[283], cy[11], cx[284], cy[12], 0xFF3D3E3D);

        p.fill(cx[284], cy[11], cx[285], cy[12], 0xFF3C3F3B);

        p.fill(cx[286], cy[11], cx[287], cy[12], 0xFF2F3336);

        p.fill(cx[287], cy[11], cx[288], cy[12], 0xFF202423);
        p.fill(cx[300], cy[11], cx[301], cy[12], 0xFF202423);

        p.fill(cx[301], cy[11], cx[302], cy[12], 0xFF2F3336);

        p.fill(cx[302], cy[11], cx[303], cy[12], 0xFF202423);

        p.fill(cx[308], cy[11], cx[309], cy[12], 0xFF61625E);

        p.fill(cx[322], cy[11], cx[323], cy[12], 0xFF3D3E3D);

        p.fill(cx[4], cy[12], cx[5], cy[13], 0xFF797D7C);

        p.fill(cx[8], cy[12], cx[9], cy[13], 0xFF676763);

        p.fill(cx[54], cy[12], cx[55], cy[13], 0xFF42473F);

        p.fill(cx[70], cy[12], cx[71], cy[13], 0xFF2F3336);

        p.fill(cx[110], cy[12], cx[111], cy[13], 0xFF51504E);

        p.fill(cx[116], cy[12], cx[117], cy[13], 0xFF2F3336);

        p.fill(cx[152], cy[12], cx[153], cy[13], 0xFF3C3F3B);

        p.fill(cx[155], cy[12], cx[156], cy[13], 0xFF42473F);

        p.fill(cx[161], cy[12], cx[162], cy[13], 0xFF51504E);

        p.fill(cx[202], cy[12], cx[203], cy[13], 0xFF3C3F3B);

        p.fill(cx[211], cy[12], cx[212], cy[13], 0xFF42473F);

        p.fill(cx[212], cy[12], cx[213], cy[13], 0xFF2F3336);
        p.fill(cx[243], cy[12], cx[244], cy[13], 0xFF2F3336);

        p.fill(cx[249], cy[12], cx[250], cy[13], 0xFF51504E);

        p.fill(cx[252], cy[12], cx[253], cy[13], 0xFF3C3F3B);

        p.fill(cx[261], cy[12], cx[262], cy[13], 0xFF51504E);
        p.fill(cx[272], cy[12], cx[273], cy[13], 0xFF51504E);

        p.fill(cx[308], cy[12], cx[309], cy[13], 0xFF2F3336);

        p.fill(cx[328], cy[12], cx[329], cy[13], 0xFF42473F);

        p.fill(cx[329], cy[12], cx[330], cy[13], 0xFF3D3E3D);

        p.fill(cx[351], cy[12], cx[352], cy[13], 0xFF565B54);

        p.fill(cx[352], cy[12], cx[353], cy[13], 0xFFA5A49E);

        p.fill(cx[355], cy[12], cx[356], cy[13], 0xFF8A8C83);

        p.fill(cx[22], cy[13], cx[23], cy[14], 0xFF3C3F3B);

        p.fill(cx[62], cy[13], cx[63], cy[14], 0xFF42473F);

        p.fill(cx[63], cy[13], cx[64], cy[14], 0xFF4A4B47);
        p.fill(cx[95], cy[13], cx[96], cy[14], 0xFF4A4B47);

        p.fill(cx[110], cy[13], cx[111], cy[14], 0xFF42473F);

        p.fill(cx[134], cy[13], cx[135], cy[14], 0xFF202423);

        p.fill(cx[155], cy[13], cx[156], cy[14], 0xFF2F3336);

        p.fill(cx[163], cy[13], cx[164], cy[14], 0xFF51504E);

        p.fill(cx[179], cy[13], cx[180], cy[14], 0xFF2F3336);

        p.fill(cx[243], cy[13], cx[244], cy[14], 0xFF51504E);

        p.fill(cx[252], cy[13], cx[253], cy[14], 0xFF2F3336);

        p.fill(cx[271], cy[13], cx[272], cy[14], 0xFF4A4B47);
        p.fill(cx[297], cy[13], cx[298], cy[14], 0xFF4A4B47);

        p.fill(cx[51], cy[14], cx[52], cy[15], 0xFF202423);

        p.fill(cx[61], cy[14], cx[62], cy[15], 0xFF4A4B47);

        p.fill(cx[65], cy[14], cx[66], cy[15], 0xFFA5A49E);

        p.fill(cx[66], cy[14], cx[67], cy[15], 0xFF51504E);

        p.fill(cx[140], cy[14], cx[141], cy[15], 0xFF42473F);

        p.fill(cx[179], cy[14], cx[180], cy[15], 0xFF202423);

        p.fill(cx[214], cy[14], cx[215], cy[15], 0xFF4A4B47);

        p.fill(cx[219], cy[14], cx[220], cy[15], 0xFF42473F);

        p.fill(cx[228], cy[14], cx[229], cy[15], 0xFF3C3F3B);

        p.fill(cx[247], cy[14], cx[248], cy[15], 0xFF4A4B47);

        p.fill(cx[264], cy[14], cx[265], cy[15], 0xFF51504E);

        p.fill(cx[265], cy[14], cx[266], cy[15], 0xFF42473F);

        p.fill(cx[266], cy[14], cx[267], cy[15], 0xFF4A4B47);

        p.fill(cx[271], cy[14], cx[272], cy[15], 0xFF51504E);

        p.fill(cx[307], cy[14], cx[308], cy[15], 0xFF42473F);

        p.fill(cx[316], cy[14], cx[317], cy[15], 0xFF3D3E3D);
        p.fill(cx[327], cy[14], cx[328], cy[15], 0xFF3D3E3D);

        p.fill(cx[329], cy[14], cx[330], cy[15], 0xFF3C3F3B);

        p.fill(cx[331], cy[14], cx[332], cy[15], 0xFF51504E);

        p.fill(cx[16], cy[15], cx[17], cy[16], 0xFF3C3F3B);

        p.fill(cx[28], cy[15], cx[29], cy[16], 0xFF676763);

        p.fill(cx[88], cy[15], cx[89], cy[16], 0xFF51504E);

        p.fill(cx[104], cy[15], cx[105], cy[16], 0xFF42473F);

        p.fill(cx[112], cy[15], cx[113], cy[16], 0xFF51504E);

        p.fill(cx[117], cy[15], cx[118], cy[16], 0xFF2F3336);

        p.fill(cx[118], cy[15], cx[119], cy[16], 0xFF42473F);

        p.fill(cx[119], cy[15], cx[120], cy[16], 0xFF4A4B47);

        p.fill(cx[120], cy[15], cx[121], cy[16], 0xFF42473F);

        p.fill(cx[121], cy[15], cx[122], cy[16], 0xFF51504E);

        p.fill(cx[140], cy[15], cx[141], cy[16], 0xFFFBFBFB);

        p.fill(cx[169], cy[15], cx[170], cy[16], 0xFF51504E);

        p.fill(cx[179], cy[15], cx[180], cy[16], 0xFF3C3F3B);

        p.fill(cx[182], cy[15], cx[183], cy[16], 0xFF2F3336);

        p.fill(cx[186], cy[15], cx[187], cy[16], 0xFF51504E);

        p.fill(cx[190], cy[15], cx[191], cy[16], 0xFFA5A49E);

        p.fill(cx[191], cy[15], cx[192], cy[16], 0xFF42473F);

        p.fill(cx[192], cy[15], cx[193], cy[16], 0xFF51504E);
        p.fill(cx[195], cy[15], cx[196], cy[16], 0xFF51504E);

        p.fill(cx[217], cy[15], cx[218], cy[16], 0xFFA5A49E);

        p.fill(cx[218], cy[15], cx[219], cy[16], 0xFF51504E);

        p.fill(cx[219], cy[15], cx[220], cy[16], 0xFFA5A49E);

        p.fill(cx[227], cy[15], cx[228], cy[16], 0xFF3C3F3B);

        p.fill(cx[242], cy[15], cx[243], cy[16], 0xFF4A4B47);

        p.fill(cx[245], cy[15], cx[246], cy[16], 0xFF51504E);
        p.fill(cx[247], cy[15], cx[248], cy[16], 0xFF51504E);
        p.fill(cx[266], cy[15], cx[267], cy[16], 0xFF51504E);

        p.fill(cx[271], cy[15], cx[272], cy[16], 0xFF4A4B47);

        p.fill(cx[292], cy[15], cx[293], cy[16], 0xFF42473F);

        p.fill(cx[295], cy[15], cx[296], cy[16], 0xFF565B54);

        p.fill(cx[296], cy[15], cx[297], cy[16], 0xFF51504E);

        p.fill(cx[307], cy[15], cx[308], cy[16], 0xFF3D3E3D);

        p.fill(cx[321], cy[15], cx[322], cy[16], 0xFF3C3F3B);

        p.fill(cx[331], cy[15], cx[332], cy[16], 0xFF676763);

        p.fill(cx[339], cy[15], cx[340], cy[16], 0xFF202423);

        p.fill(cx[356], cy[15], cx[357], cy[16], 0xFFD1D2C3);

        p.fill(cx[1], cy[16], cx[2], cy[17], 0xFF8A8C83);

        p.fill(cx[28], cy[16], cx[29], cy[17], 0xFF2F3336);

        p.fill(cx[64], cy[16], cx[65], cy[17], 0xFFFBFBFB);
        p.fill(cx[67], cy[16], cx[68], cy[17], 0xFFFBFBFB);

        p.fill(cx[68], cy[16], cx[69], cy[17], 0xFFD1D2C3);

        p.fill(cx[70], cy[16], cx[71], cy[17], 0xFF51504E);

        p.fill(cx[90], cy[16], cx[91], cy[17], 0xFFFBFBFB);

        p.fill(cx[114], cy[16], cx[115], cy[17], 0xFFD1D2C3);

        p.fill(cx[115], cy[16], cx[116], cy[17], 0xFFFBFBFB);

        p.fill(cx[117], cy[16], cx[118], cy[17], 0xFF3C3F3B);

        p.fill(cx[118], cy[16], cx[119], cy[17], 0xFFA5A49E);

        p.fill(cx[120], cy[16], cx[121], cy[17], 0xFF565B54);

        p.fill(cx[121], cy[16], cx[122], cy[17], 0xFF4A4B47);

        p.fill(cx[123], cy[16], cx[124], cy[17], 0xFF51504E);
        p.fill(cx[136], cy[16], cx[137], cy[17], 0xFF51504E);

        p.fill(cx[140], cy[16], cx[141], cy[17], 0xFF3C3F3B);
        p.fill(cx[143], cy[16], cx[144], cy[17], 0xFF3C3F3B);

        p.fill(cx[144], cy[16], cx[145], cy[17], 0xFFD1D2C3);

        p.fill(cx[152], cy[16], cx[153], cy[17], 0xFF3C3F3B);

        p.fill(cx[155], cy[16], cx[156], cy[17], 0xFF202423);

        p.fill(cx[165], cy[16], cx[166], cy[17], 0xFFFBFBFB);

        p.fill(cx[166], cy[16], cx[167], cy[17], 0xFF2F3336);

        p.fill(cx[167], cy[16], cx[168], cy[17], 0xFFFBFBFB);

        p.fill(cx[168], cy[16], cx[169], cy[17], 0xFF3C3F3B);

        p.fill(cx[169], cy[16], cx[170], cy[17], 0xFFFBFBFB);

        p.fill(cx[173], cy[16], cx[174], cy[17], 0xFF51504E);

        p.fill(cx[182], cy[16], cx[183], cy[17], 0xFF3C3F3B);

        p.fill(cx[215], cy[16], cx[216], cy[17], 0xFFFBFBFB);

        p.fill(cx[219], cy[16], cx[220], cy[17], 0xFFD1D2C3);
        p.fill(cx[244], cy[16], cx[245], cy[17], 0xFFD1D2C3);

        p.fill(cx[284], cy[16], cx[285], cy[17], 0xFF42473F);

        p.fill(cx[291], cy[16], cx[292], cy[17], 0xFFD1D2C3);

        p.fill(cx[292], cy[16], cx[293], cy[17], 0xFFFBFBFB);

        p.fill(cx[296], cy[16], cx[297], cy[17], 0xFFA5A49E);

        p.fill(cx[321], cy[16], cx[322], cy[17], 0xFF3D3E3D);

        p.fill(cx[330], cy[16], cx[331], cy[17], 0xFFA5A49E);

        p.fill(cx[331], cy[16], cx[332], cy[17], 0xFF2F3336);

        p.fill(cx[3], cy[17], cx[4], cy[18], 0xFF8A8C83);
        p.fill(cx[7], cy[17], cx[8], cy[18], 0xFF8A8C83);

        p.fill(cx[28], cy[17], cx[29], cy[18], 0xFF42473F);

        p.fill(cx[51], cy[17], cx[52], cy[18], 0xFF202423);

        p.fill(cx[90], cy[17], cx[91], cy[18], 0xFF42473F);

        p.fill(cx[94], cy[17], cx[95], cy[18], 0xFF51504E);

        p.fill(cx[114], cy[17], cx[115], cy[18], 0xFFECEEEF);

        p.fill(cx[115], cy[17], cx[116], cy[18], 0xFF3C3F3B);

        p.fill(cx[117], cy[17], cx[118], cy[18], 0xFFFBFBFB);

        p.fill(cx[118], cy[17], cx[119], cy[18], 0xFF51504E);

        p.fill(cx[120], cy[17], cx[121], cy[18], 0xFFFBFBFB);

        p.fill(cx[121], cy[17], cx[122], cy[18], 0xFF42473F);

        p.fill(cx[130], cy[17], cx[131], cy[18], 0xFF202423);

        p.fill(cx[140], cy[17], cx[141], cy[18], 0xFFFBFBFB);
        p.fill(cx[144], cy[17], cx[145], cy[18], 0xFFFBFBFB);

        p.fill(cx[155], cy[17], cx[156], cy[18], 0xFF42473F);
        p.fill(cx[165], cy[17], cx[166], cy[18], 0xFF42473F);

        p.fill(cx[166], cy[17], cx[167], cy[18], 0xFFFBFBFB);

        p.fill(cx[167], cy[17], cx[168], cy[18], 0xFFD1D2C3);

        p.fill(cx[169], cy[17], cx[170], cy[18], 0xFF565B54);

        p.fill(cx[170], cy[17], cx[171], cy[18], 0xFFFBFBFB);

        p.fill(cx[192], cy[17], cx[193], cy[18], 0xFF8A8C83);

        p.fill(cx[193], cy[17], cx[194], cy[18], 0xFFFBFBFB);

        p.fill(cx[194], cy[17], cx[195], cy[18], 0xFF51504E);
        p.fill(cx[215], cy[17], cx[216], cy[18], 0xFF51504E);

        p.fill(cx[216], cy[17], cx[217], cy[18], 0xFFFBFBFB);

        p.fill(cx[217], cy[17], cx[218], cy[18], 0xFF3C3F3B);

        p.fill(cx[219], cy[17], cx[220], cy[18], 0xFFFBFBFB);

        p.fill(cx[241], cy[17], cx[242], cy[18], 0xFF3C3F3B);

        p.fill(cx[244], cy[17], cx[245], cy[18], 0xFFECEEEF);

        p.fill(cx[264], cy[17], cx[265], cy[18], 0xFF51504E);

        p.fill(cx[266], cy[17], cx[267], cy[18], 0xFFFBFBFB);

        p.fill(cx[267], cy[17], cx[268], cy[18], 0xFFA5A49E);

        p.fill(cx[268], cy[17], cx[269], cy[18], 0xFFFBFBFB);
        p.fill(cx[270], cy[17], cx[271], cy[18], 0xFFFBFBFB);

        p.fill(cx[316], cy[17], cx[317], cy[18], 0xFF42473F);

        p.fill(cx[324], cy[17], cx[325], cy[18], 0xFF3C3F3B);

        p.fill(cx[325], cy[17], cx[326], cy[18], 0xFF3D3E3D);

        p.fill(cx[4], cy[18], cx[5], cy[19], 0xFF8A8C83);

        p.fill(cx[17], cy[18], cx[18], cy[19], 0xFF3C3F3B);

        p.fill(cx[27], cy[18], cx[28], cy[19], 0xFF676763);

        p.fill(cx[54], cy[18], cx[55], cy[19], 0xFF3C3F3B);

        p.fill(cx[56], cy[18], cx[57], cy[19], 0xFF42473F);
        p.fill(cx[64], cy[18], cx[65], cy[19], 0xFF42473F);

        p.fill(cx[65], cy[18], cx[66], cy[19], 0xFFA5A49E);

        p.fill(cx[66], cy[18], cx[67], cy[19], 0xFF51504E);

        p.fill(cx[73], cy[18], cx[74], cy[19], 0xFF202423);

        p.fill(cx[80], cy[18], cx[81], cy[19], 0xFF3C3F3B);

        p.fill(cx[89], cy[18], cx[90], cy[19], 0xFF42473F);

        p.fill(cx[103], cy[18], cx[104], cy[19], 0xFF3C3F3B);

        p.fill(cx[155], cy[18], cx[156], cy[19], 0xFF202423);

        p.fill(cx[190], cy[18], cx[191], cy[19], 0xFF51504E);

        p.fill(cx[208], cy[18], cx[209], cy[19], 0xFF2F3336);

        p.fill(cx[224], cy[18], cx[225], cy[19], 0xFF42473F);

        p.fill(cx[291], cy[18], cx[292], cy[19], 0xFF4A4B47);

        p.fill(cx[292], cy[18], cx[293], cy[19], 0xFF42473F);

        p.fill(cx[319], cy[18], cx[320], cy[19], 0xFF3D3E3D);

        p.fill(cx[323], cy[18], cx[324], cy[19], 0xFF42473F);

        p.fill(cx[324], cy[18], cx[325], cy[19], 0xFF3D3E3D);

        p.fill(cx[325], cy[18], cx[326], cy[19], 0xFF42473F);

        p.fill(cx[340], cy[18], cx[341], cy[19], 0xFF2F3336);

        p.fill(cx[11], cy[19], cx[12], cy[20], 0xFF202423);

        p.fill(cx[12], cy[19], cx[13], cy[20], 0xFF2F3336);

        p.fill(cx[61], cy[19], cx[62], cy[20], 0xFF51504E);
        p.fill(cx[120], cy[19], cx[121], cy[20], 0xFF51504E);

        p.fill(cx[282], cy[19], cx[283], cy[20], 0xFF3D3E3D);

        p.fill(cx[292], cy[19], cx[293], cy[20], 0xFF4A4B47);

        p.fill(cx[293], cy[19], cx[294], cy[20], 0xFF51504E);

        p.fill(cx[317], cy[19], cx[318], cy[20], 0xFF3D3E3D);

        p.fill(cx[334], cy[19], cx[335], cy[20], 0xFF61625E);

        p.fill(cx[349], cy[19], cx[350], cy[20], 0xFF3C3F3B);

        p.fill(cx[351], cy[19], cx[352], cy[20], 0xFF676763);

        p.fill(cx[19], cy[20], cx[20], cy[21], 0xFF3C3F3B);

        p.fill(cx[59], cy[20], cx[60], cy[21], 0xFF51504E);

        p.fill(cx[61], cy[20], cx[62], cy[21], 0xFF42473F);
        p.fill(cx[65], cy[20], cx[66], cy[21], 0xFF42473F);

        p.fill(cx[72], cy[20], cx[73], cy[21], 0xFF51504E);

        p.fill(cx[87], cy[20], cx[88], cy[21], 0xFF42473F);

        p.fill(cx[90], cy[20], cx[91], cy[21], 0xFF4A4B47);

        p.fill(cx[110], cy[20], cx[111], cy[21], 0xFF51504E);

        p.fill(cx[120], cy[20], cx[121], cy[21], 0xFF42473F);

        p.fill(cx[124], cy[20], cx[125], cy[21], 0xFF51504E);

        p.fill(cx[141], cy[20], cx[142], cy[21], 0xFF42473F);
        p.fill(cx[149], cy[20], cx[150], cy[21], 0xFF42473F);
        p.fill(cx[162], cy[20], cx[163], cy[21], 0xFF42473F);
        p.fill(cx[166], cy[20], cx[167], cy[21], 0xFF42473F);
        p.fill(cx[205], cy[20], cx[206], cy[21], 0xFF42473F);
        p.fill(cx[266], cy[20], cx[267], cy[21], 0xFF42473F);
        p.fill(cx[268], cy[20], cx[269], cy[21], 0xFF42473F);

        p.fill(cx[287], cy[20], cx[288], cy[21], 0xFF4A4B47);
        p.fill(cx[293], cy[20], cx[294], cy[21], 0xFF4A4B47);

        p.fill(cx[294], cy[20], cx[295], cy[21], 0xFF42473F);

        p.fill(cx[295], cy[20], cx[296], cy[21], 0xFF4A4B47);
    }

    private static void part8(Paint p, int[] cx, int[] cy) {

        p.fill(cx[323], cy[20], cx[324], cy[21], 0xFF3D3E3D);

        p.fill(cx[340], cy[20], cx[341], cy[21], 0xFF42473F);

        p.fill(cx[4], cy[21], cx[5], cy[22], 0xFF8A8C83);

        p.fill(cx[5], cy[21], cx[6], cy[22], 0xFF6D6A64);
        p.fill(cx[7], cy[21], cx[8], cy[22], 0xFF6D6A64);

        p.fill(cx[57], cy[21], cx[58], cy[22], 0xFF202423);
        p.fill(cx[59], cy[21], cx[60], cy[22], 0xFF202423);

        p.fill(cx[70], cy[21], cx[71], cy[22], 0xFF51504E);

        p.fill(cx[71], cy[21], cx[72], cy[22], 0xFF2F3336);

        p.fill(cx[72], cy[21], cx[73], cy[22], 0xFF202423);
        p.fill(cx[74], cy[21], cx[75], cy[22], 0xFF202423);

        p.fill(cx[83], cy[21], cx[84], cy[22], 0xFF3C3F3B);

        p.fill(cx[94], cy[21], cx[95], cy[22], 0xFF51504E);

        p.fill(cx[96], cy[21], cx[97], cy[22], 0xFF2F3336);

        p.fill(cx[97], cy[21], cx[98], cy[22], 0xFF51504E);

        p.fill(cx[110], cy[21], cx[111], cy[22], 0xFF202423);

        p.fill(cx[123], cy[21], cx[124], cy[22], 0xFF2F3336);

        p.fill(cx[124], cy[21], cx[125], cy[22], 0xFF202423);

        p.fill(cx[127], cy[21], cx[128], cy[22], 0xFF2F3336);

        p.fill(cx[135], cy[21], cx[136], cy[22], 0xFF202423);

        p.fill(cx[136], cy[21], cx[137], cy[22], 0xFF42473F);

        p.fill(cx[143], cy[21], cx[144], cy[22], 0xFF51504E);

        p.fill(cx[145], cy[21], cx[146], cy[22], 0xFF565B54);

        p.fill(cx[149], cy[21], cx[150], cy[22], 0xFF202423);
        p.fill(cx[155], cy[21], cx[156], cy[22], 0xFF202423);

        p.fill(cx[162], cy[21], cx[163], cy[22], 0xFF51504E);

        p.fill(cx[163], cy[21], cx[164], cy[22], 0xFF2F3336);

        p.fill(cx[164], cy[21], cx[165], cy[22], 0xFF51504E);

        p.fill(cx[172], cy[21], cx[173], cy[22], 0xFF2F3336);

        p.fill(cx[173], cy[21], cx[174], cy[22], 0xFF42473F);

        p.fill(cx[205], cy[21], cx[206], cy[22], 0xFF51504E);

        p.fill(cx[209], cy[21], cx[210], cy[22], 0xFF3C3F3B);
        p.fill(cx[255], cy[21], cx[256], cy[22], 0xFF3C3F3B);

        p.fill(cx[264], cy[21], cx[265], cy[22], 0xFF2F3336);

        p.fill(cx[298], cy[21], cx[299], cy[22], 0xFF42473F);

        p.fill(cx[299], cy[21], cx[300], cy[22], 0xFF51504E);

        p.fill(cx[300], cy[21], cx[301], cy[22], 0xFF202423);

        p.fill(cx[329], cy[21], cx[330], cy[22], 0xFF42473F);

        p.fill(cx[330], cy[21], cx[331], cy[22], 0xFF3C3F3B);

        p.fill(cx[331], cy[21], cx[332], cy[22], 0xFF3D3E3D);

        p.fill(cx[332], cy[21], cx[333], cy[22], 0xFF3C3F3B);

        p.fill(cx[335], cy[21], cx[336], cy[22], 0xFF3D3E3D);

        p.fill(cx[355], cy[21], cx[356], cy[22], 0xFF6D6A64);

        p.fill(cx[7], cy[22], cx[8], cy[23], 0xFF676763);

        p.fill(cx[13], cy[22], cx[14], cy[23], 0xFF42473F);

        p.fill(cx[58], cy[22], cx[59], cy[23], 0xFF202423);

        p.fill(cx[72], cy[22], cx[73], cy[23], 0xFF2F3336);

        p.fill(cx[73], cy[22], cx[74], cy[23], 0xFF202423);

        p.fill(cx[99], cy[22], cx[100], cy[23], 0xFF2F3336);

        p.fill(cx[104], cy[22], cx[105], cy[23], 0xFF202423);

        p.fill(cx[124], cy[22], cx[125], cy[23], 0xFF2F3336);

        p.fill(cx[129], cy[22], cx[130], cy[23], 0xFF202423);

        p.fill(cx[131], cy[22], cx[132], cy[23], 0xFF3C3F3B);

        p.fill(cx[175], cy[22], cx[176], cy[23], 0xFF2F3336);

        p.fill(cx[180], cy[22], cx[181], cy[23], 0xFF202423);

        p.fill(cx[200], cy[22], cx[201], cy[23], 0xFF2F3336);

        p.fill(cx[205], cy[22], cx[206], cy[23], 0xFF202423);
        p.fill(cx[230], cy[22], cx[231], cy[23], 0xFF202423);

        p.fill(cx[235], cy[22], cx[236], cy[23], 0xFF2F3336);
        p.fill(cx[250], cy[22], cx[251], cy[23], 0xFF2F3336);

        p.fill(cx[255], cy[22], cx[256], cy[23], 0xFF202423);

        p.fill(cx[259], cy[22], cx[260], cy[23], 0xFF3C3F3B);
        p.fill(cx[277], cy[22], cx[278], cy[23], 0xFF3C3F3B);

        p.fill(cx[300], cy[22], cx[301], cy[23], 0xFF2F3336);

        p.fill(cx[349], cy[22], cx[350], cy[23], 0xFF797D7C);

        p.fill(cx[43], cy[23], cx[44], cy[24], 0xFF676763);

        p.fill(cx[44], cy[23], cx[45], cy[24], 0xFF42473F);
        p.fill(cx[46], cy[23], cx[47], cy[24], 0xFF42473F);

        p.fill(cx[47], cy[23], cx[48], cy[24], 0xFF61625E);

        p.fill(cx[59], cy[23], cx[60], cy[24], 0xFF202423);
        p.fill(cx[80], cy[23], cx[81], cy[24], 0xFF202423);

        p.fill(cx[103], cy[23], cx[104], cy[24], 0xFF3C3F3B);

        p.fill(cx[104], cy[23], cx[105], cy[24], 0xFF2F3336);

        p.fill(cx[105], cy[23], cx[106], cy[24], 0xFF3C3F3B);
        p.fill(cx[109], cy[23], cx[110], cy[24], 0xFF3C3F3B);

        p.fill(cx[131], cy[23], cx[132], cy[24], 0xFF2F3336);

        p.fill(cx[151], cy[23], cx[152], cy[24], 0xFF3C3F3B);
        p.fill(cx[153], cy[23], cx[154], cy[24], 0xFF3C3F3B);

        p.fill(cx[154], cy[23], cx[155], cy[24], 0xFF2F3336);

        p.fill(cx[175], cy[23], cx[176], cy[24], 0xFF3C3F3B);

        p.fill(cx[180], cy[23], cx[181], cy[24], 0xFF2F3336);

        p.fill(cx[181], cy[23], cx[182], cy[24], 0xFF3C3F3B);
        p.fill(cx[184], cy[23], cx[185], cy[24], 0xFF3C3F3B);

        p.fill(cx[205], cy[23], cx[206], cy[24], 0xFF2F3336);

        p.fill(cx[209], cy[23], cx[210], cy[24], 0xFF3C3F3B);

        p.fill(cx[227], cy[23], cx[228], cy[24], 0xFF2F3336);
        p.fill(cx[259], cy[23], cx[260], cy[24], 0xFF2F3336);
        p.fill(cx[277], cy[23], cx[278], cy[24], 0xFF2F3336);

        p.fill(cx[279], cy[23], cx[280], cy[24], 0xFF202423);
        p.fill(cx[299], cy[23], cx[300], cy[24], 0xFF202423);

        p.fill(cx[313], cy[23], cx[314], cy[24], 0xFF42473F);

        p.fill(cx[315], cy[23], cx[316], cy[24], 0xFF51504E);

        p.fill(cx[339], cy[23], cx[340], cy[24], 0xFF3D3E3D);

        p.fill(cx[41], cy[24], cx[42], cy[25], 0xFF676763);

        p.fill(cx[42], cy[24], cx[43], cy[25], 0xFF6D6A64);

        p.fill(cx[80], cy[24], cx[81], cy[25], 0xFF3C3F3B);
        p.fill(cx[279], cy[24], cx[280], cy[25], 0xFF3C3F3B);

        p.fill(cx[317], cy[24], cx[318], cy[25], 0xFF42473F);

        p.fill(cx[328], cy[24], cx[329], cy[25], 0xFF3D3E3D);
        p.fill(cx[338], cy[24], cx[339], cy[25], 0xFF3D3E3D);

        p.fill(cx[339], cy[24], cx[340], cy[25], 0xFF3C3F3B);

        p.fill(cx[40], cy[25], cx[41], cy[26], 0xFF676763);

        p.fill(cx[81], cy[25], cx[82], cy[26], 0xFF3C3F3B);
        p.fill(cx[100], cy[25], cx[101], cy[26], 0xFF3C3F3B);
        p.fill(cx[145], cy[25], cx[146], cy[26], 0xFF3C3F3B);
        p.fill(cx[185], cy[25], cx[186], cy[26], 0xFF3C3F3B);
        p.fill(cx[299], cy[25], cx[300], cy[26], 0xFF3C3F3B);

        p.fill(cx[319], cy[25], cx[320], cy[26], 0xFF676763);

        p.fill(cx[338], cy[25], cx[339], cy[26], 0xFF42473F);

        p.fill(cx[339], cy[25], cx[340], cy[26], 0xFF3D3E3D);

        p.fill(cx[37], cy[26], cx[38], cy[27], 0xFF2F3336);

        p.fill(cx[38], cy[26], cx[39], cy[27], 0xFF676763);

        p.fill(cx[39], cy[26], cx[40], cy[27], 0xFF565B54);

        p.fill(cx[320], cy[26], cx[321], cy[27], 0xFF61625E);

        p.fill(cx[37], cy[27], cx[38], cy[28], 0xFF676763);

        p.fill(cx[48], cy[27], cx[49], cy[28], 0xFF3C3F3B);

        p.fill(cx[49], cy[27], cx[50], cy[28], 0xFF2F3336);

        p.fill(cx[91], cy[27], cx[92], cy[28], 0xFFD1D2C3);

        p.fill(cx[141], cy[27], cx[142], cy[28], 0xFF2F3336);

        p.fill(cx[142], cy[27], cx[143], cy[28], 0xFFD1D2C3);
        p.fill(cx[167], cy[27], cx[168], cy[28], 0xFFD1D2C3);
        p.fill(cx[192], cy[27], cx[193], cy[28], 0xFFD1D2C3);
        p.fill(cx[243], cy[27], cx[244], cy[28], 0xFFD1D2C3);
        p.fill(cx[268], cy[27], cx[269], cy[28], 0xFFD1D2C3);

        p.fill(cx[321], cy[27], cx[322], cy[28], 0xFF51504E);

        p.fill(cx[339], cy[27], cx[340], cy[28], 0xFF3D3E3D);

        p.fill(cx[350], cy[27], cx[351], cy[28], 0xFF797D7C);

        p.fill(cx[351], cy[27], cx[352], cy[28], 0xFF6D6A64);

        p.fill(cx[36], cy[28], cx[37], cy[29], 0xFF676763);

        p.fill(cx[41], cy[28], cx[42], cy[29], 0xFF3C3F3B);

        p.fill(cx[46], cy[28], cx[47], cy[29], 0xFF2F3336);

        p.fill(cx[91], cy[28], cx[92], cy[29], 0xFFA5A49E);
        p.fill(cx[117], cy[28], cx[118], cy[29], 0xFFA5A49E);
        p.fill(cx[142], cy[28], cx[143], cy[29], 0xFFA5A49E);
        p.fill(cx[167], cy[28], cx[168], cy[29], 0xFFA5A49E);
        p.fill(cx[192], cy[28], cx[193], cy[29], 0xFFA5A49E);
        p.fill(cx[243], cy[28], cx[244], cy[29], 0xFFA5A49E);
        p.fill(cx[268], cy[28], cx[269], cy[29], 0xFFA5A49E);

        p.fill(cx[316], cy[28], cx[317], cy[29], 0xFF2F3336);

        p.fill(cx[323], cy[28], cx[324], cy[29], 0xFF565B54);

        p.fill(cx[347], cy[28], cx[348], cy[29], 0xFF61625E);

        p.fill(cx[6], cy[29], cx[7], cy[30], 0xFF676763);

        p.fill(cx[7], cy[29], cx[8], cy[30], 0xFF565B54);

        p.fill(cx[34], cy[29], cx[35], cy[30], 0xFF676763);

        p.fill(cx[324], cy[29], cx[325], cy[30], 0xFF51504E);

        p.fill(cx[325], cy[29], cx[326], cy[30], 0xFF676763);

        p.fill(cx[33], cy[30], cx[34], cy[31], 0xFF61625E);

        p.fill(cx[326], cy[30], cx[327], cy[31], 0xFF51504E);

        p.fill(cx[327], cy[30], cx[328], cy[31], 0xFF3C3F3B);

        p.fill(cx[354], cy[30], cx[355], cy[31], 0xFF676763);

        p.fill(cx[30], cy[31], cx[31], cy[32], 0xFF3C3F3B);

        p.fill(cx[31], cy[31], cx[32], cy[32], 0xFF676763);

        p.fill(cx[38], cy[31], cx[39], cy[32], 0xFF3C3F3B);

        p.fill(cx[327], cy[31], cx[328], cy[32], 0xFF51504E);

        p.fill(cx[347], cy[31], cx[348], cy[32], 0xFF61625E);

        p.fill(cx[354], cy[31], cx[355], cy[32], 0xFF565B54);

        p.fill(cx[355], cy[31], cx[356], cy[32], 0xFF3C3F3B);

        p.fill(cx[30], cy[32], cx[31], cy[33], 0xFF2F3336);

        p.fill(cx[328], cy[32], cx[329], cy[33], 0xFF51504E);

        p.fill(cx[347], cy[32], cx[348], cy[33], 0xFF42473F);

        p.fill(cx[30], cy[33], cx[31], cy[34], 0xFF565B54);

        p.fill(cx[30], cy[34], cx[31], cy[35], 0xFF51504E);

        p.fill(cx[42], cy[34], cx[43], cy[35], 0xFF3C3F3B);
        p.fill(cx[317], cy[34], cx[318], cy[35], 0xFF3C3F3B);
        p.fill(cx[330], cy[34], cx[331], cy[35], 0xFF3C3F3B);

        p.fill(cx[12], cy[35], cx[13], cy[36], 0xFF2F3336);

        p.fill(cx[320], cy[35], cx[321], cy[36], 0xFF3C3F3B);

        p.fill(cx[347], cy[35], cx[348], cy[36], 0xFF42473F);

        p.fill(cx[348], cy[35], cx[349], cy[36], 0xFF6D6A64);

        p.fill(cx[351], cy[35], cx[352], cy[36], 0xFF797D7C);

        p.fill(cx[8], cy[36], cx[9], cy[37], 0xFF202423);

        p.fill(cx[9], cy[36], cx[10], cy[37], 0xFF2F3336);

        p.fill(cx[21], cy[36], cx[22], cy[37], 0xFF42473F);

        p.fill(cx[317], cy[36], cx[318], cy[37], 0xFF202423);

        p.fill(cx[332], cy[36], cx[333], cy[37], 0xFF2F3336);

        p.fill(cx[346], cy[37], cx[347], cy[38], 0xFF42473F);

        p.fill(cx[24], cy[38], cx[25], cy[39], 0xFF2F3336);
        p.fill(cx[31], cy[38], cx[32], cy[39], 0xFF2F3336);

        p.fill(cx[329], cy[38], cx[330], cy[39], 0xFF51504E);

        p.fill(cx[324], cy[39], cx[325], cy[40], 0xFF3C3F3B);

        p.fill(cx[332], cy[39], cx[333], cy[40], 0xFF2F3336);

        p.fill(cx[351], cy[39], cx[352], cy[40], 0xFF51504E);

        p.fill(cx[5], cy[40], cx[6], cy[41], 0xFF565B54);

        p.fill(cx[7], cy[40], cx[8], cy[41], 0xFF676763);

        p.fill(cx[8], cy[40], cx[9], cy[41], 0xFF42473F);

        p.fill(cx[30], cy[40], cx[31], cy[41], 0xFF2F3336);

        p.fill(cx[38], cy[40], cx[39], cy[41], 0xFF3C3F3B);

        p.fill(cx[331], cy[40], cx[332], cy[41], 0xFF2F3336);

        p.fill(cx[332], cy[40], cx[333], cy[41], 0xFF3C3F3B);

        p.fill(cx[346], cy[40], cx[347], cy[41], 0xFF42473F);

        p.fill(cx[352], cy[40], cx[353], cy[41], 0xFF51504E);

        p.fill(cx[354], cy[40], cx[355], cy[41], 0xFF3C3F3B);

        p.fill(cx[355], cy[40], cx[356], cy[41], 0xFF2F3336);

        p.fill(cx[5], cy[41], cx[6], cy[42], 0xFF51504E);

        p.fill(cx[7], cy[41], cx[8], cy[42], 0xFF42473F);

        p.fill(cx[34], cy[41], cx[35], cy[42], 0xFF3C3F3B);
        p.fill(cx[39], cy[41], cx[40], cy[42], 0xFF3C3F3B);
        p.fill(cx[318], cy[41], cx[319], cy[42], 0xFF3C3F3B);

        p.fill(cx[326], cy[41], cx[327], cy[42], 0xFF2F3336);

        p.fill(cx[352], cy[41], cx[353], cy[42], 0xFF42473F);

        p.fill(cx[354], cy[41], cx[355], cy[42], 0xFF2F3336);

        p.fill(cx[355], cy[41], cx[356], cy[42], 0xFF202423);

        p.fill(cx[7], cy[42], cx[8], cy[43], 0xFF676763);

        p.fill(cx[34], cy[42], cx[35], cy[43], 0xFF2F3336);

        p.fill(cx[325], cy[42], cx[326], cy[43], 0xFF3C3F3B);

        p.fill(cx[354], cy[42], cx[355], cy[43], 0xFF676763);
        p.fill(cx[9], cy[43], cx[10], cy[44], 0xFF676763);

        p.fill(cx[12], cy[43], cx[13], cy[44], 0xFF565B54);

        p.fill(cx[350], cy[43], cx[351], cy[44], 0xFF6D6A64);

        p.fill(cx[351], cy[43], cx[352], cy[44], 0xFF51504E);

        p.fill(cx[6], cy[44], cx[7], cy[45], 0xFF565B54);

        p.fill(cx[7], cy[44], cx[8], cy[45], 0xFF676763);

        p.fill(cx[9], cy[44], cx[10], cy[45], 0xFF3C3F3B);

        p.fill(cx[42], cy[44], cx[43], cy[45], 0xFF42473F);

        p.fill(cx[6], cy[45], cx[7], cy[46], 0xFF51504E);

        p.fill(cx[7], cy[45], cx[8], cy[46], 0xFF61625E);

        p.fill(cx[8], cy[45], cx[9], cy[46], 0xFF2F3336);

        p.fill(cx[34], cy[45], cx[35], cy[46], 0xFF51504E);

        p.fill(cx[35], cy[45], cx[36], cy[46], 0xFF3C3F3B);

        p.fill(cx[346], cy[45], cx[347], cy[46], 0xFF42473F);

        p.fill(cx[0], cy[46], cx[1], cy[47], 0xFF51504E);

        p.fill(cx[25], cy[46], cx[26], cy[47], 0xFF565B54);
        p.fill(cx[30], cy[46], cx[31], cy[47], 0xFF565B54);

        p.fill(cx[32], cy[46], cx[33], cy[47], 0xFF2F3336);
        p.fill(cx[35], cy[46], cx[36], cy[47], 0xFF2F3336);

        p.fill(cx[337], cy[46], cx[338], cy[47], 0xFF61625E);

        p.fill(cx[346], cy[46], cx[347], cy[47], 0xFF51504E);

        p.fill(cx[359], cy[46], cx[360], cy[47], 0xFF565B54);

        p.fill(cx[19], cy[47], cx[20], cy[48], 0xFF42473F);
        p.fill(cx[29], cy[47], cx[30], cy[48], 0xFF42473F);

        p.fill(cx[12], cy[48], cx[13], cy[49], 0xFF61625E);

        p.fill(cx[13], cy[48], cx[14], cy[49], 0xFF3C3F3B);

        p.fill(cx[29], cy[48], cx[30], cy[49], 0xFF51504E);

        p.fill(cx[336], cy[50], cx[337], cy[51], 0xFF3C3F3B);

        p.fill(cx[22], cy[51], cx[23], cy[52], 0xFF42473F);

        p.fill(cx[23], cy[51], cx[24], cy[52], 0xFFD1D2C3);

        p.fill(cx[35], cy[51], cx[36], cy[52], 0xFF2F3336);

        p.fill(cx[38], cy[51], cx[39], cy[52], 0xFF3C3F3B);

        p.fill(cx[39], cy[51], cx[40], cy[52], 0xFF2F3336);

        p.fill(cx[333], cy[51], cx[334], cy[52], 0xFF797D7C);

        p.fill(cx[23], cy[52], cx[24], cy[53], 0xFF42473F);

        p.fill(cx[317], cy[52], cx[318], cy[53], 0xFF3C3F3B);

        p.fill(cx[318], cy[52], cx[319], cy[53], 0xFF42473F);

        p.fill(cx[319], cy[52], cx[320], cy[53], 0xFF3C3F3B);

        p.fill(cx[322], cy[52], cx[323], cy[53], 0xFFECEEEF);

        p.fill(cx[333], cy[52], cx[334], cy[53], 0xFFFBFBFB);
        p.fill(cx[336], cy[52], cx[337], cy[53], 0xFFFBFBFB);

        p.fill(cx[8], cy[53], cx[9], cy[54], 0xFF565B54);

        p.fill(cx[19], cy[53], cx[20], cy[54], 0xFF51504E);

        p.fill(cx[25], cy[53], cx[26], cy[54], 0xFF3C3F3B);
        p.fill(cx[37], cy[53], cx[38], cy[54], 0xFF3C3F3B);

        p.fill(cx[317], cy[53], cx[318], cy[54], 0xFF2F3336);

        p.fill(cx[351], cy[53], cx[352], cy[54], 0xFF565B54);

        p.fill(cx[8], cy[54], cx[9], cy[55], 0xFF42473F);

        p.fill(cx[30], cy[54], cx[31], cy[55], 0xFF565B54);

        p.fill(cx[36], cy[54], cx[37], cy[55], 0xFF3C3F3B);
        p.fill(cx[38], cy[54], cx[39], cy[55], 0xFF3C3F3B);

        p.fill(cx[332], cy[54], cx[333], cy[55], 0xFF4A4B47);

        p.fill(cx[8], cy[55], cx[9], cy[56], 0xFF3C3F3B);

        p.fill(cx[19], cy[56], cx[20], cy[57], 0xFF51504E);

        p.fill(cx[39], cy[56], cx[40], cy[57], 0xFF3C3F3B);

        p.fill(cx[331], cy[56], cx[332], cy[57], 0xFF4A4B47);

        p.fill(cx[351], cy[56], cx[352], cy[57], 0xFF676763);

        p.fill(cx[18], cy[57], cx[19], cy[58], 0xFF42473F);

        p.fill(cx[19], cy[57], cx[20], cy[58], 0xFF565B54);
        p.fill(cx[23], cy[57], cx[24], cy[58], 0xFF565B54);

        p.fill(cx[34], cy[57], cx[35], cy[58], 0xFF202423);

        p.fill(cx[39], cy[57], cx[40], cy[58], 0xFF2F3336);

        p.fill(cx[330], cy[57], cx[331], cy[58], 0xFF565B54);

        p.fill(cx[351], cy[57], cx[352], cy[58], 0xFF6D6A64);

        p.fill(cx[34], cy[58], cx[35], cy[59], 0xFF42473F);

        p.fill(cx[34], cy[59], cx[35], cy[60], 0xFF51504E);

        p.fill(cx[42], cy[59], cx[43], cy[60], 0xFF3C3F3B);

        p.fill(cx[8], cy[60], cx[9], cy[61], 0xFF2F3336);
        p.fill(cx[17], cy[60], cx[18], cy[61], 0xFF2F3336);

        p.fill(cx[34], cy[60], cx[35], cy[61], 0xFF42473F);

        p.fill(cx[42], cy[60], cx[43], cy[61], 0xFF202423);

        p.fill(cx[328], cy[60], cx[329], cy[61], 0xFF2F3336);

        p.fill(cx[17], cy[61], cx[18], cy[62], 0xFF202423);
        p.fill(cx[31], cy[61], cx[32], cy[62], 0xFF202423);

        p.fill(cx[34], cy[61], cx[35], cy[62], 0xFF3C3F3B);

        p.fill(cx[342], cy[61], cx[343], cy[62], 0xFF202423);

        p.fill(cx[12], cy[62], cx[13], cy[63], 0xFF61625E);

        p.fill(cx[29], cy[62], cx[30], cy[63], 0xFF202423);

        p.fill(cx[29], cy[63], cx[30], cy[64], 0xFF2F3336);

        p.fill(cx[42], cy[63], cx[43], cy[64], 0xFF42473F);

        p.fill(cx[27], cy[64], cx[28], cy[65], 0xFF51504E);

        p.fill(cx[34], cy[64], cx[35], cy[65], 0xFF202423);

        p.fill(cx[327], cy[64], cx[328], cy[65], 0xFF2F3336);

        p.fill(cx[331], cy[64], cx[332], cy[65], 0xFF61625E);

        p.fill(cx[19], cy[65], cx[20], cy[66], 0xFF42473F);
        p.fill(cx[29], cy[65], cx[30], cy[66], 0xFF42473F);

        p.fill(cx[33], cy[65], cx[34], cy[66], 0xFF2F3336);

        p.fill(cx[35], cy[65], cx[36], cy[66], 0xFF3C3F3B);

        p.fill(cx[329], cy[65], cx[330], cy[66], 0xFF51504E);

        p.fill(cx[341], cy[65], cx[342], cy[66], 0xFF676763);

        p.fill(cx[8], cy[67], cx[9], cy[68], 0xFF3C3F3B);

        p.fill(cx[340], cy[67], cx[341], cy[68], 0xFF42473F);

        p.fill(cx[23], cy[69], cx[24], cy[70], 0xFF3C3F3B);
        p.fill(cx[42], cy[69], cx[43], cy[70], 0xFF3C3F3B);

        p.fill(cx[336], cy[69], cx[337], cy[70], 0xFFECEEEF);

        p.fill(cx[337], cy[69], cx[338], cy[70], 0xFF42473F);
        p.fill(cx[23], cy[70], cx[24], cy[71], 0xFF42473F);

        p.fill(cx[25], cy[70], cx[26], cy[71], 0xFFFBFBFB);

        p.fill(cx[26], cy[70], cx[27], cy[71], 0xFF565B54);

        p.fill(cx[37], cy[70], cx[38], cy[71], 0xFFECEEEF);

        p.fill(cx[38], cy[70], cx[39], cy[71], 0xFFFBFBFB);

        p.fill(cx[39], cy[70], cx[40], cy[71], 0xFFD1D2C3);

        p.fill(cx[320], cy[70], cx[321], cy[71], 0xFFECEEEF);

        p.fill(cx[321], cy[70], cx[322], cy[71], 0xFFFBFBFB);

        p.fill(cx[322], cy[70], cx[323], cy[71], 0xFFECEEEF);

        p.fill(cx[333], cy[70], cx[334], cy[71], 0xFFD1D2C3);

        p.fill(cx[336], cy[70], cx[337], cy[71], 0xFF2F3336);

        p.fill(cx[40], cy[71], cx[41], cy[72], 0xFF42473F);

        p.fill(cx[321], cy[71], cx[322], cy[72], 0xFF3C3F3B);

        p.fill(cx[335], cy[71], cx[336], cy[72], 0xFF42473F);

        p.fill(cx[336], cy[71], cx[337], cy[72], 0xFF51504E);

        p.fill(cx[350], cy[72], cx[351], cy[73], 0xFF676763);

        p.fill(cx[19], cy[73], cx[20], cy[74], 0xFF42473F);
        p.fill(cx[340], cy[73], cx[341], cy[74], 0xFF42473F);

        p.fill(cx[350], cy[73], cx[351], cy[74], 0xFF61625E);

        p.fill(cx[350], cy[74], cx[351], cy[75], 0xFF676763);

        p.fill(cx[353], cy[74], cx[354], cy[75], 0xFF61625E);

        p.fill(cx[21], cy[75], cx[22], cy[76], 0xFF51504E);
        p.fill(cx[330], cy[75], cx[331], cy[76], 0xFF51504E);
        p.fill(cx[335], cy[75], cx[336], cy[76], 0xFF51504E);

        p.fill(cx[338], cy[75], cx[339], cy[76], 0xFF565B54);
        p.fill(cx[353], cy[75], cx[354], cy[76], 0xFF565B54);

        p.fill(cx[17], cy[79], cx[18], cy[80], 0xFF202423);
        p.fill(cx[31], cy[79], cx[32], cy[80], 0xFF202423);
        p.fill(cx[327], cy[79], cx[328], cy[80], 0xFF202423);

        p.fill(cx[38], cy[80], cx[39], cy[81], 0xFF3C3F3B);

        p.fill(cx[39], cy[80], cx[40], cy[81], 0xFF2F3336);

        p.fill(cx[42], cy[80], cx[43], cy[81], 0xFF202423);

        p.fill(cx[351], cy[80], cx[352], cy[81], 0xFF2F3336);

        p.fill(cx[8], cy[81], cx[9], cy[82], 0xFF42473F);

        p.fill(cx[35], cy[81], cx[36], cy[82], 0xFF3C3F3B);
        p.fill(cx[39], cy[81], cx[40], cy[82], 0xFF3C3F3B);

        p.fill(cx[339], cy[81], cx[340], cy[82], 0xFF202423);

        p.fill(cx[352], cy[81], cx[353], cy[82], 0xFF61625E);

        p.fill(cx[8], cy[82], cx[9], cy[83], 0xFF6D6A64);

        p.fill(cx[18], cy[82], cx[19], cy[83], 0xFF565B54);

        p.fill(cx[24], cy[82], cx[25], cy[83], 0xFF61625E);

        p.fill(cx[25], cy[82], cx[26], cy[83], 0xFF565B54);

        p.fill(cx[29], cy[82], cx[30], cy[83], 0xFF676763);

        p.fill(cx[35], cy[82], cx[36], cy[83], 0xFF2F3336);
        p.fill(cx[327], cy[82], cx[328], cy[83], 0xFF2F3336);

        p.fill(cx[330], cy[82], cx[331], cy[83], 0xFF565B54);
        p.fill(cx[335], cy[82], cx[336], cy[83], 0xFF565B54);
        p.fill(cx[340], cy[82], cx[341], cy[83], 0xFF565B54);

        p.fill(cx[341], cy[82], cx[342], cy[83], 0xFF202423);

        p.fill(cx[342], cy[82], cx[343], cy[83], 0xFF2F3336);

        p.fill(cx[352], cy[82], cx[353], cy[83], 0xFF565B54);

        p.fill(cx[19], cy[83], cx[20], cy[84], 0xFF42473F);

        p.fill(cx[33], cy[83], cx[34], cy[84], 0xFF2F3336);

        p.fill(cx[35], cy[83], cx[36], cy[84], 0xFF3C3F3B);

        p.fill(cx[336], cy[83], cx[337], cy[84], 0xFF2F3336);

        p.fill(cx[329], cy[84], cx[330], cy[85], 0xFF42473F);
        p.fill(cx[331], cy[84], cx[332], cy[85], 0xFF42473F);

        p.fill(cx[8], cy[85], cx[9], cy[86], 0xFF2F3336);

        p.fill(cx[8], cy[86], cx[9], cy[87], 0xFF3C3F3B);

        p.fill(cx[346], cy[86], cx[347], cy[87], 0xFF2F3336);

        p.fill(cx[7], cy[87], cx[8], cy[88], 0xFF6D6A64);

        p.fill(cx[8], cy[87], cx[9], cy[88], 0xFF676763);
        p.fill(cx[22], cy[87], cx[23], cy[88], 0xFF676763);

        p.fill(cx[25], cy[87], cx[26], cy[88], 0xFFFBFBFB);

        p.fill(cx[334], cy[87], cx[335], cy[88], 0xFF42473F);

        p.fill(cx[335], cy[87], cx[336], cy[88], 0xFFFBFBFB);

        p.fill(cx[336], cy[87], cx[337], cy[88], 0xFF2F3336);

        p.fill(cx[337], cy[87], cx[338], cy[88], 0xFF4A4B47);

        p.fill(cx[351], cy[87], cx[352], cy[88], 0xFF61625E);

        p.fill(cx[8], cy[88], cx[9], cy[89], 0xFF202423);

        p.fill(cx[22], cy[88], cx[23], cy[89], 0xFFFBFBFB);

        p.fill(cx[25], cy[88], cx[26], cy[89], 0xFF51504E);

        p.fill(cx[42], cy[88], cx[43], cy[89], 0xFF42473F);

        p.fill(cx[335], cy[88], cx[336], cy[89], 0xFF3C3F3B);

        p.fill(cx[336], cy[88], cx[337], cy[89], 0xFFFBFBFB);

        p.fill(cx[338], cy[88], cx[339], cy[89], 0xFF4A4B47);

        p.fill(cx[341], cy[88], cx[342], cy[89], 0xFF51504E);

        p.fill(cx[25], cy[89], cx[26], cy[90], 0xFFFBFBFB);

        p.fill(cx[34], cy[89], cx[35], cy[90], 0xFF51504E);

        p.fill(cx[317], cy[89], cx[318], cy[90], 0xFF3C3F3B);

        p.fill(cx[340], cy[89], cx[341], cy[90], 0xFF42473F);

        p.fill(cx[341], cy[89], cx[342], cy[90], 0xFF61625E);

        p.fill(cx[7], cy[90], cx[8], cy[91], 0xFF565B54);

        p.fill(cx[34], cy[90], cx[35], cy[91], 0xFF202423);

        p.fill(cx[358], cy[90], cx[359], cy[91], 0xFF61625E);

        p.fill(cx[34], cy[91], cx[35], cy[92], 0xFF42473F);
        p.fill(cx[340], cy[91], cx[341], cy[92], 0xFF42473F);

        p.fill(cx[18], cy[93], cx[19], cy[94], 0xFF3C3F3B);

        p.fill(cx[23], cy[93], cx[24], cy[94], 0xFF2F3336);
        p.fill(cx[329], cy[93], cx[330], cy[94], 0xFF2F3336);

        p.fill(cx[330], cy[93], cx[331], cy[94], 0xFF51504E);

        p.fill(cx[341], cy[93], cx[342], cy[94], 0xFF202423);

        p.fill(cx[325], cy[95], cx[326], cy[96], 0xFF2F3336);
        p.fill(cx[17], cy[96], cx[18], cy[97], 0xFF2F3336);

        p.fill(cx[17], cy[97], cx[18], cy[98], 0xFF202423);
        p.fill(cx[30], cy[99], cx[31], cy[100], 0xFF202423);

        p.fill(cx[31], cy[99], cx[32], cy[100], 0xFF2F3336);

        p.fill(cx[340], cy[99], cx[341], cy[100], 0xFF202423);

        p.fill(cx[341], cy[99], cx[342], cy[100], 0xFF2F3336);

        p.fill(cx[34], cy[100], cx[35], cy[101], 0xFF202423);

        p.fill(cx[329], cy[100], cx[330], cy[101], 0xFF565B54);

        p.fill(cx[330], cy[100], cx[331], cy[101], 0xFF51504E);

        p.fill(cx[19], cy[101], cx[20], cy[102], 0xFF42473F);
        p.fill(cx[29], cy[101], cx[30], cy[102], 0xFF42473F);

        p.fill(cx[33], cy[101], cx[34], cy[102], 0xFF2F3336);

        p.fill(cx[329], cy[101], cx[330], cy[102], 0xFF42473F);

        p.fill(cx[330], cy[101], cx[331], cy[102], 0xFF2F3336);
        p.fill(cx[334], cy[101], cx[335], cy[102], 0xFF2F3336);

        p.fill(cx[8], cy[102], cx[9], cy[103], 0xFF202423);

        p.fill(cx[330], cy[102], cx[331], cy[103], 0xFF42473F);

        p.fill(cx[331], cy[102], cx[332], cy[103], 0xFF4A4B47);

        p.fill(cx[332], cy[102], cx[333], cy[103], 0xFF42473F);

        p.fill(cx[6], cy[103], cx[7], cy[104], 0xFF565B54);

        p.fill(cx[332], cy[103], cx[333], cy[104], 0xFF4A4B47);

        p.fill(cx[351], cy[103], cx[352], cy[104], 0xFF565B54);
        p.fill(cx[348], cy[104], cx[349], cy[105], 0xFF565B54);

        p.fill(cx[19], cy[105], cx[20], cy[106], 0xFF42473F);

        p.fill(cx[23], cy[105], cx[24], cy[106], 0xFF565B54);

        p.fill(cx[24], cy[105], cx[25], cy[106], 0xFF51504E);

        p.fill(cx[25], cy[105], cx[26], cy[106], 0xFFECEEEF);

        p.fill(cx[34], cy[105], cx[35], cy[106], 0xFF3D3E3D);

        p.fill(cx[321], cy[105], cx[322], cy[106], 0xFF51504E);

        p.fill(cx[322], cy[105], cx[323], cy[106], 0xFF2F3336);

        p.fill(cx[340], cy[105], cx[341], cy[106], 0xFF42473F);

        p.fill(cx[350], cy[105], cx[351], cy[106], 0xFF61625E);

        p.fill(cx[26], cy[106], cx[27], cy[107], 0xFF565B54);

        p.fill(cx[42], cy[106], cx[43], cy[107], 0xFF42473F);

        p.fill(cx[319], cy[106], cx[320], cy[107], 0xFF3C3F3B);

        p.fill(cx[322], cy[106], cx[323], cy[107], 0xFFECEEEF);
        p.fill(cx[333], cy[106], cx[334], cy[107], 0xFFECEEEF);

        p.fill(cx[335], cy[106], cx[336], cy[107], 0xFF2F3336);

        p.fill(cx[336], cy[106], cx[337], cy[107], 0xFFA5A49E);

        p.fill(cx[340], cy[106], cx[341], cy[107], 0xFF51504E);

        p.fill(cx[8], cy[107], cx[9], cy[108], 0xFF676763);

        p.fill(cx[9], cy[107], cx[10], cy[108], 0xFF51504E);
        p.fill(cx[29], cy[107], cx[30], cy[108], 0xFF51504E);

        p.fill(cx[42], cy[107], cx[43], cy[108], 0xFF202423);

        p.fill(cx[332], cy[107], cx[333], cy[108], 0xFF4A4B47);

        p.fill(cx[333], cy[107], cx[334], cy[108], 0xFF51504E);

        p.fill(cx[7], cy[108], cx[8], cy[109], 0xFF676763);

        p.fill(cx[42], cy[108], cx[43], cy[109], 0xFF42473F);

        p.fill(cx[335], cy[109], cx[336], cy[110], 0xFF4A4B47);

        p.fill(cx[332], cy[110], cx[333], cy[111], 0xFF51504E);

        p.fill(cx[333], cy[110], cx[334], cy[111], 0xFF42473F);
        p.fill(cx[335], cy[110], cx[336], cy[111], 0xFF42473F);

        p.fill(cx[351], cy[110], cx[352], cy[111], 0xFF676763);

        p.fill(cx[24], cy[111], cx[25], cy[112], 0xFF51504E);

        p.fill(cx[25], cy[111], cx[26], cy[112], 0xFF565B54);

        p.fill(cx[26], cy[111], cx[27], cy[112], 0xFF42473F);

        p.fill(cx[27], cy[111], cx[28], cy[112], 0xFF676763);

        p.fill(cx[28], cy[111], cx[29], cy[112], 0xFF51504E);

        p.fill(cx[329], cy[111], cx[330], cy[112], 0xFF42473F);

        p.fill(cx[338], cy[111], cx[339], cy[112], 0xFF51504E);

        p.fill(cx[339], cy[111], cx[340], cy[112], 0xFF565B54);
        p.fill(cx[351], cy[111], cx[352], cy[112], 0xFF565B54);

        p.fill(cx[358], cy[111], cx[359], cy[112], 0xFF61625E);
        p.fill(cx[351], cy[112], cx[352], cy[113], 0xFF61625E);

        p.fill(cx[34], cy[113], cx[35], cy[114], 0xFF202423);

        p.fill(cx[351], cy[113], cx[352], cy[114], 0xFF565B54);

        p.fill(cx[359], cy[113], cx[360], cy[114], 0xFF61625E);

        p.fill(cx[8], cy[114], cx[9], cy[115], 0xFF202423);

        p.fill(cx[10], cy[114], cx[11], cy[115], 0xFF51504E);

        p.fill(cx[329], cy[114], cx[330], cy[115], 0xFF2F3336);

        p.fill(cx[330], cy[114], cx[331], cy[115], 0xFF3C3F3B);

        p.fill(cx[327], cy[115], cx[328], cy[116], 0xFF202423);

        p.fill(cx[7], cy[116], cx[8], cy[117], 0xFF565B54);

        p.fill(cx[341], cy[117], cx[342], cy[118], 0xFF2F3336);

        p.fill(cx[18], cy[118], cx[19], cy[119], 0xFF565B54);

        p.fill(cx[30], cy[118], cx[31], cy[119], 0xFF2F3336);
        p.fill(cx[327], cy[118], cx[328], cy[119], 0xFF2F3336);
        p.fill(cx[329], cy[118], cx[330], cy[119], 0xFF2F3336);

        p.fill(cx[335], cy[118], cx[336], cy[119], 0xFF61625E);

        p.fill(cx[336], cy[118], cx[337], cy[119], 0xFF565B54);

        p.fill(cx[339], cy[118], cx[340], cy[119], 0xFF676763);

        p.fill(cx[340], cy[118], cx[341], cy[119], 0xFF51504E);

        p.fill(cx[341], cy[118], cx[342], cy[119], 0xFF42473F);

        p.fill(cx[27], cy[119], cx[28], cy[120], 0xFF3C3F3B);

        p.fill(cx[28], cy[119], cx[29], cy[120], 0xFF2F3336);

        p.fill(cx[29], cy[119], cx[30], cy[120], 0xFF42473F);
        p.fill(cx[330], cy[119], cx[331], cy[120], 0xFF42473F);

        p.fill(cx[7], cy[121], cx[8], cy[122], 0xFF565B54);

        p.fill(cx[8], cy[121], cx[9], cy[122], 0xFF202423);

        p.fill(cx[341], cy[121], cx[342], cy[122], 0xFF51504E);

        p.fill(cx[7], cy[122], cx[8], cy[123], 0xFF676763);

        p.fill(cx[8], cy[122], cx[9], cy[123], 0xFF565B54);
        p.fill(cx[329], cy[122], cx[330], cy[123], 0xFF565B54);

        p.fill(cx[334], cy[122], cx[335], cy[123], 0xFF4A4B47);

        p.fill(cx[341], cy[122], cx[342], cy[123], 0xFF61625E);
        p.fill(cx[7], cy[123], cx[8], cy[124], 0xFF61625E);

        p.fill(cx[8], cy[123], cx[9], cy[124], 0xFF202423);

        p.fill(cx[23], cy[123], cx[24], cy[124], 0xFF42473F);

        p.fill(cx[25], cy[123], cx[26], cy[124], 0xFFD1D2C3);

        p.fill(cx[332], cy[123], cx[333], cy[124], 0xFF4A4B47);

        p.fill(cx[333], cy[123], cx[334], cy[124], 0xFFD1D2C3);

        p.fill(cx[334], cy[123], cx[335], cy[124], 0xFF42473F);

        p.fill(cx[335], cy[123], cx[336], cy[124], 0xFF8B9494);

        p.fill(cx[336], cy[123], cx[337], cy[124], 0xFF3C3F3B);

        p.fill(cx[337], cy[123], cx[338], cy[124], 0xFF797D7C);
    }

    private static void part9(Paint p, int[] cx, int[] cy) {

        p.fill(cx[8], cy[124], cx[9], cy[125], 0xFF676763);

        p.fill(cx[25], cy[124], cx[26], cy[125], 0xFF51504E);

        p.fill(cx[318], cy[124], cx[319], cy[125], 0xFF3C3F3B);

        p.fill(cx[333], cy[124], cx[334], cy[125], 0xFFA5A49E);

        p.fill(cx[335], cy[124], cx[336], cy[125], 0xFF8A8C83);

        p.fill(cx[336], cy[124], cx[337], cy[125], 0xFF2F3336);

        p.fill(cx[337], cy[124], cx[338], cy[125], 0xFFA5A49E);

        p.fill(cx[351], cy[124], cx[352], cy[125], 0xFF42473F);

        p.fill(cx[8], cy[125], cx[9], cy[126], 0xFF2F3336);

        p.fill(cx[25], cy[125], cx[26], cy[126], 0xFFFBFBFB);

        p.fill(cx[335], cy[125], cx[336], cy[126], 0xFF2F3336);

        p.fill(cx[336], cy[125], cx[337], cy[126], 0xFFD1D2C3);

        p.fill(cx[337], cy[125], cx[338], cy[126], 0xFF3C3F3B);

        p.fill(cx[346], cy[125], cx[347], cy[126], 0xFF51504E);

        p.fill(cx[351], cy[125], cx[352], cy[126], 0xFF565B54);

        p.fill(cx[333], cy[127], cx[334], cy[128], 0xFF4A4B47);

        p.fill(cx[19], cy[128], cx[20], cy[129], 0xFF51504E);

        p.fill(cx[333], cy[128], cx[334], cy[129], 0xFF42473F);

        p.fill(cx[352], cy[128], cx[353], cy[129], 0xFF61625E);

        p.fill(cx[30], cy[129], cx[31], cy[130], 0xFF2F3336);

        p.fill(cx[335], cy[129], cx[336], cy[130], 0xFF51504E);

        p.fill(cx[34], cy[130], cx[35], cy[131], 0xFF3C3F3B);

        p.fill(cx[7], cy[132], cx[8], cy[133], 0xFF61625E);

        p.fill(cx[17], cy[132], cx[18], cy[133], 0xFF2F3336);

        p.fill(cx[7], cy[133], cx[8], cy[134], 0xFF565B54);
        p.fill(cx[351], cy[133], cx[352], cy[134], 0xFF565B54);

        p.fill(cx[34], cy[134], cx[35], cy[135], 0xFF3C3F3B);

        p.fill(cx[351], cy[134], cx[352], cy[135], 0xFF676763);

        p.fill(cx[8], cy[135], cx[9], cy[136], 0xFF2F3336);

        p.fill(cx[12], cy[135], cx[13], cy[136], 0xFF565B54);

        p.fill(cx[318], cy[135], cx[319], cy[136], 0xFF3C3F3B);

        p.fill(cx[329], cy[135], cx[330], cy[136], 0xFF2F3336);

        p.fill(cx[351], cy[135], cx[352], cy[136], 0xFF42473F);

        p.fill(cx[8], cy[136], cx[9], cy[137], 0xFF202423);

        p.fill(cx[12], cy[136], cx[13], cy[137], 0xFF51504E);

        p.fill(cx[317], cy[136], cx[318], cy[137], 0xFF3C3F3B);

        p.fill(cx[335], cy[136], cx[336], cy[137], 0xFF4A4B47);

        p.fill(cx[12], cy[137], cx[13], cy[138], 0xFF565B54);

        p.fill(cx[33], cy[137], cx[34], cy[138], 0xFF2F3336);

        p.fill(cx[332], cy[137], cx[333], cy[138], 0xFF42473F);

        p.fill(cx[333], cy[137], cx[334], cy[138], 0xFF3C3F3B);

        p.fill(cx[341], cy[137], cx[342], cy[138], 0xFF61625E);

        p.fill(cx[346], cy[137], cx[347], cy[138], 0xFF3D3E3D);

        p.fill(cx[3], cy[138], cx[4], cy[139], 0xFF51504E);

        p.fill(cx[331], cy[138], cx[332], cy[139], 0xFF4A4B47);

        p.fill(cx[341], cy[138], cx[342], cy[139], 0xFF565B54);

        p.fill(cx[6], cy[140], cx[7], cy[141], 0xFF2F3336);

        p.fill(cx[7], cy[140], cx[8], cy[141], 0xFF676763);

        p.fill(cx[330], cy[140], cx[331], cy[141], 0xFF51504E);
        p.fill(cx[21], cy[141], cx[22], cy[142], 0xFF51504E);

        p.fill(cx[23], cy[141], cx[24], cy[142], 0xFF3C3F3B);

        p.fill(cx[24], cy[141], cx[25], cy[142], 0xFF4A4B47);

        p.fill(cx[25], cy[141], cx[26], cy[142], 0xFF8B9494);

        p.fill(cx[29], cy[141], cx[30], cy[142], 0xFF51504E);

        p.fill(cx[41], cy[141], cx[42], cy[142], 0xFF2F3336);

        p.fill(cx[332], cy[141], cx[333], cy[142], 0xFF51504E);

        p.fill(cx[337], cy[141], cx[338], cy[142], 0xFFFBFBFB);

        p.fill(cx[341], cy[141], cx[342], cy[142], 0xFF565B54);
        p.fill(cx[21], cy[142], cx[22], cy[143], 0xFF565B54);

        p.fill(cx[23], cy[142], cx[24], cy[143], 0xFF8A8C83);

        p.fill(cx[24], cy[142], cx[25], cy[143], 0xFFFBFBFB);

        p.fill(cx[25], cy[142], cx[26], cy[143], 0xFF565B54);

        p.fill(cx[39], cy[142], cx[40], cy[143], 0xFFD1D2C3);

        p.fill(cx[41], cy[142], cx[42], cy[143], 0xFF3C3F3B);
        p.fill(cx[317], cy[142], cx[318], cy[143], 0xFF3C3F3B);

        p.fill(cx[319], cy[142], cx[320], cy[143], 0xFF61625E);

        p.fill(cx[320], cy[142], cx[321], cy[143], 0xFFD1D2C3);

        p.fill(cx[332], cy[142], cx[333], cy[143], 0xFF2F3336);

        p.fill(cx[337], cy[142], cx[338], cy[143], 0xFF51504E);

        p.fill(cx[25], cy[143], cx[26], cy[144], 0xFF3C3F3B);

        p.fill(cx[34], cy[143], cx[35], cy[144], 0xFF202423);

        p.fill(cx[41], cy[143], cx[42], cy[144], 0xFF2F3336);
        p.fill(cx[335], cy[143], cx[336], cy[144], 0xFF2F3336);

        p.fill(cx[336], cy[143], cx[337], cy[144], 0xFFFBFBFB);

        p.fill(cx[337], cy[143], cx[338], cy[144], 0xFF2F3336);

        p.fill(cx[346], cy[143], cx[347], cy[144], 0xFF42473F);

        p.fill(cx[41], cy[144], cx[42], cy[145], 0xFF3C3F3B);

        p.fill(cx[340], cy[144], cx[341], cy[145], 0xFF51504E);

        p.fill(cx[41], cy[145], cx[42], cy[146], 0xFF2F3336);

        p.fill(cx[341], cy[145], cx[342], cy[146], 0xFF565B54);

        p.fill(cx[358], cy[145], cx[359], cy[146], 0xFF51504E);
        p.fill(cx[26], cy[146], cx[27], cy[147], 0xFF51504E);

        p.fill(cx[27], cy[146], cx[28], cy[147], 0xFF4A4B47);

        p.fill(cx[333], cy[146], cx[334], cy[147], 0xFF42473F);
        p.fill(cx[338], cy[146], cx[339], cy[147], 0xFF42473F);

        p.fill(cx[358], cy[146], cx[359], cy[147], 0xFF565B54);

        p.fill(cx[20], cy[147], cx[21], cy[148], 0xFF61625E);

        p.fill(cx[30], cy[147], cx[31], cy[148], 0xFF2F3336);

        p.fill(cx[329], cy[147], cx[330], cy[148], 0xFF42473F);

        p.fill(cx[332], cy[147], cx[333], cy[148], 0xFF51504E);

        p.fill(cx[333], cy[147], cx[334], cy[148], 0xFF565B54);

        p.fill(cx[334], cy[147], cx[335], cy[148], 0xFF51504E);
        p.fill(cx[338], cy[147], cx[339], cy[148], 0xFF51504E);

        p.fill(cx[339], cy[147], cx[340], cy[148], 0xFF565B54);

        p.fill(cx[340], cy[147], cx[341], cy[148], 0xFF51504E);
        p.fill(cx[350], cy[147], cx[351], cy[148], 0xFF51504E);

        p.fill(cx[34], cy[148], cx[35], cy[149], 0xFF202423);

        p.fill(cx[347], cy[148], cx[348], cy[149], 0xFF51504E);

        p.fill(cx[34], cy[149], cx[35], cy[150], 0xFF42473F);

        p.fill(cx[325], cy[149], cx[326], cy[150], 0xFF2F3336);

        p.fill(cx[326], cy[149], cx[327], cy[150], 0xFF202423);

        p.fill(cx[351], cy[149], cx[352], cy[150], 0xFF61625E);

        p.fill(cx[352], cy[149], cx[353], cy[150], 0xFF42473F);

        p.fill(cx[354], cy[149], cx[355], cy[150], 0xFF565B54);

        p.fill(cx[7], cy[150], cx[8], cy[151], 0xFF51504E);

        p.fill(cx[9], cy[150], cx[10], cy[151], 0xFF6D6A64);

        p.fill(cx[10], cy[150], cx[11], cy[151], 0xFF61625E);

        p.fill(cx[34], cy[150], cx[35], cy[151], 0xFF51504E);

        p.fill(cx[351], cy[150], cx[352], cy[151], 0xFF565B54);

        p.fill(cx[10], cy[151], cx[11], cy[152], 0xFF3C3F3B);

        p.fill(cx[11], cy[151], cx[12], cy[152], 0xFF61625E);

        p.fill(cx[347], cy[151], cx[348], cy[152], 0xFF51504E);

        p.fill(cx[348], cy[151], cx[349], cy[152], 0xFF42473F);

        p.fill(cx[349], cy[151], cx[350], cy[152], 0xFF797D7C);

        p.fill(cx[351], cy[151], cx[352], cy[152], 0xFF51504E);

        p.fill(cx[10], cy[152], cx[11], cy[153], 0xFF61625E);

        p.fill(cx[11], cy[152], cx[12], cy[153], 0xFF3C3F3B);

        p.fill(cx[12], cy[152], cx[13], cy[153], 0xFF565B54);

        p.fill(cx[33], cy[152], cx[34], cy[153], 0xFF202423);
        p.fill(cx[326], cy[152], cx[327], cy[153], 0xFF202423);

        p.fill(cx[346], cy[152], cx[347], cy[153], 0xFF2F3336);

        p.fill(cx[12], cy[153], cx[13], cy[154], 0xFF51504E);

        p.fill(cx[343], cy[153], cx[344], cy[154], 0xFF202423);

        p.fill(cx[346], cy[153], cx[347], cy[154], 0xFF3C3F3B);

        p.fill(cx[12], cy[154], cx[13], cy[155], 0xFF61625E);

        p.fill(cx[351], cy[154], cx[352], cy[155], 0xFF51504E);
        p.fill(cx[0], cy[155], cx[1], cy[156], 0xFF51504E);

        p.fill(cx[0], cy[156], cx[1], cy[157], 0xFF565B54);

        p.fill(cx[7], cy[156], cx[8], cy[157], 0xFF51504E);

        p.fill(cx[8], cy[156], cx[9], cy[157], 0xFF42473F);

        p.fill(cx[41], cy[156], cx[42], cy[157], 0xFF2F3336);

        p.fill(cx[7], cy[157], cx[8], cy[158], 0xFF565B54);

        p.fill(cx[351], cy[157], cx[352], cy[158], 0xFF51504E);

        p.fill(cx[355], cy[157], cx[356], cy[158], 0xFF42473F);

        p.fill(cx[4], cy[158], cx[5], cy[159], 0xFF3C3F3B);
        p.fill(cx[41], cy[158], cx[42], cy[159], 0xFF3C3F3B);

        p.fill(cx[42], cy[158], cx[43], cy[159], 0xFF2F3336);

        p.fill(cx[351], cy[158], cx[352], cy[159], 0xFF202423);

        p.fill(cx[3], cy[159], cx[4], cy[160], 0xFF42473F);

        p.fill(cx[10], cy[159], cx[11], cy[160], 0xFF2F3336);

        p.fill(cx[3], cy[160], cx[4], cy[161], 0xFF61625E);

        p.fill(cx[13], cy[160], cx[14], cy[161], 0xFF3C3F3B);

        p.fill(cx[14], cy[160], cx[15], cy[161], 0xFF2F3336);
        p.fill(cx[42], cy[160], cx[43], cy[161], 0xFF2F3336);

        p.fill(cx[317], cy[160], cx[318], cy[161], 0xFF3C3F3B);

        p.fill(cx[318], cy[160], cx[319], cy[161], 0xFF2F3336);

        p.fill(cx[3], cy[161], cx[4], cy[162], 0xFF565B54);

        p.fill(cx[4], cy[161], cx[5], cy[162], 0xFF3C3F3B);

        p.fill(cx[5], cy[161], cx[6], cy[162], 0xFF565B54);

        p.fill(cx[13], cy[161], cx[14], cy[162], 0xFF2F3336);

        p.fill(cx[30], cy[161], cx[31], cy[162], 0xFF42473F);

        p.fill(cx[42], cy[161], cx[43], cy[162], 0xFF3C3F3B);

        p.fill(cx[317], cy[161], cx[318], cy[162], 0xFF2F3336);

        p.fill(cx[358], cy[161], cx[359], cy[162], 0xFF4A4B47);

        p.fill(cx[5], cy[162], cx[6], cy[163], 0xFF676763);

        p.fill(cx[24], cy[162], cx[25], cy[163], 0xFF2F3336);

        p.fill(cx[42], cy[162], cx[43], cy[163], 0xFF202423);

        p.fill(cx[317], cy[162], cx[318], cy[163], 0xFF3C3F3B);

        p.fill(cx[342], cy[162], cx[343], cy[163], 0xFF2F3336);

        p.fill(cx[5], cy[163], cx[6], cy[164], 0xFF61625E);

        p.fill(cx[41], cy[163], cx[42], cy[164], 0xFF51504E);

        p.fill(cx[319], cy[163], cx[320], cy[164], 0xFF42473F);

        p.fill(cx[333], cy[163], cx[334], cy[164], 0xFF3C3F3B);
        p.fill(cx[342], cy[163], cx[343], cy[164], 0xFF3C3F3B);
        p.fill(cx[347], cy[163], cx[348], cy[164], 0xFF3C3F3B);

        p.fill(cx[3], cy[164], cx[4], cy[165], 0xFF3D3E3D);

        p.fill(cx[4], cy[164], cx[5], cy[165], 0xFF3C3F3B);

        p.fill(cx[5], cy[164], cx[6], cy[165], 0xFF51504E);
        p.fill(cx[39], cy[164], cx[40], cy[165], 0xFF51504E);
        p.fill(cx[320], cy[164], cx[321], cy[165], 0xFF51504E);

        p.fill(cx[342], cy[164], cx[343], cy[165], 0xFF2F3336);

        p.fill(cx[347], cy[164], cx[348], cy[165], 0xFF565B54);
        p.fill(cx[3], cy[165], cx[4], cy[166], 0xFF565B54);

        p.fill(cx[4], cy[165], cx[5], cy[166], 0xFF42473F);

        p.fill(cx[20], cy[165], cx[21], cy[166], 0xFF2F3336);

        p.fill(cx[24], cy[165], cx[25], cy[166], 0xFF3C3F3B);
        p.fill(cx[345], cy[165], cx[346], cy[166], 0xFF3C3F3B);

        p.fill(cx[356], cy[165], cx[357], cy[166], 0xFF61625E);

        p.fill(cx[3], cy[166], cx[4], cy[167], 0xFF51504E);

        p.fill(cx[4], cy[166], cx[5], cy[167], 0xFF3C3F3B);

        p.fill(cx[51], cy[166], cx[52], cy[167], 0xFF202423);

        p.fill(cx[90], cy[166], cx[91], cy[167], 0xFF4A4B47);

        p.fill(cx[131], cy[166], cx[132], cy[167], 0xFF51504E);
        p.fill(cx[133], cy[166], cx[134], cy[167], 0xFF51504E);
        p.fill(cx[149], cy[166], cx[150], cy[167], 0xFF51504E);
        p.fill(cx[219], cy[166], cx[220], cy[167], 0xFF51504E);

        p.fill(cx[246], cy[166], cx[247], cy[167], 0xFF4A4B47);

        p.fill(cx[247], cy[166], cx[248], cy[167], 0xFF51504E);

        p.fill(cx[316], cy[166], cx[317], cy[167], 0xFF61625E);
        p.fill(cx[318], cy[166], cx[319], cy[167], 0xFF61625E);

        p.fill(cx[319], cy[166], cx[320], cy[167], 0xFF3C3F3B);
        p.fill(cx[5], cy[167], cx[6], cy[168], 0xFF3C3F3B);
        p.fill(cx[41], cy[167], cx[42], cy[168], 0xFF3C3F3B);

        p.fill(cx[53], cy[167], cx[54], cy[168], 0xFF4A4B47);
        p.fill(cx[79], cy[167], cx[80], cy[168], 0xFF4A4B47);

        p.fill(cx[87], cy[167], cx[88], cy[168], 0xFF42473F);

        p.fill(cx[106], cy[167], cx[107], cy[168], 0xFF51504E);

        p.fill(cx[133], cy[167], cx[134], cy[168], 0xFF42473F);

        p.fill(cx[148], cy[167], cx[149], cy[168], 0xFF51504E);

        p.fill(cx[149], cy[167], cx[150], cy[168], 0xFF42473F);
        p.fill(cx[219], cy[167], cx[220], cy[168], 0xFF42473F);
        p.fill(cx[243], cy[167], cx[244], cy[168], 0xFF42473F);

        p.fill(cx[296], cy[167], cx[297], cy[168], 0xFF3D3E3D);

        p.fill(cx[297], cy[167], cx[298], cy[168], 0xFF42473F);

        p.fill(cx[316], cy[167], cx[317], cy[168], 0xFF565B54);

        p.fill(cx[318], cy[167], cx[319], cy[168], 0xFF3C3F3B);

        p.fill(cx[347], cy[167], cx[348], cy[168], 0xFF565B54);

        p.fill(cx[354], cy[167], cx[355], cy[168], 0xFF2F3336);

        p.fill(cx[5], cy[168], cx[6], cy[169], 0xFF61625E);
        p.fill(cx[43], cy[168], cx[44], cy[169], 0xFF61625E);

        p.fill(cx[53], cy[168], cx[54], cy[169], 0xFF51504E);
        p.fill(cx[60], cy[168], cx[61], cy[169], 0xFF51504E);

        p.fill(cx[61], cy[168], cx[62], cy[169], 0xFF565B54);

        p.fill(cx[62], cy[168], cx[63], cy[169], 0xFF51504E);

        p.fill(cx[63], cy[168], cx[64], cy[169], 0xFF3C3F3B);

        p.fill(cx[68], cy[168], cx[69], cy[169], 0xFF565B54);

        p.fill(cx[92], cy[168], cx[93], cy[169], 0xFF3C3F3B);

        p.fill(cx[101], cy[168], cx[102], cy[169], 0xFF565B54);

        p.fill(cx[102], cy[168], cx[103], cy[169], 0xFF42473F);

        p.fill(cx[115], cy[168], cx[116], cy[169], 0xFF51504E);

        p.fill(cx[117], cy[168], cx[118], cy[169], 0xFFECEEEF);

        p.fill(cx[118], cy[168], cx[119], cy[169], 0xFF42473F);

        p.fill(cx[123], cy[168], cx[124], cy[169], 0xFF51504E);

        p.fill(cx[126], cy[168], cx[127], cy[169], 0xFF42473F);

        p.fill(cx[127], cy[168], cx[128], cy[169], 0xFF3C3F3B);

        p.fill(cx[128], cy[168], cx[129], cy[169], 0xFF565B54);
        p.fill(cx[130], cy[168], cx[131], cy[169], 0xFF565B54);

        p.fill(cx[133], cy[168], cx[134], cy[169], 0xFF3C3F3B);

        p.fill(cx[134], cy[168], cx[135], cy[169], 0xFF51504E);

        p.fill(cx[142], cy[168], cx[143], cy[169], 0xFFFBFBFB);

        p.fill(cx[148], cy[168], cx[149], cy[169], 0xFF3C3F3B);

        p.fill(cx[149], cy[168], cx[150], cy[169], 0xFF51504E);

        p.fill(cx[154], cy[168], cx[155], cy[169], 0xFF42473F);
        p.fill(cx[156], cy[168], cx[157], cy[169], 0xFF42473F);
        p.fill(cx[164], cy[168], cx[165], cy[169], 0xFF42473F);

        p.fill(cx[168], cy[168], cx[169], cy[169], 0xFF3C3F3B);

        p.fill(cx[193], cy[168], cx[194], cy[169], 0xFFFBFBFB);

        p.fill(cx[226], cy[168], cx[227], cy[169], 0xFF42473F);
        p.fill(cx[235], cy[168], cx[236], cy[169], 0xFF42473F);

        p.fill(cx[241], cy[168], cx[242], cy[169], 0xFF3C3F3B);

        p.fill(cx[242], cy[168], cx[243], cy[169], 0xFFECEEEF);

        p.fill(cx[262], cy[168], cx[263], cy[169], 0xFF42473F);
        p.fill(cx[264], cy[168], cx[265], cy[169], 0xFF42473F);

        p.fill(cx[269], cy[168], cx[270], cy[169], 0xFF3C3F3B);
        p.fill(cx[276], cy[168], cx[277], cy[169], 0xFF3C3F3B);

        p.fill(cx[279], cy[168], cx[280], cy[169], 0xFF42473F);

        p.fill(cx[289], cy[168], cx[290], cy[169], 0xFF3C3F3B);

        p.fill(cx[310], cy[168], cx[311], cy[169], 0xFF4A4B47);

        p.fill(cx[315], cy[168], cx[316], cy[169], 0xFF565B54);

        p.fill(cx[316], cy[168], cx[317], cy[169], 0xFF3C3F3B);

        p.fill(cx[334], cy[168], cx[335], cy[169], 0xFF2F3336);

        p.fill(cx[347], cy[168], cx[348], cy[169], 0xFF676763);

        p.fill(cx[351], cy[168], cx[352], cy[169], 0xFF61625E);

        p.fill(cx[357], cy[168], cx[358], cy[169], 0xFF51504E);

        p.fill(cx[5], cy[169], cx[6], cy[170], 0xFF676763);

        p.fill(cx[28], cy[169], cx[29], cy[170], 0xFF2F3336);

        p.fill(cx[117], cy[169], cx[118], cy[170], 0xFF51504E);

        p.fill(cx[142], cy[169], cx[143], cy[170], 0xFFD1D2C3);

        p.fill(cx[193], cy[169], cx[194], cy[170], 0xFF202423);

        p.fill(cx[242], cy[169], cx[243], cy[170], 0xFFD1D2C3);

        p.fill(cx[243], cy[169], cx[244], cy[170], 0xFFECEEEF);

        p.fill(cx[332], cy[169], cx[333], cy[170], 0xFF3C3F3B);

        p.fill(cx[347], cy[169], cx[348], cy[170], 0xFF61625E);
        p.fill(cx[350], cy[169], cx[351], cy[170], 0xFF61625E);

        p.fill(cx[351], cy[169], cx[352], cy[170], 0xFF51504E);

        p.fill(cx[4], cy[170], cx[5], cy[171], 0xFF565B54);

        p.fill(cx[6], cy[170], cx[7], cy[171], 0xFF3C3F3B);

        p.fill(cx[7], cy[170], cx[8], cy[171], 0xFF2F3336);

        p.fill(cx[12], cy[170], cx[13], cy[171], 0xFF3C3F3B);
        p.fill(cx[28], cy[170], cx[29], cy[171], 0xFF3C3F3B);

        p.fill(cx[91], cy[170], cx[92], cy[171], 0xFFD1D2C3);

        p.fill(cx[142], cy[170], cx[143], cy[171], 0xFFECEEEF);

        p.fill(cx[242], cy[170], cx[243], cy[171], 0xFF202423);
        p.fill(cx[322], cy[170], cx[323], cy[171], 0xFF202423);

        p.fill(cx[345], cy[170], cx[346], cy[171], 0xFF2F3336);

        p.fill(cx[350], cy[170], cx[351], cy[171], 0xFF676763);

        p.fill(cx[7], cy[171], cx[8], cy[172], 0xFF61625E);

        p.fill(cx[321], cy[171], cx[322], cy[172], 0xFF202423);

        p.fill(cx[327], cy[171], cx[328], cy[172], 0xFF2F3336);

        p.fill(cx[355], cy[171], cx[356], cy[172], 0xFF565B54);

        p.fill(cx[7], cy[172], cx[8], cy[173], 0xFF676763);
        p.fill(cx[10], cy[172], cx[11], cy[173], 0xFF676763);

        p.fill(cx[30], cy[172], cx[31], cy[173], 0xFF3C3F3B);

        p.fill(cx[319], cy[172], cx[320], cy[173], 0xFF202423);

        p.fill(cx[340], cy[172], cx[341], cy[173], 0xFF2F3336);

        p.fill(cx[33], cy[173], cx[34], cy[174], 0xFF3C3F3B);

        p.fill(cx[322], cy[173], cx[323], cy[174], 0xFF2F3336);
        p.fill(cx[341], cy[173], cx[342], cy[174], 0xFF2F3336);

        p.fill(cx[351], cy[173], cx[352], cy[174], 0xFF676763);

        p.fill(cx[33], cy[174], cx[34], cy[175], 0xFF2F3336);

        p.fill(cx[42], cy[174], cx[43], cy[175], 0xFF202423);
        p.fill(cx[117], cy[174], cx[118], cy[175], 0xFF202423);
        p.fill(cx[149], cy[174], cx[150], cy[175], 0xFF202423);
        p.fill(cx[152], cy[174], cx[153], cy[175], 0xFF202423);
        p.fill(cx[164], cy[174], cx[165], cy[175], 0xFF202423);
        p.fill(cx[236], cy[174], cx[237], cy[175], 0xFF202423);
        p.fill(cx[239], cy[174], cx[240], cy[175], 0xFF202423);

        p.fill(cx[342], cy[174], cx[343], cy[175], 0xFF2F3336);
        p.fill(cx[29], cy[175], cx[30], cy[176], 0xFF2F3336);
        p.fill(cx[117], cy[175], cx[118], cy[176], 0xFF2F3336);
        p.fill(cx[149], cy[175], cx[150], cy[176], 0xFF2F3336);
        p.fill(cx[164], cy[175], cx[165], cy[176], 0xFF2F3336);
        p.fill(cx[236], cy[175], cx[237], cy[176], 0xFF2F3336);
        p.fill(cx[239], cy[175], cx[240], cy[176], 0xFF2F3336);

        p.fill(cx[3], cy[176], cx[4], cy[177], 0xFF676763);

        p.fill(cx[73], cy[176], cx[74], cy[177], 0xFF202423);

        p.fill(cx[84], cy[176], cx[85], cy[177], 0xFF2F3336);

        p.fill(cx[104], cy[176], cx[105], cy[177], 0xFF202423);

        p.fill(cx[124], cy[176], cx[125], cy[177], 0xFF2F3336);
        p.fill(cx[200], cy[176], cx[201], cy[177], 0xFF2F3336);

        p.fill(cx[230], cy[176], cx[231], cy[177], 0xFF202423);

        p.fill(cx[340], cy[176], cx[341], cy[177], 0xFF2F3336);
        p.fill(cx[345], cy[176], cx[346], cy[177], 0xFF2F3336);

        p.fill(cx[347], cy[176], cx[348], cy[177], 0xFF565B54);

        p.fill(cx[356], cy[176], cx[357], cy[177], 0xFF676763);
        p.fill(cx[5], cy[177], cx[6], cy[178], 0xFF676763);

        p.fill(cx[6], cy[177], cx[7], cy[178], 0xFF797D7C);

        p.fill(cx[7], cy[177], cx[8], cy[178], 0xFF3C3F3B);

        p.fill(cx[71], cy[177], cx[72], cy[178], 0xFF51504E);
        p.fill(cx[91], cy[177], cx[92], cy[178], 0xFF51504E);

        p.fill(cx[110], cy[177], cx[111], cy[178], 0xFF42473F);

        p.fill(cx[135], cy[177], cx[136], cy[178], 0xFF3C3F3B);

        p.fill(cx[136], cy[177], cx[137], cy[178], 0xFF51504E);

        p.fill(cx[137], cy[177], cx[138], cy[178], 0xFF61625E);

        p.fill(cx[143], cy[177], cx[144], cy[178], 0xFF51504E);

        p.fill(cx[149], cy[177], cx[150], cy[178], 0xFF202423);
        p.fill(cx[160], cy[177], cx[161], cy[178], 0xFF202423);
        p.fill(cx[181], cy[177], cx[182], cy[178], 0xFF202423);

        p.fill(cx[196], cy[177], cx[197], cy[178], 0xFF565B54);
        p.fill(cx[217], cy[177], cx[218], cy[178], 0xFF565B54);
        p.fill(cx[220], cy[177], cx[221], cy[178], 0xFF565B54);

        p.fill(cx[340], cy[177], cx[341], cy[178], 0xFF3C3F3B);
        p.fill(cx[345], cy[177], cx[346], cy[178], 0xFF3C3F3B);

        p.fill(cx[347], cy[177], cx[348], cy[178], 0xFF2F3336);

        p.fill(cx[354], cy[177], cx[355], cy[178], 0xFF676763);

        p.fill(cx[3], cy[178], cx[4], cy[179], 0xFF797D7C);

        p.fill(cx[4], cy[178], cx[5], cy[179], 0xFF8B9494);

        p.fill(cx[26], cy[178], cx[27], cy[179], 0xFF3C3F3B);

        p.fill(cx[42], cy[178], cx[43], cy[179], 0xFF2F3336);

        p.fill(cx[86], cy[178], cx[87], cy[179], 0xFF42473F);

        p.fill(cx[110], cy[178], cx[111], cy[179], 0xFF51504E);

        p.fill(cx[111], cy[178], cx[112], cy[179], 0xFF3C3F3B);

        p.fill(cx[205], cy[178], cx[206], cy[179], 0xFF2F3336);

        p.fill(cx[213], cy[178], cx[214], cy[179], 0xFF51504E);

        p.fill(cx[234], cy[178], cx[235], cy[179], 0xFF202423);

        p.fill(cx[249], cy[178], cx[250], cy[179], 0xFF51504E);

        p.fill(cx[274], cy[178], cx[275], cy[179], 0xFF565B54);

        p.fill(cx[317], cy[178], cx[318], cy[179], 0xFF3C3F3B);
        p.fill(cx[319], cy[178], cx[320], cy[179], 0xFF3C3F3B);

        p.fill(cx[351], cy[178], cx[352], cy[179], 0xFF51504E);

        p.fill(cx[7], cy[179], cx[8], cy[180], 0xFF565B54);

        p.fill(cx[26], cy[179], cx[27], cy[180], 0xFF2F3336);

        p.fill(cx[86], cy[179], cx[87], cy[180], 0xFF51504E);

        p.fill(cx[125], cy[179], cx[126], cy[180], 0xFF202423);

        p.fill(cx[136], cy[179], cx[137], cy[180], 0xFF4A4B47);
        p.fill(cx[138], cy[179], cx[139], cy[180], 0xFF4A4B47);

        p.fill(cx[162], cy[179], cx[163], cy[180], 0xFF42473F);

        p.fill(cx[167], cy[179], cx[168], cy[180], 0xFF4A4B47);

        p.fill(cx[171], cy[179], cx[172], cy[180], 0xFF51504E);
        p.fill(cx[189], cy[179], cx[190], cy[180], 0xFF51504E);
        p.fill(cx[196], cy[179], cx[197], cy[180], 0xFF51504E);

        p.fill(cx[205], cy[179], cx[206], cy[180], 0xFF3C3F3B);

        p.fill(cx[244], cy[179], cx[245], cy[180], 0xFF51504E);

        p.fill(cx[317], cy[179], cx[318], cy[180], 0xFF2F3336);

        p.fill(cx[322], cy[179], cx[323], cy[180], 0xFF3C3F3B);

        p.fill(cx[332], cy[179], cx[333], cy[180], 0xFF2F3336);

        p.fill(cx[351], cy[179], cx[352], cy[180], 0xFF202423);

        p.fill(cx[7], cy[180], cx[8], cy[181], 0xFFA5A49E);

        p.fill(cx[28], cy[180], cx[29], cy[181], 0xFF2F3336);

        p.fill(cx[56], cy[180], cx[57], cy[181], 0xFF3C3F3B);

        p.fill(cx[88], cy[180], cx[89], cy[181], 0xFF4A4B47);

        p.fill(cx[92], cy[180], cx[93], cy[181], 0xFF51504E);
        p.fill(cx[116], cy[180], cx[117], cy[181], 0xFF51504E);

        p.fill(cx[125], cy[180], cx[126], cy[181], 0xFF2F3336);

        p.fill(cx[144], cy[180], cx[145], cy[181], 0xFF4A4B47);

        p.fill(cx[162], cy[180], cx[163], cy[181], 0xFF51504E);
        p.fill(cx[165], cy[180], cx[166], cy[181], 0xFF51504E);

        p.fill(cx[171], cy[180], cx[172], cy[181], 0xFF42473F);

        p.fill(cx[214], cy[180], cx[215], cy[181], 0xFF4A4B47);
        p.fill(cx[219], cy[180], cx[220], cy[181], 0xFF4A4B47);

        p.fill(cx[244], cy[180], cx[245], cy[181], 0xFF42473F);

        p.fill(cx[322], cy[180], cx[323], cy[181], 0xFF2F3336);
        p.fill(cx[331], cy[180], cx[332], cy[181], 0xFF2F3336);
        p.fill(cx[33], cy[181], cx[34], cy[182], 0xFF2F3336);

        p.fill(cx[41], cy[181], cx[42], cy[182], 0xFF3C3F3B);

        p.fill(cx[56], cy[181], cx[57], cy[182], 0xFF2F3336);

        p.fill(cx[62], cy[181], cx[63], cy[182], 0xFFA5A49E);

        p.fill(cx[92], cy[181], cx[93], cy[182], 0xFF42473F);

        p.fill(cx[112], cy[181], cx[113], cy[182], 0xFF51504E);
        p.fill(cx[114], cy[181], cx[115], cy[182], 0xFF51504E);

        p.fill(cx[116], cy[181], cx[117], cy[182], 0xFF3C3F3B);

        p.fill(cx[117], cy[181], cx[118], cy[182], 0xFF42473F);

        p.fill(cx[125], cy[181], cx[126], cy[182], 0xFF202423);

        p.fill(cx[140], cy[181], cx[141], cy[182], 0xFFD1D2C3);

        p.fill(cx[143], cy[181], cx[144], cy[182], 0xFF3C3F3B);

        p.fill(cx[144], cy[181], cx[145], cy[182], 0xFF42473F);

        p.fill(cx[165], cy[181], cx[166], cy[182], 0xFFD1D2C3);

        p.fill(cx[166], cy[181], cx[167], cy[182], 0xFF42473F);

        p.fill(cx[194], cy[181], cx[195], cy[182], 0xFFFBFBFB);

        p.fill(cx[218], cy[181], cx[219], cy[182], 0xFF42473F);

        p.fill(cx[230], cy[181], cx[231], cy[182], 0xFF2F3336);

        p.fill(cx[239], cy[181], cx[240], cy[182], 0xFF51504E);

        p.fill(cx[242], cy[181], cx[243], cy[182], 0xFF4A4B47);

        p.fill(cx[243], cy[181], cx[244], cy[182], 0xFF42473F);

        p.fill(cx[244], cy[181], cx[245], cy[182], 0xFFFBFBFB);

        p.fill(cx[263], cy[181], cx[264], cy[182], 0xFF51504E);

        p.fill(cx[272], cy[181], cx[273], cy[182], 0xFF4A4B47);

        p.fill(cx[273], cy[181], cx[274], cy[182], 0xFF42473F);

        p.fill(cx[292], cy[181], cx[293], cy[182], 0xFFFBFBFB);
        p.fill(cx[295], cy[181], cx[296], cy[182], 0xFFFBFBFB);

        p.fill(cx[316], cy[181], cx[317], cy[182], 0xFF3C3F3B);

        p.fill(cx[330], cy[181], cx[331], cy[182], 0xFF2F3336);

        p.fill(cx[352], cy[181], cx[353], cy[182], 0xFFD1D2C3);
        p.fill(cx[6], cy[182], cx[7], cy[183], 0xFFD1D2C3);

        p.fill(cx[62], cy[182], cx[63], cy[183], 0xFF42473F);

        p.fill(cx[89], cy[182], cx[90], cy[183], 0xFFFBFBFB);

        p.fill(cx[90], cy[182], cx[91], cy[183], 0xFF42473F);

        p.fill(cx[92], cy[182], cx[93], cy[183], 0xFF2F3336);

        p.fill(cx[116], cy[182], cx[117], cy[183], 0xFF42473F);

        p.fill(cx[117], cy[182], cx[118], cy[183], 0xFF676763);

        p.fill(cx[118], cy[182], cx[119], cy[183], 0xFF565B54);

        p.fill(cx[123], cy[182], cx[124], cy[183], 0xFF42473F);

        p.fill(cx[141], cy[182], cx[142], cy[183], 0xFF2F3336);

        p.fill(cx[142], cy[182], cx[143], cy[183], 0xFFFBFBFB);

        p.fill(cx[143], cy[182], cx[144], cy[183], 0xFF2F3336);

        p.fill(cx[144], cy[182], cx[145], cy[183], 0xFFFBFBFB);

        p.fill(cx[166], cy[182], cx[167], cy[183], 0xFF51504E);

        p.fill(cx[168], cy[182], cx[169], cy[183], 0xFF676763);

        p.fill(cx[169], cy[182], cx[170], cy[183], 0xFFFBFBFB);

        p.fill(cx[191], cy[182], cx[192], cy[183], 0xFF42473F);

        p.fill(cx[201], cy[182], cx[202], cy[183], 0xFF2F3336);

        p.fill(cx[215], cy[182], cx[216], cy[183], 0xFFFBFBFB);

        p.fill(cx[216], cy[182], cx[217], cy[183], 0xFF2F3336);

        p.fill(cx[217], cy[182], cx[218], cy[183], 0xFFFBFBFB);
        p.fill(cx[240], cy[182], cx[241], cy[183], 0xFFFBFBFB);

        p.fill(cx[243], cy[182], cx[244], cy[183], 0xFFD1D2C3);

        p.fill(cx[244], cy[182], cx[245], cy[183], 0xFF2F3336);

        p.fill(cx[264], cy[182], cx[265], cy[183], 0xFF51504E);

        p.fill(cx[291], cy[182], cx[292], cy[183], 0xFFFBFBFB);

        p.fill(cx[330], cy[182], cx[331], cy[183], 0xFF565B54);

        p.fill(cx[352], cy[182], cx[353], cy[183], 0xFF61625E);

        p.fill(cx[353], cy[182], cx[354], cy[183], 0xFFD1D2C3);

        p.fill(cx[28], cy[183], cx[29], cy[184], 0xFF2F3336);
        p.fill(cx[32], cy[183], cx[33], cy[184], 0xFF2F3336);

        p.fill(cx[62], cy[183], cx[63], cy[184], 0xFF3C3F3B);

        p.fill(cx[65], cy[183], cx[66], cy[184], 0xFF42473F);

        p.fill(cx[89], cy[183], cx[90], cy[184], 0xFF3C3F3B);

        p.fill(cx[92], cy[183], cx[93], cy[184], 0xFFFBFBFB);

        p.fill(cx[116], cy[183], cx[117], cy[184], 0xFF51504E);

        p.fill(cx[117], cy[183], cx[118], cy[184], 0xFFFBFBFB);

        p.fill(cx[141], cy[183], cx[142], cy[184], 0xFF3C3F3B);

        p.fill(cx[142], cy[183], cx[143], cy[184], 0xFFD1D2C3);

        p.fill(cx[143], cy[183], cx[144], cy[184], 0xFF3C3F3B);

        p.fill(cx[144], cy[183], cx[145], cy[184], 0xFFD1D2C3);

        p.fill(cx[168], cy[183], cx[169], cy[184], 0xFFFBFBFB);

        p.fill(cx[169], cy[183], cx[170], cy[184], 0xFF3C3F3B);

        p.fill(cx[170], cy[183], cx[171], cy[184], 0xFFFBFBFB);

        p.fill(cx[171], cy[183], cx[172], cy[184], 0xFF42473F);

        p.fill(cx[191], cy[183], cx[192], cy[184], 0xFF3C3F3B);

        p.fill(cx[194], cy[183], cx[195], cy[184], 0xFFFBFBFB);

        p.fill(cx[215], cy[183], cx[216], cy[184], 0xFFD1D2C3);

        p.fill(cx[216], cy[183], cx[217], cy[184], 0xFF3C3F3B);

        p.fill(cx[219], cy[183], cx[220], cy[184], 0xFF2F3336);

        p.fill(cx[220], cy[183], cx[221], cy[184], 0xFFFBFBFB);

        p.fill(cx[236], cy[183], cx[237], cy[184], 0xFF51504E);

        p.fill(cx[240], cy[183], cx[241], cy[184], 0xFF676763);

        p.fill(cx[243], cy[183], cx[244], cy[184], 0xFF3C3F3B);

        p.fill(cx[244], cy[183], cx[245], cy[184], 0xFFD1D2C3);

        p.fill(cx[267], cy[183], cx[268], cy[184], 0xFFFBFBFB);

        p.fill(cx[268], cy[183], cx[269], cy[184], 0xFF3C3F3B);

        p.fill(cx[356], cy[183], cx[357], cy[184], 0xFF8A8C83);

        p.fill(cx[142], cy[184], cx[143], cy[185], 0xFF51504E);

        p.fill(cx[170], cy[184], cx[171], cy[185], 0xFF4A4B47);

        p.fill(cx[223], cy[184], cx[224], cy[185], 0xFF51504E);
        p.fill(cx[240], cy[184], cx[241], cy[185], 0xFF51504E);

        p.fill(cx[242], cy[184], cx[243], cy[185], 0xFF42473F);

        p.fill(cx[271], cy[184], cx[272], cy[185], 0xFF51504E);

        p.fill(cx[292], cy[184], cx[293], cy[185], 0xFFFBFBFB);
        p.fill(cx[295], cy[184], cx[296], cy[185], 0xFFFBFBFB);

        p.fill(cx[314], cy[184], cx[315], cy[185], 0xFF3C3F3B);

        p.fill(cx[323], cy[184], cx[324], cy[185], 0xFF2F3336);

        p.fill(cx[8], cy[185], cx[9], cy[186], 0xFF42473F);

        p.fill(cx[23], cy[185], cx[24], cy[186], 0xFF202423);

        p.fill(cx[43], cy[185], cx[44], cy[186], 0xFF3C3F3B);

        p.fill(cx[61], cy[185], cx[62], cy[186], 0xFF51504E);

        p.fill(cx[96], cy[185], cx[97], cy[186], 0xFF4A4B47);

        p.fill(cx[97], cy[185], cx[98], cy[186], 0xFF42473F);

        p.fill(cx[150], cy[185], cx[151], cy[186], 0xFF2F3336);

        p.fill(cx[242], cy[185], cx[243], cy[186], 0xFF4A4B47);

        p.fill(cx[267], cy[185], cx[268], cy[186], 0xFF51504E);

        p.fill(cx[273], cy[185], cx[274], cy[186], 0xFF42473F);

        p.fill(cx[8], cy[186], cx[9], cy[187], 0xFF3C3F3B);

        p.fill(cx[28], cy[186], cx[29], cy[187], 0xFF202423);

        p.fill(cx[61], cy[186], cx[62], cy[187], 0xFF42473F);

        p.fill(cx[62], cy[186], cx[63], cy[187], 0xFF4A4B47);

        p.fill(cx[65], cy[186], cx[66], cy[187], 0xFF51504E);
        p.fill(cx[69], cy[186], cx[70], cy[187], 0xFF51504E);

        p.fill(cx[70], cy[186], cx[71], cy[187], 0xFF42473F);

        p.fill(cx[105], cy[186], cx[106], cy[187], 0xFF2F3336);

        p.fill(cx[163], cy[186], cx[164], cy[187], 0xFF42473F);
    }

    private static void part10(Paint p, int[] cx, int[] cy) {

        p.fill(cx[172], cy[186], cx[173], cy[187], 0xFF42473F);

        p.fill(cx[211], cy[186], cx[212], cy[187], 0xFF4A4B47);

        p.fill(cx[234], cy[186], cx[235], cy[187], 0xFF202423);

        p.fill(cx[248], cy[186], cx[249], cy[187], 0xFF42473F);
        p.fill(cx[267], cy[186], cx[268], cy[187], 0xFF42473F);

        p.fill(cx[319], cy[186], cx[320], cy[187], 0xFF2F3336);

        p.fill(cx[336], cy[186], cx[337], cy[187], 0xFF202423);

        p.fill(cx[351], cy[186], cx[352], cy[187], 0xFF42473F);

        p.fill(cx[6], cy[187], cx[7], cy[188], 0xFF8B9494);

        p.fill(cx[8], cy[187], cx[9], cy[188], 0xFF51504E);

        p.fill(cx[25], cy[187], cx[26], cy[188], 0xFF202423);

        p.fill(cx[29], cy[187], cx[30], cy[188], 0xFF565B54);
        p.fill(cx[60], cy[187], cx[61], cy[188], 0xFF565B54);

        p.fill(cx[61], cy[187], cx[62], cy[188], 0xFF61625E);

        p.fill(cx[66], cy[187], cx[67], cy[188], 0xFF51504E);
        p.fill(cx[97], cy[187], cx[98], cy[188], 0xFF51504E);
        p.fill(cx[111], cy[187], cx[112], cy[188], 0xFF51504E);
        p.fill(cx[116], cy[187], cx[117], cy[188], 0xFF51504E);

        p.fill(cx[117], cy[187], cx[118], cy[188], 0xFF61625E);

        p.fill(cx[118], cy[187], cx[119], cy[188], 0xFF565B54);

        p.fill(cx[123], cy[187], cx[124], cy[188], 0xFF42473F);

        p.fill(cx[171], cy[187], cx[172], cy[188], 0xFF61625E);

        p.fill(cx[172], cy[187], cx[173], cy[188], 0xFF51504E);

        p.fill(cx[195], cy[187], cx[196], cy[188], 0xFF565B54);

        p.fill(cx[196], cy[187], cx[197], cy[188], 0xFF51504E);

        p.fill(cx[197], cy[187], cx[198], cy[188], 0xFF565B54);

        p.fill(cx[198], cy[187], cx[199], cy[188], 0xFF42473F);

        p.fill(cx[216], cy[187], cx[217], cy[188], 0xFF565B54);

        p.fill(cx[223], cy[187], cx[224], cy[188], 0xFF51504E);
        p.fill(cx[246], cy[187], cx[247], cy[188], 0xFF51504E);

        p.fill(cx[247], cy[187], cx[248], cy[188], 0xFF42473F);

        p.fill(cx[248], cy[187], cx[249], cy[188], 0xFF51504E);

        p.fill(cx[254], cy[187], cx[255], cy[188], 0xFF202423);

        p.fill(cx[273], cy[187], cx[274], cy[188], 0xFF42473F);

        p.fill(cx[324], cy[187], cx[325], cy[188], 0xFF2F3336);

        p.fill(cx[8], cy[188], cx[9], cy[189], 0xFF8B9494);

        p.fill(cx[10], cy[188], cx[11], cy[189], 0xFF3C3F3B);

        p.fill(cx[11], cy[188], cx[12], cy[189], 0xFF2F3336);

        p.fill(cx[41], cy[188], cx[42], cy[189], 0xFF3C3F3B);
        p.fill(cx[43], cy[188], cx[44], cy[189], 0xFF3C3F3B);

        p.fill(cx[57], cy[188], cx[58], cy[189], 0xFF202423);

        p.fill(cx[58], cy[188], cx[59], cy[189], 0xFF2F3336);

        p.fill(cx[94], cy[188], cx[95], cy[189], 0xFF42473F);

        p.fill(cx[173], cy[188], cx[174], cy[189], 0xFF3C3F3B);

        p.fill(cx[174], cy[188], cx[175], cy[189], 0xFF202423);
        p.fill(cx[199], cy[188], cx[200], cy[189], 0xFF202423);
        p.fill(cx[274], cy[188], cx[275], cy[189], 0xFF202423);

        p.fill(cx[301], cy[188], cx[302], cy[189], 0xFF2F3336);

        p.fill(cx[324], cy[188], cx[325], cy[189], 0xFF3C3F3B);

        p.fill(cx[351], cy[188], cx[352], cy[189], 0xFF8A8C83);

        p.fill(cx[10], cy[189], cx[11], cy[190], 0xFF2F3336);
        p.fill(cx[13], cy[189], cx[14], cy[190], 0xFF2F3336);

        p.fill(cx[58], cy[189], cx[59], cy[190], 0xFF202423);
        p.fill(cx[73], cy[189], cx[74], cy[190], 0xFF202423);
        p.fill(cx[88], cy[189], cx[89], cy[190], 0xFF202423);
        p.fill(cx[95], cy[189], cx[96], cy[190], 0xFF202423);
        p.fill(cx[114], cy[189], cx[115], cy[190], 0xFF202423);
        p.fill(cx[140], cy[189], cx[141], cy[190], 0xFF202423);
        p.fill(cx[171], cy[189], cx[172], cy[190], 0xFF202423);
        p.fill(cx[214], cy[189], cx[215], cy[190], 0xFF202423);
        p.fill(cx[286], cy[189], cx[287], cy[190], 0xFF202423);
        p.fill(cx[289], cy[189], cx[290], cy[190], 0xFF202423);
        p.fill(cx[301], cy[189], cx[302], cy[190], 0xFF202423);

        p.fill(cx[349], cy[189], cx[350], cy[190], 0xFF3C3F3B);

        p.fill(cx[10], cy[190], cx[11], cy[191], 0xFF676763);

        p.fill(cx[11], cy[190], cx[12], cy[191], 0xFF2F3336);

        p.fill(cx[323], cy[190], cx[324], cy[191], 0xFF3C3F3B);

        p.fill(cx[349], cy[190], cx[350], cy[191], 0xFF51504E);

        p.fill(cx[7], cy[191], cx[8], cy[192], 0xFF565B54);

        p.fill(cx[36], cy[191], cx[37], cy[192], 0xFF3C3F3B);
        p.fill(cx[41], cy[191], cx[42], cy[192], 0xFF3C3F3B);

        p.fill(cx[12], cy[192], cx[13], cy[193], 0xFF3D3E3D);

        p.fill(cx[13], cy[192], cx[14], cy[193], 0xFF2F3336);

        p.fill(cx[16], cy[192], cx[17], cy[193], 0xFF3C3F3B);

        p.fill(cx[28], cy[192], cx[29], cy[193], 0xFF51504E);

        p.fill(cx[100], cy[192], cx[101], cy[193], 0xFF2F3336);

        p.fill(cx[101], cy[192], cx[102], cy[193], 0xFF202423);
        p.fill(cx[131], cy[192], cx[132], cy[193], 0xFF202423);

        p.fill(cx[278], cy[192], cx[279], cy[193], 0xFF2F3336);

        p.fill(cx[331], cy[192], cx[332], cy[193], 0xFF42473F);

        p.fill(cx[345], cy[192], cx[346], cy[193], 0xFF2F3336);

        p.fill(cx[346], cy[192], cx[347], cy[193], 0xFF676763);

        p.fill(cx[89], cy[193], cx[90], cy[194], 0xFF3D3E3D);
        p.fill(cx[94], cy[193], cx[95], cy[194], 0xFF3D3E3D);

        p.fill(cx[95], cy[193], cx[96], cy[194], 0xFF3C3F3B);
        p.fill(cx[272], cy[193], cx[273], cy[194], 0xFF3C3F3B);
        p.fill(cx[285], cy[193], cx[286], cy[194], 0xFF3C3F3B);
        p.fill(cx[287], cy[193], cx[288], cy[194], 0xFF3C3F3B);
        p.fill(cx[51], cy[194], cx[52], cy[195], 0xFF3C3F3B);

        p.fill(cx[55], cy[194], cx[56], cy[195], 0xFF2F3336);

        p.fill(cx[56], cy[194], cx[57], cy[195], 0xFF3C3F3B);

        p.fill(cx[79], cy[194], cx[80], cy[195], 0xFF42473F);

        p.fill(cx[80], cy[194], cx[81], cy[195], 0xFF202423);

        p.fill(cx[89], cy[194], cx[90], cy[195], 0xFF42473F);

        p.fill(cx[225], cy[194], cx[226], cy[195], 0xFF4A4B47);

        p.fill(cx[276], cy[194], cx[277], cy[195], 0xFF3D3E3D);

        p.fill(cx[330], cy[194], cx[331], cy[195], 0xFF676763);

        p.fill(cx[356], cy[194], cx[357], cy[195], 0xFF565B54);

        p.fill(cx[20], cy[195], cx[21], cy[196], 0xFF2F3336);
        p.fill(cx[27], cy[195], cx[28], cy[196], 0xFF2F3336);
        p.fill(cx[51], cy[195], cx[52], cy[196], 0xFF2F3336);
        p.fill(cx[56], cy[195], cx[57], cy[196], 0xFF2F3336);
        p.fill(cx[58], cy[195], cx[59], cy[196], 0xFF2F3336);

        p.fill(cx[79], cy[195], cx[80], cy[196], 0xFF51504E);

        p.fill(cx[97], cy[195], cx[98], cy[196], 0xFF3D3E3D);
        p.fill(cx[159], cy[195], cx[160], cy[196], 0xFF3D3E3D);
        p.fill(cx[194], cy[195], cx[195], cy[196], 0xFF3D3E3D);

        p.fill(cx[279], cy[195], cx[280], cy[196], 0xFF42473F);

        p.fill(cx[291], cy[195], cx[292], cy[196], 0xFF3C3F3B);

        p.fill(cx[292], cy[195], cx[293], cy[196], 0xFF2F3336);

        p.fill(cx[293], cy[195], cx[294], cy[196], 0xFF42473F);

        p.fill(cx[319], cy[195], cx[320], cy[196], 0xFF2F3336);

        p.fill(cx[89], cy[196], cx[90], cy[197], 0xFF3D3E3D);
        p.fill(cx[109], cy[196], cx[110], cy[197], 0xFF3D3E3D);
        p.fill(cx[126], cy[196], cx[127], cy[197], 0xFF3D3E3D);

        p.fill(cx[127], cy[196], cx[128], cy[197], 0xFF3C3F3B);

        p.fill(cx[267], cy[196], cx[268], cy[197], 0xFF3D3E3D);
        p.fill(cx[281], cy[196], cx[282], cy[197], 0xFF3D3E3D);

        p.fill(cx[351], cy[196], cx[352], cy[197], 0xFF4A4B47);
        p.fill(cx[6], cy[197], cx[7], cy[198], 0xFF4A4B47);

        p.fill(cx[76], cy[197], cx[77], cy[198], 0xFF3D3E3D);

        p.fill(cx[107], cy[197], cx[108], cy[198], 0xFF3C3F3B);

        p.fill(cx[127], cy[197], cx[128], cy[198], 0xFF3D3E3D);

        p.fill(cx[272], cy[197], cx[273], cy[198], 0xFF3C3F3B);

        p.fill(cx[354], cy[197], cx[355], cy[198], 0xFF4A4B47);

        p.fill(cx[65], cy[198], cx[66], cy[199], 0xFF3D3E3D);
        p.fill(cx[291], cy[198], cx[292], cy[199], 0xFF3D3E3D);

        p.fill(cx[336], cy[198], cx[337], cy[199], 0xFF3C3F3B);

        p.fill(cx[359], cy[198], cx[360], cy[199], 0xFF51504E);

        p.fill(cx[24], cy[199], cx[25], cy[200], 0xFF3C3F3B);

        p.fill(cx[65], cy[199], cx[66], cy[200], 0xFF42473F);

        p.fill(cx[66], cy[199], cx[67], cy[200], 0xFF3D3E3D);
        p.fill(cx[83], cy[199], cx[84], cy[200], 0xFF3D3E3D);
        p.fill(cx[269], cy[199], cx[270], cy[200], 0xFF3D3E3D);
        p.fill(cx[273], cy[199], cx[274], cy[200], 0xFF3D3E3D);

        p.fill(cx[274], cy[199], cx[275], cy[200], 0xFF3C3F3B);

        p.fill(cx[275], cy[199], cx[276], cy[200], 0xFF3D3E3D);

        p.fill(cx[281], cy[199], cx[282], cy[200], 0xFF42473F);

        p.fill(cx[282], cy[199], cx[283], cy[200], 0xFF3D3E3D);
        p.fill(cx[288], cy[199], cx[289], cy[200], 0xFF3D3E3D);

        p.fill(cx[294], cy[199], cx[295], cy[200], 0xFF42473F);

        p.fill(cx[295], cy[199], cx[296], cy[200], 0xFF3D3E3D);

        p.fill(cx[315], cy[199], cx[316], cy[200], 0xFF3C3F3B);

        p.fill(cx[65], cy[200], cx[66], cy[201], 0xFF3D3E3D);

        p.fill(cx[88], cy[200], cx[89], cy[201], 0xFF42473F);

        p.fill(cx[265], cy[200], cx[266], cy[201], 0xFF3D3E3D);

        p.fill(cx[273], cy[200], cx[274], cy[201], 0xFF42473F);

        p.fill(cx[275], cy[200], cx[276], cy[201], 0xFF3C3F3B);
        p.fill(cx[277], cy[200], cx[278], cy[201], 0xFF3C3F3B);

        p.fill(cx[279], cy[200], cx[280], cy[201], 0xFF42473F);

        p.fill(cx[315], cy[200], cx[316], cy[201], 0xFF2F3336);
        p.fill(cx[320], cy[200], cx[321], cy[201], 0xFF2F3336);
        p.fill(cx[323], cy[200], cx[324], cy[201], 0xFF2F3336);

        p.fill(cx[349], cy[200], cx[350], cy[201], 0xFF3D3E3D);

        p.fill(cx[11], cy[201], cx[12], cy[202], 0xFF3C3F3B);

        p.fill(cx[49], cy[201], cx[50], cy[202], 0xFF2F3336);

        p.fill(cx[88], cy[201], cx[89], cy[202], 0xFF3C3F3B);
        p.fill(cx[96], cy[201], cx[97], cy[202], 0xFF3C3F3B);

        p.fill(cx[97], cy[201], cx[98], cy[202], 0xFF3D3E3D);
        p.fill(cx[110], cy[201], cx[111], cy[202], 0xFF3D3E3D);
        p.fill(cx[270], cy[201], cx[271], cy[202], 0xFF3D3E3D);

        p.fill(cx[271], cy[201], cx[272], cy[202], 0xFF42473F);

        p.fill(cx[273], cy[201], cx[274], cy[202], 0xFF3D3E3D);

        p.fill(cx[281], cy[201], cx[282], cy[202], 0xFF42473F);

        p.fill(cx[293], cy[201], cx[294], cy[202], 0xFF3C3F3B);
        p.fill(cx[311], cy[201], cx[312], cy[202], 0xFF3C3F3B);
        p.fill(cx[315], cy[201], cx[316], cy[202], 0xFF3C3F3B);
        p.fill(cx[320], cy[201], cx[321], cy[202], 0xFF3C3F3B);
        p.fill(cx[323], cy[201], cx[324], cy[202], 0xFF3C3F3B);

        p.fill(cx[337], cy[201], cx[338], cy[202], 0xFF2F3336);

        p.fill(cx[349], cy[201], cx[350], cy[202], 0xFF42473F);

        p.fill(cx[0], cy[202], cx[1], cy[203], 0xFF4A4B47);

        p.fill(cx[11], cy[202], cx[12], cy[203], 0xFF42473F);

        p.fill(cx[49], cy[202], cx[50], cy[203], 0xFF3C3F3B);

        p.fill(cx[51], cy[202], cx[52], cy[203], 0xFF2F3336);

        p.fill(cx[66], cy[202], cx[67], cy[203], 0xFF3C3F3B);

        p.fill(cx[88], cy[202], cx[89], cy[203], 0xFF42473F);
        p.fill(cx[110], cy[202], cx[111], cy[203], 0xFF42473F);

        p.fill(cx[136], cy[202], cx[137], cy[203], 0xFF3D3E3D);
        p.fill(cx[149], cy[202], cx[150], cy[203], 0xFF3D3E3D);

        p.fill(cx[150], cy[202], cx[151], cy[203], 0xFF42473F);

        p.fill(cx[237], cy[202], cx[238], cy[203], 0xFF3C3F3B);

        p.fill(cx[238], cy[202], cx[239], cy[203], 0xFF3D3E3D);

        p.fill(cx[281], cy[202], cx[282], cy[203], 0xFF3C3F3B);

        p.fill(cx[293], cy[202], cx[294], cy[203], 0xFF3D3E3D);

        p.fill(cx[294], cy[202], cx[295], cy[203], 0xFF3C3F3B);

        p.fill(cx[311], cy[202], cx[312], cy[203], 0xFF2F3336);
        p.fill(cx[315], cy[202], cx[316], cy[203], 0xFF2F3336);

        p.fill(cx[337], cy[202], cx[338], cy[203], 0xFF3C3F3B);
        p.fill(cx[349], cy[202], cx[350], cy[203], 0xFF3C3F3B);
    }
}
