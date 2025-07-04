package com.lmh.coverage.utils;

import com.lmh.coverage.entity.Edge;
import com.lmh.coverage.entity.Path;
import com.lmh.coverage.entity.transitionDefine;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.*;

import static dk.brics.automaton.Transition.appendCharString;

public class myBasicOperations {
    public static Automaton myComplement(Automaton attemptAutomaton){

        State dead = new State();
        Set<State> attemptAutomatonStates = attemptAutomaton.getStates();
        Set<State> states = attemptAutomaton.getStates();
        Iterator<State> stateIterator = states.iterator();

        Set<transitionDefine> transitions = new HashSet<>();

        while (stateIterator.hasNext()){
            State nextState = stateIterator.next();
            for (Transition transition : nextState.getTransitions()) {
                transitionDefine transitionDefine = new transitionDefine(transition);
                transitions.add(transitionDefine);
            }
        }

        Set<transitionDefine> unionDefine = transitionDefine.getUnionDefine(transitions);

        stateIterator = attemptAutomatonStates.iterator();
        while (stateIterator.hasNext()){
            State nextState = stateIterator.next();

            for (transitionDefine next : unionDefine) {

                char minChar = '\u0000';
                char maxChar = '\u0000';
                if (next.getMax() == next.getMin()) {
                    if (!nextState.haveStep(next.getMin())) {
                        minChar = maxChar = next.getMin();
                    }
                } else {
                    for (int i = 0; next.getMin() + i <= next.getMax(); i++) {
                        if (!nextState.haveStep((char) (next.getMin() + i))) {
                            if (minChar == '\u0000') {
                                minChar = (char) (next.getMin() + i);
                            } else {
                                maxChar = (char) (next.getMin() + i);
                            }
                        }
                    }
                }
                if (minChar != '\u0000' && maxChar != '\u0000') {
                    Transition deadTransition = new Transition(minChar, maxChar, dead);
                    nextState.addTransition(deadTransition);
                }else if(minChar != '\u0000'){
                    Transition deadTransition = new Transition(minChar, dead);
                    nextState.addTransition(deadTransition);
                }

            }
        }

        for (State p : attemptAutomaton.getStates())
            p.setAccept(!p.isAccept());

        for (transitionDefine next : unionDefine) {
            dead.addTransition(new Transition(next.getMin(), next.getMax(), dead));
        }

        Automaton.setStateNumbers(attemptAutomaton.getStates());

        return attemptAutomaton;
    }

    public static List<Integer> dijkstraToSource(int begin, int end, int[][] adjMatrix) {

        int numNodes = adjMatrix.length;


        int[] distances = new int[numNodes];

        for (int i = 0; i < numNodes; i++) {
            distances[i] = Integer.MAX_VALUE;
        }
        distances[begin] = 0;


        boolean[] visited = new boolean[numNodes];

        Map<Integer, List<Integer>> paths = new HashMap<>();
        for (int i = 0; i < numNodes; i++) {
            paths.put(i, new ArrayList<>());
        }

        for (int i = 0; i < numNodes; i++) {
            int minNode = -1;
            int minDistance = Integer.MAX_VALUE;

            for (int j = 0; j < numNodes; j++) {
                if (!visited[j] && distances[j] < minDistance) {
                    minNode = j;
                    minDistance = distances[j];
                }
            }

            if (minNode == -1) {
                break;
            }

            visited[minNode] = true;

            for (int j = 0; j < numNodes; j++) {
                if (adjMatrix[minNode][j] > 0) {
                    int newDistance = distances[minNode] + adjMatrix[minNode][j];
                    if (newDistance < distances[j]) {
                        distances[j] = newDistance;
                        paths.get(j).clear();
                        paths.get(j).addAll(paths.get(minNode));
                        paths.get(j).add(minNode);
                    }
                }
            }
        }

        return paths.get(end);
    }

