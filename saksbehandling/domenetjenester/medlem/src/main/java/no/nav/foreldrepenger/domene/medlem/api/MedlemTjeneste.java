package no.nav.foreldrepenger.domene.medlem.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;

public interface MedlemTjeneste {

    /**
     * Finn medlemskapsperioder i MEDL2 register for en person.
     *
     * @param finnMedlemRequest Inneholder fødselsnummer, start-/slutt- dato for søket, og behandling-/fagsak- ID.
     * @return Liste av medlemsperioder funnet
     */
    List<Medlemskapsperiode> finnMedlemskapPerioder(FinnMedlemRequest finnMedlemRequest);

    void bekreftErMedlem(Behandling behandling, String manuellVurderingKode);

    void aksjonspunktBekreftMeldlemVurdering(Behandling behandling, BekreftErMedlemVurderingAksjonspunktDto adapter);

    void aksjonspunktBekreftOppholdVurdering(Behandling behandling, BekreftOppholdVurderingAksjonspunktDto adapter);

    void aksjonspunktBekreftBosattVurdering(Behandling behandling, BekreftBosattVurderingAksjonspunktDto adapter);

    void aksjonspunktAvklarFortsattMedlemskap(Behandling behandling, AvklarFortsattMedlemskapAksjonspunktDto adapter);

    Optional<MedlemskapAggregat> hentMedlemskap(Behandling behandling);

    boolean oppdaterMedlemskapHvisEndret(Behandling behandling, Optional<MedlemskapAggregat> medlemskap, List<RegistrertMedlemskapPerioder> list1, List<RegistrertMedlemskapPerioder> list2);

    boolean erMedlemskapPerioderEndret(Behandling behandling, Optional<MedlemskapAggregat> medlemskap, List<RegistrertMedlemskapPerioder> list1, List<RegistrertMedlemskapPerioder> list2);

    EndringsresultatSnapshot finnAktivGrunnlagId(Behandling behandling);

    /**
     * Sjekker endringer i personopplysninger som tilsier at bruker 'ikke er'/'skal miste' medlemskap.
     * Sjekker statsborgerskap (kun mht endring i {@link Region}, ikke land),
     * {@link PersonstatusType}, og {@link AdresseType}
     * for intervall { max(seneste vedtatte medlemskapsperiode, skjæringstidspunkt), nå}.
     *
     * Metoden gjelder revurdering foreldrepenger
     */
    EndringsresultatPersonopplysningerForMedlemskap søkerHarEndringerIPersonopplysninger(Behandling revurderingBehandling);

    Map<LocalDate, VurderMedlemskap> utledVurderingspunkterMedAksjonspunkt(Behandling behandling);

    DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);
}
