﻿insert into main.urm_object_entity ( param_object_id , paramentity_type , custom , use_props , app_table , id_field , object_type , meta_object_id , meta_objectversion_type , data_objectversion_type,  version ) values ( 3 , 111 , 'yes' , 'yes' , null , null , 1 , 2 , 2 , 2 , 101 );
insert into main.urm_object_entity ( param_object_id , paramentity_type , custom , use_props , app_table , id_field , object_type , meta_object_id , meta_objectversion_type , data_objectversion_type,  version ) values ( 3 , 112 , 'yes' , 'yes' , null , null , 1 , 2 , 2 , 2 , 101 );
update main.urm_object_version set version = 101 where owner_object_id = 1;
commit;
