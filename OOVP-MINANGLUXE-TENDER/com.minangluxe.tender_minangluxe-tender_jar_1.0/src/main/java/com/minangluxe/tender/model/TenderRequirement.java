package com.minangluxe.tender.model;

public class TenderRequirement {
    private final String paket; // S1/S2/S3/B
    private final Integer beratGram;
    private final String need;

    // Quantity handling
    private final Integer qtyTotal; // total quantity/item count
    private final Integer qtyHot;   // optional: for patterns like "2 hot 3" (interpreted as qtyTotal=2, qtyHot=3)

    // Budget handling
    private final Long budgetTotalRp;
    private final Long budgetPerItemRp;

    public TenderRequirement(String paket,
                              Integer beratGram,
                              String need,
                              Integer qtyTotal,
                              Integer qtyHot,
                              Long budgetTotalRp,
                              Long budgetPerItemRp) {
        this.paket = paket;
        this.beratGram = beratGram;
        this.need = need;
        this.qtyTotal = qtyTotal;
        this.qtyHot = qtyHot;
        this.budgetTotalRp = budgetTotalRp;
        this.budgetPerItemRp = budgetPerItemRp;
    }

    public String getPaket() { return paket; }
    public Integer getBeratGram() { return beratGram; }
    public String getNeed() { return need; }

    public Integer getQtyTotal() { return qtyTotal; }
    public Integer getQtyHot() { return qtyHot; }

    public Long getBudgetTotalRp() { return budgetTotalRp; }
    public Long getBudgetPerItemRp() { return budgetPerItemRp; }
}

