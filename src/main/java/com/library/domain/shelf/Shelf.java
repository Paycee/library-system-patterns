package com.library.domain.shelf;

/**
 * KOREN AGREGATA "Polica".
 *
 * Polica NE sadrži listu knjiga. Knjiga zna na kojoj je polici (shelfId),
 * ali iznajmljivanje knjige nema veze sa pravilima police.
 * Zato su to dva odvojena agregata, a ne polica-sa-knjigama.
 *
 * label je žanr ("KRIMI") ili slovo ("A"), zavisi kako je biblioteka sortirana.
 */
public class Shelf {
    private final long id;
    private final String label;

    public Shelf(long id, String label) {
        this.id = id;
        this.label = label;
    }

    public long getId()      { return id; }
    public String getLabel() { return label; }
}
