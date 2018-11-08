package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto;

import java.time.LocalDate;

public class VilkårResultat {

    private Long id;
    private LocalDate kjøreTidspunkt;
    private String vilkarType;
    private String utfall;
    private String avslagKode;
    private LocalDate opptjeningFom;
    private LocalDate opptjeningTom;
    private String opptjentTid;

    public VilkårResultat() {
    }

    public VilkårResultat(Long id, LocalDate kjøreTidspunkt, String vilkarType, String utfall, String avslagKode, LocalDate opptjeningFom, LocalDate opptjeningTom, String opptjentTid) {
        this.id = id;
        this.kjøreTidspunkt = kjøreTidspunkt;
        this.vilkarType = vilkarType;
        this.utfall = utfall;
        this.avslagKode = avslagKode;
        this.opptjeningFom = opptjeningFom;
        this.opptjeningTom = opptjeningTom;
        this.opptjentTid = opptjentTid;
    }

    public Long getId() {
        return id;
    }

    public String getVilkarType() {
        return vilkarType;
    }

    public String getUtfall() {
        return utfall;
    }

    public LocalDate getOpptjeningFom() {
        return opptjeningFom;
    }

    public LocalDate getOpptjeningTom() {
        return opptjeningTom;
    }

    public String getOpptjentTid() {
        return opptjentTid;
    }

    public String getAvslagKode() {
        return avslagKode;
    }

    public LocalDate getKjøreTidspunkt() {
        return kjøreTidspunkt;
    }

    @Override
    public String toString() {
        return "VilkårResultat{" +
            "id=" + id +
            ", kjøreTidspunkt=" + kjøreTidspunkt +
            ", vilkarType='" + vilkarType + '\'' +
            ", utfall='" + utfall + '\'' +
            ", avslagKode='" + avslagKode + '\'' +
            ", opptjeningFom=" + opptjeningFom +
            ", opptjeningTom=" + opptjeningTom +
            ", opptjentTid='" + opptjentTid + '\'' +
            '}';
    }
}
