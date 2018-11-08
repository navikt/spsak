package no.nav.foreldrepenger.domene.familiehendelse;

public class VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger {

    private Boolean erMorForSykVedFøsel;

    public VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger(Boolean erMorForSykVedFøsel) {
        this.erMorForSykVedFøsel = erMorForSykVedFøsel;
    }

    public Boolean getErMorForSykVedFøsel() {
        return erMorForSykVedFøsel;
    }
}
