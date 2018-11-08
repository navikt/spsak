package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

public enum BeregningsgrunnlagHjemmel {
    F_14_7,
    F_14_7_8_30,
    F_14_7_8_35,
    F_14_7_8_38,
    F_14_7_8_40,
    F_14_7_8_41,
    F_14_7_8_42,
    F_14_7_8_43,
    F_14_7_8_47,
    F_14_7_8_49;
    
    public static final BeregningsgrunnlagHjemmel HJEMMEL_BARE_ARBEIDSTAKER = F_14_7_8_30;
    public static final BeregningsgrunnlagHjemmel HJEMMEL_BARE_SELVSTENDIG = F_14_7_8_35;
    public static final BeregningsgrunnlagHjemmel HJEMMEL_BARE_FRILANSER = F_14_7_8_38;
    public static final BeregningsgrunnlagHjemmel HJEMMEL_ARBEIDSTAKER_OG_FRILANSER = F_14_7_8_40;
    public static final BeregningsgrunnlagHjemmel HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG = F_14_7_8_41;
    public static final BeregningsgrunnlagHjemmel HJEMMEL_FRILANSER_OG_SELVSTENDIG = F_14_7_8_42;
    public static final BeregningsgrunnlagHjemmel HJEMMEL_ARBEIDSTAKER_OG_FRILANSER_OG_SELVSTENDIG = F_14_7_8_43;

}
