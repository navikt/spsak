--PK-43460
CREATE OR REPLACE PROCEDURE MIGRER_KODELISTE_FK (p_KODELISTE IN VARCHAR2, p_kodeverk IN VARCHAR2)
IS
  r_table_name VARCHAR2(50 CHAR) := p_KODELISTE;
  l_table varchar2(100 char);
  l_table_short varchar2(25 char);
  l_constraint_name VARCHAR2(100 char);
  l_col_name varchar2(100 char);
  sql_stmt varchar2(4000);
  l_kodeverk varchar2(100);
  l_num number(19,0);
  name_in_use exception; --declare a user defined exception
  pragma exception_init( name_in_use, -02264 ); --bind the error code to the above 
  
  column_exists exception;
  pragma exception_init( column_exists, -01430 );  
   
  CURSOR fk_refs IS
    select ac.table_name, ac.constraint_name, cols.column_name --, status, owner
    FROM all_constraints ac
      inner join all_cons_columns cols on cols.constraint_name  = ac.constraint_name and cols.owner=ac.owner
    WHERE r_owner = sys_context( 'userenv', 'current_schema' ) 
    AND ac.constraint_type = 'R'
    and ac.r_constraint_name in
     (
       select constraint_name from all_constraints
       where constraint_type in ('P', 'U')
       AND table_name = r_table_name
       and owner = sys_context( 'userenv', 'current_schema' ) 
     )
    ORDER BY ac.table_name, ac.constraint_name;
  
BEGIN
  
  l_num := 80;
  OPEN fk_refs;
  LOOP
     FETCH fk_refs INTO l_table, l_constraint_name, l_col_name;
     EXIT WHEN fk_refs%NOTFOUND;

     l_kodeverk := 'KL_' || p_kodeverk;
 
     BEGIN
	     sql_stmt := 'alter table ' || l_table || ' add (' || l_kodeverk || ' varchar2(100 CHAR) as (''' || p_kodeverk|| '''))';
	     DBMS_OUTPUT.put_line(sql_stmt);
	     EXECUTE IMMEDIATE sql_stmt;
	 EXCEPTION WHEN column_exists THEN 
	     NULL;
     END;
     
     sql_stmt := 'alter table ' || l_table || ' modify ('|| l_col_name ||' varchar2(100 CHAR))';
     DBMS_OUTPUT.put_line(sql_stmt);
     EXECUTE IMMEDIATE sql_stmt;
     
     l_table_short := SUBSTR(l_table,1,LEAST(24,LENGTH(l_table)));
     LOOP
       BEGIN
         sql_stmt := 'ALTER TABLE ' || l_table || ' ADD CONSTRAINT FK_' || l_table_short || '_' || l_num || ' FOREIGN KEY (' || l_kodeverk || ', ' || l_col_name || ') REFERENCES KODELISTE (kodeverk, kode)';
         DBMS_OUTPUT.put_line(sql_stmt);
         EXECUTE IMMEDIATE sql_stmt;
         EXIT;
       EXCEPTION WHEN name_in_use THEN
           l_num := l_num + 1;
           CONTINUE;
       END;
     END LOOP;
     
     sql_stmt := 'ALTER TABLE ' || l_table || ' DROP CONSTRAINT ' || l_constraint_name;
     DBMS_OUTPUT.put_line(sql_stmt);
     EXECUTE IMMEDIATE sql_stmt;
         
     sql_stmt := 'UPDATE KODEVERK set beskrivelse=(select comments from all_tab_comments where owner=sys_context(''USERENV'', ''CURRENT_SCHEMA'') and table_name=''' || p_KODELISTE || ''') WHERE kode='''|| p_KODEVERK || ''' ';
     DBMS_OUTPUT.put_line(sql_stmt);
     EXECUTE IMMEDIATE sql_stmt;
 
     
  END LOOP;
  
  CLOSE fk_refs;
END;
/