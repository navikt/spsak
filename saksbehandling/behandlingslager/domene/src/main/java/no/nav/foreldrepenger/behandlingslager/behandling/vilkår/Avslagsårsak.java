package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Avslagsårsak")
@DiscriminatorValue(Avslagsårsak.DISCRIMINATOR)
public class Avslagsårsak extends Kodeliste {

    public static final String DISCRIMINATOR = "AVSLAGSARSAK"; //$NON-NLS-1$

    public static final Avslagsårsak MANGLENDE_DOKUMENTASJON = new Avslagsårsak("1019"); //$NON-NLS-1$
    public static final Avslagsårsak SØKER_ER_IKKE_MEDLEM = new Avslagsårsak("1020"); //$NON-NLS-1$
    public static final Avslagsårsak SØKER_ER_UTVANDRET = new Avslagsårsak("1021"); //$NON-NLS-1$
    public static final Avslagsårsak SØKT_FOR_SENT = new Avslagsårsak("1007"); //$NON-NLS-1$
    public static final Avslagsårsak IKKE_TILSTREKKELIG_OPPTJENING = new Avslagsårsak("1035"); //$NON-NLS-1$
    public static final Avslagsårsak FOR_LAVT_BEREGNINGSGRUNNLAG = new Avslagsårsak("1041"); //$NON-NLS-1$
    public static final Avslagsårsak UDEFINERT = new Avslagsårsak("-"); //$NON-NLS-1$

    @Transient
    private String lovReferanse;

    Avslagsårsak() {
        // Hibernate trenger den
    }

    private Avslagsårsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public String getLovReferanse(FagsakYtelseType fagsakYtelseType) {
        if (getEkstraData() == null) {
            return null;
        }
        lovReferanse = LovhjemmelJsonHjelper.findLovhjemmelIJson(fagsakYtelseType, getEkstraData(), getKodeverk(), getKode());
        return lovReferanse;
    }
}
