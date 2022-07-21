/**
 * Nome do Script: Cria tabela custom - cus_risk_results.java
 * 
 * Descrição: Criação de tabela custom para receber informações do script Busca riscos/controles/auditorias e alimenta tabela custom.
 * 
 * Criado em:  30/06/2022
 * 
 * Autor: João Pedro Kipper - (jpk) - jpk@interact.com.br - (TID-03485) Script created
 * 
 * V: 1.0
 *
 */

//  CREATE TABLE cus_risk_results ( 
//              Rev int NOT NULL,
//              Rev_Name varchar(1000) NOT NULL,
//              Revision_State_Name varchar(150),
//              Unidade varchar(250) NOT NULL, 
//              Unidade_Id int NOT NULL, 
//              Macroprocess varchar(250) NOT NULL, 
//              Macroprocess_Id int NOT NULL, 
//              Process varchar(250) NOT NULL, 
//              Process_Id int NOT NULL, 
//              Subprocess varchar(250) NOT NULL, 
//              Subprocess_Id int NOT NULL, 
//              Risk varchar(250) NOT NULL, 
//              Risk_Id int NOT NULL, 
//              Risk_Code varchar(150) NOT NULL, 
//              InerenteImpact double precision NOT NULL,
//              InerenteProbability double precision NOT NULL,
//              InerenteLevel double precision NOT NULL,
//              InerenteImpactField varchar(150) NOT NULL,
//              InerenteImpactValue double precision NOT NULL,
//              InerenteProbabilityField varchar(150)NOT NULL,
//              InerenteProbabilityValue double precision NOT NULL,
//              Inherent_Impact double precision NOT NULL, 
//              Inherent_Impact_Label varchar(150) NOT NULL, 
//              Inherent_Impact_Level double precision NOT NULL, 
//              Residual_Impact double precision NOT NULL, 
//              Residual_Impact_Label varchar(150) NOT NULL, 
//              Residual_Impact_Level double precision NOT NULL, 
//              Inherent_Probability double precision NOT NULL, 
//              Inherent_Probability_Label varchar(150) NOT NULL, 
//              Inherent_Probability_Level double precision NOT NULL, 
//              Residual_Probability double precision NOT NULL, 
//              Residual_Probability_Label varchar(150) NOT NULL, 
//              Residual_Probability_Level double precision NOT NULL, 
//              Calculate_Inherent_Nivel double precision NOT NULL, 
//              Calculate_Residual_Nivel double precision NOT NULL, 
//              ResidualSegmImpact double precision NOT NULL,
//              ResidualSegmProbability double precision NOT NULL,
//              ResidualSegmLevel double precision NOT NULL,
//              ResidualSegmImpactField varchar(150) NOT NULL,
//              ResidualSegmImpactValue double precision NOT NULL,
//              ResidualSegmProbabilityField varchar(150) NOT NULL,
//              ResidualSegmProbabilityValue double precision NOT NULL,
//              Risk_Sistem varchar(150) NOT NULL, 
//              Control varchar(1000) NOT NULL, 
//              Control_Id varchar(100) NOT NULL,
//              Control_Code varchar(100) NOT NULL, 
//              Control_Score varchar(1000) NOT NULL,
//              Control_Qualification varchar(50) NOT NULL,
//              Audit varchar(1000) NOT NULL, 
//              Audit_Option_1 varchar(500) NOT NULL, 
//              Audit_Option_2 varchar(500) NOT NULL, 
//              Audit_Option_3 varchar(500) NOT NULL, 
//              Audit_Option_4 varchar(500) NOT NULL, 
//              Audit_Option_5 varchar(500) NOT NULL )


table = "CREATE TABLE cus_risk_results ( " +
"Rev int NOT NULL, " +
"Rev_Name TEXT NOT NULL, " +
"Revision_State_Name TEXT, " +
"Unidade TEXT NOT NULL, " +
"Unidade_Id int NOT NULL, " +
"Macroprocess TEXT NOT NULL, " +
"Macroprocess_Id int NOT NULL, " +
"Process TEXT NOT NULL, " +
"Process_Id int NOT NULL, " +
"Subprocess TEXT NOT NULL, " +
"Subprocess_Id int NOT NULL, " +
"Risk TEXT NOT NULL, " +
"Risk_Id int NOT NULL, " +
"Risk_Code TEXT NOT NULL, " +

"InerenteImpact double precision NOT NULL, " +
"InerenteProbability double precision NOT NULL, " +
"InerenteLevel double precision NOT NULL, " +
"InerenteImpactField varchar(150) NOT NULL, " +
"InerenteImpactValue double precision NOT NULL, " +
"InerenteProbabilityField varchar(150)NOT NULL, " +
"InerenteProbabilityValue double precision NOT NULL, " +

"Inherent_Impact double precision NOT NULL, " +
"Inherent_Impact_Label varchar(150) NOT NULL, " +
"Inherent_Impact_Level double precision NOT NULL, " +
"Residual_Impact double precision NOT NULL, " +
"Residual_Impact_Label varchar(150) NOT NULL, " +
"Residual_Impact_Level double precision NOT NULL, " +
"Inherent_Probability double precision NOT NULL, " +
"Inherent_Probability_Label varchar(150) NOT NULL, " +
"Inherent_Probability_Level double precision NOT NULL, " +
"Residual_Probability double precision NOT NULL, " +
"Residual_Probability_Label varchar(150) NOT NULL, " +
"Residual_Probability_Level double precision NOT NULL, " +
"Calculate_Inherent_Nivel double precision NOT NULL, " +
"Calculate_Residual_Nivel double precision NOT NULL, " +

"ResidualSegmImpact double precision NOT NULL, " +
"ResidualSegmProbability double precision NOT NULL, " +
"ResidualSegmLevel double precision NOT NULL, " +
"ResidualSegmImpactField varchar(150) NOT NULL, " +
"ResidualSegmImpactValue double precision NOT NULL, " +
"ResidualSegmProbabilityField varchar(150) NOT NULL, " +
"ResidualSegmProbabilityValue double precision NOT NULL, " +

"Risk_Sistem varchar(150) NOT NULL, " +
"Control TEXT NOT NULL, " +
"Control_Id varchar(100) NOT NULL," +
"Control_Code TEXT NOT NULL, " +
"Control_Score varchar(100) NOT NULL," +
"Control_Qualification varchar(100) NOT NULL," +
"Audit TEXT NOT NULL, " +
"Audit_Option_1 TEXT NOT NULL, " +
"Audit_Option_2 TEXT NOT NULL, " +
"Audit_Option_3 TEXT NOT NULL, " +
"Audit_Option_4 TEXT NOT NULL, " +
"Audit_Option_5 TEXT NOT NULL )";

SA.query( table ).execute();



           
//SA.query("DROP TABLE cus_risk_results").execute();

