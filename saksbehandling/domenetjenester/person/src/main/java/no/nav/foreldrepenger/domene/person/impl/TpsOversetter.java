package no.nav.foreldrepenger.domene.person.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Gyldighetsperiode;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Personhistorikkinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.PersonstatusPeriode;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.StatsborgerskapPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.geografisk.SpråkKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kjoenn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personstatus;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Spraak;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.log.util.LoggerUtils;

@ApplicationScoped
public class TpsOversetter {

    private static final Logger log = LoggerFactory.getLogger(TpsOversetter.class);

    private NavBrukerKodeverkRepository navBrukerKodeverkRepository;
    private BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository;
    private SpråkKodeverkRepository språkKodeverkRepository;
    private TpsAdresseOversetter tpsAdresseOversetter;

    TpsOversetter() {
        // for CDI proxy
    }

    @Inject
    public TpsOversetter(NavBrukerKodeverkRepository navBrukerKodeverkRepository,
                         BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository,
                         SpråkKodeverkRepository språkKodeverkRepository,
                         TpsAdresseOversetter tpsAdresseOversetter) {

        this.navBrukerKodeverkRepository = navBrukerKodeverkRepository;
        this.behandlingsgrunnlagKodeverkRepository = behandlingsgrunnlagKodeverkRepository;
        this.språkKodeverkRepository = språkKodeverkRepository;
        this.tpsAdresseOversetter = tpsAdresseOversetter;
    }

    private no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder utledLandkode(Statsborgerskap statsborgerskap) {
        no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder landkode = no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder.UDEFINERT;
        if (Optional.ofNullable(statsborgerskap).isPresent()) {
            landkode = behandlingsgrunnlagKodeverkRepository.finnLandkode(statsborgerskap.getLand().getValue());
        }
        return landkode;
    }

    public Personinfo tilBrukerInfo(AktørId aktørId, Bruker bruker) { // NOSONAR - ingen forbedring å forkorte metoden her
        String navn = bruker.getPersonnavn().getSammensattNavn();
        String adresse = tpsAdresseOversetter.finnAdresseFor(bruker);
        String adresseLandkode = tpsAdresseOversetter.finnAdresseLandkodeFor(bruker);
        String utlandsadresse = tpsAdresseOversetter.finnUtlandsadresseFor(bruker);

        LocalDate fødselsdato = finnFødselsdato(bruker);
        LocalDate dødsdato = finnDødsdato(bruker);

        Aktoer aktoer = bruker.getAktoer();
        PersonIdent pi = (PersonIdent) aktoer;
        String ident = pi.getIdent().getIdent();
        NavBrukerKjønn kjønn = tilBrukerKjønn(bruker.getKjoenn());
        PersonstatusType personstatus = tilPersonstatusType(bruker.getPersonstatus());

        no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder landkoder = utledLandkode(bruker.getStatsborgerskap());
        Region region = behandlingsgrunnlagKodeverkRepository.finnHøyestRangertRegion(Collections.singletonList(landkoder.getKode()));

        String diskresjonskode = bruker.getDiskresjonskode() == null ? null : bruker.getDiskresjonskode().getValue();
        String geografiskTilknytning = bruker.getGeografiskTilknytning() != null ? bruker.getGeografiskTilknytning().getGeografiskTilknytning() : null;

        List<Adresseinfo> adresseinfoList = tpsAdresseOversetter.lagListeMedAdresseInfo(bruker);
        SivilstandType sivilstandType = bruker.getSivilstand() == null ? null : behandlingsgrunnlagKodeverkRepository.finnSivilstandType(bruker.getSivilstand().getSivilstand().getValue());

        return new Personinfo.Builder()
            .medAktørId(aktørId)
            .medPersonIdent(no.nav.foreldrepenger.domene.typer.PersonIdent.fra(ident))
            .medNavn(navn)
            .medAdresse(adresse)
            .medAdresseLandkode(adresseLandkode)
            .medFødselsdato(fødselsdato)
            .medDødsdato(dødsdato)
            .medNavBrukerKjønn(kjønn)
            .medPersonstatusType(personstatus)
            .medStatsborgerskap(new no.nav.foreldrepenger.behandlingslager.aktør.Statsborgerskap(landkoder.getKode()))
            .medRegion(region)
            .medUtlandsadresse(utlandsadresse)
            .medForetrukketSpråk(bestemForetrukketSpråk(bruker))
            .medGegrafiskTilknytning(geografiskTilknytning)
            .medDiskresjonsKode(diskresjonskode)
            .medAdresseInfoList(adresseinfoList)
            .medSivilstandType(sivilstandType)
            .medLandkode(landkoder)
            .build();
    }

