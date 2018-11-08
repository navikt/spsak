RENAME SO_FORDELING TO YF_FORDELING;
RENAME SO_FORDELING_PERIODE TO YF_FORDELING_PERIODE;

RENAME SEQ_SO_FORDELING TO SEQ_YF_FORDELING;
RENAME SEQ_SO_FORDELING_PERIODE TO SEQ_YF_FORDELING_PERIODE;

ALTER TABLE GR_YTELSES_FORDELING
  ADD (
  overstyrt_fordeling_id NUMBER,
  bekreftet_fordeling_id NUMBER
  );

ALTER TABLE GR_YTELSES_FORDELING
  ADD CONSTRAINT FK_GR_YTELSES_FORDELING_7 FOREIGN KEY (overstyrt_fordeling_id) REFERENCES YF_FORDELING (ID);
ALTER TABLE GR_YTELSES_FORDELING
  ADD CONSTRAINT FK_GR_YTELSES_FORDELING_8 FOREIGN KEY (bekreftet_fordeling_id) REFERENCES YF_FORDELING (ID);


ALTER TABLE YF_FORDELING_PERIODE
  ADD (
  begrunnelse VARCHAR2(4000 CHAR)
  );
