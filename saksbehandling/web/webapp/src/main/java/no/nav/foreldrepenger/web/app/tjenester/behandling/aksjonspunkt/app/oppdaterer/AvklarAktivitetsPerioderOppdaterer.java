package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.aksjonspunkt.BekreftOpptjeningPeriodeDto;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarAktivitetsPerioderDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.OpptjeningAktivitetDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.spsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.Tid;


@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarAktivitetsPerioderDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarAktivitetsPerioderOppdaterer implements AksjonspunktOppdaterer<AvklarAktivitetsPerioderDto> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String IKKE_GODKJENT_FOR_PERIODEN = "ikke godkjent for perioden ";
    private static final String GODKJENT_FOR_PERIODEN = "godkjent for perioden ";

    private AksjonspunktRepository aksjonspunktRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private VirksomhetTjeneste virksomhetTjeneste;
    private TpsTjeneste tpsTjeneste;
    private OpptjeningRepository opptjeningRepository;
    private BehandlingRepository behandlingRepository;

    AvklarAktivitetsPerioderOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarAktivitetsPerioderOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                              ResultatRepositoryProvider resultatRepositoryProvider,
                                              InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                              HistorikkTjenesteAdapter historikkAdapter,
                                              VirksomhetTjeneste virksomhetTjeneste,
                                              TpsTjeneste tpsTjeneste) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.historikkAdapter = historikkAdapter;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.tpsTjeneste = tpsTjeneste;
    }


    @Override
    public OppdateringResultat oppdater(AvklarAktivitetsPerioderDto dto, Behandling behandling, VilkårResultat.Builder vilkårBuilder) {
        if (dto.getOpptjeningAktivitetList().stream().anyMatch(oa -> oa.getErGodkjent() == null)) {
            throw new IllegalStateException("AvklarAktivitetsPerioder: Uavklarte aktiviteter til oppdaterer");
        }
        inntektArbeidYtelseTjeneste.bekreftPeriodeAksjonspunkt(behandling, map(dto.getOpptjeningAktivitetList(), behandling));
        // TODO (HN) Legge til støtte for å opprette historikkinnslag på aktiviteter som blir endret etter de er lagt til
        // Trenger en sjekk på om aktiviteten er endret etter den opprinnelig ble opprettet i samme operasjon

        boolean erEndret = erDetGjortEndringer(dto, behandling);

        if (erEndret) {
            AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }

        return OppdateringResultat.utenOveropp();
    }

    private boolean erDetGjortEndringer(AvklarAktivitetsPerioderDto dto, Behandling behandling) {
        boolean erEndret = false;
        for (OpptjeningAktivitetDto oaDto : dto.getOpptjeningAktivitetList()) {
            LocalDateInterval tilVerdi = new LocalDateInterval(oaDto.getOpptjeningFom(), oaDto.getOpptjeningTom());
            if (!oaDto.getErGodkjent()) {
                lagUtfallHistorikk(oaDto, behandling, tilVerdi, IKKE_GODKJENT_FOR_PERIODEN);
                erEndret = true;
            } else if (oaDto.getErGodkjent() && oaDto.getErEndret() != null && oaDto.getErEndret()) {
                lagUtfallHistorikk(oaDto, behandling, tilVerdi, GODKJENT_FOR_PERIODEN);
                erEndret = true;
            }
        }
        return erEndret;
    }

    private void lagUtfallHistorikk(OpptjeningAktivitetDto oaDto, Behandling behandling, LocalDateInterval tilVerdi, String godkjentForPerioden) {
        Optional<Opptjening> opptjeningOptional = opptjeningRepository.finnOpptjening(behandlingRepository.hentResultat(behandling.getId()));
        if (opptjeningOptional.isPresent()) {
            LocalDateInterval opptjentPeriode =
                new LocalDateInterval(opptjeningOptional.get().getFom(), opptjeningOptional.get().getTom());
            if (tilVerdi.contains(opptjentPeriode)) {
                byggHistorikkinnslag(behandling, oaDto, null, godkjentForPerioden + formaterPeriode(opptjentPeriode),
                    HistorikkinnslagType.FAKTA_ENDRET, HistorikkEndretFeltType.AKTIVITET);
            } else {
                byggHistorikkinnslag(behandling, oaDto, null, godkjentForPerioden + formaterPeriode(tilVerdi),
                    HistorikkinnslagType.FAKTA_ENDRET, HistorikkEndretFeltType.AKTIVITET);
            }
            lagEndretHistorikk(behandling, oaDto, opptjentPeriode);
        } else {
            byggHistorikkinnslag(behandling, oaDto, null, godkjentForPerioden + formaterPeriode(tilVerdi),
                HistorikkinnslagType.FAKTA_ENDRET, HistorikkEndretFeltType.AKTIVITET);
        }
    }

    private void lagEndretHistorikk(Behandling behandling, OpptjeningAktivitetDto oaDto, LocalDateInterval opptjentPeriode) {
        if (erAktivitetEndretForOpptjening(oaDto, opptjentPeriode)) {
            LocalDateInterval fraInterval = new LocalDateInterval(oaDto.getOriginalFom(), oaDto.getOriginalTom());
            LocalDateInterval tilInterval = hentTilInterval(oaDto, opptjentPeriode);
            byggHistorikkinnslag(behandling, oaDto, formaterPeriode(fraInterval), formaterPeriode(tilInterval),
                HistorikkinnslagType.FAKTA_ENDRET, HistorikkEndretFeltType.AKTIVITET_PERIODE);
        }
    }

    private LocalDateInterval hentTilInterval(OpptjeningAktivitetDto oaDto, LocalDateInterval opptjentPeriode) {
        LocalDate fom;
        LocalDate tom;

        if (opptjentPeriode.getTomDato() != null && oaDto.getOpptjeningTom() != null && oaDto.getOpptjeningTom().isEqual(opptjentPeriode.getTomDato())) {
            if (oaDto.getOriginalTom() != null && oaDto.getOriginalTom().isAfter(opptjentPeriode.getTomDato())) {
                tom = oaDto.getOriginalTom();
            } else {
                tom = oaDto.getOpptjeningTom();
            }
        } else {
            tom = oaDto.getOpptjeningTom();
        }

        if (opptjentPeriode.getFomDato() != null && oaDto.getOpptjeningFom() != null && oaDto.getOpptjeningFom().isEqual(opptjentPeriode.getFomDato())) {
            if (oaDto.getOriginalFom() != null && oaDto.getOriginalFom().isBefore(opptjentPeriode.getFomDato())) {
                fom = oaDto.getOriginalFom();
            } else {
                fom = oaDto.getOpptjeningFom();
            }
        } else {
            fom = oaDto.getOpptjeningFom();
        }

        return new LocalDateInterval(fom, tom);
    }

    private void byggHistorikkinnslag(Behandling behandling, OpptjeningAktivitetDto oaDto, String fraVerdi, String tilVerdi,
                                      HistorikkinnslagType histType, HistorikkEndretFeltType feltType) {
        if (OpptjeningAktivitetType.ARBEID.equals(oaDto.getAktivitetType())) {
            lagHistorikkinnslagDel(behandling, byggArbeidTekst(oaDto), fraVerdi, tilVerdi, oaDto.getBegrunnelse(), histType, feltType);
        } else {
            lagHistorikkinnslagDel(behandling, byggAnnenAktivitetTekst(oaDto), fraVerdi, tilVerdi, oaDto.getBegrunnelse(), histType, feltType);
        }
    }

    private String byggArbeidTekst(OpptjeningAktivitetDto oaDto) {
        if (OrganisasjonsNummerValidator.erGyldig(oaDto.getOppdragsgiverOrg())) {
            Virksomhet virksomhet = virksomhetTjeneste.finnOrganisasjon(oaDto.getOppdragsgiverOrg())
                .orElseThrow(IllegalArgumentException::new); // Utvikler feil hvis exception
            if (virksomhet.getNavn() == null) {
                return hentArbeidNavn(OpptjeningAktivitetType.ARBEID.getKode()) + " for " + virksomhet.getNavn() + " (" + oaDto.getOppdragsgiverOrg() + ")";
            }
            return oaDto.getAktivitetType().getNavn() + " for " + virksomhet.getNavn() + " (" + oaDto.getOppdragsgiverOrg() + ")";
        } else {
            return hentArbeidNavn(OpptjeningAktivitetType.ARBEID.getKode()) + " for organisasjonsnr. " + oaDto.getOppdragsgiverOrg();
        }
    }

    private String byggAnnenAktivitetTekst(OpptjeningAktivitetDto oaDto) {
        if (oaDto.getAktivitetType().getNavn() == null) {
            return hentArbeidNavn(oaDto.getAktivitetType().getKode());
        }
        return oaDto.getAktivitetType().getNavn();
    }

    private String hentArbeidNavn(String aktivitetTypeKode) {
        return opptjeningRepository.getOpptjeningAktivitetTypeForKode(aktivitetTypeKode).getNavn();
    }

    private void lagHistorikkinnslagDel(Behandling behandling, String navnVerdi, String fraVerdi, String tilVerdi,
                                        String begrunnelse, HistorikkinnslagType type, HistorikkEndretFeltType feltType) {
        HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = historikkAdapter.tekstBuilder();
        historikkInnslagTekstBuilder
            .medEndretFelt(feltType, navnVerdi,
                fraVerdi, tilVerdi)
            .medSkjermlenke(SkjermlenkeType.FAKTA_OM_OPPTJENING)
            .medBegrunnelse(begrunnelse);

        historikkAdapter.opprettHistorikkInnslag(behandling, type);
    }

    private String formaterPeriode(LocalDateInterval periode) {
        return formatDate(periode.getFomDato()) + " - " + formatDate(periode.getTomDato());
    }

    private String formatDate(LocalDate localDate) {
        if (Tid.TIDENES_ENDE.equals(localDate)) {
            return "d.d.";
        }
        return DATE_FORMATTER.format(localDate);
    }

    private boolean erAktivitetEndretForOpptjening(OpptjeningAktivitetDto oaDto, LocalDateInterval opptjentPeriode) {
        boolean aktivitetEndret = false;

        if (erLocalDateGyldigOgEndret(opptjentPeriode.getTomDato(), oaDto.getOriginalTom(), oaDto.getOpptjeningTom())
            || erLocalDateGyldigOgEndret(opptjentPeriode.getFomDato(), oaDto.getOriginalFom(), oaDto.getOpptjeningFom())) {
            if (!oaDto.getOriginalFom().equals(oaDto.getOpptjeningFom()) || !oaDto.getOriginalTom().equals(oaDto.getOpptjeningTom())) {
                aktivitetEndret = true;
            }
        }

        return aktivitetEndret;
    }

    private boolean erLocalDateGyldigOgEndret(LocalDate opptjentPeriode, LocalDate dtoOriginal, LocalDate dtoOpptjening) {
        return opptjentPeriode != null && dtoOriginal != null && dtoOpptjening != null && !dtoOpptjening.isEqual(opptjentPeriode);
    }

    private List<BekreftOpptjeningPeriodeDto> map(List<OpptjeningAktivitetDto> liste, Behandling behandling) {
        List<BekreftOpptjeningPeriodeDto> list = new ArrayList<>();
        Opptjening opptjening = opptjeningRepository.finnOpptjening(behandlingRepository.hentResultat(behandling.getId()))
            .orElseThrow(IllegalArgumentException::new);

        liste.forEach(l -> {
            BekreftOpptjeningPeriodeDto adapter = new BekreftOpptjeningPeriodeDto();
            adapter.setAktivitetType(l.getAktivitetType());
            adapter.setOriginalFom(l.getOriginalFom());
            adapter.setOriginalTom(l.getOriginalTom());

            if (OpptjeningAktivitetType.ARBEID.equals(l.getAktivitetType())) {
                if (OrganisasjonsNummerValidator.erGyldig(l.getOppdragsgiverOrg())) {
                    Virksomhet virksomhet = virksomhetTjeneste.finnOrganisasjon(l.getOppdragsgiverOrg())
                        .orElseThrow(IllegalArgumentException::new); // Utvikler feil hvis exception
                    if (virksomhet.getNavn() != null) {
                        adapter.setArbeidsgiver(virksomhet.getNavn());
                    }
                } else if (l.getOppdragsgiverOrg() != null && PersonIdent.erGyldigFnr(l.getOppdragsgiverOrg())) {
                    final Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForFnr(PersonIdent.fra(l.getOppdragsgiverOrg()));
                    adapter.setArbeidsgiver(personinfo.map(Personinfo::getNavn).orElse("N/A"));
                } else {
                    adapter.setArbeidsgiver("N/A");
                }
            }
            adapter.setOppdragsgiverOrg(l.getOppdragsgiverOrg());
            adapter.setStillingsandel(l.getStillingsandel());
            adapter.setNaringRegistreringsdato(l.getNaringRegistreringsdato());
            adapter.setErManueltOpprettet(l.getErManueltOpprettet());
            adapter.setErGodkjent(l.getErGodkjent());
            boolean erEndret = erEndret(
                DatoIntervallEntitet.fraOgMedTilOgMed(opptjening.getFom(), opptjening.getTom()),
                DatoIntervallEntitet.fraOgMedTilOgMed(l.getOpptjeningFom(), l.getOpptjeningTom()), l.getOriginalFom() != null
                    ? DatoIntervallEntitet.fraOgMedTilOgMed(l.getOriginalFom(), l.getOriginalTom()) : null);
            settPeriode(opptjening, l, adapter, erEndret);
            adapter.setErEndret(erEndret);

            adapter.setBegrunnelse(l.getBegrunnelse());
            adapter.setArbeidsforholdRef(l.getArbeidsforholdRef());
            list.add(adapter);
        });
        return list;
    }

    private void settPeriode(Opptjening opptjening, OpptjeningAktivitetDto l, BekreftOpptjeningPeriodeDto adapter, boolean erEndret) {
        if (erEndret) {
            if (l.getOpptjeningFom().equals(opptjening.getFom())) {
                if (l.getOriginalFom() != null && l.getOriginalFom().isBefore(l.getOpptjeningFom())) {
                    adapter.setOpptjeningFom(l.getOriginalFom() != null ? l.getOriginalFom() : l.getOpptjeningFom());
                } else {
                    adapter.setOpptjeningFom(l.getOpptjeningFom());
                }
            } else {
                adapter.setOpptjeningFom(l.getOpptjeningFom());
            }
            if (l.getOpptjeningTom().equals(opptjening.getTom())) {
                if (l.getOriginalTom() != null && l.getOriginalTom().isAfter(l.getOpptjeningTom())) {
                    adapter.setOpptjeningTom(l.getOriginalTom() != null ? l.getOriginalTom() : l.getOpptjeningTom());
                } else {
                    adapter.setOpptjeningTom(l.getOpptjeningTom());
                }
            } else {
                adapter.setOpptjeningTom(l.getOpptjeningTom());
            }
        } else {
            adapter.setOpptjeningFom(l.getOriginalFom());
            adapter.setOpptjeningTom(l.getOriginalTom());
        }
    }

    boolean erEndret(DatoIntervallEntitet beregnetOpptjening, DatoIntervallEntitet aktivitetPeriode, DatoIntervallEntitet orginalPeriode) {
        if (orginalPeriode == null) {
            return true;
        }
        if (orginalPeriode.inkluderer(beregnetOpptjening.getFomDato()) && orginalPeriode.inkluderer(beregnetOpptjening.getTomDato())) {
            return !beregnetOpptjening.equals(aktivitetPeriode);
        } else if (beregnetOpptjening.inkluderer(orginalPeriode.getTomDato()) && beregnetOpptjening.inkluderer(orginalPeriode.getFomDato())) {
            return !orginalPeriode.equals(aktivitetPeriode);
        } else if (beregnetOpptjening.inkluderer(orginalPeriode.getTomDato()) && !beregnetOpptjening.inkluderer(orginalPeriode.getFomDato())) {
            return !DatoIntervallEntitet.fraOgMedTilOgMed(beregnetOpptjening.getFomDato(), orginalPeriode.getTomDato()).equals(aktivitetPeriode);
        }
        return !DatoIntervallEntitet.fraOgMedTilOgMed(orginalPeriode.getFomDato(), beregnetOpptjening.getTomDato()).equals(aktivitetPeriode);
    }
}
