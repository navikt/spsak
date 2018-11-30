package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BehandlingResultatType")
@DiscriminatorValue(BehandlingResultatType.DISCRIMINATOR)
public class BehandlingResultatType extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_RESULTAT_TYPE";

    public static final BehandlingResultatType IKKE_FASTSATT = new BehandlingResultatType("IKKE_FASTSATT"); //$NON-NLS-1$
    public static final BehandlingResultatType INNVILGET = new BehandlingResultatType("INNVILGET"); //$NON-NLS-1$
    public static final BehandlingResultatType AVSLÅTT = new BehandlingResultatType("AVSLÅTT"); //$NON-NLS-1$
    public static final BehandlingResultatType OPPHØR = new BehandlingResultatType("OPPHØR"); //$NON-NLS-1$
    public static final BehandlingResultatType HENLAGT_SØKNAD_TRUKKET = new BehandlingResultatType("HENLAGT_SØKNAD_TRUKKET"); //$NON-NLS-1$
    public static final BehandlingResultatType HENLAGT_FEILOPPRETTET = new BehandlingResultatType("HENLAGT_FEILOPPRETTET"); //$NON-NLS-1$
    public static final BehandlingResultatType HENLAGT_BRUKER_DØD = new BehandlingResultatType("HENLAGT_BRUKER_DØD"); //$NON-NLS-1$
    public static final BehandlingResultatType MERGET_OG_HENLAGT = new BehandlingResultatType("MERGET_OG_HENLAGT"); //$NON-NLS-1$
    public static final BehandlingResultatType HENLAGT_SØKNAD_MANGLER = new BehandlingResultatType("HENLAGT_SØKNAD_MANGLER"); //$NON-NLS-1$
    public static final BehandlingResultatType FORELDREPENGER_ENDRET = new BehandlingResultatType("FORELDREPENGER_ENDRET"); //$NON-NLS-1$
    public static final BehandlingResultatType INGEN_ENDRING = new BehandlingResultatType("INGEN_ENDRING"); //$NON-NLS-1$
    public static final BehandlingResultatType MANGLER_BEREGNINGSREGLER = new BehandlingResultatType("MANGLER_BEREGNINGSREGLER"); //$NON-NLS-1$

    private static final Set<BehandlingResultatType> HENLEGGELSESKODER_FOR_SØKNAD = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(HENLAGT_SØKNAD_TRUKKET, HENLAGT_FEILOPPRETTET, HENLAGT_BRUKER_DØD, MANGLER_BEREGNINGSREGLER)));
    private static final Set<BehandlingResultatType> ALLE_HENLEGGELSESKODER = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(HENLAGT_SØKNAD_TRUKKET, HENLAGT_FEILOPPRETTET, HENLAGT_BRUKER_DØD, MERGET_OG_HENLAGT, HENLAGT_SØKNAD_MANGLER, MANGLER_BEREGNINGSREGLER)));
    private static final Set<BehandlingResultatType> INNVILGET_KODER = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(INNVILGET, FORELDREPENGER_ENDRET)));

    protected BehandlingResultatType() {
        // Hibernate trenger denne
    }

    protected BehandlingResultatType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static Set<BehandlingResultatType> getAlleHenleggelseskoder() {
        return ALLE_HENLEGGELSESKODER;
    }

    public static Set<BehandlingResultatType> getHenleggelseskoderForSøknad() {
        return HENLEGGELSESKODER_FOR_SØKNAD;
    }

    public static Set<BehandlingResultatType> getInnvilgetKoder() {
        return INNVILGET_KODER;
    }

    public boolean erHenlagt() {
        return ALLE_HENLEGGELSESKODER.contains(this);
    }

}
