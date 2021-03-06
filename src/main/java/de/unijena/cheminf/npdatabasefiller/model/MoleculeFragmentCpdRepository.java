/*
 * Copyright (c) 2019.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.unijena.cheminf.npdatabasefiller.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MoleculeFragmentCpdRepository  extends CrudRepository<MoleculeFragmentCpd, Integer> {

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd WHERE fragment_id= :fragment_id AND height= :height AND computed_with_sugar= :computed_with_sugar  ")
    List<Object[]> countDistinctMoleculesByFragmentIdAndHeightAndAndComputedWithSugar(@Param("fragment_id") Integer fragment_id, @Param("height") Integer height, @Param("computed_with_sugar") Integer computed_with_sugar);





    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE fragment_id= :fragment_id AND height= :height AND computed_with_sugar= :computed_with_sugar AND is_a_np=1 ")
    List<Object[]> countDistinctNPMoleculesByFragmentIdAndHeightAndAndComputedWithSugar(@Param("fragment_id") Integer fragment_id, @Param("height") Integer height, @Param("computed_with_sugar") Integer computed_with_sugar);


    @Query(nativeQuery = true, value="SELECT SUM(nbfragmentinmolecule) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE fragment_id= :fragment_id AND height= :height AND computed_with_sugar= :computed_with_sugar AND is_a_np=1")
    List<Object[]> countTotalOccurenciesInNPMoleculesByFragmentIdAndHeightAndComputedWithSugar(@Param("fragment_id") Integer fragment_id, @Param("height") Integer height, @Param("computed_with_sugar") Integer computed_with_sugar);




    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE fragment_id= :fragment_id AND height= :height AND computed_with_sugar= :computed_with_sugar AND is_a_np=0 ")
    List<Object[]> countDistinctSMMoleculesByFragmentIdAndHeightAndAndComputedWithSugar(@Param("fragment_id") Integer fragment_id, @Param("height") Integer height, @Param("computed_with_sugar") Integer computed_with_sugar);


    @Query(nativeQuery = true, value="SELECT SUM(nbfragmentinmolecule) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE fragment_id= :fragment_id AND height= :height AND computed_with_sugar= :computed_with_sugar AND is_a_np=0")
    List<Object[]> countTotalOccurenciesInSMMoleculesByFragmentIdAndHeightAndComputedWithSugar(@Param("fragment_id") Integer fragment_id, @Param("height") Integer height, @Param("computed_with_sugar") Integer computed_with_sugar);





    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd WHERE height= :height AND computed_with_sugar=1")
    List<Object[]> countTotalMoleculesWithSugar(@Param("height")Integer height);

    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd WHERE height= :height AND computed_with_sugar=0")
    List<Object[]> countTotalMoleculesWithoutSugar(@Param("height")Integer height);

    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE is_a_np=1")
    List<Object[]> countTotalNPMolecules();

    @Query(nativeQuery = true, value="SELECT COUNT(DISTINCT mol_id) FROM molecule_fragment_cpd INNER JOIN molecule USING(mol_id) WHERE is_a_np=0")
    List<Object[]> countTotalSMMolecules();

    @Query(nativeQuery = true, value="SELECT * FROM molecule_fragment_cpd WHERE fragment_id= :fragment_id")
    List<MoleculeFragmentCpd> findByfragment_id(@Param("fragment_id")Integer fragment_id);


    @Query(nativeQuery = true, value="SELECT f.fragment_id, cpd.nbfragmentinmolecule, f.scorenp, f.signature  " +
            "FROM molecule_fragment_cpd cpd INNER JOIN fragment_with_sugar f USING(fragment_id) " +
            "WHERE cpd.computed_with_sugar=1 AND f.height= :height AND cpd.mol_id= :mol_id")
    List<Object[]> findAllSugarFragmentsByMolid(@Param("mol_id") Integer mol_id, @Param("height") Integer height );

    @Query(nativeQuery = true, value="SELECT f.fragment_id, cpd.nbfragmentinmolecule, f.scorenp, f.signature " +
            "FROM molecule_fragment_cpd cpd INNER JOIN fragment_without_sugar f USING(fragment_id) " +
            "WHERE cpd.computed_with_sugar=0 AND f.height= :height AND cpd.mol_id= :mol_id")
    List<Object[]> findAllSugarfreeFragmentsByMolid(@Param("mol_id") Integer mol_id, @Param("height") Integer height );




}
