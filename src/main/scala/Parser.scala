import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.process.PTBEscapingProcessor
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.parser.lexparser.LexicalizedParser

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer


case class Token(val token : String, val tag : Option[String], val offset : Int)


object Sentence {
  private val taggedTokenPattern = """^(.*)(?<!\\)/([A-Z$\.:-]+)$|^(.*)$""".r

  def tokenize(sentence : String) : List[Token] = {
    // this implementation is being paranoid about double spaces:
    // we can either return (or discard?) empty tokens and preserve offsets
    // in the original String or use String#split and have no empty tokens
    // but offsets may be incorrect
    val spaces = (for ( i <- (0 to sentence.length - 1) if sentence(i) == ' ') yield i).toList
    val begins = 0 :: spaces.map(_+1)
    val ends = spaces ::: sentence.length :: Nil

    begins zip ends map (
      be => be match { case (b, e) => (b, sentence.substring(b, e)) }
      ) map (
        os => os match { case (offset, s) => s match {
        case taggedTokenPattern(null, null, token) => Token(token, None, offset)
        case taggedTokenPattern(token, tag, null) => Token(token, Some(tag), offset)
        } }
      ) toList ; //semicolon to disambiguate to postfix operator usage (self-pedagogical note)
  }
}


case class GrammaticalRelation(val relation : String, val headTokenIndex : Int, val depTokenIndex : Int)


trait ParserError ;
case object NoMemory extends ParserError ;
case object Skipped extends ParserError ;
case object Unparsable extends ParserError ;


case class ParseResult(val tokens : List[Token],
                       val rels : List[GrammaticalRelation])

class Parser {
  private val model = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"
  private val parser = LexicalizedParser.loadModel(model, "-retainTmpSubcategories")
  private val gsf = parser.treebankLanguagePack.grammaticalStructureFactory
  private val escaper = new PTBEscapingProcessor()

  def parse(sentence : String) : Either[ParserError, ParseResult] = {
    val tokens = Sentence.tokenize(sentence)
    val query = parser.lexicalizedParserQuery
    val stanfordWordList = edu.stanford.nlp.ling.Sentence.toWordList(tokens.map(t=>escaper.escapeString(t.token)):_*)
    val succeeded = query.parse(stanfordWordList)
    if (succeeded) {
      val parseTree = query.getBestParse
      val rels = getGRs(parseTree)
      Right(ParseResult(updateTokensFromParse(tokens, parseTree), rels))
    } else {
      (query.parseNoMemory, query.parseSkipped, query.parseUnparsable) match {
        case (true, _, _) => Left(NoMemory)
        case (false, true, _) => Left(Skipped)
        case _ => Left(Unparsable)
      }
    }
  }

  private def updateTokensFromParse(tokens : List[Token], parseTree : Tree) : List[Token] = {
    val taggedWords = parseTree.taggedYield
    (for (pair <- tokens.zip(taggedWords)) yield {
      Token(pair._1.token, Some(pair._2.tag), pair._1.offset)
    }) toList ;
  }

  private def getGRs(parseTree : Tree) : List[GrammaticalRelation] = {
    val gs = gsf.newGrammaticalStructure(parseTree)
    val deps = List(gs.typedDependenciesCCprocessed(true):_*)
    def relnString(reln:edu.stanford.nlp.trees.GrammaticalRelation) : String = {
      reln.getSpecific() match {
        case null => reln.getShortName()
        case s => reln.getShortName() + "_" + s
      }
    }
    deps.map(dep => GrammaticalRelation(relnString(dep.reln), dep.gov.index - 1, dep.dep.index - 1))
  }
}
