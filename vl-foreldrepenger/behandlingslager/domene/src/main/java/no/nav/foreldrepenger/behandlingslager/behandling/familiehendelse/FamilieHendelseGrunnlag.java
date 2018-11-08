package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;

/**
 * Grunnlag som inneholder tre versjoner av FamilieHendelse.
 * <p>
 * De forskjellige versjonene har kilde som følge:
 * 1: SøknadVersjon -> Søkers oppgitte data.
 * 2: BekreftetVersjon -> Bekreftede data fra registrene
 * 3: OverstyrtVersjon -> Saksbehandler overstyrer ved å behandle et aksjonspunkt.
 *
 * @see no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse
 */
public interface FamilieHendelseGrunnlag {

    /**
     * Søkers oppgitte data.
     *
     * @return FamilieHendelse
     */
    FamilieHendelse getSøknadVersjon();

    /**
     * Bekreftede data fra registrene
     *
     * @return Optional FamilieHendelse
     */
    Optional<FamilieHendelse> getBekreftetVersjon();

    /**
     * Saksbehandler overstyrer ved å behandle et aksjonspunkt.
     *
     * @return Optional FamilieHendelse
     */
    Optional<FamilieHendelse> getOverstyrtVersjon();

    /**
     * Gir den mest relevante versjonen avhengig av hva som er tilstede.
     * <ol>
     * <li>Overstyrt versjon</li>
     * <li>Register versjon</li>
     * <li>Søknad versjon</li>
     * </ol>
     *
     * @return FamilieHendelse
     */
    FamilieHendelse getGjeldendeVersjon();

    /**
     * Gir den mest relevante versjonen av Adopsjon avhengig av hva som er tilstede.
     * <ol>
     * <li>Overstyrt versjon</li>
     * <li>Register versjon</li>
     * <li>Søknad versjon</li>
     * </ol>
     *
     * @return adopsjon
     */
    Optional<Adopsjon> getGjeldendeAdopsjon();

    /**
     * Gir den mest relevante versjonen av Terminbekreftelse avhengig av hva som er tilstede.
     * <ol>
     * <li>Overstyrt versjon</li>
     * <li>Register versjon</li>
     * <li>Søknad versjon</li>
     * </ol>
     *
     * @return Terminbekreftelse
     */
    Optional<Terminbekreftelse> getGjeldendeTerminbekreftelse();

    /**
     * Gir den mest relevante versjonen av Barn avhengig av hva som er tilstede.
     * 1: Overstyrt versjon hvis tilstede og har innhold
     * 2: Register versjon hvis tilstede og har innhold
     * 3: Søknad versjon
     *
     * @return Liste av UidentifisertBarn
     */
    List<UidentifisertBarn> getGjeldendeBarna();

    /**
     * Gir den mest relevante versjonen av antall barn avhengig av hva som er tilstede.
     * 1: Overstyrt versjon
     * 2: Register versjon
     * 3: Søknad versjon
     *
     * @return antall barn
     */
    Integer getGjeldendeAntallBarn();

    /**
     * Gir den mest relevante versjonen avhengig av hva som er tilstede.
     * 1: Overstyrt versjon
     * 2: Register versjon
     *
     * @return FamilieHendelse
     */
    Optional<FamilieHendelse> getGjeldendeBekreftetVersjon();

    /**
     * Gir den mest relevante versjonen avhengig av hva som er tilstede.
     * <ol>
     * <li>Overstyrt versjon</li>
     * <li>Register versjon</li>
     * <li>Søknad versjon</li>
     * </ol>
     *
     * @return foreslått Fødselsdatoen
     */
    LocalDate finnGjeldendeFødselsdato();

    boolean getHarBekreftedeData();

    boolean getHarOverstyrteData();
}