    public static List<Integer> dijkstraToAccept(int[][] adjMatrix, boolean[] isAcceptState, int source) {
        int numNodes = adjMatrix.length;


        int[] distances = new int[numNodes];

        for (int i = 0; i < numNodes; i++) {
            distances[i] = Integer.MAX_VALUE;
        }
        distances[source] = 0;

        boolean[] visited = new boolean[numNodes];

        Map<Integer, List<Integer>> paths = new HashMap<>();
        for (int i = 0; i < numNodes; i++) {
            paths.put(i, new ArrayList<>());
        }

        for (int i = 0; i < numNodes; i++) {
            int minNode = -1;
            int minDistance = Integer.MAX_VALUE;

            for (int j = 0; j < numNodes; j++) {
                if (!visited[j] && distances[j] < minDistance) {
                    minNode = j;
                    minDistance = distances[j];
                }
            }

            if (minNode == -1) {
                break;
            }

            visited[minNode] = true;

            for (int j = 0; j < numNodes; j++) {
                if (adjMatrix[minNode][j] > 0) {
                    int newDistance = distances[minNode] + adjMatrix[minNode][j];
                    if (newDistance < distances[j]) {
                        distances[j] = newDistance;
                        paths.get(j).clear();
                        paths.get(j).addAll(paths.get(minNode));
                        paths.get(j).add(minNode);
                    }
                }
            }
        }

        if (isAcceptState[source]) {
            return List.of(source);
        }

        for (int i = 0; i < numNodes; i++) {
            if (isAcceptState[i] && distances[i]!= Integer.MAX_VALUE) {
                List<Integer> path = paths.get(i);
                path.add(i);
                return path;
            }
        }

        return new ArrayList<>();
    }

    public static int[][] automatonToG(Automaton automaton) {
        Set<State> states = automaton.getStates();
        int size = states.size();
        int[][] G = new int[size][size];
        Iterator<State> stateIterator = states.iterator();
        while (stateIterator.hasNext()){
            State state = stateIterator.next();
            int number = state.getNumber();
            Set<Transition> transitions = state.getTransitions();
            for(Transition transition:transitions){
                G[number][transition.getDest().getNumber()] = 1;
            }
        }
        return G;
    }

    public static boolean[] getAcceptState(Automaton automaton){
        Set<State> states = automaton.getStates();
        int size = states.size();
        boolean[] isAcceptState = new boolean[size];
        Iterator<State> stateIterator = states.iterator();
        while (stateIterator.hasNext()){
            State state = stateIterator.next();
            if(state.isAccept())
                isAcceptState[state.getNumber()] = true;
        }
        return isAcceptState;
    }

    public static HashSet<String> boardProcess(LinkedList<String> generatedStrings) {
        HashSet<String> stringsSet = new HashSet<>();
        int flag = 0;
        while (flag <= 1){
            for(String s:generatedStrings){
                if(s.equals("ε")){
                    stringsSet.add("");
                    continue;
                }
                StringBuilder stringBuilder = getStringBuilder(s, flag);
                stringsSet.add(String.valueOf(stringBuilder.reverse()));
            }
            flag++;
        }

        return stringsSet;
    }

