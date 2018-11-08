package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.søknad.v1;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil.convertToLocalDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.Innsendingsvalg;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.AnnenAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.FrilansEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.FrilansoppdragEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.UtenlandskVirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOppholdEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPart;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPartBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadVedleggEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentFeil;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentOversetter;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.NamespaceRef;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.søknad.v1.SøknadConstants;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelderMedNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelderUtenNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdUtlandet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Periode;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.AnnenOpptjening;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.EgenNaering;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Frilans;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.NorskOrganisasjon;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Opptjening;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Regnskapsfoerer;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.UtenlandskArbeidsforhold;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.UtenlandskOrganisasjon;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.AnnenOpptjeningTyper;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Dekningsgrader;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Innsendingstype;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Land;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Omsorgsovertakelseaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Virksomhetstyper;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Gradering;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.LukketPeriodeMedVedlegg;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Oppholdsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Overfoeringsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Utsettelsesperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Uttaksperiode;

@NamespaceRef(SøknadConstants.NAMESPACE)
@ApplicationScoped
public class MottattDokumentOversetterSøknad implements MottattDokumentOversetter<MottattDokumentWrapperSøknad> { // NOSONAR - (essv)kan akseptere lang mapperklasse

    private VirksomhetTjeneste virksomhetTjeneste;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private PersonopplysningRepository personopplysningRepository;
    private FamilieHendelseRepository familieHendelseRepository;
    private SøknadRepository søknadRepository;
    private MedlemskapRepository medlemskapRepository;
    private KodeverkRepository kodeverkRepository;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private TpsTjeneste tpsAdapter;
    private BehandlingRevurderingRepository behandlingRevurderingRepository;

    MottattDokumentOversetterSøknad() {
        // for CDI proxy
    }

    @Inject
    public MottattDokumentOversetterSøknad(BehandlingRepositoryProvider repositoryProvider,
                                           VirksomhetTjeneste virksomhetTjeneste,
                                           TpsTjeneste tpsAdapter) {
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.tpsAdapter = tpsAdapter;
        this.behandlingRevurderingRepository = repositoryProvider.getBehandlingRevurderingRepository();
    }

    @Override
    public void trekkUtDataOgPersister(MottattDokumentWrapperSøknad wrapper, MottattDokument mottattDokument, Behandling behandling, Optional<LocalDate> gjelderFra) {
        if (erEndring(mottattDokument)) {
            persisterEndringssøknad(wrapper, mottattDokument, behandling, gjelderFra);
        } else {
            persisterSøknad(wrapper, mottattDokument, behandling);
        }
    }

    private SøknadEntitet.Builder kopierSøknad(Behandling behandling) {
        SøknadEntitet.Builder søknadBuilder;
        Optional<Behandling> originalBehandling = behandling.getOriginalBehandling();
        if (originalBehandling.isPresent()) {
            Søknad originalSøknad = søknadRepository.hentSøknad(originalBehandling.get());
            søknadBuilder = new SøknadEntitet.Builder(originalSøknad);

            OppgittAnnenPartBuilder oppgittAnnenPartBuilder = new OppgittAnnenPartBuilder(originalSøknad.getSøknadAnnenPart());
            personopplysningRepository.lagre(behandling, oppgittAnnenPartBuilder);
            final Optional<OppgittAnnenPart> annenPartFraSøknad = personopplysningRepository.hentPersonopplysninger(behandling).getOppgittAnnenPart();
            annenPartFraSøknad.ifPresent(søknadBuilder::medSøknadAnnenPart);

            OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder = new OppgittTilknytningEntitet.Builder(originalSøknad.getOppgittTilknytning());
            medlemskapRepository.lagreOppgittTilkytning(behandling, oppgittTilknytningBuilder.build());
            final Optional<MedlemskapAggregat> medlemskapAggregat = medlemskapRepository.hentMedlemskap(behandling);
            medlemskapAggregat.flatMap(MedlemskapAggregat::getOppgittTilknytning).ifPresent(søknadBuilder::medOppgittTilknytning);
        } else {
            søknadBuilder = new SøknadEntitet.Builder();
        }

        return søknadBuilder;
    }

    private void persisterEndringssøknad(MottattDokumentWrapperSøknad wrapper, MottattDokument mottattDokument, Behandling behandling, Optional<LocalDate> gjelderFra) {
        LocalDate mottattDato = mottattDokument.getMottattDato();
        boolean elektroniskSøknad = mottattDokument.getElektroniskRegistrert();

        //Kopier og oppdater søknadsfelter.
        final SøknadEntitet.Builder søknadBuilder = kopierSøknad(behandling);
        byggFelleselementerForSøknad(søknadBuilder, wrapper, elektroniskSøknad, mottattDato, gjelderFra);
        List<Behandling> henlagteBehandlingerEtterInnvilget = behandlingRevurderingRepository.finnHenlagteBehandlingerEtterSisteInnvilgedeIkkeHenlagteBehandling(behandling.getFagsakId());
        if (!henlagteBehandlingerEtterInnvilget.isEmpty()) {
            søknadBuilder.medSøknadsdato(søknadRepository.hentSøknad(henlagteBehandlingerEtterInnvilget.get(0).getId()).getSøknadsdato());
        }

        if (wrapper.getOmYtelse() instanceof Endringssoeknad) { // NOSONAR
            final Endringssoeknad omYtelse = (Endringssoeknad) wrapper.getOmYtelse();
            byggYtelsesSpesifikkeFelterForEndringssøknad(omYtelse, behandling, søknadBuilder);
        }
        søknadBuilder.medErEndringssøknad(true);
        final Søknad søknad = søknadBuilder.build();

        søknadRepository.lagreOgFlush(behandling, søknad);
    }

