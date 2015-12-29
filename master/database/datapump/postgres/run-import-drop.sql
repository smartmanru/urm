DO $$
DECLARE
	sname RECORD;
BEGIN
	FOR sname IN SELECT schema_name FROM information_schema.schemata LOOP
		if sname.schema_name NOT LIKE 'pg_%' and sname.schema_name <> 'information_schema' then
			execute 'drop schema ' || sname.schema_name || ' cascade';
		end if;
	END LOOP;
END $$;
