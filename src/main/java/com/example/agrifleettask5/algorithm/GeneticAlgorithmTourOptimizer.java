package com.example.agrifleettask5.algorithm;

import com.example.agrifleettask5.model.TourSolution;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@Component
public class GeneticAlgorithmTourOptimizer {
    private static final Random random = new Random();
    private static final int DEFAULT_POPULATION_SIZE = 100;
    private static final int DEFAULT_GENERATIONS = 200;
    private static final double DEFAULT_MUTATION_RATE = 0.02;
    private static final double DEFAULT_ELITE_RATE = 0.1;

    public TourSolution optimize(double[][] distanceMatrix, boolean returnToDepot) {
        return optimize(distanceMatrix, returnToDepot, DEFAULT_POPULATION_SIZE,
                DEFAULT_GENERATIONS, DEFAULT_MUTATION_RATE);
    }

    public TourSolution optimize(double[][] distanceMatrix, boolean returnToDepot,
                                  int populationSize, int generations, double mutationRate) {
        if (distanceMatrix == null || distanceMatrix.length < 2) {
            return new TourSolution(new int[0], 0.0, "GA", false,
                    "O(generations * popSize * n^2)", "O(popSize * n)", 0);
        }

        int numFarms = distanceMatrix.length - 1; // Exclude depot
        long startedAt = System.nanoTime();

        // Initialize population
        List<int[]> population = initializePopulation(numFarms, populationSize);

        // Evolve for specified generations
        for (int generation = 0; generation < generations; generation++) {
            // Evaluate fitness
            double[] fitnesses = evaluatePopulation(population, distanceMatrix, returnToDepot);

            // Selection and breeding
            List<int[]> newPopulation = new ArrayList<>();
            int eliteCount = Math.max(1, (int) (populationSize * DEFAULT_ELITE_RATE));

            // Preserve elite
            for (int i = 0; i < eliteCount; i++) {
                int bestIdx = findBestIdx(fitnesses);
                newPopulation.add(population.get(bestIdx).clone());
                fitnesses[bestIdx] = Double.POSITIVE_INFINITY; // Mark as used
            }

            // Restore fitness values for remaining selection
            fitnesses = evaluatePopulation(population, distanceMatrix, returnToDepot);

            // Fill rest of population via tournament selection + crossover + mutation
            while (newPopulation.size() < populationSize) {
                int[] parent1 = tournamentSelect(population, fitnesses);
                int[] parent2 = tournamentSelect(population, fitnesses);
                int[] child = orderedCrossover(parent1, parent2);
                mutate(child, mutationRate);
                newPopulation.add(child);
            }

            population = newPopulation;
        }

        // Find best solution
        double[] fitnesses = evaluatePopulation(population, distanceMatrix, returnToDepot);
        int bestIdx = findBestIdx(fitnesses);
        int[] bestTour = population.get(bestIdx);
        double bestDistance = calculateTourDistance(bestTour, distanceMatrix, returnToDepot);

        long elapsed = System.nanoTime() - startedAt;

        return new TourSolution(bestTour, bestDistance, "GA", false,
                "O(generations * popSize * n^2)", "O(popSize * n)", elapsed);
    }

    private List<int[]> initializePopulation(int numFarms, int populationSize) {
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(randomTour(numFarms));
        }
        return population;
    }

    private int[] randomTour(int numFarms) {
        int[] tour = new int[numFarms];
        for (int i = 0; i < numFarms; i++) {
            tour[i] = i;
        }
        // Fisher-Yates shuffle
        for (int i = numFarms - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = tour[i];
            tour[i] = tour[j];
            tour[j] = temp;
        }
        return tour;
    }

    private double[] evaluatePopulation(List<int[]> population, double[][] distanceMatrix,
                                         boolean returnToDepot) {
        double[] fitnesses = new double[population.size()];
        for (int i = 0; i < population.size(); i++) {
            double distance = calculateTourDistance(population.get(i), distanceMatrix, returnToDepot);
            fitnesses[i] = -distance; // Negative because we maximize fitness
        }
        return fitnesses;
    }

    private double calculateTourDistance(int[] tour, double[][] distanceMatrix, boolean returnToDepot) {
        if (tour == null || tour.length == 0) {
            return 0.0;
        }
        double distance = distanceMatrix[0][tour[0] + 1]; // Depot to first farm
        for (int i = 0; i < tour.length - 1; i++) {
            distance += distanceMatrix[tour[i] + 1][tour[i + 1] + 1]; // Farm to farm
        }
        if (returnToDepot) {
            distance += distanceMatrix[tour[tour.length - 1] + 1][0]; // Last farm to depot
        }
        return distance;
    }

    private int findBestIdx(double[] fitnesses) {
        int bestIdx = 0;
        for (int i = 1; i < fitnesses.length; i++) {
            if (fitnesses[i] > fitnesses[bestIdx]) {
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private int[] tournamentSelect(List<int[]> population, double[] fitnesses) {
        int tournamentSize = 5;
        int bestIdx = random.nextInt(population.size());
        for (int i = 1; i < tournamentSize; i++) {
            int idx = random.nextInt(population.size());
            if (fitnesses[idx] > fitnesses[bestIdx]) {
                bestIdx = idx;
            }
        }
        return population.get(bestIdx);
    }

    private int[] orderedCrossover(int[] parent1, int[] parent2) {
        int n = parent1.length;
        int[] child = new int[n];
        Arrays.fill(child, -1);

        // Select random segment from parent1
        int start = random.nextInt(n);
        int end = random.nextInt(n);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // Copy segment from parent1
        for (int i = start; i <= end; i++) {
            child[i] = parent1[i];
        }

        // Fill remaining positions with parent2 values in order
        int childPos = (end + 1) % n;
        for (int i = 0; i < n; i++) {
            int parent2Val = parent2[(end + 1 + i) % n];
            boolean alreadyUsed = false;
            for (int j = start; j <= end; j++) {
                if (child[j] == parent2Val) {
                    alreadyUsed = true;
                    break;
                }
            }
            if (!alreadyUsed) {
                child[childPos] = parent2Val;
                childPos = (childPos + 1) % n;
            }
        }

        return child;
    }

    private void mutate(int[] tour, double mutationRate) {
        for (int i = 0; i < tour.length; i++) {
            if (random.nextDouble() < mutationRate) {
                int j = random.nextInt(tour.length);
                int temp = tour[i];
                tour[i] = tour[j];
                tour[j] = temp;
            }
        }
    }
}
