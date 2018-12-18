package de.unijena.cheminf.npdatabasefiller.model;

import de.unijena.cheminf.npdatabasefiller.model.Molecule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository

public interface MoleculeRepository extends CrudRepository<Molecule, Integer> {


    List<Molecule> findAll();

    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT(mol_id)) FROM molecule WHERE mol_id= :mol_id")
    Integer findAtomCountByMolId(@Param("mol_id") Integer mol_id);

    @Query(nativeQuery = true, value = "SELECT * FROM molecule INNER JOIN ori_molecule ON(mol_id=unique_mol_id) WHERE source='OLD2012'")
    List<Molecule> findMoleculesFromOLD2012();

    Molecule findByInchikey(String inchikey);


}
