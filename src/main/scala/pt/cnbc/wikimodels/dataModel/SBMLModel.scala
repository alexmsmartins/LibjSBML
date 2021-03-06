/*
 * Model.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 * @author Alexandre Martins
 */

package pt.cnbc.wikimodels.dataModel

import java.util.Date

import scala.collection.JavaConversions._

import scala.reflect.BeanInfo
import scala.xml.Elem
import scala.xml.Group
import scala.xml.MetaData
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.UnprefixedAttribute
import scala.xml.XML

import thewebsemantic.Id
import thewebsemantic.Namespace
import thewebsemantic.RdfProperty
import thewebsemantic.RdfType

import pt.cnbc.wikimodels.util.SBMLHandler
import pt.cnbc.wikimodels.exceptions.BadFormatException


/**
 * Represents a user of WikiModels
 */

@BeanInfo
@Namespace("http://wikimodels.cnbc.pt/ontologies/sbml.owl#")
@RdfType("Model")
case class SBMLModel() extends Element {
  override final val sbmlType = "Model"
  var id: String = null
  var name: String = null

  //listOf definitions
  @RdfProperty("http://wikimodels.cnbc.pt/ontologies/sbml.owl#hasFunctionDefinition")
  var listOfFunctionDefinitions: java.util.Collection[FunctionDefinition] = Set.empty[FunctionDefinition]
  //var listOfUnitDefinitions:List[ÛnitDefinition] = List()
  //var listOfCompartmentTypes:List[CompartmentType] = List()
  //var listOfSpeciesTypes:List[SpeciesType] = List()
  @RdfProperty("http://wikimodels.cnbc.pt/ontologies/sbml.owl#hasCompartment")
  var listOfCompartments: java.util.Collection[Compartment] = Set.empty[Compartment]
  @RdfProperty("http://wikimodels.cnbc.pt/ontologies/sbml.owl#hasSpecies")
  var listOfSpecies: java.util.Collection[Species] = Set.empty[Species]
  @RdfProperty("http://wikimodels.cnbc.pt/ontologies/sbml.owl#hasParameter")
  var listOfParameters: java.util.Collection[Parameter] = Set.empty[Parameter]
  //var listOfInitialAssignments:List[InitialAssignment] = List()
  //TODO listOfRules is missing
  //var listOfRules: java.util.Collection[Rules] = null
  @RdfProperty("http://wikimodels.cnbc.pt/ontologies/sbml.owl#hasConstraint")
  var listOfConstraints: java.util.Collection[Constraint] = Set.empty[Constraint]
  @RdfProperty("http://wikimodels.cnbc.pt/ontologies/sbml.owl#hasReaction")
  var listOfReactions: java.util.Collection[Reaction] = Set.empty[Reaction]
  //var listOfEvents:List[Event] = List()

  def this(metaid: String,
           notes: NodeSeq,
           id: String,
           name: String) = {
    this ()
    this.metaid = metaid
    this.setNotesFromXML(notes)
    this.id = id
    this.name = name
    //if( ! SBMLHandler.idExistsAndIsValid(this.metaid) ) throw new BadFormatException("MetaId is invalid.")
    SBMLHandler.idExistsAndIsValid(this.id)
  }

  /**
   * generates a XML representation of the SBML Model
   * it does not include the DOCTYPE declaration or the sbml top tag
   * Those should be added by a proper export funtion when it is created
   * @return the XML representing the user
   */


  override def toXML(): Elem = {
    Console.println("SBMLModel.toXML is including the following notes in the model: " + notes)
    <model metaid={metaid} id={id} name={name}>
      <!--order is important according to SBML Specifications-->
      {Console.println("Notes of the model are " + notes)}
      {SBMLHandler.genNotesFromHTML(notes)}
      {if (listOfFunctionDefinitions != null && listOfFunctionDefinitions.size != 0)
        <listOfFunctionDefinitions>
          {listOfFunctionDefinitions.map(i => i.toXML)}
        </listOfFunctionDefinitions> else scala.xml.Null}
      {if (false)
        <listOfUnitDefinitions>
        </listOfUnitDefinitions> else scala.xml.Null}
      {if (false)
        <listOfCompartmentTypes>
        </listOfCompartmentTypes> else scala.xml.Null}
      {if (false)
        <listOfSpeciesTypes>
        </listOfSpeciesTypes> else scala.xml.Null}
      {if (listOfCompartments != null && listOfCompartments.size != 0)
        <listOfCompartments>
          {listOfCompartments.map(i => i.toXML)}
        </listOfCompartments> else scala.xml.Null}
      {if (listOfSpecies != null && listOfSpecies.size != 0)
        <listOfSpecies>
          {listOfSpecies.map(i => i.toXML)}
        </listOfSpecies> else scala.xml.Null}
      {if (listOfParameters != null && listOfParameters.size != 0)
        <listOfParameters>
          {listOfParameters.map(i => i.toXML)}
        </listOfParameters> else scala.xml.Null}
      {if (false)
        <listOfInitialAssignments>
        </listOfInitialAssignments> else scala.xml.Null}
      {if (false)
        <listOfRules>
        </listOfRules> else scala.xml.Null}
      {if (listOfConstraints != null && listOfConstraints.size != 0)
        <listOfConstraints>
          {listOfConstraints.map(i => i.toXML)}
        </listOfConstraints> else scala.xml.Null}
      {if (listOfReactions != null && listOfReactions.size != 0)
        <listOfReactions>
          {listOfReactions.map(i => i.toXML)}
        </listOfReactions> else scala.xml.Null}
      {if (false)
        <listOfEvents>
        </listOfEvents> else scala.xml.Null}
    </model>
  }

  override def theId = this.id

  override def theName = this.name
}

object SBMLModel{

}
