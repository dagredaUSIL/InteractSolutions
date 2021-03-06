/**
 * Filename: Envia notificação de processos pendentes por usuário.java
 * 
 * Description: Verifica os processos de cada usuário, gera tabela com os
 *              processos em produção pendentes
 * 
 * Revision: 1.0 -  Andreas D. Silva(ads@interact.com.br) 14/02/2022
 * 
 */

import com.interact.sas.web.zk.cmn.util.ProtocolHelper;
import com.interact.sas.bpm.data.ProcessDefinition;
import com.interact.sas.web.zk.ext.sa.LogMarvel;
import com.interact.sas.cmn.ApplicationContext;
import com.interact.sas.cmn.data.TagMapping;
import com.interact.sas.cmn.data.TagSubject;
import com.interact.sas.bpm.data.TreeItem;
import com.interact.sas.cmn.data.TagKind;
import com.interact.sas.cmn.data.Atom;
import java.util.*;

businessProcessManager = com.interact.sas.bpm.ModuleContext.getInstance().getBusinessProcessManager();
userManager            = com.interact.sas.cmn.ModuleContext.getInstance().getUserManager();
tagManager             = com.interact.sas.cmn.ModuleContext.getInstance().getTagManager();

LogMarvel logs = SA.log();

String KIND_FAMILY = "INFORME_POLITICAS";
String DATA_MNEMONIC = "data_vencimento";
String PROCESS_TYPE_MNEMONIC = "TIPO_PROCESSOS";
String MAIN_LINK = ApplicationContext.getLocalAddress();

void downloadText( String text )
{
    try
    {
        java.io.File temp = java.io.File.createTempFile( "text", ".txt" );
        java.io.BufferedWriter bw = new BufferedWriter( new java.io.FileWriter( temp.getAbsolutePath() ) );
        bw.write( text );
        bw.flush();
        bw.close();
        com.interact.sas.web.zk.cmn.util.FileUtilities.downloadFile( temp );
    }
    catch( Exception e )
    {
        logs.log( "[ERRO] Problema no método downloadText() " + e.getMessage() );
        haveError = true;
    }
}

sendMail( content, user )
{
    report = SA.report();
    report.setDomain( "bpm" );
	report.setSubject( "SA-BPM - Procesos Pendientes" );
    report.addElement( content );
    report.sendTo( user.getMailAddress() );
}

boolean checkSendMail( processes )
{
    for( entry : processes.entrySet() )
    {
        if( entry.getValue() <= 30 )
        {
            return true;
        }   
    }
    return false;
}

String generateMailContent( processes, user )
{
    try
    {
        String content = "<!DOCTYPE html> <style>.classp { font-family: Arial; font-size: 16; }.classtable { border: 1px solid gray; border-collapse: collapse; font-family: Arial;  }.classth { border: 1px solid gray; border-collapse: collapse; font-family: Arial;  background-color: #215caa; color: white; height: auto; width: auto; font-weight: 500; text-align: center; vertical-align: middle;}.classtd { border: 1px solid gray; border-collapse: collapse; font-family: Arial;  background-color: #f2f2f2; text-align: center; vertical-align: middle; word-break: break-all;}.classtr { width: auto; }</style><body><div><p class=\"classp\">Estimado  "+ user.getName() +","+"</p><p class=\"classp\">A continuación se muestra la lista de flujogramas de su responsabilidad que se encuentran vencidos y/o próximos a vencer</p></div><br><div><table class=\"classtable\"><th class=\"classth\" height=\"40\"width=auto>Proceso</th><th class=\"classth\" width=auto>Flujograma</th><th class=\"classth\" width=auto>Fecha de vencimiento</th><th class=\"classth\" width=auto>Plazo de Finalización</th>";
        for( entry : processes.entrySet() )
        {
            String color = "#15c900";

            if( entry.getValue() < 0 )
            {
                color = "#ff0000";
            }

            process = businessProcessManager.getProcessDefinition( new Atom( entry.getKey() ) );

            parentTreeItem = businessProcessManager.getParentTreeItem( TreeItem.FAMILY_PROCESS, process.getId() );

            String parentProcessName = "<p>-</p>";

            parentProcess = null;

            if( parentTreeItem != null )
            {
                parentProcess = businessProcessManager.getProcessDefinition( new Atom(parentTreeItem.getSourceId()) );

                if( parentProcess != null ) { parentProcessName = "<p>"+ parentProcess.getName() +"</p>"; }
            }


            content += "<tr><td class=\"classtd\" height=\"35\">" + parentProcessName + "</td><td class=\"classtd\"><a href=\""+ MAIN_LINK + ProtocolHelper.inspect( process ) +"\">"+ process.getName() +"</a></td><td class=\"classtd\" style=\"color:"+ color +"\">"+ getFinalDate( process ) +"</td><td class=\"classtd\" style=\"color:"+ color +"\">"+ entry.getValue() +"</td></tr>";
        }

        content += "</table></div><p class=\"classp\">Por lo anterior, solicitamos su gestión para:</p><ol><li class=\"classp\">Utilizar el link de la tabla de este correo para visualizar el contenido de cada uno de los flujogramas y definir el paso a seguir respecto a su actualización</li><li class=\"classp\">Gestionar la actualización del flujograma, según corresponda:  <ul><li class=\"classp\">Si el flujograma continúa vigente, debe notificar vía a correo electrónico a Gestión por Procesos;</li><li class=\"classp\">Si el flujograma requiere cambios, debes crear en Suite SA la solicitud de actualización del flujograma.</li></ul></li></ol><p class=\"classp\">Cualquier inquietud al respecto favor comunicarse con Gestión por Procesos</p></body>";
        logs.log("Content generated");
        return content;
    }
    catch( Exception e )
    {
        logs.log("ERRO GENERATING CONTENT - " + ProtocolHelper.inspect( parentProcess ) + "  " + parentProcess.getName() +"  " + process.getName() + "  " + entry.getValue() );
    }
}

