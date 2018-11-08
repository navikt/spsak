package no.nav.foreldrepenger.domene.ytelsefordeling;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public interface YtelseFordelingTjeneste {

    YtelseFordelingAggregat hentAggregat(Behandling behandling);

    Optional<YtelseFordelingAggregat> hentAggregatHvisEksisterer(Behandling behandling);

    void aksjonspunktBekreftFaktaForOmsorg(Behandling behandling, BekreftFaktaForOmsorgVurderingAksjonspunktDto adapter);

    void overstyrSøknadsperioder(Behandling behandling, List<OppgittPeriode> overstyrteSøknadsperioder, List<PeriodeUttakDokumentasjon> dokumentasjonsperioder);

    boolean erEndret(Behandling origBehandling, Behandling nyBehandling);

    EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling);

    DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);

    void aksjonspunktAvklarStartdatoForPerioden(Behandling behandling, BekreftStartdatoForPerioden adapter);
}
