package no.nav.foreldrepenger.web.app.tjenester.kodeverk.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.RevurderingVarslingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAndeltype;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkBegrunnelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.uttak.GraderingAvslagÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.InnvilgetÅrsak;

public interface HentKodeverkTjeneste {

    List<Class<? extends Kodeliste>> KODEVERK_SOM_BRUKES_PÅ_KLIENT = Collections.unmodifiableList(Arrays.asList(
        OppholdÅrsak.class,
        RelatertYtelseTilstand.class,
        FagsakStatus.class,
        RelatertYtelseType.class,
        BehandlingÅrsakType.class,
        KlageMedholdÅrsak.class,
        KlageAvvistÅrsak.class,
        HistorikkBegrunnelseType.class,
        OppgaveÅrsak.class,
        MedlemskapManuellVurderingType.class,
        VergeType.class,
        MorsAktivitet.class,
        VirksomhetType.class,
        Landkoder.class,
        PersonstatusType.class,
        FagsakYtelseType.class,
        Venteårsak.class,
        ForeldreType.class,
        InnsynResultatType.class,
        BehandlingType.class,
        ArbeidType.class,
        UtsettelseÅrsak.class,
        OverføringÅrsak.class,
        UttakPeriodeType.class,
        UtsettelseÅrsak.class,
        IkkeOppfyltÅrsak.class,
        InnvilgetÅrsak.class,
        OpptjeningAktivitetType.class,
        RevurderingVarslingÅrsak.class,
        Inntektskategori.class,
        DokumentTypeId.class,
        BeregningsgrunnlagAndeltype.class,
        AktivitetStatus.class,
        Arbeidskategori.class,
        UttakPeriodeVurderingType.class,
        Fagsystem.class,
        Region.class,
        SivilstandType.class,
        FaktaOmBeregningTilfelle.class,
        GraderingAvslagÅrsak.class,
        SkjermlenkeType.class
    ));

    Map<String, List<Kodeliste>> hentGruppertKodeliste();

    List<OrganisasjonsEnhet> hentBehandlendeEnheter();
}