String formatDate( String content ) throws Exception
{   
    String[] splitedDate = content.split( "-" );
    String date = splitedDate[2] + "/" + splitedDate[1] + "/" + splitedDate[0];

    return date;
}

String getFinalDate( process )
{
    try
    {
        kind = tagManager.getKind( KIND_FAMILY + ":" + DATA_MNEMONIC );
        subject = process.getTagSubject();
        mapping = tagManager.getMapping( subject, kind );
        date = mapping.getContent();
        
        return formatDate( date );
    }
    catch( Exception e )
    {
        logs.log("ERRO final date: "+ date + " message: " + e.getMessage() );
        return "-";
    }
}

int getDaysLeft( process )
{
    try
    {        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String finalDateString = getFinalDate( process );

        if( finalDateString.equalsIgnoreCase( "-" ) ) 
            return 0;
    
        finalDate = sdf.parse( finalDateString );
        now = Calendar.getInstance().getTime();

        long dt = (finalDate.getTime() - now.getTime()) + 3600000;

        int daysLeft = ( dt / 86400000L );

        return daysLeft;
    }
    catch( Exception e )
    {
        logs.log( "ERRO GETTING DAYS LEFT -" + e.getMessage() );
        return 0;
    }
}

Map orderProcessesToMap( List processes )
{
    Map orderedMap = new LinkedHashMap();
    List daysLeftList = new ArrayList();

    for( process : processes )
    {
        if( process.getStage() == ProcessDefinition.STAGE_PRODUCTION )
        {
            int daysLeft = getDaysLeft( process );
            
            if( daysLeft < 30 )
            {
                daysLeftList.add(daysLeft);
            }
        }
    
    }

    Collections.sort( daysLeftList );

    daysLeftOrderedList = new LinkedList( daysLeftList );

    for( daysLeft : daysLeftOrderedList )
    {
        for( process : processes )
        {
            int processDaysLeft = getDaysLeft( process );

            if( processDaysLeft == daysLeft )
            {
                orderedMap.put( process.getId(), daysLeft );
            }
        }
    }

    return orderedMap;
}

List getUserProcesses( user )
{
    List processes = SA.list();
    List userProcesses = businessProcessManager.getProcessDefinitions( user );

    for( process : userProcesses )
    {
        if( process.getOwnerId() == user.getId() && getProcessType(process).equalsIgnoreCase("Flujograma") && !getFinalDate(process).equalsIgnoreCase("-") )
        {
            processes.add( process );
        }
    }
    return processes;
}

String getProcessType( ProcessDefinition process )
{

    String typeKey;
    String type;

    try
    {
        TagSubject tagSubject = process.getTagSubject(); 
        TagKind tagKind = tagManager.getKind( KIND_FAMILY + ":" + PROCESS_TYPE_MNEMONIC );
        TagMapping tagMapping = tagManager.getMapping( tagSubject, tagKind );
	
	    typeKey = tagMapping.getContent();

        if( typeKey.equals( "4" ) )
            type = "Subproceso";

        if( typeKey.equals( "3" ) )
            type = "Flujograma";

        if( typeKey.equals( "2" ) )
            type = "Proceso";

        if( typeKey.equals( "1" ) )
            type = "Macroproceso";

    }
    catch( Exception e )
    {
        type = "No clasificado";
    }
    return type; 
}

doWork()
{
    try
    {
        List users = userManager.getUsers();

        for( user : users )
        {
            String content = "";

            Map processes = SA.map();
            processes.clear();

            List userProcesses = getUserProcesses( user );

            if( !userProcesses.isEmpty() )
            {
                processes = orderProcessesToMap( userProcesses );

                if( !processes.isEmpty() )
                {
                    boolean sendMail = checkSendMail( processes );

                    if( sendMail )
                    {            
                        content = generateMailContent( processes, user );
                
                        if( content != null )
                        {
                            sendMail( content, user );
                        }
                    }
                }
            }
        }
    }
    catch( Exception e )
    {
        downloadText( logs.dump() );
    } 
}

doWork();