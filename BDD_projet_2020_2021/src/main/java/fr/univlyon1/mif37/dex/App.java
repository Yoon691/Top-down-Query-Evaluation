package fr.univlyon1.mif37.dex;

import fr.univlyon1.mif37.dex.mapping.Atom;
import fr.univlyon1.mif37.dex.mapping.Mapping;


import fr.univlyon1.mif37.dex.mapping.topDown.RecursiveQsqEngine;
import fr.univlyon1.mif37.dex.mapping.topDown.RecursiveQsqEngine;
import fr.univlyon1.mif37.dex.parser.MappingParser;
import fr.univlyon1.mif37.dex.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        MappingParser mp = new MappingParser(App.class.getResourceAsStream("/exemple1.txt"));
        Mapping mapping = mp.mapping();
        RecursiveQsqEngine recursiveQsqEngine = new RecursiveQsqEngine();
        //recursiveQsqEngine.query();
        //RecursiveQsqEngine.QSQRState qsqrState= new RecursiveQsqEngine.QSQRState(Map<Object,Object> unadornedRules)
        //recursiveQsqEngine.QSQRState qsqrState = new RecursiveQsqEngine.QSQRState(Map<Object,Object> unadornedRules);
        LOG.info("\n"+mapping.toString());
        LOG.info("Parsed {} edb(s), {} idb(s) and {} tgd(s).",
                mapping.getEDB().size(),
                mapping.getIDB().size(),
                mapping.getTgds().size()
                );
        for (int i = 0; i < mapping.getTgds().size(); i++){
            recursiveQsqEngine.query(mapping.getTgds().get(i).getRight());
        }

    }
}
