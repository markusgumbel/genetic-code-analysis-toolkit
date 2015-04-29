package de.hsma.gentool.gui.editor;

import static de.hsma.gentool.Utilities.*;
import static de.hsma.gentool.nucleic.Acid.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import de.hsma.gentool.Utilities.Characters;
import de.hsma.gentool.nucleic.Acid;
import de.hsma.gentool.nucleic.Base;
import de.hsma.gentool.nucleic.Tuple;

public class NucleicDocument extends DefaultStyledDocument {
	private static final long serialVersionUID = 1l;

	private static Pattern patternTupleLength;
	
	private int defaultTupleLength;
	private Acid defaultAcid = RNA;
	
	@Override public void insertString(int offset, String text, AttributeSet attributes) throws BadLocationException {
		// only allow input of bases & with maximum one whitespace
		if((text=Tuple.tupleString(text)).isEmpty())
			return;
		
		// simplify input if only DNA or RNA input is chosen
		     if(DNA.equals(defaultAcid)) text = text.replace(Base.URACILE.letter, Base.THYMINE.letter);
		else if(RNA.equals(defaultAcid)) text = text.replace(Base.THYMINE.letter, Base.URACILE.letter);
		
		// determine position and white spaces in document / text
		boolean startOfText = offset==0, endOfText = offset==getLength(), nearEndOfText = endOfText||offset+1==getLength(),
			afterSpace = !startOfText&&SPACE.equals(getText(offset-1,1)), beforeSpace = !endOfText&&SPACE.equalsIgnoreCase(getText(offset,1)), beforeTwoSpace = beforeSpace&&!nearEndOfText&&SPACE.equalsIgnoreCase(getText(offset+1,1)),
			containsSpace = text.contains(SPACE), startsWithSpace = text.startsWith(SPACE), endsWithSpace = text.endsWith(SPACE);

		// white space handling
		if((startOfText||afterSpace)&&startsWithSpace)
			if((text = Characters.SPACE.trim(text,Characters.Trim.LEFT)).isEmpty())
				return;
			else startsWithSpace = false;
		if(((startOfText&&endOfText&&startsWithSpace)||beforeTwoSpace)&&endsWithSpace)
			if((text = Characters.SPACE.trim(text,Characters.Trim.RIGHT)).isEmpty())
				return;
			else endsWithSpace = false;
		
		// default tuple length input (di-nucleoide, codon, ...)
		if(defaultTupleLength>0) {
			StringBuilder builder = new StringBuilder();
			
			if(text.length()!=1) {
				Matcher matcher = patternTupleLength.matcher(text);
				while(matcher.find()) {
					if(builder.length()!=0)
						builder.append(SPACE);
					builder.append(matcher.group(1));
				}
				if(startsWithSpace) builder.insert(0,SPACE);
				if(endsWithSpace) builder.append(SPACE);
			} else builder.append(text);
			
			if(endOfText&&!endsWithSpace) {
				int currentTupleLength = builder.length()-builder.lastIndexOf(SPACE)-1;
				if(!containsSpace)
					for(int lookbackOffset=offset-1; lookbackOffset>=0; lookbackOffset--)
						if(!SPACE.equals(getText(lookbackOffset, 1)))
							currentTupleLength++;
						else break;
				if(currentTupleLength%defaultTupleLength==0)
					builder.append(SPACE);
			}
			
			text = builder.toString();
		}
		
		super.insertString(offset, text, attributes);
	}
	
	public int getDefaultTupleLength() { return defaultTupleLength; }
	public void setDefaultTupleLength(int defaultTupleLength) {
		if(defaultTupleLength>0)
			patternTupleLength = Pattern.compile(" ?(\\S{1,"+(this.defaultTupleLength=defaultTupleLength)+"}) *");
		else { this.defaultTupleLength = 0; patternTupleLength = null; }
	}
	
	public Acid getDefaultAcid() { return defaultAcid; }
	public void setDefaultAcid(Acid defaultAcid) {
		this.defaultAcid = defaultAcid;
	}
	
	public void adaptText() {
		try { replace(0,getLength(),getText(0,getLength()).replace(SPACE,EMPTY),null); }
		catch(BadLocationException e) { /* nothing to do here */ }
	}
}