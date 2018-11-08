package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher;

public interface EditDistanceLetter {

    int kostnadSettInn();

    int kostnadSlette();

    int kostnadEndre(EditDistanceLetter annen);

    boolean lik(EditDistanceLetter annen);
}
