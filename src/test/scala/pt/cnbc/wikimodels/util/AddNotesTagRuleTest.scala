/*
 * AddNotesTagRuleTest.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pt.cnbc.wikimodels.util

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import scala.xml.NodeSeq
import scala.xml.Node
import alexmsmartins.log.LoggerWrapper

class AddNotesTagRuleTest extends LoggerWrapper {


    @Before
    def setUp: Unit = {
    }

    @After
    def tearDown: Unit = {
    }

    @Test
    def transform:Unit = {
        val html:Seq[Node] = Seq( <p><h1>Title</h1></p>,
                                 <p>first paragraph</p>)
        val finalhtml =
        <notes><p xmlns='http://www.w3c.org/1999/xhtml'><h1>Title</h1></p><p xmlns='http://www.w3c.org/1999/xhtml' >first paragraph</p></notes> ;
        val transf:Seq[Node] = (new AddNotesTagRule()).transform(html)
        debug("html before transformation -> " + html.toString )
        debug("html transformation -> " + transf.toString )
        debug("anticipated notes -> " + finalhtml.toString )
        assertTrue( transf == finalhtml )
    }
}
