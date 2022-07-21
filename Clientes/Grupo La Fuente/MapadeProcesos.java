/**
 * Filename: ARTEFACT:MAP:Dashboard:Process
 * 
 * Description: Implementation of the ARTEFACT:MAP:Dashboard:Process script.
 * 
 * Revision: 1.0 - Yuri Silveira Schmitz (ys@interact.com.br) 20/10/2021 - TID-02721 - Script Created.
 * Revision: 2.0 - Yuri Silveira Schmitz (ys@interact.com.br) 20/10/2021 -    ""     - Implementation of the 'PROCESO' screen.
 * Revision: 3.0 - Andreas D. Silva (ads@interact.com.br) 19/11/2021     -    ""     - Implementation of SIPOC table and references screen.
 *
 * Author:      Yuri Silveira Schmitz
 * EMail:       ys@interact.com.br
 * Internet:    www.interact.com.br
 *
 * Arguments: - Name: source || Label: ProcessDefinition || Data Type: Caractere
 *
 *(+) global.properties
 * WEB.Components.Dashboard.Inspect.Lookups=com.interact.sas.bpm.data.ProcessDefinition
 * WEB.Components.Dashboard.Inspect.rule=ARTEFACT:MAP:Dashboard:Process
 *
 * Copyright © 1999-2021 by Interact Solutions Ltda.
 * Rua Carlos Fett Filho, 47/301
 * 95.900.000, LAJEADO, RS
 * BRAZIL
 */

import com.interact.sas.bpm.data.ProcessDefinition;
import com.interact.sas.bpm.data.TreeItem;
import com.interact.sas.bpm.db.BusinessProcessManager;
import com.interact.sas.cmn.ApplicationContext;
import com.interact.sas.cmn.data.Atom;
import com.interact.sas.crt.data.Certification;
import com.interact.sas.web.zk.cmn.parts.DefaultEditorTab;
import com.interact.sas.web.zk.cmn.util.ResourceLocator;
import com.interact.sas.web.zk.ext.sa.ReportMarvel;
import com.interact.sas.web.zk.ext.sa.SystemAutomator;
import com.interact.sas.web.zk.ext.sa.SystemAutomatorShell;
import com.interact.sas.cmn.data.Context;
import com.interact.sas.web.zk.cmn.util.ProtocolHelper;
import com.interact.sas.cmn.db.FileManager;
import com.interact.sas.cmn.fm.FileFamilies;
import com.interact.sas.web.zk.dashboard.util.DashboardContextItem;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.North;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Html;

//VAR
String MAIN_LINK = ApplicationContext.getLocalAddress();
String DASHBOARD_NAME = "MANUALES Y NORMAS CORPORATIVAS";

// this value will come from the clicked item on the first Dashboard
macroProcess = SA.discover( source );

String MACROPROCESS = "1"; // Macroproceso
String PROCESS      = "2"; // Proceso
String FLOWCHART    = "3"; // Flujograma
String SUBPROCESS   = "4"; // Subproceso

// ATRIBUTES
String ATRIBUTE_FAMILY       = "INFORME_POLITICAS";
String PURPOSE               = "PROPOSITO_MACROS";
String BEGINNING             = "INICIO_PROCESSOS";
String END                   = "FIM_PROCESSOS";
String LEGAL_REQUIREMENTS    = "REQ_LEGALES_PROCESSOS";
String DCA                   = "DCA_PROCESSOS";
String SPECIFIC_REQUIREMENTS = "REC_ESPECIFICOS_PROCESSOS";
String SIPOC                 = "SIPOC_PROCESSOS";
String PROCESS_TYPE          = "TIPO_PROCESSOS";
String SISTEMAS              = "SISTEMAS";

Div mainDiv = new Div();
Div processLayoutDiv = new Div();

/**
 * main
 * 
 */
Object main()
{
	mainDiv.setHeight( "100%" );
	mainDiv.setStyle( "background-color: #bfbfbf; overflow: auto;" );
	mainDiv.appendChild( getMacroProcessDetails() );

	return mainDiv;
}

