package com.medilab.enums;

public enum TestUnit {
    MG_DL("mg/dL"),
    MMOL_L("mmol/L"),
    G_DL("g/dL"),
    IU_L("IU/L"),
    FL("fL"),
    PG("pg"),
    MM_HR("mm/hr"),
    NG_ML("ng/mL"),
    UG_DL("ug/dL"),
    PERCENTAGE("%"),
    CELLS_UL("cells/uL"),
    MILLION_UL("million/uL"),
    NONE("");

    private final String symbol;

    TestUnit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