    public static LinkedList<String> generateStrings(LinkedList<Path> coverList) {
        LinkedList<String> strings = new LinkedList<>();
        for(Path p:coverList){
            LinkedList<Transition> transitions = p.getTransitions();
            if(transitions.isEmpty()){
                continue;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (Transition t:transitions){
                stringBuilder.append(t.getMin());
                if (t.getMin() != t.getMax()) {
                    stringBuilder.append("~");
                    appendCharString(t.getMax(), stringBuilder);
                }
            }
            strings.add(stringBuilder.toString());
        }
        return strings;
    }

    public static void extendEPC(LinkedList<Path> EPC, Automaton automaton) {
        int[][] G = automatonToG(automaton);

        boolean[] isAcceptState = getAcceptState(automaton);
        Iterator<Path> iterator = EPC.iterator();
        while(iterator.hasNext()){
            Path path = iterator.next();
            int j = EPC.indexOf(path);
            LinkedList<Transition> transitions = path.getTransitions();
            if(transitions.isEmpty())
                continue;
            int initial = automaton.getInitialState().getNumber();
            int end = transitions.getLast().getDest().getNumber();
            if(path.getInitial() != initial){

                Path newPath = new Path(initial);
                List<Integer> shortestPath = dijkstraToSource(initial, path.getInitial(), G);
                shortestPath.add(path.getInitial());
                for (int i = 1; i < shortestPath.size(); i++) {
                    State firstState = automaton.getStateByNumber(shortestPath.get(i - 1));
                    Transition transition = firstState.getTransitionByDest(shortestPath.get(i));
                    if(transition != null)
                        newPath.push(transition);
                    else
                        throw new RuntimeException("error");
                }
                newPath.add(path);
                EPC.set(j, newPath);
            }
            if(!isAcceptState[end]){
                Path newPath = EPC.get(j).deepClone();
                List<Integer> pathToAccept = dijkstraToAccept(G, isAcceptState, end);
                if(!pathToAccept.isEmpty()){
                    Integer lastNumber = pathToAccept.get(0);
                    pathToAccept.remove(0);
                    for(Integer dest:pathToAccept){
                        State lastState = automaton.getStateByNumber(lastNumber);
                        if(lastState == null) {
                            throw new RuntimeException("error");
                        }
                        else {
                            Transition transition = lastState.getTransitionByDest(dest);
                            if(transition != null)
                                newPath.push(transition);
                            else
                                throw new RuntimeException("error");
                            lastNumber = dest;
                        }
                    }
                    EPC.set(j,newPath);
                }else{

                    LinkedList<Transition> newPathTransitions = newPath.getTransitions();
                    while (!isAcceptState[end]){
                        newPath.pop();
                        if(!newPathTransitions.isEmpty())
                            end = newPathTransitions.getLast().getDest().getNumber();
                        else
                            break;
                    }
                    if(!newPathTransitions.isEmpty() || isAcceptState[newPath.getInitial()])
                        EPC.set(j,newPath);
                    else
                        iterator.remove();
                }
            }

        }

    }

    /*public static void acceptExtendEPC(LinkedList<Path> EPC, Automaton automaton) {

        Set<Integer> acceptStatesNumber = new HashSet<>();
        Set<State> acceptStates = automaton.getAcceptStates();
        for (State acceptState : acceptStates)
            acceptStatesNumber.add(acceptState.getNumber());

        Set<Integer> acceptedStatesNumber = new HashSet<>();
        for(Path path:EPC){
            LinkedList<Transition> transitions = path.getTransitions();
            if(transitions.isEmpty())
                continue;
            State dest = transitions.getLast().getDest();
            acceptedStatesNumber.add(dest.getNumber());
        }

        LinkedList<Path> addList = new LinkedList<>();
        for(Path p:EPC){
            LinkedList<Transition> transitions = p.getTransitions();
            Path clone = p.deepClone();
            for(int j = transitions.size()-1;j>=0;j--){
                Transition t = transitions.get(j);
                int number = t.getDest().getNumber();
                if(acceptStatesNumber.contains(number)){
                    acceptedStatesNumber.add(number);
                    addList.add(clone.deepClone());
                }
                clone.pop();
            }
            if(clone.getTransitions().isEmpty() && !acceptedStatesNumber.contains(clone.getInitial()) && acceptStatesNumber.contains(clone.getInitial())){
                acceptedStatesNumber.add(clone.getInitial());
                addList.add(clone);
            }
        }
        EPC.addAll(addList);
    }*/

    public static Map<State, LinkedList<Edge>> generateEdgeMap(Automaton automaton){
        Map<State, LinkedList<Edge>> form = new HashMap<>();

        Set<State> states = automaton.getStates();

        for (State firstState : states) {
            LinkedList<Edge> edgePairs = new LinkedList<>();

            for (Transition transition : firstState.getTransitions()) {
                edgePairs.add(new Edge(transition,false));
            }

            form.put(firstState, edgePairs);
        }

        return form;
    }

    private static StringBuilder getStringBuilder(String s, int flag) {
        Stack<Character> stack = new Stack<>();
        char[] charArray = s.toCharArray();
        for(int i = 0;i<charArray.length;i++){
            if(charArray[i] == '~'){
                if(flag == 0){
                    ++i;
                }else {
                    if(!stack.isEmpty()){
                        stack.pop();
                        stack.push(charArray[++i]);
                    }
                }
            }else
                stack.push(charArray[i]);
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (!stack.isEmpty()){
            stringBuilder.append(stack.pop());
        }
        return stringBuilder;
    }

}
