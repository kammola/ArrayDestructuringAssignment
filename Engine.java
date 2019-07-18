import java.io.*;
import java.util.*;


public class Engine {


    public class Assignment implements Comparable<Assignment> {
        String type;
        String arr;
        int idx;
        String variable;
        String line;
        String indent;

        public Assignment (String type, String variable, String arr, int idx, String line, String indent) {
            this.type = type;
            this.variable = variable;
            this.arr = arr;
            this.idx = idx;
            this.line = line;
            this.indent = indent;
        }

        public int compareTo (Assignment o) {
            if (idx == o.idx)
                return variable.compareTo(o.variable);
            return idx - o.idx;
        }
    }

    public class AssignmentsArrayPair {
        ArrayList<Assignment> toProcess;
        ArrayList<Assignment>  rest;


        public AssignmentsArrayPair (ArrayList<Assignment>  toProcess, ArrayList<Assignment>  rest) {
            this.toProcess = toProcess;
            this.rest = rest;
        }
    }

    public String destructure (ArrayList<Assignment> assignments) {
        Collections.sort(assignments);

        String type = assignments.get(0).type;
        String arr = assignments.get(0).arr;
        String indent = assignments.get(0).indent;

        int nxt = 0;
        StringBuilder res = new StringBuilder(indent + type + (type.compareTo("") == 0 ? "[": " ["));

        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).idx == nxt) {
                res = res.append(new StringBuilder(assignments.get(i).variable));
                if(i == assignments.size() - 1)
                    res = res.append(new StringBuilder("]"));
                else
                    res = res.append(new StringBuilder(", "));

            } else {
                res= res.append(new StringBuilder(", "));
                i--;
            }

            nxt++;
        }

        return res.append(new StringBuilder(" = " + arr + ";")).toString();
    }

    public AssignmentsArrayPair getReadyAssignments (ArrayList<Assignment> assignments)  {
        if (assignments.size() == 0)
            return new AssignmentsArrayPair(new ArrayList<>(), new ArrayList<>());

        Assignment base = assignments.get(0);
        String type = base.type;
        String arr = base.arr;

        ArrayList<Assignment> toProcess = new ArrayList<>();
        ArrayList<Assignment> rest = new ArrayList<>();

        HashSet<String> variables = new HashSet<>();

        for (int i = 0; i < assignments.size(); i++) {
            Assignment cur = assignments.get(i);
            if(cur.type.compareTo(type) == 0 && cur.arr.compareTo(arr) == 0 && !variables.contains(cur.variable)) {
                toProcess.add(assignments.get(i));
                variables.add(cur.variable);
            } else {
                for (int j = i; j < assignments.size(); j++)
                    rest.add(assignments.get(j));
                break;
            }

        }

        return new AssignmentsArrayPair(toProcess, rest);

    }

    public Assignment parseIntoAssignment (String line) {
        //check if it is ann assignment statement
        if(!line.contains("="))
            return null;
        String indent = "";

        for (int i = 0; i < line.length(); i++) {
            if(line.charAt(i) == ' ')
                indent += " ";
            else
                break;
        }

        String[] parts = line.trim().split("=");
        String typedVariable = parts[0].trim();
        String value = parts[1].replace(';', ' ').trim();

        //check if it is assignment to array element

        if(!value.endsWith("]") || value.startsWith("["))
            return null;

        String type = "";
        String variable = typedVariable;

        if(typedVariable.contains(" ")) {
            String[] tyVr = typedVariable.split(" ");
            type = tyVr[0];
            variable = tyVr[1];
        }

        int openBracketIdx = value.lastIndexOf("[");
        int idx = Integer.parseInt(value.substring(openBracketIdx + 1, value.length() - 1));
        String arr = value.substring(0, openBracketIdx);

        return new Assignment(type, variable, arr, idx, line, indent);
    }

    public String deconstructionAssignment(BufferedReader br) throws IOException {
        StringBuilder res = new StringBuilder("");
        ArrayList<Assignment> assignments = new ArrayList<>();
        StringBuilder tmp = new StringBuilder("");
        while(br.ready()) {
            String curLine = br.readLine();
            if(curLine.startsWith("//") || curLine.compareTo("") == 0) {
                tmp = tmp.append(new StringBuilder(curLine + "\n"));
                continue;
            } else {
                Assignment assignment = parseIntoAssignment(curLine);
                if(assignment == null) {
                    while(true) {
                        AssignmentsArrayPair curPair = getReadyAssignments(assignments);
                        if(curPair.toProcess.size() == 0)
                            break;
                        if(curPair.toProcess.size() == 1)
                            res = res.append(new StringBuilder(curPair.toProcess.get(0).line + "\n"));
                        else
                            res = res.append(new StringBuilder(destructure(curPair.toProcess) + "\n"));

                        assignments = curPair.rest;

                    }
                    if(tmp.toString().compareTo("") != 0)
                        res = res.append(tmp);
                    tmp = new StringBuilder();
                    res = res.append(new StringBuilder(curLine + "\n"));

                } else {
                    assignments.add(assignment);
                }
            }

        }

        while(true) {
            AssignmentsArrayPair curPair = getReadyAssignments(assignments);

            if(curPair.toProcess.size() == 0)
                break;
            if(curPair.toProcess.size() == 1)
                res = res.append(new StringBuilder(curPair.toProcess.get(0).line));
            else
                res = res.append(new StringBuilder(destructure(curPair.toProcess)));

            assignments = curPair.rest;

        }
        if(tmp.toString().compareTo("") != 0)
            res = res.append(tmp);

        return res.toString();
    }


    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Engine engine = new Engine();
        System.out.println("Please enter the file path!");
        while(true) {
            String file = br.readLine();
            BufferedReader brf = null;
            try {
                brf = new BufferedReader(new FileReader(file));
            } catch (Exception e) {
                System.out.println("That is invalid path! Please enter a valid one!");
                continue;
            }

            System.out.println("Please Enter the output file name");
            String outputFile = br.readLine();
            PrintWriter out = new PrintWriter(new File(outputFile));
            out.print(engine.deconstructionAssignment(brf));
            out.flush();
            out.close();
            break;

        }




    }
}
