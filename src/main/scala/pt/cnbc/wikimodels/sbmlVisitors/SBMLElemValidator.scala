/*
 * Copyright (c) 2011. Alexandre Martins. All rights reserved.
 */

package pt.cnbc.wikimodels.sbmlVisitors

/*
* SBMLElemValidator.scala
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*  @author Alexandre Martins
*/

import java.lang.{Double, String}
import java.io.{FileInputStream, File}
import javax.xml.validation.SchemaFactory
import javax.xml.XMLConstants
import javax.xml._
import org.sbml.libsbml.SBMLReader
import javax.xml.transform.stream.StreamSource
import org.xml.sax.InputSource
import scala.collection.JavaConversions._
import pt.cnbc.wikimodels.exceptions.ValidationDefaultCase._
import pt.cnbc.wikimodels.util.{SBMLParser, XMLParser, XSDAwareXML}
import dataVisitors.SBMLBeanVisitor
import pt.cnbc.wikimodels.dataModel._


object SBMLValidator {
  def libSBMLValidation(level:Int, version:Int, sbml:String):List[String] = {
    //FIXME: This code could be simpler if avoiding calling toList was possible
    val reader = new SBMLReader()
    val sbmlDoc = reader.readSBMLFromString(sbml)

    val indexSeq = for (errorNum <- Range(0, sbmlDoc.getNumErrors().toInt) )
      yield {
        val error = sbmlDoc.getError(errorNum.toLong)
        error.getCategoryAsString + " " + error.getSeverityAsString + " at [" + error.getLine +
          ","+ error.getColumn + "]: " + error.getMessage
      }
    indexSeq.toList
  }

  //### General XML validation  ###

  /**
   * Checks errors related to general XML syntax
   * Michael Hucka et al.
   * Systems Biology Markup Language (SBML) Level 2:
   * Structures and Facilities for Model Definitions. (2008).
   * at <http://www.sbml.org/specifications/sbml-level-2/version-4/release-1/sbml-level-2-version-4-rel-1.pdf>
   * on page 141
   *
   * NOTE: it is assumed that <?xml version="1.0" encoding="UTF-8"?> was removed.
   * This must be valid UTF-8
   */
  def xmlSyntaxValidation(level:Int, version:Int, xmlStr:String):List[String] = {
    //XML syntax

    try{
      scala.xml.XML.loadString(xmlStr)
      Nil
    } catch {
      case e:  org.xml.sax.SAXParseException =>
        "XML ERROR at [" + e.getLineNumber + ","+ e.getColumnNumber + "]: " + e.getMessage :: Nil
      case e => exceptionHandling(e)
    }
  }

  def sbmlSchemaValidation(level:Int, version:Int, xmlStr:String):List[String] = {


    try{     //XML Schmea validation
      // A schema can be loaded in like ...

      val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
      val s = sf.newSchema(this.getClass.getClassLoader.getResource("sbml-l"+level+"v"+version+".xsd"))
      //Use our class:

      val xmlElem = XSDAwareXML(s).loadString(xmlStr)
      Nil
    } catch {
      case e:org.xml.sax.SAXParseException =>{
        "SBML_SCHEMA ERROR:" + e.getLocalizedMessage + "\n" + e.printStackTrace :: Nil
      }
      case e => exceptionHandling(e)
    }
  }
}

/**
 * Validates according to SBML rules. If this methods accepts a model that contradicts any rule within the SBML specification, that is a bug.
 */
object SBMLStrictValidator extends SBMLBeanVisitor[List[String]]{
  import pt.cnbc.wikimodels.exceptions.BadFormatException
  private var metaIdTab = Map.empty[String, Int]
  private var idTab = Map.empty[String, Element{def id:String}]



  protected def visitModel(m: SBMLModel): List[String] = {
    SBMLLooseValidator.visit(m) :::
    NonRecursSBMLValidator.visit(m)
  }

  protected def visitCompartment(c: Compartment): List[String] =
    NonRecursSBMLValidator.visit(c)

  protected def visitConstraint(ct: Constraint): List[String] =
    NonRecursSBMLValidator.visit(ct)

  protected def visitFunctionDefinition(fd: FunctionDefinition): List[String] =
    NonRecursSBMLValidator.visit(fd)

  protected def visitKineticLaw(kl: KineticLaw): List[String] =
    NonRecursSBMLValidator.visit(kl)

  protected def visitModifierSpeciesReference(msr: ModifierSpeciesReference): List[String] =
    NonRecursSBMLValidator.visit(msr)

  protected def visitParameter(p: Parameter): List[String] =
    NonRecursSBMLValidator.visit(p)

  protected def visitReaction(r: Reaction): List[String] =
    NonRecursSBMLValidator.visit(r)

  protected def visitSpecies(s: Species): List[String] =
    NonRecursSBMLValidator.visit(s)

