/*
 * Copyright [2014] [Mannheim University of Applied Sciences]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package de.hsma.gentool.operation.test;

import java.util.Collection;
import de.hsma.gentool.log.Logger;
import de.hsma.gentool.nucleic.Acid;
import de.hsma.gentool.nucleic.Tuple;
import de.hsma.gentool.operation.Cataloged;
import de.hsma.gentool.operation.Named;

@Named(name="self-complementary", icon="style_go") @Cataloged(group="Tests")
public class SelfComplementary implements Test {
	@Override public boolean test(Collection<Tuple> tuples,Object... values) {
		Logger logger = getLogger();
		
		if(tuples.isEmpty())
			return true; //an empty set of tuples is self complementary
		
		Acid acid;
		if((acid=Tuple.tuplesAcid(tuples))==null) {
			logger.log("Tuples with variable acids, can't check for self complementary.");
			return false; //tuples not all in same acid
		}
		
		Tuple complement;
		for(Tuple tuple:tuples)
			if(!tuples.contains(complement=tuple.getComplement(acid))) {
				logger.log("Complement tuple "+complement+" is not contained in sequence, code not self complementary.");
				return false; //contains a complement, not self complementary
			}
		
		return true;
	}
}