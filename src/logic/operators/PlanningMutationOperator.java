/**
 * 
 */
package logic.operators;

import java.util.List;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import entities.Employee;
import entities.PlannedTask;
import logic.NextReleaseProblem;
import logic.PlanningSolution;

/**
 * @author Vavou
 *
 */
public class PlanningMutationOperator implements MutationOperator<PlanningSolution> {

	/* --- Attributes --- */
	
	/**
	 * Generated Id
	 */
	private static final long serialVersionUID = 6178932989907368331L;
	
	/**
	 * The number of tasks of the problem
	 */
	private int numberOfTasks;

	/**
	 * Mutation probability between 0.0 and 1.0
	 */
	private double mutationProbability;

	/**
	 * Random generator
	 */
	private JMetalRandom randomGenerator;

	/**
	 * The Next Release Problem which contents the employees and tasks list
	 */
	private NextReleaseProblem problem;

	/* --- Getters and setters --- */
	
	/**
	 * @return the mutationProbability
	 */
	public double getMutationProbability() {
		return mutationProbability;
	}
	
	/* --- Constructors */
	
	/**
	 * Constructor
	 * @param problem The problem
	 * @param mutationProbability The mutation probability between 0.0 and 1.0
	 */
	public PlanningMutationOperator(NextReleaseProblem problem, double mutationProbability) {
		if (mutationProbability < 0) {
			throw new JMetalException("Mutation probability is negative: " + mutationProbability) ;
		}
		
		this.numberOfTasks = problem.getTasks().size();
		this.mutationProbability = mutationProbability;
		this.problem = problem;
		randomGenerator = JMetalRandom.getInstance() ;
	}
	
	/* --- Methods --- */
	
	@Override
	public PlanningSolution execute(PlanningSolution parent) {
		PlanningSolution child = new PlanningSolution(parent);
		int nbPlannedTasks = child.getNumberOfPlannedTasks();
		
		for (int i = 0 ; i < nbPlannedTasks ; i++) {
			if (doMutation()) { // If we have to do a mutation
				PlannedTask taskToMutate = child.getPlannedTask(i);
				if (randomGenerator.nextDouble() < 0.5) {
					changeEmployee(taskToMutate);
				}
				else {
					changeTask(child, taskToMutate, i);
				}
			}
		}
		
		for (int i = nbPlannedTasks ; i < problem.getTasks().size() ; i++) {
			if (doMutation()) {
				addNewTask(child);
			}
		}
		
		return child;
	}
	
	/**
	 * Defines if we do or not the mutation
	 * It randomly chose a number and checks if it is lower than the mutation probability
	 * @return true if the mutation must be done
	 */
	private boolean doMutation() {
		return randomGenerator.nextDouble() <= mutationProbability;
	}

	/**
	 * Add an random unplanned task to the planning
	 * - chose randomly an unplanned task
	 * - remove it from the unplanned tasks list of the solution
	 * - chose randomly an employee
	 * - create and add the planned task with the chosen task and employee
	 * @param solution the solution to mutate
	 */
	private void addNewTask(PlanningSolution solution) {
		solution.scheduleRandomTask();
	}
	
	/**
	 * Replaces a task by another one.
	 * It can be a planned or an unplanned task, it updates the unplannedTasks list in the second case
	 * @param solution The solution to mutate
	 * @param taskToChange The planned task to modify
	 * @param taskPosition The position of the task to modify in the planning (the plannedTask list)
	 */
	private void changeTask(PlanningSolution solution, PlannedTask taskToChange, int taskPosition) {
		int randomPosition = randomGenerator.nextInt(0, numberOfTasks);
		if (randomPosition < solution.getNumberOfPlannedTasks() - 1) { // If the random selected task is already planned then exchange with the current
			if (taskPosition == randomPosition) { 
				randomPosition++; // If problem then apply a % (modulo) size
			}
			solution.exchange(taskPosition, randomPosition);
		}
		else { // If the random selected task is not yet planned, let's do it
			solution.unschedule(taskToChange);
			solution.scheduleRandomTask(taskPosition);
		}
	}
	
	/**
	 * Change the employee of a planned task by a random one
	 * @param taskToChange the planned task to modify
	 */
	private void changeEmployee(PlannedTask taskToChange) {
		List<Employee> skilledEmployees = problem.getEmployees(taskToChange.getTask().getRequiredSkills().get(0));
		if (skilledEmployees.size() > 1) {
			taskToChange.setEmployee(skilledEmployees.get(randomGenerator.nextInt(0, skilledEmployees.size()-1)));
		}
	}
}
