package no.nav.foreldrepenger.domene.familiehendelse;

import java.util.List;
import java.util.Optional;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public interface FamilieHendelseTjeneste {

    List<Interval> beregnGyldigeFødselsperioder(Behandling behandling);

    void aksjonspunktBekreftMannAdopterer(Behandling behandling, BekreftAdopsjonsAksjonspunktDto adapter);

    void aksjonspunktBekreftEktefellesBarn(Behandling behandling, BekreftAdopsjonsAksjonspunktDto adapter);

    void aksjonspunktAvklarManglendeFødsel(Behandling behandling, AvklarManglendeFødselAksjonspunktDto adapter);

    FamilieHendelseGrunnlag hentAggregat(Behandling behandling);

    void oppdaterFødselPåGrunnlag(Behandling behandling, List<FødtBarnInfo> bekreftetTps);

    boolean erUtstedtdatoEllerTermindatoEndret(Behandling behandling, TerminbekreftelseAksjonspunktDto adapter);

    Optional<FamilieHendelseGrunnlag> finnAggregat(Behandling behandling);

    void aksjonspunktBekreftTerminbekreftelse(Behandling behandling, TerminbekreftelseAksjonspunktDto adapter);

    void aksjonspunktBekreftDokumentasjon(Behandling behandling, BekreftDokumentasjonAksjonspunktDto adapter);

    void aksjonspunktVurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger(Behandling behandling, VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger syksomVedFødselForForeldrepenger);

    EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling);

    DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);

    Optional<FamilieHendelseGrunnlag> hentFamilieHendelseGrunnlagForBehandling(Behandling behandling);

    Optional<Boolean> gjelderFødsel(Behandling behandling);

    List<Personopplysning> finnBarnSøktStønadFor(Behandling behandling);

}
