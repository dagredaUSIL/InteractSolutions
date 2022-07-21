/**
 * Nome do Script: Busca riscos/controles/auditorias e alimenta tabela custom.java
 * 
 * Descrição: Coleta dados da estrutura de riscos e alimenta tabela cus_risk_results
 * 
 * Criado em:  30/06/2022
 * 
 * Autor: João Pedro Kipper - (jpk) - jpk@interact.com.br - (TID-03485) Script created
 * 
 * V: 1.0
 *
 */

import java.util.ArrayList;
import java.util.List;

import com.interact.sas.web.zk.orm.util.RiskCalculator.riskResult;
import com.interact.sas.web.zk.orm.util.RiskCalculator.RiskInfo;
import com.interact.sas.orm.db.ControlTechniqueResultManager;
import com.interact.sas.cmn.db.services.DatabaseConnection;
import com.interact.sas.orm.db.StructureCategoryManager;
import com.interact.sas.web.zk.orm.util.RiskCalculator;
import com.interact.sas.orm.db.ControlTechniqueManager;
import com.interact.sas.orm.db.OperationalRiskManager;
import com.interact.sas.orm.data.StructureCategory;
import com.interact.sas.orm.db.BusinessUnitManager;
import com.interact.sas.web.zk.ext.sa.QueryMarvel;
import com.interact.sas.orm.db.AuditResultManager;
import com.interact.sas.orm.data.ProbabilityRange;
import com.interact.sas.orm.db.ProbabilityManager;
import com.interact.sas.orm.data.ControlTechnique;
import com.interact.sas.cmn.db.services.Database;
import com.interact.sas.orm.data.OperationalRisk;
import com.interact.sas.orm.db.RiskResultManager;
import com.interact.sas.orm.db.RiskFactorManager;
import com.interact.sas.web.zk.ext.sa.LogMarvel;
import com.interact.sas.orm.db.AuditPlanManager;
import com.interact.sas.cmn.db.CategoryManager;
import com.interact.sas.orm.db.BaselineManager;
import com.interact.sas.cmn.ApplicationContext;
import com.interact.sas.orm.db.RiskMapManager;
import com.interact.sas.orm.data.BusinessUnit;
import com.interact.sas.orm.data.ImpactLevel;
import com.interact.sas.orm.data.RiskResult;
import com.interact.sas.orm.data.RiskFactor;
import com.interact.sas.cmn.data.TagSubject;
import com.interact.sas.cmn.data.TagMapping;
import com.interact.sas.orm.db.RiskManager;
import com.interact.sas.orm.data.AuditPlan;
import com.interact.sas.cmn.db.TagManager;
import com.interact.sas.cmn.data.Category;
import com.interact.sas.orm.AuditResult;
import java.sql.PreparedStatement;

ControlTechniqueResultManager controlTechniqueResultManager 	= com.interact.sas.orm.ModuleContext.getInstance().getControlTechniqueResultManager();
StructureCategoryManager structureCategoryManager 				= com.interact.sas.orm.ModuleContext.getInstance().getStructureCategoryManager();
ControlTechniqueManager controlTechniqueManager 				= com.interact.sas.orm.ModuleContext.getInstance().getControlTechniqueManager();
OperationalRiskManager operationalRiskManager 					= com.interact.sas.orm.ModuleContext.getInstance().getOperationalRiskManager();
BusinessUnitManager businessUnitManager 						= com.interact.sas.orm.ModuleContext.getInstance().getBusinessUnitManager();
ProbabilityManager probabilityManager 							= com.interact.sas.orm.ModuleContext.getInstance().getProbabilityManager();
AuditResultManager auditResultManager 	 						= com.interact.sas.orm.ModuleContext.getInstance().getAuditResultManager();
RiskResultManager riskResultManager		 						= com.interact.sas.orm.ModuleContext.getInstance().getRiskResultManager();
AuditPlanManager auditPlanManager 		 						= com.interact.sas.orm.ModuleContext.getInstance().getAuditPlanManager();
CategoryManager categoryManager 								= com.interact.sas.cmn.ModuleContext.getInstance().getCategoryManager();
BaselineManager baselineManager 								= com.interact.sas.orm.ModuleContext.getInstance().getBaselineManager();
RiskMapManager riskMapManager 									= com.interact.sas.orm.ModuleContext.getInstance().getRiskMapManager();
RiskManager riskManager 										= com.interact.sas.orm.ModuleContext.getInstance().getRiskManager();
TagManager tagManager 											= com.interact.sas.cmn.ModuleContext.getInstance().getTagManager();

RiskFactorManager riskFactorManager = com.interact.sas.orm.ModuleContext.getInstance().getRiskFactorManager();

String SISTEMA_DE_RIESGO_MNEMONIC = "SISTEMAS_DE_RIESGOS:SISTEMA_DE_RIESGO";
int TAG_OPTION_ID = 1; //select * from cmn_tag_options
String TABLE = "cus_risk_results";
String[] columnsName = null;

//LOGS
boolean sendLogs = true;
boolean debug = false;
LogMarvel logs = SA.log();
logs.recipients("jpk@interact.com.br");
logs.log("[INFO] REGISTROS DE LOG" );
logs.log("[INFO] SCRIPT - Alimenta Tabela cus_risk_results");
logs.log("[INFO] CLIENTE - " + ApplicationContext.getInstance().getOrganizationName() );
logs.log(" ");

class UnitBaseline
{
	String name;
	int rev;
	int unit;
}

/**
 * Busca as baselines
 *
 * @return ArrayList<UnitBaseline>
 * @throws Exception
 */
List getBaselines()
{
	LinkedList baselines = new LinkedList();
	List result = SA.list();

	result = SA.query( "select rev, ref_unit, name, dt_registered from orm_baselines order by dt_registered desc" ).fetch();

	for( Object items : result )
	{
		baselines.add( getUnitBaseline( items ) );
	}

	return baselines;
}

/**
 * Converte o resultado de uma query em um objeto UnitBaseline
 *
 * @param items Object[]
 * @return UnitBaseline
 * @throws Exception
 */
UnitBaseline getUnitBaseline( Object[] items ) throws Exception
{

	UnitBaseline baseline = new UnitBaseline();

	String rev              = items[0];
	String unit             = items[1];
	String name             = items[2] + " - " + items[3];

	baseline.rev            = SA.cint( rev );
	baseline.unit           = SA.cint( unit );
	baseline.name           = name;

	return baseline;
}

/**
 * Busca o nome da baseline
 *
 * @return List
 * @throws Exception
 */
String getBaselineName( int rev, int unit )
{
	String baselineName = "";
	List baselines = new ArrayList();

	queryResult = SA.query( " select rev, ref_unit, name, dt_registered from orm_baselines " +
						" where rev = " + rev + 
						" and ref_unit = " + unit ).fetch1();
	
	Object[] result = queryResult;

	if( result != null )
	{
		rev           = SA.cint( result[0] );
		ref_unit      = SA.cint( result[1] );
		name          = result[2];
		dt_registered = result[3];

		baselines.add( rev           );
		baselines.add( ref_unit      );
		baselines.add( name          );
		baselines.add( dt_registered );
		
		baselineName = name + " - " + dt_registered;
	}
	else
	{
		baselineName = "Baseline Residual de Segmento";
	}

	return baselineName;
}

class RiskResults
{
	public int revision;
	public String revisionName;
	public String stateName;

	public String inherentImpactLabel;
	public String residualImpactLabel;

	public String inherentProbabilityLabel;
	public String residualProbabilityLabel;

	public double inherentImpact;
	public int inherentImpactLevel;

	public double inherentProbability;
	public int inherentProbabilityLevel;

	public double residualImpact;
	public int residualImpactLevel;

	public double residualProbability;
	public int residualProbabilityLevel;

	public RiskResults()
	{
	}

	public void setRevision( int value )
	{
		this.revision = value;
	}

	public int getRevision()
	{
		return this.revision;
	}

	public void setStateName( String name )
	{
		this.stateName = name;
	}

	public String getStateName()
	{
		return this.stateName;
	}

	public void setRevisionName( String name )
	{
		this.revisionName = name;
	}

	public String getRevisionName()
	{
		return this.revisionName;
	}

	/**
     * 
     * @param label String
     */
	public void setResidualImpactLabel( String label )
	{
		this.residualImpactLabel = label;
	}

	/**
     * 
     * @return String
     */
	public String getResidualImpactLabel()
	{
		return this.residualImpactLabel;
	}

	/**
     * 
     * @param label String
     */
	public void setResidualProbabilityLabel( String label )
	{
		this.residualProbabilityLabel = label;
	}

	/**
     * 
     * @return String
     */
	public String getResidualProbabilityLabel()
	{
		return this.residualProbabilityLabel;
	}

	/**
     * 
     * @param value double
     */
	public void setResidualImpact( double value )
	{
		this.residualImpact = value;
	}

	/**
     * 
     * @return double
     */
	public double getResidualImpact()
	{
		return this.residualImpact;
	}

	/**
     * 
     * @param value double
     */
	public void setResidualProbability( double value )
	{
		this.residualProbability = value;
	}

	/**
     * 
     * @return double
     */
	public double getResidualProbability()
	{
		return this.residualProbability;
	}

