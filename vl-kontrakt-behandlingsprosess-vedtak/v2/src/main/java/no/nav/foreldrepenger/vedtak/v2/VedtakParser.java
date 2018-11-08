package no.nav.foreldrepenger.vedtak.v2;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.es.v2.BeregningsgrunnlagEngangsstoenad;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.PersonopplysningerEngangsstoenad;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.PersonopplysningerForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.uttak.fp.v2.UttakForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.v2.Vedtak;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagAdopsjon;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagFoedsel;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagMedlemskap;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagSoekersopplysningsplikt;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagSoeknadsfrist;
import no.nav.vedtak.felles.xml.vedtak.ytelse.fp.v2.YtelseForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.ytelse.es.v2.YtelseEngangsstoenad;

public class VedtakParser {

    static final Class<?>[] CLASSES = {Vedtak.class,
            //ES klasser
            VilkaarsgrunnlagFoedsel.class,
            VilkaarsgrunnlagAdopsjon.class,
            VilkaarsgrunnlagMedlemskap.class,
            VilkaarsgrunnlagSoeknadsfrist.class,
            VilkaarsgrunnlagSoekersopplysningsplikt.class,
            BeregningsgrunnlagEngangsstoenad.class,
            YtelseEngangsstoenad.class,
            PersonopplysningerEngangsstoenad.class,
            //FP klasser:
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagFoedsel.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagAdopsjon.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagMedlemskap.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagOpptjening.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagSoeknadsfrist.class,
            no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagSoekersopplysningsplikt.class,
            BeregningsgrunnlagForeldrepenger.class,
            YtelseForeldrepenger.class,
            UttakForeldrepenger.class,
            PersonopplysningerForeldrepenger.class
    };

    public static Vedtak unmarshall(String xml) throws JAXBException, XMLStreamException {
        return unmarshall(new StreamSource(new StringReader(xml)));
    }

    public static Vedtak unmarshall(InputStream inputStream) throws JAXBException, XMLStreamException {
        return unmarshall(new StreamSource(inputStream));
    }

    public static Vedtak unmarshall(Source source) throws JAXBException, XMLStreamException {
        return JaxbHelper.unmarshalXMLWithStAX(Vedtak.class, source, CLASSES);
    }

    public static String marshall(Object jaxbObject) throws JAXBException, SAXException {
        return JaxbHelper.marshalJaxb(Vedtak.class, jaxbObject, CLASSES);
    }

    public static String marshallAndValidate(Object jaxbObject) throws JAXBException, SAXException {
        return JaxbHelper.marshalAndValidateJaxb(Vedtak.class, jaxbObject, ForeldrepengerVedtakConstants.XSD_LOCATION, CLASSES);
    }
}

