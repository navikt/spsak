alter table IAY_EGEN_NAERING add varig_endring VARCHAR2(1 CHAR) DEFAULT 'N';
COMMENT ON COLUMN IAY_EGEN_NAERING.varig_endring IS 'Om det i søknaden er angitt varig endring i næring';
