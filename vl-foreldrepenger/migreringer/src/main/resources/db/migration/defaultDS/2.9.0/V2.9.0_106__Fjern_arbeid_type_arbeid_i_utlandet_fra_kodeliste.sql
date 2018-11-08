-- Fiks for at punkt 7007 skal plukkes opp av batch-jobb og bli tatt av vent
UPDATE AKSJONSPUNKT_DEF SET AKSJONSPUNKT_TYPE='AUTO' WHERE KODE='7007';

