package de.unijena.cheminf.npdatabasefiller.services;


import com.google.common.collect.Lists;
import de.unijena.cheminf.npdatabasefiller.misc.BeanUtil;
import de.unijena.cheminf.npdatabasefiller.misc.FragmentCreationTask;
import de.unijena.cheminf.npdatabasefiller.misc.LinearSugars;
import de.unijena.cheminf.npdatabasefiller.model.*;
import de.unijena.cheminf.npdatabasefiller.misc.MoleculeConnectivityChecker;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.BondManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author mSorok
 */


@Service
public class FragmentsCalculatorService {

    @Autowired
    MoleculeRepository mr;

    @Autowired
    FragmentWithSugarRepository fr;

    @Autowired
    FragmentWithoutSugarRepository fro;

    @Autowired
    MoleculeFragmentCpdRepository cpdRepository;


    @Autowired
    AtomContainerToMoleculeService atomContainerToMoleculeService;





    ElectronDonation model = ElectronDonation.cdk();
    CycleFinder cycles = Cycles.cdkAromaticSet();
    Aromaticity aromaticity = new Aromaticity(model, cycles);


    private final LinearSugars linearSugarChains = LinearSugars.getInstance();




    public void doWork( Integer numberOfThreads){

        int height = 2;
        try {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);


        List<Molecule> allMolecules = mr.findAll();

        List<List<Molecule>> moleculeListBatch = Lists.partition(allMolecules, 1000);

        int taskcount = 0;


        List<Callable<Object>> todo = new ArrayList<Callable<Object>>(moleculeListBatch.size());

        System.out.println("Total number of tasks:" + moleculeListBatch.size());

        for(List<Molecule> molBatch : moleculeListBatch){
            FragmentCreationTask task = new FragmentCreationTask();
            task.getMoleculesToCompute(molBatch);

            taskcount++;

            System.out.println("Task "+taskcount+" created");
            task.taskid=taskcount;

            todo.add(Executors.callable(task));

            //executor.execute(task);

            //System.out.println("Task "+taskcount+" executing");

        }


            executor.invokeAll(todo);

            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        //find non-unique sugar fragment strings
        // eliminate duplicates
        // curate molecule_fragment_cpd for ids

        for(Object[] obj : fr.findRedundantSignatures(height)){

            String signature = obj[0].toString();
            String stringListOfIds = obj[1].toString();

            String [] listOfIds = stringListOfIds.split(" ");
            ArrayList<Integer> intListOfIds = new ArrayList<>();
            for(String id : listOfIds){ intListOfIds.add(Integer.parseInt(id)); }

            int minId = Collections.min(intListOfIds);
            for(int id:intListOfIds){
                if(id != minId) {
                    //delete by id
                    fr.deleteById(id);
                    //retrieve the cpd - modify - save
                    List<MoleculeFragmentCpd> mfcList = cpdRepository.findByfragment_id(id);
                    for(MoleculeFragmentCpd mfc : mfcList ){
                        mfc.setFragment_id(minId);
                        cpdRepository.save(mfc);
                    }
                }
            }

        }


        //find non-unique sugar-free fragment strings
        // eliminate duplicates
        // curate molecule_fragment_cpd for ids


        // curation of the DB for redundant fragments - take min ID for same fingerprint (and same height) - replace in in molecule_fragment_cpd - remove from fragments all fragments without cpd to a molecule



    }






    public void doWork(){



        List<Molecule> allMolecules = mr.findAll();

        System.out.println("Number of molecules to process: " + allMolecules.size());
        System.out.println("Starting fragment calculation");
        System.out.print("0% ....");

        int count = 1;


        for(Molecule molecule : allMolecules) {


            IAtomContainer ac = atomContainerToMoleculeService.createAtomContainer(molecule);


            /**
             * Filling for molecules without sugar removal
             */
            int height = 2;
            List<String> allFragments = generateAtomSignatures(ac, height);

            for (String f : allFragments) {

                List<FragmentWithSugar> inDBlist = fr.findBySignatureAndHeight(f, height);
                if(inDBlist.isEmpty()){

                    FragmentWithSugar newFragment = new FragmentWithSugar();
                    newFragment.setSignature(f);
                    newFragment.setHeight(height);

                    newFragment = fr.save(newFragment);

                    MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                    mfc.setMol_id(molecule.getId());
                    mfc.setFragment_id(newFragment.getFragment_id());
                    mfc.setHeight(height);
                    mfc.setComputed_with_sugar(1);

                    cpdRepository.save(mfc);


                } else {
                    FragmentWithSugar inDB = inDBlist.get(0);
                    MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                    mfc.setMol_id(molecule.getId());
                    mfc.setFragment_id(inDB.getFragment_id());
                    mfc.setHeight(height);
                    mfc.setComputed_with_sugar(1);


                    cpdRepository.save(mfc);
                }


            }



            /**
             * Filling for molecules after sugar removal
             */
            IAtomContainer sugarlessMolecule = removeSugars(ac);
            if (sugarlessMolecule == null) {
                molecule.setContainsSugar(0);
                molecule = mr.save(molecule);

            } else {
                // run the fragments computation on it!
                allFragments = generateAtomSignatures(sugarlessMolecule, height);

                if(allFragments != null){

                for (String f : allFragments) {

                     List<FragmentWithoutSugar> inDBlist = fro.findBySignatureAndHeight(f, height);
                    if(inDBlist.isEmpty()){
                        FragmentWithoutSugar newFragment = new FragmentWithoutSugar();
                        newFragment.setSignature(f);
                        newFragment.setHeight(height);

                        newFragment = fro.save(newFragment);

                        MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                        mfc.setMol_id(molecule.getId());
                        mfc.setFragment_id(newFragment.getFragment_id());
                        mfc.setHeight(height);
                        mfc.setComputed_with_sugar(0);

                        cpdRepository.save(mfc);

                    } else {
                        FragmentWithoutSugar inDB = inDBlist.get(0);
                        MoleculeFragmentCpd mfc = new MoleculeFragmentCpd();
                        mfc.setMol_id(molecule.getId());
                        mfc.setFragment_id(inDB.getFragment_id());
                        mfc.setHeight(height);
                        mfc.setComputed_with_sugar(0);
                        cpdRepository.save(mfc);
                    }


                }
            }

            }

            count++;

            if(count == (int)Math.round( (double)allMolecules.size()*0.75 ) ){
                System.out.print(". 75% ....");
            }
            else if(count == (int)Math.round( (double)allMolecules.size()*0.50 )) {
                System.out.print(". 50% ....");
            }
            else if(count == (int)Math.round( (double)allMolecules.size()*0.25 )) {
                System.out.print(". 25% ....");
            }

/*
            if(count%1000==0){
                System.out.println(count+" processed");
            }

*/

        }
        System.out.print(". 100%\n");
        System.out.println("All fragments calculated and injected in database!");

    }