    public Personhistorikkinfo tilPersonhistorikkInfo(String aktørId, HentPersonhistorikkResponse response) {

        Personhistorikkinfo.Builder builder = Personhistorikkinfo
            .builder()
            .medAktørId(aktørId);

        konverterPersonstatusPerioder(response, builder);

        konverterStatsborgerskapPerioder(response, builder);

        tpsAdresseOversetter.konverterBostedadressePerioder(response, builder);

        tpsAdresseOversetter.konverterPostadressePerioder(response, builder);

        tpsAdresseOversetter.konverterMidlertidigAdressePerioder(response, builder);

        return builder.build();
    }

    private void konverterPersonstatusPerioder(HentPersonhistorikkResponse response, Personhistorikkinfo.Builder builder) {
        Optional.ofNullable(response.getPersonstatusListe()).ifPresent(list -> {
            list.forEach(e -> {
                Personstatus personstatus = new Personstatus();
                personstatus.setPersonstatus(e.getPersonstatus());
                PersonstatusType personstatusType = tilPersonstatusType(personstatus);

                Gyldighetsperiode gyldighetsperiode = Gyldighetsperiode.innenfor(
                    DateUtil.convertToLocalDate(e.getPeriode().getFom()),
                    DateUtil.convertToLocalDate(e.getPeriode().getTom()));

                PersonstatusPeriode periode = new PersonstatusPeriode(gyldighetsperiode, personstatusType);
                builder.leggTil(periode);
            });
        });
    }

    private void konverterStatsborgerskapPerioder(HentPersonhistorikkResponse response, Personhistorikkinfo.Builder builder) {
        Optional.ofNullable(response.getStatsborgerskapListe()).ifPresent(list -> {
            list.forEach(e -> {
                Gyldighetsperiode gyldighetsperiode = Gyldighetsperiode.innenfor(
                    DateUtil.convertToLocalDate(e.getPeriode().getFom()),
                    DateUtil.convertToLocalDate(e.getPeriode().getTom()));

                no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder landkoder = behandlingsgrunnlagKodeverkRepository.finnLandkode(e.getStatsborgerskap().getLand().getValue());
                StatsborgerskapPeriode element = new StatsborgerskapPeriode(gyldighetsperiode,
                    new no.nav.foreldrepenger.behandlingslager.aktør.Statsborgerskap(landkoder.getKode()));
                builder.leggTil(element);
            });
        });
    }

    private LocalDate finnDødsdato(Bruker person) {
        LocalDate dødsdato = null;
        Doedsdato dødsdatoJaxb = person.getDoedsdato();
        if (dødsdatoJaxb != null) {
            dødsdato = DateUtil.convertToLocalDate(dødsdatoJaxb.getDoedsdato());
        }
        return dødsdato;
    }

    private LocalDate finnFødselsdato(Bruker person) {
        LocalDate fødselsdato = null;
        Foedselsdato fødselsdatoJaxb = person.getFoedselsdato();
        if (fødselsdatoJaxb != null) {
            fødselsdato = DateUtil.convertToLocalDate(fødselsdatoJaxb.getFoedselsdato());
        }
        return fødselsdato;
    }

    private Språkkode bestemForetrukketSpråk(Bruker person) {
        Språkkode defaultSpråk = Språkkode.nb;
        Spraak språk = person.getMaalform();
        // For å slippe å håndtere foreldet forkortelse "NO" andre steder i løsningen
        if (språk == null || "NO".equals(språk.getValue())) {
            return defaultSpråk;
        }
        Optional<Språkkode> kode = språkKodeverkRepository.finnSpråkMedKodeverkEiersKode(språk.getValue());
        if (kode.isPresent()) {
            return kode.get();
        }
        if (log.isInfoEnabled()) {
            log.info("Mottok ukjent språkkode: '{}'. Defaulter til '{}'", LoggerUtils.removeLineBreaks(språk.getValue()), defaultSpråk.getKode()); //NOSONAR
        }
        return defaultSpråk;
    }

    GeografiskTilknytning tilGeografiskTilknytning(no.nav.tjeneste.virksomhet.person.v3.informasjon.GeografiskTilknytning geografiskTilknytning,
                                                   Diskresjonskoder diskresjonskoder) {
        String geoTilkn = geografiskTilknytning != null ? geografiskTilknytning.getGeografiskTilknytning() : null;
        String diskKode = diskresjonskoder != null ? diskresjonskoder.getValue() : null;
        return new GeografiskTilknytning(geoTilkn, diskKode);
    }

    private NavBrukerKjønn tilBrukerKjønn(Kjoenn kjoenn) {
        return Optional.ofNullable(kjoenn)
            .map(Kjoenn::getKjoenn)
            .map(kj -> navBrukerKodeverkRepository.finnBrukerKjønn(kj.getValue()))
            .orElse(NavBrukerKjønn.UDEFINERT);
    }

    private PersonstatusType tilPersonstatusType(Personstatus personstatus) {
        return navBrukerKodeverkRepository.finnPersonstatus(personstatus.getPersonstatus().getValue());
    }

    public Adresseinfo tilAdresseInfo(Person person) {
        return tpsAdresseOversetter.tilAdresseInfo(person);
    }

    
}