/**
 * getMacroProcessDetails
 * 
 * @return Borderlayout
 */
Borderlayout getMacroProcessDetails()
{
	Label macroProcessLabel = new Label( "MACROPROCESO" );
	macroProcessLabel.setHeight( "100%" );
	macroProcessLabel.setStyle( "display: flex; justify-content: center; align-items: center; font-size: 23px; font-family: verdana;" );

	North north = new North();
	north.setHeight( "70px" );
	north.setStyle( "background-color: #0033CC; color: white;" );
	north.appendChild( macroProcessLabel );

	String processName =  macroProcess != null ? macroProcess.getName() : com.interact.sas.cmn.Naming.NOT_AVAILABLE;

	Label firstChildLabel = new Label( processName );
	firstChildLabel.setHeight( "100%" );
	firstChildLabel.setStyle( "display: flex; justify-content: center; align-items: center; font-size: 23px; font-weight: bold;" );

	Div centerDivFirstChild = new Div();
	centerDivFirstChild.setHeight( "55px" );
	centerDivFirstChild.setStyle( "background-color: #a6a6a6; border-bottom: 2px solid;" );
	centerDivFirstChild.appendChild( firstChildLabel );

	String titleLabelStyle = "display: flex; justify-content: center;" + 
		"align-items: center; font-size: 19px;" + 
		"font-weight: bold;";

	Label purposeTitle = new Label( "Propósito" );
	purposeTitle.setHeight( "70px" );
	purposeTitle.setStyle( titleLabelStyle );

	Label purposeText = new Label( getAtributeValue( macroProcess, PURPOSE ) );
	purposeText.setHeight( "calc(100% - 165px)" );
	purposeText.setStyle( "white-space: normal !important; overflow: auto; " + 
						 "color: #737373; font-family: serif; " + 
						 "font-size: 18px; word-spacing: 5px; " + 
						 "line-height: 30px; margin: 30px 40px 65px 40px; " +
						 "text-align: justify;" );

	Div leftDiv = new Div();
	leftDiv.setWidth( "60%" );
	leftDiv.setStyle( "display: flex; flex-direction: column;" );
	leftDiv.appendChild( purposeTitle );
	leftDiv.appendChild( purposeText );

	Separator separator = new Separator();
	separator.setStyle( "margin: 70px 0px 65px;" );
	separator.setOrient( "vertical" );
	separator.setBar( true );

	Label processListTitle = new Label( "Procesos vinculados" );
	processListTitle.setHeight( "70px" );
	processListTitle.setStyle( titleLabelStyle );

	Div rightDiv = new Div();
	rightDiv.setWidth( "40%" );
	rightDiv.setStyle( "display: flex; flex-direction: column;" );
	rightDiv.appendChild( processListTitle );
	rightDiv.appendChild( getProcessListDiv() );

	Div centerDivSecondChild = new Div();
	centerDivSecondChild.setHeight( "calc(100% - 57px)" );
	centerDivSecondChild.setStyle( "display: flex; flex-direction: row;" );
	centerDivSecondChild.appendChild( leftDiv );
	centerDivSecondChild.appendChild( separator );
	centerDivSecondChild.appendChild( rightDiv );

	Div centerDiv = new Div();
	centerDiv.setHeight( "calc(100% - 5px)" );
	centerDiv.setStyle( "background-color: #ffffff; border: 2px solid; border-radius: 0px 0px 15px 15px;" );
	centerDiv.appendChild( centerDivFirstChild );
	centerDiv.appendChild( centerDivSecondChild );

	Center center = new Center();
	center.setStyle( "background: transparent; padding: 7px 7px 20px;" );
	center.appendChild( centerDiv );

	Borderlayout bLayout = new Borderlayout();
	bLayout.appendChild( north );
	bLayout.appendChild( center );

	return bLayout;
}

/**
 * getProcessDetails
 * 
 * @param process ProcessDefinition
 * @return Div
 */
