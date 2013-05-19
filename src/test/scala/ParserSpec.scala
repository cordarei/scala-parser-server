import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class ParserSpec extends FunSpec with ShouldMatchers {

  it("should return collapsed dependencies") {

    val sentence = "The strongest rain ever recorded in India shut down the financial hub of Mumbai , snapped communication lines , closed airports and forced thousands of people to sleep in their offices or walk home during the night , officials said today ."
    val parser = new Parser
    val result = parser.parse(sentence) match { case Right(r) => r }
    val tokens = result.tokens
    val relations = result.rels

    relations should contain(GrammaticalRelation("prep_in", 4, 6))
  }

  it("should return pos tags from the parser") {
    val sentence = "It was the cat in the hat ."
    val parser = new Parser
    val result = parser.parse(sentence) match { case Right(r) => r }
    val tokens = result.tokens
    tokens should contain(Token("It", Some("PRP"), 0))
  }
}
