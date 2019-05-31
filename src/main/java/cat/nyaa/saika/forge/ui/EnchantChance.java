package cat.nyaa.saika.forge.ui;

public class EnchantChance {
    private int success;
    private int half;
    private int fail;
    private int epicFail;

    public EnchantChance(int success, int half, int fail, int epicFail){
        this.success = success;
        this.half = half;
        this.fail = fail;
        this.epicFail = epicFail;
    }

    public int getSuccess() {
        return success;
    }

    public int getHalf() {
        return half;
    }

    public int getFail() {
        return fail;
    }

    public int getEpicFail() {
        return epicFail;
    }
}