Div getProcessDetails( ProcessDefinition process )
{
	String centralize = "display: flex; justify-content: center; align-items: center;";

	Borderlayout processBLayout = new Borderlayout();
	{
		Label processLabel = new Label( "PROCESO" );
		processLabel.setHeight( "100%" );
		processLabel.setStyle( centralize + "font-size: 23px; font-family: verdana;" );

		North north = new North();
		north.setHeight( "70px" );
		north.setStyle( "background-color: #0033CC; color: white;" );
		north.appendChild( processLabel );

		Label firstChildLabel = new Label( process.getName() );
		firstChildLabel.setHeight( "100%" );
		firstChildLabel.setStyle( centralize + "font-size: 23px; font-weight: bold;" );

		Div centerDivFirstChild = new Div();
		centerDivFirstChild.setHeight( "55px" );
		centerDivFirstChild.setStyle( "background-color: #a6a6a6; border-bottom: 2px solid;" );
		centerDivFirstChild.appendChild( firstChildLabel );

		String titleLabelStyle = "display: flex; justify-content: center; " + 
			"align-items: center; font-size: 19px; " + 
			"font-weight: bold; color: white; " +
			"background-color: #ba0000;";

		Label startTitleLabel = new Label( "Inicio" );
		startTitleLabel.setHeight( "50px" );
		startTitleLabel.setStyle( titleLabelStyle );

		String atributeValueStyle = "white-space: normal !important; overflow: auto; " + 
			"color: #737373; font-family: serif; " + 
			"font-size: 18px; word-spacing: 5px; " + 
			"line-height: 30px; margin: 30px 40px 65px 40px; " +
			"text-align: justify; height: calc(100% - 145px);";

		Label startAtributeLabel = new Label( getAtributeValue( process, BEGINNING ) );
		startAtributeLabel.setStyle( atributeValueStyle );

		Div leftDiv = new Div();
		leftDiv.setWidth( "50%" );
		leftDiv.setStyle( "display: flex; flex-direction: column; border-right: 1px solid black" );
		leftDiv.appendChild( startTitleLabel );
		leftDiv.appendChild( startAtributeLabel );

		Label endTitleLabel = new Label( "Fin" );
		endTitleLabel.setHeight( "50px" );
		endTitleLabel.setStyle( titleLabelStyle );

		Label endAtributeLabel = new Label( getAtributeValue( process, END ) );
		endAtributeLabel.setStyle( atributeValueStyle);

		Div rightDiv = new Div();
		rightDiv.setWidth( "50%" );
		rightDiv.setStyle( "display: flex; flex-direction: column; border-left: 1px solid black" );
		rightDiv.appendChild( endTitleLabel );
		rightDiv.appendChild( endAtributeLabel );

		Div centerDivSecondChild = new Div();
		centerDivSecondChild.setHeight( "calc(100% - 57px)" );
		centerDivSecondChild.setStyle( "display: flex; flex-direction: row;" );
		centerDivSecondChild.appendChild( leftDiv );
		centerDivSecondChild.appendChild( rightDiv );

		Div centerDiv = new Div();
		centerDiv.setHeight( "calc(100% - 5px)" );
		centerDiv.setStyle( "background-color: #ffffff; border: 2px solid; border-radius: 0px 0px 15px 15px;" );
		centerDiv.appendChild( centerDivFirstChild );
		centerDiv.appendChild( centerDivSecondChild );

		Center center = new Center();
		center.setStyle( "background: transparent; padding: 7px 7px 20px;" );
		center.appendChild( centerDiv );

		processBLayout.appendChild( north );
		processBLayout.appendChild( center );
	}

	Div atributesDiv = new Div();
	{
		String divStyle = "display: flex; flex-direction: row; border: 2px solid; margin: 5px; height: 100px";

		String divTitleStyle = centralize + "background-color: #CCCCFF; border-right: 2px solid; width: 40%";
		String divContentStyle = centralize + "background-color: #ffffff; overflow-y: auto; width: 70%;";

		String labelTitleStyle = centralize + "font-size: 20px; font-weight: bold; white-space: unset !important; padding: 10px;";
		String labelContentStyle = centralize + "white-space: normal !important; color: #737373; font-family: verdana; font-size: 18px; " +
			"word-spacing: 3px; line-height: 25px; text-align: justify; margin: 15px; height: calc(100% - 30px)";

		Label requirementsTitleLabel = new Label( "Requisitos Legales" );
		requirementsTitleLabel.setStyle( labelTitleStyle  );

		Div requirementsTitleDiv = new Div();
		requirementsTitleDiv.setStyle( divTitleStyle );
		requirementsTitleDiv.appendChild( requirementsTitleLabel );

		Label requirementsContentLabel = new Label( getAtributeValue( process, LEGAL_REQUIREMENTS ) );
		requirementsContentLabel.setStyle( labelContentStyle );

		Div requirementsContentDiv = new Div();
		requirementsContentDiv.setStyle( divContentStyle );
		requirementsContentDiv.appendChild( requirementsContentLabel );

		Div requirementsDiv = new Div();
		requirementsDiv.setStyle( divStyle );
		requirementsDiv.appendChild( requirementsTitleDiv );
		requirementsDiv.appendChild( requirementsContentDiv );

		Label dcaTitleLabel = new Label( "DCA" );
		dcaTitleLabel.setStyle( labelTitleStyle  );

		Div dcaTitleDiv = new Div();
		dcaTitleDiv.setStyle( divTitleStyle );
		dcaTitleDiv.appendChild( dcaTitleLabel );

		Label dcaContentLabel = new Label( getAtributeValue( process, DCA ) );
		dcaContentLabel.setStyle( labelContentStyle );

		Div dcaContentDiv = new Div();
		dcaContentDiv.setStyle( divContentStyle );
		dcaContentDiv.appendChild( dcaContentLabel );

		Div dcaDiv = new Div();
		dcaDiv.setStyle( divStyle );
		dcaDiv.appendChild( dcaTitleDiv );
		dcaDiv.appendChild( dcaContentDiv );

		Label resourcesTitleLabel = new Label( "Recursos Específicos" );
		resourcesTitleLabel.setStyle( labelTitleStyle  );

		Div resourcesTitleDiv = new Div();
		resourcesTitleDiv.setStyle( divTitleStyle );
		resourcesTitleDiv.appendChild( resourcesTitleLabel );

		Label resourcesContentLabel = new Label( getAtributeValue( process, SPECIFIC_REQUIREMENTS ) );
		resourcesContentLabel.setStyle( labelContentStyle );

		Div resourcesContentDiv = new Div();
		resourcesContentDiv.setStyle( divContentStyle );
		resourcesContentDiv.appendChild( resourcesContentLabel );

		Div resourcesDiv = new Div();
		resourcesDiv.setStyle( divStyle );
		resourcesDiv.appendChild( resourcesTitleDiv );
		resourcesDiv.appendChild( resourcesContentDiv );
		
		// Sistemas
		
		Label sistemasTitleLabel = new Label( "Sistemas" );
		sistemasTitleLabel.setStyle( labelTitleStyle  );

		Div sistemasTitleDiv = new Div();
		sistemasTitleDiv.setStyle( divTitleStyle );
		sistemasTitleDiv.appendChild( sistemasTitleLabel );

		Label sistemasContentLabel = new Label( getAtributeValue( process, SISTEMAS ) );
		sistemasContentLabel.setStyle( labelContentStyle );

		Div sistemasContentDiv = new Div();
		sistemasContentDiv.setStyle( divContentStyle );
		sistemasContentDiv.appendChild( sistemasContentLabel );

		Div sistemasDiv = new Div();
		sistemasDiv.setStyle( divStyle );
		sistemasDiv.appendChild( sistemasTitleDiv );
		sistemasDiv.appendChild( sistemasContentDiv );
		
		//
		
		atributesDiv.appendChild( requirementsDiv );
		atributesDiv.appendChild( dcaDiv );
		atributesDiv.appendChild( resourcesDiv );
		atributesDiv.appendChild( sistemasDiv );
	}

	processLayoutDiv.getChildren().clear();
	processLayoutDiv.appendChild( processBLayout );
	processLayoutDiv.appendChild( atributesDiv );

	return processLayoutDiv;
}

