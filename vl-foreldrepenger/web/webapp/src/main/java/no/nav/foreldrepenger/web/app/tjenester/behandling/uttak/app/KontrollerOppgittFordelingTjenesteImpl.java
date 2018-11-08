package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak.SYKDOM_ANNEN_FORELDER;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak.INSTITUSJON_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak.INSTITUSJON_SØKER;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak.SYKDOM;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.UttakDokumentasjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl.KontrollerFaktaUttakFeil;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.BekreftetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakDokumentasjonDto;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class KontrollerOppgittFordelingTjenesteImpl implements KontrollerOppgittFordelingTjeneste {

    private YtelseFordelingTjeneste ytelseFordelingTjeneste;
    private KodeverkRepository kodeverkRepository;
    private VirksomhetRepository virksomhetRepository;

    KontrollerOppgittFordelingTjenesteImpl() {
        //For CDI proxy
    }

    @Inject
    public KontrollerOppgittFordelingTjenesteImpl(YtelseFordelingTjeneste ytelseFordelingTjeneste,
                                                  BehandlingRepositoryProvider repositoryProvider,
                                                  VirksomhetRepository virksomhetRepository) {
        this.ytelseFordelingTjeneste = ytelseFordelingTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.virksomhetRepository = virksomhetRepository;
    }

    @Override
    public void avklarFaktaUttaksperiode(AvklarFaktaUttakDto dto, Behandling behandling) {
        overstyrSøknadsperiode(dto.getBekreftedePerioder(), behandling);
    }

    @Override
    public void manuellAvklarFaktaUttaksperiode(ManuellAvklarFaktaUttakDto dto, Behandling behandling) {
        overstyrSøknadsperiode(dto.getBekreftedePerioder(), behandling);
    }

    private void overstyrSøknadsperiode(List<BekreftetUttakPeriodeDto> bekreftedePerioder, Behandling behandling) {

        YtelseFordelingAggregat ytelseFordelingAggregat = ytelseFordelingTjeneste.hentAggregat(behandling);
        List<OppgittPeriode> gjeldendeFordeling = ytelseFordelingAggregat
            .getGjeldendeSøknadsperioder()
            .getOppgittePerioder();

        Optional<AvklarteUttakDatoer> avklarteUttakDatoer = ytelseFordelingAggregat.getAvklarteDatoer();
        AvklarFaktaUttakValidator.validerOpplysninger(bekreftedePerioder, gjeldendeFordeling, avklarteUttakDatoer);

        final List<OppgittPeriode> overstyrtPerioder = new ArrayList<>();
        final List<PeriodeUttakDokumentasjon> dokumentasjonsperioder = new ArrayList<>();

        for (BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto : bekreftedePerioder) {
            KontrollerFaktaPeriodeDto bekreftetPeriode = bekreftetUttakPeriodeDto.getBekreftetPeriode();
            final OppgittPeriodeBuilder oppgittPeriodeBuilder = oversettPeriode(bekreftetPeriode);
            if (bekreftetPeriode.getÅrsak().isPresent()) {
                dokumentasjonsperioder.addAll(oversettDokumentasjonsperioder(bekreftetPeriode.getÅrsak().get(), bekreftetPeriode.getDokumentertePerioder()));
            }
            overstyrtPerioder.add(oppgittPeriodeBuilder.build());
        }

        ytelseFordelingTjeneste.overstyrSøknadsperioder(behandling, overstyrtPerioder, dokumentasjonsperioder);

    }

    private OppgittPeriodeBuilder oversettPeriode(KontrollerFaktaPeriodeDto faktaPeriodeDto) {
        Objects.requireNonNull(faktaPeriodeDto, "kontrollerFaktaPeriodeDto"); // NOSONAR $NON-NLS-1$
        final OppgittPeriodeBuilder periodeBuilder = OppgittPeriodeBuilder.ny()
            .medPeriode(faktaPeriodeDto.getFom(), faktaPeriodeDto.getTom())
            .medSamtidigUttak(faktaPeriodeDto.getSamtidigUttak())
            .medSamtidigUttaksprosent(faktaPeriodeDto.getSamtidigUttaksprosent())
            .medFlerbarnsdager(faktaPeriodeDto.isFlerbarnsdager());

        if (faktaPeriodeDto.getOrgnr() != null) {
            periodeBuilder.medVirksomhet(hentVirksomhet(faktaPeriodeDto.getOrgnr()));
        }

        if (faktaPeriodeDto.getUttakPeriodeType() != null) {
            UttakPeriodeType periodeType = kodeverkRepository.finn(UttakPeriodeType.class, faktaPeriodeDto.getUttakPeriodeType().getKode());
            periodeBuilder.medPeriodeType(periodeType);
        }
        if (faktaPeriodeDto.getArbeidstidsprosent() != null) {
            periodeBuilder.medErArbeidstaker(faktaPeriodeDto.getErArbeidstaker());
            periodeBuilder.medArbeidsprosent(faktaPeriodeDto.getArbeidstidsprosent());
        }
        if (erUtsettelse(faktaPeriodeDto)) {
            Årsak utsettelseÅrsak = kodeverkRepository.finn(UtsettelseÅrsak.class, faktaPeriodeDto.getUtsettelseÅrsak().getKode());
            periodeBuilder.medÅrsak(utsettelseÅrsak);
        } else if (erOverføring(faktaPeriodeDto)) {
            Årsak overføringsÅrsak = kodeverkRepository.finn(OverføringÅrsak.class, faktaPeriodeDto.getOverføringÅrsak().getKode());
            periodeBuilder.medÅrsak(overføringsÅrsak);
        }
        if (faktaPeriodeDto.getBegrunnelse() != null) {
            periodeBuilder.medBegrunnelse(faktaPeriodeDto.getBegrunnelse());
        }
        if (faktaPeriodeDto.getResultat() != null) {
            UttakPeriodeVurderingType vurderingType = kodeverkRepository.finn(UttakPeriodeVurderingType.class, faktaPeriodeDto.getResultat().getKode());
            periodeBuilder.medVurdering(vurderingType);
        }

        if (faktaPeriodeDto.getMorsAktivitet() != null && !Årsak.UDEFINERT.getKode().equals(faktaPeriodeDto.getMorsAktivitet().getKode())) {
            MorsAktivitet morsAktivitet = kodeverkRepository.finn(MorsAktivitet.class, faktaPeriodeDto.getMorsAktivitet().getKode());
            periodeBuilder.medMorsAktivitet(morsAktivitet);
        }

        periodeBuilder.medPeriodeKilde(faktaPeriodeDto.getPeriodeKilde());

        return periodeBuilder;
    }

    private Virksomhet hentVirksomhet(String orgnr) {
        return virksomhetRepository.hent(orgnr)
            .orElseThrow(() -> FeilFactory.create(KontrollerFaktaUttakFeil.class).finnerIkkeVirksomhet(orgnr).toException());
    }

    private List<PeriodeUttakDokumentasjon> oversettDokumentasjonsperioder(Årsak årsak, List<UttakDokumentasjonDto> dokumentasjonPerioder) {
        return dokumentasjonPerioder.stream().map(periode -> {
            UttakDokumentasjonType uttakDokumentasjonType = finnUttakDokumentasjonType(årsak, periode.getDokumentasjonType());
            return new PeriodeUttakDokumentasjonEntitet(periode.getFom(), periode.getTom(), uttakDokumentasjonType);
        }).collect(Collectors.toList());
    }

    private UttakDokumentasjonType finnUttakDokumentasjonType(Årsak årsak, UttakDokumentasjonType dokumentasjonType) {
        if (dokumentasjonType != null) {
            return kodeverkRepository.finn(UttakDokumentasjonType.class, dokumentasjonType.getKode());
        }

        if (årsak != null) {
            if (SYKDOM.getKode().equals(årsak.getKode())) {
                return kodeverkRepository.finn(UttakDokumentasjonType.class, UttakDokumentasjonType.SYK_SØKER);
            }
            if (INSTITUSJON_BARN.getKode().equals(årsak.getKode())) {
                return kodeverkRepository.finn(UttakDokumentasjonType.class, UttakDokumentasjonType.INNLAGT_BARN);
            }
            if (INSTITUSJON_SØKER.getKode().equals(årsak.getKode())) {
                return kodeverkRepository.finn(UttakDokumentasjonType.class, UttakDokumentasjonType.INNLAGT_SØKER);
            }
            if (INSTITUSJONSOPPHOLD_ANNEN_FORELDRE.getKode().equals(årsak.getKode())) {
                return kodeverkRepository.finn(UttakDokumentasjonType.class, UttakDokumentasjonType.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE);
            }
            if (SYKDOM_ANNEN_FORELDER.getKode().equals(årsak.getKode())) {
                return kodeverkRepository.finn(UttakDokumentasjonType.class, UttakDokumentasjonType.SYKDOM_ANNEN_FORELDER);
            }
        }
        throw new IllegalStateException("Finner ikke uttakDokumentasjonType for årsak: " + årsak); //NOSONAR
    }

    private boolean erOverføring(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        return bekreftetPeriode.getOverføringÅrsak() != null && !Årsak.UDEFINERT.getKode().equals(bekreftetPeriode.getOverføringÅrsak().getKode());
    }

    private boolean erUtsettelse(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        return bekreftetPeriode.getUtsettelseÅrsak() != null && !Årsak.UDEFINERT.getKode().equals(bekreftetPeriode.getUtsettelseÅrsak().getKode());
    }

}
