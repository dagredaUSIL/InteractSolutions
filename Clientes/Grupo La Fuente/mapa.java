import com.interact.sas.bpm.data.ProcessInstance;
import com.interact.sas.dms.db.DocumentManager;
import com.interact.sas.dms.data.DocumentFolder;
import com.interact.sas.web.zk.cmn.ApplicationContext;
import com.interact.sas.web.zk.cmn.util.ProtocolHelper;

pi = CONTEXT.getProcessInstance();

serial = pi.getSerial();
name = pi.getName();
client = CONTEXT.getAttribute( "NomeCliente" );
childFolderName = serial + "-" + name;

dm = com.interact.sas.dms.ModuleContext.getInstance().getDocumentManager();

	if(!client.equals("Interact"))
	{
				//pega a pasta pai no SA-DM
				idCliente = CONTEXT.getAttribute("idCliente");
				print(idCliente);

				account = SA.fetch1("crm:account","SELECT \n" +
								 "      map.ref_source \n" +
								 "from cmn_tag_mappings\n" +
								 "map join cmn_tag_kinds kind on (map.ref_kind = kind.id)join\n" +
								 "cmn_tag_families family on (kind.ref_family = family.id)\n" +
								 "where kind.mnemonic = 'CANAIS.CRM.CMN_UNITS'\n" + //alterar o nome do atributo
								 "and family.mnemonic = 'CANAIS.CRM'\n" + //alterar o nome da familia
								 "and map.type = 1000\n" +
								 "and map.content =  '" + idCliente + "'");


				idAccount = account.getId();

				folders = SA.fetch1("dms:folder", "select \n" + 
										"map.ref_source from cmn_tag_mappings \n" + 
										"map  join cmn_tag_kinds kind \n" + 
										"join cmn_tag_families family on (kind.ref_family = family.id)\n" + 
										"where kind.mnemonic = 'CANAIS.CRM.CONTA'\n" + 
										"and family.mnemonic = 'account'\n" + 
										"and map.type = 200\n" + 
										"and map.content =  '" + idAccount + "'");

				folder = folders.getId();
				print(folder);
				//Pega a pastas definidas
				parentFolder = dm.getFolder( folder );

				folder = dm.getFolderByName( "Customizações e Ordem de Serviços", parentFolder.getId());
				print(folder.getId());
				
				folder_2 = dm.getFolderByName( "Ordens de Serviços - OS", folder.getId());

				print(folder_2.getId());

				childFolder = dm.getFolderByName( childFolderName, folder_2.getId() );
				
		

					//caso a pasta n exista, cria a pasta da intância 
					if ( childFolder == null ) 
					{
						childFolder = new DocumentFolder( childFolderName );
						dm.addFolder( folder_2, childFolder );
					}
	

}