	/**
     * 
     * @param value int
     */
	public void setResidualImpactLevel( int value )
	{
		this.residualImpactLevel = value;
	}

	/**
     * 
     * @return int
     */
	public int getResidualImpactLevel()
	{
		return this.residualImpactLevel;
	}

	/**
     * 
     * @param value int
     */
	public void setResidualProbabilityLevel( int value )
	{
		this.residualProbabilityLevel = value;
	}

	/**
     * 
     * @return int
     */
	public int getResidualProbabilityLevel()
	{
		return this.residualProbabilityLevel;
	}

	/**
     * 
     * @param label String
     */
	public void setInherentImpactLabel( String label )
	{
		this.inherentImpactLabel = label;
	}

	/**
     * 
     * @return String
     */
	public String getInherentImpactLabel()
	{
		return this.inherentImpactLabel;
	}

	/**
     * 
     * @param label String
     */
	public void setInherentProbabilityLabel( String label )
	{
		this.inherentProbabilityLabel = label;
	}

	/**
     * 
     * @return String
     */
	public String getInherentProbabilityLabel()
	{
		return this.inherentProbabilityLabel;
	}

	/**
     * 
     * @param value double
     */
	public void setInherentImpact( double value )
	{
		this.inherentImpact = value;
	}

	/**
     * 
     * @return double
     */
	public double getInherentImpact()
	{
		return this.inherentImpact;
	}

	/**
     * 
     * @param value double
     */
	public void setInherentProbability( double value )
	{
		this.inherentProbability = value;
	}

	/**
     * 
     * @return double
     */
	public double getInherentProbability()
	{
		return this.inherentProbability;
	}

	/**
     * 
     * @param value int
     */
	public void setInherentImpactLevel( int value )
	{
		this.inherentImpactLevel = value;
	}

	/**
     * 
     * @return int
     */
	public int getInherentImpactLevel()
	{
		return this.inherentImpactLevel;
	}

	/**
     * 
     * @param value int
     */
	public void setInherentProbabilityLevel( int value )
	{
		this.inherentProbabilityLevel = value;
	}

	/**
     * 
     * @return int
     */
	public int getInherentProbabilityLevel()
	{
		return this.inherentProbabilityLevel;
	}

	/**
     * 
     * @return int
     */
	public int calculateInherentNivel()
	{
		if( inherentImpactLevel == 0 ) inherentImpactLevel = 1;
		if( inherentProbabilityLevel == 0 ) inherentProbabilityLevel = 1;

		return this.inherentImpactLevel * this.inherentProbabilityLevel;
	}

	/**
     * 
     * @return int
     */
	public int calculateResidualNivel()
	{
		if( residualImpactLevel == 0 ) residualImpactLevel = 1;
		if( residualProbabilityLevel == 0 ) residualProbabilityLevel = 1;

		return this.residualImpactLevel * this.residualProbabilityLevel;
	}
}

/**
 * 
 * @param processID int
 * @param riskID int
 * @param controlID int
 * @param revision int
 * @return control
 */
List getControlByRevision( int processID, int riskID, int controlID, int revision )
{
	List control = new ArrayList();

	sql = " select distinct " +
	" risk_control.rev, " + 
	" control_result.ref_process, " +
	" risk_control.ref_risk, " +
	" risk_control.ref_control, " +
	" control_result.score as score_control_result, " +
	" control_result.ref_owner, " +
	" control_result.id as control_result_ID, " +
	" control_result.ref_audit " +

	" from orm_risks_controls risk_control " +

	" join orm_control_results control_result on ( control_result.ref_control = risk_control.ref_control " + 
	" and control_result.rev = risk_control.rev and control_result.ref_process = risk_control.ref_sc ) " +

	" where control_result.ref_process = " + processID + " and" +
	" risk_control.ref_risk = " + riskID + " and" +
	" control_result.ref_control = " + controlID + " and" +
	" control_result.rev = " + revision + 
	" order by control_result_ID desc";

	queryResult = SA.query( sql ).fetch1();

	//Object[] result = (Object[]) queryResult.get( 0 );
	//Object[] result = (Object[]) queryResult;
	//SA.prompt("QUERY: " + queryResult + "\n" + sql);
	if( queryResult != null )
	{
		int rev                       = SA.cint(    queryResult[0] );
		int ref_process               = SA.cint(    queryResult[1] );
		int ref_risk                  = SA.cint(    queryResult[2] );
		int ref_control               = SA.cint(    queryResult[3] );
		double score_control_result   = SA.cdouble( queryResult[4] );
		int ref_owner                 = SA.cint(    queryResult[5] );
		int control_result_ID         = SA.cint(    queryResult[6] );
		int ref_audit                 = SA.cint(    queryResult[7] );

		control.add( rev                  );
		control.add( ref_process          );
		control.add( ref_risk             );
		control.add( ref_control          );
		control.add( score_control_result );
		control.add( ref_owner            );
		control.add( control_result_ID    );
		control.add( ref_audit            );
	}
	else
	{
		control.add( "n/d" );
		control.add( "n/d" );
		control.add( "n/d" );
		control.add( "n/d" );
		control.add( "n/d" );
		control.add( "n/d" );
		control.add( "n/d" );
		control.add( "n/d" );
	}

	return control;
}

/**
 * 
 * @param process StructureCategory
 * @param risk OperationalRisk
 * @param control ControlTechnique
 * @param revision int
 * @param ref_audit int
 * @return n/d
 */
