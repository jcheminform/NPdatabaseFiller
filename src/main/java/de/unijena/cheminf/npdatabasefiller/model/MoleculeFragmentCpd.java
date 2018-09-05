package de.unijena.cheminf.npdatabasefiller.model;


import javax.persistence.*;

@Entity
@Table(name="molecule_fragment_cpd" ,
        indexes = {
        @Index(name = "IDX1", columnList = "mol_id, fragment_id" ) ,
                @Index(name="IDX2", columnList ="mol_id"),
                @Index(name="IDX3", columnList="fragment_id")  })
public class MoleculeFragmentCpd {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer mfc_id;


    private Integer mol_id;

    private Integer fragment_id;

    private Integer height;

    private boolean contains_sugar;




    public Integer getMol_id() {
        return mol_id;
    }

    public void setMol_id(Integer mol_id) {
        this.mol_id = mol_id;
    }

    public Integer getFragment_id() {
        return fragment_id;
    }

    public void setFragment_id(Integer fragment_id) {
        this.fragment_id = fragment_id;
    }

    public Integer getMfc_id() {
        return mfc_id;
    }

    public void setMfc_id(Integer mfc_id) {
        this.mfc_id = mfc_id;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public boolean contains_sugar() {
        return contains_sugar;
    }

    public void setContains_sugar(boolean contains_sugar) {
        this.contains_sugar = contains_sugar;
    }
}