Div getSipocDetails( ProcessDefinition process )
{
	Div sipocTableDiv = new Div();

	String centralize = "display: flex; justify-content: center; align-items: center;";

	Label sipocTitleLabel = new Label( "SIPOC" );
	sipocTitleLabel.setHeight( "100%" );
	sipocTitleLabel.setStyle( centralize + "font-size: 23px; font-family: verdana;" );

	Div sipocTitleDiv = new Div();
	sipocTitleDiv.setHeight( "70px" );
	sipocTitleDiv.setStyle( "background-color: #0033CC; color: white;" );
	sipocTitleDiv.appendChild( sipocTitleLabel );

	mapping = com.interact.sas.cmn.ModuleContext
		.getInstance()
		.getTagManager()
		.getMapping( process.getTagSubject(), ATRIBUTE_FAMILY + ":" + SIPOC );

	if( mapping == null )  
	{  
		Label nullTable = new Label( "Este proceso no tiene el atributo 'SIPOC' vinculado." );
		nullTable.setStyle( centralize + "font-size: 20px; font-family: verdana;" );
		nullTable.setHeight( "12%" );

		sipocTableDiv.appendChild( sipocTitleDiv );
		sipocTableDiv.appendChild( nullTable );

		return sipocTableDiv;
	} 
	else 
	{
		content = mapping.getContent();
	}

	String sipocTable =com.interact.sas.cmn.util.HtmlToolkit.smartFormat( content ); 

	sipocTable = sipocTable.replace( "style=\"color: #000000\"", "style=\"color: #000000; margin: auto;\"" );

	Html html = new Html();
	html.setContent( sipocTable );

	Div table = new Div();
	table.appendChild( html );

	Label empty = new Label("             ");
	Div espace = new Div();
	espace.appendChild( empty );
	espace.setHeight("30px");


	sipocTableDiv.appendChild( sipocTitleDiv );
	sipocTableDiv.appendChild( table );
	sipocTableDiv.appendChild( espace );

	return sipocTableDiv;
}

