select * from cmn_users 			-- Retorna os usuários
select * from cmn_units				-- Retorna as unidades
select * from cmn_departments		-- Retorna os Departamentos
select * from cmn_sectors			-- Retorna os setores
select * from cmn_functions			-- Retorna as funções	

select * from cmn_annotations		-- Retorna as anotações do sistema (para todos os módulos)


-- Dados de usuários em uma consulta
select 
	cmn_users.name as 'User',
	cmn_users.login as 	'Login',
	cmn_units.name as 'Unit',
	cmn_departments.name as 'Department',
	cmn_sectors.name as 'Sector',
	cmn_functions.name as 'Funstion',
	case cmn_employments.is_default
		when 0 then 'Adicional'
		when 1 then 'Principal'
	end as 'Status function',
	case cmn_users.category
		when -1 then 'Deletado'
		when 0 then 'Sem Acesso'
		when 1 then 'Público'
		when 2 then 'Explorer'
		when 3 then 'Operador'
		when 4 then 'Administrador'
		when 5 then 'Super Usuário'
	end as 'Category'
	
from cmn_employments
join cmn_users on (cmn_employments.ref_user = cmn_users.id)
join cmn_units on (cmn_employments.ref_unit = cmn_units.id)
join cmn_departments on (cmn_employments.ref_department = cmn_departments.id)
join cmn_sectors on (cmn_employments.ref_sector = cmn_sectors.id)
join cmn_functions on (cmn_employments.ref_function = cmn_functions.id)

where cmn_users.id = 218




-- Catgorias de documentos
select * from dms_categories

-- Eventos de documentos
select * from dms_events where ref_document = 151


-- Consulta de documentos
select 
	dms_documents.id as 'ID Document',
	dms_documents.code as 'Code Document',
	dms_documents.name as 'Name Document',
	dms_documents.version as 'Version',
	case dms_documents.state
		when 0 then 'Disponível'
		when 1 then 'Em Elaboração'
		when 2 then 'Arquivado'
		when 3 then 'Quarentena'
		when 4 then 'Obsoleto'
		when 5 then 'Aguardando Vigência'
		when 6 then 'Lixeira'
		when 7 then ''
		when 8 then ''
		when 9 then ''
		when 10 then 'Aguardando Conversão'
	end as 'Status',
	dms_categories.name as 'Category',
	cmn_users.name as 'Responsavel',
	if(dms_documents.ref_folder = 0, 
		' Repositório',
	   (select dms_folders.name from dms_folders where dms_folders.id = dms_documents.ref_folder)) as 'Folder'

from dms_documents
join dms_categories on (dms_documents.ref_category = dms_categories.id)
join cmn_users on (dms_documents.ref_owner = cmn_users.id)

where dms_documents.code = 'MANUAL-001'



