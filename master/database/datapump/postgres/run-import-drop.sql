DECLARE
	sname RECORD;
BEGIN
	FOR sname IN SELECT schema_name FROM information_schema.schemata LOOP
		if sname LIKE 'pg_%' then
			execute 'drop schema ' || sname.schema_name;
		end if;
	END LOOP;
END;