Html getFlowchartsHtmlList(ProcessDefinition process)
{
	String htmlprocessFlowchartList = "<ul> \n";
	Html flowchartHtmlList = new Html();

	List protocols = SA.list();

	tasks = com.interact.sas.bpm.ModuleContext
		.getInstance()
		.getBusinessProcessManager()
		.getTaskDefinitionsWithStartEnd( process );

	if( !tasks.isEmpty() )
	{
		for( task : tasks )
		{
			List taskContext = com.interact.sas.cmn.ModuleContext
				.getInstance()
				.getContextManager()
				.getContext( Context.FAMILY_BPM_TASK_DEFINITION_ITEMS, task.getId());

			if( !taskContext.isEmpty() )
			{
				for( context : taskContext )
				{
					String protocol = context.getUrl();

					if( protocol.contains("process") && !protocols.contains( protocol ))
					{
						protocols.add(protocol);
						processDefinition = protocol.contains("definition") ? com.interact.sas.bpm.protocols.ProcessDefinitionProtocolHandler.getInstance().decode( protocol ) : com.interact.sas.bpm.protocols.ProcessDefinitionUpdatedProtocolHandler.getInstance().decode( protocol );
						link = MAIN_LINK + ProtocolHelper.inspect( processDefinition );
						processDefinitionLink = "<a href=\"" + link + "\"target=\"_blank\">" + processDefinition.getName() + "</a>"; 

						htmlprocessFlowchartList += "<li>" + processDefinitionLink + "</li>\n";
					}
				}
			}
		}
	}

	htmlprocessFlowchartList += "</ul>";

	flowchartHtmlList.setContent( htmlprocessFlowchartList );
	return flowchartHtmlList;
}

