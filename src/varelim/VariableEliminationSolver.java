package varelim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * This code was made by Daan Bergman, s1034115
 */
public class VariableEliminationSolver {

	private Variable query;
	private ArrayList<Variable> observed;
	private ArrayList<Factor> factors;
	private int factorCount = 1;

	/*
	 * Creates a solver. All the tables are converted into factors.
	 */
	public VariableEliminationSolver(Variable query, ArrayList<Variable> observed, ArrayList<Table> network) {
		factors = new ArrayList<>();
		this.query = query;
		this.observed = observed;
		for (Table table : network) {
			factors.add(new Factor(factorCount, table));
			factorCount++;
		}
	}

	/**
	 * Calculates the probability of the query given the observed variables and
	 * network
	 */
	public void solve() {
		observeValues();
		LinkedList<Variable> eliminationOrder = getEliminationOrder();
		System.out.println("\nElimination Order: \n"+eliminationOrder);
		while (!eliminationOrder.isEmpty()) {
			Variable var = eliminationOrder.remove();
			System.out.println("\nEliminating: " + var.getName());
			ArrayList<Factor> toBeJoined = getFactorsWithVariable(var);
			System.out.println("\nFactors that will be multiplied: \n" + toBeJoined);
			Factor joined = new Factor(factorCount, toBeJoined);
			factorCount++;
			System.out.println("\nMultiplication Result: \n" + joined);
			joined.sumOut(var);
			System.out.println("\nSummed out Result: \n"+ joined);
			factors.removeAll(toBeJoined);
			factors.add(joined);
		}
		System.out.println("Elimination process is finished.\nThe remaining factors are: \n" + factors);
		Factor result = new Factor(factorCount, factors);
		System.out.println("Multiplying the remaining factors resulted in: \n" + result);
		result.normalize();
		System.out.println("Normalized result: \n"+result);
		System.out.println(getResult(result));
	}

	/**
	 * If a variable has been observed, a factor with that variable only keeps the
	 * rows containing the observed value.
	 */
	private void observeValues() {
		for (Factor factor : factors) {
			for (Variable var : observed) {
				factor.observe(var);
			}
		}
	}

	/**
	 * Gets the elimination order.
	 * 
	 * @return Variable ordering
	 */
	private LinkedList<Variable> getEliminationOrder() {
		LinkedHashSet<Variable> set = new LinkedHashSet<>();
		for (Factor factor : factors) {
			set.addAll(factor.getVariables());
		}
		set.remove(query);
		return new LinkedList<>(set);
	}

	/**
	 * Gets all the factors that make use of a variable
	 * 
	 * @param var - the variable
	 * @return the factors
	 */
	private ArrayList<Factor> getFactorsWithVariable(Variable var) {
		ArrayList<Factor> factorsWithVar = new ArrayList<>();
		for (Factor factor : factors) {
			if (factor.getVariables().contains(var))
				factorsWithVar.add(factor);
		}
		return factorsWithVar;
	}

	/**
	 * Converts the result into a clear and readable string
	 * 
	 * @param result
	 * @return String
	 */
	public String getResult(Factor result) {
		StringBuilder sb = new StringBuilder();
		sb.append("\nProbability of ").append(query.getName()).append(" given ");
		for (Variable var : observed) {
			sb.append(var.getName() + " = " + var.getObservedValue() + ", ");
		}
		for (ProbRow row : result.getTable()) {
			sb.append("\n" + row);
		}
		return sb.toString();
	}

}