    public List<String> generateAtomSignatures(IAtomContainer atomContainer, Integer height) {

        List<String> atomSignatures = new ArrayList<>();



        //atomContainer = calculateAromaticity(atomContainer);

        if(!atomContainer.isEmpty()) {

            for (IAtom atom : atomContainer.atoms()) {
                try {
                    AtomSignature atomSignature = new AtomSignature(atom, height, atomContainer);
                    atomSignatures.add(atomSignature.toCanonicalString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return atomSignatures;
        }
        else{
            return null;
        }
    }




    private IAtomContainer calculateAromaticity(IAtomContainer molecule) {

        try {
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
            aromaticity.apply(molecule);
            molecule.setProperty("AROMATICITY_PERCEIVED", true);
        } catch (CDKException | NullPointerException e) {
            e.printStackTrace();
        }


        return molecule;

    }





    private IAtomContainer removeSugars(IAtomContainer molecule){

        try {

            IRingSet ringset = Cycles.sssr(molecule).toRingSet();

            // RING SUGARS
            for (IAtomContainer one_ring : ringset.atomContainers()) {
                try {
                    IMolecularFormula molecularFormula = MolecularFormulaManipulator.getMolecularFormula(one_ring);
                    String formula = MolecularFormulaManipulator.getString(molecularFormula);
                    IBond.Order bondorder = AtomContainerManipulator.getMaximumBondOrder(one_ring);

                    if (formula.equals("C5O") | formula.equals("C4O") | formula.equals("C6O")) {
                        if (IBond.Order.SINGLE.equals(bondorder)) {
                            if (shouldRemoveRing(one_ring, molecule, ringset) == true) {
                                for (IAtom atom : one_ring.atoms()) {
                                    {

                                        molecule.removeAtom(atom);
                                    }
                                }
                            }

                        }
                    }
                }catch(NullPointerException e){
                    return null;
                }
            }
            Map<Object, Object> properties = molecule.getProperties();
            IAtomContainerSet molset = ConnectivityChecker.partitionIntoMolecules(molecule);
            for (int i = 0; i < molset.getAtomContainerCount(); i++) {
                molset.getAtomContainer(i).setProperties(properties);
                int size = molset.getAtomContainer(i).getBondCount();
                if (size >= 5) {
                    if (!linearSugarChains.hasSugarChains(molset.getAtomContainer(i), ringset.getAtomContainerCount())) {

                        return (IAtomContainer) molset.getAtomContainer(i);
                    }
                }
            }
            //
        } catch (NullPointerException e) {
        } catch (CDKException e) {
        }
        return null;

    }





    private boolean shouldRemoveRing(IAtomContainer possibleSugarRing, IAtomContainer molecule, IRingSet sugarRingsSet) {

        boolean shouldRemoveRing = false;
        List<IAtom> allConnectedAtoms = new ArrayList<IAtom>();
        List<IBond> bonds = new ArrayList<IBond>();
        int oxygenAtomCount = 0;

        IRingSet connectedRings = sugarRingsSet.getConnectedRings((IRing) possibleSugarRing);

        /*
         * get bonds to check for bond order of connected atoms in a sugar ring
         *
         */
        for (IAtom atom : possibleSugarRing.atoms()) {
            bonds.addAll(molecule.getConnectedBondsList(atom));
        }

        if (IBond.Order.SINGLE.equals(BondManipulator.getMaximumBondOrder(bonds))
                && connectedRings.getAtomContainerCount() == 0) {

            /*
             * get connected atoms of all atoms in sugar ring to check for glycoside bond
             */
            for (IAtom atom : possibleSugarRing.atoms()) {
                List<IAtom> connectedAtoms = molecule.getConnectedAtomsList(atom);
                allConnectedAtoms.addAll(connectedAtoms);
            }

            for (IAtom connected_atom : allConnectedAtoms) {
                if (!possibleSugarRing.contains(connected_atom)) {
                    if (connected_atom.getSymbol().matches("O")) {
                        oxygenAtomCount++;
                    }
                }
            }
            if (oxygenAtomCount > 0) {
                return true;
            }
        }
        return shouldRemoveRing;
    }





}