  protected def visitSpeciesReference(sr: SpeciesReference): List[String] =
    NonRecursSBMLValidator.visit(sr)

  protected def checkMetaIdIsUnique(e:Element) = {
    if(e.metaid == null){
      e.sbmlType + " has no metaid" :: Nil
    } else if( metaIdTab contains e.metaid ){
      "There is already " + (metaIdTab get e.metaid).get + " elements with the same metaId in the model." :: Nil
      metaIdTab += (e.metaid -> (metaIdTab.getOrElse(e.metaid,0) + 1))
    } else Nil
  }
}

/**
 * Detects errors in an SBML model that are specific to WikiModels.
 */
object WikiModelsValidator extends SBMLBeanVisitor[List[String]]{
  protected def visitModel(m: SBMLModel): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitCompartment(c: Compartment): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitConstraint(ct: Constraint): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitFunctionDefinition(fd: FunctionDefinition): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitKineticLaw(kl: KineticLaw): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitModifierSpeciesReference(msr: ModifierSpeciesReference): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitParameter(p: Parameter): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitReaction(r: Reaction): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitSpecies(s: Species): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil

  protected def visitSpeciesReference(sr: SpeciesReference): List[String] = "TODO : WikiModelsValidator Stub error" :: Nil
}


/**
 * Detects obvious errors in SBML that are not acceptable even in a half finished model.
 * This validator is very tolerant to inconsistencies and is meant to be used during SBML model editing.
 */
object SBMLLooseValidator extends SBMLBeanVisitor[List[String]]{
  protected def visitModel(m: SBMLModel): List[String] = {
    checkOptionalMetaId(m.metaid) :::
      checkOptionalId(m.id) :::
      checkOptionalName(m.name) :::
      helpers.SBMLl2v4Checks.isValidSIdType(m.id) :::
      m.listOfFunctionDefinitions.map(visitFunctionDefinition(_) ).flatMap(i => i).toList :::
      m.listOfCompartments.map(visitCompartment(_) ).flatMap(i => i).toList :::
      m.listOfSpecies.map(visitSpecies(_) ).flatMap(i => i).toList :::
      m.listOfParameters.map(visitParameter(_) ).flatMap(i => i).toList :::
      m.listOfConstraints.map(visitConstraint(_) ).flatMap(i => i).toList :::
      m.listOfReactions.map(visitReaction(_) ).flatMap(i => i).toList ::: List[String]()
  }

  protected def visitCompartment(c: Compartment): List[String] =
    checkOptionalMetaId(c.metaid) :::
      checkMandatoryId(c.id) :::
      checkOptionalName(c.name) :::
      checkSpatialDimensions(c.spatialDimensions) :::
      checkSizeValIsValid(c.size)

  protected def visitConstraint(ct: Constraint): List[String] = "TODO  SBMLLooseValidator Constraint stub" :: Nil

  protected def visitFunctionDefinition(fd: FunctionDefinition): List[String] =
    checkOptionalMetaId(fd.metaid) :::
      checkMandatoryId(fd.id) :::
      checkOptionalName(fd.name)

  protected def visitKineticLaw(kl: KineticLaw): List[String] = "TODO : SBMLLooseValidator Stub error" :: Nil

  protected def visitModifierSpeciesReference(msr: ModifierSpeciesReference): List[String] = "TODO : SBMLLooseValidator Stub error" :: Nil

  protected def visitParameter(p: Parameter): List[String] =
    checkOptionalMetaId(p.metaid) :::
      checkMandatoryId(p.id) :::
      checkOptionalName(p.name)

  protected def visitReaction(r: Reaction): List[String] =
    checkOptionalMetaId(r.metaid) :::
      checkMandatoryId(r.id) :::
      checkOptionalName(r.name)

  protected def visitSpecies(s: Species): List[String] =
    checkOptionalMetaId(s.metaid) :::
      checkMandatoryId(s.id) :::
      checkOptionalName(s.name)

  protected def visitSpeciesReference(sr: SpeciesReference): List[String] = "TODO : SBMLLooseValidator Stub error" :: Nil

  def checkOptionalName(name:String):List[String] = {
    if (name != null)
      helpers.SBMLl2v4Checks.isValidSIdType(name)
    else Nil
  }

  def checkOptionalId(id:String):List[String] = {
    if (id != null)
      helpers.SBMLl2v4Checks.isValidSIdType(id)
    else Nil
  }

  def checkMandatoryId(id:String):List[String] = {
    if (id == null)
      "Id should have a value" :: Nil
    else
      helpers.SBMLl2v4Checks.isValidSIdType(id)
  }

  def checkOptionalMetaId(metaid:String): List[String] = {
    if (metaid == null)
      helpers.XMLChecks.isValidIDType(metaid)
    else Nil
  }

