package de.hsma.gentool.operation.test;

import java.util.Collection;
import de.hsma.gentool.Parameter;
import de.hsma.gentool.nucleic.Tuple;
import de.hsma.gentool.operation.Cataloged;
import de.hsma.gentool.operation.Named;
import de.hsma.gentool.operation.transformation.CommonSubstitution;
import de.hsma.gentool.operation.transformation.Transformation;

@Named(name="invariant to", icon="book_next") @Cataloged(group="Tests")
public class CommonTest implements Test {
	private static final Transformation
		COMMON = new CommonSubstitution();
	
	public static Parameter[] getParameters() { return CommonSubstitution.getParameters(); }
	
	@Override public boolean test(Collection<Tuple> tuples,Object... values) { return test(tuples,(String)values[0]); }
	public boolean test(Collection<Tuple> tuples,String name) {
		Collection<Tuple> transformed = COMMON.transform(tuples,name);
		return tuples.containsAll(transformed)&&transformed.containsAll(tuples);
	}
}