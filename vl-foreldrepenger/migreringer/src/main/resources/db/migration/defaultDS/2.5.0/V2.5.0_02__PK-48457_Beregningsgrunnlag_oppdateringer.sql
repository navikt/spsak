
-- sporing av regelinput

alter table BEREGNINGSGRUNNLAG add (regelinput_skjaringstidspunkt CLOB);
alter table BEREGNINGSGRUNNLAG add (regelinput_brukers_status CLOB);
alter table BEREGNINGSGRUNNLAG_PERIODE add (regel_input CLOB);
alter table BEREGNINGSGRUNNLAG_PERIODE add (regel_input_fastsett CLOB);
