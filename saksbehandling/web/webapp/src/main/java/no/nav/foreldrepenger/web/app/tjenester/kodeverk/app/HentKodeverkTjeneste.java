package no.nav.foreldrepenger.web.app.tjenester.kodeverk.app;

import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
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
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

public interface HentKodeverkTjeneste {

    List<Class<? extends Kodeliste>> KODEVERK_SOM_BRUKES_PÅ_KLIENT = List.of(
        RelatertYtelseTilstand.class,
        FagsakStatus.class,
        RelatertYtelseType.class,
        BehandlingÅrsakType.class,
        HistorikkBegrunnelseType.class,
        MedlemskapManuellVurderingType.class,
        VergeType.class,
        VirksomhetType.class,
        Landkoder.class,
        PersonstatusType.class,
        FagsakYtelseType.class,
        Venteårsak.class,
        BehandlingType.class,
        ArbeidType.class,
        OpptjeningAktivitetType.class,
        Inntektskategori.class,
        DokumentTypeId.class,
        BeregningsgrunnlagAndeltype.class,
        AktivitetStatus.class,
        Arbeidskategori.class,
        Fagsystem.class,
        Region.class,
        SivilstandType.class,
        FaktaOmBeregningTilfelle.class,
        SkjermlenkeType.class
    );

    List<String> KODEVERK_SOM_BRUKES_PÅ_KLIENT_DISCRIMINATORS = List.of(
        RelatertYtelseTilstand.DISCRIMINATOR,
        FagsakStatus.DISCRIMINATOR,
        RelatertYtelseType.DISCRIMINATOR,
        BehandlingÅrsakType.DISCRIMINATOR,
        HistorikkBegrunnelseType.DISCRIMINATOR,
        MedlemskapManuellVurderingType.DISCRIMINATOR,
        VergeType.DISCRIMINATOR,
        VirksomhetType.DISCRIMINATOR,
        Landkoder.DISCRIMINATOR,
        PersonstatusType.DISCRIMINATOR,
        FagsakYtelseType.DISCRIMINATOR,
        Venteårsak.DISCRIMINATOR,
        BehandlingType.DISCRIMINATOR,
        ArbeidType.DISCRIMINATOR,
        OpptjeningAktivitetType.DISCRIMINATOR,
        Inntektskategori.DISCRIMINATOR,
        DokumentTypeId.DISCRIMINATOR,
        BeregningsgrunnlagAndeltype.DISCRIMINATOR,
        AktivitetStatus.DISCRIMINATOR,
        Arbeidskategori.DISCRIMINATOR,
        Fagsystem.DISCRIMINATOR,
        Region.DISCRIMINATOR,
        SivilstandType.DISCRIMINATOR,
        FaktaOmBeregningTilfelle.DISCRIMINATOR,
        SkjermlenkeType.DISCRIMINATOR
    );

    Map<String, List<Kodeliste>> hentGruppertKodeliste();

    List<OrganisasjonsEnhet> hentBehandlendeEnheter();
}
