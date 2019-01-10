package no.nav.foreldrepenger.web.app.exceptions;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktKode;
import no.nav.vedtak.feil.Feil;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger log = LoggerFactory.getLogger(ConstraintViolationMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();

        if (constraintViolations.isEmpty() && exception instanceof ResteasyViolationException) {
            String message = ((ResteasyViolationException) exception).getException().getMessage();
            log.error(message);
            Feil feil = FeltValideringFeil.FACTORY.feilIOppsettAvFeltvalidering();
            return lagResponse(new FeilDto(FeilType.GENERELL_FEIL, feil.getFeilmelding()));
        }

        Collection<FeltFeilDto> feilene = new ArrayList<>();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            String kode = getKode(constraintViolation.getLeafBean());
            String feltNavn = getFeltNavn(constraintViolation.getPropertyPath());
            feilene.add(new FeltFeilDto(feltNavn, constraintViolation.getMessage(), kode));
        }
        List<String> feltNavn = feilene.stream().map(felt -> felt.getNavn()).collect(Collectors.toList());

        Feil feil = FeltValideringFeil.FACTORY.feltverdiKanIkkeValideres(feltNavn);
        feil.log(log);
        return lagResponse(new FeilDto(feil.getFeilmelding(), feilene));
    }

    private static Response lagResponse(FeilDto entity) {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(entity)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String getKode(Object leafBean) {
        return leafBean instanceof AksjonspunktKode ? ((AksjonspunktKode) leafBean).getKode() : null;
    }

    private String getFeltNavn(Path propertyPath) {
        return propertyPath instanceof PathImpl ? ((PathImpl) propertyPath).getLeafNode().toString() : null;
    }

}
