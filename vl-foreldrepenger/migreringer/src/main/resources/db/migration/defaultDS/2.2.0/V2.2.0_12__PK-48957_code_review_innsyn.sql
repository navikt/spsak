alter table INNSYN drop constraint FK_INNSYN_DOKUMENT;
alter table INNSYN drop column INNSYN_DOKUMENT_ID;

alter table INNSYN_DOKUMENT add constraint FK_INNSYN_DOKUMENT_01 foreign key (INNSYN_ID) references INNSYN (ID);
