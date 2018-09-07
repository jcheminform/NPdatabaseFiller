package de.unijena.cheminf.npdatabasefiller.model;

import javax.persistence.*;

@Entity
@Table(name="fragment_without_sugar", indexes = {  @Index(name = "IDX1", columnList = "signature"), @Index(name="IDX2", columnList = "height, signature") } )
public class FragmentWithoutSugar {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer fragment_id;

    @Column(length = 300)
    private String signature;

    private Integer height;

    private Double scoreNP;

    private Double scoreSM;

    public Integer getFragment_id() {
        return fragment_id;
    }

    public void setFragment_id(Integer fragment_id) {
        this.fragment_id = fragment_id;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String atom_signature) {
        this.signature = atom_signature;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getScoreNP() {
        return scoreNP;
    }

    public void setScoreNP(Double scoreNP) {
        this.scoreNP = scoreNP;
    }

    public Double getScoreSM() {
        return scoreSM;
    }

    public void setScoreSM(Double scoreSM) {
        this.scoreSM = scoreSM;
    }
}
