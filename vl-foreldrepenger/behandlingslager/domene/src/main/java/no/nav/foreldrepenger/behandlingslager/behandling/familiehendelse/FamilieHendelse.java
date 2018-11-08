package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;

/**
 * Samle betegnelse for Fødsel, Adopsjon, Omsorgsovertakelse og Terminbekreftelse.
 *
 * Fødsler ligger i listen med UidentifisertBarn
 * Barn som skal adopteres / overdra omsorgen for ligger i listen med UidentifisertBarn
 */
public interface FamilieHendelse {

    /**
     * Antall barn i hendelsen
     * @return antallbarn
     */
    Integer getAntallBarn();

    /**
     * Liste over Uidentifiserte barn, dvs barn uten fnr. Dette betyr ikke at de ikke har fnr men at de ikke er identifisert med det i Behandlingen
     * @return Liste over barn
     */
    List<UidentifisertBarn> getBarna();

    /**
     * Data vedrørende terminbekreftelsen som er relevant for behandlingen
     * @return terminbekreftelsen
     */
    Optional<Terminbekreftelse> getTerminbekreftelse();

    /**
     * Data vedrørende adopsjonsbekreftelsen / omsorgsovertakelse som er relevant for behandlingen.
     * @return adopsjon
     */
    Optional<Adopsjon> getAdopsjon();

    /**
     * Internt kodeverk som identifiserer hva søknaden er basert på. F.eks basert på føsel.
     * @return FamilieHendelseTypen
     */
    FamilieHendelseType getType();

    /**
     * Henter ut fødselsdatoen fra listen over UidentifiserteBarn hvis typen er Føsel
     * @return Fødselsdatoen
     */
    Optional<LocalDate> getFødselsdato();

    /**
     * Sjekker om det er født et barn med dødsdato på samme dag som det er født.
     * @return boolean
     */
    boolean getInnholderDøfødtBarn();

    /**
     * Sjekker om hendelsen omhandler et dødt barn.
     * @return boolean
     */
    boolean getInnholderDødtBarn();

    /**
     * Henter ut oppgitt / vurdert status om mor er for syk til å ta seg av barnet
     * @return boolean
     */
    Boolean erMorForSykVedFødsel();

    /**
     * Beregnet skjæringstidspunkt for hendelsen.
     * - Termindato
     * - Fødselsdato
     * - Omsorgsovertakelse dato
     * - Foreldreansvars dato
     * - Dato for stebarnsadopsjon
     *
     * NB: Tar ikke hensyn til perioder med permisjon.
     *
     * @return Skjæringstidspunktet for hendelse
     */
    LocalDate getSkjæringstidspunkt();

    /**
     * Vurderer om hendelsen er av typen fødsel
     * @return true/false
     */
    boolean getGjelderFødsel();

    /**
     * Vurderer om hendelsen er av typen adopsjon/omsorgovertakelse
     * @return true/false
     */
    boolean getGjelderAdopsjon();
}
