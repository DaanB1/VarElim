package varelim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This code was made by Daan Bergman, s1034115
 */
public class Factor {

	private int id;
	private ArrayList<Variable> variables;
	private ArrayList<ProbRow> table;

	/**
	 * Creates a factor from a table
	 * @param table
	 */
	public Factor(int id, Table table) {
		this.id = id;
		Variable variable = table.getVariable();
		this.variables = new ArrayList<>();
		this.table = new ArrayList<>();
		variables.add(variable);
		if (variable.hasParents()) {
			variables.addAll(variable.getParents());
		}
		this.table = table.getTable();
	}

	/**
	 * Creates a factor from multiple factors by joining them together
	 */
	public Factor(int id, ArrayList<Factor> factors) {
		this.id = id;
		// If there only is one factor, copy that factor
		if(factors.size() == 1) {
			this.variables = factors.get(0).getVariables();
			this.table = factors.get(0).getTable();
			return;
		}
		
		// Get all variables the product will have
		Set<Variable> _variables = new HashSet<>();
		for (Factor factor : factors) {
			_variables.addAll(factor.getVariables());
		}
		variables = new ArrayList<>(_variables);

		// Calculate the amount of rows the table will have
		int totalRows = 1;
		for (Variable var : variables) {
			totalRows *= var.getNrOfValues();
		}

		table = new ArrayList<>();
		for (int i = 0; i < totalRows; i++) {
			// Generate the value assignments. For example: True True or True False etc.
			ArrayList<String> values = new ArrayList<>();
			for (int j = variables.size() - 1; j >= 0; j--) {
				int nr = variables.get(j).getNrOfValues();
				int index = (i / (int) Math.pow(nr, j)) % nr;
				values.add(variables.get(j).getValues().get(index));
			}
			// Skip rows that have value assignments inconsistent with observed values
			boolean skip = false;
			for(Variable var : variables) {
				if(var.getObserved()) {
					int index = variables.indexOf(var);
					if(!values.get(index).contentEquals(var.getObservedValue())) {
						skip = true;
						break;
					}
				}
			}
			if(skip)
				continue;
			
			// Calculate probability of that assignment
			double prob = 1;
			for (Factor factor : factors) {
				// get all variables in factor
				ArrayList<Variable> vars = factor.getVariables();

				// from the previously generated values, get only the values of those variables
				ArrayList<String> vals = new ArrayList<>();
				for (Variable var : vars) {
					int index = variables.indexOf(var);
					vals.add(values.get(index));
				}

				// look for a row in factor with the same values
				for (ProbRow row : factor.getTable()) {
					if (row.getValues().equals(vals)) {
						//multiply total probability by the probability the row
						prob *= row.getProb();
					}
				}
			}
			// construct new row from variable assignment and probability
			ProbRow row = new ProbRow(values, prob);
			table.add(row);
		}

	}

	/**
	 * Eliminates a variable by summing it out
	 * 
	 * @param variable
	 */
	public void sumOut(Variable variable) {
		//remove variable from table
		int index = variables.indexOf(variable);
		for (ProbRow row : table) {
			row.getValues().remove(index);
		}
		variables.remove(variable);
		
		//add together all rows that now have the same values
		ArrayList<ProbRow> newTable = new ArrayList<>();
		LinkedHashSet<ArrayList<String>> distinctValues = new LinkedHashSet<>();
		for(ProbRow row : table) {
			distinctValues.add(row.getValues());
		}
		for(ArrayList<String> values : distinctValues) {
			double probabilitySum = 0;
			for(ProbRow row : table) {
				if(row.getValues().equals(values)) {
					probabilitySum += row.getProb();
				}
			}
			newTable.add(new ProbRow(values, probabilitySum));
		}
		table = newTable;
	}
	
	/**
	 * Normalizes the table
	 */
	public void normalize() {
		double sum = 0;
		for(ProbRow row : table) {
			sum += row.getProb();
		}
		ArrayList<ProbRow> normalizedTable = new ArrayList<>();
		for(ProbRow row : table) {
			normalizedTable.add(new ProbRow(row.getValues(), row.getProb() / sum));
		}
		table = normalizedTable;
	}

	/**
	 * Removes all ProbRows where the variable is not the observed value
	 * 
	 * @param variable
	 * @param value
	 */
	public void observe(Variable variable) {
		ArrayList<ProbRow> toBeRemoved = new ArrayList<>();
		int index = variables.indexOf(variable);
		if (index < 0)
			return;

		for (ProbRow row : table) {
			if (!row.getValues().get(index).contentEquals(variable.getObservedValue())) {
				toBeRemoved.add(row);
			}
		}
		table.removeAll(toBeRemoved);
	}

	public ArrayList<Variable> getVariables() {
		return variables;
	}
	
	public ArrayList<ProbRow> getTable() {
		return table;
	}
	
	public String getName() {
		StringBuilder sb = new StringBuilder();
		sb.append("Factor"+id+" (");
		for(Variable var : variables) {
			sb.append(var.getName()+",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()+"\n");
		for (ProbRow row : table) {
			sb.append(row+"\n");
		}
		return sb.toString();
	}

}
