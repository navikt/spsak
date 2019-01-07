package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.søknad.v1;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.Sykefravær;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentParserRef;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentOversetter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.sykepenger.kontrakter.søknad.SykepengersøknadConstants;
import no.nav.sykepenger.kontrakter.søknad.v1.SykepengesøknadV1;
import no.nav.sykepenger.kontrakter.søknad.v1.fravær.FraværsPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.opptjening.AnnenInntektskilde;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.EgenmeldingPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.KorrigertArbeidstidPeriode;

@DokumentParserRef(SykepengersøknadConstants.REFERER_V1)
@ApplicationScoped
public class MottattDokumentOversetterSøknad implements MottattDokumentOversetter<MottattDokumentWrapperSøknad> {

    private VirksomhetTjeneste virksomhetTjeneste;
    private KodeverkRepository kodeverkRepository;
    private SøknadRepository søknadRepository;
    private SykefraværRepository sykefraværRepository;


    MottattDokumentOversetterSøknad() {
        // for CDI proxy
    }

    @Inject
    public MottattDokumentOversetterSøknad(GrunnlagRepositoryProvider repositoryProvider, VirksomhetTjeneste virksomhetTjeneste) {
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.sykefraværRepository = repositoryProvider.getSykefraværRepository();
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    @Override
    public void trekkUtDataOgPersister(MottattDokumentWrapperSøknad wrapper, InngåendeSaksdokument mottattDokument, Behandling behandling, Optional<LocalDate> gjelderFra) {
        SykepengesøknadV1 skjema = wrapper.getSkjema();
        Arbeidsgiver arbeidsgiver = mapArbeidsgiver(skjema.getArbeidsgiverId());

        SøknadEntitet.Builder builder = new SøknadEntitet.Builder();
        builder.medMottattDato(mottattDokument.getForsendelseMottatt())
            .medSøknadsdato(mottattDokument.getForsendelseMottatt())
            .medSøknadReferanse(skjema.getSøknadId())
            .medSykemeldinReferanse(skjema.getSykemeldingId())
            .medArbeidsgiver(arbeidsgiver)
            .medOppgittSykefravær(mapSykefravær(behandling, arbeidsgiver, skjema))
            .medOppgittOpptjening(mapOppgittOpptjening(wrapper));

        søknadRepository.lagreOgFlush(behandling, builder.build());
    }

    private Sykefravær mapSykefravær(Behandling behandling, Arbeidsgiver arbeidsgiver, SykepengesøknadV1 skjema) {
        SykefraværBuilder builder = sykefraværRepository.oppretBuilderForSykefravær(behandling.getId());

        List<KorrigertArbeidstidPeriode> korrigertArbeidstid = skjema.getKorrigertArbeidstid();
        if (korrigertArbeidstid != null) {
            korrigertArbeidstid.forEach(p -> mapPeriode(p, arbeidsgiver, builder));
        }
        List<EgenmeldingPeriode> egenmeldinger = skjema.getEgenmeldinger();
        if (egenmeldinger != null) {
            egenmeldinger.forEach(em -> mapPeriode(em, arbeidsgiver, builder));
        }
        List<FraværsPeriode> fravær = skjema.getFravær();
        if (fravær != null) {
            fravær.forEach(f -> mapPeriode(f, arbeidsgiver, builder));
        }

        // FIXME SP - Map resten av verdiene

        sykefraværRepository.lagre(behandling, builder);
        Optional<SykefraværGrunnlag> grunnlag = sykefraværRepository.hentHvisEksistererFor(behandling.getId());

        return grunnlag.map(SykefraværGrunnlag::getSykefravær).orElse(null);
    }

    private void mapPeriode(KorrigertArbeidstidPeriode p, Arbeidsgiver arbeidsgiver, SykefraværBuilder builder) {
        SykefraværPeriodeBuilder periodeBuilder = builder.periodeBuilder();
        periodeBuilder.medArbeidsgiver(arbeidsgiver)
            .medPeriode(p.getFom(), p.getTom())
            .medType(SykefraværPeriodeType.SYKEMELDT)
            .medArbeidsgrad(new Prosentsats(p.getFaktiskGrad()));
        // FIXME SP - Map resten av verdiene

        builder.leggTil(periodeBuilder);
    }

    private void mapPeriode(EgenmeldingPeriode p, Arbeidsgiver arbeidsgiver, SykefraværBuilder builder) {
        SykefraværPeriodeBuilder periodeBuilder = builder.periodeBuilder();
        periodeBuilder.medArbeidsgiver(arbeidsgiver)
            .medPeriode(p.getFom(), p.getTom())
            .medType(SykefraværPeriodeType.EGENMELDING);

        builder.leggTil(periodeBuilder);
    }

    private void mapPeriode(FraværsPeriode p, Arbeidsgiver arbeidsgiver, SykefraværBuilder builder) {
        SykefraværPeriodeBuilder periodeBuilder = builder.periodeBuilder();
        periodeBuilder.medArbeidsgiver(arbeidsgiver)
            .medPeriode(p.getFom(), p.getTom())
            .medType(kodeverkRepository.finn(SykefraværPeriodeType.class, p.getType().name()));

        builder.leggTil(periodeBuilder);
    }

    private Arbeidsgiver mapArbeidsgiver(String arbeidsgiverId) {
        if (OrganisasjonsNummerValidator.erGyldig(arbeidsgiverId)) {
            return Arbeidsgiver.virksomhet(virksomhetTjeneste.finnOrganisasjon(arbeidsgiverId).orElseThrow(IllegalArgumentException::new));
        } else if (arbeidsgiverId != null) {
            return Arbeidsgiver.person(new AktørId(arbeidsgiverId));
        }
        return null;
    }

    private OppgittOpptjening mapOppgittOpptjening(MottattDokumentWrapperSøknad wrapper) {
        List<AnnenInntektskilde> andreInntektskilder = wrapper.getAndreInntektskilder();
        if (andreInntektskilder != null && !andreInntektskilder.isEmpty()) {
            // FIXME SP : map til oppgittOpptjening
            return null;
        }
        return null;
    }
}