Html getContextIndicatorsHtmlList( process )
{
	String htmlprocessIndicatorsList = "<ul> \n";

	List protocols = SA.list();

	tasks = com.interact.sas.bpm.ModuleContext
		.getInstance()
		.getBusinessProcessManager()
		.getTaskDefinitionsWithStartEnd( process );

	if( !tasks.isEmpty() )
	{
		for( task : tasks )
		{
			List taskContext = com.interact.sas.cmn.ModuleContext
				.getInstance()
				.getContextManager()
				.getContext( Context.FAMILY_BPM_TASK_DEFINITION_ITEMS, task.getId());

			if( !taskContext.isEmpty() )
			{
				for( context : taskContext )
				{
					String protocol = context.getUrl();

					if( protocol.contains("indicator") && !protocols.contains( protocol ))
					{
						protocols.add(protocol);
						indicator = com.interact.sas.bsc.protocols.IndicatorProtocolHandler.getInstance().decode( protocol );
						link = MAIN_LINK + ProtocolHelper.inspect( indicator );
						indicatorLink = "<a href=\"" + link + "\"target=\"_blank\">" + indicator.getName() + "</a>"; 

						htmlprocessIndicatorsList += "<li>" + indicatorLink + "</li>\n";
					}
				}
			}
		}
	}

	htmlprocessIndicatorsList += "</ul>";

	Html inicatorsHtmlList = new Html();
	inicatorsHtmlList.setContent( htmlprocessIndicatorsList );

	return inicatorsHtmlList;
}

Html getContextDocumentsHtmlList( process )
{
	String htmlprocessDocsList = "<ul> \n";

	List protocols = SA.list();

	tasks = com.interact.sas.bpm.ModuleContext
		.getInstance()
		.getBusinessProcessManager()
		.getTaskDefinitionsWithStartEnd( process );

	if( !tasks.isEmpty() )
	{
		for( task : tasks )
		{
			List taskContext = com.interact.sas.cmn.ModuleContext
				.getInstance()
				.getContextManager()
				.getContext( Context.FAMILY_BPM_TASK_DEFINITION_ITEMS, task.getId());

			if( !taskContext.isEmpty() )
			{
				for( context : taskContext )
				{
					String protocol = context.getUrl();

					if( protocol.contains("dms") && !protocols.contains( protocol ))
					{
						protocols.add(protocol);
						doc = protocol.contains("document") ? com.interact.sas.dms.protocols.DocumentSpecificProtocolHandler.getInstance().decode( protocol ) : com.interact.sas.dms.protocols.DocumentWorkProtocolHandler.getInstance().decode( protocol );
						String link = MAIN_LINK + ProtocolHelper.inspect( doc );
						documentLink = "<a href=\"" + link + "\"target=\"_blank\">" + doc.getName() + "</a>";

						htmlprocessDocsList += "<li>" + documentLink + "</li>\n"; 
					}
				}
			}
		}
	}

	htmlprocessDocsList += "</ul>";

	Html documentsHtmlList = new Html();
	documentsHtmlList.setContent( htmlprocessDocsList );

	return documentsHtmlList;
}

Html getHtmlDashboardLink()
{
	fileManager = com.interact.sas.cmn.ModuleContext.getInstance().getFileManager();

	dashboardFile = fileManager.getFileByName( FileFamilies.FAMILY_DASHBOARD_PLUS, DASHBOARD_NAME );

	if(dashboardFile != null)
	{
		link = MAIN_LINK + ProtocolHelper.inspect( new DashboardContextItem( dashboardFile ) );

		dashboardLink = "<ul>\n<li><a href=\"" + link + "\"target=\"_blank\">" + dashboardFile.getName() + "</a></li>\n</ul>";
	}

	Html dashboardHtmlLink = new Html( dashboardLink );

	return dashboardHtmlLink;
}