    private void persisterSøknad(MottattDokumentWrapperSøknad wrapper, MottattDokument mottattDokument, Behandling behandling) {
        LocalDate mottattDato = mottattDokument.getMottattDato();
        boolean elektroniskSøknad = mottattDokument.getElektroniskRegistrert();
        final FamilieHendelseBuilder hendelseBuilder = familieHendelseRepository.opprettBuilderFor(behandling);
        final SøknadEntitet.Builder søknadBuilder = new SøknadEntitet.Builder();
        byggFelleselementerForSøknad(søknadBuilder, wrapper, elektroniskSøknad, mottattDato, Optional.empty());
        if (wrapper.getOmYtelse() != null) {
            byggMedlemskap(wrapper, søknadBuilder, behandling, mottattDato);
        }
        if (skalByggeSøknadAnnenPart(wrapper)) {
            byggSøknadAnnenPart(wrapper, søknadBuilder, behandling);
        }

        byggYtelsesSpesifikkeFelter(wrapper, behandling, søknadBuilder);
        byggOpptjeningsspesifikkeFelter(wrapper, behandling, søknadBuilder);
        SoekersRelasjonTilBarnet soekersRelasjonTilBarnet = getSoekersRelasjonTilBarnet(wrapper);
        if (soekersRelasjonTilBarnet instanceof Foedsel) { // NOSONAR
            byggFødselsrelaterteFelter((Foedsel) soekersRelasjonTilBarnet, hendelseBuilder);
        } else if (soekersRelasjonTilBarnet instanceof Termin) { // NOSONAR
            byggTerminrelaterteFelter((Termin) soekersRelasjonTilBarnet, hendelseBuilder);
        } else if (soekersRelasjonTilBarnet instanceof Adopsjon) { // NOSONAR
            byggAdopsjonsrelaterteFelter((Adopsjon) soekersRelasjonTilBarnet, hendelseBuilder);
        } else if (soekersRelasjonTilBarnet instanceof Omsorgsovertakelse) {
            byggOmsorgsovertakelsesrelaterteFelter((Omsorgsovertakelse) soekersRelasjonTilBarnet, hendelseBuilder, søknadBuilder);
        }
        familieHendelseRepository.lagre(behandling, hendelseBuilder);
        søknadBuilder.medErEndringssøknad(false);
        final RelasjonsRolleType relasjonsRolleType = utledRolle(wrapper.getBruker(), behandling);
        final Søknad søknad = søknadBuilder
            .medFamilieHendelse(familieHendelseRepository.hentAggregat(behandling).getSøknadVersjon())
            .medRelasjonsRolleType(relasjonsRolleType).build();
        søknadRepository.lagreOgFlush(behandling, søknad);
    }

    private RelasjonsRolleType utledRolle(Bruker bruker, Behandling behandling) {
        NavBrukerKjønn kjønn = tpsAdapter.hentBrukerForAktør(behandling.getAktørId())
            .map(Personinfo::getKjønn)
            .orElseThrow(() -> MottattDokumentFeil.FACTORY.dokumentManglerRelasjonsRolleType(behandling.getId()).toException());

        if (bruker == null || bruker.getSoeknadsrolle() == null) {
            return NavBrukerKjønn.MANN.equals(kjønn) ? RelasjonsRolleType.FARA : RelasjonsRolleType.MORA;
        }
        if (ForeldreType.MOR.getKode().equals(bruker.getSoeknadsrolle().getKode()) && erKvinne(kjønn)) {
            return RelasjonsRolleType.MORA;
        }
        if (ForeldreType.FAR.getKode().equals(bruker.getSoeknadsrolle().getKode()) && erMann(kjønn)) {
            return RelasjonsRolleType.FARA;
        }
        if (ForeldreType.MEDMOR.getKode().equals(bruker.getSoeknadsrolle().getKode()) && erKvinne(kjønn)) {
            return RelasjonsRolleType.MEDMOR;
        }
        // TODO: Mangler annen-omsorgsperson ..
        return NavBrukerKjønn.MANN.equals(kjønn) ? RelasjonsRolleType.FARA : RelasjonsRolleType.MORA;
    }

    private boolean erKvinne(NavBrukerKjønn kjønn) {
        return NavBrukerKjønn.KVINNE.equals(kjønn);
    }

    private boolean erMann(NavBrukerKjønn kjønn) {
        return NavBrukerKjønn.MANN.equals(kjønn);
    }

    private boolean erEndring(MottattDokument mottattDokument) {
        return mottattDokument.getDokumentTypeId().equals(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD);
    }

    private void byggYtelsesSpesifikkeFelterForEndringssøknad(Endringssoeknad omYtelse, Behandling behandling, SøknadEntitet.Builder søknadBuilder) {
        oversettOgLagreEndringssøknadPerioder(behandling, omYtelse);
        final YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
        søknadBuilder.medFordeling(ytelseFordelingAggregat.getOppgittFordeling());
    }

