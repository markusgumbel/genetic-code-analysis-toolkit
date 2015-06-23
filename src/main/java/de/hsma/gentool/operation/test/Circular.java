package de.hsma.gentool.operation.test;

import java.util.Arrays;
import java.util.Collection;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.ICombinatoricsVector;
import de.hsma.gentool.Parameter;
import de.hsma.gentool.Parameter.Type;
import de.hsma.gentool.log.Logger;
import de.hsma.gentool.nucleic.Tuple;
import de.hsma.gentool.operation.Cataloged;
import de.hsma.gentool.operation.Named;
import de.hsma.gentool.operation.transformation.ShiftSequence;
import de.hsma.gentool.operation.transformation.Transformation;

@Named(name="n-circular?") @Cataloged(group="Tests")
@Parameter.Annotation(key="n",label="n-Circular",type=Type.NUMBER,value="1,10")
public class Circular implements Test {	
	private static final Test
		DUPLICATE_FREE = new DuplicateFree();
	private static final Transformation
		SHIFT = new ShiftSequence();
	
	@Override public boolean test(Collection<Tuple> tuples,Object... values) { return test(tuples,(Integer)values[0]); }
	public boolean test(Collection<Tuple> tuples,int n) {
		Logger logger = getLogger();
		
		if(tuples.isEmpty())
			return true; //an empty set of tuples is comma-free
		
		int length;
		if((length=Tuple.tuplesLength(tuples))==0) {
			logger.log("Tuples of variable length, can't check for "+n+"-circular.");
			return false; //tuples not all of same length
		}
		
		if(!DUPLICATE_FREE.test(tuples)) {
			logger.log("Duplicate tuples in sequence, code not "+n+"-circular.");
			return false; //duplicate tuples
		}
		
		if(n<=0) return true;
		else if(n==1) {
			int shift; Tuple shifted;
			for(Tuple tuple:tuples) for(shift=1,shifted=tuple;shift<length;shift++)
				if(tuples.contains(shifted = SHIFT.transform(Arrays.asList(tuple)).iterator().next())) {
					logger.log((!tuple.equals(shifted)?"Tuples "+tuple+" and "+shifted+" belong to the same equivalence class":
						"Tuple "+tuple+" is contained in sequence")+", code not 1-circular.");
					return false; //lemma 3.2, is 1-circular if and only if X contains at most one codon from each complete conjugacy class
				}
		} else {
			if(!test(tuples,n-1))
				return false; //lemma 3.2, if X is a n-circularcode, then X is also m-circular for all m<n
			
			Collection<Tuple> shifted;
			for(ICombinatoricsVector<Tuple> combination:Factory.createSimpleCombinationGenerator(Factory.createVector(tuples),n))
				for(ICombinatoricsVector<Tuple> permutation:Factory.createPermutationGenerator(combination))
					for(int shift=1;shift<length;shift++)
						if(tuples.containsAll(shifted=SHIFT.transform(permutation.getVector(),shift))) {
							logger.log("Partition "+permutation.getVector()+" and shift "+shifted+" contained in sequence, code not "+n+"-circular.");
							return false;
						}
		}

		return true;
	}
}