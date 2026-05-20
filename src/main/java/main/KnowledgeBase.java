package main;

import java.util.*;

public class KnowledgeBase {
    private final Map<String, List<Triple>> sIndex = new HashMap<>();
    private final Map<String, List<Triple>> pIndex = new HashMap<>();
    private final Map<String, List<Triple>> oIndex = new HashMap<>();
    private final Random random = new Random();

    public void addTriple(String s, String p, String o) {
        Triple t = new Triple(s, p, o);
        sIndex.computeIfAbsent(s, k -> new ArrayList<>()).add(t);
        pIndex.computeIfAbsent(p, k -> new ArrayList<>()).add(t);
        oIndex.computeIfAbsent(o, k -> new ArrayList<>()).add(t);
    }

    public String query(String s, String p, String o) {
        int unknowns = 0;
        if ("@UNKNOWN@".equals(s)) unknowns++;
        if ("@UNKNOWN@".equals(p)) unknowns++;
        if ("@UNKNOWN@".equals(o)) unknowns++;

        if (unknowns == 0) {
            return verify(s, p, o) ? "@TRUE@" : "@FALSE@";
        } 
        
        if (unknowns == 1) {
            return findSingle(s, p, o);
        }

        // DISCOVERY MODE: 2 or 3 unknowns
        return discoverRandom(s, p, o);
    }

    private String findSingle(String s, String p, String o) {
        if ("@UNKNOWN@".equals(s)) {
            List<Triple> candidates = oIndex.getOrDefault(o, Collections.emptyList());
            for (Triple t : candidates) if (t.predicate().equals(p)) return t.subject();
        }
        if ("@UNKNOWN@".equals(p)) {
            List<Triple> candidates = sIndex.getOrDefault(s, Collections.emptyList());
            for (Triple t : candidates) if (t.object().equals(o)) return t.predicate();
        }
        if ("@UNKNOWN@".equals(o)) {
            List<Triple> candidates = sIndex.getOrDefault(s, Collections.emptyList());
            for (Triple t : candidates) if (t.predicate().equals(p)) return t.object();
        }
        return null;
    }

    private String discoverRandom(String s, String p, String o) {
        List<Triple> candidates;
        
        if (!"@UNKNOWN@".equals(s)) {
            candidates = sIndex.getOrDefault(s, Collections.emptyList());
        } else if (!"@UNKNOWN@".equals(p)) {
            candidates = pIndex.getOrDefault(p, Collections.emptyList());
        } else if (!"@UNKNOWN@".equals(o)) {
            candidates = oIndex.getOrDefault(o, Collections.emptyList());
        } else {
            // ALL THREE are unknown: pick any random triple from the entire database
            if (sIndex.isEmpty()) return null;
            String randomSubj = new ArrayList<>(sIndex.keySet()).get(random.nextInt(sIndex.size()));
            candidates = sIndex.get(randomSubj);
        }

        if (candidates.isEmpty()) return null;

        // Pick a random triple from the filtered list
        Triple randomTriple = candidates.get(random.nextInt(candidates.size()));
        
        // Since we can't return 2 missing values in one String, 
        // we return the entire triple as a confirmed fact.
        return randomTriple.toString(); 
    }

    public boolean verify(String s, String p, String o) {
        List<Triple> candidates = sIndex.getOrDefault(s, Collections.emptyList());
        for (Triple t : candidates) {
            if (t.predicate().equals(p) && t.object().equals(o)) return true;
        }
        return false;
    }
}