    private void byggYtelsesSpesifikkeFelter(MottattDokumentWrapperSøknad skjemaWrapper, Behandling behandling, SøknadEntitet.Builder søknadBuilder) {
        if (skjemaWrapper.getOmYtelse() instanceof Foreldrepenger) { // NOSONAR - ok måte å finne riktig JAXB-type
            final Foreldrepenger omYtelse = (Foreldrepenger) skjemaWrapper.getOmYtelse();

            oversettOgLagreRettighet(behandling, omYtelse);
            oversettOgLagreDekningsgrad(behandling, omYtelse);
            oversettOgLagreFordeling(behandling, omYtelse);

            final YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);

            søknadBuilder.medFordeling(ytelseFordelingAggregat.getOppgittFordeling())
                .medDekningsgrad(ytelseFordelingAggregat.getOppgittDekningsgrad())
                .medRettighet(ytelseFordelingAggregat.getOppgittRettighet());
        }
    }

    private void byggOpptjeningsspesifikkeFelter(MottattDokumentWrapperSøknad skjemaWrapper, Behandling behandling, SøknadEntitet.Builder søknadBuilder) {
        if (skjemaWrapper.getOmYtelse() instanceof Foreldrepenger) { // NOSONAR - ok måte å finne riktig JAXB-type
            final Foreldrepenger omYtelse = (Foreldrepenger) skjemaWrapper.getOmYtelse();
            Opptjening opptjening = omYtelse.getOpptjening();
            if (opptjening != null && (!opptjening.getUtenlandskArbeidsforhold().isEmpty() || !opptjening.getAnnenOpptjening().isEmpty() || !opptjening.getEgenNaering().isEmpty() || nonNull(opptjening.getFrilans()))) {
                inntektArbeidYtelseRepository.lagre(behandling, mapOppgittOpptjening(opptjening));
                søknadBuilder.medOppgittOpptjening(inntektArbeidYtelseRepository
                    .hentAggregat(behandling, null).getOppgittOpptjening().orElseThrow(() -> new IllegalStateException("Oppgitt oppjening ikke funnet")));
            }
        }
    }

    private void oversettOgLagreRettighet(Behandling behandling, Foreldrepenger omYtelse) {
        if (!isNull(omYtelse.getRettigheter())) {
            final OppgittRettighet oppgittRettighet = new OppgittRettighetEntitet(omYtelse.getRettigheter().isHarAnnenForelderRett(),
                omYtelse.getRettigheter().isHarOmsorgForBarnetIPeriodene(), omYtelse.getRettigheter().isHarAleneomsorgForBarnet());
            ytelsesFordelingRepository.lagre(behandling, oppgittRettighet);
        }
    }

    private void oversettOgLagreFordeling(Behandling behandling, Foreldrepenger omYtelse) {
        final List<LukketPeriodeMedVedlegg> perioder = omYtelse.getFordeling() != null ? omYtelse.getFordeling().getPerioder().stream().collect(toList()) : new ArrayList<>();
        final List<OppgittPeriode> oppgittPerioder = new ArrayList<>();
        for (LukketPeriodeMedVedlegg lukketPeriode : perioder) {
            final OppgittPeriodeBuilder oppgittPeriodeBuilder = oversettPeriode(lukketPeriode);
            oppgittPerioder.add(oppgittPeriodeBuilder.build());
        }
        ytelsesFordelingRepository.lagre(behandling, new OppgittFordelingEntitet(oppgittPerioder, omYtelse.getFordeling().isAnnenForelderErInformert()));
    }

    private void oversettOgLagreEndringssøknadPerioder(Behandling behandling, Endringssoeknad omYtelse) {
        final List<LukketPeriodeMedVedlegg> perioder = omYtelse.getFordeling() != null ? omYtelse.getFordeling().getPerioder() : new ArrayList<>();
        final List<OppgittPeriode> oppgittPerioder = new ArrayList<>();
        for (LukketPeriodeMedVedlegg lukketPeriode : perioder) {
            final OppgittPeriodeBuilder oppgittPeriodeBuilder = oversettPeriode(lukketPeriode);
            oppgittPerioder.add(oppgittPeriodeBuilder.build());
        }
        ytelsesFordelingRepository.lagre(behandling, new OppgittFordelingEntitet(oppgittPerioder, hentAnnenForelderErInformert(behandling)));
    }

    private boolean hentAnnenForelderErInformert(Behandling behandling) {
        //Papirsøknad frontend støtter ikke å sette annenForelderErInformert. Kopierer fra førstegangsbehandling
        Optional<Behandling> originalBehandling = behandling.getOriginalBehandling();
        if (!originalBehandling.isPresent()) {
            throw new IllegalArgumentException("Utviklerfeil: Endringssøknad må ha original behandling");
        }
        Søknad originalSøknad = søknadRepository.hentSøknad(originalBehandling.get());
        return originalSøknad.getFordeling().getErAnnenForelderInformert();
    }

    private void oversettOgLagreDekningsgrad(Behandling behandling, Foreldrepenger omYtelse) {
        final Dekningsgrader dekingsgrad = omYtelse.getDekningsgrad().getDekningsgrad();
        if (Integer.toString(OppgittDekningsgradEntitet.ÅTTI_PROSENT).equalsIgnoreCase(dekingsgrad.getKode())) {
            ytelsesFordelingRepository.lagre(behandling, OppgittDekningsgradEntitet.bruk80());
        } else if (Integer.toString(OppgittDekningsgradEntitet.HUNDRE_PROSENT).equalsIgnoreCase(dekingsgrad.getKode())) {
            ytelsesFordelingRepository.lagre(behandling, OppgittDekningsgradEntitet.bruk100());
        }
    }

    private OppgittPeriodeBuilder oversettPeriode(LukketPeriodeMedVedlegg lukketPeriode) {
        final OppgittPeriodeBuilder oppgittPeriodeBuilder = OppgittPeriodeBuilder.ny()
            .medPeriode(
                convertToLocalDate(lukketPeriode.getFom()),
                convertToLocalDate(lukketPeriode.getTom())
            );
        if (lukketPeriode instanceof Uttaksperiode) { // NOSONAR
            final Uttaksperiode periode = (Uttaksperiode) lukketPeriode;
            oversettUttakperiode(oppgittPeriodeBuilder, periode);
        } else if (lukketPeriode instanceof Oppholdsperiode) { // NOSONAR
            oppgittPeriodeBuilder.medÅrsak(kodeverkRepository.finn(OppholdÅrsak.class, ((Oppholdsperiode) lukketPeriode).getAarsak().getKode()));
            oppgittPeriodeBuilder.medPeriodeType(kodeverkRepository.finn(UttakPeriodeType.class, UttakPeriodeType.ANNET.getKode()));
        } else if (lukketPeriode instanceof Overfoeringsperiode) { // NOSONAR
            oppgittPeriodeBuilder.medÅrsak(kodeverkRepository.finn(OverføringÅrsak.class, ((Overfoeringsperiode) lukketPeriode).getAarsak().getKode()));
            oppgittPeriodeBuilder.medPeriodeType(kodeverkRepository.finn(UttakPeriodeType.class, ((Overfoeringsperiode) lukketPeriode).getOverfoeringAv().getKode()));
        } else if (lukketPeriode instanceof Utsettelsesperiode) { // NOSONAR
            Utsettelsesperiode utsettelsesperiode = (Utsettelsesperiode) lukketPeriode;
            oversettUtsettelsesperiode(oppgittPeriodeBuilder, utsettelsesperiode);
        } else { // NOSONAR
            throw new IllegalStateException("Ukjent periodetype.");
        }
        return oppgittPeriodeBuilder;
    }

    private void oversettUtsettelsesperiode(OppgittPeriodeBuilder oppgittPeriodeBuilder, Utsettelsesperiode utsettelsesperiode) {
        String orgNr = utsettelsesperiode.getVirksomhetsnummer();
        if (isNotEmpty(orgNr)) {
            Virksomhet virksomhet = virksomhetTjeneste.hentOgLagreOrganisasjon(orgNr);
            oppgittPeriodeBuilder.medVirksomhet(virksomhet);
        }
        oppgittPeriodeBuilder.medErArbeidstaker(utsettelsesperiode.isErArbeidstaker());
        oppgittPeriodeBuilder.medPeriodeType(kodeverkRepository.finn(UttakPeriodeType.class, utsettelsesperiode.getUtsettelseAv().getKode()));
        oppgittPeriodeBuilder.medÅrsak(kodeverkRepository.finn(UtsettelseÅrsak.class, utsettelsesperiode.getAarsak().getKode()));
    }

    private void oversettUttakperiode(OppgittPeriodeBuilder oppgittPeriodeBuilder, Uttaksperiode periode) {
        oppgittPeriodeBuilder.medPeriodeType(kodeverkRepository.finn(UttakPeriodeType.class, periode.getType().getKode()));
        if (periode.isOenskerSamtidigUttak() != null) {
            oppgittPeriodeBuilder.medSamtidigUttak(periode.isOenskerSamtidigUttak());
        }
        if(periode.isOenskerFlerbarnsdager() != null) {
            oppgittPeriodeBuilder.medFlerbarnsdager(periode.isOenskerFlerbarnsdager());
        }
        if(periode.getSamtidigUttakProsent() != null) {
            oppgittPeriodeBuilder.medSamtidigUttaksprosent(BigDecimal.valueOf(periode.getSamtidigUttakProsent()));
        }
        if (periode instanceof Gradering) {
            Gradering gradering = ((Gradering) periode);

            String orgNr = (String) gradering.getVirksomhetsnummer();
            if (isNotEmpty(orgNr)) {
                Virksomhet virksomhet = virksomhetTjeneste.hentOgLagreOrganisasjon(orgNr);
                oppgittPeriodeBuilder.medVirksomhet(virksomhet);
            }
            oppgittPeriodeBuilder.medErArbeidstaker(gradering.isErArbeidstaker());
            oppgittPeriodeBuilder.medArbeidsprosent(BigDecimal.valueOf(gradering.getArbeidtidProsent()));
        }
        if (periode.getMorsAktivitetIPerioden() != null && !periode.getMorsAktivitetIPerioden().getKode().isEmpty()) {
            oppgittPeriodeBuilder.medMorsAktivitet(kodeverkRepository.finn(MorsAktivitet.class, periode.getMorsAktivitetIPerioden().getKode()));
        }
    }

    private OppgittOpptjeningBuilder mapOppgittOpptjening(Opptjening opptjening) {
        OppgittOpptjeningBuilder builder = OppgittOpptjeningBuilder.ny();
        opptjening.getAnnenOpptjening().forEach(annenOpptjening -> builder.leggTilAnnenAktivitet(mapAnnenAktivitet(annenOpptjening)));
        opptjening.getEgenNaering().forEach(egenNaering -> builder.leggTilEgneNæringer(mapEgenNæring(egenNaering)));
        opptjening.getUtenlandskArbeidsforhold().forEach(arbeidsforhold -> builder.leggTilOppgittArbeidsforhold(mapOppgittUtenlandskArbeidsforhold(arbeidsforhold)));
        if (nonNull(opptjening.getFrilans())) {
            opptjening.getFrilans().getPeriode().forEach(periode -> builder.leggTilAnnenAktivitet(mapFrilansPeriode(periode)));
            builder.leggTilFrilansOpplysninger(mapFrilansOpplysninger(opptjening.getFrilans()));
        }
        return builder;
    }

    private FrilansEntitet mapFrilansOpplysninger(Frilans frilans) {
        FrilansEntitet frilansEntitet = new FrilansEntitet();
        frilansEntitet.setErNyoppstartet(frilans.isErNyoppstartet());
        frilansEntitet.setHarInntektFraFosterhjem(frilans.isHarInntektFraFosterhjem());
        frilansEntitet.setHarNærRelasjon(frilans.isNaerRelasjon());
        frilansEntitet.setFrilansoppdrag(frilans.getFrilansoppdrag()
            .stream()
            .map(fo -> {
                FrilansoppdragEntitet frilansoppdragEntitet = new FrilansoppdragEntitet(fo.getOppdragsgiver(), mapPeriode(fo.getPeriode()));
                frilansoppdragEntitet.setFrilans(frilansEntitet);
                return frilansoppdragEntitet;
            }).collect(Collectors.toList()));
        return frilansEntitet;
    }

    private OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder mapOppgittUtenlandskArbeidsforhold(UtenlandskArbeidsforhold utenlandskArbeidsforhold) {
        OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder builder = OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder.ny();
        Landkoder landkode = kodeverkRepository.finn(Landkoder.class, utenlandskArbeidsforhold.getArbeidsland().getKode());
        builder.medUtenlandskVirksomhet(new UtenlandskVirksomhetEntitet(landkode, utenlandskArbeidsforhold.getArbeidsgiversnavn()));
        builder.medErUtenlandskInntekt(true);
        builder.medArbeidType(ArbeidType.UTENLANDSK_ARBEIDSFORHOLD);

        DatoIntervallEntitet periode = mapPeriode(utenlandskArbeidsforhold.getPeriode());
        builder.medPeriode(periode);
        return builder;
    }

    private AnnenAktivitetEntitet mapFrilansPeriode(Periode periode) {
        DatoIntervallEntitet datoIntervallEntitet = mapPeriode(periode);
        return new AnnenAktivitetEntitet(datoIntervallEntitet, ArbeidType.FRILANSER);
    }

    private AnnenAktivitetEntitet mapAnnenAktivitet(AnnenOpptjening annenOpptjening) {
        DatoIntervallEntitet datoIntervallEntitet = mapPeriode(annenOpptjening.getPeriode());
        AnnenOpptjeningTyper type = annenOpptjening.getType();

        ArbeidType arbeidType = kodeverkRepository.finn(ArbeidType.class, type.getKode());
        return new AnnenAktivitetEntitet(datoIntervallEntitet, arbeidType);
    }

    private List<OppgittOpptjeningBuilder.EgenNæringBuilder> mapEgenNæring(EgenNaering egenNæring) {
        List<OppgittOpptjeningBuilder.EgenNæringBuilder> builders = new ArrayList<>();
        egenNæring.getVirksomhetstype().forEach(virksomhettype -> builders.add(mapEgenNæringForType(egenNæring, virksomhettype)));
        return builders;
    }

    private OppgittOpptjeningBuilder.EgenNæringBuilder mapEgenNæringForType(EgenNaering egenNæring, Virksomhetstyper virksomhettype) {
        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny();
        if (egenNæring instanceof NorskOrganisasjon) {
            NorskOrganisasjon norskOrganisasjon = (NorskOrganisasjon) egenNæring;
            Virksomhet virksomhet = virksomhetTjeneste.hentOgLagreOrganisasjon(norskOrganisasjon.getOrganisasjonsnummer());
            egenNæringBuilder.medVirksomhet(virksomhet);
        } else {
            UtenlandskOrganisasjon utenlandskOrganisasjon = (UtenlandskOrganisasjon) egenNæring;
            Landkoder landkode = kodeverkRepository.finn(Landkoder.class, utenlandskOrganisasjon.getArbeidsland().getKode());
            egenNæringBuilder.medUtenlandskVirksomhet(new UtenlandskVirksomhetEntitet(landkode, utenlandskOrganisasjon.getNavn()));
        }

        // felles
        VirksomhetType virksomhetType = kodeverkRepository.finn(VirksomhetType.class, virksomhettype.getKode());
        egenNæringBuilder.medPeriode(mapPeriode(egenNæring.getPeriode()))
            .medVirksomhetType(virksomhetType);

        Optional<Regnskapsfoerer> regnskapsfoerer = Optional.ofNullable(egenNæring.getRegnskapsfoerer());
        regnskapsfoerer.ifPresent(r -> egenNæringBuilder.medRegnskapsførerNavn(r.getNavn()).medRegnskapsførerTlf(r.getTelefon()));

        egenNæringBuilder.medBegrunnelse(egenNæring.getBeskrivelseAvEndring())
            .medEndringDato(DateUtil.convertToLocalDate(egenNæring.getEndringsDato()))
            .medNyoppstartet(egenNæring.isErNyoppstartet())
            .medNyIArbeidslivet(egenNæring.isErNyIArbeidslivet())
            .medVarigEndring(egenNæring.isErVarigEndring())
            .medNærRelasjon(egenNæring.isNaerRelasjon() == null ? false : egenNæring.isNaerRelasjon());
        if (egenNæring.getNaeringsinntektBrutto() != null) {
            egenNæringBuilder.medBruttoInntekt(new BigDecimal(egenNæring.getNaeringsinntektBrutto()));
        }
        return egenNæringBuilder;
    }

    private DatoIntervallEntitet mapPeriode(Periode periode) {
        LocalDate fom = DateUtil.convertToLocalDate(periode.getFom());
        LocalDate tom = DateUtil.convertToLocalDate(periode.getTom());
        if (tom == null) {
            return DatoIntervallEntitet.fraOgMed(fom);
        }
        return DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    private void byggFødselsrelaterteFelter(Foedsel fødsel, FamilieHendelseBuilder hendelseBuilder) {
        if (fødsel.getFoedselsdato() == null) {
            throw new IllegalArgumentException("Utviklerfeil: Ved fødsel skal det være eksakt én fødselsdato");
        }

        LocalDate fødselsdato = convertToLocalDate(fødsel.getFoedselsdato());
        int antallBarn = fødsel.getAntallBarn();
        List<LocalDate> fødselsdatoene = new ArrayList<>();
        for (int i = 1; i <= antallBarn; i++) {
            fødselsdatoene.add(fødselsdato);
        }

        hendelseBuilder.medAntallBarn(antallBarn);
        for (LocalDate localDate : fødselsdatoene) {
            hendelseBuilder.leggTilBarn(localDate);
        }
    }

    private void byggTerminrelaterteFelter(Termin termin, FamilieHendelseBuilder hendelseBuilder) {
        Objects.requireNonNull(termin.getTermindato(), "Termindato må være oppgitt");

        hendelseBuilder.medAntallBarn(termin.getAntallBarn());
        hendelseBuilder.medTerminbekreftelse(hendelseBuilder.getTerminbekreftelseBuilder()
            .medTermindato(convertToLocalDate(termin.getTermindato()))
            .medUtstedtDato(convertToLocalDate(termin.getUtstedtdato())));
    }

    private void byggOmsorgsovertakelsesrelaterteFelter(Omsorgsovertakelse omsorgsovertakelse, FamilieHendelseBuilder hendelseBuilder, SøknadEntitet.Builder søknadBuilder) {
        List<LocalDate> fødselsdatoene = Arrays.stream(omsorgsovertakelse.getFoedselsdato().toArray())
            .map(date -> convertToLocalDate((XMLGregorianCalendar) date))
            .collect(toList());

        hendelseBuilder.medAntallBarn(omsorgsovertakelse.getAntallBarn());
        final FamilieHendelseBuilder.AdopsjonBuilder familieHendelseAdopsjon = hendelseBuilder.getAdopsjonBuilder()
            .medOmsorgsovertakelseDato(convertToLocalDate(omsorgsovertakelse.getOmsorgsovertakelsesdato()));
        for (LocalDate localDate : fødselsdatoene) {
            hendelseBuilder.leggTilBarn(localDate);
        }
        hendelseBuilder.erOmsorgovertagelse();
        hendelseBuilder.medAdopsjon(familieHendelseAdopsjon);

        // Må også settes på søknad
        søknadBuilder.medFarSøkerType(tolkFarSøkerType(omsorgsovertakelse.getOmsorgsovertakelseaarsak()));
    }

    private FarSøkerType tolkFarSøkerType(Omsorgsovertakelseaarsaker omsorgsovertakelseaarsaker) {
        return kodeverkRepository.finn(FarSøkerType.class, omsorgsovertakelseaarsaker.getKode());
    }


    private void byggAdopsjonsrelaterteFelter(Adopsjon adopsjon, FamilieHendelseBuilder hendelseBuilder) {
        List<LocalDate> fødselsdatoene = Arrays.stream(adopsjon.getFoedselsdato().toArray())
            .map(date -> convertToLocalDate((XMLGregorianCalendar) date))
            .collect(toList());

        hendelseBuilder.medAntallBarn(adopsjon.getAntallBarn());
        final FamilieHendelseBuilder.AdopsjonBuilder familieHendelseAdopsjon = hendelseBuilder.getAdopsjonBuilder()
            .medAnkomstDato(convertToLocalDate(adopsjon.getAnkomstdato()))
            .medErEktefellesBarn(adopsjon.isAdopsjonAvEktefellesBarn())
            .medOmsorgsovertakelseDato(convertToLocalDate(adopsjon.getOmsorgsovertakelsesdato()));
        for (LocalDate localDate : fødselsdatoene) {
            hendelseBuilder.leggTilBarn(localDate);
        }
        hendelseBuilder.medAdopsjon(familieHendelseAdopsjon);
    }

    private void byggMedlemskap(MottattDokumentWrapperSøknad skjema, SøknadEntitet.Builder søknadBuilder, Behandling behandling, LocalDate forsendelseMottatt) {
        Medlemskap medlemskap;
        Ytelse omYtelse = skjema.getOmYtelse();
        LocalDate mottattDato = convertToLocalDate(skjema.getSkjema().getMottattDato());
        OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder = new OppgittTilknytningEntitet.Builder()
            .medOppholdNå(true).medOppgittDato(forsendelseMottatt);

        if (omYtelse instanceof Engangsstønad) { // NOSONAR - ok måte å finne riktig JAXB-type
            medlemskap = ((Engangsstønad) omYtelse).getMedlemskap();
        } else if (omYtelse instanceof Foreldrepenger) { // NOSONAR - ok måte å finne riktig JAXB-type
            medlemskap = ((Foreldrepenger) omYtelse).getMedlemskap();
        } else {
            throw new IllegalStateException("Ytelsestype er ikke støttet");
        }
        Boolean iNorgeVedFoedselstidspunkt = medlemskap.isINorgeVedFoedselstidspunkt();
        oppgittTilknytningBuilder.medOppholdNå(Boolean.TRUE.equals(iNorgeVedFoedselstidspunkt));

        Objects.requireNonNull(medlemskap, "Medlemskap må være oppgitt");

        settOppholdUtlandPerioder(medlemskap, mottattDato, oppgittTilknytningBuilder);
        settOppholdNorgePerioder(medlemskap, mottattDato, oppgittTilknytningBuilder);
        medlemskapRepository.lagreOppgittTilkytning(behandling, oppgittTilknytningBuilder.build());
        final Optional<MedlemskapAggregat> medlemskapAggregat = medlemskapRepository.hentMedlemskap(behandling);
        final Optional<OppgittTilknytning> oppgittTilknytning = medlemskapAggregat.flatMap(MedlemskapAggregat::getOppgittTilknytning);
        oppgittTilknytning.ifPresent(søknadBuilder::medOppgittTilknytning);
    }

    private void settOppholdUtlandPerioder(Medlemskap medlemskap, LocalDate mottattDato, OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder) {
        medlemskap.getOppholdUtlandet().forEach(opphUtl -> {
            boolean tidligereOpphold = convertToLocalDate(opphUtl.getPeriode().getFom()).isBefore(mottattDato);
            oppgittTilknytningBuilder.leggTilOpphold(byggUtlandsopphold(opphUtl, tidligereOpphold));
        });
    }

    private OppgittLandOpphold byggUtlandsopphold(OppholdUtlandet utenlandsopphold, boolean tidligereOpphold) {
        return new OppgittLandOppholdEntitet.Builder()
            .medLand(kodeverkRepository.finn(Landkoder.class, utenlandsopphold.getLand().getKode()))
            .medPeriode(
                DateUtil.convertToLocalDate(utenlandsopphold.getPeriode().getFom()),
                DateUtil.convertToLocalDate(utenlandsopphold.getPeriode().getTom())
            )
            .erTidligereOpphold(tidligereOpphold)
            .build();
    }

    private void settOppholdNorgePerioder(Medlemskap medlemskap, LocalDate mottattDato, OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder) {
        medlemskap.getOppholdNorge().forEach(opphNorge -> {
            boolean tidligereOpphold = convertToLocalDate(opphNorge.getPeriode().getFom()).isBefore(mottattDato);
            OppgittLandOpphold oppholdNorgeSistePeriode = new OppgittLandOppholdEntitet.Builder()
                .erTidligereOpphold(tidligereOpphold)
                .medLand(Landkoder.NOR)
                .medPeriode(
                    convertToLocalDate(opphNorge.getPeriode().getFom()),
                    convertToLocalDate(opphNorge.getPeriode().getTom())
                )
                .build();
            oppgittTilknytningBuilder.leggTilOpphold(oppholdNorgeSistePeriode);
        });
    }

    private SoekersRelasjonTilBarnet getSoekersRelasjonTilBarnet(MottattDokumentWrapperSøknad skjema) {
        SoekersRelasjonTilBarnet relasjonTilBarnet = null;
        Ytelse omYtelse = skjema.getOmYtelse();
        if (omYtelse instanceof Foreldrepenger) { // NOSONAR
            relasjonTilBarnet = ((Foreldrepenger) omYtelse).getRelasjonTilBarnet();
        } else if (omYtelse instanceof Engangsstønad) { // NOSONAR
            relasjonTilBarnet = ((Engangsstønad) omYtelse).getSoekersRelasjonTilBarnet();
        }

        Objects.requireNonNull(relasjonTilBarnet, "Relasjon til barnet må være oppgitt");
        return relasjonTilBarnet;
    }


    private SøknadEntitet.Builder byggFelleselementerForSøknad(SøknadEntitet.Builder søknadBuilder, MottattDokumentWrapperSøknad skjemaWrapper, Boolean elektroniskSøknad, LocalDate forsendelseMottatt, Optional<LocalDate> gjelderFra) {
        søknadBuilder.medElektroniskRegistrert(elektroniskSøknad)
            .medMottattDato(forsendelseMottatt)
            .medBegrunnelseForSenInnsending(skjemaWrapper.getBegrunnelseForSenSoeknad())
            .medTilleggsopplysninger(skjemaWrapper.getTilleggsopplysninger())
            .medSøknadsdato(gjelderFra.orElse(forsendelseMottatt));

        for (Vedlegg vedlegg : skjemaWrapper.getPåkrevdVedleggListe()) {
            byggSøknadVedlegg(søknadBuilder, vedlegg, true);
        }

        for (Vedlegg vedlegg : skjemaWrapper.getIkkePåkrevdVedleggListe()) {
            byggSøknadVedlegg(søknadBuilder, vedlegg, false);
        }

        return søknadBuilder;
    }

    private boolean skalByggeSøknadAnnenPart(MottattDokumentWrapperSøknad skjema) {
        AnnenForelder annenForelder = null;
        if (skjema.getOmYtelse() instanceof Foreldrepenger) {
            annenForelder = ((Foreldrepenger) skjema.getOmYtelse()).getAnnenForelder();
            return (annenForelder != null);
        } else if (skjema.getOmYtelse() instanceof Engangsstønad) {
            annenForelder = ((Engangsstønad) skjema.getOmYtelse()).getAnnenForelder();
        }
        return (annenForelder != null);
    }

    private void byggSøknadAnnenPart(MottattDokumentWrapperSøknad skjema, SøknadEntitet.Builder søknadBuilder, Behandling behandling) {
        OppgittAnnenPartBuilder oppgittAnnenPartBuilder = new OppgittAnnenPartBuilder();

        AnnenForelder annenForelder = null;

        if (skjema.getOmYtelse() instanceof Foreldrepenger) {
            annenForelder = ((Foreldrepenger) skjema.getOmYtelse()).getAnnenForelder();
        } else if (skjema.getOmYtelse() instanceof Engangsstønad) {
            annenForelder = ((Engangsstønad) skjema.getOmYtelse()).getAnnenForelder();
        }

        if (annenForelder instanceof AnnenForelderMedNorskIdent) { // NOSONAR - ok måte å finne riktig JAXB-type
            AnnenForelderMedNorskIdent annenForelderMedNorskIdent = (AnnenForelderMedNorskIdent) annenForelder;
            oppgittAnnenPartBuilder.medAktørId(new AktørId(annenForelderMedNorskIdent.getAktoerId()));

        } else if (annenForelder instanceof AnnenForelderUtenNorskIdent) { // NOSONAR - ok måte å finne riktig JAXB-type
            AnnenForelderUtenNorskIdent annenForelderUtenNorskIdent = (AnnenForelderUtenNorskIdent) annenForelder;
            oppgittAnnenPartBuilder.medUtenlandskFnr(annenForelderUtenNorskIdent.getUtenlandskPersonidentifikator());
            Optional<String> funnetLandkode = Optional.ofNullable(annenForelderUtenNorskIdent.getLand()).map(Land::getKode);
            funnetLandkode.ifPresent(s -> oppgittAnnenPartBuilder.medUtenlandskFnrLand(kodeverkRepository.finn(Landkoder.class, s)));
        }

        personopplysningRepository.lagre(behandling, oppgittAnnenPartBuilder);
        final Optional<OppgittAnnenPart> annenPartFraSøknad = personopplysningRepository.hentPersonopplysninger(behandling).getOppgittAnnenPart();
        annenPartFraSøknad.ifPresent(søknadBuilder::medSøknadAnnenPart);

    }

    private void byggSøknadVedlegg(SøknadEntitet.Builder søknadBuilder, Vedlegg vedlegg, boolean påkrevd) {
        SøknadVedleggEntitet.Builder vedleggBuilder = new SøknadVedleggEntitet.Builder()
            .medErPåkrevdISøknadsdialog(påkrevd)
            .medInnsendingsvalg(tolkInnsendingsvalg(vedlegg.getInnsendingstype()))
            .medSkjemanummer(vedlegg.getSkjemanummer())
            .medTilleggsinfo(vedlegg.getTilleggsinformasjon());
        søknadBuilder.leggTilVedlegg(vedleggBuilder.build());
    }

    private Innsendingsvalg tolkInnsendingsvalg(Innsendingstype innsendingstype) {
        // FIXME (MAUR) Slå opp mot kodeverk..
        switch (innsendingstype.getKode()) {
            case "IKKE_VALGT":
                return Innsendingsvalg.IKKE_VALGT;
            case "LASTET_OPP":
                return Innsendingsvalg.LASTET_OPP;
            case "SEND_SENERE":
                return Innsendingsvalg.SEND_SENERE;
            case "SENDES_IKKE":
                return Innsendingsvalg.SENDES_IKKE;
            case "VEDLEGG_ALLEREDE_SENDT":
                return Innsendingsvalg.VEDLEGG_ALLEREDE_SENDT;
            case "VEDLEGG_SENDES_AV_ANDRE":
                return Innsendingsvalg.VEDLEGG_SENDES_AV_ANDRE;
            default:
                return null;
        }
    }

    private boolean isNotEmpty(String orgNr) {
        return !isNull(orgNr) && !orgNr.isEmpty();
    }

}
