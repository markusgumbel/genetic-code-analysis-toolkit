package net.gumbix.geneticcode.dich.ct

import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConversions.mapAsScalaMap

import net.gumbix.geneticcode.core.CodonMapping
import net.gumbix.geneticcode.dich.Classifier
import net.gumbix.geneticcode.dich.Codon
import net.gumbix.geneticcode.dich.IUPAC
import net.gumbix.lal.F2Vector
import net.gumbix.lal.F2VectorSpace
import net.gumbix.util.Combine.combine

/**
 * @author Markus Gumbel (m.gumbel@hs-mannheim.de)
 *         (c) 2012 Markus Gumbel
 */
class CodingClassTable(override val bdas: List[Classifier[Int]],
                       override val iupacNumber: Int = IUPAC.EUPLOTID_NUCLEAR)
  extends ClassTable(bdas, iupacNumber) {

  val f2 = new F2VectorSpace(bdas.size)

  /**
   * The classes as F2-vectors.
   * @return
   */
  def f2Vectors = classes.map(c => F2Vector(c: _*)).toList

  lazy val isVectorSubSpace = f2.isSubSpace(f2Vectors)

  lazy val isMovedVectorSubSpace = f2.isMovedSubSpace(f2Vectors)

  /**
   * Hamming distance for a list of 0s and 1s. Empty list gives 0.
   * List must be of the same length.
   * @param l1
   * @param l2
   * @return
   */
  def hammingLDist(l1: List[Int], l2: List[Int]) = {
    if (l1.size != l2.size) throw
      new IllegalArgumentException("Lists must be of the same size: " +
        l1.size + "<>" + l2.size)
    val l = l1.zip(l2)
    l.filter(p => p._1 != p._2).size
  }

  /**
   * Hamming distance between two codons according the to classes
   * generated by the BDAs.
   * @param c1
   * @param c2
   * @return
   */
  def hammingCDist(c1: Codon, c2: Codon) = {
    val l1 = codon2class(c1)
    val l2 = codon2class(c2)
    hammingLDist(codon2class(c1), codon2class(c2))
  }

  /**
   * Hamming distance of all codons. The list is ordered such
   * that for two elements always hold: i<j.
   */
  lazy val hammingDist: List[((Codon, Codon), Int)] = {
    val c = combine(codons)
    for ((c1, c2) <- c) yield {
      val h = hammingCDist(c1, c2)
      ((c1, c2), h)
    }
  }

  /**
   * Minimum and maximum hamming distances for all codons.
   */
  lazy val hammingDistMinMax =
    MinMax(hammingDist.minBy(x => x._2)._2, hammingDist.maxBy(x => x._2)._2)

  /**
   * The hamming distance grouped by amino acids.
   */
  lazy val hammingDistanceGrouped:
  Map[(CodonMapping, CodonMapping), List[((Codon, Codon), Int)]] = {

    // Can be used to make generic group function:
    def group(e: ((Codon, Codon), Int)) = {
      val (c1, c2) = e._1
      val aa1 = codon2AA(c1)
      val aa2 = codon2AA(c2)
      (aa1, aa2)
    }
    hammingDist.groupBy(group)
  }

  lazy val hammingDistanceGroupedMinMax:
  Map[(CodonMapping, CodonMapping), MinMax] = {
    hammingDistanceGrouped.mapValues {
      v =>
        MinMax(v.minBy(e => e._2)._2, v.maxBy(e => e._2)._2)
    }
  }

  /**
   * Filter amino acid pairs according to a predicate <code>compare</code>.
   * Then return the min. and max. range.
   * @param compare
   * @return
   */
  private def extHammingDistPerAA(compare: (String, String) => Boolean) = {
    // z contains the intended amino acid pairs:
    val z = hammingDistanceGroupedMinMax.filterKeys {
      e =>
        val (a1, a2) = e
        // TODO do not use toString
        compare(a1.toString, a2.toString)
    }
    // Find the pair with the smallest min-value:
    val min = z.minBy {
      e =>
        val ((c1, c2), h) = e
        h.min
    }
    // ... and the pair with the greatest max-value:
    val max = z.maxBy {
      e =>
        val ((c1, c2), h) = e
        h.max
    }
    MinMax(min._2.min, max._2.max) // range
  }

  lazy val hammingDistInAA = extHammingDistPerAA((s1, s2) => s1 == s2)

  lazy val hammingDistBtwAA = extHammingDistPerAA((s1, s2) => s1 != s2)

  override def mkFullString() = {
    super.mkFullString +
    "\nSub space = " + isVectorSubSpace +
    "\nMoved sub space = " + isMovedVectorSubSpace +
    "\nh (in) = " + hammingDistInAA +
    "\nh (btw.) = " + hammingDistBtwAA +
    "\nh_g = " + hammingDistanceGrouped +
    "\nh_g_min_max = " + hammingDistanceGroupedMinMax
  }
}

case class MinMax(min: Int, max: Int) {
  override def toString = min + "..." + max
}