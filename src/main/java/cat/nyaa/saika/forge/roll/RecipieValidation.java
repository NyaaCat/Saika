package cat.nyaa.saika.forge.roll;

public enum RecipieValidation {
    VALID(0x11), INVALID_IRON(0x10), INVALID_ELEMENT(0x01), INVALID_BOTH(0x00), NO_ITEM(0x100);

    int state;

    RecipieValidation(int i) {
        state = i;
    }

    public static RecipieValidation ofState(int state) {
        state &= 0x11;
        switch (state) {
            case 0x00:
                return INVALID_BOTH;
            case 0x01:
                return INVALID_ELEMENT;
            case 0x10:
                return INVALID_IRON;
            case 0x11:
                return VALID;
            default:
                return INVALID_BOTH;
        }
    }
}