  def checkSpatialDimensions(spatialDimensions: java.lang.Integer):List[String] =
    spatialDimensions match {
      case null => Nil
      case x if(x<=3 && x>=0) => Nil
      case _ => List("SpatialDimensions must have a value between 0 and 3")
    }

  def checkSizeValIsValid(size: Double):List[String] = {
    size match {
      case null => Nil
      case s if(size <= 0) => List("Size must have a positive value")
      case _ => Nil
    }
  }
}

/**
 * Detects some inconsistencies that should be presented as warnings in a half finished model.
 * It only warns about inconsistencies within the smae SBML element. And for this reason, the visitor for a certain element
 * does not check its sub-elements
 */
object NonRecursSBMLValidator extends SBMLBeanVisitor[List[String]]{
  protected def visitModel(m: SBMLModel): List[String] =
    "TODO : WithinSBMLElementValidatorStub error" ::
    Nil

  protected def visitCompartment(c: Compartment): List[String] =
    if (c.spatialDimensions == 0 && c.size != null){
      List("The size attribute must not be present if the spatialDimensions attribute has a value of “0”")
    } else Nil

  protected def visitConstraint(ct: Constraint): List[String] = "TODO : NonRecursSBMLValidator Stub error" :: Nil

  protected def visitFunctionDefinition(fd: FunctionDefinition): List[String] = "TODO : NonRecursSBMLValidator Stub error" :: Nil

  protected def visitKineticLaw(kl: KineticLaw): List[String] = "TODO : NonRecursSBMLValidator Stub error" :: Nil

  protected def visitModifierSpeciesReference(msr: ModifierSpeciesReference): List[String] = "TODO : NonRecursSBMLValidator Stub error" :: Nil

  protected def visitParameter(p: Parameter): List[String] = "TODO : NonRecursSBMLValidator Stub error" :: Nil

  protected def visitReaction(r: Reaction): List[String] = "TODO : NonRecursSBMLValidator Stub error" :: Nil

  protected def visitSpecies(s: Species): List[String] = "TODO : NonRecursSBMLValidator Stub error" :: Nil

  protected def visitSpeciesReference(sr: SpeciesReference): List[String] = "TODO : NonRecursSBMLValidator Stub error" :: Nil
}

object SBMLValidatorForSimulation extends SBMLBeanVisitor[List[String]]{
  def visitModel(m: SBMLModel): List[String] = {
    helpers.XMLChecks.isValidIDType(m.metaid)  :::
      helpers.SBMLl2v4Checks.isValidSIdType(m.id) :::
    m.listOfFunctionDefinitions.map(visitFunctionDefinition(_) ).flatMap(i => i).toList :::
    m.listOfCompartments.map(visitCompartment(_) ).flatMap(i => i).toList :::
    m.listOfSpecies.map(visitSpecies(_) ).flatMap(i => i).toList :::
    m.listOfParameters.map(visitParameter(_) ).flatMap(i => i).toList :::
    m.listOfConstraints.map(visitConstraint(_) ).flatMap(i => i).toList :::
    m.listOfReactions.map(visitReaction(_) ).flatMap(i => i).toList ::: Nil
  }

  def visitCompartment(c: Compartment): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil

  def visitConstraint(ct: Constraint): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil

  def visitFunctionDefinition(fd: FunctionDefinition): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil

  def visitKineticLaw(kl: KineticLaw): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil

  def visitModifierSpeciesReference(msr: ModifierSpeciesReference): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil

  def visitParameter(p: Parameter): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil

  def visitReaction(r: Reaction): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil

  def visitSpecies(s: Species): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil

  def visitSpeciesReference(sr: SpeciesReference): List[String] = "TODO : SBMLValidatorForSimulation Stub error" :: Nil
}

package helpers {

  object XMLChecks extends XMLParser{

    /**
     * Checks if the string respects XML 1.0 ID type specification
     * This is the ID type of section 3.1.6 of the SBML specification
     */
    def isValidIDType(metaid:String):List[String] = {
      parseAll(Name, metaid) match {
        case Success(_,_) => Nil
        case Failure(_,_) => "" + metaid + " is not of type Id has defined in XML 1.0 specification" :: Nil
        case _ => {
          "Something is wrong with XML ID type validation " :: Nil
        }
      }
    }
  }

  object SBMLl2v4Checks extends SBMLParser{

    /**
    * Checks if the string respects the SID type of section 3.1.6 of the SBML specification
     */
    def isValidSIdType(id:String):List[String] = {
      parseAll( SId, id ) match {
        case Success(_,_) => Nil
        case Failure(_,_) => "" + id + " is not of type SId has defined in section 3.1.6 of the SBML specification" :: Nil
        case _ => {
          "Something is wrong with SBML SID type validation" :: Nil
        }
      }
    }
  }
}