List getAuditResult( StructureCategory process, OperationalRisk risk, ControlTechnique control, int revision, int refAudit )
{
	List result = new ArrayList();	
	probabilityCategories = probabilityManager.getProbabilities();
	countAudit = 0;
	auditPlanName = auditPlanManager.getAuditPlan( refAudit );
	probOption = 0;

	for( probabilityCategory : probabilityCategories )
	{
		if( auditPlanName == null )
		{
			audResult = auditResultManager.getAuditResultByRevision( process, risk, control, probabilityCategory, revision );
			 
			if( countAudit == 0 )
			{
				logs.log("");
				logs.log("Audit_Plan --------------------------: n/d" );
				
				result.add( "n/d" );
			}

			if( audResult != null )
			{
				probOption = audResult.getOptionId();

				if( probOption != 0 )
				{
					logs.log("PROBABILITY_NAME --------------------: " + probabilityManager.getProbabilityOption( probOption ) );
					probName = probabilityManager.getProbabilityOption( probOption );
					result.add( probName.getName() );
				}
				else
				{
					result.add( "n/d" );
				}
			}
			else
			{
				if( countAudit == 0 )
				{
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					
					result.add( "n/d" );
					result.add( "n/d" );
					result.add( "n/d" );
					result.add( "n/d" );
					result.add( "n/d" );
				}
			}
		
			// else
			// {
			// 	if( countAudit == 0 )
			// 	{
			// 		logs.log("");
			// 		logs.log("Audit_Plan --------------------------: n/d" );
			// 		logs.log("PROBABILITY_NAME --------------------: n/d" );
			// 		logs.log("PROBABILITY_NAME --------------------: n/d" );
			// 		logs.log("PROBABILITY_NAME --------------------: n/d" );
			// 		logs.log("PROBABILITY_NAME --------------------: n/d" );
			// 		logs.log("PROBABILITY_NAME --------------------: n/d" );

			// 		result.add( "n/d" );
			// 		result.add( "n/d" );
			// 		result.add( "n/d" );
			// 		result.add( "n/d" );
			// 		result.add( "n/d" );
			// 		result.add( "n/d" );
			// 	}
			// }

			countAudit++;
		}
		else
		{
			//AuditResult
			auditResult = auditResultManager.getAuditResultByRevision( auditPlanName, process, risk, control, probabilityCategory, revision );
		
			if( auditResult != null )
			{
				if( auditResult.getOptionId() != 0 )
				{
					if( countAudit == 0 )
					{
						logs.log( "Audit_Plan -------------------------: " + auditPlanName );

						auditPlanName.equals("n/d") ? result.add( auditPlanName ) : result.add( auditPlanName.getName() );
											
						countAudit++;
					}

					logs.log("PROBABILITY_NAME --------------------: " + probabilityManager.getProbabilityOption( auditResult.getOptionId() ) );

					optionName = probabilityManager.getProbabilityOption( auditResult.getOptionId() );

					result.add( optionName.getName() );
				}
				else
				{
					if( countAudit == 0 )
					{
						logs.log("");
						logs.log("Audit_Plan --------------------------: n/d" );
						logs.log("PROBABILITY_NAME --------------------: n/d" );
						logs.log("PROBABILITY_NAME --------------------: n/d" );
						logs.log("PROBABILITY_NAME --------------------: n/d" );
						logs.log("PROBABILITY_NAME --------------------: n/d" );
						logs.log("PROBABILITY_NAME --------------------: n/d" );
					
						result.add( "n/d" );
						result.add( "n/d" );
						result.add( "n/d" );
						result.add( "n/d" );
						result.add( "n/d" );
						result.add( "n/d" );

						countAudit++;
					}			
				}
			}
			else
			{
				if( countAudit == 0 )
				{
					logs.log("");
					logs.log("Audit_Plan --------------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
					logs.log("PROBABILITY_NAME --------------------: n/d" );
				
					result.add( "n/d" );
					result.add( "n/d" );
					result.add( "n/d" );
					result.add( "n/d" );
					result.add( "n/d" );
					result.add( "n/d" );

					countAudit++;
				}			
			}		
		}

		// if( auditPlanName == null && countAudit == 0 && auditResult == null )
		// {
		// 	logs.log("");
		// 	logs.log("Audit_Plan --------------------------: n/d" );
		// 	logs.log("PROBABILITY_NAME --------------------: n/d" );
		// 	logs.log("PROBABILITY_NAME --------------------: n/d" );
		// 	logs.log("PROBABILITY_NAME --------------------: n/d" );
		// 	logs.log("PROBABILITY_NAME --------------------: n/d" );
		// 	logs.log("PROBABILITY_NAME --------------------: n/d" );
			
		// 	result.add( "n/d" );
		// 	result.add( "n/d" );
		// 	result.add( "n/d" );
		// 	result.add( "n/d" );
		// 	result.add( "n/d" );
		// 	result.add( "n/d" );

		// 	countAudit++;
		// }
	}

	return result;
}

/**
 *
 * @param unidade Category
 * @param macroprocess BusinessUnit
 * @param process StructureCategory
 * @param subprocess StructureCategory
 * @param risk OperationalRisk
 * @param riskResult RiskResults
 * @param revision int
 * @param aud int
 * @return preparedList
 */
public List obtainPreparedList( Category unidade, BusinessUnit macroprocess, StructureCategory process, StructureCategory subprocess, OperationalRisk risk, RiskResults riskResult, int revision, int aud )
{
	List controls = new ArrayList();
	List auditResult  = new ArrayList();
	TagSubject subject = risk.getTagSubject();
	TagMapping tagMapping = tagManager.getMapping( subject, SISTEMA_DE_RIESGO_MNEMONIC );
	List listResult = new ArrayList();

	inerente = riskResultManager.getInherentRiskResult( subprocess, risk );
	RiskCalculator firstRiskCalculator = new RiskCalculator();
	firstRiskInfo = firstRiskCalculator.calculateRisk( subprocess, risk, true );

	residualDeSegm = riskResultManager.getEffectiveRiskResult( subprocess, risk );
	RiskCalculator lastRiskCalculator = new RiskCalculator();
	lastRiskInfo = lastRiskCalculator.calculateRisk( subprocess, risk );

	if( tagMapping == null )
	{
		mapping = "n/d";
	}
	else
	{
		mapping = tagMapping.toString();
	}

	if( aud == -1 )
	{
		logs.log("LOG -1");
		logs.log("[OK] Revision: " + riskResult.getRevision() );
		logs.log("[OK] Revision Name -------------------------: " + riskResult.getRevisionName() );
		logs.log("[OK] Revision State Name -------------------: " + riskResult.getStateName() );
		logs.log("[OK] Unidade -------------------------------: " + unidade );
		logs.log("[OK] Unidade ID ----------------------------: " + unidade.getId() );
		logs.log("[OK] Macroprocesso -------------------------: " + macroprocess );
		logs.log("[OK] Macroprocesso ID ----------------------: " + macroprocess.getId() );
		logs.log("[OK] Processo ------------------------------: " + process );
		logs.log("[OK] Processo ID ---------------------------: " + process.getId() );
		logs.log("[OK] Subprocesso ---------------------------: " + subprocess );
		logs.log("[OK] Subprocesso ID ------------------------: " + subprocess.getId() );
		logs.log("[OK] Risco ---------------------------------: " + risk );
		logs.log("[OK] Risco ID ------------------------------: " + risk.getId() );
		logs.log("[OK] Risco CODE ----------------------------: " + risk.getCode() );
		logs.log("");
		logs.log("[INFO] InerenteImpact ----------------------: " + inerente.getImpact() );
		logs.log("[INFO] InerenteProbability -----------------: " + inerente.getProbability() );
		logs.log("[INFO] InerenteLevel -----------------------: " + firstRiskInfo.getInherentLevel() );
		logs.log("[INFO] InerenteImpactField -----------------: " + firstRiskInfo.getInherentImpact() );
		logs.log("[INFO] InerenteImpactValue -----------------: " + firstRiskInfo.getInherentImpactValue() );
		logs.log("[INFO] InerenteProbabilityField ------------: " + firstRiskInfo.getInherentProbability() );
		logs.log("[INFO] InerenteProbabilityValue ------------: " + firstRiskInfo.getInherentProbabilityValue() );
		logs.log("");
		//logs.log("[INFO] Impact: ");
		logs.log("[INFO] Inerente -----------------------------: " + riskResult.getInherentImpact() );
		logs.log("[INFO] Label nível Inerente -----------------: " + riskResult.getInherentImpactLabel() );
		logs.log("[INFO] Level Inerente -----------------------: " + riskResult.getInherentImpactLevel() );
		logs.log("[INFO] Residual -----------------------------: " + riskResult.getResidualImpact() );
		logs.log("[INFO] Label nível Residual -----------------: " + riskResult.getResidualImpactLabel() );
		logs.log("[INFO] Level Residual -----------------------: " + riskResult.getResidualImpactLevel() );
		logs.log("");
		//logs.log("[INFO] Probabilidade: ");
		logs.log("[INFO] Inerente -----------------------------: " + riskResult.getInherentProbability() );
		logs.log("[INFO] Label nível Inerente -----------------: " + riskResult.getInherentProbabilityLabel() );
		logs.log("[INFO] Level Inerente -----------------------: " + riskResult.getInherentProbabilityLevel() );
		logs.log("[INFO] Residual -----------------------------: " + riskResult.getResidualProbability() );
		logs.log("[INFO] Label nível Residual -----------------: " + riskResult.getResidualProbabilityLabel() );
		logs.log("[INFO] Level Residual -----------------------: " + riskResult.getResidualProbabilityLevel() );
		logs.log("");
		//logs.log("[INFO] Níveis ---------------------: ");
		logs.log("[INFO] Inerente -----------------------------: " + riskResult.calculateInherentNivel() );
		logs.log("[INFO] Residual -----------------------------: " + riskResult.calculateResidualNivel() );
		logs.log("");
		logs.log("[INFO] ResidualSegmImpact -------------------: " + residualDeSegm.getImpact() );
		logs.log("[INFO] ResidualSegmProbability --------------: " + residualDeSegm.getProbability() );
		logs.log("[INFO] ResidualSegmLevel --------------------: " + lastRiskInfo.getResidualLevel() );
		logs.log("[INFO] ResidualSegmImpactField --------------: " + lastRiskInfo.getResidualImpact() );
		logs.log("[INFO] ResidualSegmImpactValue --------------: " + lastRiskInfo.getResidualImpactValue() );
		logs.log("[INFO] ResidualSegmProbabilityField ---------: " + lastRiskInfo.getResidualProbability() );
		logs.log("[INFO] ResidualSegmProbabilityValue ---------: " + lastRiskInfo.getResidualProbabilityValue() );
		logs.log("");

		logs.log("[INFO] Sistema de Risco --------------: " + mapping );

		logs.log("");
		logs.log("Controle -----------------------------: n/d" );
		logs.log("Controle ID --------------------------: n/d" );
		logs.log("Controle CODE ------------------------: n/d" );
		logs.log("CONTROL RESULT -----------------------: n/d" );
		logs.log("CONTROL QUALIFICATION ----------------: n/d" );
		logs.log("");
		logs.log("Audit_Plan ---------------------------: n/d" );
		logs.log("PROBABILITY_NAME ---------------------: n/d" );
		logs.log("PROBABILITY_NAME ---------------------: n/d" );
		logs.log("PROBABILITY_NAME ---------------------: n/d" );
		logs.log("PROBABILITY_NAME ---------------------: n/d" );
		logs.log("PROBABILITY_NAME ---------------------: n/d" );

		logs.log("LOG -1 ---------------------------------------\n\n");	

		if( !debug )
		{
			List list = new ArrayList();

			list.add( riskResult.getRevision()     );  //"Revision     
			list.add( riskResult.getRevisionName() );  //"Revision Name 
			list.add( riskResult.getStateName()    );  //"Revision State Name
			list.add( unidade.getName()            );  //"Unidade 
			list.add( unidade.getId()              );  //"Unidade ID 
			list.add( macroprocess.getName()       );  //"Macroprocesso 
			list.add( macroprocess.getId()         );  //"Macroprocesso ID 
			list.add( process.getName()            );  //"Processo
			list.add( process.getId()              );  //"Processo ID 
			list.add( subprocess.getName()         );  //"Subprocesso 
			list.add( subprocess.getId()           );  //"Subprocesso ID 
			list.add( risk.getName()               );  //"Risco 
			list.add( risk.getId()                 );  //"Risco ID 
			list.add( risk.getCode()               );  //"Risco CODE 

			list.add( inerente.getImpact()                        ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
			list.add( inerente.getProbability()                   ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
			list.add( firstRiskInfo.getInherentLevel()            ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
			list.add( firstRiskInfo.getInherentImpact()           ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
			list.add( firstRiskInfo.getInherentImpactValue()      ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
			list.add( firstRiskInfo.getInherentProbability()      ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
			list.add( firstRiskInfo.getInherentProbabilityValue() ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT

			list.add( riskResult.getInherentImpact()      );  //"Inerente 
			list.add( riskResult.getInherentImpactLabel() );  //"Label nível Inerente
			list.add( riskResult.getInherentImpactLevel() );  //"Level Inerente 
			list.add( riskResult.getResidualImpact()      );  //"Residual  
			list.add( riskResult.getResidualImpactLabel() );  //"Label nível Residual 
			list.add( riskResult.getResidualImpactLevel() );  //"Level Residual   
			list.add( riskResult.getInherentProbability()      );  //"Inerente   
			list.add( riskResult.getInherentProbabilityLabel() );  //"Label nível Inerente       
			list.add( riskResult.getInherentProbabilityLevel() );  //"Level Inerente       
			list.add( riskResult.getResidualProbability()      );  //"Residual 
			list.add( riskResult.getResidualProbabilityLabel() );  //"Label nível Residual    
			list.add( riskResult.getResidualProbabilityLevel() );  //"Level Residual     
			list.add( riskResult.calculateInherentNivel() );  //"Inerente 
			list.add( riskResult.calculateResidualNivel() );  //"Residual 

			list.add( residualDeSegm.getImpact()                 );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
			list.add( residualDeSegm.getProbability()            );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
			list.add( lastRiskInfo.getResidualLevel()            );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
			list.add( lastRiskInfo.getResidualImpact()           );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
			list.add( lastRiskInfo.getResidualImpactValue()      );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
			list.add( lastRiskInfo.getResidualProbability()      );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
			list.add( lastRiskInfo.getResidualProbabilityValue() );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO

			list.add( mapping );  //"Sistema de Risco        
			list.add( "n/d" );  //CONTROL_NAME
			list.add( "n/d" );  //CONTROL_ID
			list.add( "n/d" );  //CONTROL_CODE
			list.add( "n/d" );  //CONTROL_VALUE
			list.add( "n/d" );  //CONTROL_QUALIFICATION
			list.add( "n/d" );  //AUDIT_NAME
			list.add( "n/d" );  //AUDIT_OP1
			list.add( "n/d" );  //AUDIT_OP2
			list.add( "n/d" );  //AUDIT_OP3
			list.add( "n/d" );  //AUDIT_OP4
			list.add( "n/d" );  //AUDIT_OP5

			listResult.add( list );
		}
	}
	else
	{
		controlTechniques = controlTechniqueManager.getControlTechniques( subprocess, risk );

		if( !controlTechniques.isEmpty() )
		{           
			for( ControlTechnique controlTechnique : controlTechniques )
			{
				//controlResults = controlTechniqueResultManager.getControlTechniqueResultsByRevision( subprocess.getId(), controlTechnique.getId(), revision );
				//auditPlans = auditPlanManager.getAuditPlans( subprocess, controlTechnique, risk );
				//for( controlResult : controlResults )
				//{
				controls = getControlByRevision( subprocess.getId(), risk.getId(), controlTechnique.getId(), revision );

				if( controls.get( 3 ) != "n/d" )
				{
					rev                  = (int)    controls.get( 0 );
					ref_process          = (int)    controls.get( 1 );
					ref_risk             = (int)    controls.get( 2 );
					ref_control          = (int)    controls.get( 3 );
					score_control_result = (double) controls.get( 4 );
					ref_owner            = (int)    controls.get( 5 );
					control_result_ID    = (int)    controls.get( 6 );
					ref_audit            = (int)    controls.get( 7 );

					logs.log("LOG 1");
					logs.log("[OK] Revision: " + riskResult.getRevision() );
					logs.log("[OK] Revision Name ----------------: " + riskResult.getRevisionName() );
					logs.log("[OK] Revision State Name ----------: " + riskResult.getStateName() );
					logs.log("[OK] Unidade ----------------------: " + unidade );
					logs.log("[OK] Unidade ID -------------------: " + unidade.getId() );
					logs.log("[OK] Macroprocesso ----------------: " + macroprocess );
					logs.log("[OK] Macroprocesso ID -------------: " + macroprocess.getId() );
					logs.log("[OK] Processo ---------------------: " + process );
					logs.log("[OK] Processo ID ------------------: " + process.getId() );
					logs.log("[OK] Subprocesso ------------------: " + subprocess );
					logs.log("[OK] Subprocesso ID ---------------: " + subprocess.getId() );
					logs.log("[OK] Risco ------------------------: " + risk );
					logs.log("[OK] Risco ID ---------------------: " + risk.getId() );
					logs.log("[OK] Risco CODE -------------------: " + risk.getCode() );
					logs.log("");
					logs.log("[INFO] InerenteImpact ----------------------: " + inerente.getImpact() );
					logs.log("[INFO] InerenteProbability -----------------: " + inerente.getProbability() );
					logs.log("[INFO] InerenteLevel -----------------------: " + firstRiskInfo.getInherentLevel() );
					logs.log("[INFO] InerenteImpactField -----------------: " + firstRiskInfo.getInherentImpact() );
					logs.log("[INFO] InerenteImpactValue -----------------: " + firstRiskInfo.getInherentImpactValue() );
					logs.log("[INFO] InerenteProbabilityField ------------: " + firstRiskInfo.getInherentProbability() );
					logs.log("[INFO] InerenteProbabilityValue ------------: " + firstRiskInfo.getInherentProbabilityValue() );
					logs.log("");
					//logs.log("[INFO] Impact: ");
					logs.log("[INFO] Inerente -------------------: " + riskResult.getInherentImpact() );
					logs.log("[INFO] Label nível Inerente -------: " + riskResult.getInherentImpactLabel() );
					logs.log("[INFO] Level Inerente -------------: " + riskResult.getInherentImpactLevel() );
					logs.log("[INFO] Residual -------------------: " + riskResult.getResidualImpact() );
					logs.log("[INFO] Label nível Residual -------: " + riskResult.getResidualImpactLabel() );
					logs.log("[INFO] Level Residual -------------: " + riskResult.getResidualImpactLevel() );
					logs.log("");
					//logs.log("[INFO] Probabilidade: ");
					logs.log("[INFO] Inerente -------------------: " + riskResult.getInherentProbability() );
					logs.log("[INFO] Label nível Inerente -------: " + riskResult.getInherentProbabilityLabel() );
					logs.log("[INFO] Level Inerente -------------: " + riskResult.getInherentProbabilityLevel() );
					logs.log("[INFO] Residual -------------------: " + riskResult.getResidualProbability() );
					logs.log("[INFO] Label nível Residual -------: " + riskResult.getResidualProbabilityLabel() );
					logs.log("[INFO] Level Residual -------------: " + riskResult.getResidualProbabilityLevel() );
					logs.log("");
					//logs.log("[INFO] Níveis ---------------------: ");
					logs.log("[INFO] Inerente -------------------: " + riskResult.calculateInherentNivel() );
					logs.log("[INFO] Residual -------------------: " + riskResult.calculateResidualNivel() );
					logs.log("");
					logs.log("[INFO] ResidualSegmImpact -------------------: " + residualDeSegm.getImpact() );
					logs.log("[INFO] ResidualSegmProbability --------------: " + residualDeSegm.getProbability() );
					logs.log("[INFO] ResidualSegmLevel --------------------: " + lastRiskInfo.getResidualLevel() );
					logs.log("[INFO] ResidualSegmImpactField --------------: " + lastRiskInfo.getResidualImpact() );
					logs.log("[INFO] ResidualSegmImpactValue --------------: " + lastRiskInfo.getResidualImpactValue() );
					logs.log("[INFO] ResidualSegmProbabilityField ---------: " + lastRiskInfo.getResidualProbability() );
					logs.log("[INFO] ResidualSegmProbabilityValue ---------: " + lastRiskInfo.getResidualProbabilityValue() );
					logs.log("");
					logs.log("[INFO] Sistema de Risco --------------: " + mapping );
					logs.log("");
					logs.log("[OK] Controle ---------------------: " + controlTechnique.getName() );
					logs.log("[OK] Controle ID ------------------: " + controlTechnique.getId() );
					logs.log("[OK] Controle CODE ----------------: " + controlTechnique.getCode() );
					logs.log("CONTROL RESULT --------------------: " + score_control_result );

					if( score_control_result >= 0 && score_control_result <= 5 )
					{
						controlQualification = "Excelente";
					}
					else if( score_control_result > 5 && score_control_result <= 10 )
					{
						controlQualification = "Bom";
					}
					else if( score_control_result > 10 && score_control_result <= 20 )
					{
						controlQualification = "Satisfatório";
					}
					else if( score_control_result > 20 && score_control_result <= 40 )
					{
						controlQualification = "Regular";
					}
					else if( score_control_result > 40 && score_control_result <= 100 )
					{
						controlQualification = "Ruim";
					}

					logs.log("CONTROL QUALIFICATION -------------: " + controlQualification );
					logs.log("");

					auditResult = getAuditResult( subprocess, risk, controlTechnique, revision, ref_audit );
					
					if( !debug )
					{
						List list = new ArrayList();
						list.add( riskResult.getRevision()     );  //"Revision     
						list.add( riskResult.getRevisionName() );  //"Revision Name 
						list.add( riskResult.getStateName()    );  //"Revision State Name
						list.add( unidade.getName()            );  //"Unidade 
						list.add( unidade.getId()              );  //"Unidade ID 
						list.add( macroprocess.getName()       );  //"Macroprocesso 
						list.add( macroprocess.getId()         );  //"Macroprocesso ID 
						list.add( process.getName()            );  //"Processo
						list.add( process.getId()              );  //"Processo ID 
						list.add( subprocess.getName()         );  //"Subprocesso 
						list.add( subprocess.getId()           );  //"Subprocesso ID 
						list.add( risk.getName()               );  //"Risco 
						list.add( risk.getId()                 );  //"Risco ID 
						list.add( risk.getCode()               );  //"Risco CODE 
						list.add( inerente.getImpact()                        ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( inerente.getProbability()                   ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentLevel()            ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentImpact()           ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentImpactValue()      ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentProbability()      ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentProbabilityValue() ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( riskResult.getInherentImpact()      );  //"Inerente 
						list.add( riskResult.getInherentImpactLabel() );  //"Label nível Inerente
						list.add( riskResult.getInherentImpactLevel() );  //"Level Inerente 
						list.add( riskResult.getResidualImpact()      );  //"Residual  
						list.add( riskResult.getResidualImpactLabel() );  //"Label nível Residual 
						list.add( riskResult.getResidualImpactLevel() );  //"Level Residual   
						list.add( riskResult.getInherentProbability()      );  //"Inerente   
						list.add( riskResult.getInherentProbabilityLabel() );  //"Label nível Inerente       
						list.add( riskResult.getInherentProbabilityLevel() );  //"Level Inerente       
						list.add( riskResult.getResidualProbability()      );  //"Residual 
						list.add( riskResult.getResidualProbabilityLabel() );  //"Label nível Residual    
						list.add( riskResult.getResidualProbabilityLevel() );  //"Level Residual     
						list.add( riskResult.calculateInherentNivel() );  //"Inerente 
						list.add( riskResult.calculateResidualNivel() );  //"Residual 
						list.add( residualDeSegm.getImpact()                 );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( residualDeSegm.getProbability()            );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualLevel()            );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualImpact()           );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualImpactValue()      );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualProbability()      );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualProbabilityValue() );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( mapping );  //"Sistema de Risco        
						list.add( controlTechnique.getName() );  //CONTROL_NAME
						list.add( controlTechnique.getId()   );  //CONTROL_ID
						list.add( controlTechnique.getCode() );  //CONTROL_CODE
						list.add( score_control_result       );  //CONTROL_VALUE
						list.add( controlQualification       );  //CONTROL_QUALIFICATION
						list.add( auditResult.get(0) );  // Audit_Plan 
						list.add( auditResult.get(1) );  // PROBABILITY_NAME
						list.add( auditResult.get(2) );  // PROBABILITY_NAME
						list.add( auditResult.get(3) );  // PROBABILITY_NAME
						list.add( auditResult.get(4) );  // PROBABILITY_NAME
						list.add( auditResult.get(5) );  // PROBABILITY_NAME

						listResult.add( list );
					}
					logs.log("LOG 1 _______________________________________\n\n");	
				}
				else
				{
					logs.log("LOG 2");
					logs.log("[OK] Revision: " + riskResult.getRevision() );
					logs.log("[OK] Revision Name -------------------: " + riskResult.getRevisionName() );
					logs.log("[OK] Revision State Name -------------: " + riskResult.getStateName() );
					logs.log("[OK] Unidade -------------------------: " + unidade );
					logs.log("[OK] Unidade ID ----------------------: " + unidade.getId() );
					logs.log("[OK] Macroprocesso -------------------: " + macroprocess );
					logs.log("[OK] Macroprocesso ID ----------------: " + macroprocess.getId() );
					logs.log("[OK] Processo ------------------------: " + process );
					logs.log("[OK] Processo ID ---------------------: " + process.getId() );
					logs.log("[OK] Subprocesso ---------------------: " + subprocess );
					logs.log("[OK] Subprocesso ID ------------------: " + subprocess.getId() );
					logs.log("[OK] Risco ---------------------------: " + risk );
					logs.log("[OK] Risco ID ------------------------: " + risk.getId() );
					logs.log("[OK] Risco CODE ----------------------: " + risk.getCode() );
					logs.log("");
					logs.log("[INFO] InerenteImpact ----------------------: " + inerente.getImpact() );
					logs.log("[INFO] InerenteProbability -----------------: " + inerente.getProbability() );
					logs.log("[INFO] InerenteLevel -----------------------: " + firstRiskInfo.getInherentLevel() );
					logs.log("[INFO] InerenteImpactField -----------------: " + firstRiskInfo.getInherentImpact() );
					logs.log("[INFO] InerenteImpactValue -----------------: " + firstRiskInfo.getInherentImpactValue() );
					logs.log("[INFO] InerenteProbabilityField ------------: " + firstRiskInfo.getInherentProbability() );
					logs.log("[INFO] InerenteProbabilityValue ------------: " + firstRiskInfo.getInherentProbabilityValue() );
					logs.log("");
					//logs.log("[INFO] Impact: ");
					logs.log("[INFO] Inerente ----------------------: " + riskResult.getInherentImpact() );
					logs.log("[INFO] Label nível Inerente ----------: " + riskResult.getInherentImpactLabel() );
					logs.log("[INFO] Level Inerente ----------------: " + riskResult.getInherentImpactLevel() );
					logs.log("[INFO] Residual ----------------------: " + riskResult.getResidualImpact() );
					logs.log("[INFO] Label nível Residual ----------: " + riskResult.getResidualImpactLabel() );
					logs.log("[INFO] Level Residual ----------------: " + riskResult.getResidualImpactLevel() );
					logs.log("");
					//logs.log("[INFO] Probabilidade: ");
					logs.log("[INFO] Inerente ----------------------: " + riskResult.getInherentProbability() );
					logs.log("[INFO] Label nível Inerente ----------: " + riskResult.getInherentProbabilityLabel() );
					logs.log("[INFO] Level Inerente ----------------: " + riskResult.getInherentProbabilityLevel() );
					logs.log("[INFO] Residual ----------------------: " + riskResult.getResidualProbability() );
					logs.log("[INFO] Label nível Residual ----------: " + riskResult.getResidualProbabilityLabel() );
					logs.log("[INFO] Level Residual ----------------: " + riskResult.getResidualProbabilityLevel() );
					logs.log("");
					//logs.log("[INFO] Níveis ---------------------: ");
					logs.log("[INFO] Inerente ----------------------: " + riskResult.calculateInherentNivel() );
					logs.log("[INFO] Residual ----------------------: " + riskResult.calculateResidualNivel() );
					logs.log("");
					logs.log("[INFO] ResidualSegmImpact -------------------: " + residualDeSegm.getImpact() );
					logs.log("[INFO] ResidualSegmProbability --------------: " + residualDeSegm.getProbability() );
					logs.log("[INFO] ResidualSegmLevel --------------------: " + lastRiskInfo.getResidualLevel() );
					logs.log("[INFO] ResidualSegmImpactField --------------: " + lastRiskInfo.getResidualImpact() );
					logs.log("[INFO] ResidualSegmImpactValue --------------: " + lastRiskInfo.getResidualImpactValue() );
					logs.log("[INFO] ResidualSegmProbabilityField ---------: " + lastRiskInfo.getResidualProbability() );
					logs.log("[INFO] ResidualSegmProbabilityValue ---------: " + lastRiskInfo.getResidualProbabilityValue() );
					logs.log("");
					logs.log("[INFO] Sistema de Risco --------------: " + mapping );
					logs.log("");
					logs.log("Controle -----------------------------: n/d" );
					logs.log("Controle ID --------------------------: n/d" );
					logs.log("Controle CODE ------------------------: n/d" );
					logs.log("CONTROL RESULT -----------------------: n/d" );
					logs.log("CONTROL QUALIFICATION ----------------: n/d" );
					logs.log("");
					logs.log("Audit_Plan ---------------------------: n/d" );
					logs.log("PROBABILITY_NAME ---------------------: n/d" );
					logs.log("PROBABILITY_NAME ---------------------: n/d" );
					logs.log("PROBABILITY_NAME ---------------------: n/d" );
					logs.log("PROBABILITY_NAME ---------------------: n/d" );
					logs.log("PROBABILITY_NAME ---------------------: n/d" );
					logs.log("LOG 2 ----------------------------------------\n\n");	

					if( !debug )
					{
						List list = new ArrayList();
						list.add( riskResult.getRevision()     );  //"Revision     
						list.add( riskResult.getRevisionName() );  //"Revision Name 
						list.add( riskResult.getStateName()    );  //"Revision State Name
						list.add( unidade.getName()            );  //"Unidade 
						list.add( unidade.getId()              );  //"Unidade ID 
						list.add( macroprocess.getName()       );  //"Macroprocesso 
						list.add( macroprocess.getId()         );  //"Macroprocesso ID 
						list.add( process.getName()            );  //"Processo
						list.add( process.getId()              );  //"Processo ID 
						list.add( subprocess.getName()         );  //"Subprocesso 
						list.add( subprocess.getId()           );  //"Subprocesso ID 
						list.add( risk.getName()               );  //"Risco 
						list.add( risk.getId()                 );  //"Risco ID 
						list.add( risk.getCode()               );  //"Risco CODE 
						list.add( inerente.getImpact()                        ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( inerente.getProbability()                   ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentLevel()            ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentImpact()           ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentImpactValue()      ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentProbability()      ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( firstRiskInfo.getInherentProbabilityValue() ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
						list.add( riskResult.getInherentImpact()      );  //"Inerente 
						list.add( riskResult.getInherentImpactLabel() );  //"Label nível Inerente
						list.add( riskResult.getInherentImpactLevel() );  //"Level Inerente 
						list.add( riskResult.getResidualImpact()      );  //"Residual  
						list.add( riskResult.getResidualImpactLabel() );  //"Label nível Residual 
						list.add( riskResult.getResidualImpactLevel() );  //"Level Residual   
						list.add( riskResult.getInherentProbability()      );  //"Inerente   
						list.add( riskResult.getInherentProbabilityLabel() );  //"Label nível Inerente       
						list.add( riskResult.getInherentProbabilityLevel() );  //"Level Inerente       
						list.add( riskResult.getResidualProbability()      );  //"Residual 
						list.add( riskResult.getResidualProbabilityLabel() );  //"Label nível Residual    
						list.add( riskResult.getResidualProbabilityLevel() );  //"Level Residual     
						list.add( riskResult.calculateInherentNivel() );  //"Inerente 
						list.add( riskResult.calculateResidualNivel() );  //"Residual 
						list.add( residualDeSegm.getImpact()                 );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( residualDeSegm.getProbability()            );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualLevel()            );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualImpact()           );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualImpactValue()      );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualProbability()      );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( lastRiskInfo.getResidualProbabilityValue() );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
						list.add( mapping );  //"Sistema de Risco        
						list.add( "n/d" );  //CONTROL_NAME
						list.add( "n/d" );  //CONTROL_ID
						list.add( "n/d" );  //CONTROL_CODE
						list.add( "n/d" );  //CONTROL_VALUE
						list.add( "n/d" );  //CONTROL_QUALIFICATION
						list.add( "n/d" );  //AUDIT_NAME
						list.add( "n/d" );  //AUDIT_OP1
						list.add( "n/d" );  //AUDIT_OP2
						list.add( "n/d" );  //AUDIT_OP3
						list.add( "n/d" );  //AUDIT_OP4
						list.add( "n/d" );  //AUDIT_OP5
						listResult.add( list );
					}
				}
			}
		}
		else
		{
			logs.log("LOG 3");
			logs.log("[OK] Revision: " + riskResult.getRevision() );
			logs.log("[OK] Revision Name -------------------: " + riskResult.getRevisionName() );
			logs.log("[OK] Revision State Name -------------: " + riskResult.getStateName() );
			logs.log("[OK] Unidade -------------------------: " + unidade );
			logs.log("[OK] Unidade ID ----------------------: " + unidade.getId() );
			logs.log("[OK] Macroprocesso -------------------: " + macroprocess );
			logs.log("[OK] Macroprocesso ID ----------------: " + macroprocess.getId() );
			logs.log("[OK] Processo ------------------------: " + process );
			logs.log("[OK] Processo ID ---------------------: " + process.getId() );
			logs.log("[OK] Subprocesso ---------------------: " + subprocess );
			logs.log("[OK] Subprocesso ID ------------------: " + subprocess.getId() );
			logs.log("[OK] Risco ---------------------------: " + risk );
			logs.log("[OK] Risco ID ------------------------: " + risk.getId() );
			logs.log("[OK] Risco CODE ----------------------: " + risk.getCode() );
			logs.log("");
			logs.log("[INFO] InerenteImpact ----------------------: " + inerente.getImpact() );
			logs.log("[INFO] InerenteProbability -----------------: " + inerente.getProbability() );
			logs.log("[INFO] InerenteLevel -----------------------: " + firstRiskInfo.getInherentLevel() );
			logs.log("[INFO] InerenteImpactField -----------------: " + firstRiskInfo.getInherentImpact() );
			logs.log("[INFO] InerenteImpactValue -----------------: " + firstRiskInfo.getInherentImpactValue() );
			logs.log("[INFO] InerenteProbabilityField ------------: " + firstRiskInfo.getInherentProbability() );
			logs.log("[INFO] InerenteProbabilityValue ------------: " + firstRiskInfo.getInherentProbabilityValue() );
			logs.log("");
			//logs.log("[INFO] Impact: ");
			logs.log("[INFO] Inerente ----------------------: " + riskResult.getInherentImpact() );
			logs.log("[INFO] Label nível Inerente ----------: " + riskResult.getInherentImpactLabel() );
			logs.log("[INFO] Level Inerente ----------------: " + riskResult.getInherentImpactLevel() );
			logs.log("[INFO] Residual ----------------------: " + riskResult.getResidualImpact() );
			logs.log("[INFO] Label nível Residual ----------: " + riskResult.getResidualImpactLabel() );
			logs.log("[INFO] Level Residual ----------------: " + riskResult.getResidualImpactLevel() );
			logs.log("");
			//logs.log("[INFO] Probabilidade: ");
			logs.log("[INFO] Inerente ----------------------: " + riskResult.getInherentProbability() );
			logs.log("[INFO] Label nível Inerente ----------: " + riskResult.getInherentProbabilityLabel() );
			logs.log("[INFO] Level Inerente ----------------: " + riskResult.getInherentProbabilityLevel() );
			logs.log("[INFO] Residual ----------------------: " + riskResult.getResidualProbability() );
			logs.log("[INFO] Label nível Residual ----------: " + riskResult.getResidualProbabilityLabel() );
			logs.log("[INFO] Level Residual ----------------: " + riskResult.getResidualProbabilityLevel() );
			logs.log("");
			//logs.log("[INFO] Níveis ---------------------: ");
			logs.log("[INFO] Inerente ----------------------: " + riskResult.calculateInherentNivel() );
			logs.log("[INFO] Residual ----------------------: " + riskResult.calculateResidualNivel() );
			logs.log("");
			logs.log("[INFO] ResidualSegmImpact -------------------: " + residualDeSegm.getImpact() );
			logs.log("[INFO] ResidualSegmProbability --------------: " + residualDeSegm.getProbability() );
			logs.log("[INFO] ResidualSegmLevel --------------------: " + lastRiskInfo.getResidualLevel() );
			logs.log("[INFO] ResidualSegmImpactField --------------: " + lastRiskInfo.getResidualImpact() );
			logs.log("[INFO] ResidualSegmImpactValue --------------: " + lastRiskInfo.getResidualImpactValue() );
			logs.log("[INFO] ResidualSegmProbabilityField ---------: " + lastRiskInfo.getResidualProbability() );
			logs.log("[INFO] ResidualSegmProbabilityValue ---------: " + lastRiskInfo.getResidualProbabilityValue() );
			logs.log("");

			logs.log("[INFO] Sistema de Risco --------------: " + mapping );

			logs.log("");
			logs.log("Controle -----------------------------: n/d" );
			logs.log("Controle ID --------------------------: n/d" );
			logs.log("Controle CODE ------------------------: n/d" );
			logs.log("CONTROL RESULT -----------------------: n/d" );
			logs.log("CONTROL QUALIFICATION ----------------: n/d" );
			logs.log("");
			logs.log("Audit_Plan ---------------------------: n/d" );
			logs.log("PROBABILITY_NAME ---------------------: n/d" );
			logs.log("PROBABILITY_NAME ---------------------: n/d" );
			logs.log("PROBABILITY_NAME ---------------------: n/d" );
			logs.log("PROBABILITY_NAME ---------------------: n/d" );
			logs.log("PROBABILITY_NAME ---------------------: n/d" );

			logs.log("LOG 3 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");	

			if( !debug )
			{
				List list = new ArrayList();

				list.add( riskResult.getRevision()     );  //"Revision     
				list.add( riskResult.getRevisionName() );  //"Revision Name 
				list.add( riskResult.getStateName()    );  //"Revision State Name
				list.add( unidade.getName()            );  //"Unidade 
				list.add( unidade.getId()              );  //"Unidade ID 
				list.add( macroprocess.getName()       );  //"Macroprocesso 
				list.add( macroprocess.getId()         );  //"Macroprocesso ID 
				list.add( process.getName()            );  //"Processo
				list.add( process.getId()              );  //"Processo ID 
				list.add( subprocess.getName()         );  //"Subprocesso 
				list.add( subprocess.getId()           );  //"Subprocesso ID 
				list.add( risk.getName()               );  //"Risco 
				list.add( risk.getId()                 );  //"Risco ID 
				list.add( risk.getCode()               );  //"Risco CODE 
				list.add( inerente.getImpact()                        ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
				list.add( inerente.getProbability()                   ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
				list.add( firstRiskInfo.getInherentLevel()            ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
				list.add( firstRiskInfo.getInherentImpact()           ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
				list.add( firstRiskInfo.getInherentImpactValue()      ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
				list.add( firstRiskInfo.getInherentProbability()      ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
				list.add( firstRiskInfo.getInherentProbabilityValue() ); //RESULTADO RISCO INERENTE - PRIMEIRO RESULT
				list.add( riskResult.getInherentImpact()      );  //"Inerente 
				list.add( riskResult.getInherentImpactLabel() );  //"Label nível Inerente
				list.add( riskResult.getInherentImpactLevel() );  //"Level Inerente 
				list.add( riskResult.getResidualImpact()      );  //"Residual  
				list.add( riskResult.getResidualImpactLabel() );  //"Label nível Residual 
				list.add( riskResult.getResidualImpactLevel() );  //"Level Residual   
				list.add( riskResult.getInherentProbability()      );  //"Inerente   
				list.add( riskResult.getInherentProbabilityLabel() );  //"Label nível Inerente       
				list.add( riskResult.getInherentProbabilityLevel() );  //"Level Inerente       
				list.add( riskResult.getResidualProbability()      );  //"Residual 
				list.add( riskResult.getResidualProbabilityLabel() );  //"Label nível Residual    
				list.add( riskResult.getResidualProbabilityLevel() );  //"Level Residual     
				list.add( riskResult.calculateInherentNivel() );  //"Inerente 
				list.add( riskResult.calculateResidualNivel() );  //"Residual 
				list.add( residualDeSegm.getImpact()                 );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
				list.add( residualDeSegm.getProbability()            );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
				list.add( lastRiskInfo.getResidualLevel()            );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
				list.add( lastRiskInfo.getResidualImpact()           );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
				list.add( lastRiskInfo.getResidualImpactValue()      );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
				list.add( lastRiskInfo.getResidualProbability()      );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
				list.add( lastRiskInfo.getResidualProbabilityValue() );  // RESULTADO RISCO RESIDUAL DE SEGMENTO - ÚLTIMO RESULTADO DO RISCO
				list.add( mapping );  //"Sistema de Risco        
				list.add( "n/d" );  //CONTROL_NAME
				list.add( "n/d" );  //CONTROL_ID
				list.add( "n/d" );  //CONTROL_CODE
				list.add( "n/d" );  //CONTROL_VALUE
				list.add( "n/d" );  //CONTROL_QUALIFICATION
				list.add( "n/d" );  //AUDIT_NAME
				list.add( "n/d" );  //AUDIT_OP1
				list.add( "n/d" );  //AUDIT_OP2
				list.add( "n/d" );  //AUDIT_OP3
				list.add( "n/d" );  //AUDIT_OP4
				list.add( "n/d" );  //AUDIT_OP5

				listResult.add( list );
			}
		}
	}

	return listResult;
}

/**
 * 
 * @param value double
 * @param riskMapId int
 * @return ImpactLevel
 */
public ImpactLevel getImpactLevel( double value, int riskMapId )
{
	List impactLevels = riskManager.getImpactLevels( riskMapId );

	ImpactLevel result = null;

	for ( ImpactLevel impactLevel : impactLevels )
	{
		if ( impactLevel.getLevel() >= value )
		{
			result = impactLevel;
			break;
		}
	}

	return result;
}

/**
 * 
 * @param value double 
 * @param riskMap int
 * @return ProbabilityRange
 */
public ProbabilityRange getProbabilityLevel( double value, int riskMap )
{
	List probabilities = probabilityManager.getProbabilityRanges( riskMap );

	ProbabilityRange result = null;
	
	for ( ProbabilityRange probabilityRange : probabilities )
	{
		if ( probabilityRange.inRange( value ))
		{
			result = probabilityRange;
			break;
		}
	}

	return result;
}

/**
 * 
 * @param process StructureCategory
 * @param risk OperationalRisk
 * @param revision int
 * @param aud int
 * @return List
 */
public List getInherentResultsByRevision( StructureCategory process, OperationalRisk risk, int revision, int aud )
{
	List list = new ArrayList();

	queryResult = SA.query("select impact, probability from orm_risk_results where ref_process = " + process.getId() + 
									 " and ref_risk = " + risk.getId() + " and rev = " + revision ).fetch();
	

	if( !queryResult.isEmpty() )
	{
		Object[] result = (Object[]) queryResult.get( 0 );

		double impact      = SA.cdouble( result[0] );
		double probability = SA.cdouble( result[1] );

		list.add( impact );
		list.add( probability );
	}

	return list;
}

/**
 * 
 * @param process StructureCategory
 * @param risk OperationalRisk
 * @param revision int
 * @param riskMapId int
 * @param unit int
 * @param aud int
 * @return RiskResults
 */
RiskResults getFirstRiskResults( StructureCategory process, OperationalRisk risk, int revision, int riskMapId, int unit, int aud )
{
	RiskResults riskResults = new RiskResults();
	String revisionName = "";
	String baselineName = "";
	double inherentImpact;
	double inherentProbability;
	double residualImpact;
	double residualProbability;

	inherentResults = riskResultManager.getRiskResult( -1, process, risk );

	if( inherentResults == null )
	{
		logs.log("[RISK ERROR] Nenhum resultado encontrado! Processo: " + process + " , Risco: " + risk );
	}
	else
	{
		inherentImpact      = inherentResults.getImpact();
		inherentProbability = inherentResults.getProbability();

		residualImpact      = inherentImpact;
		residualProbability = inherentProbability;

		ImpactLevel inherentImpactLevel = getImpactLevel( inherentImpact, riskMapId );
		ProbabilityRange inherentProbabilityLevel = getProbabilityLevel( inherentProbability, riskMapId );

		ImpactLevel residualImpactLevel = getImpactLevel( inherentImpact, riskMapId );
		ProbabilityRange residualProbabilityLevel = getProbabilityLevel( inherentProbability, riskMapId );

		stateName = "Inerente";
		baselineName = "n/d";

		riskResults.setRevision( revision );
		riskResults.setStateName( stateName );
		
		riskResults.setRevisionName( baselineName );

		riskResults.setInherentImpact( inherentImpact );
		riskResults.setInherentProbability( inherentProbability );

		riskResults.setInherentImpactLevel( inherentImpactLevel.getLevel() );
		riskResults.setInherentProbabilityLevel( inherentProbabilityLevel.getLevel() );

		riskResults.setResidualImpact( residualImpact );
		riskResults.setResidualProbability( residualProbability );

		riskResults.setResidualImpactLevel( residualImpactLevel.getLevel() );
		riskResults.setResidualProbabilityLevel( residualProbabilityLevel.getLevel() );

		riskResults.setInherentImpactLabel( inherentImpactLevel.getName() );
		riskResults.setResidualImpactLabel( residualImpactLevel.getName() );

		riskResults.setInherentProbabilityLabel( inherentProbabilityLevel.getName() );
		riskResults.setResidualProbabilityLabel( residualProbabilityLevel.getName() );
	}

	return riskResults;
}

/**
 * 
 * @param process StructureCategory
 * @param risk OperationalRisk
 * @param revision int
 * @param riskMapId int
 * @param unit int
 * @return RiskResults
 */
RiskResults getRiskResults( StructureCategory process, OperationalRisk risk, int revision, int riskMapId, int unit )
{
	RiskResults riskResults = new RiskResults();
	List inherentResults = new ArrayList();
	String revisionName = "n/d";
	String baselineName = "n/d";
	double inherentImpact;
	double inherentProbability;
	double residualImpact;
	double residualProbability;

	List inherentResults = getInherentResultsByRevision( process, risk, revision, 0 );
	RiskResult residualResults = riskResultManager.getEffectiveRiskResultByRevision( process.getId(), risk.getId(), revision );	

	if( !inherentResults.isEmpty() )
	{
		inherentImpact      = (double) inherentResults.get( 0 );
		inherentProbability = (double) inherentResults.get( 1 );
	}
	else
	{
		return null;
	}

	//if( residualResults != null )
	//{
	residualImpact = residualResults.getImpact();
	residualProbability = residualResults.getProbability();
	//}

	ImpactLevel inherentImpactLevel = getImpactLevel( inherentImpact, riskMapId );
	ProbabilityRange inherentProbabilityLevel = getProbabilityLevel( inherentProbability, riskMapId );

	ImpactLevel residualImpactLevel = getImpactLevel( residualImpact, riskMapId );
	ProbabilityRange residualProbabilityLevel = getProbabilityLevel( residualProbability, riskMapId );

	if( revision == 0 )
	{
		stateName = "Residual de Segmento";
		baselineName = getBaselineName( revision, unit );
	}
	else if( revision > 0 )
	{
		stateName = "Residual";
		baselineName = getBaselineName( revision, unit );
	}	

	riskResults.setRevision( revision );
	riskResults.setStateName( stateName );
	
	riskResults.setRevisionName( baselineName );

	riskResults.setInherentImpact( inherentImpact );
	riskResults.setInherentProbability( inherentProbability );

	riskResults.setInherentImpactLevel( inherentImpactLevel.getLevel() );
	riskResults.setInherentProbabilityLevel( inherentProbabilityLevel.getLevel() );

	riskResults.setResidualImpact( residualImpact );
	riskResults.setResidualProbability( residualProbability );

	riskResults.setResidualImpactLevel( residualImpactLevel.getLevel() );
	riskResults.setResidualProbabilityLevel( residualProbabilityLevel.getLevel() );

	riskResults.setInherentImpactLabel( inherentImpactLevel.getName() );
	riskResults.setResidualImpactLabel( residualImpactLevel.getName() );

	riskResults.setInherentProbabilityLabel( inherentProbabilityLevel.getName() );
	riskResults.setResidualProbabilityLabel( residualProbabilityLevel.getName() );

	return riskResults;
}

/**
 * 
 * @return obtainRiskStructure
 * @throws Exception
 */
List obtainRiskStructure() throws Exception
{
	int lastRev = 0;
	aud = -1;
	List data = new ArrayList();

	List categories = categoryManager.getCategories( Category.FAMILY_ORM_BUSINESS_UNIT );

	try
    {
		for( Category category : categories ) //unidade
		{
			List businessUnits = businessUnitManager.getBusinessUnits( category.getId() ); //macroprocesso

			if( businessUnits.isEmpty() ) 
			{
				logs.log("[SC ERROR] Nenhum Macroprocesso encontrado! Unidade: " + category );
			}
			else
			{
				for( BusinessUnit businessUnit : businessUnits )
				{      
					Object baselines = getBaselines();
					for( baseline : baselines )
					{
						int revision = baseline.rev;
						lastRev = Math.max( lastRev, revision );
						//String revisionName = baseline.name; 
					}
					for( rev = 0; rev <= lastRev; rev++ )
					{
						List structureCategories = structureCategoryManager.getMainStructureCategories( businessUnit );
				
						if( structureCategories.isEmpty() ) 
						{
							logs.log("[SC ERROR] Nenhum Processo encontrado! Macroprocesso: " + businessUnit );
						}
						else
						{
							if( rev == 0 )
							{
								for( StructureCategory structureCategory : structureCategories ) //processo
								{            
									List processes = structureCategoryManager.getStructureCategoryChildrens( structureCategory.getId() );
									
									if( processes.isEmpty() ) 
									{
										logs.log("[SC ERROR] Nenhum Subprocesso encontrado! Processo: " + structureCategory );
									}
									else
									{
										for( StructureCategory process : processes ) //subprocesso
										{
											int riskMapId = riskMapManager.getRiskMapId( process.getUnitId() );
											List operationalRisks = operationalRiskManager.getRisks( process );

											if( operationalRisks.isEmpty() ) 
											{
												logs.log("[SC ERROR] Nenhum Risco encontrado! Subprocesso: " + process );
											}
											else
											{
												for( OperationalRisk operationalRisk : operationalRisks )
												{
													riskResults = getFirstRiskResults( process, operationalRisk, 0, riskMapId, businessUnit.getId(), aud );
													
													if( riskResults == null )
													{
														break;
													}
													
													List preparedList = obtainPreparedList( category, businessUnit, structureCategory, process, operationalRisk, riskResults, rev, -1 );
													data.addAll( preparedList );
												}
											}
										}
									}
								}
							}
						}
						
						List structureCategories = structureCategoryManager.getMainStructureCategoriesByRevision( businessUnit, rev );

						if( structureCategories.isEmpty() ) 
						{
							logs.log("[SC ERROR] Nenhum Processo encontrado! Macroprocesso: " + businessUnit );
						}
						else
						{
							for( StructureCategory structureCategory : structureCategories ) //processo
							{            
								List processes = structureCategoryManager.getStructureCategoryChildrens( structureCategory.getId() );
								
								if( processes.isEmpty() ) 
								{
									logs.log("[SC ERROR] Nenhum Subprocesso encontrado! Processo: " + structureCategory );
								}
								else
								{
									for( StructureCategory process : processes ) //subprocesso
									{
										int riskMapId = riskMapManager.getRiskMapId( process.getUnitId() );
										List operationalRisks = operationalRiskManager.getRisks( process );

										if( operationalRisks.isEmpty() ) 
										{
											logs.log("[SC ERROR] Nenhum Risco encontrado! Subprocesso: " + process );
										}
										else
										{
											for( OperationalRisk operationalRisk : operationalRisks )
											{
												riskResults = getRiskResults( process, operationalRisk, rev, riskMapId, businessUnit.getId() );
												
												if( riskResults == null )
												{
													break;
												}
												List preparedList = obtainPreparedList( category, businessUnit, structureCategory, process, operationalRisk, riskResults, rev, 0 );
												data.addAll( preparedList );
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		logs.log("[OK] Informações de riscos da estrutura obtidas!" );
        logs.log("[OK] Registros obtidos      : " + data.size() );
        logs.log("");
    }
    catch( Exception e )
    {
        throw new Exception( " Problemas ao buscar informações de riscos da estrutura: " + e.getMessage() );
    }

	return data;
}

void validate() throws Exception
{
    try
    {   
        logs.log( "[OK] Procurando tabela '" + TABLE + "'..." );

        QueryMarvel queryMarvel = SA.query( "SELECT * FROM " + TABLE );

        Object[] object = queryMarvel.fetch1();

        columnsName = queryMarvel.columns();

        if( columnsName == null || columnsName.length < 1 )
        {
            throw new Exception( "Problemas ao obter colunas da tabela " + TABLE + "!" );
        }

        logs.log("[OK] Tabela encontrada!");
        logs.log("");
    }
    catch( Exception e )
    {
        throw new Exception( "Problemas ao buscar tabela " + TABLE + ": " + e.getMessage() );
    }
}

void clearTable() throws Exception
{
    try
    {
        SA.query( "DELETE FROM " + TABLE ).execute();
    }
    catch( Exception e )
    {
        throw new Exception( "Problemas ao limpar a tabela '" + TABLE + "': " + e.getMessage() );
    }
}

void insertData( List datas )
{
    try
    {
		//dataCheck = "";

        logs.log("[OK] Salvando informações...");

        if( datas == null || datas.isEmpty() )
        {
            throw new Exception( "Nenhum registro para adicionar!");
        }

        //REALIZA PAGINAÇÃO DAS INFORMAÇÕES! Máximo 1000!
        int MAX_PER_PAGE = 1000;

        int pagesToInsert = ( (int) datas.size() / MAX_PER_PAGE ) + ( datas.size() % MAX_PER_PAGE == 0 ? 0 : 1 );
        
        for( int i = 0; i < pagesToInsert; i++ )
        {
            int fromIndex = ( i * MAX_PER_PAGE );
            int toIndex = ( ( (i+1) * MAX_PER_PAGE ) < datas.size() ? ( (i+1) * MAX_PER_PAGE ) - 1 : datas.size() );

            List partDatas = datas.subList( fromIndex, toIndex );

            //SQL - INSERT
            String sql = "INSERT INTO " + TABLE;

            //COLUNAS
            String columns = "";
            String values = "";

            for( String columnName : columnsName )
            {
				// print(columnName);

                columns += ( columns.isEmpty() ? "" : ", " ) + columnName;
                values += ( values.isEmpty() ? "" : ", " ) + "?";
            }

            sql += " ( " + columns + " ) ";
            sql += "VALUES ( " + values + " ) ";

            Database database = null;
            DatabaseConnection connection = null;
            PreparedStatement preparedStatement = null;

            try
            {
                database = Database.getInstance();
                connection = database.getConnection();
                preparedStatement = connection.prepareStatement( sql ); 

                for( List data : partDatas )
                {
					//print(data);
					//dataCheck = data;
                    if( data.size() != columnsName.length )
                    {
                        throw new Exception( "Problemas ao inserir registro, numero de registros não confere com numero de colunas da tabela!");
                    }
                    
                    for( int i = 1; i <= columnsName.length; i++ )
                    {
                        preparedStatement.setObject( i, data.get( i-1 ) );
                    }

                    preparedStatement.addBatch();
                }

                preparedStatement.executeBatch();

                logs.log("[INFO] Registros Criados.");
                logs.log("");

            }
            catch( Exception e )
            {
                logs.log("[ERRO] Problemas ao inserir valores na tabela: " + e.getMessage() );
                sendLogs = true;
            }
            finally
            {
                preparedStatement != null ? preparedStatement.close() : null;
                connection != null ? connection.close() : null;
            }
        }

        logs.log("[OK] Informações salvas com sucesso!");
    }
    catch( Exception e )
    {
        ApplicationContext.getInstance().logError( "[ERRO] Script: Alimenta Tabela " + TABLE + ", -m insertData -e: " + e.getMessage() );
        logs.log("[ERRO] Problemas ao inserir valores na tabela: " + e.getMessage() );
        sendLogs = true;
    }
}

void main()
{
    logs.log("[OK] Executando Script..." );
    logs.log("");

    try
    {
        //Verificar se a tabela customizada existe
        validate();

        //List< List<values> >
        List data = obtainRiskStructure();

        //Se conseguiu buscar todas informações sem erro, então limpa a tabela!
		if( !debug )
		{
			clearTable();
		}

        //Insere os registros na tabela!
		if( !debug )
		{
        	insertData( data );
		}
    }
    catch( Exception e )
    {
        ApplicationContext.getInstance().logError( "[ERRO] Script: Alimenta Tabela " + TABLE + ", e: " + e.getMessage() );
        logs.log("[ERRO] Problemas ao executar script: " + e.getMessage() );
        sendLogs = true;
    }

    logs.log("[OK] Script Executado." );

    sendLogs ? logs.deliver() : null;
}

main();