Div getReferences( ProcessDefinition process )
{
	String labelStyle = "display: flex; justify-content: center; align-items: center; font-size: 20px; font-family: verdana; color: white; height: 100%;";
	String divStyle = "background-color: #FFC000; border-radius: 15px; height: 10%;";
	String listStyle = "color: black; font-family: serif; font-size: 18px; background: transparent; margin: auto; display: flex; justify-content: center; align-items: center; height: auto";

	//Se possível pegar nome e link do dashboard que será passado
	Label manualesYNormas = new Label("POLÍTICAS, NORMAS Y PROCEDIMIENTOS");;
	manualesYNormas.setStyle( labelStyle );

	manualesyNormasLink = getHtmlDashboardLink();
	manualesyNormasLink.setStyle(listStyle);

	Div manualesYNormasDiv = new Div();
	manualesYNormasDiv.appendChild( manualesYNormas );
	manualesYNormasDiv.setStyle( divStyle );

	Label indicadoresDelProceso = new Label( "INDICADORES DEL PROCESO" );
	indicadoresDelProceso.setStyle( labelStyle );

	indicadoresList = getContextIndicatorsHtmlList( process );
	indicadoresList.setStyle( listStyle );

	Div indicadoresDelProcesoDiv = new Div();
	indicadoresDelProcesoDiv.appendChild( indicadoresDelProceso );
	indicadoresDelProcesoDiv.setStyle( divStyle );

	Label flujogamasDelProceso = new Label( "FLUJOGRAMAS DEL PROCESO" );
	flujogamasDelProceso.setStyle( labelStyle );

	flujogramasList = getFlowchartsHtmlList( process );
	flujogramasList.setStyle( listStyle );

	Div flujogramasDelProcesoDiv = new Div();
	flujogramasDelProcesoDiv.appendChild( flujogamasDelProceso );
	flujogramasDelProcesoDiv.setStyle( divStyle );

	Label documentosDelProceso = new Label( "INSTRUCTIVOS, MANUALES, FORMULARIOS Y OTROS" );
	documentosDelProceso.setStyle( labelStyle );

	documentosList = getContextDocumentsHtmlList( process );
	documentosList.setStyle( listStyle );

	Div documentosDelProcesoDiv = new Div();
	documentosDelProcesoDiv.appendChild( documentosDelProceso );

	documentosDelProcesoDiv.setStyle( divStyle );

	Div references = new Div();
	references.appendChild( manualesYNormasDiv );
	references.appendChild( manualesyNormasLink );
	references.appendChild( indicadoresDelProcesoDiv );
	references.appendChild( indicadoresList );
	references.appendChild( flujogramasDelProcesoDiv );
	references.appendChild( flujogramasList );
	references.appendChild( documentosDelProcesoDiv );
	references.appendChild( documentosList );

	return references; 
}

/**
 * getProcessListDiv
 * 
 * @return Div
 */
