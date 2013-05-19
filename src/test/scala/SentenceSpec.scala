import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class SentenceSpec extends FunSpec with ShouldMatchers {

  describe("Sentence") {

    it("should tokenize an untagged sentence"){

      val s = "I saw a man with a dog ."

      val tokens = Sentence.tokenize(s)

      tokens should be === List[Token](
        Token("I", None, 0),
        Token("saw", None, 2),
        Token("a", None, 6),
        Token("man", None, 8),
        Token("with", None, 12),
        Token("a", None, 17),
        Token("dog", None, 19),
        Token(".", None, 23))
    }

    it("should tokenize a tagged sentence"){

      val s = "I/PRP saw/VBD a/DT man/NN with/IN a/DT dog/NN ./."

      val tokens = Sentence.tokenize(s)

      tokens should be === List[Token](
        Token("I", Some("PRP"), 0),
        Token("saw", Some("VBD"), 6),
        Token("a", Some("DT"), 14),
        Token("man", Some("NN"), 19),
        Token("with", Some("IN"), 26),
        Token("a", Some("DT"), 34),
        Token("dog", Some("NN"), 39),
        Token(".", Some("."), 46))
    }

    it("should tokenize a partially tagged sentence") {

      val s = "I saw/VBD a/DT man with/IN a/DT dog ./."

      val tokens = Sentence.tokenize(s)

      tokens should be === List[Token](
        Token("I", None, 0),
        Token("saw", Some("VBD"), 2),
        Token("a", Some("DT"), 10),
        Token("man", None, 15),
        Token("with", Some("IN"), 19),
        Token("a", Some("DT"), 27),
        Token("dog", None, 32),
        Token(".", Some("."), 36))
    }
  }
}
