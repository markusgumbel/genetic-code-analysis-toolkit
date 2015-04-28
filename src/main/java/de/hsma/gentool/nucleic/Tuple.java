package de.hsma.gentool.nucleic;

import static de.hsma.gentool.Utilities.*;
import static de.hsma.gentool.nucleic.Acid.*;
import static de.hsma.gentool.nucleic.Base.*;
import static de.hsma.gentool.nucleic.Compound.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.ImmutableMap;
import de.hsma.gentool.Utilities.Characters;

public class Tuple implements Comparable<Tuple> {
	private static final Map<Base,Base> COMPLEMENT_SUBSTITUTION = ImmutableMap.of(
		ADENINE,URACILE, URACILE,ADENINE, THYMINE,ADENINE, GUANINE,CYTOSINE, CYTOSINE,GUANINE);
	private static final Map<Acid,Map<Base,Base>> ACID_SUBSTITUTIONS = ImmutableMap.of(
		RNA,ImmutableMap.of(THYMINE,URACILE), DNA,ImmutableMap.of(URACILE,THYMINE));
	private static final Pattern PATTERN_NO_BASE;
	static {
		StringBuilder bases = new StringBuilder();
		for(Base base:Base.values())
			bases.append(base.letter);
		PATTERN_NO_BASE = Pattern.compile(bases.insert(0,"[^").append(" ]").toString());
	}
	
	private Base[] bases;
	private String string;
	
	public Tuple() { this(EMPTY); }
	public Tuple(String string) {
		this.bases = Base.parseBase(this.string=string.toUpperCase());
	}
	public Tuple(Base... bases) {
		this.string = Base.toString(this.bases=bases);
	}
	
	public Base[] getBases() { return bases; }
	public Compound getCompound() { return Compound.forTuple(this); }
	
	public int length() { return string.length(); }
	
	public Tuple toAcid(Acid acid) {
		return new Tuple(substitute(bases,ACID_SUBSTITUTIONS.get(acid)));
	}
	
	public Tuple getComplement() { return getComplement(RNA); }
	public Tuple getComplement(Acid acid) {
		return new Tuple(reverse(substitute(bases,COMPLEMENT_SUBSTITUTION))).toAcid(acid);
	}
	
	@Override public int compareTo(Tuple tuple) {
		int base;
		for(base=0;base<this.bases.length;base++) {
			if(tuple.bases.length>=base) return 1;
			int compare = this.bases[base].compareTo(tuple.bases[base]);
			if(compare!=0) return compare;
		}
		return this.bases.length==tuple.bases.length?0:-1;
	}
	
	@Override public int hashCode() { return string.hashCode(); }
	@Override public boolean equals(Object anObject) {
		if(anObject==this)
			return true;
		if(!(anObject instanceof Tuple))
			return false;
		return string.equals(((Tuple)anObject).string);
	}
	@Override public String toString() { return toString(false); }
	public String toString(boolean appendCompound) {
		if(appendCompound) {
			StringBuilder builder = new StringBuilder(toString()).append(SPACE).append('(');
			Compound compound = getCompound();
			if(compound==null) {
				if(isStart(this)) builder.append(START);
				else if(isStop(this)) builder.append(STOP);
				else builder.append("Unknown");
			} else builder.append(compound);
			return builder.append(')').toString();
		} return string;
	}
	
	public static String tupleString(String string) {
		return Characters.WHITESPACE.condense(
			PATTERN_NO_BASE.matcher(string.toUpperCase()).replaceAll(SPACE));
	}
	
	public static List<Tuple> uniformAcid(List<Tuple> tuples) { return uniformAcid(tuples,DNA); }
	public static List<Tuple> uniformAcid(List<Tuple> tuples, Acid acid) {
		List<Tuple> uniformTuples = new ArrayList<>(tuples);
		ListIterator<Tuple> tuple = uniformTuples.listIterator();
		while(tuple.hasNext()) tuple.set(tuple.next().toAcid(acid));
		return uniformTuples;
	}
	
	public static List<Tuple> splitTuples(String string) {
		List<Tuple> tuples = new ArrayList<Tuple>();
		StringTokenizer strings = new StringTokenizer(string,WHITESPACE+",;");
		while(strings.hasMoreTokens())
			try { tuples.add(new Tuple(strings.nextToken())); }
			catch(IllegalArgumentException e) { tuples.add(null); }
		return tuples;
	}
	public static List<Tuple> splitTuples(String[] strings) {
		List<Tuple> tuples = new ArrayList<Tuple>();
		for(String string:strings) tuples.addAll(splitTuples(string));
		return tuples;
	}
	
	public static List<Tuple> sliceTuples(String string, int length) {
		List<Tuple> tuples = new ArrayList<Tuple>();
		Matcher tuple = Pattern.compile(".{"+length+"}|.*$").matcher(Characters.WHITESPACE.replace(string,EMPTY));
		while(tuple.find()) tuples.add(new Tuple(tuple.group()));
		return tuples;
	}
	public static List<Tuple> sliceTuples(String[] strings, int length) {
		List<Tuple> tuples = new ArrayList<Tuple>();
		for(String string:strings) tuples.addAll(sliceTuples(string, length));
		return tuples;
	}
	
	public static String joinTuples(Collection<Tuple> tuples) { return joinTuples(tuples, SPACE); }
	public static String joinTuples(Collection<Tuple> tuples, String glue) { return joinTuples(tuples, glue, false); }
	public static String joinTuples(Collection<Tuple> tuples, boolean appendCompounds) { return joinTuples(tuples, SPACE, appendCompounds); } 
	public static String joinTuples(Collection<Tuple> tuples, String glue, boolean appendCompounds) {
		StringBuilder builder = new StringBuilder();
		if(tuples!=null&&tuples.size()!=0) {
			for(Tuple tuple:tuples) if(tuple!=null)
				builder.append(glue).append(tuple.toString(appendCompounds));
			return builder.delete(0,glue.length()).toString();
		} else return EMPTY;
	}
	
	public static List<Tuple> allTuples(int length) { return allTuples(RNA, length); }
	public static List<Tuple> allTuples(Acid acid) { return allTuples(acid, 3); }
	public static List<Tuple> allTuples(Acid acid, int length) {
		List<Tuple> tuples = new ArrayList<>(pow(length,acid.bases.length));
		allTuples(tuples, new Tuple(), acid.bases, length);
		return tuples;
	}
	protected static void allTuples(Collection<Tuple> tuples, Tuple tuple, Base[] bases, int length) {
		if(tuple.string.length()<length)
			for(Base base:bases)
				allTuples(tuples,new Tuple(tuple.string+base.letter),bases,length);
		else tuples.add(tuple);
	}
}