Div getProcessListDiv() 
{
	Div processesDiv = new Div();
	processesDiv.setHeight( "calc(100% - 135px)" );
	processesDiv.setStyle( "overflow-y: auto; margin: 0px 10px 65px;" );

	String imageResource = ResourceLocator.getImageResource( "sas/bpm/sb_administrator.png" );

	List processes = getMacroProcessChildren();

	for ( ProcessDefinition process : processes )
	{
		Image processImage = new Image();
		processImage.setHeight( "100%" );
		processImage.setWidth( "100%" );
		processImage.setStyle( "max-width: 72px; max-height: 54px;" );
		processImage.setSrc( imageResource );

		Label processLabel = new Label( process.getName() );
		processLabel.setStyle( "display: flex; align-items: center; " + 
							  "margin-right: 10px; font-size: 14px; " + 
							  "font-weight: bold; white-space: normal !important;" );

		Div processDiv = new Div();
		processDiv.setStyle( "display: flex; flex-direction: row; align-items: center; margin: 10px 0px; cursor: pointer; user-select: none;" );
		processDiv.appendChild( processImage );
		processDiv.appendChild( processLabel );
		processDiv.setAttribute( "source", process );

		processDiv.addEventListener( org.zkoss.zk.ui.event.Events.ON_CLICK, new EventListener() 
									{
										public void onEvent( Event event ) throws Exception 
										{
											mainDivChildren = mainDiv.getChildren();

											if( !mainDivChildren.isEmpty() )
											{					
												divMacroprocessDetail = mainDivChildren.get(0);
												
												mainDivChildren.clear();
												
												mainDiv.appendChild( divMacroprocessDetail );
											}
											mainDiv.appendChild( getProcessDetails( (ProcessDefinition) event.getTarget().getAttribute( "source" ) ) );
											mainDiv.appendChild( getSipocDetails( (ProcessDefinition) event.getTarget().getAttribute( "source" ) ) ); 
											mainDiv.appendChild( getReferences( (ProcessDefinition) event.getTarget().getAttribute( "source" ) ) ); 

											Clients.resize( mainDiv );
											Clients.scrollIntoView( processLayoutDiv );
										};
									} );

		processesDiv.appendChild( processDiv );
	}

	return processesDiv;
}

/**
 * List
 * 
 * @return getMacroProcessChildren
 */
List getMacroProcessChildren()
{
	List list = new ArrayList();

	try
	{
		BusinessProcessManager processManager = com.interact.sas.bpm.ModuleContext
			.getInstance()
			.getBusinessProcessManager();

		TreeItem treeItemSource = processManager.getTreeItem( TreeItem.FAMILY_PROCESS, macroProcess.getId() );

		if ( treeItemSource != null )
		{
			List treeItemChildrem = processManager.getChildTreeItems( treeItemSource.getId() );

			if ( treeItemChildrem != null )
			{
				for ( TreeItem treeItem : treeItemChildrem )
				{
					ProcessDefinition processChild = processManager.getProcessDefinition( new Atom( treeItem.getSourceId() ) );

					if ( processChild != null )
					{
						String processType = getAtributeValue( processChild, PROCESS_TYPE );

						if ( processType != null && processType.toLowerCase().trim().equals( PROCESS ) )
						{
							list.add( processChild ); 
						}
					}
				}
			}
		}
	}

	catch ( Exception e )
	{
		logException( "getMacroProcessChildren()", e );
	}

	return list;
}

/**
 * getAtributeValue
 * 
 * @param process ProcessDefinition
 * @param mnemonic String
 * @return String
 */
String getAtributeValue( ProcessDefinition process, String mnemonic ) 
{
	String mappingData = "";

	try
	{
		mappingData = com.interact.sas.cmn.ModuleContext
			.getInstance()
			.getTagManager()
			.getMappingData( process.getTagSubject(), ATRIBUTE_FAMILY + ":" + mnemonic );
	}

	catch ( Exception e )
	{
		logException( "getAtributeValue( ProcessDefinition process, String mnemonic )", e );
	}

	return mappingData;
}

/**
 * logException
 * 
 * @param method String
 * @param exception Exception
 */
void logException( String method, Exception exception )
{
	try
	{
		reportMarvel = SA.report();
		reportMarvel.setDomain( "cmn" );
		reportMarvel.setDate( new java.text.SimpleDateFormat( "dd/MM/yy - HH:mm:ss" ).format( new java.util.Date() ) );
		reportMarvel.setSubject( "[EXCEPTION] " + ApplicationContext.getInstance().getOrganizationName() );
		reportMarvel.addElement( "font-weight: bold;", "ARTEFACT:MAP:Dashboard:Process || TID-02721" );
		reportMarvel.addElement( "An Exception occurred at <span style=\"font-style: italic; font-weight: bold;\">" + method + "</span>" );
		reportMarvel.addException( exception );

		reportMarvel.sendTo( "staff.projects@interact.com.br" );
	}

	catch ( Throwable e )
	{
		print( "[ERRO]: " + e.getMessage() );
	}
}

main();
