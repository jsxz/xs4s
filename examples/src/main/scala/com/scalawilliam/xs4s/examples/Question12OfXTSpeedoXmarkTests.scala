package com.scalawilliam.xs4s.examples

import java.io.{File, FileInputStream, InputStream}
import javax.xml.stream.XMLInputFactory

import com.scalawilliam.xs4s.XmlElementExtractor
import com.scalawilliam.xs4s.Implicits._

object Question12OfXTSpeedoXmarkTests extends App {

  /** Doing the same thing as the following XSLT:
    * https://github.com/Saxonica/XT-Speedo/blob/master/data/xmark-tests/q12.xsl
    */
  val xmlInputFactory = XMLInputFactory.newInstance()

  // xmark1 XML file - 100MB or so - get it from XT Speedo chaps
  def fileAsInputStream = new FileInputStream(new File("downloads/xmark4.xml"))

  case class InitialOpen(value: Double)

  case class Person(name: String, income: Double)

  val splitter = XmlElementExtractor {
    case List("site", "open_auctions", "open_auction", "initial") =>
      initialElement =>
        Seq(InitialOpen(initialElement.text.toDouble))
    case List("site", "people", "person") =>
      personElement => for {
        name <- personElement \ "name" map (_.text)
        income = (personElement \ "profile" \ "@income").map(_.text.toDouble).headOption.getOrElse(0.0)
      } yield Person(name, income)
  }

  println(testInput(fileAsInputStream))

  def testInput(inputStream: InputStream): scala.xml.Elem = {

    val collectedData = {
      val xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream)

      try xmlEventReader
        .toIterator
        .scanCollect(splitter.Scan)
        .toList
        .flatten
      finally xmlEventReader.close()
    }

    <out>
      {for {
      Person(name, income) <- collectedData
      noItems = collectedData.count {
        case InitialOpen(value) if income > 5000 * value => true;
        case _ => false
      }
    } yield <items name={name}>
      {noItems.toString}
    </items>}
    </out>
  }

}
