package no.nav.vedtak.feil.deklarasjon;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.feil.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunksjonellFeil {
    String feilkode();

    String feilmelding();

    String løsningsforslag();

    LogLevel logLevel() default LogLevel.WARN;

    Class<? extends FunksjonellException> exceptionClass() default FunksjonellException.class;
}
